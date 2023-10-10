package refactoring.step_by_step.bpmn_to_model_input;

public enum DistributionType
{
	CONSTANT("const");

	private final String label;

	DistributionType(final String s)
	{
		this.label = s;
	}

	String label()
	{
		return this.label;
	}
}
