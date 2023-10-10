// Copyright 2022 Voyance Systems

package simulator.model;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/** */
public class Simulation {
  private static final Logger logger = LoggerFactory.getLogger(Simulation.class);
  double gtime = 0;
  public static boolean TRACE = false;
  public static String TRACE_ID = "";
  private SimulationResult simulationResult;
  // private Collection<Node> nodes;
  // private Collection<Flow> flows;
  private StartEvent init; // one single start event
  private PriorityQueue<Token> tokens = new PriorityQueue<>();
  private ResourceBank resources;
  private Map<Integer, Double> processExecs = new HashMap<>(), processTstamps = new HashMap<>();
  // for synchronization times: timestamps, times, and number of measures (to calculate average)
  private Map<Integer, Map<String, Double>> syncTimes = new HashMap<>(),
      syncTimestamps = new HashMap<>();
  private Map<String, Integer> syncCounters = new HashMap<>();

  /**
   * The simulation is initialized without resources. They are assumed to be added before the
   * simulation is run.
   *
   * @param init Start event of the process
   */
  public Simulation(StartEvent init) {
    this.init = init;
    this.resources = new ResourceBank();
  }

  public Simulation(StartEvent init, Set<Resource> resources) {
    this(init);
    resources.forEach(r -> addResource(r));
  }

  /**
   * Static method that calculates the averages of values associated to string identifiers in
   * different runs
   */
  public static Map<String, Double> calculateAverages(
      Map<Integer, Map<String, Double>> m, Map<String, Integer> c) {
    Map<String, Double> times = new HashMap<>();
    Map<String, Double> avgs = new HashMap<>();
    m.values()
        .forEach(
            msd ->
                msd.entrySet()
                    .forEach(
                        e ->
                            times.put(
                                e.getKey(),
                                times.getOrDefault(e.getKey(), 0.0) + msd.get(e.getKey()))));
    times.keySet().forEach(id -> avgs.put(id, times.get(id) / c.get(id)));
    return avgs;
  }

  /** @return the simulationResult */
  public SimulationResult getSimulationResult() {
    return simulationResult;
  }

  public void addResource(Resource r) {
    resources.addResource(r);
  }

  public void setNumberOfInstancesOfResource(String r, int i) {
    resources.setNumberOfInstancesOfResource(r, i);
  }

  public PriorityQueue<Token> getTokens() {
    return tokens;
  }

  public ResourceBank getResources() {
    return resources;
  }

  public double getGtime() {
    return gtime;
  }

  public Map<Integer, Double> getProcessExecs() {
    return processExecs;
  }

  Map<Integer, Double> getProcessTstamps() {
    return processTstamps;
  }

  public Map<Integer, Map<String, Double>> getSyncTimes() {
    return syncTimes;
  }

  Map<Integer, Map<String, Double>> getSyncTimestamps() {
    return syncTimestamps;
  }

  public Map<String, Integer> getSyncCounters() {
    return syncCounters;
  }

  public Triplet<Double, Double, Double> runSimulation(
      Map<String, Integer> initial, int population, AbstractRealDistribution interArrivalTime) {
    for (String r : initial.keySet()) {
      setNumberOfInstancesOfResource(r, initial.get(r));
    }
    return runSimulation(population, interArrivalTime);
  }

  public Triplet<Double, Double, Double> runSimulation(Map<String, Integer> initial, int population) {
    return runSimulation(
        initial, population, new ExponentialDistribution(5)); // TODO: what is the default value?
  }

  /**
   * Simulates the execution of the process population number of times, using a workload with an
   * inter-arrival time. The available resources are supposed to have been previously initialized,
   * either by using the constructor Simulation(StartEvent, Set<Resource>) or the constructor
   * Simulation(StartEvent init) and then adding them by using the addResource(Resource) method.
   *
   * @param population number of times that the process is executed
   * @param interArrivalTime
   * @return a triplet with the resulting AET, cost, and emissions
   */
  private Triplet<Double, Double, Double> runSimulation(
      int population, AbstractRealDistribution interArrivalTime) {
    Instant start = Instant.now();
    tokens.clear();
    // TODO the workload generation and the simulation could be run in parallel
    workload(population, interArrivalTime);
    run();
    // TODO results may be shown
    double execTimes = 0;
    int n = 0;
    for (Double v : getProcessExecs().values()) {
      // logger.debug("Exec counter {}, value {}, and time {} ", n, v, execTimes);
      n++;
      execTimes += v;
    }
    Instant end = Instant.now();
    double avgExecTime = execTimes / n;
    double totalCost = resources.getTotalCost(getGtime());
  
    Map<String, Double> usage = resources.getUsages(getGtime());
    Map<String, Map<Double, Integer>> avlHistory = resources.getAvlHistory();
    Map<Double, Double> costHistory = resources.getCostHistory();
    Map<String, Map<Double, Double>> costHistoryPerResource = resources.getCostHistoryPerResource();

    Double totalEmissions = resources.getTotalEmissions();
    Map<String, Map<Double, Double>> emissionsHistoryPerResource =
        resources.getEmissionsHistoryPerResource();

    logger.debug("Population: {}", population);
    logger.debug("Simulation time: {}", Duration.between(start, end));
    logger.debug("Total exec time: {}", gtime);
    logger.debug("Average exec time: {}", avgExecTime);
    logger.debug("Total cost: {}", totalCost);
    logger.debug("Average cost: {}", totalCost / population);
    logger.debug("Average usage percentage: {}", usage);
    
    logger.debug("Total emissions: {}", totalEmissions);
    logger.debug("Emissions history: {}", emissionsHistoryPerResource);
    
    // logger.debug("Availability history: {}", avlHistory);
    // logger.debug("Cost history: {}", costHistory);
    // logger.debug("Cost history per resource: {}", costHistoryPerResource);
    if (!getTokens().isEmpty()) {
      logger.debug("Pending tokens: {}", getTokens());
    }
    //	logger.debug("Sync times of parallel merges: {}", calculateAverages(getSyncTimes(),
    // getSyncCounters()));

    this.simulationResult =
        new SimulationResult(
            population,
            Duration.between(start, end),
            gtime,
            avgExecTime,
            totalCost,
            usage,
            avlHistory,
            calculateAverages(getSyncTimes(), getSyncCounters()),
            totalEmissions,
            emissionsHistoryPerResource, costHistoryPerResource);
    logger.debug("Simulation Result: {}", simulationResult);
    return Triplet.with(avgExecTime, totalCost, totalEmissions);
  }

  /***
   * Closed workload, POPULATION runs with inter-arrival time given by a distribution.
   * The workload follows the distribution.
   * The first job is scheduled for time 0.
   ***/
  private void workload(int population, AbstractRealDistribution iat) {
    double t = 0;
    for (int i = 0; i < population; i++) {
      tokens.add(new Token(i, init, t)); // adds a token at the start event
      getSyncTimestamps().put(i, new HashMap<String, Double>());
      getSyncTimes().put(i, new HashMap<String, Double>());
      t += iat.sample();
    }
  }

  /**
   * The simulation is not deterministic. Different runs may lead to different results. TODO: Is it... ANSWER: Not with the same seeds
   * true that variability is reduced for greater populations?
   */
  private void run() {
    Collection<Token> shiftedTokens = new ArrayList<>();
    boolean exit = false;
    while (!exit && !tokens.isEmpty()) {
      // traverse the tokens queue until a ready one is found
      Token t = tokens.peek();
      FlowElement e = t.getAt();
      boolean stop = false;
      while (!stop && !e.isReady(this)) {
        if (t.getTimer() > 0) {
          stop = true;
        } else {
          shiftedTokens.add(tokens.poll());
          if (tokens.isEmpty()) {
            stop = true;
          } else {
            t = tokens.peek();
            e = t.getAt();
          }
        }
      }
      if (tokens.isEmpty()) {
        // if there is no token, either ready or not, the execution must be aborted
        // there might be tokens in the shiftedTokens queue, but if they were not ready before, and
        // nothing has
        // happened, they will continue non-ready
        exit = true;
      } else {
        // if there is a token, it is either ready or have a non-0 timer
        if (t.getTimer() > 0) {
          gtime += t.getTimer();
          decTimers(tokens, t.getTimer());
          decTimers(shiftedTokens, t.getTimer());
        }
        // if the token at the front is ready and its timer is 0 then it can be run
        if (e.isReady(this)) {
          if (TRACE) {
            try {
              PrintWriter pw =
                  new PrintWriter(
                      new FileOutputStream(
                          new File("traces/trace" + TRACE_ID + "-" + t.getId() + ".csv"), true));
              pw.printf("%s,%s\n", t.getAt().getId(), gtime);
              pw.close();
            } catch (FileNotFoundException fnfe) {
              logger.debug("Trace file cannot be opened");
            }
          }
          e.run(this);
        }
      }
      tokens.addAll(shiftedTokens);
      shiftedTokens.clear();
    }
    this.getResources().updateResources(gtime);
  }

  private void decTimers(Collection<Token> tokens, double timer) {
    tokens.forEach(t -> t.setTimer(t.getTimer() - timer));
  }
}
