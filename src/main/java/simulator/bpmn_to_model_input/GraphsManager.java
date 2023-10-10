package simulator.bpmn_to_model_input;

import other.Utils;
import resources.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GraphsManager extends Manager
{
	private static final String COST_KEYWORD = "cost";
	private static final String GRAPH_ATTRIBUTES_NAMES_FILE = "x_graph_attrs_names.txt";
	private static final String GRAPH_ATTRIBUTES_FILE = "x_graph_attrs.txt";
	private static final String GRAPH_INDICATOR_FILE = "x_graph_indicator.txt";
	private final Map<Integer, GraphAttribute> graphAttributes;

	public GraphsManager()
	{
		this.graphAttributes = new HashMap<>();
	}

	public void addAttribute(final int identifier,
							 final GraphAttribute graphAttribute)
	{
		this.graphAttributes.put(identifier, graphAttribute);
	}

	@Override
	public void write(final File workingDirectory) throws FileNotFoundException
	{
		final PrintWriter attributesNamesFile = new PrintWriter(Path.of(workingDirectory.getPath(), GRAPH_ATTRIBUTES_NAMES_FILE).toString());
		final PrintWriter attributesFile = new PrintWriter(Path.of(workingDirectory.getPath(), GRAPH_ATTRIBUTES_FILE).toString());
		final PrintWriter indicatorFile = new PrintWriter(Path.of(workingDirectory.getPath(), GRAPH_INDICATOR_FILE).toString());
		final int maxValue = Utils.max(this.graphAttributes.keySet());

		for (int i = 0; i <= maxValue; i++)
		{
			final GraphAttribute currentGraphAttribute = this.graphAttributes.get(i);

			if (currentGraphAttribute != null)
			{
				attributesNamesFile.print(
					GraphAttribute.IAT_DISTRIBUTION + GlobalConstants.SEPARATOR +
					GraphAttribute.IAT_DISTRIBUTION_FIRST_PARAM + GlobalConstants.SEPARATOR +
					GraphAttribute.IAT_DISTRIBUTION_SECOND_PARAM
				);

				for (Resource resource : currentGraphAttribute.resourceNames().resources())
				{
					attributesNamesFile.print(GlobalConstants.SEPARATOR + resource.name());
					attributesNamesFile.print(GlobalConstants.SEPARATOR + resource.name() + " " + COST_KEYWORD);
				}

				attributesNamesFile.println();

				attributesFile.print(
					DistributionUtils.distributionName(currentGraphAttribute.distribution()) + GlobalConstants.SEPARATOR +
					DistributionUtils.distributionFirstParam(currentGraphAttribute.distribution()) + GlobalConstants.SEPARATOR +
					DistributionUtils.distributionSecondParam(currentGraphAttribute.distribution())
				);

				for (Resource resource : currentGraphAttribute.resourceNames().resources())
				{
					attributesFile.print(GlobalConstants.SEPARATOR + currentGraphAttribute.resourceNames().getUsageOf(resource));
					attributesFile.print(GlobalConstants.SEPARATOR + resource.cost());
				}

				attributesFile.println();

				for (int j = 0; j < currentGraphAttribute.nbNodesTotal(); j++)
				{
					indicatorFile.println(i);
				}
			}
		}

		attributesNamesFile.close();
		attributesFile.close();
		indicatorFile.close();
	}

	public Map<Integer, GraphAttribute> graphAttributes()
	{
		return this.graphAttributes;
	}
}
