package simulator.bpmn_to_model_input;

import simulator.model.SimulationResult;

import java.util.ArrayList;

public class SimulationResultsAnalyzer
{
	private SimulationResultsAnalyzer()
	{

	}

	public static ExecutionAttribute analyze(final ArrayList<SimulationResult> multiInstanceResults,
									  		 final ArrayList<SimulationResult> singleInstanceResults)
	{
		final double executionTime = SimulationResultsAnalyzer.computeAET(singleInstanceResults);
		final double averageExecutionTime = SimulationResultsAnalyzer.computeAET(multiInstanceResults);
		final double totalCost = SimulationResultsAnalyzer.computeCost(multiInstanceResults);
		final int nbInstances = multiInstanceResults.get(0).getPopulation();

		return new ExecutionAttribute(averageExecutionTime, executionTime, totalCost, nbInstances);
	}

	//Private methods

	private static double computeAET(final ArrayList<SimulationResult> simulationResults)
	{
		double aet = 0d;

		for (SimulationResult simulationResult : simulationResults)
		{
			aet += simulationResult.getAvgExecTime();
		}

		return aet / (double) simulationResults.size();
	}

	private static double computeCost(ArrayList<SimulationResult> simulationResults)
	{
		double cost = 0d;

		for (SimulationResult simulationResult : simulationResults)
		{
			cost += simulationResult.getTotalCost();
		}

		return cost / (double) simulationResults.size();
	}
}
