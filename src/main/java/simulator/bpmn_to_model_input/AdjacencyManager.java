package simulator.bpmn_to_model_input;

import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.SequenceFlow;
import other.Pair;
import other.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdjacencyManager extends Manager
{
	private static final String ADJACENCY_FILE = "x_A.txt";
	private final Map<Integer, ArrayList<Pair<Integer, Integer>>> adjacencyMap;

	public AdjacencyManager()
	{
		this.adjacencyMap = new HashMap<>();
	}

	public void fillMap(final int identifier,
						final ArrayList<BpmnProcessObject> objects,
						final Map<String, Integer> map)
	{
		final ArrayList<Pair<Integer, Integer>> currentList = adjacencyMap.computeIfAbsent(identifier, a -> new ArrayList<>());

		for (BpmnProcessObject object : objects)
		{
			if (object instanceof SequenceFlow)
			{
				final SequenceFlow sequenceFlow = (SequenceFlow) object;
				final int sourceObject = map.get(sequenceFlow.sourceRef());
				final int targetObject = map.get(sequenceFlow.targetRef());
				currentList.add(new Pair<>(sourceObject, targetObject));
			}
		}
	}

	@Override
	public void write(final File workingDirectory) throws FileNotFoundException
	{
		final PrintWriter printWriter = new PrintWriter(Path.of(workingDirectory.getPath(), ADJACENCY_FILE).toString());
		final int maxValue = Utils.max(this.adjacencyMap.keySet());

		for (int i = 0; i <= maxValue; i++)
		{
			final ArrayList<Pair<Integer, Integer>> currentAdjacencies = this.adjacencyMap.get(i);

			if (currentAdjacencies != null)
			{
				for (Pair<Integer, Integer> currentAdjacency : currentAdjacencies)
				{
					printWriter.println(currentAdjacency.first() + GlobalConstants.SEPARATOR + currentAdjacency.second());
				}
			}
		}

		printWriter.close();
	}
}
