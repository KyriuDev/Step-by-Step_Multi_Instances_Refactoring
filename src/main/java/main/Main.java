package main;

import bpmn.BpmnParser;
import bpmn.graph.Graph;
import bpmn.graph.GraphToList;
import bpmn.graph.ListToGraph;
import bpmn.graph.Node;
import bpmn.types.process.BpmnProcessFactory;
import bpmn.types.process.BpmnProcessObject;
import bpmn.types.process.Task;
import bpmn.writing.direct.DirectWriter;
import bpmn.writing.generation.GraphicalGenerationWriter;
import constants.CommandLineOption;
import constants.PrintLevel;
import hash.cluster.ClusterHasher;
import loops_management.LoopFinder;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import other.Dumper;
import other.Pair;
import other.Utils;
import probabilities.ProbabilityCorrecter;
import refactoring.legacy.Cluster;
import refactoring.legacy.dependencies.DependenciesVerifier;
import refactoring.legacy.dependencies.Dependency;
import refactoring.legacy.exceptions.BadDependencyException;
import refactoring.legacy.partial_order_to_bpmn.AbstractGraphsGenerator;
import refactoring.legacy.partial_order_to_bpmn.BPMNGenerator;
import refactoring.step_by_step.*;
import resources.GlobalResourceSet;
import resources.ResourcePool;
import simulator.GlobalInfoEnum;
import simulator.GlobalInfoGenerator;
import simulator.SimulationResultsAggregator;
import simulator.Simulator;
import simulator.bpmn_to_model_input.ExecutionAttribute;
import simulator.bpmn_to_model_input.SimulationResultsAnalyzer;
import simulator.model.SimulationResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class Main
{
    public static final int PRINT_LEVEL = PrintLevel.PRINT_SOME_IMPORTANT;
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException, IOException, BadDependencyException
    {
        final CommandLineParser commandLineParser;

        try
        {
            commandLineParser = new CommandLineParser(args);
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalStateException("Some necessary files have not be found or are not valid.");
        }

        //Parse the BPMN file
        final BpmnParser parser;

        try
        {
            parser = new BpmnParser((File) commandLineParser.get(CommandLineOption.BPMN_PROCESS));
            parser.parse();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("BPMN process \"{}\" could not be parsed properly. Exiting.", ((File) commandLineParser.get(CommandLineOption.BPMN_PROCESS)).getPath());
            return;
        }

        //Get global dependencies
        final HashSet<Dependency> globalDependencies = (HashSet<Dependency>) commandLineParser.get(CommandLineOption.GLOBAL_DEPENDENCIES);

        Dumper.getInstance().initializeDumper(commandLineParser, parser);

        final ArrayList<BpmnProcessObject> objects = parser.bpmnProcess().objects();
        BpmnProcessFactory.setObjectIDs(objects);

        for (Dependency dependency : globalDependencies)
        {
            boolean found = false;

            for (BpmnProcessObject object : objects)
            {
                if (object.name() != null
                        && object.name().equals(dependency.firstNode().bpmnObject().id()))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
                throw new IllegalStateException("ID |" + dependency.firstNode().bpmnObject().id() + "| does not correspond to any existing object.");
            found = false;

            for (BpmnProcessObject object : objects)
            {
                if (object.name() != null
                        && object.name().equals(dependency.secondNode().bpmnObject().id()))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
                throw new IllegalStateException("ID |" + dependency.secondNode().bpmnObject().id() + "| does not correspond to any existing object.");
        }

        //Compute the set of all resources used
        final GlobalResourceSet globalResourceSet = new GlobalResourceSet(objects);
        globalResourceSet.computeGlobalResources();

        //BPMN objects -> BPMN Graph
        ListToGraph listToGraph = new ListToGraph(objects);
        Graph graph = listToGraph.convert();

        //Compute all loops in the BPMN process
        LoopFinder loopFinder = new LoopFinder(graph);
        loopFinder.findLoops();

        //Correct probabilities
        ProbabilityCorrecter probabilityCorrecter = new ProbabilityCorrecter(graph, loopFinder);
        probabilityCorrecter.correctProbabilities();

        //Extract dependencies from process and keep a copy of the original main cluster
        DependenciesExtractorV2 initialExtractor = new DependenciesExtractorV2(graph, loopFinder);
        final Cluster mainCluster = initialExtractor.extractDependencies();
        final int initialNbTasks = mainCluster.nbTasks();

        //Create the tree of possibilities
        final TreeNode initialNode = new TreeNode(mainCluster.copy(), null);
        final Tree tree = new Tree(initialNode);

        //Store already elected tasks
        final HashSet<Task> alreadyElectedTasks = new HashSet<>();
        final HashSet<TreeNode> currentLeafs = new HashSet<>();
        currentLeafs.add(initialNode);

        int j = 0;
        long totalTime = 0L;

        //Generate global information
        final Map<GlobalInfoEnum, Object> globalInformation;

        if (commandLineParser.containsKey(CommandLineOption.GLOBAL_INFORMATION))
        {
            globalInformation = (Map<GlobalInfoEnum, Object>) commandLineParser.get(CommandLineOption.GLOBAL_INFORMATION);
        }
        else
        {
            globalInformation = GlobalInfoGenerator.generateGlobalInformation(parser.bpmnProcess().objects());
            GlobalInfoGenerator.writeGlobalInfo(globalInformation, (File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY), "current");
        }

        final File workingDirectory = (File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY);
        final ResourcePool availablePool = (ResourcePool) globalInformation.get(GlobalInfoEnum.GLOBAL_RESOURCE_POOL);

        if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
        {
            System.out.println("Available pool: " + availablePool.toString());
        }

        final AbstractRealDistribution iat = (AbstractRealDistribution) globalInformation.get(GlobalInfoEnum.IAT);
        final int nbInstances = (Integer) globalInformation.get(GlobalInfoEnum.NB_INSTANCES);

        Simulator initialSimulator = new Simulator(
                (File) commandLineParser.get(CommandLineOption.BPMN_PROCESS),
                parser.bpmnProcess().objects(),
                globalResourceSet,
                availablePool,
                iat,
                nbInstances
        );
        initialNode.addSimulationResult(SimulationResultsAggregator.aggregate(initialSimulator.simulateMultipleInstances()));

        while (!alreadyElectedTasks.containsAll(parser.bpmnProcess().tasks()))
        {
            final long initTime = System.nanoTime();

            if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_IMPORTANT)
            {
                System.out.println("Iteration n°" + ++j + ":");
            }

            //Elect task to move
            TaskElector taskElector = new TaskElector(
                    parser.bpmnProcess().objects(),
                    alreadyElectedTasks,
                    (ResourcePool) commandLineParser.get(CommandLineOption.REAL_POOL),
                    mainCluster
            );
            Pair<Node, Cluster> electedTask = taskElector.electTask();

            //No task was found --> all tasks have already been processed
            if (electedTask == null)
            {
                if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
                {
                    System.out.println("No eligible task was found.");
                    System.out.print("Already elected tasks: [");

                    for (Task task : alreadyElectedTasks)
                    {
                        System.out.print(task.id() + ",");
                    }

                    System.out.println("]");
                }

                break;
            }

            final Scanner taskScanner1 = new Scanner(System.in);
            System.out.println("Do you agree moving task \"" + electedTask.first().bpmnObject().name() + "\"?");

            String taskAnswerUser = taskScanner1.nextLine();

            while (!Utils.userAgreesMovingTask(taskAnswerUser))
            {
                TaskElector taskElector2 = new TaskElector(
                        parser.bpmnProcess().objects(),
                        alreadyElectedTasks,
                        (ResourcePool) commandLineParser.get(CommandLineOption.REAL_POOL),
                        mainCluster
                );
                electedTask = taskElector2.electTask();

                if (electedTask == null)
                {
                    if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
                    {
                        System.out.println("No eligible task was found.");
                        System.out.print("Already elected tasks: [");

                        for (Task task : alreadyElectedTasks)
                        {
                            System.out.print(task.id() + ",");
                        }

                        System.out.println("]");
                    }

                    break;
                }

                final Scanner taskScanner2 = new Scanner(System.in);
                System.out.println("Do you agree moving task \"" + electedTask.first().bpmnObject().name() + "\"?");

                taskAnswerUser = taskScanner2.nextLine();
            }

            if (electedTask == null)
            {
                if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
                {
                    System.out.println("No eligible task was found.");
                    System.out.print("Already elected tasks: [");

                    for (Task task : alreadyElectedTasks)
                    {
                        System.out.print(task.id() + ",");
                    }

                    System.out.println("]");
                }

                break;
            }

            if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_IMPORTANT)
            {
                System.out.println("Elected task |" + electedTask.first().bpmnObject().id() + "| is in cluster |" + electedTask.second().id() + "|.");
                System.out.println("The current tree has " + currentLeafs.size() + " leafs.");
            }

            int nbLeafsBeforePruning = 0;
            long execTime = System.nanoTime();

            final HashSet<String> newAlreadyBuiltDependencies = new HashSet<>();
            final HashSet<TreeNode> newLeafs = new HashSet<>();
            int currentLeafIndex = 0;
            int nbProcessesWithSameDependencies = 0;
            int nbProcessesWithBadDependencies = 0;

            for (final TreeNode currentLeaf : currentLeafs)
            {
                if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
                {
                    System.out.println("Processing leaf n°" + currentLeafIndex++);
                }

                final Cluster currentMainCluster = currentLeaf.mainCluster().copy();

                if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
                {
                    System.out.println("Dependencies extracted.");
                }

                //Set cluster corresponding to current graph
                electedTask.setSecond(Utils.getTaskCluster(currentMainCluster, electedTask.first()));

                if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
                {
                    System.out.println("Cluster set up");
                }

                //Correct dependencies
                DependenciesCorrectorV2 dependenciesCorrectorV2 = new DependenciesCorrectorV2(currentMainCluster, electedTask.first());
                dependenciesCorrectorV2.correctDependencies();

                if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
                {
                    System.out.println("Dependencies corrected");
                    System.out.println("Main cluster:\n\n" + currentMainCluster.stringify(0));
                }

                //Generate all the combinations for the elected task
                AbstractGraphsCombinerV2 combinerV2 = new AbstractGraphsCombinerV2(currentMainCluster, electedTask);
                try
                {
                    combinerV2.computePossibleAbstractGraphs();
                }
                catch (BadDependencyException e)
                {
                    //Happens when the abstract graph model cannot capture the expressiveness of the process, e.g. https://convecs.inria.fr/doc/publications/Duran-Salaun-17.pdf
                    throw new RuntimeException("The BPMN process given as entry can not be mapped to an equivalent abstract graph. Execution aborted.");
                }
                final ArrayList<Cluster> generatedClusters = combinerV2.onlyGeneratedClusters();

                int generatedLeafIndex = 0;

                //Generate all leafs
                for (int i = 0; i < generatedClusters.size(); i++)
                {
                    if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
                    {
                        System.out.println("Processing generated leaf n°" + generatedLeafIndex++ + " of leaf n°" + currentLeafIndex);
                    }

                    final Cluster newMainCluster = generatedClusters.get(i).copy();
                    Utils.recomputeDependencies(newMainCluster, newMainCluster);
                    final ClusterHasher hasher = new ClusterHasher(newMainCluster);
                    final String hash = hasher.hash();

                    boolean shouldContinue = false;

                    if (!DependenciesVerifier.verifyDependencies(globalDependencies, newMainCluster))
                    {
                        nbProcessesWithBadDependencies++;
                        shouldContinue = true;
                    }

                    if (newAlreadyBuiltDependencies.contains(hash))
                    {
                        nbProcessesWithSameDependencies++;
                        shouldContinue = true;
                    }

                    if (shouldContinue) continue;

                    newAlreadyBuiltDependencies.add(hash);

                    if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
                    {
                        System.out.println("Generated cluster:\n\n" + newMainCluster.stringify(0));
                    }

                    final TreeNode nextNode = new TreeNode(newMainCluster, (Task) electedTask.first().bpmnObject());
                    currentLeaf.addChild(nextNode);
                    nextNode.addParent(currentLeaf);
                    newLeafs.add(nextNode);
                }
            }

            if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_IMPORTANT)
            {
                System.out.println("The current step generated " + newLeafs.size() + " valid leafs, " + nbProcessesWithSameDependencies + " leafs with same dependencies, and " + nbProcessesWithBadDependencies + " leafs with bad dependencies.");
            }

            totalTime += System.nanoTime() - execTime;

            if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
            {
                System.out.println("Nb leafs without pruning: " + nbLeafsBeforePruning);
            }

            final TreeNode previousNode = currentLeafs.iterator().next();

            currentLeafs.clear();
            currentLeafs.addAll(newLeafs);

            //Prune tree with heuristic
            if (true)
            {
                if (true)
                {
                    TreePrunerV1.prune(
                            currentLeafs,
                            workingDirectory,
                            availablePool,
                            iat,
                            nbInstances
                    );
                }
                else
                {
                    TreePrunerV2.prune(
                            currentLeafs,
                            workingDirectory,
                            availablePool,
                            iat,
                            nbInstances
                    );
                }
            }

            final TreeNode nextNode = currentLeafs.iterator().next();

            final Cluster copy = nextNode.mainCluster().copy();

            if (copy.abstractGraphs().isEmpty())
            {
                Utils.resetIndependentNodes(copy);
                final AbstractGraphsGenerator abstractGraphsGenerator = new AbstractGraphsGenerator(copy);
                abstractGraphsGenerator.generateAbstractGraphs();
            }

            BPMNGenerator generator = new BPMNGenerator(copy);
            Graph currentGraph = generator.generate();
            generator = null;

            //Dumper.getInstance().graphicDump(currentGraph, fileName + "1");

            GraphToList graphToList = new GraphToList(currentGraph);
            HashSet<BpmnProcessObject> objects2 = graphToList.convert();
            graphToList = null;
            currentGraph = null;

            GraphicalGenerationWriter graphicalWriter;
            graphicalWriter = new GraphicalGenerationWriter(
                    commandLineParser,
                    new ArrayList<>(objects2),
                    ""
            );

            graphicalWriter.write();
            graphicalWriter = null;

            final String path = Path.of(((File) commandLineParser.get(CommandLineOption.WORKING_DIRECTORY)).getPath(), "refactored_process.bpmn").toString();

            final Scanner abstractGraphScanner = new Scanner(System.in);
            System.out.println("The generated process has been stored at \"" + path + "\". Please verify that it fits your needs and type \"Yes\" if it is the case, \"No\" otherwise.");

            String abstractGraphAnswer = abstractGraphScanner.nextLine();

            if (!Utils.userAgreesWithAbstractGraph(abstractGraphAnswer))
            {
                currentLeafs.clear();
                currentLeafs.add(previousNode);
                System.out.println("The generated process has been discarded. Its previous version will be used for the next computations.");
            }

            new File(path).delete();

            System.out.println("Iteration n°" + j + " took " + Utils.nanoSecToReadable(System.nanoTime() - initTime) + ".\n");
        }

        //The tree has been built, we need to simulate and compute the best steps
        AtomicReference<TreeNode> bestLeaf = new AtomicReference<>(null);
        AtomicReference<TreeNode> worstLeaf = new AtomicReference<>(null);
        AtomicReference<Double> bestAET = new AtomicReference<>(Double.MAX_VALUE);
        AtomicReference<Double> worstAET = new AtomicReference<>(-1d);

        final ExecutorService executorService = Executors.newWorkStealingPool();
        int index = 0;

        for (TreeNode treeNode : currentLeafs)
        {
            final String fileName = "tmp_diagram_" + ++index + ".bpmn";

            if (treeNode.mainCluster().abstractGraphs().isEmpty())
            {
                Utils.resetIndependentNodes(treeNode.mainCluster());
                final AbstractGraphsGenerator abstractGraphsGenerator = new AbstractGraphsGenerator(treeNode.mainCluster());
                abstractGraphsGenerator.generateAbstractGraphs();
            }

            if (treeNode.mainCluster().nbTasks() < initialNbTasks)
            {
                throw new IllegalStateException("The main cluster contains only " + treeNode.mainCluster().nbTasks() + "!\n\n" + treeNode.mainCluster().stringify(0));
            }

            BPMNGenerator generator = new BPMNGenerator(treeNode.mainCluster());
            final Graph currentGraph = generator.generate();
            generator.unprocessClusters();

            GraphToList graphToList = new GraphToList(currentGraph);
            final ArrayList<BpmnProcessObject> objectsList = new ArrayList<>(graphToList.convert());

            DirectWriter directWriter;
            try
            {
                directWriter = new DirectWriter(
                        workingDirectory,
                        objectsList,
                        fileName
                );
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            directWriter.writeInitialBpmnFile();

            executorService.execute(() ->
            {
                Simulator simulator = new Simulator(
                        new File(Path.of(workingDirectory.getPath(), fileName).toString()),
                        objectsList,
                        globalResourceSet,
                        availablePool,
                        iat,
                        nbInstances
                );
                final ArrayList<SimulationResult> results = simulator.simulateMultipleInstances();
                final ExecutionAttribute executionAttribute = SimulationResultsAnalyzer.analyze(results, results);

                //Delete temporary process
                new File(Path.of(workingDirectory.getPath(), fileName).toString()).delete();

                if (bestLeaf.get() == null)
                {

                    bestLeaf.set(treeNode);
                    worstLeaf.set(treeNode);
                    bestAET.set(executionAttribute.aet());
                    worstAET.set(executionAttribute.aet());
                }
                else
                {
                    if (executionAttribute.aet() < bestAET.get())
                    {
                        bestLeaf.set(treeNode);
                        bestAET.set(executionAttribute.aet());
                    }
                    else if (executionAttribute.aet() > worstAET.get())
                    {
                        worstLeaf.set(treeNode);
                        worstAET.set(executionAttribute.aet());
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        //bestLeaf contain the leaf with the shortest AET, we can now write the processes corresponding to each step of its path
        final ArrayList<TreeNode> pathToInitialNode = bestLeaf.get().computePathToInitialNode();
        Collections.reverse(pathToInitialNode);
        pathToInitialNode.remove(0);

        int i = 1;

        for (TreeNode step : pathToInitialNode)
        {
            if (step.mainCluster().abstractGraphs().isEmpty())
            {
                Utils.resetIndependentNodes(step.mainCluster());
                final AbstractGraphsGenerator abstractGraphsGenerator = new AbstractGraphsGenerator(step.mainCluster());
                abstractGraphsGenerator.generateAbstractGraphs();
            }

            BPMNGenerator generator = new BPMNGenerator(step.mainCluster());
            final Graph currentGraph = generator.generate();
            generator.unprocessClusters();

            final GraphToList graphToList = new GraphToList(currentGraph);
            graphToList.convert();

            GraphicalGenerationWriter writer = new GraphicalGenerationWriter(
                    commandLineParser,
                    graphToList.objectsList(),
                    "best_step_" + i++
            );
            writer.write();
        }

        //worstLeaf contain the leaf with the longest AET, we can now write the processes corresponding to each step of its path
        final ArrayList<TreeNode> pathToInitialNode2 = worstLeaf.get().computePathToInitialNode();
        Collections.reverse(pathToInitialNode2);
        pathToInitialNode2.remove(0);

        int k = 1;

        for (TreeNode step : pathToInitialNode2)
        {
            if (step.mainCluster().abstractGraphs().isEmpty())
            {
                Utils.resetIndependentNodes(step.mainCluster());
                final AbstractGraphsGenerator abstractGraphsGenerator = new AbstractGraphsGenerator(step.mainCluster());
                abstractGraphsGenerator.generateAbstractGraphs();
            }

            BPMNGenerator generator = new BPMNGenerator(step.mainCluster());
            final Graph currentGraph = generator.generate();
            generator.unprocessClusters();

            final GraphToList graphToList = new GraphToList(currentGraph);
            graphToList.convert();

            GraphicalGenerationWriter writer = new GraphicalGenerationWriter(
                    commandLineParser,
                    graphToList.objectsList(),
                    "worst_step_" + k++
            );
            writer.write();
        }

        final HashSet<TreeNode> leafs = tree.showLeafs();

        if (PRINT_LEVEL >= PrintLevel.PRINT_SOME_LESS_IMPORTANT)
        {
            System.out.println("AET of best leaf: " + bestAET.get());
            System.out.println("The tree of solutions contains " + leafs.size() + " leafs:\n\n");

            if (PRINT_LEVEL >= PrintLevel.PRINT_ALL)
            {
                for (TreeNode leaf : leafs)
                {
                    System.out.println(leaf.mainCluster().toString());
                }

                System.out.println(tree);
                System.out.println("The tree of solutions contains " + leafs.size() + " leafs.");
            }
        }
    }
}