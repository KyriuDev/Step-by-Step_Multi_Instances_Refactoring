package hash.graph;

import hash.HashConstants;

import java.util.ArrayList;

public class HashPath
{
	private final ArrayList<AbstractHashNode> immutableNodes;
	private final ArrayList<AbstractHashNode> modifiableNodes;

	public HashPath()
	{
		this.immutableNodes = new ArrayList<>();
		this.modifiableNodes = new ArrayList<>();
	}

	public void addNode(final AbstractHashNode node)
	{
		this.immutableNodes.add(node);
		this.modifiableNodes.add(node);
	}

	public ArrayList<AbstractHashNode> getImmutableNodes()
	{
		return this.immutableNodes;
	}

	public ArrayList<AbstractHashNode> getModifiableNodes()
	{
		return this.modifiableNodes;
	}

	public AbstractHashNode getImmutableNodeAt(final int position)
	{
		return this.immutableNodes.get(position);
	}

	public AbstractHashNode getModifiableNodeAt(final int position)
	{
		return this.modifiableNodes.get(position);
	}

	public AbstractHashNode removeFirstNode()
	{
		if (this.modifiableNodes.isEmpty()) return null;

		return this.modifiableNodes.remove(0);
	}

	public boolean isEmpty()
	{
		return this.modifiableNodes.isEmpty();
	}

	public String stringify(final int depth)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("	".repeat(depth));
		builder.append("- Path contains the following elements:\n");

		for (AbstractHashNode node : this.immutableNodes)
		{
			if (node.getType() == HashConstants.GATEWAY)
			{
				final HashGateway gateway = (HashGateway) node;
				builder.append("	".repeat(depth + 1));
				builder.append("- ");
				builder.append(gateway.isParallel() ? "Parallel" : "Exclusive");
				builder.append(gateway.isMerge() ? " merge" : " split");
				builder.append(" gateway");

				for (HashPath path : gateway.getRawPaths())
				{
					builder.append(path.stringify(depth + 2));
				}

				builder.append("\n");
			}
			else if (node.getType() == HashConstants.TASK)
			{
				builder.append("- Task ");
				builder.append(((HashTask) node).getName());
				builder.append("\n");
			}
			else if (node.getType() == HashConstants.EVENT)
			{
				builder.append("- End event");
				builder.append("\n");
			}
		}

		return builder.toString();
	}
}
