package refactoring.step_by_step;

import refactoring.legacy.dependencies.Dependency;
import refactoring.legacy.dependencies.EnhancedGraph;

import java.util.ArrayList;
import java.util.HashSet;

public class PartialOrdersVerifier
{
	private final HashSet<Dependency> originalDependencies;
	private final ArrayList<EnhancedGraph> bpmnProcesses;

	public PartialOrdersVerifier(final HashSet<Dependency> dependencies,
								 final ArrayList<EnhancedGraph> bpmnProcesses)
	{
		this.originalDependencies = dependencies;
		this.bpmnProcesses = bpmnProcesses;
	}

	public void removeIncorrectProcesses()
	{
		return;
	}
}
