// Copyright 2022 Voyance Systems

/** */
package simulator.transform.intermediate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BpmnFlow {

  @JsonProperty("id")
  private String id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("source")
  private String source;

  @JsonProperty("target")
  private String target;

  @JsonProperty("type")
  private String type;

  public BpmnFlow(String id) {
    this.id = id;
    this.type = null;
    this.source = null;
    this.target = null;
    this.type = null;
  }

  @JsonCreator
  public BpmnFlow(
      @JsonProperty("id") String id,
      @JsonProperty("name") String name,
      @JsonProperty("type") String type,
      @JsonProperty("source") String source,
      @JsonProperty("target") String target) {
    super();
    this.id = id;
    this.name = name;
    this.type = type;
    this.source = source;
    this.target = target;
  }

  /** @return the id */
  public String getId() {
    return id;
  }

  /** @param id the id to set */
  public void setId(String id) {
    this.id = id;
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the source */
  public String getSource() {
    return source;
  }

  /** @param source the source to set */
  public void setSource(String source) {
    this.source = source;
  }

  /** @return the type */
  public String getType() {
    return type;
  }

  /** @param type the type to set */
  public void setType(String type) {
    this.type = type;
  }

  /** @return the target */
  public String getTarget() {
    return target;
  }

  /** @param target the target to set */
  public void setTarget(String target) {
    this.target = target;
  }

  /** Clone method */
  public Object clone() {
    BpmnFlow sequence = new BpmnFlow(this.id, this.name, this.type, this.source, this.target);
    return sequence;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result
        .append("[")
        .append("Id: ")
        .append(this.id)
        .append(", ")
        .append("Name: ")
        .append(this.name)
        .append(", ")
        .append("Type: ")
        .append(this.type)
        .append(", ")
        .append("Source: ")
        .append(this.source)
        .append(", ")
        .append("Target: ")
        .append(this.target)
        .append("]");

    return result.toString();
  }
}
