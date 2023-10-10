// Copyright 2022 Voyance Systems

package simulator.model;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class ResourceBank {

    private static final Logger logger = LoggerFactory.getLogger(ResourceBank.class);
    /**
     * Resources are identified by their unique identifiers. The information on resources is kept in a
     * map that associates a resource object to its identifier.
     */
    private final Map<String, SimulationResource> resources;
    /**
     * global cost along time
     */
    private SortedMap<Double, Double> costHistory;
    /**
     * cost of all resources, active and inactive
     */
    private double totalCost;
    /**
     * last update of the cost and emissions of resources, everytime the number of instances of any resource changes the
     * cost and emissions must be updated
     */
    private double lastUpdate;

    /**
     * global emissions along time
     */
    private SortedMap<Double, Double> emissionsHistory;
    /**
     * total emissions
     */
    private double totalEmissions;

    public ResourceBank() {
        resources = new HashMap<>();
        costHistory = new TreeMap<>();
        costHistory.put(0.0, 0.0);

        emissionsHistory = new TreeMap<>();
        emissionsHistory.put(0.0, 0.0);

        lastUpdate = 0;
    }

    public static Resource createResource(String id, double ac, double ic, double emissions) {
        return new ResourceInfo(id, ac, ic, emissions);
    }

    public static Resource createResource(String id, double ac, double ic) {
        return new ResourceInfo(id, ac, ic, 0.0);
    }

    public static Resource createResource(String id, double ac) {
        return createResource(id, ac, ac);
    }

    public void addResource(Resource r) {
        resources.put(r.getId(), new SimulationResource(r));
    }

    public void setNumberOfInstancesOfResource(String id, int n) {
        resources.get(id).setNumberOfInstances(n);
    }

    public int getNumInstances(String key) {
        return resources.get(key).getNumberOfAvailableInstances();
    }

    /**
     * The grabResources method allows a task to take the resources' instances it requires for its
     * execution. We assume that we have previously checked that there are enough resources.
     *
     * @param reqResources is a map with the number of required instances of each type
     * @param ts           is a timestamp
     */
    public void grabResources(Map<String, Integer> reqResources, double ts) {
        updateResources(ts);
        for (Map.Entry<String, Integer> e : reqResources.entrySet()) {
            resources.get(e.getKey()).grabResource(e.getValue(), ts);
        }
    }

    /**
     * The releaseResources method allows a task to release the resources' instances it required for
     * its execution.
     *
     * @param reqResources is a map with the number of instances of each type to release
     * @param ts           is a timestamp
     */
    public void releaseResources(Map<String, Integer> reqResources, double ts) {
        updateResources(ts);
        for (Map.Entry<String, Integer> e : reqResources.entrySet()) {
            resources.get(e.getKey()).releaseResource(e.getValue(), ts);
        }
    }

    public void updateResources(double ts) {
        resources.values().forEach(r -> r.update(ts)); // this updates all resources
        updateGlobalCost(ts); // this updates the global cost
        updateGlobalEmissions(ts); // this updates the global emissions
        lastUpdate = ts;
    }

    private void updateGlobalCost(double ts) {
        double cost =
                resources.values().stream()
                        .map(
                                r ->
                                        (ts - lastUpdate)
                                                * (r.getNumberOfAvailableInstances() * r.getCostWhenInactive()
                                                + (r.getTotalNumberOfInstances() - r.getNumberOfAvailableInstances())
                                                * r.getCostWhenActive()))
                        .reduce(0.0, Double::sum);
        totalCost += cost;
        costHistory.put(ts, cost / ts); // this gives the current cost per time unit
    }

    private void updateGlobalEmissions(double ts) {
        double emissions =
                resources.values().stream()
                        .map(r -> (ts - lastUpdate) * (r.getTotalNumberOfInstances() - r.getNumberOfAvailableInstances()) * r.getEmissions())
                        .reduce(0.0, Double::sum);
        totalEmissions += emissions;
        emissionsHistory.put(ts, emissions / ts); // this gives the current emissions per time unit
    }

    public Map<String, Double> getUsages(double ts) {
        Map<String, Double> usage = new HashMap<>();
        for (Map.Entry<String, SimulationResource> e : resources.entrySet()) {
            // e.getValue().update(0, ts); // unnecessary if all replicas have been released, which is the
            // case if the process has terminated without tokens
            usage.put(e.getKey(), e.getValue().getUsagePercentage(ts));
        }
        return usage;
    }

    public Map<String, Map<Double, Integer>> getAvlHistory() {
        Map<String, Map<Double, Integer>> avl = new HashMap<>();
        for (Map.Entry<String, SimulationResource> e : resources.entrySet()) {
            avl.put(e.getKey(), e.getValue().getAvlHistory());
        }
        return avl;
    }

    public double getTotalCost(double gtime) {
        return resources.values().stream().map(r -> r.getCost()).reduce(0.0, Double::sum);
        // it could be done more efficiently if we don't want the history
        // return resources.values().stream().map(r -> r.totalTimeOfUse * r.costWhenActive + (gtime -
        // r.totalTimeOfUse) * r.costWhenInactive).reduce(0.0, Double::sum);
    }

    public double getTotalEmissions() {
        return resources.values().stream().map(r -> r.getTotalEmissions()).reduce(0.0, Double::sum); //TODO: Verify with Paco
        // it could be done more efficiently if we don't want the history
        // return resources.values().stream().map(r -> r.totalTimeOfUse * r.costWhenActive + (gtime -
        // r.totalTimeOfUse) * r.costWhenInactive).reduce(0.0, Double::sum);
    }

    public Map<Double, Double> getCostHistory() {
        return costHistory;
    }

    public Map<Double, Double> getEmissionsHistory() {
        return emissionsHistory;
    }

    public Map<String, Map<Double, Double>> getCostHistoryPerResource() {
        Map<String, Map<Double, Double>> map = new HashMap<>();
        resources.values().stream().forEach(r -> map.put(r.getId(), r.getCostHistory()));
        return map;
    }

    public Map<String, Map<Double, Double>> getEmissionsHistoryPerResource() {
        Map<String, Map<Double, Double>> map = new HashMap<>();
        resources.values().stream().forEach(r -> map.put(r.getId(), r.getEmissionsHistory()));
        return map;
    }

    /**
     * The Resource class keeps information on a resource type during a simulation
     */
    static class ResourceInfo extends Element implements Resource {    //TODO: Refactor and move out static class?
        /**
         * cost per time unit when active
         */
        private double costWhenActive;
        /**
         * cost per time unit when inactive
         */
        private double costWhenInactive;

        /**
         * emissions per resource
         */
        private double emissionInfo;

        public ResourceInfo(String id) {
            super(id);
        }

        public ResourceInfo(String id, double ac, double ic, double emissionInfo) {
            this(id);
            setCostWhenActive(ac);
            setCostWhenInactive(ic);
            this.emissionInfo = emissionInfo;
        }

        @Override
        public double getCostWhenActive() {
            return costWhenActive;
        }

        @Override
        public void setCostWhenActive(double c) {
            costWhenActive = c;
        }

        @Override
        public double getCostWhenInactive() {
            return costWhenInactive;
        }

        @Override
        public void setCostWhenInactive(double c) {
            costWhenInactive = c;
        }

        @Override
        public double getEmissions() {
            return emissionInfo;
        }

        @Override
        public void setEmissions(double e) {
            emissionInfo = e;
        }
    }

    /**
     * The Resource class keeps information on a resource type during a simulation
     */
    static class SimulationResource extends Element {
        /**
         * cost along time
         */
        private final SortedMap<Double, Double> costHistory;
        /**
         * Emissions along time
         */
        private final SortedMap<Double, Double> emissionsHistory;
        /**
         * number of available replicas of the resource along time
         */
        private final SortedMap<Double, Integer> avlHistory;
        private final SortedMap<Double, Integer> inUseHistory;
        /**
         * total number of replicas of the resource
         */
        private int total;
        /**
         * number of available replicas of the resource
         */
        private int available;
        /**
         * cost per time unit when active
         */
        private double costWhenActive;
        // TODO we don't want to store this info here, possibly will be in an external database
        /**
         * cost per time unit when inactive
         */
        private double costWhenInactive;
        // TODO we don't want to store this info here, possibly will be in an external database
        /**
         * total cost
         */
        private double totalCost;

        /**
         * emissions
         */
        private double emissions;

        /**
         * total emissions
         */
        private double totalEmissions;

        private double totalTimeOfUse;
        private double lastUpdate;

        SimulationResource(String id) {
            super(id);
            avlHistory = new TreeMap<>();
            inUseHistory = new TreeMap<>();
            costHistory = new TreeMap<>();
            costHistory.put(0.0, 0.0);
            emissionsHistory = new TreeMap<>();
            emissionsHistory.put(0.0, 0.0);
        }

        SimulationResource(Resource r) {
            this(r.getId());
            setCostWhenActive(r.getCostWhenActive());
            setCostWhenInactive(r.getCostWhenInactive());
            setEmissions(r.getEmissions());
        }

        SimulationResource(String id, int n, double c) {
            this(id, n, c, c);
        }

        SimulationResource(String id, int n, double ac, double ic) {
            this(id, n, ac, ic, 0.0); //default emissions 0
        }

        SimulationResource(String id, int n, double ac, double ic, double emissions) {
            this(id);
            setNumberOfInstances(n);
            setCostWhenActive(ac);
            setCostWhenInactive(ic); // if not specified, active and inactive cost is the same
            avlHistory.put(0.0, total);
            inUseHistory.put(0.0, total);
            costHistory.put(0.0, 0.0);
            setEmissions(emissions);
            emissionsHistory.put(0.0, 0.0);
            lastUpdate = 0;
        }

        SimulationResource(String id, Pair<Integer, Double> info) {
            this(id, info.getValue0(), info.getValue1());
        }

        SimulationResource(String id, Triplet<Integer, Double, Double> info) {
            this(id, info.getValue0(), info.getValue1(), info.getValue2());
        }

        public void setNumberOfInstances(int n) {
            total = n;
            available = n;
        }

        public int getNumberOfAvailableInstances() {
            return available;
        }

        public double getTotalNumberOfInstances() {
            return total;
        }

        public void grabResource(int req, double ts) {
            // we assume that we have previously checked that there are enough resources
            available -= req;
            avlHistory.put(ts, available);
            inUseHistory.put(ts, total-available);
        }

        public void releaseResource(int req, double ts) {
            available += req;
            avlHistory.put(ts, available);
            inUseHistory.put(ts, total-available);
        }

        void update(double ts) {
            avlHistory.put(ts, available);
            inUseHistory.put(ts, total-available);
            totalTimeOfUse += (ts - lastUpdate) * (total - available);
            totalCost += (ts - lastUpdate) * (available * costWhenInactive + (total - available) * costWhenActive);
            costHistory.put(ts, totalCost);
            totalEmissions += (ts - lastUpdate) * emissions * (total - available);
            emissionsHistory.put(ts, totalEmissions);
            lastUpdate = ts;
        }

        public double getUsagePercentage(double ts) {
            return (100 * totalTimeOfUse) / (total * ts);
        }

        public Map<Double, Integer> getAvlHistory() {
            return avlHistory;
        }

        public Map<Double, Integer> getInUseHistory() {
            return inUseHistory;
        }

        public double getCost() {
            return totalCost;
        }

        public double getCostWhenActive() {
            return costWhenActive;
        }

        public void setCostWhenActive(double c) {
            costWhenActive = c;
        }

        public double getCostWhenInactive() {
            return costWhenInactive;
        }

        public void setCostWhenInactive(double c) {
            costWhenInactive = c;
        }

        public Map<Double, Double> getCostHistory() {
            return costHistory;
        }

        public double getEmissions() {
            return emissions;
        }

        public void setEmissions(double e) {
            emissions = e;
        }

        public double getTotalEmissions() {
            return totalEmissions;
        }

        public Map<Double, Double> getEmissionsHistory() {
            return emissionsHistory;
        }
    }
}
