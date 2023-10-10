package simulator.bpmn_to_model_input;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

public class ModelInputManager extends Manager
{
	private static final String SUBDIRECTORY_NAME = "model_inputs";
	private final AdjacencyManager adjacencyManager;
	private final EdgesManager edgesManager;
	private final ExecutionAttributesManager executionAttributesManager;
	private final GraphsManager graphsManager;
	private final NodesManager nodesManager;

	public ModelInputManager()
	{
		this.adjacencyManager = new AdjacencyManager();
		this.edgesManager = new EdgesManager();
		this.executionAttributesManager = new ExecutionAttributesManager();
		this.graphsManager = new GraphsManager();
		this.nodesManager = new NodesManager();
	}

	public void write(final File workingDirectory) throws FileNotFoundException
	{
		final File subdirectory = new File(Path.of(workingDirectory.getPath(), SUBDIRECTORY_NAME).toString());

		if (!this.createSubdirectory(workingDirectory))
		{
			throw new IllegalStateException("Could not create repository |" + subdirectory.getPath() + "|.");
		}

		this.adjacencyManager.write(subdirectory);
		this.edgesManager.write(subdirectory);
		this.executionAttributesManager.write(subdirectory);
		this.graphsManager.write(subdirectory);
		this.nodesManager.write(subdirectory);
	}

	public AdjacencyManager adjacencyManager()
	{
		return this.adjacencyManager;
	}

	public EdgesManager edgesManager()
	{
		return this.edgesManager;
	}

	public ExecutionAttributesManager executionAttributesManager()
	{
		return this.executionAttributesManager;
	}

	public GraphsManager graphsManager()
	{
		return this.graphsManager;
	}

	public NodesManager nodesManager()
	{
		return this.nodesManager;
	}

	//Private methods

	private boolean createSubdirectory(final File workingDirectory)
	{
		return new File(Path.of(workingDirectory.getPath(), SUBDIRECTORY_NAME).toString()).mkdir();
	}
}
