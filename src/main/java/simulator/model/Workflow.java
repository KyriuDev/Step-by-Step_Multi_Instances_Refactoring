//Copyright 2022 Voyance Systems

/** */
package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *     Workflow class is not used by the simulation. It is useful for BPMN transformation.
 */
public class Workflow {

	private static final Logger logger = LoggerFactory.getLogger(Workflow.class);

	private String id;
	private Map<String, Node> nodes;
	private Map<String, Flow> flows;
	private Collection<Node> startEvents; //TODO: optimize, this is hack for simulation

	/** @param id */
	public Workflow(String id) {
		super();
		this.id = id;
		this.nodes = new HashMap<String, Node>();
		this.flows = new HashMap<String, Flow>();
		this.startEvents = new ArrayList<Node>();
	}

	/**
	 * @param id
	 * @param nodes
	 * @param flows
	 */
	public Workflow(String id, Map<String, Node> nodes, Map<String, Flow> flows) {
		super();
		this.id = id;
		this.nodes = nodes;
		this.flows = flows;
		this.startEvents = new ArrayList<Node>();
	}

	/** @return the id */
	public String getId() {
		return id;
	}

	/** @param id the id to set */
	public void setId(String id) {
		this.id = id;
	}

	/** @return the nodes */
	public Map<String, Node> getNodes() {
		return nodes;
	}

	/** @param nodes the nodes to set */
	public void setNodes(Map<String, Node> nodes) {
		this.nodes = new HashMap<String, Node>(nodes);
	}

	/** @return the flows */
	public Map<String, Flow> getFlows() {
		return flows;
	}

	/** @param flows the flows to set */
	public void setFlows(Map<String, Flow> flows) {
		this.flows = new HashMap<String, Flow>(flows);
	}

	/** @param flow the flow to add */
	public void addFlow(Flow flow) {
		this.flows.put(flow.getId(), flow);
	}

	/** @param flows the flows to add */
	public void addFlows(Map<String, Flow> flows) {
		this.flows.putAll(flows);
	}

	/** @param node the node to add */
	public void addNode(Node node) {
		this.nodes.put(node.getId(), node);
	}

	/** @param nodes the nodes to add */
	public void addNode(Map<String, Node> nodes) {
		this.nodes.putAll(nodes);
	}

	/** @return the startEvents */
	public Collection<Node> getStartEvents() {
		return new ArrayList<Node>(startEvents);
	}

	/** @param startEvents the startEvents to set */
	public void setStartEvents(Collection<Node> startEvents) {
		this.startEvents = new ArrayList<Node>(startEvents);
	}
}
