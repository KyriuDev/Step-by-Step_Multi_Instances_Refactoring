package hash.graph;

import bpmn.graph.Graph;
import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessType;
import bpmn.types.process.Gateway;
import bpmn.types.process.Task;
import bpmn.types.process.events.Event;
import hash.HashConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

public class GraphHasher
{
	private final HashMap<String, HashGateway> speedCorrespondences;

	public GraphHasher()
	{
		this.speedCorrespondences = new HashMap<>();
	}

	public String hash(final Graph graph)
	{
		final HashPath mainPath = new HashPath();
		this.computePath(graph.initialNode(), mainPath, new HashSet<>());
		final StringBuilder builder = new StringBuilder();
		this.write(mainPath, builder);

		return builder.toString();
	}

	public void clear()
	{
		this.speedCorrespondences.clear();
	}

	//Private methods

	private void computePath(final Node node,
							 final HashPath path,
							 final HashSet<Node> visitedNodes)
	{
		if (node.bpmnObject() instanceof Event)
		{
			visitedNodes.add(node);

			if (node.bpmnObject().type() == BpmnProcessType.END_EVENT)
			{
				//End event
				path.addNode(HashEvent.generateEndEvent());
			}
			else
			{
				//Start event
				path.addNode(HashEvent.generateStartEvent());
				this.computePath(node.childNodes().iterator().next(), path, visitedNodes);
			}
		}
		else if (node.bpmnObject() instanceof Task)
		{
			visitedNodes.add(node);
			path.addNode(new HashTask(node.bpmnObject().name()));
			this.computePath(node.childNodes().iterator().next(), path, visitedNodes);
		}
		else if (node.bpmnObject() instanceof Gateway)
		{
			final Gateway gateway = (Gateway) node.bpmnObject();

			if (node.bpmnObject().type() == BpmnProcessType.EXCLUSIVE_GATEWAY)
			{
				//Exclusive gateway
				if (gateway.isSplitGateway())
				{
					//Split gateway
					visitedNodes.add(node);
					final HashGateway exclusiveGateway = HashGateway.generateExclusiveSplitGateway();
					exclusiveGateway.setPhysicalGatewayId(node.bpmnObject().id());
					path.addNode(exclusiveGateway);

					for (Node child : node.childNodes())
					{
						final HashPath childPath = new HashPath();
						exclusiveGateway.addRawPath(childPath);
						this.computePath(child, childPath, new HashSet<>(visitedNodes));
					}
				}
				else
				{
					//Merge gateway
					if (visitedNodes.contains(node))
					{
						//We have already crossed that node --> it's a loop start --> we add another gateway of type "loop start" and stop recursion
						final HashGateway loopStart = HashGateway.generateStartOfLoopExclusiveMergeGateway();
						path.addNode(loopStart);
						//throw new IllegalStateException();
					}
					else
					{
						visitedNodes.add(node);
						final HashGateway hashGateway = HashGateway.generateExclusiveMergeGateway();
						path.addNode(hashGateway);
						this.computePath(node.childNodes().iterator().next(), path, visitedNodes);
					}
				}
			}
			else
			{
				//Parallel gateway
				if (gateway.isSplitGateway())
				{
					//Split gateway
					visitedNodes.add(node);
					final HashGateway parallelGateway = HashGateway.generateParallelSplitGateway();
					parallelGateway.setPhysicalGatewayId(node.bpmnObject().id());
					path.addNode(parallelGateway);

					for (Node child : node.childNodes())
					{
						final HashPath childPath = new HashPath();
						parallelGateway.addRawPath(childPath);
						this.computePath(child, childPath, new HashSet<>(visitedNodes));
					}
				}
				else
				{
					//Merge gateway
					visitedNodes.add(node);
					path.addNode(HashGateway.generateParallelMergeGateway());
					this.computePath(node.childNodes().iterator().next(), path, visitedNodes);
				}
			}
		}
		else
		{
			//Sequence flow
			this.computePath(node.childNodes().iterator().next(), path, visitedNodes);
		}
	}

	private void write(final HashPath mainPath,
					   final StringBuilder builder)
	{
		for (AbstractHashNode node : mainPath.getImmutableNodes())
		{
			if (node.getType() == HashConstants.TASK)
			{
				//Task
				builder.append(((HashTask) node).getName());
			}
			else if (node.getType() == HashConstants.EVENT)
			{
				if (((HashEvent) node).isStartEvent())
				{
					//Start event
					builder.append(HashConstants.START_EVENT);
				}
				else
				{
					//End event
					builder.append(HashConstants.END_EVENT);
				}
			}
			else if (node.getType() == HashConstants.GATEWAY)
			{
				final HashGateway gateway = (HashGateway) node;

				if (gateway.isParallel())
				{
					//Parallel gateway
					if (gateway.isMerge())
					{
						//Merge gateway: impossible here
						throw new IllegalStateException("There should not be any parallel merge gateway is the main path!");
					}
					else
					{
						//Split gateway
						final String hash = this.computeHash(gateway);
						builder.append(hash);
					}
				}
				else
				{
					//Exclusive gateway
					if (gateway.isMerge())
					{
						//Merge gateway
						builder.append(HashConstants.EXCLUSIVE_MERGE);
					}
					else
					{
						//Split gateway
						final String hash = this.computeHash(gateway);
						builder.append(hash);
					}
				}
			}
			else
			{
				throw new IllegalStateException();
			}
		}
	}

	private String computeHash(final HashGateway gateway)
	{
		final StringBuilder builder = new StringBuilder();

		if (gateway.hashComputed())
		{
			builder.append(gateway.hash());
		}
		else
		{
			final HashGateway correspondingGateway = this.speedCorrespondences.get(gateway.getPhysicalGatewayId());

			if (correspondingGateway != null)
			{
				if (!correspondingGateway.hashComputed()) throw new IllegalStateException("Found a corresponding gateway but its hash has not been computed.");

				builder.append(correspondingGateway.hash());
				gateway.setHash(correspondingGateway.hash());
			}
			else
			{
				builder.append(gateway.isParallel() ? HashConstants.PARALLEL_SPLIT : HashConstants.EXCLUSIVE_SPLIT);

				final ArrayList<HashPath> orderedPaths = this.orderPaths(gateway.getRawPaths());
				gateway.setOrderedPaths(orderedPaths);

				final String writtenOrderedPaths = this.writeOrderedPaths(orderedPaths);
				builder.append(writtenOrderedPaths);

				gateway.setHash(builder.toString());
				this.speedCorrespondences.put(gateway.getPhysicalGatewayId(), gateway);
			}
		}

		return builder.toString();
	}

	private String writeOrderedPaths(final ArrayList<HashPath> orderedPaths)
	{
		final StringBuilder builder = new StringBuilder();
		String separator = "";

		for (HashPath path : orderedPaths)
		{
			builder.append(separator);
			separator = String.valueOf(HashConstants.BRANCH_SEPARATOR);

			for (AbstractHashNode node : path.getImmutableNodes())
			{
				if (node.getType() == HashConstants.TASK)
				{
					builder.append(((HashTask) node).getName());
				}
				else if (node.getType() == HashConstants.EVENT)
				{
					if (((HashEvent) node).isStartEvent())
					{
						//TODO is it possible?
						builder.append(HashConstants.START_EVENT);
					}
					else
					{
						builder.append(HashConstants.END_EVENT);
					}
				}
				else if (node.getType() == HashConstants.GATEWAY)
				{
					final HashGateway gateway = (HashGateway) node;

					if (gateway.isMerge())
					{
						builder.append(gateway.isParallel()
								? HashConstants.PARALLEL_MERGE
								: gateway.isStartOfLoop()
								? HashConstants.ALREADY_VISITED_MERGE
								: HashConstants.EXCLUSIVE_MERGE);
					}
					else
					{
						if (gateway.hashComputed())
						{
							builder.append(gateway.hash());
						}
						else
						{
							final String hash = this.computeHash(gateway);
							builder.append(hash);
						}
					}
				}
			}
		}

		return builder.toString();
	}

	private ArrayList<HashPath> orderPaths(final HashSet<HashPath> rawPaths)
	{
		final ArrayList<HashPath> orderedPaths = new ArrayList<>();
		final ArrayList<HashPath> tasksPaths = new ArrayList<>();
		final ArrayList<HashPath> parallelSplitsPaths = new ArrayList<>();
		final ArrayList<HashPath> exclusiveSplitsPaths = new ArrayList<>();
		final ArrayList<HashPath> endOfLoopExclusiveMergesPaths = new ArrayList<>();
		final ArrayList<HashPath> endEventsPaths = new ArrayList<>();

		for (HashPath path : rawPaths)
		{
			final AbstractHashNode firstNode = path.getModifiableNodeAt(0);

			if (firstNode.getType() == HashConstants.GATEWAY)
			{
				final HashGateway pathGateway = (HashGateway) firstNode;

				if (pathGateway.isParallel())
				{
					parallelSplitsPaths.add(path);
				}
				else
				{
					if (pathGateway.isMerge())
					{
						endOfLoopExclusiveMergesPaths.add(path);
					}
					else
					{
						exclusiveSplitsPaths.add(path);
					}
				}
			}
			else if (firstNode.getType() == HashConstants.TASK)
			{
				tasksPaths.add(path);
			}
			else if (firstNode.getType() == HashConstants.EVENT)
			{
				endEventsPaths.add(path);
			}
		}

		tasksPaths.sort(this.tasksComparator());
		parallelSplitsPaths.sort(this.gatewaysComparator());
		exclusiveSplitsPaths.sort(this.gatewaysComparator());
		endOfLoopExclusiveMergesPaths.sort(this.gatewaysComparator());

		orderedPaths.addAll(endOfLoopExclusiveMergesPaths);
		orderedPaths.addAll(exclusiveSplitsPaths);
		orderedPaths.addAll(parallelSplitsPaths);
		orderedPaths.addAll(tasksPaths);
		orderedPaths.addAll(endEventsPaths);

		return orderedPaths;
	}

	private Comparator<HashPath> tasksComparator()
	{
		return (o1, o2) ->
		{
			if (o1.isEmpty() && o2.isEmpty()) return 0;

			final String name1 = ((HashTask) o1.getModifiableNodeAt(0)).getName();
			final String name2 = ((HashTask) o2.getModifiableNodeAt(0)).getName();

			final int result = name1.compareTo(name2);

			if (result == 0)
			{
				//We need to go further
				o1.removeFirstNode();
				o2.removeFirstNode();

				final HashSet<HashPath> paths = new HashSet<>();
				paths.add(o1);
				paths.add(o2);
				final ArrayList<HashPath> orderedPaths = this.orderPaths(paths);

				if (orderedPaths.get(0) == o1)
				{
					return -1;
				}
				else
				{
					return 1;
				}
			}

			return result;
		};
	}

	private Comparator<HashPath> gatewaysComparator()
	{
		return (o1, o2) ->
		{
			if (o1.isEmpty() && o2.isEmpty()) return 0;

			final HashGateway gateway1 = (HashGateway) o1.getModifiableNodeAt(0);
			final HashGateway gateway2 = (HashGateway) o2.getModifiableNodeAt(0);

			if (!gateway1.hashComputed()) gateway1.setHash(this.computeHash(gateway1));
			if (!gateway2.hashComputed()) gateway2.setHash(this.computeHash(gateway2));

			return gateway1.hash().compareTo(gateway2.hash());
		};
	}
}
