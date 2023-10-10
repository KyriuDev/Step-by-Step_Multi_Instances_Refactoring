package refactoring.legacy.partial_order_to_bpmn;

import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessType;
import bpmn.types.process.Task;
import constants.PrintLevel;
import refactoring.legacy.dependencies.*;
import refactoring.legacy.Cluster;
import refactoring.legacy.exceptions.BadDependencyException;

import java.util.HashSet;

import static main.Main.PRINT_LEVEL;

public class AbstractGraphsGenerator
{
	private final Cluster mainCluster;

	public AbstractGraphsGenerator(final Cluster mainCluster)
	{
		this.mainCluster = mainCluster;
	}

	public void generateAbstractGraphs() throws BadDependencyException
	{
		this.generateAbstractGraph(this.mainCluster);
	}

	//Private methods

	private void generateAbstractGraph(final Cluster cluster) throws BadDependencyException
	{
		for (DependencyGraph dependencyGraph : cluster.dependencyGraphs())
		{
			final PartialOrder2AbstractGraph partialOrder2AbstractGraph = new PartialOrder2AbstractGraph(dependencyGraph);
			final AbstractGraph abstractGraph = partialOrder2AbstractGraph.generateAbstractGraph();
			cluster.addAbstractGraph(abstractGraph);

			if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
			{
				System.out.println("Generated abstract graph: \n\n" + abstractGraph.stringify(0));
			}
		}

		final HashSet<EnhancedNode> independentNodes = new HashSet<>();

		for (EnhancedNode node : cluster.elements())
		{
			if (node.isIndependent())
			{
				independentNodes.add(node);
			}

			if (node.type() == EnhancedType.CHOICE)
			{
				final EnhancedChoice choice = (EnhancedChoice) node;

				for (Cluster subCluster : choice.clusters())
				{
					this.generateAbstractGraph(subCluster);
				}
			}
			else if (node.type() == EnhancedType.LOOP)
			{
				final EnhancedLoop loop = (EnhancedLoop) node;

				this.generateAbstractGraph(loop.entryToExitCluster());
				this.generateAbstractGraph(loop.exitToEntryCluster());
			}
		}

		if (!independentNodes.isEmpty())
		{
			final AbstractNode node = new AbstractNode();
			final AbstractGraph graph = new AbstractGraph(node);

			for (EnhancedNode enhancedNode : independentNodes)
			{
				node.addNode(enhancedNode.node());
			}

			if (!cluster.abstractGraphs().isEmpty())
			{
				//Put all existing abstract graphs as subgraphs of a new abstract graph containing a single node
				for (AbstractGraph abstractGraph : cluster.abstractGraphs())
				{
					node.addSubgraph(abstractGraph);
				}

				cluster.abstractGraphs().clear();
			}

			cluster.addAbstractGraph(graph);
		}

		if (cluster.abstractGraphs().size() > 1)
		{
			//Merge abstract graphs
			final AbstractNode mainNode = new AbstractNode();
			final AbstractGraph mainGraph = new AbstractGraph(mainNode);

			for (AbstractGraph graph : cluster.abstractGraphs())
			{
				mainNode.addSubgraph(graph);
			}

			cluster.abstractGraphs().clear();
			cluster.addAbstractGraph(mainGraph);
		}
	}
}
