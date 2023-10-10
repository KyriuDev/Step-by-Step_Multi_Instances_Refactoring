package refactoring.step_by_step;

import other.Utils;

import java.util.HashSet;

public class Tree
{
	private final String id;
	private TreeNode initialNode;

	public Tree()
	{
		this.id = Utils.generateRandomIdentifier(20);
	}

	public Tree(final TreeNode initialNode)
	{
		this.id = Utils.generateRandomIdentifier(20);
		this.initialNode = initialNode;
	}

	public void setInitialNode(final TreeNode treeNode)
	{
		this.initialNode = treeNode;
	}

	public TreeNode initialNode()
	{
		return this.initialNode;
	}

	public int countLeafs()
	{
		return this.countLeafs(this.initialNode, new HashSet<>());
	}

	public int countLeafsV2()
	{
		final HashSet<TreeNode> leafs = new HashSet<>();
		this.countLeafsV2(this.initialNode, leafs);
		return leafs.size();
	}

	public HashSet<TreeNode> showLeafs()
	{
		final HashSet<TreeNode> leafs = new HashSet<>();
		this.countLeafsV2(this.initialNode, leafs);
		return leafs;
	}

	//Private methods

	private void countLeafsV2(final TreeNode currentNode,
							  final HashSet<TreeNode> leafs)
	{
		if (!currentNode.hasChild())
		{
			leafs.add(currentNode);
		}

		for (TreeNode child : currentNode.childNodes())
		{
			this.countLeafsV2(child, leafs);
		}
	}

	private int countLeafs(final TreeNode currentNode,
						   final HashSet<TreeNode> visitedNodes)
	{
		if (visitedNodes.contains(currentNode))
		{
			return 0;
		}

		visitedNodes.add(currentNode);

		if (!currentNode.hasChild())
		{
			return 1;
		}

		int nbLeafs = 0;

		for (TreeNode child : currentNode.childNodes())
		{
			nbLeafs += this.countLeafs(child, visitedNodes);
		}

		return nbLeafs;
	}

	//Overrides

	@Override
	public String toString()
	{
		return this.initialNode.stringify(0);
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

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Tree))
		{
			return false;
		}

		return this.id.equals(((Tree) o).id);
	}
}
