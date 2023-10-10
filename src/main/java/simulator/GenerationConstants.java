package simulator;

public class GenerationConstants
{
	private GenerationConstants()
	{

	}

	public static final int NUMBER_OF_RESOURCES = 5; //Total number of different resources
	public static final int MAX_NUMBER_OF_REPLICAS = 3; //Maximum number of replicas of each resource used by a task
	public static final int MAX_DIFFERENT_RESOURCES = 3; //Maximum number of different resources used by a task
	public static final int MAX_TASK_DURATION = 30; //Maximum duration of a task
	public static final int MAX_FLOW_DURATION = MAX_TASK_DURATION / 2; //Maximum duration of a flow
	public static final int NB_CHILDS_UPPER_BOUND = 10; //Maximum number of child flows for each gateway
	public static final int MIN_NUMBER_OF_TASKS = 3; //Minimum number of tasks in the generated process
	public static final int MAX_NUMBER_OF_TASKS = 20; //Maximum number of tasks in the generated process
	public static final int MAX_NUMBER_OF_INSTANCES = 99; //Maximum number of instances of a process to simulate
	public static final int MAX_RESOURCE_COST = 20; //Maximum cost for a given resource
	public static final double INTRICATION_FACTOR = 2d; //Division factor for the probability of having an intrication of gateways of same type
	public static final double EXCLUSIVE_GATEWAY_PROBABILITY = 0.1d; //Original probability of generating an exclusive gateway
	public static final double PARALLEL_GATEWAY_PROBABILITY = 0.3d; //Original probability of generating a parallel gateway
	public static final double TASK_PROBABILITY = 0.6d; //Original probability of generating a task
	public static final double BALANCED_PROBABILITY = 0.8d; //Probability of generating a balanced gateway
	public static final double UNBALANCED_PROBABILITY = 1d - BALANCED_PROBABILITY; //Probability of generating an unbalanced gateway
	public static final double LOOP_PROBABILITY = 0.1d; //Probability of generating a loop from an exclusive gateway
	public static final double CHOICE_PROBABILITY = 1d - LOOP_PROBABILITY; //Probability of generating a choice from an exclusive gateway
	public static final double TWO_CHILD_PROBABILITY = 0.5d; //Allows a bit more flexibility for 2 childs
	public static final double TIMED_FLOW_PROBABILITY = 0.05d; //Probability of having a flow with a duration != 0
}
