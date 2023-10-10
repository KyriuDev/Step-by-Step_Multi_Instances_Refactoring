package refactoring.legacy.dependencies;

import bpmn.graph.Node;
import bpmn.types.process.Gateway;
import constants.PrintLevel;
import refactoring.legacy.Cluster;

import java.util.HashSet;

import static main.Main.PRINT_LEVEL;

public class DependenciesVerifier
{
	private DependenciesVerifier()
	{

	}

	public static boolean verifyDependencies(final HashSet<Dependency> globalDependencies,
											 final Cluster cluster)
	{
		for (Dependency dependency : globalDependencies)
		{
			boolean dependencyVerified = false;

			for (DependencyGraph dependencyGraph : cluster.dependencyGraphs())
			{
				//System.out.println(dependencyGraph.stringify(0));

				for (Node initialNode : dependencyGraph.initialNodes())
				{
					final DependencyStatus status = DependenciesVerifier.verifyDependencyV1(dependency, cluster, initialNode, DependencyStatus.NOT_FOUND);
					//System.out.println(status);

					if (status == DependencyStatus.FOUND)
					{
						dependencyVerified = true;
						break;
					}
				}

				if (dependencyVerified)
				{
					break;
				}
			}

			if (!dependencyVerified)
			{
				if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
				{
					System.out.println("Dependency (" + dependency.firstNode().bpmnObject().id() + "," + dependency.secondNode().bpmnObject().id() + ") was not verified!");
				}

				return false;
			}

			if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
			{
				System.out.println("Dependency (" + dependency.firstNode().bpmnObject().id() + "," + dependency.secondNode().bpmnObject().id() + ") was verified.");
			}
		}

		return true;
	}

	//Private methods

	private static DependencyStatus verifyDependencyV1(final Dependency dependency,
													   final Cluster currentCluster,
													   final Node currentNode,
													   final DependencyStatus status)
	{
		DependencyStatus nextStatus;

		//System.out.println("Current status: " + status);
		//System.out.println("|" + currentNode.bpmnObject().rawName() + "|/|" + dependency.firstNode().bpmnObject().rawID() + "|/|" + dependency.secondNode().bpmnObject().rawID() + "|");
		//System.out.println("Object type: " + currentNode.bpmnObject().type());

		if (currentNode.bpmnObject().name().equals(dependency.firstNode().bpmnObject().id()))
		{
			//Current node is the first task of the dependency
			if (status == DependencyStatus.NOT_FOUND)
			{
				nextStatus = DependencyStatus.LEFT_TASK_FOUND;
			}
			else
			{
				return DependencyStatus.ERROR;
			}
		}
		else if (currentNode.bpmnObject().name().equals(dependency.secondNode().bpmnObject().id()))
		{
			//Current node is the second task of the dependency
			if (status == DependencyStatus.LEFT_TASK_FOUND)
			{
				return DependencyStatus.FOUND;
			}
			else
			{
				return DependencyStatus.RIGHT_TASK_FOUND;
			}
		}
		else
		{
			nextStatus = status;
		}

		if (currentNode.bpmnObject() instanceof Gateway)
		{
			final EnhancedNode enhancedNode = currentCluster.findEnhancedNodeFrom(currentNode);

			if (enhancedNode.type() == EnhancedType.CHOICE)
			{
				final HashSet<DependencyStatus> subClusterStatus = new HashSet<>();

				for (Cluster subCluster : ((EnhancedChoice) enhancedNode).clusters())
				{
					for (DependencyGraph dependencyGraph : subCluster.dependencyGraphs())
					{
						for (Node initialNode : dependencyGraph.initialNodes())
						{
							final DependencyStatus currentStatus = DependenciesVerifier.verifyDependencyV1(dependency, subCluster, initialNode, DependencyStatus.NOT_FOUND);
							subClusterStatus.add(currentStatus);

							if (currentStatus == DependencyStatus.FOUND) return DependencyStatus.FOUND;
						}
					}

					for (EnhancedNode node : subCluster.elements())
					{
						final Node copy = node.node().weakCopy();
						final DependencyStatus nonDependentNodeStatus = DependenciesVerifier.verifyDependencyV1(dependency, subCluster, copy, DependencyStatus.NOT_FOUND);
						subClusterStatus.add(nonDependentNodeStatus);

						if (nonDependentNodeStatus == DependencyStatus.FOUND) return DependencyStatus.FOUND;
					}
				}

				if (subClusterStatus.contains(DependencyStatus.RIGHT_TASK_FOUND))
				{
					if (nextStatus == DependencyStatus.LEFT_TASK_FOUND)
					{
						return DependencyStatus.FOUND;
					}
					else
					{
						return DependencyStatus.ERROR;
					}
				}
				else if (subClusterStatus.contains(DependencyStatus.LEFT_TASK_FOUND))
				{
					nextStatus = DependencyStatus.LEFT_TASK_FOUND;
				}
			}
			else
			{
				final EnhancedLoop enhancedLoop = (EnhancedLoop) currentCluster.findEnhancedNodeFrom(currentNode);
				final HashSet<DependencyStatus> entryToExitStatus = new HashSet<>();

				for (DependencyGraph dependencyGraph : enhancedLoop.entryToExitCluster().dependencyGraphs())
				{
					for (Node initialNode : dependencyGraph.initialNodes())
					{
						final DependencyStatus currentStatus = DependenciesVerifier.verifyDependencyV1(dependency, enhancedLoop.entryToExitCluster(), initialNode, DependencyStatus.NOT_FOUND);
						entryToExitStatus.add(currentStatus);

						if (currentStatus == DependencyStatus.FOUND) return DependencyStatus.FOUND;
					}
				}

				for (EnhancedNode node : enhancedLoop.entryToExitCluster().elements())
				{
					final Node copy = node.node().weakCopy();
					final DependencyStatus nonDependentNodeStatus = DependenciesVerifier.verifyDependencyV1(dependency, enhancedLoop.entryToExitCluster(), copy, DependencyStatus.NOT_FOUND);
					entryToExitStatus.add(nonDependentNodeStatus);

					if (nonDependentNodeStatus == DependencyStatus.FOUND) return DependencyStatus.FOUND;
				}

				final HashSet<DependencyStatus> exitToEntryStatus = new HashSet<>();

				for (DependencyGraph dependencyGraph : enhancedLoop.exitToEntryCluster().dependencyGraphs())
				{
					for (Node initialNode : dependencyGraph.initialNodes())
					{
						final DependencyStatus currentStatus = DependenciesVerifier.verifyDependencyV1(dependency, enhancedLoop.exitToEntryCluster(), initialNode, DependencyStatus.NOT_FOUND);
						exitToEntryStatus.add(currentStatus);

						if (currentStatus == DependencyStatus.FOUND) return DependencyStatus.FOUND;
					}
				}

				for (EnhancedNode node : enhancedLoop.exitToEntryCluster().elements())
				{
					final Node copy = node.node().weakCopy();
					final DependencyStatus nonDependentNodeStatus = DependenciesVerifier.verifyDependencyV1(dependency, enhancedLoop.exitToEntryCluster(), copy, DependencyStatus.NOT_FOUND);
					exitToEntryStatus.add(nonDependentNodeStatus);

					if (nonDependentNodeStatus == DependencyStatus.FOUND) return DependencyStatus.FOUND;
				}

				if (entryToExitStatus.contains(DependencyStatus.LEFT_TASK_FOUND)
						&& exitToEntryStatus.contains(DependencyStatus.RIGHT_TASK_FOUND))
				{
					return DependencyStatus.FOUND;
				}
				else if (nextStatus == DependencyStatus.LEFT_TASK_FOUND
						&& (entryToExitStatus.contains(DependencyStatus.RIGHT_TASK_FOUND)
						|| exitToEntryStatus.contains(DependencyStatus.RIGHT_TASK_FOUND)))
				{
					return DependencyStatus.FOUND;
				}
				else if (entryToExitStatus.contains(DependencyStatus.LEFT_TASK_FOUND)
						|| exitToEntryStatus.contains(DependencyStatus.LEFT_TASK_FOUND))
				{
					nextStatus = DependencyStatus.LEFT_TASK_FOUND;
				}
				else if (entryToExitStatus.contains(DependencyStatus.RIGHT_TASK_FOUND)
						|| exitToEntryStatus.contains(DependencyStatus.RIGHT_TASK_FOUND))
				{
					return DependencyStatus.ERROR;
				}
			}
		}

		for (Node child : currentNode.childNodes())
		{
			final DependencyStatus childStatus = DependenciesVerifier.verifyDependencyV1(dependency, currentCluster, child, nextStatus);

			if (childStatus == DependencyStatus.FOUND)
			{
				return DependencyStatus.FOUND;
			}
		}

		return nextStatus;
	}
}
