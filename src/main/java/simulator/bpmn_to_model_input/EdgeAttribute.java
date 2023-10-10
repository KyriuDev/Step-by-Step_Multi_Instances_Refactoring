package simulator.bpmn_to_model_input;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;

public class EdgeAttribute
{
	public static final String EDGE_TYPE = "type";
	public static final String DELAY_DISTRIBUTION = "delay_distr";
	public static final String DELAY_DISTRIBUTION_FIRST_PARAMETER = "delay_par1";
	public static final String DELAY_DISTRIBUTION_SECOND_PARAMETER = "delay_par2";
	public static final String EDGE_PROBABILITY = "proba";

	private final EdgeType type;
	private final AbstractRealDistribution distribution;
	private final double probability;

	public EdgeAttribute(final EdgeType edgeType,
						 final AbstractRealDistribution distribution,
						 final double probability)
	{
		this.type = edgeType;
		this.distribution = distribution;
		this.probability = probability;
	}

	public EdgeType edgeType()
	{
		return this.type;
	}

	public AbstractRealDistribution distribution()
	{
		return this.distribution;
	}

	public double probability()
	{
		return this.probability;
	}
}
