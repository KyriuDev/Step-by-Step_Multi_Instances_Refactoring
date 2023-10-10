package simulator.bpmn_to_model_input;

import other.Utils;
import resources.Resource;
import resources.ResourcePool;
import simulator.GlobalInfoEnum;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GlobalInfosParser
{
	private final File globalInfos;

	public GlobalInfosParser(final File globalInfos)
	{
		this.globalInfos = globalInfos;
	}

	public Map<GlobalInfoEnum, Object> parse() throws FileNotFoundException
	{
		final Map<GlobalInfoEnum, Object> globalInfoObjectMap = new HashMap<>();
		final Scanner scanner = new Scanner(this.globalInfos);

		while (scanner.hasNextLine())
		{
			final String line = scanner.nextLine();

			if (line.contains(GlobalInfo.IAT.label()))
			{
				this.parseIAT(globalInfoObjectMap, line);
			}
			else if (line.contains(GlobalInfo.NB_INSTANCES.label()))
			{
				this.parseNbInstances(globalInfoObjectMap, line);
			}
			else if (line.contains(GlobalInfo.AVAILABLE_RESOURCES.label()))
			{
				this.parseAvailableResources(globalInfoObjectMap, line);
			}
		}

		return globalInfoObjectMap;
	}

	//Private methods

	private void parseIAT(final Map<GlobalInfoEnum, Object> map,
						  final String line)
	{
		final String type = line.substring(line.indexOf(':') + 1, line.indexOf(',')).trim();
		final String line2 = line.substring(line.indexOf(',') + 1);
		final String param1 = line2.substring(0, line2.indexOf(',')).trim();
		final String line3 = line2.substring(line2.indexOf(',') + 1);
		final String param2 = line3.trim();

		map.put(GlobalInfoEnum.IAT, DistributionUtils.valuesToDistribution(type, param1, param2));
	}

	private void parseNbInstances(final Map<GlobalInfoEnum, Object> map,
								  final String line)
	{
		final int nbInstances;

		try
		{
			nbInstances = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not parse number of instances properly (" + line + ").");
		}

		map.put(GlobalInfoEnum.NB_INSTANCES, nbInstances);
	}

	private void parseAvailableResources(final Map<GlobalInfoEnum, Object> map,
										 final String line)
	{
		final ResourcePool availablePool = new ResourcePool();
		String resources = line.substring(line.indexOf(':') + 1).trim();
		int comaIndex = resources.indexOf(',');

		if (comaIndex == -1)
		{
			this.parseResource(resources, availablePool);
		}
		else
		{
			String resource = resources.substring(0, comaIndex);
			resources = resources.substring(comaIndex + 1);
			this.parseResource(resource, availablePool);
			comaIndex = resources.indexOf(',');

			while (comaIndex != -1)
			{
				resource = resources.substring(0, comaIndex);
				resources = resources.substring(comaIndex + 1);
				this.parseResource(resource, availablePool);
				comaIndex = resources.indexOf(',');
			}

			this.parseResource(resources, availablePool);
		}

		map.put(GlobalInfoEnum.GLOBAL_RESOURCE_POOL, availablePool);
	}

	private void parseResource(final String resourceStr,
							   final ResourcePool resourcePool)
	{
		final char[] chars = resourceStr.toCharArray();
		final StringBuilder nbReplicasBuilder = new StringBuilder();
		final StringBuilder nameBuilder = new StringBuilder();
		final StringBuilder costBuilder = new StringBuilder();
		boolean isName = false;
		boolean isCost = false;

		for (char c : chars)
		{
			if (isName)
			{
				if (c == '(')
				{
					isCost = true;
					isName = false;
				}
				else
				{
					nameBuilder.append(c);
				}
			}
			else if (isCost)
			{
				if (c == ')')
				{
					break;
				}

				costBuilder.append(c);
			}
			else
			{
				if (Utils.isAnInt(c))
				{
					nbReplicasBuilder.append(c);
				}
				else
				{
					if (!nbReplicasBuilder.toString().isEmpty())
					{
						isName = true;
						nameBuilder.append(c);
					}
				}
			}
		}

		final int cost;
		final int nbReplicas;
		final String name = nameBuilder.toString().trim();

		try
		{
			cost = Integer.parseInt(costBuilder.toString().trim());
			nbReplicas = Integer.parseInt(nbReplicasBuilder.toString().trim());
		}
		catch (NumberFormatException e)
		{
			throw new RuntimeException("Cost (" + costBuilder.toString().trim() + ") or number of replicas (" + nbReplicasBuilder.toString().trim() + ") is not a number.");
		}

		final Resource resource = new Resource(name);
		resource.setCost(cost);
		//System.out.println("Cost: " + resource.cost());

		resourcePool.addResource(resource, nbReplicas);
	}
}
