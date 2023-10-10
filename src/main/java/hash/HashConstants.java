package hash;

public class HashConstants
{
	//HASH TYPES
	public static final int TASK = 0;
	public static final int GATEWAY = 1;
	public static final int EVENT = 2;

	//HASH VALUES
	public static final char PARALLEL_SPLIT = '[';
	public static final char PARALLEL_MERGE = ']';
	public static final char EXCLUSIVE_SPLIT = '{';
	public static final char EXCLUSIVE_MERGE = '}';
	public static final char ALREADY_VISITED_MERGE = '§';
	public static final char BRANCH_SEPARATOR = '|';
	public static final char START_EVENT = '@';
	public static final char END_EVENT = '#';
	public static final char DEPENDENCY_GRAPHS_START = '(';
	public static final char DEPENDENCY_GRAPHS_END = ')';
	public static final char INDEPENDENT_NODES_START = '[';
	public static final char INDEPENDENT_NODES_END = ']';
	public static final char CLUSTER_START = '/';
	public static final char CLUSTER_END = '\\';

	public static final String PARALLEL_SPLIT_STR = String.valueOf(PARALLEL_SPLIT);
	public static final String PARALLEL_MERGE_STR = String.valueOf(PARALLEL_MERGE);
	public static final String EXCLUSIVE_SPLIT_STR = String.valueOf(EXCLUSIVE_SPLIT);
	public static final String EXCLUSIVE_MERGE_STR = String.valueOf(EXCLUSIVE_MERGE);
	public static final String ALREADY_VISITED_MERGE_STR = String.valueOf(ALREADY_VISITED_MERGE);
	public static final String BRANCH_SEPARATOR_STR = String.valueOf(BRANCH_SEPARATOR);
	public static final String START_EVENT_STR = String.valueOf(START_EVENT);
	public static final String END_EVENT_STR = String.valueOf(END_EVENT);

	/*
		PRIORITY ORDER (HIGHEST TO LOWEST):
			- LOOP EXCLUSIVE GATEWAYS > CLASSIC EXCLUSIVE GATEWAYS > PARALLEL GATEWAYS > TASKS > END EVENTS
			- HIGHEST NUMBER OF GATEWAYS OF SAME TYPE
			- LOOPING PATH > EXIT-LOOP PATH
	 */
}
