package refactoring.legacy.partial_order_to_bpmn;

import bpmn.graph.Node;
import other.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class AbstractGraphSimplifier
{
	private AbstractGraphSimplifier()
	{

	}

	public static void simplifyAbstractGraph(final AbstractGraph abstractGraph)
	{
		//Remove useless subgraphs
		final HashSet<AbstractNode> deepestNodes = new HashSet<>();
		AbstractGraphSimplifier.getDeepestUselessSubgraphs(abstractGraph, deepestNodes);

		while (!deepestNodes.isEmpty())
		{
			for (AbstractNode deepNode : deepestNodes)
			{
				for (Iterator<AbstractGraph> iterator = deepNode.subGraphs().iterator(); iterator.hasNext(); )
				{
					final AbstractGraph currentSubgraph = iterator.next();
					final ArrayList<AbstractNode> subgraphNodes = currentSubgraph.extractNodes();

					if (subgraphNodes.size() == 1)
					{
						iterator.remove();
						final AbstractNode uselessNode = subgraphNodes.get(0);

						for (Node node : uselessNode.listNodes())
						{
							deepNode.addNode(node);
						}
					}
				}
			}

			deepestNodes.clear();
			AbstractGraphSimplifier.getDeepestUselessSubgraphs(abstractGraph, deepestNodes);
		}

		//Decapsulate nodes
		final HashSet<Pair<AbstractNode, AbstractNode>> encapsulatingNodes = new HashSet<>();
		AbstractGraphSimplifier.getDeepestEncapsulatingNodes(abstractGraph, encapsulatingNodes);

		while (!encapsulatingNodes.isEmpty())
		{
			for (Pair<AbstractNode, AbstractNode> encapsulatingNode : encapsulatingNodes)
			{
				final AbstractNode previousNode = encapsulatingNode.first();
				final AbstractNode nextNode = encapsulatingNode.second().hasSuccessor() ? encapsulatingNode.second().successor() : null;

				final AbstractNode firstNodeToConnect = encapsulatingNode.second().subGraphs().iterator().next().startNode();
				final AbstractNode lastNodeToConnect = encapsulatingNode.second().subGraphs().iterator().next().lastNode();

				if (previousNode != null)
				{
					previousNode.setSuccessor(firstNodeToConnect);

					if (nextNode != null)
					{
						lastNodeToConnect.setSuccessor(nextNode);
						encapsulatingNode.second().setSuccessor(null);
					}
				}
				else
				{
					abstractGraph.setStartNode(firstNodeToConnect);

					if (nextNode != null)
					{
						lastNodeToConnect.setSuccessor(nextNode);
						encapsulatingNode.second().setSuccessor(null);
					}
				}
			}

			encapsulatingNodes.clear();
			AbstractGraphSimplifier.getDeepestEncapsulatingNodes(abstractGraph, encapsulatingNodes);
		}
	}

	//Private methods

	private static void getDeepestUselessSubgraphs(final AbstractGraph currentGraph,
												   final HashSet<AbstractNode> deepestNodes)
	{
		final ArrayList<AbstractNode> abstractNodes = currentGraph.extractNodes();
		final HashSet<AbstractGraph> nextGraphs = new HashSet<>();

		for (AbstractNode abstractNode : abstractNodes)
		{
			if (!abstractNode.subGraphs().isEmpty())
			{
				for (AbstractGraph subgraph : abstractNode.subGraphs())
				{
					final ArrayList<AbstractNode> subnodes = subgraph.extractNodes();
					boolean validSubgraph = true;

					for (AbstractNode subnode : subnodes)
					{
						if (!subnode.subGraphs().isEmpty())
						{
							validSubgraph = false;
							nextGraphs.add(subgraph);
							//System.out.println("Found " + subnode.subGraphs().size() + " next subgraphs to check");
						}
					}

					if (validSubgraph
						&& subnodes.size() == 1)
					{
						deepestNodes.add(abstractNode);
					}
				}
			}
		}

		for (AbstractGraph nextGraph : nextGraphs)
		{
			AbstractGraphSimplifier.getDeepestUselessSubgraphs(nextGraph, deepestNodes);
		}
	}

	private static void getDeepestEncapsulatingNodes(final AbstractGraph currentGraph,
													 final HashSet<Pair<AbstractNode, AbstractNode>> previousAndCurrentNodes)
	{
		final ArrayList<AbstractNode> abstractNodes = currentGraph.extractNodes();
		final HashSet<AbstractGraph> nextGraphs = new HashSet<>();

		for (int i = 0; i < abstractNodes.size() - 1; i++)
		{
			final AbstractNode previousNode = i == 0 ? null : abstractNodes.get(i);
			final AbstractNode currentNode = abstractNodes.get(i);

			if (!currentNode.subGraphs().isEmpty())
			{
				if (currentNode.listNodes().isEmpty())
				{
					if (currentNode.subGraphs().size() == 1)
					{
						//Eligible candidate
						final ArrayList<AbstractNode> subnodes = currentNode.subGraphs().iterator().next().extractNodes();
						boolean validSubgraph = true;

						for (AbstractNode subnode : subnodes)
						{
							if (subnode.listNodes().isEmpty()
								&& subnode.subGraphs().size() == 1)
							{
								validSubgraph = false;
							}
						}

						if (validSubgraph)
						{
							previousAndCurrentNodes.add(new Pair<>(previousNode, currentNode));
						}
						else
						{
							nextGraphs.addAll(currentNode.subGraphs());
						}
					}
					else
					{
						nextGraphs.addAll(currentNode.subGraphs());
					}
				}
				else
				{
					nextGraphs.addAll(currentNode.subGraphs());
				}
			}
		}

		for (AbstractGraph nextGraph : nextGraphs)
		{
			AbstractGraphSimplifier.getDeepestEncapsulatingNodes(nextGraph, previousAndCurrentNodes);
		}
	}
}
