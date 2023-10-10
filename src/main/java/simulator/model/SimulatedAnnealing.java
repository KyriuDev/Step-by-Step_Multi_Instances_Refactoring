//Copyright 2022 Voyance Systems

package simulator.model;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SimulatedAnnealing {
    private static final Logger logger = LoggerFactory.getLogger(SimulatedAnnealing.class);

    public static double INITIAL_TEMPERATURE = 1000;
    public static double COOLING_FACTOR = 0.995;
    public static int MAX_JUMP = 5;
    public static Random rnd = new Random();
    private StartEvent sv;
    private Set<Resource> resources;
    private Triplet<Double, Double, Double> coefficients;

    /**
     * @param sv           start event of the process
     * @param resources    set of resources
     * @param coefficients coefficients for execution time and costs to be used in the fitness function
     */
    public SimulatedAnnealing(StartEvent sv, Set<Resource> resources, Triplet<Double, Double, Double> coefficients) {
        this.sv = sv;
        this.resources = resources;
        this.coefficients = coefficients;
    }

    private double normalize(double v, double min, double max) {
        return max == 0 ? 0 : v / max;
        // return (min == max) ? 1 : (v - min) / (max - min);
    }

    /**
     * Recomputes the value of a triplet solution (AET,cost,emissions) given ranges [minAET,maxAET], [minCost,maxCost], and [minEmissions,maxEmissions]
     * and coefficients (w_AET,w_cost,w_emissions)
     *
     * @param p            solution triplet
     * @param minAET       minimum AET
     * @param maxAET       maximum AET
     * @param minCost      minimum cost
     * @param maxCost      maximum cost
     * @param minEmissions minimum emissions
     * @param maxEmissions maximum emissions
     * @param coefficients coefficients (w_AET,w_cost,w_emissions)
     * @return
     */
    double value(Triplet<Double, Double, Double> p, double minAET, double maxAET, double minCost, double maxCost, double minEmissions, double maxEmissions, Triplet<Double, Double, Double> coefficients) {
        return coefficients.getValue0() * normalize(p.getValue0(), minAET, maxAET) + coefficients.getValue1() * normalize(p.getValue1(), minCost, maxCost) + coefficients.getValue2() * normalize(p.getValue2(), minEmissions, maxEmissions);
//        double total = 0;
//        for (int i = 0; i < p.getSize(); i++) {
//            total += (Double) coefficients.getValue(i) * (Double) p.getValue(i);
//        }
//        return total;
    }

    /**
     * Checks that the assignment of resources is valid.
     * TODO Currently it just checks that all values are in the range [1,100), we could check that it's inside a given range, or any other property
     *
     * @param current assignment to be checked
     * @return validity of the assignment
     */
    private boolean valid(Map<String, Integer> current) {
        return current.values().stream().allMatch(i -> (0 < i && i < 100));
    }

    /**
     * The acceptance probability is defined by the formula
     * P = exp(- (f(s_2) - f(s_1)) / T_k)
     * for s_1 and s_2 the current and the next solutions, respectively, f the function defining the
     * value for such solution, and T_k the temperature at step k. If the solution is better, it is
     * directly taken (probability 1), otherwise the formula is used to calculate the acceptance
     * probability.
     *
     * @param f1          value of the current solution
     * @param f2          value of the next candidate solution
     * @param temperature current temperature
     * @return
     */
    private double acceptanceProbability(double f1, double f2, double temperature) {
        return (f2 < f1) ? 1.0 : Math.exp((f1 - f2) / temperature);
    }

    private Map<String, Integer> next(Map<String, Integer> current, String[] resNames) {
        Map<String, Integer> next = new HashMap<>(current);
        String r = resNames[rnd.nextInt(resNames.length)];
        int jump = rnd.nextInt(MAX_JUMP) + 1;
        next.put(r, rnd.nextBoolean() ? next.get(r) + jump : next.get(r) - jump);
        return next;
    }

    public Map<String, Integer> search(Map<String, Integer> initial, int population) {
        logger.debug("Search initiated: {}", initial);
        Map<Map<String, Integer>, Triplet<Double, Double, Double>> visited = new HashMap<>();
        Map<String, Integer> current = new HashMap<>(initial);
        Simulation sim = new Simulation(sv, resources);
        Triplet<Double, Double, Double> currentResult = sim.runSimulation(initial, population);
        String[] resNames = current.keySet().toArray(new String[current.keySet().size()]);

        double minAET = currentResult.getValue0();
        double maxAET = currentResult.getValue0();
        double minCost = currentResult.getValue1();
        double maxCost = currentResult.getValue1();
        double minEmissions = currentResult.getValue2();
        double maxEmissions = currentResult.getValue2();
        double currentValue = value(currentResult, minAET, maxAET, minCost, maxCost, minEmissions, maxEmissions, coefficients);
        visited.put(current, currentResult);

        Map<String, Integer> best = current;
        double bestValue = currentValue;

        for (double t = INITIAL_TEMPERATURE; t > 1; t *= COOLING_FACTOR) {
            Map<String, Integer> neighbor = next(current, resNames);
            if (valid(neighbor) // inside ranges and valid combination
                    && visited.get(neighbor) == null) { // not visited
                sim = new Simulation(sv, resources);
                Triplet<Double, Double, Double> neighborResult = sim.runSimulation(neighbor, population);
                // if the min or max AET or cost change, we must recalculate the values of the visited combinations,
                // possibly changing the best one
                if (neighborResult.getValue0() < minAET || neighborResult.getValue0() > maxAET ||
                        neighborResult.getValue1() < minCost || neighborResult.getValue1() > maxCost ||
                        neighborResult.getValue2() < minEmissions || neighborResult.getValue2() > maxEmissions) {
                    if (neighborResult.getValue0() < minAET) minAET = neighborResult.getValue0();
                    if (neighborResult.getValue0() > maxAET) maxAET = neighborResult.getValue0();
                    if (neighborResult.getValue1() < minCost) minCost = neighborResult.getValue1();
                    if (neighborResult.getValue1() > maxCost) maxCost = neighborResult.getValue1();
                    if (neighborResult.getValue2() < minEmissions) minEmissions = neighborResult.getValue2();
                    if (neighborResult.getValue2() > maxEmissions) maxEmissions = neighborResult.getValue2();
                    for (Map.Entry<Map<String, Integer>, Triplet<Double, Double, Double>> e : visited.entrySet()) {
                        double nv = value(e.getValue(), minAET, maxAET, minCost, maxCost, minEmissions, maxEmissions, coefficients);
                        if (nv < bestValue) {
                            if (!best.equals(e.getKey())) {
                                best = e.getKey();
                            }
                            bestValue = nv;
                        }
                    }
                }
                visited.put(neighbor, neighborResult);
                double neighborValue = value(neighborResult, minAET, maxAET, minCost, maxCost, minEmissions, maxEmissions, coefficients);
                if (neighborValue < bestValue) {
                    best = neighbor;
                    bestValue = neighborValue;
                }
                logger.debug("Search: current {} / result {} / value {}", current, currentResult, currentValue);
                logger.debug("Search: best {} / result {} / value {}", best, visited.get(best), bestValue);

                if (rnd.nextDouble() < acceptanceProbability(currentValue, neighborValue, t)) {
                    current = neighbor;
                    currentResult = neighborResult;
                    currentValue = neighborValue;
                }
            }
        }
        return best;
    }
}