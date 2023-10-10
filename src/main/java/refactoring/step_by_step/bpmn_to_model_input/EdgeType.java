package refactoring.step_by_step.bpmn_to_model_input;

public enum EdgeType
{
	WEAK_FLOW("flow"),
	STRONG_FLOW("dependency");

	public final String label;

	EdgeType(final String s)
	{
		this.label = s;
	}
}
