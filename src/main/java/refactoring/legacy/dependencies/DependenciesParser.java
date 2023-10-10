package refactoring.legacy.dependencies;

import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessType;
import bpmn.types.process.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

public class DependenciesParser
{
	private final File dependencies;
	private HashSet<Dependency> dependenciesSet;

	public DependenciesParser(final File dependencies)
	{
		this.dependencies = dependencies;
	}

	/**
	 * Dependencies are expected to be put in the following format (one per line): (A,B)
	 * This means that task "B" is semantically dependent of the execution of task "A",
	 * and thus can not execute before it.
	 *
	 * @return the set of dependencies built
	 */
	public HashSet<Dependency> parse() throws FileNotFoundException
	{
		final HashSet<Dependency> builtDependencies = new HashSet<>();
		final Scanner scanner = new Scanner(this.dependencies);

		while (scanner.hasNextLine())
		{
			final String line = scanner.nextLine();
			final Dependency dependency = this.parseLine(line);

			if (dependency != null)
			{
				builtDependencies.add(dependency);
			}
		}

		scanner.close();

		return this.dependenciesSet = builtDependencies;
	}

	public HashSet<Dependency> dependencies()
	{
		return this.dependenciesSet;
	}

	//Private methods

	public Dependency parseLine(final String line)
	{
		final int leftParenthesisIndex = line.indexOf('(');
		final int comaIndex = line.indexOf(',');
		final int rightParenthesisIndex = line.indexOf(')');

		if (leftParenthesisIndex == -1
			|| comaIndex == -1
			|| rightParenthesisIndex == -1)
		{
			System.out.println("Line |" + line + "| did not correspond the schema (A,B) and was thus ignored.");
			return null;
		}

		final String firstTask = line.substring(leftParenthesisIndex + 1, comaIndex).trim();
		final String secondTask = line.substring(comaIndex + 1, rightParenthesisIndex).trim();

		return new Dependency(
				new Node(new Task(firstTask, BpmnProcessType.TASK, -1)),
				new Node(new Task(secondTask, BpmnProcessType.TASK, -1))
		);
	}
}
