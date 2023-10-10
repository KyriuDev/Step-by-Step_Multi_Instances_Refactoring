package simulator;

import bpmn.BpmnParser;
import bpmn.graph.ListToGraph;
import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.SequenceFlow;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import resources.GlobalResourceSet;
import resources.Resource;
import resources.ResourcePool;
import simulator.model.*;
import simulator.transform.BpmnWorkflowTransformer;
import simulator.transform.BpmnXmlParser;
import simulator.transform.intermediate.BpmnWorkflow;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class Simulator
{
	private static final Logger logger = LoggerFactory.getLogger(Simulator.class);

	public static final int NB_REP = 100;
	private final File bpmnFile;
	private final Collection<BpmnProcessObject> objects;
	private final GlobalResourceSet globalResourceSet;
	private final ResourcePool availablePool;
	private final AbstractRealDistribution iat;
	private final int nbInstances;

	public Simulator(final File bpmnFile,
					 final Collection<BpmnProcessObject> objects,
					 final GlobalResourceSet globalResourceSet,
					 final ResourcePool resourcePool,
					 final AbstractRealDistribution iat,
					 final int nbInstances)
	{
		this.bpmnFile = bpmnFile;
		this.objects = objects;
		this.globalResourceSet = globalResourceSet;
		this.availablePool = resourcePool;
		this.iat = iat;
		this.nbInstances = nbInstances;
	}

	public ArrayList<SimulationResult> simulateMultipleInstances()
	{
		try
		{
			return this.simulate(this.bpmnFile, NB_REP, this.nbInstances);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public ArrayList<SimulationResult> simulateSingleInstance()
	{
		try
		{
			return this.simulate(this.bpmnFile, NB_REP, 1);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	//Private methods

	private ArrayList<SimulationResult> simulate(final File bpmnFile,
												 final int nbRep,
												 final int nbInstances) throws IOException
	{
		final ArrayList<SimulationResult> simulationResults = new ArrayList<>();

		/*final BpmnParser myParser;
		try
		{
			myParser = new BpmnParser(bpmnFile, false, false, false);
		}
		catch (ParserConfigurationException | IOException | SAXException e)
		{
			throw new RuntimeException(e);
		}
		myParser.parse();
		final ListToGraph listToGraph = new ListToGraph(myParser.bpmnProcess().objects());
		System.out.println("Graph:\n\n" + listToGraph.convert().toString());*/

		for (int i = 0; i < nbRep; i++)
		{
			final InputStream inputStream = new FileInputStream(bpmnFile);
			final BpmnXmlParser parser = new BpmnXmlParser(inputStream);
			parser.handle();
			final BpmnWorkflow bpmnWorkflow = parser.getOutputWorklow();
			final BpmnWorkflowTransformer transformer = new BpmnWorkflowTransformer(bpmnWorkflow);
			transformer.transform();
			final Workflow workflow = transformer.getWorkflow();
			inputStream.close();

			//Associate resource instances
			for (BpmnProcessObject object : this.objects)
			{
				if (object instanceof bpmn.types.process.Task)
				{
					final bpmn.types.process.Task task = (bpmn.types.process.Task) object;
					final Map<String, Integer> currentMap = new HashMap<>();

					for (Resource resource : task.resourceUsage().resources())
					{
						currentMap.put(resource.name(), task.resourceUsage().getUsageOf(resource));
					}

					((Task) workflow.getNodes().get(task.id())).setRequiredResources(currentMap);
				}
			}

			//Set durations (task/flow)
			for (BpmnProcessObject object : this.objects)
			{
				if (object instanceof bpmn.types.process.Task)
				{
					final bpmn.types.process.Task task = (bpmn.types.process.Task) object;
					((Task) workflow.getNodes().get(task.id())).setDuration(new ConstantRealDistribution(task.duration()));
				}
				else if (object instanceof bpmn.types.process.SequenceFlow)
				{
					final SequenceFlow sequenceFlow = (SequenceFlow) object;
					(workflow.getFlows().get(sequenceFlow.id())).setDelay(sequenceFlow.duration());
				}
			}

			final List<Node> startEvents = new ArrayList<>(workflow.getStartEvents());
			final Simulation sequenceSim = new Simulation((StartEvent) startEvents.get(0));

			// Generate resources
			final Map<String, Integer> availableResources = new HashMap<>();

			for (Resource resource : this.globalResourceSet.resourcesSet())
			{
				sequenceSim.addResource(ResourceBank.createResource(resource.name(), resource.cost()));
				//System.out.println("Resource cost: " + resource.cost());
				availableResources.put(resource.name(), this.availablePool.getUsageOf(resource));
			}

			sequenceSim.runSimulation(availableResources, nbInstances, this.iat);

			if (!sequenceSim.getTokens().isEmpty())
			{
				throw new IllegalStateException("Tokens are not empty at the end of the simulation!");
			}

			simulationResults.add(sequenceSim.getSimulationResult());

			//System.out.println("Finished simulation n°" + i);
		}

		//System.out.println("Finished simulation of process \"refactored_process_" + index + ".bpmn\"");

		return simulationResults;
	}
}
