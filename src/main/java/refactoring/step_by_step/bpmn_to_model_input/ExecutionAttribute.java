package refactoring.step_by_step.bpmn_to_model_input;

public class ExecutionAttribute
{
	public static final String AET = "avg_time";
	public static final String ET = "std_time";
	public static final String COST = "cost";
	public static final String NB_INSTANCES = "nb_instances";

	private final int aet;
	private final int et;
	private final int cost;
	private final int nbInstances;


	public ExecutionAttribute(final int aet,
							  final int et,
							  final int cost,
							  final int nbInstances)
	{
		this.aet = aet;
		this.et = et;
		this.cost = cost;
		this.nbInstances = nbInstances;
	}
}
