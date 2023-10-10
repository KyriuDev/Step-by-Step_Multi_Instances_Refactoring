package refactoring.step_by_step.bpmn_to_model_input;

import java.util.HashMap;
import java.util.Map;

public class ExecutionAttributesManager
{
	//Contains for each graph (identified by its index) the execution attributes (see class ExecutionAttributes)
	private final Map<Integer, ExecutionAttribute> executionAttributes;

	public ExecutionAttributesManager()
	{
		this.executionAttributes = new HashMap<>();
	}
}
