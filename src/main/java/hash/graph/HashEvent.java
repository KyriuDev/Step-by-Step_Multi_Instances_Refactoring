package hash.graph;

import hash.HashConstants;

public class HashEvent extends AbstractHashNode
{
	private final boolean isStartEvent;

	private HashEvent(final boolean isStartEvent)
	{
		super(HashConstants.EVENT);
		this.isStartEvent = isStartEvent;
	}

	public static HashEvent generateStartEvent()
	{
		return new HashEvent(true);
	}

	public static HashEvent generateEndEvent()
	{
		return new HashEvent(false);
	}

	public boolean isStartEvent()
	{
		return this.isStartEvent;
	}
}
