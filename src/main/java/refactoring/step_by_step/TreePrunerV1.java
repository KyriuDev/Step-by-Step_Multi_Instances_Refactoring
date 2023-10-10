package refactoring.step_by_step;

import bpmn.graph.Graph;
import bpmn.graph.GraphToList;
import bpmn.types.process.BpmnProcessObject;
import bpmn.writing.direct.DirectWriter;
import constants.PrintLevel;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import other.Pair;
import other.Utils;
import refactoring.legacy.Cluster;
import refactoring.legacy.exceptions.BadDependencyException;
import refactoring.legacy.partial_order_to_bpmn.AbstractGraphsGenerator;
import refactoring.legacy.partial_order_to_bpmn.BPMNGenerator;
import resources.GlobalResourceSet;
import resources.Resource;
import resources.ResourcePool;
import simulator.SimulationResultsAggregator;
import simulator.Simulator;
import simulator.model.SimulationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static main.Main.PRINT_LEVEL;

public class TreePrunerV1
{
	private static final int NB_LEAFS_TO_KEEP = 1;
	private static final double RES_WEIGHT = 1d;
	private static final double AET_WEIGHT = 0.6d; //AET gives information but is not that meaningful in this context
	private static final double LOCAL_WEIGHT = 0.5d; //Local computations give information but not as much as global ones

	private TreePrunerV1()
	{

	}

	public static void prune(final HashSet<TreeNode> currentNodes,
							 final File workingDirectory,
							 final ResourcePool availablePool,
							 final AbstractRealDistribution iat,
							 final int nbInstances) throws InterruptedException, BadDependencyException
	{
		TreePrunerV1.effectivePrune(currentNodes, workingDirectory, availablePool, iat, nbInstances, AET_WEIGHT, RES_WEIGHT, LOCAL_WEIGHT);
	}

	public static void prune(final HashSet<TreeNode> currentNodes,
							 final File workingDirectory,
							 final ResourcePool availablePool,
							 final AbstractRealDistribution iat,
							 final int nbInstances,
							 final double aetWeight,
							 final double resWeight,
							 final double localWeight) throws InterruptedException, BadDependencyException
	{
		TreePrunerV1.effectivePrune(currentNodes, workingDirectory, availablePool, iat, nbInstances, aetWeight, resWeight, localWeight);
	}

	//Private methods

	private static void effectivePrune(final HashSet<TreeNode> currentNodes,
									   final File workingDirectory,
									   final ResourcePool availablePool,
									   final AbstractRealDistribution iat,
									   final int nbInstances,
									   final double aetWeight,
									   final double resWeight,
									   final double localWeight) throws InterruptedException, BadDependencyException
	{
		final HashSet<TreeNode> bestLeafs = new HashSet<>();
		final HashMap<TreeNode, HashMap<TreeNode, Indicator>> globalIndicators = new HashMap<>();
		TreeNode currentWorstBestLeaf = null;
		int index = 0;

		final ExecutorService executorService = Executors.newWorkStealingPool();

		for (TreeNode child : currentNodes)
		{
			child.addAllSimulationResults(child.parentNodes().iterator().next());
			final String fileName = "tmp_diagram_" + ++index + ".bpmn";
			final Cluster copy = child.mainCluster().copy();

			if (copy.abstractGraphs().isEmpty())
			{
				Utils.resetIndependentNodes(copy);
				final AbstractGraphsGenerator abstractGraphsGenerator = new AbstractGraphsGenerator(copy);
				abstractGraphsGenerator.generateAbstractGraphs();
			}

			BPMNGenerator generator = new BPMNGenerator(copy);
			Graph currentGraph = generator.generate();
			generator = null;

			//Dumper.getInstance().graphicDump(currentGraph, fileName + "1");

			GraphToList graphToList = new GraphToList(currentGraph);
			HashSet<BpmnProcessObject> objects = graphToList.convert();
			graphToList = null;
			currentGraph = null;

			DirectWriter directWriter;
			try
			{
				directWriter = new DirectWriter(
						workingDirectory,
						new ArrayList<>(objects),
						fileName
				);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			directWriter.writeInitialBpmnFile();
			directWriter = null;

			final GlobalResourceSet globalSet = new GlobalResourceSet(availablePool);
			globalSet.computeGlobalResources();

			executorService.execute(() ->
			{
				final SimulationResult result = TreePrunerV1.simulate(workingDirectory, availablePool, iat, nbInstances, fileName, globalSet, objects);
				child.addSimulationResult(result);
			});
		}

		executorService.shutdown();
		executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		final HashMap<TreeNode, Indicator> localIndicators = new HashMap<>();
		final HashSet<TreeNode> nodes = new HashSet<>();

		for (TreeNode child : currentNodes)
		{
			/*
			 * Possible values : < 1 | 1 | > 1
			 * - Value < 1 means that the current leaf is a "good" choice
			 * - Value = 1 means that the current leaf is an "average" choice
			 * - Value > 1 means that the current leaf is a "bad" choice
			 */
			nodes.add(child);

			//Compute mean AET difference
			final Pair<Double, Double> aetMeans = TreePrunerV1.getOldAndNewAetMeans(child.parentNodes().iterator().next(), child.lastResult());
			final double meanAetDiff = aetMeans.first() - aetMeans.second();

			//Compute STD AET difference
			final double stdAetDiff = TreePrunerV1.getStdAetDiff(child.parentNodes().iterator().next(), child.lastResult(), aetMeans.first(), aetMeans.second());

			//Compute local AET difference
			final double localAetDiff = child.parentNodes().iterator().next().lastResult().getAvgExecTime() - child.lastResult().getAvgExecTime();

			//Compute mean res difference
			final Pair<Double, Double> resMeans = TreePrunerV1.getOldAndNewResMeans(child.parentNodes().iterator().next(), child.lastResult());
			final double meanResDiff = resMeans.second() - resMeans.first();

			//Compute local res difference
			final double localResDiff = TreePrunerV1.getLocalResDiff(child.parentNodes().iterator().next().lastResult(), child.lastResult(), child.movedTask().resourceUsage());

			if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
			{
				System.out.println("Mean AET diff: " + meanAetDiff);
				System.out.println("STD AET diff: " + stdAetDiff);
				System.out.println("Local AET diff: " + localAetDiff);
				System.out.println("Mean res diff: " + meanResDiff);
				System.out.println("Local res diff: " + localResDiff);
			}

			final Indicator indicator = new Indicator(
					meanAetDiff,
					stdAetDiff,
					localAetDiff,
					meanResDiff,
					localResDiff
			);
			localIndicators.put(child, indicator);
		}

		//BOF
		globalIndicators.put(currentNodes.iterator().next().parentNodes().iterator().next(), localIndicators);

		final Indicator maxIndicator = TreePrunerV1.computeMaxAbsoluteIndicator(globalIndicators);

		for (TreeNode parentNode : globalIndicators.keySet())
		{
			final HashMap<TreeNode, Indicator> childMap = globalIndicators.get(parentNode);

			for (TreeNode child : childMap.keySet())
			{
				final Indicator currentIndicator = childMap.get(child);
				final double normalizedMeanAetDiff = maxIndicator.getMeanAETdiff() != 0d ? currentIndicator.getMeanAETdiff() / maxIndicator.getMeanAETdiff() : 0d;
				final double normalizedStdAetDiff = maxIndicator.getStdAETdiff() != 0d ? currentIndicator.getStdAETdiff() / maxIndicator.getStdAETdiff() : 0d;
				final double normalizedLocalAetDiff = maxIndicator.getLocalAETdiff() != 0d ? currentIndicator.getLocalAETdiff() / maxIndicator.getLocalAETdiff() : 0d;
				final double normalizedMeanResDiff = maxIndicator.getMeanResUsageDiff() != 0d ? currentIndicator.getMeanResUsageDiff() / maxIndicator.getMeanResUsageDiff() : 0d;
				final double normalizedLocalResDiff = maxIndicator.getLocalResUsageDiff() != 0d ?currentIndicator.getLocalResUsageDiff() / maxIndicator.getLocalResUsageDiff() : 0d;
				final double oldMean = TreePrunerV1.getOldAndNewAetMeans(parentNode, parentNode.lastResult()).first();

				final double score = TreePrunerV1.computeScoreV3(
						normalizedMeanAetDiff,
						normalizedStdAetDiff,
						normalizedLocalAetDiff,
						normalizedMeanResDiff,
						normalizedLocalResDiff,
						oldMean,
						aetWeight,
						resWeight,
						localWeight
				);

				child.setScore(score);

				if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
				{
					System.out.println("Child score: " + score);
				}

				if (bestLeafs.size() < NB_LEAFS_TO_KEEP)
				{
					bestLeafs.add(child);
				}
				else
				{
					if (currentWorstBestLeaf == null)
					{
						currentWorstBestLeaf = TreePrunerV1.computeWorstNode(bestLeafs);
					}

					if (child.score() > currentWorstBestLeaf.score())
					{
						bestLeafs.remove(currentWorstBestLeaf);
						bestLeafs.add(child);
						currentWorstBestLeaf = TreePrunerV1.computeWorstNode(bestLeafs);
					}
				}
			}
		}

		currentNodes.clear();
		currentNodes.addAll(bestLeafs);
	}

	private static Indicator computeMaxAbsoluteIndicator(final HashMap<TreeNode, HashMap<TreeNode, Indicator>> indicators)
	{
		final Indicator firstIndicator = indicators.values().iterator().next().values().iterator().next();
		double maxMeanAetDiff = Math.abs(firstIndicator.getMeanAETdiff());
		double maxStdAetDiff = Math.abs(firstIndicator.getStdAETdiff());
		double maxLocalAetDiff = Math.abs(firstIndicator.getLocalAETdiff());
		double maxMeanResDiff = Math.abs(firstIndicator.getMeanResUsageDiff());
		double maxLocalResDiff = Math.abs(firstIndicator.getLocalResUsageDiff());

		for (HashMap<TreeNode, Indicator> subMap : indicators.values())
		{
			for (Indicator subIndicator : subMap.values())
			{
				if (Math.abs(subIndicator.getMeanAETdiff()) > maxMeanAetDiff)
				{
					maxMeanAetDiff = Math.abs(subIndicator.getMeanAETdiff());
				}
				if (Math.abs(subIndicator.getStdAETdiff()) > maxStdAetDiff)
				{
					maxStdAetDiff = Math.abs(subIndicator.getStdAETdiff());
				}
				if (Math.abs(subIndicator.getLocalAETdiff()) > maxLocalAetDiff)
				{
					maxLocalAetDiff = Math.abs(subIndicator.getLocalAETdiff());
				}
				if (Math.abs(subIndicator.getMeanResUsageDiff()) > maxMeanResDiff)
				{
					maxMeanResDiff = Math.abs(subIndicator.getMeanResUsageDiff());
				}
				if (Math.abs(subIndicator.getLocalResUsageDiff()) > maxLocalResDiff)
				{
					maxLocalResDiff = Math.abs(subIndicator.getLocalResUsageDiff());
				}
			}
		}

		return new Indicator(
				maxMeanAetDiff,
				maxStdAetDiff,
				maxLocalAetDiff,
				maxMeanResDiff,
				maxLocalResDiff
		);

	}

	private static TreeNode computeWorstNode(final HashSet<TreeNode> nodes)
	{
		final Iterator<TreeNode> iterator = nodes.iterator();
		TreeNode worstNode = iterator.next();

		while (iterator.hasNext())
		{
			final TreeNode currentNode = iterator.next();

			if (currentNode.score() < worstNode.score())
			{
				worstNode = currentNode;
			}
		}

		return worstNode;
	}

	private static Pair<Double, Double> getOldAndNewAetMeans(final TreeNode parent,
															 final SimulationResult childResults)
	{
		double oldAetMeansSum = 0d;

		for (SimulationResult result : parent.results())
		{
			oldAetMeansSum += result.getAvgExecTime();
		}

		final double oldAetMean = oldAetMeansSum / (double) parent.results().size();
		final double newAetMean = (oldAetMeansSum + childResults.getAvgExecTime()) / (double) (parent.results().size() + 1);

		return new Pair<>(oldAetMean, newAetMean);
	}

	private static double getStdAetDiff(final TreeNode parent,
										final SimulationResult childResults,
										final double oldAetMean,
										final double newAetMean)
	{
		double oldSum = 0d;
		double newSum = 0d;

		for (SimulationResult result : parent.results())
		{
			oldSum += Math.pow(result.getAvgExecTime() - oldAetMean, 2);
			newSum += Math.pow(result.getAvgExecTime() - newAetMean, 2);
		}

		newSum += Math.pow(childResults.getAvgExecTime() - newAetMean, 2);

		final double oldAetStd = Math.sqrt(oldSum / (double) parent.results().size());
		final double newAetStd = Math.sqrt(newSum / (double) (parent.results().size() + 1));

		return oldAetStd - newAetStd;
	}

	private static Pair<Double, Double> getOldAndNewResMeans(final TreeNode parent,
															 final SimulationResult childResults)
	{
		double oldResMeansSum = 0d;

		for (SimulationResult result : parent.results())
		{
			oldResMeansSum += TreePrunerV1.getResAvgUsage(result.getUsagePercentage());
		}

		final double oldResMean = oldResMeansSum / (double) parent.results().size();
		final double newResMean = (oldResMeansSum + TreePrunerV1.getResAvgUsage(childResults.getUsagePercentage())) / (double) (parent.results().size() + 1);

		return new Pair<>(oldResMean, newResMean);
	}

	private static double getStdResDiff(final TreeNode parent,
										final SimulationResult childResults,
										final double oldResMean,
										final double newResMean)
	{
		double oldSum = 0d;
		double newSum = 0d;

		for (SimulationResult result : parent.results())
		{
			oldSum += Math.pow(TreePrunerV1.getResAvgUsage(result.getUsagePercentage()) - oldResMean, 2);
			newSum += Math.pow(TreePrunerV1.getResAvgUsage(result.getUsagePercentage()) - newResMean, 2);
		}

		newSum += Math.pow(TreePrunerV1.getResAvgUsage(childResults.getUsagePercentage()) - newResMean, 2);

		final double oldAetStd = Math.sqrt(oldSum / (double) parent.results().size());
		final double newAetStd = Math.sqrt(newSum / (double) (parent.results().size() + 1));

		return oldAetStd - newAetStd;
	}

	private static double getLocalResDiff(final SimulationResult parent,
										  final SimulationResult child,
										  final ResourcePool resourcesUsed)
	{
		double localUsage = 0d;

		for (Resource resource : resourcesUsed.resources())
		{
			final double parentUsage = parent.getUsagePercentage().get(resource.name());
			final double childUsage = child.getUsagePercentage().get(resource.name());

			localUsage += (childUsage - parentUsage);
		}

		return localUsage / (double) resourcesUsed.resources().size();
	}

	private static double getResAvgUsage(final Map<String, Double> resUsage)
	{
		double usage = 0d;

		for (Double currentUsage : resUsage.values())
		{
			usage += currentUsage;
		}

		return usage / (double) resUsage.size();
	}

	private static SimulationResult simulate(final File workingDirectory,
											 final ResourcePool availablePool,
											 final AbstractRealDistribution iat,
											 final int nbInstances,
											 final String fileName,
											 final GlobalResourceSet globalSet,
											 final HashSet<BpmnProcessObject> objects)
	{
		final Simulator simulator = new Simulator(
				new File(Path.of(workingDirectory.getPath(), fileName).toString()),
				objects,
				globalSet,
				availablePool,
				iat,
				nbInstances
		);

		final SimulationResult result = SimulationResultsAggregator.aggregate(simulator.simulateMultipleInstances());

		new File(Path.of(workingDirectory.getPath(), fileName).toString()).delete();

		return result;
	}

	//SCORES

	private static double computeScoreV1(final double meanAetDiff,
										 final double stdAetDiff,
										 final double localAetDiff,
										 final double meanResDiff,
										 final double localResDiff,
										 final double aetWeight,
										 final double resWeight,
										 final double localWeight)
	{
		return aetWeight * (meanAetDiff + stdAetDiff + localWeight * localAetDiff)
				+ resWeight * (meanResDiff + localWeight * localResDiff);
	}

	private static double computeScoreV2(final double meanAetDiff,
										 final double stdAetDiff,
										 final double localAetDiff,
										 final double meanResDiff,
										 final double stdResDiff,
										 final double localResDiff,
										 final double meanAet,
										 final double aetWeight,
										 final double resWeight,
										 final double localWeight)
	{
		if (localAetDiff > 0.1d * meanAet)
		{
			return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight);
		}
		else
		{
			if (localAetDiff < 0)
			{
				//If AET did not vary much but resource usage increased we are more likely to be on a good track
				if (meanResDiff > 0)
				{
					return TreePrunerV1.computeScoreV1(getNegativeValueOf(meanAetDiff), getNegativeValueOf(stdAetDiff), getNegativeValueOf(localAetDiff), getNegativeValueOf(meanResDiff), getNegativeValueOf(localResDiff), aetWeight, resWeight, localWeight);
				}
				else
				{
					return TreePrunerV1.computeScoreV1(getNegativeValueOf(meanAetDiff), getNegativeValueOf(stdAetDiff), getNegativeValueOf(localAetDiff), meanResDiff, localResDiff, aetWeight, resWeight, localWeight);
				}
			}
			else
			{
				//If AET did not vary much but resource usage increased we are more likely to be on a good track
				if (meanResDiff > 0)
				{
					return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, getNegativeValueOf(meanResDiff), getNegativeValueOf(localResDiff), aetWeight, resWeight, localWeight);
				}
				else
				{
					return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight);
				}
			}
		}
	}

	private static double computeScoreV3(final double meanAetDiff,
										 final double stdAetDiff,
										 final double localAetDiff,
										 final double meanResDiff,
										 final double localResDiff,
										 final double meanAet,
										 final double aetWeight,
										 final double resWeight,
										 final double localWeight)
	{
		if (meanAetDiff > 0 && meanResDiff > 0)
		{
			return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight) * Rewards.GOOD;
		}
		else if (meanAetDiff > 0)
		{
			return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight) * Rewards.VERY_GOOD;
		}
		else if (meanResDiff > 0)
		{
			if (-localAetDiff < 0.1d * meanAet)
			{
				return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight) * Rewards.OK;
			}
			else
			{
				return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight) * Rewards.BAD;
			}
		}
		else
		{
			if (-localAetDiff > 0.1d * meanAet)
			{
				return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight) * Rewards.BAD;
			}
			else
			{
				return TreePrunerV1.computeScoreV1(meanAetDiff, stdAetDiff, localAetDiff, meanResDiff, localResDiff, aetWeight, resWeight, localWeight) * Rewards.VERY_BAD;
			}
		}
	}

	private static double getNegativeValueOf(final double d)
	{
		return -Math.abs(d);
	}
}
