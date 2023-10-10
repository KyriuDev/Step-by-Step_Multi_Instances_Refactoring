package refactoring.step_by_step.bpmn_to_model_input;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import resources.ResourcePool;

public class NodeAttribute
{
	public static final String NODE_TYPE = "node";
	public static final String DURATION_DISTRIBUTION = "dur_distr";
	public static final String DURATION_DISTRIBUTION_FIRST_PARAM = "dur_par1";
	public static final String DURATION_DISTRIBUTION_SECOND_PARAM = "dur_par2";

	private final NodeType type;
	private final AbstractRealDistribution distribution;
	private final ResourcePool requiredResources;
	private final String label;

	public NodeAttribute(final NodeType type,
						 final AbstractRealDistribution distribution,
						 final ResourcePool requiredResources,
						 final String label)
	{
		this.type = type;
		this.distribution = distribution;
		this.requiredResources = requiredResources;
		this.label = label;
	}

	public NodeType type()
	{
		return this.type;
	}

	public AbstractRealDistribution distribution()
	{
		return this.distribution;
	}

	public ResourcePool resources()
	{
		return this.requiredResources;
	}

	public String label()
	{
		return this.label;
	}
}
