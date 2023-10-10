package refactoring.step_by_step;

import bpmn.graph.Node;
import constants.PrintLevel;
import other.Utils;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import static main.Main.PRINT_LEVEL;

public class DependenciesCorrector
{
	private final HashSet<Dependency> originalDependencies;
	private final Node nodeToRemove;
	private final DependencyGraph originalDependencyGraph;
	private ArrayList<HashSet<Dependency>> finalDependenciesList;
	private final HashSet<Node> independentNodes;
	private final Cluster mainCluster;

	public DependenciesCorrector(final HashSet<Dependency> dependencies,
								 final Node nodeToRemove,
								 final Cluster mainCluster)
	{
		this.originalDependencies = dependencies;
		this.nodeToRemove = nodeToRemove;
		this.originalDependencyGraph = Utils.buildDependencyGraph(new HashSet<>(this.originalDependencies));
		this.independentNodes = new HashSet<>();
		this.mainCluster = mainCluster;
	}

	/**
	 * Branch source of targeted dependencies to target of sourced dependencies.
	 * A -> nodeToRemove + nodeToRemove -> B becomes A -> B
	 *
	 * @return
	 */
	public void correctDependencies()
	{
		final HashSet<Dependency> targetedDependencies = new HashSet<>();
		final HashSet<Dependency> sourcedDependencies = new HashSet<>();
		final HashSet<Node> remainingNonDependentNodes = new HashSet<>();

		for (Iterator<Dependency> iterator = this.originalDependencies.iterator(); iterator.hasNext(); )
		{
			final Dependency dependency = iterator.next();
			remainingNonDependentNodes.add(dependency.firstNode());
			remainingNonDependentNodes.add(dependency.secondNode());

			if (dependency.firstNode().equals(this.nodeToRemove))
			{
				sourcedDependencies.add(dependency);
				iterator.remove();
				this.mainCluster.getGlobalDependencies().remove(dependency);
			}
			else if (dependency.secondNode().equals(this.nodeToRemove))
			{
				targetedDependencies.add(dependency);
				iterator.remove();
				this.mainCluster.getGlobalDependencies().remove(dependency);
			}
		}

		remainingNonDependentNodes.remove(this.nodeToRemove);

		for (Dependency dependency : this.originalDependencies)
		{
			remainingNonDependentNodes.remove(dependency.firstNode());
			remainingNonDependentNodes.remove(dependency.secondNode());
		}

		final HashSet<Dependency> rawDependencies = new HashSet<>(this.originalDependencies);

		for (Dependency targetedDependency : targetedDependencies)
		{
			for (Dependency sourcedDependency : sourcedDependencies)
			{
				remainingNonDependentNodes.remove(targetedDependency.firstNode());
				remainingNonDependentNodes.remove(sourcedDependency.secondNode());
				final Dependency newDependency = new Dependency(targetedDependency.firstNode(), sourcedDependency.secondNode());
				rawDependencies.add(newDependency);
				this.mainCluster.addGlobalDependency(newDependency);
			}
		}

		if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
		{
			System.out.println("Raw dependencies:\n");
			for (Dependency dependency : rawDependencies)
			{
				System.out.println("(" + dependency.firstNode().bpmnObject().id() + "," + dependency.secondNode().bpmnObject().id() + ")");
			}
		}

		this.finalDependenciesList = Utils.splitDependencies(rawDependencies);

		if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
		{
			System.out.println("Split dependencies:\n");

			for (HashSet<Dependency> dependencies : this.finalDependenciesList)
			{
				System.out.print("Next set: ");

				for (Dependency dependency : dependencies)
				{
					System.out.println("(" + dependency.firstNode().bpmnObject().id() + "," + dependency.secondNode().bpmnObject().id() + ")");
				}
			}
		}

		if (!this.finalDependenciesList.isEmpty())
		{
			final ArrayList<HashSet<Dependency>> dependenciesListCopy = new ArrayList<>(this.finalDependenciesList);
			this.finalDependenciesList.clear();

			for (HashSet<Dependency> finalDependencies : dependenciesListCopy)
			{
				final DependencyGraph initialDependencyGraph = Utils.buildDependencyGraph(finalDependencies);

				if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
				{
					System.out.println("Dependency graph built in correct dependencies");
				}

				this.finalDependenciesList.add(this.correctDependencies(initialDependencyGraph));

				if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
				{
					System.out.println("Dependencies corrected in correct dependencies");
				}
				//System.out.println("Dependency graph:\n\n" + initialDependencyGraph.stringify(0));
			}
		}

		this.independentNodes.addAll(remainingNonDependentNodes);
	}

	public ArrayList<HashSet<Dependency>> dependenciesList()
	{
		return this.finalDependenciesList;
	}

	public ArrayList<DependencyGraph> dependencyGraphs()
	{
		final ArrayList<DependencyGraph> dependencyGraphs = new ArrayList<>();

		for (HashSet<Dependency> dependencies : this.finalDependenciesList)
		{
			dependencyGraphs.add(Utils.buildDependencyGraph(dependencies));
		}

		return dependencyGraphs;
	}

	public HashSet<Node> independentNodes()
	{
		return this.independentNodes;
	}

	public DependencyGraph originalDependencyGraph()
	{
		return this.originalDependencyGraph;
	}

	//Private methods

	private HashSet<Dependency> correctDependencies(final DependencyGraph dependencyGraph)
	{
		final HashSet<Dependency> originalDependencies = dependencyGraph.toDependencySet();
		final ArrayList<Dependency> dependenciesToRemove = new ArrayList<>();

		for (final Node startNode : dependencyGraph.initialNodes())
		{
			this.computeUselessDependencies(startNode, new HashSet<>(), dependenciesToRemove);
		}

		dependenciesToRemove.forEach(originalDependencies::remove);

		return Utils.buildDependencyGraph(originalDependencies).toDependencySet();
	}

	private void computeUselessDependencies(final Node currentNode,
											final HashSet<Node> visitedNodes,
											final ArrayList<Dependency> dependenciesToRemove)
	{
		if (visitedNodes.contains(currentNode))
		{
			return;
		}

		visitedNodes.add(currentNode);

		for (Node pivotChild : currentNode.childNodes())
		{
			for (Node otherChild : currentNode.childNodes())
			{
				if (!pivotChild.equals(otherChild))
				{
					if (otherChild.hasSuccessor(pivotChild))
					{
						dependenciesToRemove.add(new Dependency(currentNode, pivotChild));
					}
				}
			}
		}

		for (Node child : currentNode.childNodes())
		{
			this.computeUselessDependencies(child, visitedNodes, dependenciesToRemove);
		}
	}
}
