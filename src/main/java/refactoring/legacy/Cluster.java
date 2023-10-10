package refactoring.legacy;

import other.Utils;
import bpmn.graph.Node;
import refactoring.legacy.dependencies.*;
import refactoring.legacy.partial_order_to_bpmn.AbstractGraph;

import java.util.*;

public class Cluster
{
	private final HashSet<Dependency> globalDependencies;
	private final ArrayList<AbstractGraph> abstractGraphs;
	private double probability;
	private final HashSet<EnhancedNode> elements;
	private final HashSet<Dependency> tempDependencies;

	private final ArrayList<DependencyGraph> dependencyGraphs;
	private final String id;

	private boolean processed;

	private EnhancedGraph bpmnGraph;

	private final HashSet<AbstractGraph> possiblyOptimalBalancedClusters;
	private final HashMap<String, EnhancedNode> enhancedNodesHashMap;
	private String hash;

	public Cluster()
	{
		this.elements = new HashSet<>();
		this.tempDependencies = new HashSet<>();
		this.probability = 1d;
		this.id = Utils.generateRandomIdentifier(20);
		this.dependencyGraphs = new ArrayList<>();
		this.abstractGraphs = new ArrayList<>();
		this.processed = false;
		this.bpmnGraph = null;
		this.possiblyOptimalBalancedClusters = new HashSet<>();
		this.enhancedNodesHashMap = new HashMap<>();
		this.globalDependencies = new HashSet<>();
		this.hash = "-1";
	}

	public Cluster(final String id)
	{
		this.elements = new HashSet<>();
		this.tempDependencies = new HashSet<>();
		this.probability = 1d;
		this.id = id;
		this.dependencyGraphs = new ArrayList<>();
		this.abstractGraphs = new ArrayList<>();
		this.processed = false;
		this.bpmnGraph = null;
		this.possiblyOptimalBalancedClusters = new HashSet<>();
		this.enhancedNodesHashMap = new HashMap<>();
		this.globalDependencies = new HashSet<>();
		this.hash = "-1";
	}

	public void clear()
	{
		this.globalDependencies.clear();
		this.abstractGraphs.clear();
		this.elements.clear();
		this.tempDependencies.clear();
		this.dependencyGraphs.clear();
		this.possiblyOptimalBalancedClusters.clear();
		this.enhancedNodesHashMap.clear();
	}

	public String setHash(final String hash)
	{
		return this.hash = hash;
	}

	public boolean hashComputed()
	{
		return !this.hash.equals("-1");
	}

	public void addPossiblyOptimalBalancedAbstractGraph(final AbstractGraph abstractGraph)
	{
		this.possiblyOptimalBalancedClusters.add(abstractGraph);
	}

	public void addAllPossiblyOptimalBalancedAbstractGraphs(final Collection<AbstractGraph> abstractGraphs)
	{
		this.possiblyOptimalBalancedClusters.addAll(abstractGraphs);
	}

	public HashSet<AbstractGraph> possiblyOptimalBalancedGraphs()
	{
		return this.possiblyOptimalBalancedClusters;
	}

	public boolean hasBeenCorrected()
	{
		return this.possiblyOptimalBalancedClusters.isEmpty();
	}

	public void addElement(final EnhancedNode enhancedNode)
	{
		this.elements.add(enhancedNode);
	}

	public void addDependency(final Dependency dependency)
	{
		this.tempDependencies.add(dependency);
	}

	public void setProbability(final double probability)
	{
		this.probability = probability;
	}

	public double probability()
	{
		return this.probability;
	}

	public void setProcessed()
	{
		this.processed = true;
	}

	public void unprocess()
	{
		this.processed = false;
	}

	public boolean hasBeenProcessed()
	{
		return this.processed;
	}

	public String hash()
	{
		return this.hash;
	}

	public boolean isEmpty()
	{
		return this.elements.isEmpty() && this.tempDependencies.isEmpty();
	}

	public void addDependencyGraph(final DependencyGraph graph)
	{
		this.dependencyGraphs.add(graph);
	}

	public ArrayList<DependencyGraph> dependencyGraphs()
	{
		return this.dependencyGraphs;
	}

	public HashSet<Dependency> dependencies()
	{
		return this.tempDependencies;
	}

	public HashSet<EnhancedNode> elements()
	{
		return this.elements;
	}

	public void addAbstractGraph(final AbstractGraph abstractGraph)
	{
		this.abstractGraphs.add(abstractGraph);
	}

	public void setBpmnGraph(final EnhancedGraph graph)
	{
		this.bpmnGraph = graph;
	}

	public void addGlobalDependency(final Dependency dependency)
	{
		this.globalDependencies.add(dependency);
	}

	public void addGlobalDependencies(final HashSet<Dependency> dependencies)
	{
		this.globalDependencies.addAll(dependencies);
	}

	public HashSet<Dependency> getGlobalDependencies()
	{
		return this.globalDependencies;
	}

	public EnhancedGraph bpmnGraph()
	{
		return this.bpmnGraph;
	}

	public ArrayList<AbstractGraph> abstractGraphs()
	{
		return this.abstractGraphs;
	}

	public void replaceSubClusterBy(final Cluster cluster)
	{
		this.replaceSubClusterBy(this, cluster);
	}

	public int nbTasks()
	{
		int nbTasks = 0;

		for (EnhancedNode enhancedNode : this.elements)
		{
			if (enhancedNode.type() == EnhancedType.CLASSICAL)
			{
				nbTasks++;
			}
			else if (enhancedNode.type() == EnhancedType.CHOICE)
			{
				final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;

				for (Cluster cluster : enhancedChoice.clusters())
				{
					nbTasks += cluster.nbTasks();
				}
			}
			else if (enhancedNode.type() == EnhancedType.LOOP)
			{
				final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;

				nbTasks += enhancedLoop.entryToExitCluster().nbTasks() + enhancedLoop.exitToEntryCluster().nbTasks();
			}
		}

		return nbTasks;
	}

	public String showDependencyGraphs(final int depth)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("	".repeat(depth))
				.append("Current cluster has the following dependency graphs:\n");

		int i = 1;

		for (DependencyGraph dependencyGraph : this.dependencyGraphs)
		{
			builder.append("	".repeat(depth + 1))
					.append("Graph n°")
					.append(i++)
					.append(":\n\n");

			for (Node initialNode : dependencyGraph.initialNodes())
			{
				builder.append(initialNode.stringify(depth + 3, new ArrayList<>()))
						.append("\n\n");
			}
		}

		for (EnhancedNode enhancedNode : this.elements)
		{
			if (enhancedNode.type() == EnhancedType.CHOICE)
			{
				final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;

				for (Cluster cluster : enhancedChoice.clusters())
				{
					builder.append(cluster.showDependencyGraphs(depth + 1))
							.append("\n");
				}
			}
			else if (enhancedNode.type() == EnhancedType.LOOP)
			{
				final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;

				builder.append(enhancedLoop.entryToExitCluster())
						.append("\n")
						.append(enhancedLoop.exitToEntryCluster())
						.append("\n");
			}
		}

		return builder.toString();
	}

	public String stringify(final int depth)
	{
		final StringBuilder builder = new StringBuilder();

		if (this.isEmpty())
		{
			builder.append("	".repeat(depth))
					.append("Cluster |")
					.append(this.id)
					.append("| contains no elements.");
		}
		else
		{
			builder.append("	".repeat(depth))
					.append("Cluster |")
					.append(this.id)
					.append("| contains the following elements:\n");

			for (EnhancedNode enhancedNode : this.elements)
			{
				builder.append(enhancedNode.stringify(depth + 1))
						.append("\n");
			}

			if (!this.tempDependencies.isEmpty())
			{
				builder.append("\n")
						.append("	".repeat(depth))
						.append("and the following dependencies:\n");

				for (Dependency dependency : this.tempDependencies)
				{
					builder.append(dependency.stringify(depth + 1))
							.append("\n");
				}
			}

			if (!this.abstractGraphs.isEmpty())
			{
				builder.append("\n")
						.append("	".repeat(depth))
						.append("and the following abstract graphs:\n");

				for (AbstractGraph abstractGraph : abstractGraphs)
				{
					builder.append(abstractGraph.stringify(depth + 1))
							.append("\n");
				}
			}

			if (!this.globalDependencies.isEmpty())
			{
				builder.append("\n")
						.append("	".repeat(depth))
						.append("and the following global dependencies:\n");

				for (Dependency dependency : this.globalDependencies)
				{
					builder.append(dependency.stringify(depth + 1))
							.append("\n");
				}
			}

			if (!this.dependencyGraphs.isEmpty())
			{
				builder.append("\n")
						.append("	".repeat(depth))
						.append("and the following dependency graphs:\n");

				for (DependencyGraph dependencyGraph : this.dependencyGraphs)
				{
					builder.append(dependencyGraph.stringify(depth + 1))
							.append("\n");
				}
			}

			if (!this.possiblyOptimalBalancedClusters.isEmpty())
			{
				builder.append("\n")
						.append("	".repeat(depth))
						.append("and the following possibly optimal balanced clusters:\n");

				for (AbstractGraph abstractGraph : this.possiblyOptimalBalancedClusters)
				{
					builder.append(abstractGraph.stringify(depth + 1))
							.append("\n");
				}
			}

			if (!this.enhancedNodesHashMap.isEmpty())
			{
				builder.append("\n")
						.append("	".repeat(depth))
						.append("and the following enhanced nodes in hashmap:\n");

				for (EnhancedNode enhancedNode : this.enhancedNodesHashMap.values())
				{
					builder.append(enhancedNode.stringify(depth + 1))
							.append("\n");
				}
			}
		}

		return builder.toString();
	}

	public EnhancedNode findEnhancedNodeFrom(Node node)
	{
		if (this.enhancedNodesHashMap.isEmpty())
		{
			for (EnhancedNode currentNode : this.elements)
			{
				if (currentNode.node().equals(node))
				{
					return currentNode;
				}
			}
		}
		else
		{
			return this.enhancedNodesHashMap.get(node.bpmnObject().id());
		}

		throw new IllegalStateException("No corresponding enhanced node found for node |" + node.bpmnObject().id() + "|.");
	}

	public void hashElements()
	{
		for (EnhancedNode enhancedNode : this.elements)
		{
			this.enhancedNodesHashMap.put(enhancedNode.node().bpmnObject().id(), enhancedNode);
		}
	}

	public String id()
	{
		return this.id;
	}

	public Cluster copy()
	{
		final Cluster cluster = new Cluster(this.id);

		for (EnhancedNode enhancedNode : this.elements)
		{
			cluster.elements.add(enhancedNode.copy());
		}

		for (Dependency dependency : this.tempDependencies)
		{
			cluster.tempDependencies.add(dependency.copy());
		}

		cluster.setProbability(this.probability);

		for (DependencyGraph dependencyGraph : this.dependencyGraphs)
		{
			cluster.dependencyGraphs.add(dependencyGraph.copy());
		}

		for (AbstractGraph abstractGraph : this.abstractGraphs)
		{
			cluster.abstractGraphs.add(abstractGraph.copy());
		}

		cluster.processed = this.processed;
		cluster.bpmnGraph = this.bpmnGraph;

		for (AbstractGraph abstractGraph : this.possiblyOptimalBalancedClusters)
		{
			cluster.possiblyOptimalBalancedClusters.add(abstractGraph.copy());
		}

		for (EnhancedNode enhancedNode : this.enhancedNodesHashMap.values())
		{
			cluster.enhancedNodesHashMap.put(enhancedNode.node().bpmnObject().id(), enhancedNode.copy());
		}

		cluster.globalDependencies.addAll(new HashSet<>(this.globalDependencies));

		return cluster;
	}

	//Overrides

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Cluster))
		{
			return false;
		}

		return this.id.equals(((Cluster) o).id);
	}

	@Override
	public int hashCode()
	{
		int hash = 7;

		for (int i = 0; i < this.id.length(); i++)
		{
			hash = hash * 31 + this.id.charAt(i);
		}

		return hash;
	}

	//Private methods

	private void computeHash(final StringBuilder builder,
							 final Cluster cluster)
	{
		cluster.dependencyGraphs().sort(Comparator.comparing(DependencyGraph::hash));

		for (DependencyGraph dependencyGraph : cluster.dependencyGraphs())
		{
			builder.append(dependencyGraph.hash())
					.append("/");
		}

		final ArrayList<Node> independentNodes = new ArrayList<>();

		for (EnhancedNode node : this.elements)
		{
			if (node.isIndependent())
			{
				independentNodes.add(node.node());
			}
		}

		independentNodes.sort(Comparator.comparing(o -> o.bpmnObject().name()));

		for (Node node : independentNodes)
		{
			builder.append(node.bpmnObject().name())
					.append("-");
		}

		final ArrayList<String> choiceHashs = new ArrayList<>();
		final ArrayList<String> loopHashs = new ArrayList<>();

		for (EnhancedNode enhancedNode : cluster.elements)
		{
			if (enhancedNode.type() == EnhancedType.CHOICE)
			{
				for (Cluster subCluster : ((EnhancedChoice) enhancedNode).clusters())
				{
					final StringBuilder subBuilder = new StringBuilder();
					this.computeHash(subBuilder, subCluster);
					choiceHashs.add(subBuilder.toString());
				}
			}
			else if (enhancedNode.type() == EnhancedType.LOOP)
			{
				for (Cluster subCluster : ((EnhancedLoop) enhancedNode).clusters())
				{
					final StringBuilder subBuilder = new StringBuilder();
					this.computeHash(subBuilder, subCluster);
					loopHashs.add(subBuilder.toString());
				}
			}
		}

		choiceHashs.sort(Comparator.naturalOrder());
		loopHashs.sort(Comparator.naturalOrder());

		for (String hash : choiceHashs)
		{
			builder.append(hash)
					.append("/");
		}

		for (String hash : loopHashs)
		{
			builder.append(hash)
					.append("/");
		}
	}

	private void replaceSubClusterBy(final Cluster currentCluster,
									 final Cluster replacementCluster)
	{
		for (EnhancedNode enhancedNode : currentCluster.elements)
		{
			if (enhancedNode.type() == EnhancedType.CHOICE)
			{
				final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;

				for (Node node : enhancedChoice.choicePaths().keySet())
				{
					final Cluster cluster = enhancedChoice.choicePaths().get(node);

					if (cluster.equals(replacementCluster))
					{
						enhancedChoice.choicePaths().put(node, replacementCluster);
					}
					else
					{
						this.replaceSubClusterBy(cluster, replacementCluster);
					}
				}
			}
			else if (enhancedNode.type() == EnhancedType.LOOP)
			{
				final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;

				if (enhancedLoop.exitToEntryCluster().equals(replacementCluster))
				{
					enhancedLoop.setExitToEntryCluster(replacementCluster);
				}
				else if (enhancedLoop.entryToExitCluster().equals(replacementCluster))
				{
					enhancedLoop.setEntryToExitCluster(replacementCluster);
				}
				else
				{
					this.replaceSubClusterBy(enhancedLoop.entryToExitCluster(), replacementCluster);
					this.replaceSubClusterBy(enhancedLoop.exitToEntryCluster(), replacementCluster);
				}
			}
		}
	}
}
