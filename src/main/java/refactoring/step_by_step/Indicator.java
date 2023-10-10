package refactoring.step_by_step;

public class Indicator
{
	private final double meanAETdiff;
	private final double stdAETdiff;
	private final double localAETdiff;
	private final double meanResUsageDiff;
	private final double localResUsageDiff;

	public Indicator(final double meanAETdiff,
					 final double stdAETdiff,
					 final double localAETdiff,
					 final double meanResUsageDiff,
					 final double localResUsageDiff)
	{
		this.meanAETdiff = meanAETdiff;
		this.stdAETdiff = stdAETdiff;
		this.localAETdiff = localAETdiff;
		this.meanResUsageDiff = meanResUsageDiff;
		this.localResUsageDiff = localResUsageDiff;
	}


	public double getMeanAETdiff()
	{
		return meanAETdiff;
	}

	public double getStdAETdiff()
	{
		return stdAETdiff;
	}

	public double getLocalAETdiff()
	{
		return localAETdiff;
	}

	public double getMeanResUsageDiff()
	{
		return meanResUsageDiff;
	}
	public double getLocalResUsageDiff()
	{
		return localResUsageDiff;
	}
}
