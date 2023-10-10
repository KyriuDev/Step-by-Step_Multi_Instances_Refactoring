package refactoring.step_by_step;

import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.Task;
import other.Pair;
import other.Utils;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.EnhancedChoice;
import refactoring.legacy.dependencies.EnhancedLoop;
import refactoring.legacy.dependencies.EnhancedNode;
import refactoring.legacy.dependencies.EnhancedType;
import resources.Resource;
import resources.ResourcePool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;


public class TaskElector
{
	private static final int NB_TASKS_TO_TAKE = -1; //0 or less means "take all tasks".
	private static final TaskElectionHeuristic HEURISTIC = TaskElectionHeuristic.LONGEST_LAST;
	private static final double DURATION_WEIGHT = 0.5D;
	private static final double COST_WEIGHT = 0.5D;
	private final Collection<Task> tasksAlreadyChosen;
	private final Collection<BpmnProcessObject> allTasks;
	private final Collection<BpmnProcessObject> tasks;
	private final ResourcePool availableResources;
	private final Cluster mainCluster;
	private Pair<Node, Cluster> electedTask;

	public TaskElector(final Collection<BpmnProcessObject> objects,
					   final ResourcePool availableResources,
					   final Cluster cluster)
	{
		this.availableResources = availableResources;
		this.allTasks = new ArrayList<>(objects);
		this.tasksAlreadyChosen = new ArrayList<>();
		this.tasks = this.keepTasksOnly(objects, null);
		this.mainCluster = cluster;
	}

	public TaskElector(final Collection<BpmnProcessObject> objects,
					   final Collection<Task> tasksAlreadyChosen,
					   final ResourcePool availableResources,
					   final Cluster cluster)
	{
		this.allTasks = new ArrayList<>(objects);
		this.tasksAlreadyChosen = tasksAlreadyChosen;
		this.tasks = this.keepTasksOnly(objects, tasksAlreadyChosen);
		this.availableResources = availableResources;
		this.mainCluster = cluster;

		if (NB_TASKS_TO_TAKE > 0
				&& tasksAlreadyChosen.size() >= NB_TASKS_TO_TAKE)
		{
			//We have reached the number of tasks to move, so we stop
			this.tasks.clear();
		}
	}

	public void clear()
	{
		//this.tasksAlreadyChosen.clear();
		this.allTasks.clear();
		this.tasks.clear();
	}

	public Pair<Node, Cluster> electTask()
	{
		if (this.tasks.isEmpty())
		{
			return null;
		}

		switch (HEURISTIC)
		{
			case RANDOM:
			{
				Node task = this.randomHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				this.tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.randomHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			case LONGEST_FIRST:
			{
				Node task = this.longestFirstHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.longestFirstHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			case LONGEST_LAST:
			{
				Node task = this.longestLastHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.longestLastHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			case HIGHEST_COST_FIRST:
			{
				Node task = this.highCostFirstHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.highCostFirstHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			case HIGHEST_COST_LAST:
			{
				Node task = this.highCostLastHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.highCostLastHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			case HYBRID_BIGGEST_FIRST:
			{
				Node task = this.hybridBiggestFirstHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.hybridBiggestFirstHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			case HYBRID_BIGGEST_LAST:
			{
				Node task = this.hybridBiggestLastHeuristic();
				Cluster cluster = this.getTaskCluster(task);
				tasksAlreadyChosen.add((Task) task.bpmnObject());

				while (this.invalidateTask(cluster))
				{
					tasks.clear();
					tasks.addAll(this.keepTasksOnly(this.allTasks, this.tasksAlreadyChosen));

					if (tasks.isEmpty()) return null;

					task = this.hybridBiggestLastHeuristic();
					this.tasksAlreadyChosen.add((Task) task.bpmnObject());
					cluster = this.getTaskCluster(task);
				}

				return this.electedTask = new Pair<>(task, cluster);
			}
			default: throw new IllegalStateException("Heuristic |" + HEURISTIC + "| was not recognized.");
		}
	}

	public Pair<Node, Cluster> electedTask()
	{
		return this.electedTask;
	}

	//Private methods

	private Node randomHeuristic()
	{
		final Random random = new Random();
		final int randomInt = random.nextInt();
		final int taskIndex = Math.abs(randomInt) % this.tasks.size();
		final Iterator<BpmnProcessObject> tasksIterator = this.tasks.iterator();
		int i = 0;

		while (tasksIterator.hasNext())
		{
			final BpmnProcessObject task = tasksIterator.next();

			if (i++ == taskIndex)
			{
				return new Node(task);
			}
		}

		throw new IllegalStateException();
	}

	private Node longestFirstHeuristic()
	{
		int longestDuration = -1;
		BpmnProcessObject longestTask = null;

		for (BpmnProcessObject task : this.tasks)
		{
			final int taskDuration = ((Task) task).duration();

			if (taskDuration > longestDuration)
			{
				longestDuration = taskDuration;
				longestTask = task;
			}
		}

		if (longestTask == null) throw new IllegalStateException();

		return new Node(longestTask);
	}

	private Node longestLastHeuristic()
	{
		int shortestDuration = Integer.MAX_VALUE;
		BpmnProcessObject shortestTask = null;

		for (BpmnProcessObject task : this.tasks)
		{
			final int taskDuration = ((Task) task).duration();

			if (taskDuration < shortestDuration)
			{
				shortestDuration = taskDuration;
				shortestTask = task;
			}
		}

		if (shortestTask == null) throw new IllegalStateException();

		return new Node(shortestTask);
	}

	private Node highCostFirstHeuristic()
	{
		double highestCost = -1d;
		BpmnProcessObject highestCostTask = null;

		for (BpmnProcessObject task : this.tasks)
		{
			final double cost = this.computeTaskCost(task);

			if (cost > highestCost)
			{
				highestCost = cost;
				highestCostTask = task;
			}
		}

		if (highestCostTask == null) throw new IllegalStateException();

		return new Node(highestCostTask);
	}

	private Node highCostLastHeuristic()
	{
		double shortestCost = Double.MAX_VALUE;
		BpmnProcessObject shortestCostTask = null;

		for (BpmnProcessObject task : this.tasks)
		{
			final double cost = this.computeTaskCost(task);

			if (cost < shortestCost)
			{
				shortestCost = cost;
				shortestCostTask = task;
			}
		}

		if (shortestCostTask == null) throw new IllegalStateException();

		return new Node(shortestCostTask);
	}

	private Node hybridBiggestFirstHeuristic()
	{
		double biggestValue = -1;
		BpmnProcessObject biggestValueTask = null;

		for (BpmnProcessObject task : this.tasks)
		{
			final double cost = this.computeTaskCost(task);
			final int duration = ((Task) task).duration();
			final double computedValue = COST_WEIGHT * cost + DURATION_WEIGHT * (double) duration;

			if (computedValue > biggestValue)
			{
				biggestValue = computedValue;
				biggestValueTask = task;
			}
		}

		if (biggestValueTask == null) throw new IllegalStateException();

		return new Node(biggestValueTask);
	}

	private Node hybridBiggestLastHeuristic()
	{
		double smallestValue = Double.MAX_VALUE;
		BpmnProcessObject smallestValueTask = null;

		for (BpmnProcessObject task : this.tasks)
		{
			final double cost = this.computeTaskCost(task);
			final int duration = ((Task) task).duration();
			final double computedValue = COST_WEIGHT * cost + DURATION_WEIGHT * (double) duration;

			if (computedValue < smallestValue)
			{
				smallestValue = computedValue;
				smallestValueTask = task;
			}
		}

		if (smallestValueTask == null) throw new IllegalStateException();

		return new Node(smallestValueTask);
	}

	private double computeTaskCost(final BpmnProcessObject object)
	{
		double cost = 0;
		final Task task = (Task) object;

		for (Resource resource : task.resourceUsage().resources())
		{
			final int usage = task.resourceUsage().getUsageOf(resource);
			cost += ((double) usage / (double) this.availableResources.getUsageOf(resource));
		}

		return cost;
	}

	private ArrayList<BpmnProcessObject> keepTasksOnly(final Collection<BpmnProcessObject> objects,
													   final Collection<Task> tasksAlreadyChosen)
	{
		final ArrayList<BpmnProcessObject> tasks = new ArrayList<>();

		for (BpmnProcessObject object : objects)
		{
			if (object instanceof Task)
			{
				tasks.add(object);
			}
		}

		if (tasksAlreadyChosen != null)
		{
			tasks.removeAll(tasksAlreadyChosen);
		}

		return tasks;
	}

	private Cluster getTaskCluster(final Node task)
	{
		final Cluster taskCluster = Utils.getTaskCluster(this.mainCluster, task);

		if (taskCluster == null) throw new IllegalStateException("Task |" + task.bpmnObject().id() + "| was not found in any cluster.");

		return taskCluster;
	}

	private boolean invalidateTask(final Cluster taskCluster)
	{
		return taskCluster.elements().size() <= 1;
	}
}
