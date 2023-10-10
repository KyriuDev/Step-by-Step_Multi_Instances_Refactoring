package simulator.bpmn_to_model_input;

public enum EdgeType
{
	WEAK_FLOW("flow"),
	STRONG_FLOW("dependency");

	private final String label;

	EdgeType(final String s)
	{
		this.label = s;
	}

	public String label()
	{
		return this.label;
	}
}
