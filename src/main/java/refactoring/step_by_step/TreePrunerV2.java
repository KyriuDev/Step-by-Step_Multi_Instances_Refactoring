package refactoring.step_by_step;

import bpmn.graph.Graph;
import bpmn.graph.GraphToList;
import bpmn.writing.direct.DirectWriter;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import other.Utils;
import refactoring.legacy.Cluster;
import refactoring.legacy.exceptions.BadDependencyException;
import refactoring.legacy.partial_order_to_bpmn.AbstractGraphsGenerator;
import refactoring.legacy.partial_order_to_bpmn.BPMNGenerator;
import resources.GlobalResourceSet;
import resources.ResourcePool;
import simulator.SimulationResultsAggregator;
import simulator.Simulator;
import simulator.model.SimulationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TreePrunerV2
{
	private static final int MAX_DEPTH = 5;
	private static int currentDepth = 1;

	private TreePrunerV2()
	{

	}

	public static void prune(final HashSet<TreeNode> currentLeafs,
							 final File workingDirectory,
							 final ResourcePool availablePool,
							 final AbstractRealDistribution iat,
							 final int nbInstances) throws BadDependencyException
	{
		if (currentDepth == MAX_DEPTH)
		{
			TreePrunerV2.effectivePrune(currentLeafs, workingDirectory, availablePool, iat, nbInstances);
			currentDepth = 1;
		}
		else
		{
			currentDepth++;
		}
	}

	//Private methods

	private static void effectivePrune(final HashSet<TreeNode> currentLeafs,
									   final File workingDirectory,
									   final ResourcePool availablePool,
									   final AbstractRealDistribution iat,
									   final int nbInstances) throws BadDependencyException
	{
		final ExecutorService executorService = Executors.newWorkStealingPool();
		int index = 0;

		for (TreeNode leaf : currentLeafs)
		{
			final String fileName = "tmp_diagram_" + ++index + ".bpmn";
			final Cluster copy = leaf.mainCluster().copy();

			if (copy.abstractGraphs().isEmpty())
			{
				Utils.resetIndependentNodes(copy);
				final AbstractGraphsGenerator abstractGraphsGenerator = new AbstractGraphsGenerator(copy);
				abstractGraphsGenerator.generateAbstractGraphs();
			}

			BPMNGenerator generator = new BPMNGenerator(copy);
			final Graph currentGraph = generator.generate();

			final GraphToList graphToList = new GraphToList(currentGraph);
			graphToList.convert();

			final DirectWriter directWriter;
			try
			{
				directWriter = new DirectWriter(
						workingDirectory,
						graphToList.objectsList(),
						fileName
				);
			}
			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}
			directWriter.writeInitialBpmnFile();

			final GlobalResourceSet globalSet = new GlobalResourceSet(availablePool);
			globalSet.computeGlobalResources();

			executorService.execute(() ->
			{
				final SimulationResult result = TreePrunerV2.simulate(workingDirectory, availablePool, iat, nbInstances, fileName, globalSet, graphToList);
				leaf.addSimulationResult(result);
			});
		}

		TreeNode bestLeaf = currentLeafs.iterator().next();

		for (TreeNode leaf : currentLeafs)
		{
			if (leaf.results().get(0).getAvgExecTime() < bestLeaf.results().get(0).getAvgExecTime())
			{
				bestLeaf = leaf;
			}
		}

		currentLeafs.clear();
		currentLeafs.add(bestLeaf);
	}

	private static SimulationResult simulate(final File workingDirectory,
											 final ResourcePool availablePool,
											 final AbstractRealDistribution iat,
											 final int nbInstances,
											 final String fileName,
											 final GlobalResourceSet globalSet,
											 final GraphToList graphToList)
	{
		final Simulator simulator = new Simulator(
				new File(Path.of(workingDirectory.getPath(), fileName).toString()),
				graphToList.objectsList(),
				globalSet,
				availablePool,
				iat,
				nbInstances
		);

		final SimulationResult result = SimulationResultsAggregator.aggregate(simulator.simulateMultipleInstances());

		new File(Path.of(workingDirectory.getPath(), fileName).toString()).delete();

		return result;
	}
}
