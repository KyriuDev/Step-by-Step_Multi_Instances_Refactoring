package refactoring.legacy.dependencies;

import bpmn.graph.Node;

public class EnhancedNode
{
	protected final Node node;
	protected boolean isIndependent;
	protected String hash;

	public EnhancedNode(final Node node)
	{
		this.node = node;
		this.isIndependent = false;
		this.hash = node.bpmnObject().name();
	}

	public EnhancedNode(final Node node,
						final boolean isIndependent)
	{
		this.node = node;
		this.isIndependent = isIndependent;
		this.hash = node.bpmnObject().name();
	}

	//Public methods

	public String hash()
	{
		return this.hash;
	}

	public boolean hashComputed()
	{
		return this.hash != null && !this.hash.equals("-1");
	}

	public String setHash(final String hash)
	{
		return this.hash = hash;
	}

	public EnhancedType type()
	{
		return EnhancedType.CLASSICAL;
	}

	public Node node()
	{
		return this.node;
	}

	public String stringify(final int depth)
	{
		return "	".repeat(depth) + "Node |" + this.node.bpmnObject().id() + "|.";
	}

	public EnhancedNode copy()
	{
		return new EnhancedNode(this.node, this.isIndependent);
	}

	public void setIndependent()
	{
		this.isIndependent = true;
	}

	public void setDependent()
	{
		this.isIndependent = false;
	}

	public boolean isIndependent()
	{
		return this.isIndependent;
	}

	//Overrides
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof EnhancedNode))
		{
			return false;
		}

		return this.node.equals(((EnhancedNode) o).node);
	}

	@Override
	public int hashCode()
	{
		return this.node.hashCode();
	}
}
