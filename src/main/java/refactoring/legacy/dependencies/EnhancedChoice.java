package refactoring.legacy.dependencies;

import bpmn.graph.Node;
import refactoring.legacy.Cluster;

import java.util.Collection;
import java.util.HashMap;

public class EnhancedChoice extends EnhancedNode
{
	private final HashMap<Node, Cluster> choicePaths;

	public EnhancedChoice(final Node node)
	{
		super(node);
		this.choicePaths = new HashMap<>();
	}

	public EnhancedChoice(final Node node,
						  final boolean isIndependent)
	{
		super(node, isIndependent);
		this.choicePaths = new HashMap<>();
	}

	@Override
	public String stringify(final int depth)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("	".repeat(depth))
				.append("Choice |")
				.append(this.node.bpmnObject().id())
				.append("| has paths:\n");

		int i = 1;

		for (Cluster cluster : this.choicePaths.values())
		{
			builder.append("	".repeat(depth + 1))
					.append("- Path n°")
					.append(i++)
					.append(":\n");

			builder.append(cluster.stringify(depth + 2))
					.append("\n");
		}

		return builder.toString();
	}

	@Override
	public EnhancedType type()
	{
		return EnhancedType.CHOICE;
	}

	@Override
	public EnhancedChoice copy()
	{
		final EnhancedChoice enhancedChoice = new EnhancedChoice(this.node, this.isIndependent);

		for (Node flow : this.choicePaths.keySet())
		{
			final Cluster cluster = this.choicePaths.get(flow);
			enhancedChoice.choicePaths.put(flow.weakCopy(), cluster.copy());
		}

		return enhancedChoice;
	}

	@Override
	public boolean hashComputed()
	{
		return this.hash != null && !this.hash.equals(node.bpmnObject().name());
	}

	//Public methods

	/**
	 * The cluster to which we add an element corresponds to the current path of the choice.
	 * This path is identified by its initial sequence flow, that is used as key in this
	 * method.
	 *
	 * @param key the sequence flow starting the current path
	 * @param element the enhanced node belonging to the current path
	 */
	public void addElementToClusterWithKey(final Node key,
										   final EnhancedNode element)
	{
		final Cluster cluster = this.choicePaths.computeIfAbsent(key, c -> new Cluster());
		cluster.addElement(element);
	}

	/**
	 * The cluster to which we add an element corresponds to the current path of the choice.
	 * This path is identified by its initial sequence flow, that is used as key in this
	 * method.
	 *
	 * @param key the sequence flow starting the current path
	 * @param dependency the dependency belonging to the current path
	 */
	public void addDependencyToClusterWithKey(final Node key,
											  final Dependency dependency)
	{
		final Cluster cluster = this.choicePaths.computeIfAbsent(key, c -> new Cluster());
		cluster.addDependency(dependency);
	}

	public HashMap<Node, Cluster> choicePaths()
	{
		return this.choicePaths;
	}

	public Cluster getClusterFromKey(final Node key)
	{
		return this.choicePaths.computeIfAbsent(key, c -> new Cluster());
	}

	public void putClusterWithKey(final Node key,
								  final Cluster cluster)
	{
		this.choicePaths.put(key, cluster);
	}

	public Collection<Cluster> clusters()
	{
		return this.choicePaths.values();
	}
}
