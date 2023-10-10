package refactoring.step_by_step;

import bpmn.graph.Graph;
import bpmn.graph.GraphToList;
import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.Task;
import refactoring.legacy.Cluster;
import refactoring.legacy.partial_order_to_bpmn.BPMNGenerator;

import java.util.HashSet;

public class BPMNIntegrityEnsurer
{
	private BPMNIntegrityEnsurer()
	{

	}

	public static synchronized void assertWeakIntegrity(final Graph graph,
										   final HashSet<Task> tasks,
										   final String function,
										   final Cluster originalCluster,
										   final Node electedTask)
	{
		final BPMNGenerator generator = new BPMNGenerator(originalCluster);
		generator.generate();

		final GraphToList graphToList = new GraphToList(graph);
		graphToList.convert();

		for (Task task : tasks)
		{
			boolean found = false;

			for (BpmnProcessObject object : graphToList.objects())
			{
				if (object instanceof Task)
				{
					if (task.name().equals(object.name()))
					{
						found = true;
						break;
					}
				}
			}

			if (!found)
			{
				throw new IllegalStateException("The following graph built with function \"" + function + "()\" does" +
						" not contain task |" + task.id() + "| (Elected task: " + electedTask.bpmnObject().id() + ")" +
						".\n\n" + graph.toString() + "\n\nOriginal graph:\n\n" + generator.optimalGraph().toString());
			}
		}
	}
}
