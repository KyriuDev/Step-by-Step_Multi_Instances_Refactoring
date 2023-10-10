package simulator.bpmn_to_model_input;

import other.Pair;
import other.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExecutionAttributesManager extends Manager
{
	//Contains for each graph (identified by its index) the execution attributes (see class ExecutionAttributes)
	private static final String EXECUTION_ATTRIBUTES_FILE = "execution_attrs.txt";
	private static final String EXECUTION_ATTRIBUTES_NAMES_FILE = "execution_attrs_names.txt";
	private final Map<Integer, ExecutionAttribute> executionAttributes;

	public ExecutionAttributesManager()
	{
		this.executionAttributes = new HashMap<>();
	}

	public void addExecutionAttribute(final int identifier,
									  final ExecutionAttribute executionAttribute)
	{
		this.executionAttributes.put(identifier, executionAttribute);
	}

	@Override
	public void write(final File workingDirectory) throws FileNotFoundException
	{
		final PrintWriter attributesNamesFile = new PrintWriter(Path.of(workingDirectory.getPath(), EXECUTION_ATTRIBUTES_NAMES_FILE).toString());
		final PrintWriter attributesFile = new PrintWriter(Path.of(workingDirectory.getPath(), EXECUTION_ATTRIBUTES_FILE).toString());
		final int maxValue = Utils.max(this.executionAttributes.keySet());

		for (int i = 0; i <= maxValue; i++)
		{
			final ExecutionAttribute currentExecutionAttribute = this.executionAttributes.get(i);

			if (currentExecutionAttribute != null)
			{
				attributesNamesFile.println(
					ExecutionAttribute.AET + GlobalConstants.SEPARATOR +
					ExecutionAttribute.ET + GlobalConstants.SEPARATOR +
					ExecutionAttribute.COST + GlobalConstants.SEPARATOR +
					ExecutionAttribute.NB_INSTANCES
				);

				attributesFile.println(
					currentExecutionAttribute.aet() + GlobalConstants.SEPARATOR +
					currentExecutionAttribute.et() + GlobalConstants.SEPARATOR +
					currentExecutionAttribute.cost() + GlobalConstants.SEPARATOR +
					currentExecutionAttribute.nbInstances()
				);
			}
		}

		attributesNamesFile.close();
		attributesFile.close();
	}

	public Map<Integer, ExecutionAttribute> executionAttributes()
	{
		return this.executionAttributes;
	}
}
