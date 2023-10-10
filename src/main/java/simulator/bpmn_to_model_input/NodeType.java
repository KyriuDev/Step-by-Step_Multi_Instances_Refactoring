package simulator.bpmn_to_model_input;

public enum NodeType
{
	TASK("task"),
	START_EVENT("start_event"),
	END_EVENT("end_event"),
	PARALLEL_GATEWAY("parallel_gate"),
	EXCLUSIVE_GATEWAY("exclusive_gate");

	private final String label;

	NodeType(final String s)
	{
		this.label = s;
	}

	public String label()
	{
		return this.label;
	}
}
