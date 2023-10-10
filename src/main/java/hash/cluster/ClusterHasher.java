package hash.cluster;

import bpmn.graph.Node;
import bpmn.types.process.Task;
import hash.HashConstants;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ClusterHasher
{
	private final Cluster mainCluster;

	public ClusterHasher(final Cluster mainCluster)
	{
		this.mainCluster = mainCluster;
	}

	public String hash()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append(HashConstants.CLUSTER_START);

		if (!this.mainCluster.dependencyGraphs().isEmpty())
		{
			//builder.append(HashConstants.DEPENDENCY_GRAPHS_START);
			final ArrayList<String> dependencyGraphHashs = new ArrayList<>();

			for (DependencyGraph dependencyGraph : this.mainCluster.dependencyGraphs())
			{
				dependencyGraphHashs.add(this.hashDependencyGraph(dependencyGraph));
			}

			dependencyGraphHashs.sort(Comparator.naturalOrder());

			String separator = "";

			for (String hash : dependencyGraphHashs)
			{
				builder.append(separator)
						.append(hash);

				separator = HashConstants.BRANCH_SEPARATOR_STR;
			}
		}

		//Priority: Choices > Loops > Tasks
		final ArrayList<EnhancedChoice> independentChoices = new ArrayList<>();
		final ArrayList<EnhancedLoop> independentLoops = new ArrayList<>();
		final ArrayList<EnhancedNode> independentTasks = new ArrayList<>();

		for (EnhancedNode enhancedNode : this.mainCluster.elements())
		{
			if (enhancedNode.isIndependent())
			{
				this.hashEnhancedNode(enhancedNode);

				if (enhancedNode.type() == EnhancedType.CHOICE)
				{
					independentChoices.add((EnhancedChoice) enhancedNode);
				}
				else if (enhancedNode.type() == EnhancedType.LOOP)
				{
					independentLoops.add((EnhancedLoop) enhancedNode);
				}
				else
				{
					independentTasks.add(enhancedNode);
				}
			}
		}

		independentChoices.sort(Comparator.comparing(EnhancedNode::hash));
		independentLoops.sort(Comparator.comparing(EnhancedNode::hash));
		independentTasks.sort(Comparator.comparing(EnhancedNode::hash));

		builder.append(HashConstants.INDEPENDENT_NODES_START);
		String separator = "";

		for (EnhancedChoice choice : independentChoices)
		{
			builder.append(separator)
					.append(choice.hash());
			separator = HashConstants.BRANCH_SEPARATOR_STR;
		}

		for (EnhancedLoop loop : independentLoops)
		{
			builder.append(separator)
					.append(loop.hash());
			separator = HashConstants.BRANCH_SEPARATOR_STR;
		}

		for (EnhancedNode task : independentTasks)
		{
			builder.append(separator)
					.append(task.hash());
			separator = HashConstants.BRANCH_SEPARATOR_STR;
		}

		builder.append(HashConstants.INDEPENDENT_NODES_END);
		builder.append(HashConstants.CLUSTER_END);

		return this.mainCluster.setHash(builder.toString());
	}

	//Private methods

	private String hashDependencyGraph(final DependencyGraph dependencyGraph)
	{
		if (dependencyGraph.hashComputed()) return dependencyGraph.hash();

		final Map<EnhancedChoice, Node> enhancedChoicesMap = new HashMap<>();
		final Map<EnhancedLoop, Node> enhancedLoopsMap = new HashMap<>();
		final ArrayList<EnhancedNode> enhancedTasks = new ArrayList<>();
		final ArrayList<EnhancedChoice> enhancedChoices = new ArrayList<>();
		final ArrayList<EnhancedLoop> enhancedLoops = new ArrayList<>();

		for (Node initialNode : dependencyGraph.initialNodes())
		{
			if (initialNode.bpmnObject() instanceof Task)
			{
				enhancedTasks.add(new EnhancedNode(initialNode));
			}
			else
			{
				final EnhancedNode enhancedNode = this.mainCluster.findEnhancedNodeFrom(initialNode);

				if (enhancedNode.type() == EnhancedType.CHOICE)
				{
					enhancedChoices.add((EnhancedChoice) enhancedNode);
					enhancedChoicesMap.put((EnhancedChoice) enhancedNode, initialNode);
				}
				else if (enhancedNode.type() == EnhancedType.LOOP)
				{
					enhancedLoops.add((EnhancedLoop) enhancedNode);
					enhancedLoopsMap.put((EnhancedLoop) enhancedNode, initialNode);
				}

				this.hashEnhancedNode(enhancedNode);
			}
		}

		enhancedChoices.sort(Comparator.comparing(EnhancedNode::hash));
		enhancedLoops.sort(Comparator.comparing(EnhancedNode::hash));
		enhancedTasks.sort(Comparator.comparing(EnhancedNode::hash));

		//Priority: Choices > Loops > Tasks
		final ArrayList<ArrayList<EnhancedNode>> orderedPaths = new ArrayList<>();

		for (EnhancedChoice choice : enhancedChoices)
		{
			final ArrayList<EnhancedNode> path = new ArrayList<>();
			//path.add(choice);
			orderedPaths.add(path);
			this.hashDependencyGraphRecursiveCall(orderedPaths, path, enhancedChoicesMap.get(choice));
		}

		for (EnhancedLoop loop : enhancedLoops)
		{
			final ArrayList<EnhancedNode> path = new ArrayList<>();
			//path.add(loop);
			orderedPaths.add(path);
			this.hashDependencyGraphRecursiveCall(orderedPaths, path, enhancedLoopsMap.get(loop));
		}

		for (EnhancedNode task : enhancedTasks)
		{
			final ArrayList<EnhancedNode> path = new ArrayList<>();
			//path.add(task);
			orderedPaths.add(path);
			this.hashDependencyGraphRecursiveCall(orderedPaths, path, task.node());
		}

		//Now, we have all the paths ordered in orderedPaths, we just need to write them
		final StringBuilder builder = new StringBuilder();
		builder.append(HashConstants.DEPENDENCY_GRAPHS_START);
		String separator = "";

		for (ArrayList<EnhancedNode> orderedPath : orderedPaths)
		{
			builder.append(separator);
			separator = HashConstants.BRANCH_SEPARATOR_STR;

			for (EnhancedNode node : orderedPath)
			{
				final String hash = this.hashEnhancedNode(node);
				builder.append(hash);
			}
		}

		builder.append(HashConstants.DEPENDENCY_GRAPHS_END);
		//System.out.println("Builder's value: " + builder);

		return dependencyGraph.setHash(builder.toString());
	}

	private void hashDependencyGraphRecursiveCall(final ArrayList<ArrayList<EnhancedNode>> orderedPaths,
												  final ArrayList<EnhancedNode> currentPath,
												  final Node currentNode)
	{
		//We add the current node to the path
		final EnhancedNode currentEnhancedNode;

		if (currentNode.bpmnObject() instanceof Task)
		{
			currentEnhancedNode = new EnhancedNode(currentNode);
		}
		else
		{
			currentEnhancedNode = this.mainCluster.findEnhancedNodeFrom(currentNode);
		}

		currentPath.add(currentEnhancedNode);

		//If the current node has no child, stop iterating
		if (currentNode.childNodes().isEmpty()) return;

		if (currentNode.childNodes().size() > 1)
		{
			final Map<EnhancedChoice, Node> enhancedChoicesMap = new HashMap<>();
			final Map<EnhancedLoop, Node> enhancedLoopsMap = new HashMap<>();
			final ArrayList<EnhancedNode> enhancedTasks = new ArrayList<>();
			final ArrayList<EnhancedChoice> enhancedChoices = new ArrayList<>();
			final ArrayList<EnhancedLoop> enhancedLoops = new ArrayList<>();

			for (Node childNode : currentNode.childNodes())
			{
				if (childNode.bpmnObject() instanceof Task)
				{
					enhancedTasks.add(new EnhancedNode(childNode));
				}
				else
				{
					final EnhancedNode enhancedNode = this.mainCluster.findEnhancedNodeFrom(childNode);

					if (enhancedNode.type() == EnhancedType.CHOICE)
					{
						enhancedChoices.add((EnhancedChoice) enhancedNode);
						enhancedChoicesMap.put((EnhancedChoice) enhancedNode, childNode);
						this.hashEnhancedNode(enhancedNode);
					}
					else if (enhancedNode.type() == EnhancedType.LOOP)
					{
						enhancedLoops.add((EnhancedLoop) enhancedNode);
						enhancedLoopsMap.put((EnhancedLoop) enhancedNode, childNode);
						this.hashEnhancedNode(enhancedNode);
					}
				}
			}

			enhancedChoices.sort(Comparator.comparing(EnhancedNode::hash));
			enhancedLoops.sort(Comparator.comparing(EnhancedNode::hash));
			enhancedTasks.sort(Comparator.comparing(EnhancedNode::hash));

			//Priority: Choices > Loops > Tasks
			final ArrayList<ArrayList<EnhancedNode>> nextPaths = new ArrayList<>();
			nextPaths.add(currentPath);

			for (int i = 0; i < enhancedTasks.size() + enhancedChoices.size() + enhancedLoops.size() - 1; i++)
			{
				final ArrayList<EnhancedNode> nextPath = new ArrayList<>(currentPath);
				nextPaths.add(nextPath);
				orderedPaths.add(nextPath);
			}

			int index = 0;

			for (EnhancedChoice choice : enhancedChoices)
			{
				if (index == 0)
				{
					this.hashDependencyGraphRecursiveCall(orderedPaths, currentPath, enhancedChoicesMap.get(choice));
				}
				else
				{
					this.hashDependencyGraphRecursiveCall(orderedPaths, nextPaths.get(index), enhancedChoicesMap.get(choice));
				}

				index++;
			}

			for (EnhancedLoop loop : enhancedLoops)
			{
				if (index == 0)
				{
					this.hashDependencyGraphRecursiveCall(orderedPaths, currentPath, enhancedLoopsMap.get(loop));
				}
				else
				{
					this.hashDependencyGraphRecursiveCall(orderedPaths, nextPaths.get(index), enhancedLoopsMap.get(loop));
				}

				index++;
			}

			for (EnhancedNode task : enhancedTasks)
			{
				if (index == 0)
				{
					this.hashDependencyGraphRecursiveCall(orderedPaths, currentPath, task.node());
				}
				else
				{
					this.hashDependencyGraphRecursiveCall(orderedPaths, nextPaths.get(index), task.node());
				}

				index++;
			}
		}
		else
		{
			//If it has only one child, call the method recursively on it
			this.hashDependencyGraphRecursiveCall(orderedPaths, currentPath, currentNode.childNodes().iterator().next());
		}
	}

	private String hashCluster(final Cluster cluster)
	{
		if (cluster.hashComputed()) return cluster.hash();

		final ClusterHasher hasher = new ClusterHasher(cluster);
		final String hash = hasher.hash();
		cluster.setHash(hash);

		return hash;
	}

	private String hashEnhancedNode(final EnhancedNode enhancedNode)
	{
		if (enhancedNode.hashComputed()) return enhancedNode.hash();

		final ArrayList<String> clustersHash = new ArrayList<>();
		final StringBuilder builder = new StringBuilder();

		if (enhancedNode.type() == EnhancedType.CHOICE)
		{
			builder.append(HashConstants.EXCLUSIVE_SPLIT);

			for (Cluster cluster : ((EnhancedChoice) enhancedNode).clusters())
			{
				clustersHash.add(this.hashCluster(cluster));
			}

			clustersHash.sort(Comparator.naturalOrder());

			String separator = "";

			for (String hash : clustersHash)
			{
				builder.append(separator)
						.append(hash);

				separator = HashConstants.BRANCH_SEPARATOR_STR;
			}

			builder.append(HashConstants.EXCLUSIVE_MERGE);
		}
		else if (enhancedNode.type() == EnhancedType.LOOP)
		{
			builder.append(HashConstants.EXCLUSIVE_MERGE);

			for (Cluster cluster : ((EnhancedLoop) enhancedNode).clusters())
			{
				clustersHash.add(this.hashCluster(cluster));
			}

			clustersHash.sort(Comparator.naturalOrder());

			String separator = "";

			for (String hash : clustersHash)
			{
				builder.append(separator)
						.append(hash);

				separator = HashConstants.BRANCH_SEPARATOR_STR;
			}

			builder.append(HashConstants.EXCLUSIVE_SPLIT);
		}
		else
		{
			throw new IllegalStateException("Task hash should be set from the beginning!");
		}

		return enhancedNode.setHash(builder.toString());
	}
}
