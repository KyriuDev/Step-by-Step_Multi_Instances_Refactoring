package refactoring.step_by_step.bpmn_to_model_input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EdgesManager
{
	private final Map<Integer, ArrayList<EdgeAttribute>> edgesMap;

	public EdgesManager()
	{
		this.edgesMap = new HashMap<>();
	}
}
