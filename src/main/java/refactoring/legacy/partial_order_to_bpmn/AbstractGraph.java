package refactoring.legacy.partial_order_to_bpmn;

import other.Utils;
import bpmn.graph.Node;

import java.util.ArrayList;
import java.util.HashSet;

public class AbstractGraph
{
	private boolean isUseful;
	private AbstractNode startNode;
	private final HashSet<Node> allNodes;
	private final String id;

	public AbstractGraph()
	{
		this.id = Utils.generateRandomIdentifier();
		this.allNodes = new HashSet<>();
		this.isUseful = false;
	}

	public AbstractGraph(final AbstractNode startNode)
	{
		this.id = Utils.generateRandomIdentifier();
		this.allNodes = new HashSet<>();
		this.setStartNode(startNode);
		this.isUseful = false;
	}

	public AbstractGraph(final String id)
	{
		this.id = id;
		this.allNodes = new HashSet<>();
		this.isUseful = false;
	}

	public AbstractGraph(final AbstractNode startNode,
						 final String id)
	{
		this.id = id;
		this.allNodes = new HashSet<>();
		this.setStartNode(startNode);
		this.isUseful = false;
	}

	//Public methods

	public void setUseful()
	{
		this.isUseful = true;
	}

	public boolean isUseful()
	{
		return this.isUseful;
	}

	public boolean isEmpty()
	{
		AbstractNode currentNode = this.startNode;

		if (!currentNode.subGraphs().isEmpty()
			|| !currentNode.listNodes().isEmpty())
		{
			return false;
		}

		while (currentNode.hasSuccessor())
		{
			currentNode = currentNode.successor();

			if (!currentNode.subGraphs().isEmpty()
					|| !currentNode.listNodes().isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	public AbstractGraph findSubgraphStartingWith(final AbstractNode abstractNode)
	{
		if (this.startNode.id().equals(abstractNode.id()))
		{
			return this;
		}

		AbstractNode currentNode = this.startNode;

		for (AbstractGraph abstractGraph : currentNode.subGraphs())
		{
			final AbstractGraph graph = abstractGraph.findSubgraphStartingWith(abstractNode);

			if (graph != null)
			{
				return graph;
			}
		}

		while (currentNode.hasSuccessor())
		{
			currentNode = currentNode.successor();

			for (AbstractGraph abstractGraph : currentNode.subGraphs())
			{
				final AbstractGraph graph = abstractGraph.findSubgraphStartingWith(abstractNode);

				if (graph != null)
				{
					return graph;
				}
			}
		}

		return null;
	}

	public AbstractNode findNodeOfID(final String id)
	{
		AbstractNode currentNode = this.startNode;

		if (currentNode.id().equals(id))
		{
			return currentNode;
		}

		for (AbstractGraph abstractGraph : currentNode.subGraphs())
		{
			final AbstractNode node = abstractGraph.findNodeOfID(id);

			if (node != null)
			{
				return node;
			}
		}

		while (currentNode.hasSuccessor())
		{
			currentNode = currentNode.successor();

			if (currentNode.id().equals(id))
			{
				return currentNode;
			}

			for (AbstractGraph abstractGraph : currentNode.subGraphs())
			{
				final AbstractNode node = abstractGraph.findNodeOfID(id);

				if (node != null)
				{
					return node;
				}
			}
		}

		return null;
	}

	public AbstractNode findPreviousNodeOfNode(final AbstractNode abstractNode)
	{
		AbstractNode currentNode = this.startNode;

		if (abstractNode.id().equals(currentNode.id()))
		{
			//First node has no previous node
			return null;
		}

		if (currentNode.hasSuccessor()
			&& currentNode.successor().id().equals(abstractNode.id()))
		{
			return currentNode;
		}

		for (AbstractGraph abstractGraph : currentNode.subGraphs())
		{
			final AbstractNode node = abstractGraph.findPreviousNodeOfNode(abstractNode);

			if (node != null)
			{
				return node;
			}
		}

		while (currentNode.hasSuccessor())
		{
			currentNode = currentNode.successor();

			if (currentNode.hasSuccessor()
				&& currentNode.successor().id().equals(abstractNode.id()))
			{
				return currentNode;
			}

			for (AbstractGraph abstractGraph : currentNode.subGraphs())
			{
				final AbstractNode node = abstractGraph.findPreviousNodeOfNode(abstractNode);

				if (node != null)
				{
					return node;
				}
			}
		}

		return null;
	}

	public ArrayList<AbstractNode> extractNodes()
	{
		//System.out.println(this.stringify(0));

		final ArrayList<AbstractNode> nodes = new ArrayList<>();
		AbstractNode currentNode = this.startNode;
		nodes.add(currentNode);

		while (currentNode.hasSuccessor())
		{
			if (nodes.size() > 10000) throw new IllegalStateException("Deadlock in AbstractGraph.extractNodes()");

			currentNode = currentNode.successor();
			nodes.add(currentNode);
		}

		return nodes;
	}

	public void computeAllNodes()
	{
		this.computeAllNodes(this.startNode);
	}

	public void setStartNode(final AbstractNode abstractNode)
	{
		this.startNode = abstractNode;
	}

	public String id()
	{
		return this.id;

	}

	public String stringify(final int depth)
	{
		return this.startNode.stringify(depth);
	}
	public AbstractNode startNode()
	{
		return this.startNode;
	}

	public AbstractNode lastNode()
	{
		AbstractNode currentNode = this.startNode;

		while (currentNode.hasSuccessor())
		{
			currentNode = currentNode.successor();
		}

		return currentNode;
	}

	public HashSet<Node> allNodes()
	{
		return this.allNodes;
	}

	public AbstractGraph copy()
	{
		final AbstractGraph copiedGraph = new AbstractGraph(this.id);
		AbstractNode currentNewNode = this.startNode.copy();
		copiedGraph.setStartNode(currentNewNode);
		AbstractNode currentOldNode = this.startNode;
		copiedGraph.allNodes.addAll(new HashSet<>(this.allNodes));

		while (currentOldNode.hasSuccessor())
		{
			currentOldNode = currentOldNode.successor();
			final AbstractNode nextNewNode = currentOldNode.copy();
			currentNewNode.setSuccessor(nextNewNode);
			currentNewNode = nextNewNode;
		}

		return copiedGraph;
	}

	//Override

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof AbstractGraph))
		{
			return false;
		}

		return ((AbstractGraph) o).id.equals(this.id);
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

	private void computeAllNodes(final AbstractNode abstractNode)
	{
		this.allNodes.addAll(abstractNode.listNodes());

		if (abstractNode.hasSuccessor())
		{
			this.computeAllNodes(abstractNode.successor());
		}

		for (AbstractGraph subGraph : abstractNode.subGraphs())
		{
			this.computeAllNodes(subGraph.startNode());
			subGraph.computeAllNodes();
		}
	}
}
