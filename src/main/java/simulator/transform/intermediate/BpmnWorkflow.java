//Copyright 2022 Voyance Systems

/**
 * 
 */
package simulator.transform.intermediate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BpmnWorkflow {
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("nodes")
	private List<BpmnNode> nodes;
	
	@JsonProperty("sequences")
	private List<BpmnFlow> sequences;
	
	@JsonIgnore
	private Map<String, BpmnNode> nodeMap;
	
	@JsonIgnore
	private Map<String, BpmnFlow> sequenceMap;

	public BpmnWorkflow() {
		//
	}
	/**
	 * @param processId
	 * @param nodes
	 * @param sequences
	 */
	@JsonCreator
	public BpmnWorkflow(@JsonProperty("id") String processId, 
			@JsonProperty("nodes") List<BpmnNode> nodes, @JsonProperty("sequences") List<BpmnFlow> sequences) {
		this();
		this.id = processId;
		this.nodes = nodes;
		this.sequences = sequences;
		this.nodeMap = new HashMap<String, BpmnNode>();
		this.sequenceMap = new HashMap<String, BpmnFlow>();
	}

	/**
	 * @return the nodes
	 */
	public List<BpmnNode> getNodes() {
		return new ArrayList<BpmnNode>(nodes);
	}

	/**
	 * @param nodes
	 *          the nodes to set
	 */
	public void setNodes(List<BpmnNode> nodes) {
		this.nodes = new ArrayList<BpmnNode>(nodes);
	}

	/**
	 * @return the sequences
	 */
	public List<BpmnFlow> getSequences() {
		return new ArrayList<BpmnFlow>(sequences);
	}

	/**
	 * the sequences to set.
	 * 
	 * @param sequences
	 * 
	 */
	public void setSequences(List<BpmnFlow> sequences) {
		this.sequences = new ArrayList<BpmnFlow>(sequences);
	}

	/**
	 * return id.
	 * 
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * the id to set.
	 * 
	 * @param id
	 * 
	 */
	public void setId(String id) {
		this.id = id;
	}

	/***
	 * 
	 * @param nodeType
	 * @return
	 */
	public List<BpmnNode> getNodes(String nodeType) {

		List<BpmnNode> nodeList = new ArrayList<BpmnNode>();
		for (BpmnNode node : this.nodes) {
			if (node.getType().equals(nodeType)) {
				nodeList.add(node);
			}
		}

		return nodeList;
	}

	/***
	 * 
	 * @return
	 */
	public Map<String, BpmnNode> getNodeMap() {
		if (this.nodeMap.isEmpty()) {
			this.nodeMap = new HashMap<String, BpmnNode>();
			for (BpmnNode node : this.nodes) {
				nodeMap.put(node.getId(), node);
			}
		}
		return this.nodeMap;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, BpmnFlow> getSequenceMap() {
		if (this.sequenceMap.isEmpty()) {
			this.sequenceMap = new HashMap<String, BpmnFlow>();
			for (BpmnFlow seq : this.sequences) {
				sequenceMap.put(seq.getId(), seq);
			}
		}
		return this.sequenceMap;
	}

}
