package simulator.bpmn_to_model_input;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;

public class DistributionUtils
{
	private DistributionUtils()
	{

	}

	public static AbstractRealDistribution valuesToDistribution(final String value,
																final double param1,
																final double param2)
	{
		if (value.equals(DistributionType.CONSTANT.label()))
		{
			return new ConstantRealDistribution(param1);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	public static AbstractRealDistribution valuesToDistribution(final String value,
																final String param1Str,
																final String param2Str)
	{
		final double param1 = Double.parseDouble(param1Str);
		final double param2 = Double.parseDouble(param2Str);

		if (value.equals(DistributionType.CONSTANT.label()))
		{
			return new ConstantRealDistribution(param1);
		}
		else
		{
			throw new UnsupportedOperationException();
		}
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
