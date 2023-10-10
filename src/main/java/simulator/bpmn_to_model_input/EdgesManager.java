package simulator.bpmn_to_model_input;

import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.SequenceFlow;
import other.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EdgesManager extends Manager
{
	private static final String EDGES_ATTRIBUTES_FILE = "x_edges_attrs.txt";
	private static final String EDGES_ATTRIBUTES_NAMES_FILE = "x_edges_attrs_names.txt";
	private final Map<Integer, ArrayList<EdgeAttribute>> edgesMap;

	public EdgesManager()
	{
		this.edgesMap = new HashMap<>();
	}

	public void fillMap(final int identifier,
						final ArrayList<BpmnProcessObject> objects)
	{
		final ArrayList<EdgeAttribute> currentList = this.edgesMap.computeIfAbsent(identifier, a -> new ArrayList<>());

		for (BpmnProcessObject object : objects)
		{
			if (object instanceof SequenceFlow)
			{
				final SequenceFlow sequenceFlow = (SequenceFlow) object;
				final EdgeAttribute attribute = new EdgeAttribute(EdgeType.WEAK_FLOW, sequenceFlow.duration(), sequenceFlow.probability());
				currentList.add(attribute);
			}
		}
	}

	@Override
	public void write(final File workingDirectory) throws FileNotFoundException
	{
		final PrintWriter attributesNamesFile = new PrintWriter(Path.of(workingDirectory.getPath(), EDGES_ATTRIBUTES_NAMES_FILE).toString());
		final PrintWriter attributesFile = new PrintWriter(Path.of(workingDirectory.getPath(), EDGES_ATTRIBUTES_FILE).toString());
		final int maxValue = Utils.max(this.edgesMap.keySet());

		for (int i = 0; i <= maxValue; i++)
		{
			final ArrayList<EdgeAttribute> currentEdgeAttributes = this.edgesMap.get(i);

			if (currentEdgeAttributes != null)
			{
				for (EdgeAttribute edgeAttribute : currentEdgeAttributes)
				{
					attributesNamesFile.println(
						EdgeAttribute.EDGE_TYPE + GlobalConstants.SEPARATOR +
						EdgeAttribute.DELAY_DISTRIBUTION + GlobalConstants.SEPARATOR +
						EdgeAttribute.DELAY_DISTRIBUTION_FIRST_PARAMETER + GlobalConstants.SEPARATOR +
						EdgeAttribute.DELAY_DISTRIBUTION_SECOND_PARAMETER + GlobalConstants.SEPARATOR +
						EdgeAttribute.EDGE_PROBABILITY
					);

					attributesFile.println(
						edgeAttribute.edgeType().label() + GlobalConstants.SEPARATOR +
						DistributionUtils.distributionName(edgeAttribute.distribution()) + GlobalConstants.SEPARATOR +
						DistributionUtils.distributionFirstParam(edgeAttribute.distribution()) + GlobalConstants.SEPARATOR +
						DistributionUtils.distributionSecondParam(edgeAttribute.distribution()) + GlobalConstants.SEPARATOR +
						edgeAttribute.probability()
					);
				}
			}
		}

		attributesNamesFile.close();
		attributesFile.close();
	}
}
