package simulator.bpmn_to_model_input;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import resources.ResourcePool;

public class GraphAttribute
{
	public static final String IAT_DISTRIBUTION = "IAT_distr";
	public static final String IAT_DISTRIBUTION_FIRST_PARAM = "IAT_par1";
	public static final String IAT_DISTRIBUTION_SECOND_PARAM = "IAT_par2";

	private final AbstractRealDistribution iatDistribution;
	private final ResourcePool availableResources;
	private final int nbNodesTotal;

	public GraphAttribute(final AbstractRealDistribution distribution,
						  final ResourcePool availableResources,
						  final int nbNodesTotal)
	{
		this.iatDistribution = distribution;
		this.availableResources = availableResources;
		this.nbNodesTotal = nbNodesTotal;
	}

	public AbstractRealDistribution distribution()
	{
		return this.iatDistribution;
	}

	public ResourcePool resourceNames()
	{
		return this.availableResources;
	}

	public int nbNodesTotal()
	{
		return this.nbNodesTotal;
	}
}
