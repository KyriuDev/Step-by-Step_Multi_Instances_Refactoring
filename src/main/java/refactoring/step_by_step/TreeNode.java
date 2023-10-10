package refactoring.step_by_step;

import bpmn.types.process.Task;
import other.Utils;
import refactoring.legacy.Cluster;
import simulator.model.SimulationResult;

import java.util.ArrayList;
import java.util.HashSet;

public class TreeNode
{
	private double score;
	private String hash;
	private final ArrayList<SimulationResult> results;
	private final String id;
	private final Cluster mainCluster;
	private final Task movedTask;
	//private final HashSet<Node> alreadyMovedNodes;
	private final HashSet<TreeNode> childNodes;
	private final HashSet<TreeNode> parentNodes;

	public TreeNode(final Cluster mainCluster,
					final Task movedTask)
	{
		this.id = Utils.generateRandomIdentifier(15);
		this.mainCluster = mainCluster;
		//this.alreadyMovedNodes = alreadyMovedNodes;
		this.childNodes = new HashSet<>();
		this.parentNodes = new HashSet<>();
		this.movedTask = movedTask;
		this.results = new ArrayList<>();
	}

	public Cluster mainCluster()
	{
		return this.mainCluster;
	}
	public void addChild(final TreeNode treeNode)
	{
		this.childNodes.add(treeNode);
	}

	public void addParent(final TreeNode treeNode)
	{
		this.parentNodes.add(treeNode);
	}

	public HashSet<TreeNode> childNodes()
	{
		return this.childNodes;
	}

	public HashSet<TreeNode> parentNodes()
	{
		return this.parentNodes;
	}

	public boolean hasChild()
	{
		return !this.childNodes.isEmpty();
	}

	public boolean hasParents()
	{
		return !this.parentNodes.isEmpty();
	}

	public ArrayList<TreeNode> computePathToInitialNode() //TODO Several paths may exist since tree has been reduced using syntactic analysis, see if needed to handle them
	{
		final ArrayList<TreeNode> path = new ArrayList<>();
		TreeNode currentNode = this;
		path.add(currentNode);

		while (currentNode.hasParents())
		{
			currentNode = currentNode.parentNodes().iterator().next();
			path.add(currentNode);
		}

		return path;
	}

	public Task movedTask()
	{
		return this.movedTask;
	}

	public void addSimulationResult(final SimulationResult result)
	{
		this.results.add(result);
	}

	public void addAllSimulationResults(final TreeNode parent)
	{
		this.results.addAll(parent.results());
	}

	public ArrayList<SimulationResult> results()
	{
		return this.results;
	}

	public SimulationResult lastResult()
	{
		return this.results.get(this.results.size() - 1);
	}

	public void setScore(final double score)
	{
		this.score = score;
	}

	public double score()
	{
		return this.score;
	}

	public String stringify(final int depth)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("\n")
				.append("	".repeat(depth))
				.append("- Current node is |")
				.append(this.id)
				.append("| and has ")
				.append(this.childNodes.size())
				.append(" children")
				.append(this.childNodes.isEmpty() ? "." : ":");

		for (TreeNode child : this.childNodes)
		{
			builder.append(child.stringify(depth + 1));
		}

		return builder.toString();
	}

	public void setHash(final String hash)
	{
		this.hash = hash;
	}

	public String getHash()
	{
		return this.hash;
	}

	//Overrides

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

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof TreeNode))
		{
			return false;
		}

		return this.id.equals(((TreeNode) o).id);
	}
}
