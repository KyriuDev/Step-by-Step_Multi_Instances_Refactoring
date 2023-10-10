package refactoring.legacy.dependencies;

import bpmn.graph.Node;
import refactoring.legacy.Cluster;

public class EnhancedLoop extends EnhancedNode
{
	private Cluster entryToExitCluster; //Cluster of elements between entry point and exit point of the loop
	private Cluster exitToEntryCluster; //Cluster of elements between exit point and entry point of the loop

	public EnhancedLoop(final Node node)
	{
		super(node);
		this.entryToExitCluster = new Cluster();
		this.exitToEntryCluster = new Cluster();
	}

	public EnhancedLoop(final Node node,
						final boolean isIndependent)
	{
		super(node, isIndependent);
		this.entryToExitCluster = new Cluster();
		this.exitToEntryCluster = new Cluster();
	}

	//Public methods

	public Cluster entryToExitCluster()
	{
		return this.entryToExitCluster;
	}

	public Cluster exitToEntryCluster()
	{
		return this.exitToEntryCluster;
	}

	public void setEntryToExitCluster(final Cluster cluster)
	{
		this.entryToExitCluster = cluster;
	}

	public void setExitToEntryCluster(final Cluster cluster)
	{
		this.exitToEntryCluster = cluster;
	}

	public Cluster[] clusters()
	{
		return new Cluster[]{this.entryToExitCluster, this.exitToEntryCluster};
	}

	//Overrides

	@Override
	public String stringify(final int depth)
	{
		return "	".repeat(depth) +
				"Loop |" +
				this.node.bpmnObject().id() +
				"| has paths:\n" +
				"	".repeat(depth + 1) +
				"- Path from entry to exit:\n" +
				entryToExitCluster.stringify(depth + 2) +
				"\n" +
				"	".repeat(depth + 1) +
				"- Path from exit to entry:\n" +
				this.exitToEntryCluster.stringify(depth + 2) +
				"\n";
	}

	@Override
	public EnhancedType type()
	{
		return EnhancedType.LOOP;
	}

	@Override
	public EnhancedLoop copy()
	{
		final EnhancedLoop enhancedLoop = new EnhancedLoop(this.node, this.isIndependent);
		enhancedLoop.entryToExitCluster = this.entryToExitCluster.copy();
		enhancedLoop.exitToEntryCluster = this.exitToEntryCluster.copy();

		return enhancedLoop;
	}

	@Override
	public boolean hashComputed()
	{
		return this.hash != null && !this.hash.equals(node.bpmnObject().name());
	}
}
