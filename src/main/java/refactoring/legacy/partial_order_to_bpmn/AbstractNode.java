package refactoring.legacy.partial_order_to_bpmn;

import other.Utils;
import bpmn.graph.Node;

import java.util.*;

public class AbstractNode
{
	private final String id;
	private final HashSet<Node> listNodes;
	private AbstractNode successor;
	private final ArrayList<AbstractGraph> subGraphs;
	private boolean corrected;
	private boolean isProblematic;

	public AbstractNode()
	{
		this.id = Utils.generateRandomIdentifier();
		this.listNodes = new HashSet<>();
		this.subGraphs = new ArrayList<>();
		this.corrected = false;
		this.isProblematic = false;
		this.successor = null;
	}

	public AbstractNode(final String id)
	{
		this.id = id;
		this.listNodes = new HashSet<>();
		this.subGraphs = new ArrayList<>();
		this.corrected = false;
		this.isProblematic = false;
		this.successor = null;
	}

	public AbstractNode copy()
	{
		final AbstractNode copiedNode = new AbstractNode(this.id);
		if (this.corrected) copiedNode.setCorrected();
		if (this.isProblematic) copiedNode.setProblematic();

		copiedNode.listNodes.addAll(new HashSet<>(this.listNodes));

		for (AbstractGraph abstractGraph : this.subGraphs)
		{
			copiedNode.addSubgraph(abstractGraph.copy());
		}

		return copiedNode;
	}

	public void setCorrected()
	{
		this.corrected = true;
	}

	public boolean hasBeenCorrected()
	{
		return corrected;
	}

	public void setProblematic()
	{
		this.isProblematic = true;
	}

	public boolean isProblematic()
	{
		return this.isProblematic;
	}

	public HashSet<Node> listNodes()
	{
		return this.listNodes;
	}

	public Collection<AbstractGraph> subGraphs()
	{
		return this.subGraphs;
	}

	public AbstractNode successor()
	{
		return this.successor;
	}

	public String id()
	{
		return this.id;
	}

	public void setSuccessor(final AbstractNode abstractNode)
	{
		this.successor = abstractNode;
	}

	public void addNode(final Node node)
	{
		this.listNodes.add(node);
	}

	public void addSubgraph(final AbstractGraph subGraph)
	{
		this.subGraphs.add(subGraph);
	}

	public void addSubgraphAt(final AbstractGraph abstractGraph,
							  final int index)
	{
		this.subGraphs.add(index, abstractGraph);
	}

	public boolean hasSuccessor()
	{
		return this.successor != null;
	}

	public String stringify(final int depth)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("	".repeat(depth))
				.append("Abstract node |")
				.append(this.id)
				.append("| ")
				.append(this.isProblematic ? "(PROBLEMATIC) " : "")
				.append("has the following elements:\n");

		for (Node node : this.listNodes())
		{
			builder.append("	".repeat(depth + 1))
					.append("- ")
					.append(node.bpmnObject().id())
					.append("\n");
		}

		if (!this.subGraphs().isEmpty())
		{
			builder.append("	".repeat(depth))
					.append("And the following subgraphs:\n");

			int i = 1;

			for (AbstractGraph subGraph : this.subGraphs)
			{
				builder.append("	".repeat(depth + 1))
						.append("- Subgraph n°")
						.append(i++)
						.append(" (")
						.append(subGraph.id())
						.append(") ")
						.append(":\n")
						.append(subGraph.stringify(depth + 2));
			}
		}

		if (this.successor != null)
		{
			builder.append(this.successor.stringify(depth + 1))
					.append("\n");
		}

		return builder.toString();
	}
}
