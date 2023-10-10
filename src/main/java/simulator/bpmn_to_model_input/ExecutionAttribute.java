package simulator.bpmn_to_model_input;

public class ExecutionAttribute
{
	public static final String AET = "avg_time";
	public static final String ET = "std_time";
	public static final String COST = "cost";
	public static final String NB_INSTANCES = "nb_instances";

	private final double aet;
	private final double et;
	private final double cost;
	private final int nbInstances;


	public ExecutionAttribute(final double aet,
							  final double et,
							  final double cost,
							  final int nbInstances)
	{
		this.aet = aet;
		this.et = et;
		this.cost = cost;
		this.nbInstances = nbInstances;
	}

	public double aet()
	{
		return this.aet;
	}

	public double et()
	{
		return this.et;
	}

	public double cost()
	{
		return this.cost;
	}

	public double nbInstances()
	{
		return this.nbInstances;
	}
}
