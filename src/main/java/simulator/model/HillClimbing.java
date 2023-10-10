//Copyright 2022 Voyance Systems

package simulator.model;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The HillClimbing class implements a search, following the path with maximal slope, that provides the best
 * assignment of numbers of instances to resources. It keeps a structure of visited assignments, with the
 * calculated results of their simulations. Given a candidate assignment A, which can be seen as a list of
 * natural numbers [n_1, n_2, ... n_m], the algorithm evaluates all its neighbors, (-1, 0, +1) on each of the
 * instances number of the assignment, and moves to the next one as current candidate. For instance, for
 * 2 resources, with instances numbers [4, 7], the assignments
 * [3, 6], [4, 6], [5, 6], [3, 7], [4, 7], [5, 7], [3, 8], [4, 8], [5, 8]
 * are attempted. Simulations are only run for those not in the visited structure. If a better combination
 * is found the algorithm moves to it and start over. When no better assignment is found the algorithm has
 * found a local optimal (minimum), and stops.
 * Values are normalized with respect to current minimum and maximum values for both costs and exec times. When
 * a value greater than the current maximum value or smaller than the current minimum, normalized values are
 * recalculated for all visited solutions.
 * TODO The search can be improved by adding random jumps after finding a local optimum to scape from it, and hence
 *      getting closer to what could be a global optimum. Several jumps, of different sizes may be added.
 */
public class HillClimbing {
    private static final Logger logger = LoggerFactory.getLogger(HillClimbing.class);
    private StartEvent sv;
    private Set<Resource> resources;
    private Triplet<Double, Double, Double> coefficients;

    /**
     * @param sv           start event of the process
     * @param resources    set of resources
     * @param coefficients coefficients for execution time and costs to be used in the fitness function
     */
    public HillClimbing(StartEvent sv, Set<Resource> resources, Triplet<Double, Double, Double> coefficients) {
        this.sv = sv;
        this.resources = resources;
        this.coefficients = coefficients;
    }

    public static void main(String[] args) {
        // Variation v = new Variation(Map.of("a", 5, "b", 5));
        // Variation v = new Variation(Map.of("a", 5, "b", 5, "c", 5));
        Variation v = new Variation(Map.of("a", 1, "b", 1, "c", 1, "d", 1));
        while (v.hasNext()) {
            System.out.println(v.next());
        }
    }

    /**
     * The search method search for a local optimal combination of exec time and total cost for the
     * simulations using combinations of resources starting from hte initial assignment provided as
     * parameter. The method keeps all simulations executed in a map visited, assigning a pair
     * (execTime,cost) to each assignment of resources. These exec times and costs are normalized with
     * respect to the minimum and maximum values found. When a value out of these ranges is found, all
     * solutions pairs are re-normalized, what may result in a new current optimal from which the
     * search continues.
     *
     * @param initial    assignment of resources from which the search begins
     * @param population population use to carry on the simulations
     * @return found local optimal assignment
     */
    public Map<String, Integer> search(Map<String, Integer> initial, int population) {
        logger.debug("Search initiated: {}", initial);
        Map<Map<String, Integer>, Triplet<Double, Double, Double>> visited = new HashMap<>();
        Simulation sim = new Simulation(sv, resources);
        Triplet<Double, Double, Double> result = sim.runSimulation(initial, population);
        visited.put(initial, result);
        double minAET = result.getValue0();
        double maxAET = result.getValue0();
        double minCost = result.getValue1();
        double maxCost = result.getValue1();
        double minEmissions = result.getValue2();
        double maxEmissions = result.getValue2();
        Map<String, Integer> best = initial;
        boolean improved;
        do {
            Map<String, Integer> current = best;
            double bestValue = value(result, minAET, maxAET, minCost, maxCost, minEmissions, maxEmissions, coefficients);
            improved = false;
            // a Variation object will allow us to iterate over the neighbors of the current assignment
            Variation v = new Variation(current);
            while (v.hasNext()) {
                current = v.next();
                if (valid(current)) { // we can check out of ranges or invalid combinations
                    if (visited.get(current) == null) { // not visited
                        sim = new Simulation(sv, resources);
                        result = sim.runSimulation(current, population);
                        // if the min or max AET or cost change, we must recalculate the values of the visited combinations, possibly changing the best one
                        if (result.getValue0() < minAET || result.getValue0() > maxAET || result.getValue1() < minCost || result.getValue1() > maxCost || result.getValue2() < minEmissions || result.getValue2() > maxEmissions) {
                            if (result.getValue0() < minAET) minAET = result.getValue0();
                            if (result.getValue0() > maxAET) maxAET = result.getValue0();
                            if (result.getValue1() < minCost) minCost = result.getValue1();
                            if (result.getValue1() > maxCost) maxCost = result.getValue1();
                            if (result.getValue2() < minEmissions) minEmissions = result.getValue2();
                            if (result.getValue2() > maxEmissions) maxEmissions = result.getValue2();
                            for (Map.Entry<Map<String, Integer>, Triplet<Double, Double, Double>> e : visited.entrySet()) {
                                double nv = value(e.getValue(), minAET, maxAET, minCost, maxCost, minEmissions, maxEmissions, coefficients);
                                if (nv < bestValue) {
                                    if (!best.equals(e.getKey())) {
                                        best = e.getKey();
                                        improved = true;
                                    }
                                    bestValue = nv;
                                }
                            }
                        }
                        visited.put(current, result);
                        double value = value(result, minAET, maxAET, minCost, maxCost, minEmissions, maxEmissions, coefficients);
                        if (value < bestValue) {
                            best = current;
                            bestValue = value;
                            improved = true;
                        }
                        logger.debug("Search: current {} / result {} / value {}", current, result, value);
                        logger.debug("Search: best {} / result {} / value {}", best, visited.get(best), bestValue);
                    }
                }
            }
            logger.debug("Search: best {} / result {} / value {}", best, visited.get(best), bestValue);
        } while (improved);
        logger.debug("Search finished: {}", visited);
        logger.debug("Search finished: {}", best);
        return best;
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
     * Recomputes the value of a pair solution (AET,cost) given ranges [minAET,maxAET] and [minCost,maxCost]
     * and coefficients (w_AET,w_cost)
     *
     * @param p            solution pair
     * @param minAET       minimum AET
     * @param maxAET       maximum AET
     * @param minCost      minimum cost
     * @param maxCost      maximum cost
     * @param coefficients coefficients (w_AET,w_cost)
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

    private double normalize(double v, double min, double max) {
        return max == 0 ? 0 : v / max;
        // return (min == max) ? 1 : (v - min) / (max - min);
    }

    static class Variation {
        private Map<String, Integer> initial;
        private List<String> rs; // list of resource ids
        private List<Integer> is; // difference matrix (-1,-1,-1) (-1,-1,0) (-1,-1,1) (-1,0,-1) (-1,0,0) (-1,0,1)...
        private int i;

        Variation(Map<String, Integer> initial) {
            rs = new ArrayList<>(initial.keySet().size());
            initial.keySet().forEach(r -> rs.add(r));
            is = new ArrayList<>(initial.keySet().size());
            for (int j = 0; j < initial.keySet().size(); j++) is.add(-1);
            this.initial = initial;
            is.set(i, -2);
            i = 0;
        }

        boolean hasNext() {
            return !is.stream().allMatch(n -> n == 1);
        }

        Map<String, Integer> next() {
            if (!hasNext()) throw new NoSuchElementException();
            if (is.get(i) < 1) {
                is.set(i, is.get(i) + 1);
            } else {
                i++;
                int j = 0;
                while (is.get(j) == 1) {
                    is.set(j, -1);
                    j++;
                }
                is.set(j, is.get(j) + 1);
                i = 0;
            }
            Map<String, Integer> next = new HashMap<>();
            for (int j = 0; j < is.size(); j++) {
                next.put(rs.get(j), initial.get(rs.get(j)) + is.get(j));
            }
            return next;
        }
    }
}
