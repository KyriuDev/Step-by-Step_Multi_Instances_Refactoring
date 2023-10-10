package refactoring.step_by_step;

import bpmn.graph.Graph;
import bpmn.graph.GraphUtils;
import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessType;
import bpmn.types.process.Task;
import constants.PrintLevel;
import loops_management.Loop;
import loops_management.LoopFinder;
import other.Utils;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.*;

import java.util.ArrayList;
import java.util.HashSet;

import static main.Main.PRINT_LEVEL;

public class DependenciesExtractorV2
{
	private final Graph graph;
	private final LoopFinder loopFinder;
	private final HashSet<EnhancedNode> nodesToProcess;
	private Cluster cluster;

	public DependenciesExtractorV2(final Graph graph,
								   final LoopFinder loopFinder)
	{
		this.graph = graph;
		this.nodesToProcess = new HashSet<>();
		this.loopFinder = loopFinder;
		//System.out.println("Initial graph: \n\n" + this.graph.toString());
	}

	public Cluster extractDependencies()
	{
		this.cluster = new Cluster();
		this.generateDependencyGraphsFromStartNode(this.graph.initialNode(), null, this.cluster);

		while (!this.nodesToProcess.isEmpty())
		{
			if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
			{
				System.out.println("Number of nodes to process: " + this.nodesToProcess.size());
			}

			final HashSet<EnhancedNode> nodesToProcessCopy = new HashSet<>(this.nodesToProcess);
			final HashSet<EnhancedNode> newNodesToProcess = new HashSet<>();

			for (EnhancedNode enhancedNode : nodesToProcessCopy)
			{
				if (enhancedNode.type() == EnhancedType.CHOICE)
				{
					final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;
					final Node correspondingMergeGateway = GraphUtils.findCorrespondingMergeGateway(enhancedChoice.node());

					for (Node child : enhancedNode.node().childNodes())
					{
						this.nodesToProcess.clear();
						final Cluster choiceCluster = enhancedChoice.getClusterFromKey(child);
						choiceCluster.setProbability(child.bpmnObject().probability());
						this.generateDependencyGraphsFromStartNode(child, correspondingMergeGateway, choiceCluster);
						newNodesToProcess.addAll(this.nodesToProcess);
					}
				}
				else if (enhancedNode.type() == EnhancedType.LOOP)
				{
					final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;
					final Loop innerLoop = this.loopFinder.findInnerLoopOf(enhancedLoop.node());
					final Node exitNode = this.graph.getNodeFromID(innerLoop.exitPoint().bpmnObject().id());

					this.nodesToProcess.clear();
					this.generateDependencyGraphsFromStartNode(enhancedNode.node().childNodes().iterator().next(), exitNode, enhancedLoop.entryToExitCluster());
					newNodesToProcess.addAll(this.nodesToProcess);

					for (Node child : exitNode.childNodes())
					{
						if (innerLoop.hasNode(child))
						{
							this.nodesToProcess.clear();
							enhancedLoop.exitToEntryCluster().setProbability(child.bpmnObject().probability());
							this.generateDependencyGraphsFromStartNode(child, enhancedNode.node(), enhancedLoop.exitToEntryCluster());
							newNodesToProcess.addAll(this.nodesToProcess);
						}
					}
				}
			}

			this.nodesToProcess.clear();
			this.nodesToProcess.addAll(newNodesToProcess);
		}

		Utils.resetIndependentNodes(this.cluster);

		return this.cluster;
	}

	public void clear()
	{
		this.nodesToProcess.clear();
	}

	public Cluster mainCluster()
	{
		return this.cluster;
	}

	//Private methods

	private void generateDependencyGraphsFromStartNode(final Node node,
													   final Node bound,
													   final Cluster cluster)
	{
		final ArrayList<ArrayList<Node>> allPaths = new ArrayList<>();
		final ArrayList<Node> path = new ArrayList<>();
		allPaths.add(path);
		this.computeAllPathsUpTo(node, bound, path, allPaths, cluster);

		if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
		{
			System.out.println("The following paths have been found between node |" + node.bpmnObject().id() + "| and " + (bound == null ? "end" : "node |" + bound.bpmnObject().id() + "|") + ":\n\n");

			for (ArrayList<Node> pathToShow : allPaths)
			{
				final StringBuilder builder = new StringBuilder("- ");

				for (Node node1 : pathToShow)
				{
					builder.append(node1.bpmnObject().id()).append(" + ");
				}

				builder.append("\n");
				System.out.print(builder);
			}
		}

		final ArrayList<DependencyGraph> dependencyGraphs = this.separatePathsAndGenerateDependencyGraphs(allPaths);

		for (DependencyGraph dependencyGraph : dependencyGraphs)
		{
			if (dependencyGraph.isEmpty()) throw new IllegalStateException();

			if (dependencyGraph.size() > 1)
			{
				//Real dependency graph
				cluster.addDependencyGraph(dependencyGraph);
				this.cluster.addGlobalDependencies(dependencyGraph.toDependencySet());
			}
		}
	}

	private void computeAllPathsUpTo(final Node currentNode,
									 final Node boundNode,
									 final ArrayList<Node> currentPath,
									 final ArrayList<ArrayList<Node>> allPaths,
									 final Cluster cluster)
	{
		if (boundNode != null)
		{
			if (currentNode.equals(boundNode))
			{
				return;
			}
		}

		final ArrayList<Node> nextNodes = new ArrayList<>();

		if (currentNode.bpmnObject() instanceof Task)
		{
			cluster.addElement(new EnhancedNode(currentNode));
			currentPath.add(currentNode);
			nextNodes.addAll(currentNode.childNodes());
		}
		else if (currentNode.bpmnObject().type() == BpmnProcessType.EXCLUSIVE_GATEWAY)
		{
			currentPath.add(currentNode);

			if (this.loopFinder != null
					&& this.loopFinder.nodeIsInLoop(currentNode))
			{
				final Loop mostInnerLoop = this.loopFinder.findInnerLoopOf(currentNode);

				if (mostInnerLoop.entryPoint().equals(currentNode))
				{
					//Beginning of a loop
					final EnhancedLoop enhancedLoop = new EnhancedLoop(currentNode);
					this.nodesToProcess.add(enhancedLoop);
					cluster.addElement(enhancedLoop);

					for (Node child : this.graph.getNodeFromID(mostInnerLoop.exitPoint().bpmnObject().id()).childNodes())
					{
						if (!mostInnerLoop.hasNode(child))
						{
							nextNodes.add(child);
						}
					}
				}
				else
				{
					//Beginning of a choice inside a loop
					final EnhancedChoice enhancedChoice = new EnhancedChoice(currentNode);
					this.nodesToProcess.add(enhancedChoice);
					cluster.addElement(enhancedChoice);

					final Node correspondingMergeGateway = GraphUtils.findCorrespondingMergeGateway(currentNode);

					if (correspondingMergeGateway != null)
					{
						nextNodes.addAll(correspondingMergeGateway.childNodes());
					}
				}
			}
			else
			{
				final EnhancedChoice enhancedChoice = new EnhancedChoice(currentNode);
				this.nodesToProcess.add(enhancedChoice);
				cluster.addElement(enhancedChoice);

				final Node correspondingMergeGateway = GraphUtils.findCorrespondingMergeGateway(currentNode);

				if (correspondingMergeGateway != null)
				{
					nextNodes.addAll(correspondingMergeGateway.childNodes());
				}
			}
		}
		else
		{
			nextNodes.addAll(currentNode.childNodes());
		}

		final ArrayList<ArrayList<Node>> childPaths = new ArrayList<>();

		for (int i = 0; i < nextNodes.size(); i++)
		{
			if (i == 0)
			{
				childPaths.add(currentPath);
			}
			else
			{
				final ArrayList<Node> childPath = new ArrayList<>(currentPath);
				childPaths.add(childPath);
				allPaths.add(childPath);
			}
		}

		int i = 0;

		for (final Node child : nextNodes)
		{
			this.computeAllPathsUpTo(child, boundNode, childPaths.get(i++), allPaths, cluster);
		}
	}

	private ArrayList<DependencyGraph> separatePathsAndGenerateDependencyGraphs(final ArrayList<ArrayList<Node>> paths)
	{
		final ArrayList<DependencyGraph> dependencyGraphs = new ArrayList<>();
		final ArrayList<ArrayList<ArrayList<Node>>> pathsClusters = new ArrayList<>();

		for (ArrayList<Node> path : paths)
		{
			boolean pathAdded = false;

			for (ArrayList<ArrayList<Node>> clusterOfPaths : pathsClusters)
			{
				for (ArrayList<Node> clusterPath : clusterOfPaths)
				{
					for (Node node : path)
					{
						if (clusterPath.contains(node))
						{
							pathAdded = true;
							clusterOfPaths.add(path);
							break;
						}
					}

					if (pathAdded)
					{
						break;
					}
				}

				if (pathAdded)
				{
					break;
				}
			}

			if (!pathAdded)
			{
				final ArrayList<ArrayList<Node>> newCluster = new ArrayList<>();
				newCluster.add(path);
				pathsClusters.add(newCluster);
			}
		}

		for (ArrayList<ArrayList<Node>> pathsList : pathsClusters)
		{
			if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
			{
				System.out.println("Paths before transformation: " + pathsList);
			}

			final DependencyGraph dependencyGraph = Paths2DependencyGraph.transform(pathsList);
			dependencyGraphs.add(dependencyGraph);
		}

		if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
		{
			System.out.println("Found " + dependencyGraphs.size() + " dependency graphs.");
		}

		return dependencyGraphs;
	}
}
