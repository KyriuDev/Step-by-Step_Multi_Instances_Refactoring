package graph_analysis.syntax;

import constants.PrintLevel;
import refactoring.step_by_step.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static main.Main.PRINT_LEVEL;

public class SyntacticRemover
{
	private final ArrayList<TreeNode> finalNodes;
	private final Map<TreeNode, TreeNode> correspondences; //Correspondence between weak nodes (keys) and strong nodes (values)
	private final ArrayList<TreeNode> rawNodes;

	public SyntacticRemover(final Map<TreeNode, ArrayList<TreeNode>> parentWithChild)
	{
		this.finalNodes = new ArrayList<>();
		this.rawNodes = new ArrayList<>();
		this.correspondences = new HashMap<>();

		for (ArrayList<TreeNode> treeNodes : parentWithChild.values())
		{
			this.finalNodes.addAll(treeNodes);
			this.rawNodes.addAll(treeNodes);
		}

		if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_IMPORTANT)
		{
			System.out.println("The current tree has " + this.finalNodes.size() + " leafs.");
		}
	}

	/*public void removeEquivalentNodesV2()
	{
		for (int i = 0; i < this.rawNodes.size(); i++)
		{
			final TreeNode mainNode = this.rawNodes.get(i);
			//Check whether the current node is a key of the correspondences or not.
			//If yes, it is a weak node.
			final boolean canBeStrong = this.correspondences.get(mainNode) == null;

			if (canBeStrong)
			{
				//Verify if all other nodes are syntactically equivalent to the main node
				for (int j = i + 1; j < this.rawNodes.size(); j++)
				{
					final TreeNode secondaryNode = this.rawNodes.get(j);

					if (SyntacticAnalyzer.compare(mainNode.mainCluster(), secondaryNode.mainCluster()))
					{
						*//*System.out.println("First graph:\n\n" + mainNode.refactoredProcess().toString());
						System.out.println("Second graph:\n\n" + secondaryNode.refactoredProcess().toString());

						if (true) throw new IllegalStateException();*//*

						this.finalNodes.remove(secondaryNode);
						this.correspondences.put(secondaryNode, mainNode);
					}
				}

				//Mark the main node as strong
				this.correspondences.put(mainNode, mainNode);
			}
		}

		//Last node is strong if it's not already a key of the map
		this.correspondences.computeIfAbsent(this.rawNodes.get(this.rawNodes.size() - 1), k -> this.rawNodes.get(this.rawNodes.size() - 1));
	}*/

	public Map<TreeNode, TreeNode> getCorrespondences()
	{
		return this.correspondences;
	}

	public int nbOriginalNodes()
	{
		return this.rawNodes.size();
	}
}
