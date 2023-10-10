package refactoring.step_by_step.bpmn_to_model_input;

import java.util.HashMap;
import java.util.Map;

public class GraphsManager
{
	private final Map<Integer, GraphAttribute> graphAttributes;

	public GraphsManager()
	{
		this.graphAttributes = new HashMap<>();
	}
}
