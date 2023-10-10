package hash.graph;

import hash.HashConstants;

import java.util.ArrayList;
import java.util.HashSet;

public class HashGateway extends AbstractHashNode
{
	private static final String INITIAL_HASH = "!";
	private final HashSet<HashPath> rawPaths;
	private final ArrayList<HashPath> orderedPaths;
	private final boolean isMerge;
	private final boolean isParallel;

	private boolean startOfLoop;
	private String hash;
	private String physicalGatewayId;

	//Constructors

	private HashGateway(final boolean isMerge,
						final boolean isParallel,
						final boolean startOfLoop)
	{
		super(HashConstants.GATEWAY);
		this.rawPaths = new HashSet<>();
		this.orderedPaths = new ArrayList<>();
		this.isMerge = isMerge;
		this.isParallel = isParallel;
		this.startOfLoop = startOfLoop;
		this.hash = INITIAL_HASH;
		this.physicalGatewayId = "";
	}

	public static HashGateway generateExclusiveSplitGateway()
	{
		return new HashGateway(false, false, false);
	}

	public static HashGateway generateExclusiveMergeGateway()
	{
		return new HashGateway(true, false, false);
	}

	public static HashGateway generateStartOfLoopExclusiveMergeGateway()
	{
		return new HashGateway(true, false, true);
	}

	public static HashGateway generateParallelSplitGateway()
	{
		return new HashGateway(false, true, false);
	}

	public static HashGateway generateParallelMergeGateway()
	{
		return new HashGateway(true, true, false);
	}

	//Getters

	public boolean isMerge()
	{
		return this.isMerge;
	}

	public boolean isParallel()
	{
		return this.isParallel;
	}

	public boolean isStartOfLoop()
	{
		return this.startOfLoop;
	}

	public String hash()
	{
		return this.hash;
	}

	public HashSet<HashPath> getRawPaths()
	{
		return this.rawPaths;
	}

	public ArrayList<HashPath> getOrderedPaths()
	{
		return this.orderedPaths;
	}

	public String getPhysicalGatewayId()
	{
		return this.physicalGatewayId;
	}

	//Setters

	public void setHash(final String hash)
	{
		this.hash = hash;
	}

	public void setOrderedPaths(final ArrayList<HashPath> orderedPaths)
	{
		this.orderedPaths.addAll(orderedPaths);
	}

	public void setPhysicalGatewayId(final String id)
	{
		this.physicalGatewayId = id;
	}

	//Public functions

	public void addRawPath(final HashPath path)
	{
		this.rawPaths.add(path);
	}

	public boolean hashComputed()
	{
		return !this.hash.equals(INITIAL_HASH);
	}

	public void switchToLoopStart()
	{
		this.startOfLoop = true;
	}
}
