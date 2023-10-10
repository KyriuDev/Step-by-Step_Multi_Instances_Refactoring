package hash.graph;

import other.Utils;

public abstract class AbstractHashNode
{
	private final String id;
	private final int type;

	public AbstractHashNode(final int type)
	{
		this.type = type;
		this.id = Utils.generateRandomIdentifier(15);
	}

	public int getType()
	{
		return this.type;
	}
}
