// Copyright 2022 Voyance Systems

/** */
package simulator.model;

import java.time.Duration;
import java.util.Map;

public class SimulationResult {

  private Integer population;
  private Duration simulationTime;
  private Double totalExecutionTime;
  private Double avgExecTime;
  private Double totalCost;
  Map<String, Double> usagePercentage;
  Map<String, Map<Double, Integer>> avlHistory;
  Map<String, Double> syncTimes;

  private Double totalEmissions;
  private Map<String, Map<Double, Double>> emissionsHistory;
  private Map<String, Map<Double, Double>> costHistory;

  /** */
  public SimulationResult() {
    super();
  }
  /**
   * @param population
   * @param simulationTime
   * @param totalExecutionTime
   * @param avgExecTime
   * @param totalCost
   * @param usagePercentage
   * @param avlHistory
   * @param syncTimes
   * @param totalEmissions
   * @param emissionsHistory
   */
  public SimulationResult(
      Integer population,
      Duration simulationTime,
      Double totalExecutionTime,
      Double avgExecTime,
      double totalCost,
      Map<String, Double> usagePercentage,
      Map<String, Map<Double, Integer>> avlHistory,
      Map<String, Double> syncTimes,
      Double totalEmissions,
      Map<String, Map<Double, Double>> emissionsHistory,
      Map<String, Map<Double, Double>> costHistory) {
    super();
    this.population = population;
    this.simulationTime = simulationTime;
    this.totalExecutionTime = totalExecutionTime;
    this.avgExecTime = avgExecTime;
    this.totalCost = totalCost;
    this.usagePercentage = usagePercentage;
    this.avlHistory = avlHistory;
    this.syncTimes = syncTimes;
    this.totalEmissions = totalEmissions;
    this.emissionsHistory = emissionsHistory;
    this.costHistory = costHistory;
  }
  /** @return the population */
  public Integer getPopulation() {
    return population;
  }
  /** @param population the population to set */
  public void setPopulation(Integer population) {
    this.population = population;
  }
  /** @return the simulationTime */
  public Duration getSimulationTime() {
    return simulationTime;
  }
  /** @param simulationTime the simulationTime to set */
  public void setSimulationTime(Duration simulationTime) {
    this.simulationTime = simulationTime;
  }
  /** @return the totalExecutionTime */
  public Double getTotalExecutionTime() {
    return totalExecutionTime;
  }
  /** @param totalExecutionTime the totalExecutionTime to set */
  public void setTotalExecutionTime(Double totalExecutionTime) {
    this.totalExecutionTime = totalExecutionTime;
  }
  /** @return the avgExecTime */
  public double getAvgExecTime() {
    return avgExecTime;
  }
  /** @param avgExecTime the avgExecTime to set */
  public void setAvgExecTime(double avgExecTime) {
    this.avgExecTime = avgExecTime;
  }
  /** @return the totalCost */
  public double getTotalCost() {
    return totalCost;
  }
  /** @param totalCost the totalCost to set */
  public void setTotalCost(double totalCost) {
    this.totalCost = totalCost;
  }
  /** @return the usagePercentage */
  public Map<String, Double> getUsagePercentage() {
    return usagePercentage;
  }
  /** @param usagePercentage the usagePercentage to set */
  public void setUsagePercentage(Map<String, Double> usagePercentage) {
    this.usagePercentage = usagePercentage;
  }
  /** @return the avlHistory */
  public Map<String, Map<Double, Integer>> getAvlHistory() {
    return avlHistory;
  }
  /** @param avlHistory the avlHistory to set */
  public void setAvlHistory(Map<String, Map<Double, Integer>> avlHistory) {
    this.avlHistory = avlHistory;
  }
  /** @return the syncTimes */
  public Map<String, Double> getSyncTimes() {
    return syncTimes;
  }
  /** @param syncTimes the syncTimes to set */
  public void setSyncTimes(Map<String, Double> syncTimes) {
    this.syncTimes = syncTimes;
  }
  /** @return the totalEmissions */
  public Double getTotalEmissions() {
    return totalEmissions;
  }
  /** @param totalEmissions the totalEmissions to set */
  public void setTotalEmissions(Double totalEmissions) {
    this.totalEmissions = totalEmissions;
  }
  /** @return the emissionsHistory */
  public Map<String, Map<Double, Double>> getEmissionsHistory() {
    return emissionsHistory;
  }
  /** @param emissionsHistory the emissionsHistory to set */
  public void setEmissionsHistory(Map<String, Map<Double, Double>> emissionsHistory) {
    this.emissionsHistory = emissionsHistory;
  }
  /** @param avgExecTime the avgExecTime to set */
  public void setAvgExecTime(Double avgExecTime) {
    this.avgExecTime = avgExecTime;
  }
  /** @param totalCost the totalCost to set */
  public void setTotalCost(Double totalCost) {
    this.totalCost = totalCost;
  }
  /** @return the costHistory */
  public Map<String, Map<Double, Double>> getCostHistory() {
    return costHistory;
  }
  /** @param costHistory the costHistory to set */
  public void setCostHistory(Map<String, Map<Double, Double>> costHistory) {
    this.costHistory = costHistory;
  }

  @Override
  public String toString() {
    return "SimulationResult [population="
        + population
        + ", simulationTime="
        + simulationTime
        + ", totalExecutionTime="
        + totalExecutionTime
        + ", avgExecTime="
        + avgExecTime
        + ", totalCost="
        + totalCost
        + ", usagePercentage="
        + usagePercentage
        + ", avlHistory="
        + avlHistory
        + ", syncTimes="
        + syncTimes
        + ", totalEmissions="
        + totalEmissions
        + ", emissionsHistory="
        + emissionsHistory
        + ", costHistory="
        + costHistory
        + "]";
  }
}
