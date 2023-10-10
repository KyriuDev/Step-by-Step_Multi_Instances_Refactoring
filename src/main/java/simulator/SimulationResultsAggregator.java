package simulator;

import simulator.model.SimulationResult;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SimulationResultsAggregator
{
	private SimulationResultsAggregator()
	{

	}

	public static SimulationResult aggregate(final Collection<SimulationResult> simulationResults)
	{
		final double nbResults = simulationResults.size();
		final SimulationResult firstResult = simulationResults.iterator().next();
		final int population = firstResult.getPopulation();
		Duration simulationTime = Duration.ZERO;
		double totalExecutionTime = 0d;
		double avgExecTime = 0d;
		double totalCost = 0d;
		double totalEmissions = 0d;
		final Map<String, Double> usagePercentage = new HashMap<>();
		final Map<String, Map<Double, Integer>> avlHistory = new HashMap<>(); //TODO A revoir
		final Map<String, Double> syncTimes = new HashMap<>();
		final Map<String, Map<Double, Double>> emissionsHistory = new HashMap<>(); //TODO A revoir
		final Map<String, Map<Double, Double>> costHistory = new HashMap<>(); //TODO A revoir

		for (SimulationResult simulationResult : simulationResults)
		{
			simulationTime = simulationTime.plus(simulationResult.getSimulationTime());
			totalExecutionTime += simulationResult.getTotalExecutionTime();
			avgExecTime += simulationResult.getAvgExecTime();
			totalCost += simulationResult.getTotalCost();
			totalEmissions += simulationResult.getTotalEmissions();

			for (String key : simulationResult.getUsagePercentage().keySet())
			{
				usagePercentage.put(key, usagePercentage.getOrDefault(key, 0d) + simulationResult.getUsagePercentage().get(key));
			}

			for (String key : simulationResult.getSyncTimes().keySet())
			{
				syncTimes.put(key, syncTimes.getOrDefault(key, 0d) + simulationResult.getSyncTimes().get(key));
			}
		}

		simulationTime = simulationTime.dividedBy(simulationResults.size());
		totalExecutionTime = totalExecutionTime / nbResults;
		avgExecTime = avgExecTime / nbResults;
		totalCost = totalCost / nbResults;
		totalEmissions = totalEmissions / nbResults;
		usagePercentage.replaceAll((k, v) -> usagePercentage.get(k) / nbResults);
		syncTimes.replaceAll((k,v) -> syncTimes.get(k) / nbResults);

		return new SimulationResult(
				population,
				simulationTime,
				totalExecutionTime,
				avgExecTime,
				totalCost,
				usagePercentage,
				avlHistory,
				syncTimes,
				totalEmissions,
				emissionsHistory,
				costHistory
		);
	}
}
