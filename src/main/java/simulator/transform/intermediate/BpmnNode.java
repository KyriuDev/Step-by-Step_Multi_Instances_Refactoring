//Copyright 2022 Voyance Systems

/**
 * 
 */
package simulator.transform.intermediate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class BpmnNode {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("incomingFlows")
	private List<String> incomingFlows;
	
	@JsonProperty("outgoingFlows")
	private List<String> outgoingFlows;

	public BpmnNode(String id) {
		this.id = id;
		this.type=null;
		this.name=null;
		this.incomingFlows = new ArrayList<String>();
		this.outgoingFlows = new ArrayList<String>();
	}

	/**
	 * @param id
	 * @param name
	 * @param type
	 * @param incomingFlows
	 * @param outgoingFlows
	 */
	@JsonCreator
	public BpmnNode(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("type") String type, 
			@JsonProperty("incomingFlows") List<String> incomingFlows, 
			@JsonProperty("outgoingFlows") List<String> outgoingFlows) {
		this.id = id;
		this.name=name;
		this.type = type;
		this.incomingFlows = incomingFlows;
		this.outgoingFlows = outgoingFlows;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *          the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *          the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the incomingFlows
	 */
	public List<String> getIncomingFlows() {
		if (!(null == incomingFlows || incomingFlows.isEmpty())) {
			return new ArrayList<String>(this.incomingFlows);
		} else {
			return null;
		}
	}

	/**
	 * @param incomingFlows
	 *          the incomingFlows to set
	 */
	public void setIncomingFlows(List<String> incomingFlows) {
		if (!(null == incomingFlows || incomingFlows.isEmpty())) {
			this.incomingFlows = new ArrayList<String>(incomingFlows);
		}
	}

	/**
	 * @return the outgoingFlows
	 */
	public List<String> getOutgoingFlows() {
		if (!(null == outgoingFlows || outgoingFlows.isEmpty())) {
			return new ArrayList<String>(this.outgoingFlows);
		} else {
			return null;
		}
	}

	/**
	 * @param outgoingFlows
	 *          the outgoingFlows to set
	 */
	public void setOutgoingFlows(List<String> outgoingFlows) {
		if (!(null == outgoingFlows || outgoingFlows.isEmpty())) {
			this.outgoingFlows = new ArrayList<String>(outgoingFlows);
		}
	}

	/**
	 * Clone method
	 */
	public Object clone() {
		BpmnNode node = new BpmnNode(this.id, this.getName(), this.getType(), this.incomingFlows, this.outgoingFlows);
		return node;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("[").append("Id: ").append(this.getId())
		.append("Name: ").append(this.getName())
		.append(", ").append("Type: ")
		.append(this.type).append(", ").append("Incoming flows: ");
		if (null != this.incomingFlows && !incomingFlows.isEmpty()) {
			result.append(this.incomingFlows.toString());
		}
		result.append(", ").append("Outgoing flows: ");
		if (null != this.outgoingFlows && !outgoingFlows.isEmpty()) {
			result.append(this.outgoingFlows.toString());
		}
		result.append("]");
		return result.toString();
	}
}
