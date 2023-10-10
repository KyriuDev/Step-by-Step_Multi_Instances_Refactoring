package refactoring.step_by_step;

import bpmn.graph.Node;
import constants.PrintLevel;
import other.Pair;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.*;

import static main.Main.PRINT_LEVEL;

public class DependenciesCorrectorV2
{
	final Cluster mainCluster;
	final Node taskToRemove;

	public DependenciesCorrectorV2(final Cluster cluster,
								   final Node taskToRemove)
	{
		this.mainCluster = cluster;
		this.taskToRemove = taskToRemove;
	}

	public void correctDependencies()
	{
		final Pair<DependencyGraph, Cluster> graphWithCluster = this.findCorrespondingDependencyGraph(this.mainCluster, this.taskToRemove);

		if (graphWithCluster == null) throw new IllegalStateException("Task |" + this.taskToRemove.bpmnObject().id() + "| was not found in the entire cluster.");

		graphWithCluster.second().abstractGraphs().clear();
		graphWithCluster.second().elements().remove(new EnhancedChoice(this.taskToRemove));

		if (graphWithCluster.first() == null)
		{
			//The task to remove is an independent task
			if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
			{
				System.out.println("Task |" + this.taskToRemove.bpmnObject().id() + "| is independent.");
			}
		}
		else
		{
			//The task to remove is a dependent task
			final DependenciesCorrector dependenciesCorrector = new DependenciesCorrector(graphWithCluster.first().toDependencySet(), this.taskToRemove, this.mainCluster);
			dependenciesCorrector.correctDependencies();

			graphWithCluster.second().dependencyGraphs().remove(graphWithCluster.first());

			if (!dependenciesCorrector.dependenciesList().isEmpty())
			{
				//There are still some dependencies in the cluster
				for (DependencyGraph dependencyGraph : dependenciesCorrector.dependencyGraphs())
				{
					graphWithCluster.second().addDependencyGraph(dependencyGraph);
				}
			}

			//We mark all newly independent nodes as independent
			for (EnhancedNode enhancedNode : graphWithCluster.second().elements())
			{
				if (dependenciesCorrector.independentNodes().contains(enhancedNode.node()))
				{
					enhancedNode.setIndependent();
				}
			}
		}
	}

	private Pair<DependencyGraph, Cluster> findCorrespondingDependencyGraph(final Cluster cluster,
																			final Node taskToRemove)
	{
		for (DependencyGraph dependencyGraph : cluster.dependencyGraphs())
		{
			if (dependencyGraph.hasNode(taskToRemove))
			{
				return new Pair<>(dependencyGraph, cluster);
			}
		}

		for (EnhancedNode enhancedNode : cluster.elements())
		{
			if (enhancedNode.type() == EnhancedType.CLASSICAL)
			{
				if (enhancedNode.node().equals(taskToRemove))
				{
					//The current task to remove is independent
					return new Pair<>(null, cluster);
				}
			}
			else if (enhancedNode.type() == EnhancedType.CHOICE)
			{
				final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;

				for (Cluster subCluster : enhancedChoice.clusters())
				{
					final Pair<DependencyGraph, Cluster> dependencyGraph = this.findCorrespondingDependencyGraph(subCluster, taskToRemove);

					if (dependencyGraph != null)
					{
						return dependencyGraph;
					}
				}
			}
			else if (enhancedNode.type() == EnhancedType.LOOP)
			{
				final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;

				final Pair<DependencyGraph, Cluster> entryDependencyGraph = this.findCorrespondingDependencyGraph(enhancedLoop.entryToExitCluster(), taskToRemove);

				if (entryDependencyGraph != null)
				{
					return entryDependencyGraph;
				}

				final Pair<DependencyGraph, Cluster> exitDependencyGraph = this.findCorrespondingDependencyGraph(enhancedLoop.exitToEntryCluster(), taskToRemove);

				if (exitDependencyGraph != null)
				{
					return exitDependencyGraph;
				}
			}
		}

		return null;
	}
}
