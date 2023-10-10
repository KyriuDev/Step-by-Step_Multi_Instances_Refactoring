package other;

import bpmn.graph.Node;
import constants.PrintLevel;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.*;
import refactoring.legacy.partial_order_to_bpmn.AbstractGraph;
import refactoring.legacy.partial_order_to_bpmn.AbstractNode;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import static main.Main.PRINT_LEVEL;

public class Utils
{
    private static final long MICROSECONDS_THRESHOLD = 1000;
    private static final long MILLISECONDS_THRESHOLD = 1000000;
    private static final long SECONDS_THRESHOLD = 1000000000;
    private static final long MINUTES_THRESHOLD = 60000000000L;
    private static final long HOURS_THRESHOLD = 3600000000000L;
    private static final long DAYS_THRESHOLD = 86400000000000L;

    private Utils()
    {

    }

    public static String nanoSecToReadable(final long nanoseconds)
    {
        final DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);

        if (nanoseconds > DAYS_THRESHOLD)
        {
            return df.format((double) nanoseconds / (double) DAYS_THRESHOLD) + " days";
        }
        else if (nanoseconds > HOURS_THRESHOLD)
        {
            return df.format((double) nanoseconds / (double) HOURS_THRESHOLD) + "h";
        }
        else if (nanoseconds > MINUTES_THRESHOLD)
        {
            return df.format((double) nanoseconds / (double) MINUTES_THRESHOLD) + "m";
        }
        else if (nanoseconds > SECONDS_THRESHOLD)
        {
            //More than 1sec
            return df.format((double) nanoseconds / (double) SECONDS_THRESHOLD) + "s";
        }
        else if (nanoseconds > MILLISECONDS_THRESHOLD)
        {
            //More than 1ms
            return df.format((double) nanoseconds / (double) MILLISECONDS_THRESHOLD) + "ms";
        }
        else if (nanoseconds > MICROSECONDS_THRESHOLD)
        {
            //More than 1µs
            return df.format((double) nanoseconds / (double) MICROSECONDS_THRESHOLD) + "µs";
        }
        else
        {
            //Value in nanoseconds
            return df.format((double) nanoseconds) + "ns";
        }
    }

    public static boolean userAgreesMovingTask(final String userAnswer)
    {
        final String trimmedAnswer = userAnswer.toLowerCase().trim();

		return trimmedAnswer.isEmpty()
				|| trimmedAnswer.equals("y")
				|| trimmedAnswer.equals("yes");
	}

    public static boolean userAgreesWithAbstractGraph(final String userAnswer)
    {
        final String trimmedAnswer = userAnswer.toLowerCase().trim();

        return trimmedAnswer.isEmpty()
                || trimmedAnswer.equals("y")
                || trimmedAnswer.equals("yes");
    }

    public static String protectString(String s)
    {
        return s.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;");
    }

    public static String quoteString(String s)
    {
        return "\"" + s + "\"";
    }

    public static String quoteString(int i)
    {
        return "\"" + i + "\"";
    }

    public static boolean isAnInt(final String s)
    {
        try
        {
            Integer.parseInt(s);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static boolean isAnInt(final char c)
    {
        try
        {
            Integer.parseInt(String.valueOf(c));
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static String generateRandomIdentifier()
    {
        return Utils.generateRandomIdentifier(30);
    }

    public static String generateRandomIdentifier(final int length)
    {
        final StringBuilder builder = new StringBuilder();
        final Random random = new Random();

        for (int i = 0; i < length; i++)
        {
            final char c;

            //CAPS
            if (random.nextBoolean())
            {
                c = (char) (random.nextInt(25) + 65 + 1); //Exclusive upper bound
            }
            //NON CAPS
            else
            {
                c = (char) (random.nextInt(25) + 97 + 1); //Exclusive upper bound
            }

            builder.append(c);
        }

        return builder.toString();
    }

    public static double sumDoubles(final Collection<Double> doubles)
    {
        double sum = 0d;

        for (Double d : doubles)
        {
            sum += d;
        }

        return sum;
    }

    public static int max(final Collection<Integer> integers)
    {
        int max = Integer.MIN_VALUE;

        if (integers.isEmpty())
        {
            return max;
        }

        for (Integer integer : integers)
        {
            if (integer > max)
            {
                max = integer;
            }
        }

        return max;
    }

    public static String printPaths(final Collection<ArrayList<Node>> paths)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("The following paths were found:\n");

        for (ArrayList<Node> path : paths)
        {
            builder.append("     - ");

            for (Node node : path)
            {
                builder.append(node.bpmnObject().id())
                        .append(" - ");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    /**
     * In this method, we want to compute all the combinations of elements of a set.
     * For example, getCombinationsOf([1,2,3]) returns [[1], [2], [3], [1,2], [1,3],
     * [2,3], [1,2,3]].
     *
     * @param elements the elements to combine
     * @return the list of all possible combinations of the elements
     * @param <T> any type
     */
    public static <T> Collection<Collection<T>> getCombinationsOf(Collection<T> elements)
    {
        final Collection<Collection<T>> combinations = new ArrayList<>();
        final ArrayList<T> sortedElements = new ArrayList<>(elements);

        Utils.getCombinationsOf(combinations, new ArrayList<>(), sortedElements);

        return combinations;
    }

    /**
     * In this method, we want all the combinations of the elements of the list respecting
     * the order in which they are in the list, meaning that if we have elements [1,2,3],
     * the combination [1,3] is not possible because it does not respect the order for 2.
     *
     * @param elements the elements to combine
     * @return the list of all ordered combinations
     * @param <T> any element
     */
    public static <T> List<List<T>> getOrderedCombinationsOf(List<T> elements)
    {
        final List<List<T>> combinations = new ArrayList<>();

        for (int i = 0; i < elements.size(); i++)
        {
            for (int j = i; j < elements.size(); j++)
            {
                final List<T> combination = new ArrayList<>();

                for (int index = i; index <= j; index++)
                {
                    combination.add(elements.get(index));
                }

                combinations.add(combination);
            }
        }

        return combinations;
    }

    /**
     * In this method, we want to compute the cartesian product of the elements passed as argument.
     * For example, collection [[1,2], [3,4]] will have the following output: [[1,3], [1,4], [2,3], [2,4]].
     *
     * @param elements the elements to combine
     * @return the collection corresponding to the cartesian product of the original elements
     * @param <T> any element
     */
    public static <T> List<List<T>> getCartesianProductOf(List<List<T>> elements)
    {
        if (elements.size() < 2) throw new IllegalArgumentException("Cannot generate the product of one set only.");

        return getCartesianProductOf(0, elements);
    }

    //Private methods

    private static <T> List<List<T>> getCartesianProductOf(int index,
                                                           List<List<T>> elements)
    {
        List<List<T>> ret = new ArrayList<>();

        if (index == elements.size())
        {
            ret.add(new ArrayList<>());
        }
        else
        {
            for (T element : elements.get(index))
            {
                for (List<T> set : getCartesianProductOf(index + 1, elements))
                {
                    set.add(element);
                    ret.add(set);
                }
            }
        }

        return ret;
    }

    private static <T> void getCombinationsOf(Collection<Collection<T>> allCombinations,
                                             Collection<T> currentCombination,
                                             List<T> remainingElements)
    {
        if (remainingElements.isEmpty())
        {
            return;
        }

        for (int i = 0; i < remainingElements.size(); i++)
        {
            final List<T> newRemainingElements = new ArrayList<>(remainingElements);

            int toRemove = 0;

            //Avoid duplicates
            while (toRemove < i)
            {
                newRemainingElements.remove(0);
                toRemove++;
            }

            final List<T> currentCombinationCopy = new ArrayList<>(currentCombination);
            currentCombinationCopy.add(newRemainingElements.remove(0));
            allCombinations.add(currentCombinationCopy);

            Utils.getCombinationsOf(allCombinations, currentCombinationCopy, newRemainingElements);
        }
    }

    public boolean listContainsAbstractNode(final Collection<AbstractNode> list,
                                            final AbstractNode node)
    {
        for (AbstractNode abstractNode : list)
        {
            if (abstractNode.id().equals(node.id()))
            {
                return true;
            }
        }

        return false;
    }

    public static void unprocessClusters(final Cluster currentCluster)
    {
        currentCluster.unprocess();

        for (EnhancedNode enhancedNode : currentCluster.elements())
        {
            if (enhancedNode.type() == EnhancedType.CHOICE)
            {
                final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;

                for (Cluster subCluster : enhancedChoice.clusters())
                {
                    Utils.unprocessClusters(subCluster);
                }
            }
            else if (enhancedNode.type() == EnhancedType.LOOP)
            {
                final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;
                Utils.unprocessClusters(enhancedLoop.entryToExitCluster());
                Utils.unprocessClusters(enhancedLoop.exitToEntryCluster());
            }
        }
    }

    public static String getGlobalInfoFileName(final String identifier)
    {
        return "global_infos_" + identifier + ".inf";
    }

    public static Cluster getTaskCluster(final Cluster cluster,
                                         final Node task)
    {
        if (cluster.elements().contains(new EnhancedNode(task)))
        {
            return cluster;
        }

        for (EnhancedNode enhancedNode : cluster.elements())
        {
            if (enhancedNode.type() == EnhancedType.CHOICE)
            {
                final EnhancedChoice enhancedChoice = (EnhancedChoice) enhancedNode;

                for (Cluster choiceCluster : enhancedChoice.clusters())
                {
                    final Cluster taskCluster = Utils.getTaskCluster(choiceCluster, task);

                    if (taskCluster != null)
                    {
                        return taskCluster;
                    }
                }
            }
            else if (enhancedNode.type() == EnhancedType.LOOP)
            {
                final EnhancedLoop enhancedLoop = (EnhancedLoop) enhancedNode;

                final Cluster taskCluster1 = Utils.getTaskCluster(enhancedLoop.entryToExitCluster(), task);

                if (taskCluster1 != null) return taskCluster1;

                final Cluster taskCluster2 = Utils.getTaskCluster(enhancedLoop.exitToEntryCluster(), task);

                if (taskCluster2 != null) return taskCluster2;
            }
        }

        return null;
    }

    public static void resetIndependentNodes(final Cluster cluster)
    {
        final HashSet<Node> dependentNodes = new HashSet<>();

        for (DependencyGraph dependencyGraph : cluster.dependencyGraphs())
        {
            dependentNodes.addAll(dependencyGraph.toSet());
        }

        for (EnhancedNode enhancedNode : cluster.elements())
        {
            if (dependentNodes.contains(enhancedNode.node()))
            {
                enhancedNode.setDependent();
            }
            else
            {
                enhancedNode.setIndependent();
            }

            if (enhancedNode.type() == EnhancedType.CHOICE)
            {
                for (Cluster subCluster : ((EnhancedChoice) enhancedNode).clusters())
                {
                    Utils.resetIndependentNodes(subCluster);
                }
            }
            else if (enhancedNode.type() == EnhancedType.LOOP)
            {
                for (Cluster subCluster : ((EnhancedLoop) enhancedNode).clusters())
                {
                    Utils.resetIndependentNodes(subCluster);
                }
            }
        }
    }

    /**
     * This method splits a set of dependencies into (potentially multiple) connected sets
     * of dependencies (i.e., sets from which a dependency graph can be generated)
     * @param rawDependencies the merged set of dependencies
     * @return a list of sets of connected dependencies
     */
    public static ArrayList<HashSet<Dependency>> splitDependencies(final HashSet<Dependency> rawDependencies)
    {
        //Verification
        if (rawDependencies.isEmpty()) return new ArrayList<>();

        //Initialisation
        final ArrayList<HashSet<Dependency>> finalDependencySets = new ArrayList<>();
        final HashSet<Dependency> initialSet = new HashSet<>();
        finalDependencySets.add(initialSet);
        final Iterator<Dependency> initiatingIterator = rawDependencies.iterator();
        initialSet.add(initiatingIterator.next());
        initiatingIterator.remove();

        //Splitting
        int previousNumberOfDependencies = rawDependencies.size();

        while (!rawDependencies.isEmpty())
        {
            for (Iterator<Dependency> iterator = rawDependencies.iterator(); iterator.hasNext(); )
            {
                final Dependency currentDependency = iterator.next();
                boolean shouldBreak = false;

                for (HashSet<Dependency> currentFinalSet : finalDependencySets)
                {
                    for (Dependency wellPlacedDependency : currentFinalSet)
                    {
                        if (currentDependency.firstNode().equals(wellPlacedDependency.firstNode())
                                || currentDependency.secondNode().equals(wellPlacedDependency.firstNode())
                                || currentDependency.firstNode().equals(wellPlacedDependency.secondNode())
                                || currentDependency.secondNode().equals(wellPlacedDependency.secondNode()))
                        {
                            currentFinalSet.add(currentDependency);
                            iterator.remove();
                            shouldBreak = true;
                            break;
                        }
                    }

                    if (shouldBreak)
                    {
                        break;
                    }
                }
            }

            if (previousNumberOfDependencies == rawDependencies.size())
            {
                //We did not remove any non-classified dependency during this iteration --> they are all independent of the currently existing sets
                final HashSet<Dependency> newSet = new HashSet<>();
                finalDependencySets.add(newSet);
                final Iterator<Dependency> nextIterator = rawDependencies.iterator();
                newSet.add(nextIterator.next());
                nextIterator.remove();
            }

            previousNumberOfDependencies = rawDependencies.size();
        }

        return finalDependencySets;
    }

    /**
     * This method generates a dependency graph from a set of dependencies.
     * For the method to execute properly (i.e., no infinite looping), each dependency must
     * be connected to (at least) another dependency.
     * For example, the set {(A,B), (C,D)} is not valid because dependencies (A,B) and (C,D)
     * are disjoint.
     * Oppositely, the set {(A,B), (B,C), (C,D)} is a valid set.
     *
     * @param dependencies the dependencies in couple format
     * @return the dependency graph corresponding to the given dependencies
     */
    public static DependencyGraph buildDependencyGraph(final HashSet<Dependency> dependencies)
    {
        if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
        {
            for (Dependency dependency : dependencies)
            {
                System.out.println("(" + dependency.firstNode().bpmnObject().id() + "," + dependency.secondNode().bpmnObject().id() + ")");
            }
        }

        final DependencyGraph dependencyGraph = new DependencyGraph();

        if (dependencies.isEmpty())
        {
            return null;
        }

		/*if (dependencies.size() == 1)
		{
			final Dependency dependency = dependencies.iterator().next();

			if (dependency.firstNode().equals(DUMMY_NODE)
				|| dependency.secondNode().equals(DUMMY_NODE))
			{
				//We have and ad-hoc dependency
				dependencyGraph.addInitialNode(new Node(dependency.firstNode().equals(DUMMY_NODE) ? dependency.secondNode().bpmnObject() : dependency.firstNode().bpmnObject()));
				return dependencyGraph;
			}
		}*/

        HashSet<Dependency> oldDependencies = new HashSet<>();

        while (!dependencies.isEmpty())
        {
            //System.out.println("oops");

            if (oldDependencies.equals(dependencies))
            {
                throw new IllegalStateException("Infinite looping detected and avoided.");
            }

            oldDependencies.clear();
            oldDependencies.addAll(dependencies);

            for (Iterator<Dependency> iterator = dependencies.iterator(); iterator.hasNext(); )
            {
                final Dependency dependency = iterator.next();
                final Node firstNode = new Node(dependency.firstNode().bpmnObject());
                final Node secondNode = new Node(dependency.secondNode().bpmnObject());

                if (dependencyGraph.hasNode(firstNode)
                        && dependencyGraph.hasNode(secondNode))
                {
                    //Both nodes are already in the graph: connect them together and remove the second from the initial nodes
                    final Node firstNodeInGraph = dependencyGraph.getNodeFromID(firstNode.bpmnObject().id());
                    final Node secondNodeInGraph = dependencyGraph.getNodeFromID(secondNode.bpmnObject().id());
                    firstNodeInGraph.addChild(secondNodeInGraph);
                    secondNodeInGraph.addParent(firstNodeInGraph);

                    if (secondNodeInGraph.isInLoop())
                    {
                        throw new RuntimeException("Dependency (" + firstNode.bpmnObject().id() + "," +
                                secondNode.bpmnObject().id() + ") generates a loop in the dependency graph!");
                    }
                    else
                    {
                        dependencyGraph.removeInitialNode(secondNodeInGraph);
                    }

                    iterator.remove();
                }
                else if (dependencyGraph.hasNode(firstNode))
                {
                    //First node is already in graph: connect the second one to it
                    final Node firstNodeInGraph = dependencyGraph.getNodeFromID(firstNode.bpmnObject().id());
                    firstNodeInGraph.addChild(secondNode);
                    secondNode.addParent(firstNodeInGraph);

                    iterator.remove();
                }
                else if (dependencyGraph.hasNode(secondNode))
                {
                    //Second node is already in graph: connect the first node to it, remove it from the initial nodes, and mark the first node as initial node
                    final Node secondNodeInGraph = dependencyGraph.getNodeFromID(secondNode.bpmnObject().id());
                    secondNodeInGraph.addParent(firstNode);
                    firstNode.addChild(secondNodeInGraph);
                    dependencyGraph.removeInitialNode(secondNodeInGraph);
                    dependencyGraph.addInitialNode(firstNode);

                    iterator.remove();
                }
                else
                {
                    if (dependencyGraph.isEmpty())
                    {
                        dependencyGraph.addInitialNode(firstNode);
                        firstNode.addChild(secondNode);
                        secondNode.addParent(firstNode);

                        iterator.remove();
                    }
                }
            }
        }

        if (dependencyGraph.isEmpty())
        {
            throw new IllegalStateException();
        }

        return dependencyGraph;
    }

    /**
     * This method traverses the abstract graph corresponding to each cluster and regenerates
     * one or several dependency graphs from them.
     *
     * @param cluster from which the abstract graph are considered
     */
    public static void recomputeDependencies(final Cluster mainCluster,
                                             final Cluster cluster)
    {
        if (!cluster.abstractGraphs().isEmpty())
        {
            final AbstractGraph mainGraph = cluster.abstractGraphs().get(0);
            final HashSet<Dependency> rawDependencies = new HashSet<>();
            Utils.connectGraphNodes(mainGraph, rawDependencies);
            mainCluster.addGlobalDependencies(rawDependencies);
            final ArrayList<HashSet<Dependency>> splitDependencies = Utils.splitDependencies(rawDependencies);

            cluster.dependencyGraphs().clear();

            for (HashSet<Dependency> dependencies : splitDependencies)
            {
                final DependencyGraph graph = Utils.buildDependencyGraph(dependencies);
                cluster.dependencyGraphs().add(graph);
            }
        }

        for (EnhancedNode node : cluster.elements())
        {
            if (node.type() == EnhancedType.CHOICE)
            {
                for (Cluster subCluster : ((EnhancedChoice) node).clusters())
                {
                    Utils.recomputeDependencies(mainCluster, subCluster);
                }
            }
            else if (node.type() == EnhancedType.LOOP)
            {
                for (Cluster subCluster : ((EnhancedLoop) node).clusters())
                {
                    Utils.recomputeDependencies(mainCluster, subCluster);
                }
            }
        }

        cluster.abstractGraphs().clear();
    }

    //Private methods

    private static void connectGraphNodes(final AbstractGraph graph,
                                          final HashSet<Dependency> dependencies)
    {
        final ArrayList<AbstractNode> nodes = graph.extractNodes();
        AbstractNode currentNode = nodes.get(0);
        final HashSet<Node> currentTerminalNodes = new HashSet<>(currentNode.listNodes());

        for (AbstractGraph subgraph : currentNode.subGraphs())
        {
            Utils.getTerminalsAndConnectSubParts(subgraph, currentTerminalNodes, dependencies);
        }

        for (int i = 1; i < nodes.size(); i++)
        {
            final AbstractNode nextNode = nodes.get(i);
            final HashSet<Node> nextInitialNodes = new HashSet<>(nextNode.listNodes());

            for (AbstractGraph subgraph : nextNode.subGraphs())
            {
                Utils.getInitials(subgraph, nextInitialNodes);
            }

            for (Node currentTerminal : currentTerminalNodes)
            {
                for (Node nextInitial : nextInitialNodes)
                {
                    dependencies.add(new Dependency(currentTerminal, nextInitial));
                }
            }

            currentNode = nextNode;
            currentTerminalNodes.clear();
            currentTerminalNodes.addAll(currentNode.listNodes());

            for (AbstractGraph subgraph : currentNode.subGraphs())
            {
                Utils.getTerminalsAndConnectSubParts(subgraph, currentTerminalNodes, dependencies);
            }
        }
    }

    private static void getTerminalsAndConnectSubParts(final AbstractGraph graph,
                                                       final HashSet<Node> terminalNodes,
                                                       final HashSet<Dependency> dependencies)
    {
        final ArrayList<AbstractNode> nodes = graph.extractNodes();
        final AbstractNode lastNode = nodes.get(nodes.size() - 1);
        terminalNodes.addAll(lastNode.listNodes());

        for (AbstractGraph subGraph : lastNode.subGraphs())
        {
            Utils.getTerminalsAndConnectSubParts(subGraph, terminalNodes, dependencies);
        }

        AbstractNode currentNode = nodes.get(0);
        final HashSet<Node> currentTerminalNodes = new HashSet<>(currentNode.listNodes());

        for (AbstractGraph subgraph : currentNode.subGraphs())
        {
            Utils.getTerminalsAndConnectSubParts(subgraph, currentTerminalNodes, dependencies);
        }

        for (int i = 1; i < nodes.size(); i++)
        {
            final AbstractNode nextNode = nodes.get(i);
            final HashSet<Node> nextInitialNodes = new HashSet<>(nextNode.listNodes());

            for (AbstractGraph subgraph : nextNode.subGraphs())
            {
                Utils.getInitials(subgraph, nextInitialNodes);
            }

            for (Node currentTerminal : currentTerminalNodes)
            {
                for (Node nextInitial : nextInitialNodes)
                {
                    dependencies.add(new Dependency(currentTerminal, nextInitial));
                }
            }

            currentNode = nextNode;
            currentTerminalNodes.clear();
            currentTerminalNodes.addAll(currentNode.listNodes());

            for (AbstractGraph subgraph : currentNode.subGraphs())
            {
                Utils.getTerminalsAndConnectSubParts(subgraph, currentTerminalNodes, dependencies);
            }
        }
    }

    private static void getInitials(final AbstractGraph graph,
                                    final HashSet<Node> initialNodes)
    {
        final AbstractNode firstNode = graph.startNode();
        initialNodes.addAll(firstNode.listNodes());

        for (AbstractGraph subGraph : firstNode.subGraphs())
        {
            Utils.getInitials(subGraph, initialNodes);
        }
    }

    private static void getTerminals(final AbstractNode abstractNode,
                                     final HashSet<Node> terminalNodes,
                                     final HashSet<Dependency> dependencies)
    {
        terminalNodes.addAll(abstractNode.listNodes());

        for (AbstractGraph subGraph : abstractNode.subGraphs())
        {
            Utils.getTerminalsAndConnectSubParts(subGraph, terminalNodes, dependencies);
        }
    }
}
