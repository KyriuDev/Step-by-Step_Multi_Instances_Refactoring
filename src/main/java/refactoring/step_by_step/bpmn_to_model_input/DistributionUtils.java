package refactoring.step_by_step.bpmn_to_model_input;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;

public class DistributionUtils
{
	private DistributionUtils()
	{

	}

	public static String distributionName(final AbstractRealDistribution distribution)
	{
		if (distribution instanceof ConstantRealDistribution)
		{
			return DistributionType.CONSTANT.label();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public static double distributionFirstParam(final AbstractRealDistribution distribution)
	{
		if (distribution instanceof ConstantRealDistribution)
		{
			return distribution.getNumericalMean();
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public static double distributionSecondParam(final AbstractRealDistribution distribution)
	{
		if (distribution instanceof ConstantRealDistribution)
		{
			return 0d;
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}
}
