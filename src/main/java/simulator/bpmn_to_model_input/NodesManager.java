package simulator.bpmn_to_model_input;

import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.BpmnProcessType;
import bpmn.types.process.SequenceFlow;
import bpmn.types.process.Task;
import bpmn.types.process.events.Event;
import org.apache.commons.math3.distribution.ConstantRealDistribution;
import other.Utils;
import resources.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodesManager extends Manager
{
	private static final String NODE_ATTRIBUTES_FILE = "x_node_attrs.txt";
	private static final String NODE_ATTRIBUTES_NAMES_FILE = "x_node_attrs_names.txt";
	private static final String NODE_LABELS_FILE = "x_node_labels.txt";
	private final Map<Integer, ArrayList<NodeAttribute>> nodesMap;

	public NodesManager()
	{
		this.nodesMap = new HashMap<>();
	}

	public void fillMap(final int identifier,
						final ArrayList<BpmnProcessObject> objects)
	{
		final ArrayList<NodeAttribute> currentList = this.nodesMap.computeIfAbsent(identifier, a -> new ArrayList<>());

		for (BpmnProcessObject object : objects)
		{
			if (!(object instanceof SequenceFlow))
			{
				final NodeAttribute nodeAttribute;

				if (object instanceof Task)
				{
					nodeAttribute = new NodeAttribute(
						NodeType.TASK,
						new ConstantRealDistribution(((Task) object).duration()), //Modify to directly use AbstractRealDistribution
						((Task) object).resourceUsage(),
						object.name()
					);
				}
				else
				{
					final String label;

					if (object instanceof Event)
					{
						label = object.id();
					}
					else
					{
						label = object.name();
					}

					nodeAttribute = new NodeAttribute(
						this.switchInternalTypeToModelType(object.type()),
						null,
						null,
						label
					);
				}

				currentList.add(nodeAttribute);
			}
		}
	}

	@Override
	public void write(final File workingDirectory) throws FileNotFoundException
	{
		final PrintWriter printWriter1 = new PrintWriter(Path.of(workingDirectory.getPath(), NODE_ATTRIBUTES_NAMES_FILE).toString());
		final PrintWriter printWriter2 = new PrintWriter(Path.of(workingDirectory.getPath(), NODE_ATTRIBUTES_FILE).toString());
		final PrintWriter printWriter3 = new PrintWriter(Path.of(workingDirectory.getPath(), NODE_LABELS_FILE).toString());
		final int maxValue = Utils.max(this.nodesMap.keySet());

		for (int i = 0; i <= maxValue; i++)
		{
			final ArrayList<NodeAttribute> currentNodeAttributes = this.nodesMap.get(i);

			if (currentNodeAttributes != null)
			{
				for (NodeAttribute nodeAttribute : currentNodeAttributes)
				{
					if (nodeAttribute.type() == NodeType.TASK)
					{
						printWriter1.print(
							NodeAttribute.NODE_TYPE + GlobalConstants.SEPARATOR +
							NodeAttribute.DURATION_DISTRIBUTION + GlobalConstants.SEPARATOR +
							NodeAttribute.DURATION_DISTRIBUTION_FIRST_PARAM + GlobalConstants.SEPARATOR +
							NodeAttribute.DURATION_DISTRIBUTION_SECOND_PARAM
						);

						for (Resource resource : nodeAttribute.resources().resources())
						{
							printWriter1.print(GlobalConstants.SEPARATOR + resource.name());
						}

						printWriter1.println();

						printWriter2.print(
							nodeAttribute.type().label() + GlobalConstants.SEPARATOR +
							DistributionUtils.distributionName(nodeAttribute.distribution()) + GlobalConstants.SEPARATOR +
							DistributionUtils.distributionFirstParam(nodeAttribute.distribution()) + GlobalConstants.SEPARATOR +
							DistributionUtils.distributionSecondParam(nodeAttribute.distribution())
						);

						for (Resource resource : nodeAttribute.resources().resources())
						{
							printWriter2.print(GlobalConstants.SEPARATOR + nodeAttribute.resources().getUsageOf(resource));
						}

						printWriter2.println();
					}
					else
					{
						printWriter1.println(NodeAttribute.NODE_TYPE);
						printWriter2.println(nodeAttribute.type().label());
					}

					printWriter3.println(nodeAttribute.label());
				}
			}
		}

		printWriter1.close();
		printWriter2.close();
		printWriter3.close();
	}

	//Private methods

	private NodeType switchInternalTypeToModelType(final BpmnProcessType type)
	{
		if (type == BpmnProcessType.START_EVENT)
		{
			return NodeType.START_EVENT;
		}
		else if (type == BpmnProcessType.END_EVENT)
		{
			return NodeType.END_EVENT;
		}
		else if (type == BpmnProcessType.EXCLUSIVE_GATEWAY)
		{
			return NodeType.EXCLUSIVE_GATEWAY;
		}
		else if (type == BpmnProcessType.PARALLEL_GATEWAY)
		{
			return NodeType.PARALLEL_GATEWAY;
		}
		else
		{
			throw new UnsupportedOperationException("Type |" + type + "| is not supported (yet).");
		}
	}
}
