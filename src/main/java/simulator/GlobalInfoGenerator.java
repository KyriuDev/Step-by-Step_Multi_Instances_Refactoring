package simulator;

import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.SequenceFlow;
import bpmn.types.process.Task;
import constants.CommandLineOption;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;
import other.Utils;
import resources.Resource;
import resources.ResourcePool;
import simulator.bpmn_to_model_input.DistributionUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GlobalInfoGenerator
{
	private GlobalInfoGenerator()
	{

	}

	public static Map<GlobalInfoEnum, Object> generateGlobalInformation(final ArrayList<BpmnProcessObject> objects)
	{
		final Map<GlobalInfoEnum, Object> globalInfo = new HashMap<>();

		final AbstractRealDistribution iat = GlobalInfoGenerator.computeIAT(objects);
		globalInfo.put(GlobalInfoEnum.IAT, iat);

		final int nbInstances = GlobalInfoGenerator.computeNbInstances();
		globalInfo.put(GlobalInfoEnum.NB_INSTANCES, nbInstances);

		final ResourcePool globalPool = GlobalInfoGenerator.computeGlobalPool(objects);
		globalInfo.put(GlobalInfoEnum.GLOBAL_RESOURCE_POOL, globalPool);

		return globalInfo;
	}

	public static void writeGlobalInfo(final Map<GlobalInfoEnum, Object> globalInfo,
									   final File workingDirectory,
									   final String identifier) throws IOException
	{
		final Path path = Path.of(workingDirectory.getPath(), Utils.getGlobalInfoFileName(identifier));
		final PrintWriter printWriter = new PrintWriter(path.toString(), StandardCharsets.UTF_8);

		//IAT
		final AbstractRealDistribution iat = (AbstractRealDistribution) globalInfo.get(GlobalInfoEnum.IAT);
		printWriter.print("- IAT: ");
		printWriter.println(
			DistributionUtils.distributionName(iat) + "," +
			DistributionUtils.distributionFirstParam(iat) + "," +
			DistributionUtils.distributionSecondParam(iat)
		);

		//Nb instances
		printWriter.print("- Nb instances: ");
		printWriter.println(globalInfo.get(GlobalInfoEnum.NB_INSTANCES));

		//Global resources
		final ResourcePool globalPool = (ResourcePool) globalInfo.get(GlobalInfoEnum.GLOBAL_RESOURCE_POOL);
		String separator = "";
		printWriter.print("- Global resources: <");

		for (Resource resource : globalPool.resources())
		{
			final int nbReplicas = globalPool.getUsageOf(resource);
			printWriter.print(separator);
			printWriter.print(nbReplicas);
			printWriter.print(resource.name());
			printWriter.print(" (");
			printWriter.print(resource.cost());
			printWriter.print(")");
			separator = ", ";
		}

		printWriter.println(">");
		printWriter.close();
	}

	//Private main methods

	private static AbstractRealDistribution computeIAT(final ArrayList<BpmnProcessObject> objects)
	{
		final Random random = new Random();
		final int objectsDuration = GlobalInfoGenerator.sumObjectsDurations(objects);

		return new ConstantRealDistribution(random.nextInt(objectsDuration / 2) + 1); //Very basic approximation of process ET
	}

	private static int computeNbInstances()
	{
		/*final Random random = new Random();
		return random.nextInt(GenerationConstants.MAX_NUMBER_OF_INSTANCES) + 2; //At least two instances*/
		return 1000;
	}

	private static ResourcePool computeGlobalPool(final ArrayList<BpmnProcessObject> objects)
	{
		final ResourcePool minPool = GlobalInfoGenerator.computeMinimalPool(objects);
		final ResourcePool globalPool = new ResourcePool();

		for (Resource resource : minPool.resources())
		{
			final int maxSingleUsage = minPool.getUsageOf(resource);
			final Random random = new Random();
			final int nbAvailableResources = random.nextInt(maxSingleUsage * 5 - maxSingleUsage) + maxSingleUsage;
			final int cost = random.nextInt(GenerationConstants.MAX_RESOURCE_COST) + 1;
			resource.setCost(cost);
			globalPool.addResource(resource, nbAvailableResources);
		}

		return globalPool;
	}

	//Private secondary methods

	private static int sumObjectsDurations(final ArrayList<BpmnProcessObject> objects)
	{
		int duration = 0;

		for (BpmnProcessObject object : objects)
		{
			if (object instanceof Task)
			{
				duration += ((Task) object).duration();
			}
			else if (object instanceof SequenceFlow)
			{
				duration += ((SequenceFlow) object).duration().getNumericalMean();
			}
		}

		return duration;
	}

	private static ResourcePool computeMinimalPool(final ArrayList<BpmnProcessObject> objects)
	{
		final ResourcePool resourcePool = new ResourcePool();

		for (BpmnProcessObject object : objects)
		{
			if (object instanceof Task)
			{
				final ResourcePool taskUsage = ((Task) object).resourceUsage();

				for (Resource resource : taskUsage.resources())
				{
					resourcePool.addResourceIfGreater(resource, taskUsage.getUsageOf(resource));
				}
			}
		}

		return resourcePool;
	}
}
