package simulator.bpmn_to_model_input;

public enum GlobalInfo
{
	IAT("IAT"),
	NB_INSTANCES("Nb instances"),
	AVAILABLE_RESOURCES("Global resources");

	private final String label;

	GlobalInfo(final String s)
	{
		this.label = s;
	}

	public String label()
	{
		return this.label;
	}
}
