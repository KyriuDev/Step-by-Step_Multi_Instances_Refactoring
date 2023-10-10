package simulator.bpmn_to_model_input;

import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.SequenceFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodesMapper
{
	public static int currentMapping = 0;

	private NodesMapper()
	{
	}

	public static Map<String, Integer> map(final ArrayList<BpmnProcessObject> objects)
	{
		//int currentMapping = 0;
		final Map<String, Integer> map = new HashMap<>();

		for (BpmnProcessObject object : objects)
		{
			if (!(object instanceof SequenceFlow))
			{
				map.putIfAbsent(object.id(), currentMapping++);
			}
		}

		return map;
	}
}
