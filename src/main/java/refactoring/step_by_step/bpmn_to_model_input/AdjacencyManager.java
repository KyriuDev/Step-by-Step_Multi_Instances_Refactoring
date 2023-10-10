package refactoring.step_by_step.bpmn_to_model_input;

import other.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdjacencyManager
{
	private final Map<Integer, ArrayList<Pair<Integer, Integer>>> adjacencyMap;

	public AdjacencyManager()
	{
		this.adjacencyMap = new HashMap<>();
	}
}
