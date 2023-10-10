package other;

import bpmn.BpmnParser;
import bpmn.BpmnProcess;
import bpmn.graph.Graph;
import bpmn.graph.GraphToList;
import bpmn.writing.direct.DirectWriter;
import bpmn.writing.generation.GraphicalGenerationWriter;
import constants.CommandLineOption;
import main.CommandLineParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

public class Dumper
{
	private static Dumper dumper;
	private CommandLineParser commandLineParser;
	private BpmnParser bpmnParser;
	private Dumper()
	{

	}

	public static Dumper getInstance()
	{
		if (dumper == null)
		{
			dumper = new Dumper();
		}

		return dumper;
	}

	public void initializeDumper(final CommandLineParser commandLineParser,
								 final BpmnParser bpmnParser)
	{
		this.commandLineParser = commandLineParser;
		this.bpmnParser = bpmnParser;
	}

	public void graphicDump(final Graph graph,
							final String dumpValue)
	{
		if (this.commandLineParser == null)
		{
			throw new IllegalStateException("Dumper has not been initialized!");
		}

		final GraphToList graphToList = new GraphToList(graph);
		graphToList.convert();

		final BpmnProcess newBpmnProcess = new BpmnProcess(this.bpmnParser.bpmnProcess().id(), this.bpmnParser.bpmnProcess().isExecutable());
		newBpmnProcess.setObjects(graphToList.objectsList());

		final GraphicalGenerationWriter graphicalGenerationWriter = new GraphicalGenerationWriter(
				this.commandLineParser,
				this.bpmnParser.bpmnHeader(),
				newBpmnProcess,
				this.bpmnParser.bpmnCategories(),
				this.bpmnParser.documentation(),
				dumpValue
		);

		try
		{
			graphicalGenerationWriter.write();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public File simpleDump(final Graph graph,
						   final String dumpValue)
	{
		if (this.commandLineParser == null)
		{
			throw new IllegalStateException("Dumper has not been initialized!");
		}

		final GraphToList graphToList = new GraphToList(graph);
		graphToList.convert();

		final String workingDirectory = ((File) this.commandLineParser.get(CommandLineOption.WORKING_DIRECTORY)).getPath();

		final DirectWriter directWriter;

		try
		{
			directWriter = new DirectWriter(
					(File) this.commandLineParser.get(CommandLineOption.WORKING_DIRECTORY),
					graphToList.objectsList(),
					dumpValue
			);
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		directWriter.writeInitialBpmnFile();

		return new File(Path.of(workingDirectory, dumpValue).toString());
	}
}
