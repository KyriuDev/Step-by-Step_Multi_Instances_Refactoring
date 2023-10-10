package hash.graph;

import hash.HashConstants;

public class HashTask extends AbstractHashNode
{
	private final String name;

	public HashTask(final String name)
	{
		super(HashConstants.TASK);
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}
}
