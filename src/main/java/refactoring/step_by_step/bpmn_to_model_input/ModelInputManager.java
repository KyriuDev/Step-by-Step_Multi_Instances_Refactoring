package refactoring.step_by_step.bpmn_to_model_input;

import bpmn.graph.Graph;

import java.util.ArrayList;

public class ModelInputManager
{
	private final AdjacencyManager adjacencyManager;
	private final EdgesManager edgesManager;
	private final ExecutionAttributesManager executionAttributesManager;
	private final GraphsManager graphsManager;
	private final NodesManager nodesManager;
	private final ArrayList<Graph> graphs;

	public ModelInputManager(final ArrayList<Graph> graphs)
	{
		this.adjacencyManager = new AdjacencyManager();
		this.edgesManager = new EdgesManager();
		this.executionAttributesManager = new ExecutionAttributesManager();
		this.graphsManager = new GraphsManager();
		this.nodesManager = new NodesManager();
		this.graphs = graphs;
	}

	public void write()
	{

	}

	//Private methods
}
