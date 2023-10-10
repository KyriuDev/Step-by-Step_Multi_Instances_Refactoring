//Copyright 2022 Voyance Systems

package simulator.model;

/**
 * The Resource interface defines a Resource type, which not only keeps info on the (active and inactive) cost of its
 * instances, but also keeps info on the number of instances, the number of instances in use (or not in use, available),
 * and the history of costs and available instances.
 */
public interface Resource {
    String getId();
    double getCostWhenActive();
    /**
     * Sets the cost of a resource instance (per unit time) when the resource instance is active
     * @param c cost when active
     */
    void setCostWhenActive(double c);
    double getCostWhenInactive();
    /**
     * Sets the cost of a resource instance (per unit time) when the resource instance is inactive
     * @param c cost when active
     */
    void setCostWhenInactive(double c);
    
    double getEmissions();
    /**
     * Sets the emissions of a resource instance
     * @param emissions emissions
     */
    void setEmissions(double emissions);

//    void setNumberOfInstances(int n);
//    int getNumberOfAvailableInstances();
//    double getTotalNumberOfInstances();
//    Map<Double, Integer> getAvlHistory();
//    /**
//     * Grabs n instances of the given resource type at time ts
//     *
//     * @param n  number of instances to grab
//     * @param ts time stamp
//     */
//    void grabResource(int n, double ts);
//    /**
//     * Releases n instances of the given resource type at time ts
//     *
//     * @param n  number of instances to release
//     * @param ts time stamp
//     */
//    void releaseResource(int n, double ts);
//    /**
//     * Total cost of all instances (active and inactive) until the current time
//     *
//     * @return
//     */
//    double getCost();
//    Map<Double, Double> getCostHistory();
//    double getUsagePercentage(double ts);
}
