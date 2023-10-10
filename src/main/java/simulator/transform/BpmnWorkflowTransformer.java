//Copyright 2022 Voyance Systems

/** */
package simulator.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulator.model.*;
import simulator.transform.intermediate.BpmnFlow;
import simulator.transform.intermediate.BpmnNode;
import simulator.transform.intermediate.BpmnWorkflow;
import simulator.util.BpmnUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 *     fr.voyance.simulator.transform.intermediate.BpmnWorkflow as input and generates the
 *     fr.voyance.simulator.model.Workflow object for simulation
 */
public class BpmnWorkflowTransformer {

  private static final Logger logger = LoggerFactory.getLogger(BpmnWorkflowTransformer.class);

  private BpmnWorkflow bpmnWorkflow;
  private Workflow workflow;

  /** @param bpmnWorkflow */
  public BpmnWorkflowTransformer(BpmnWorkflow bpmnWorkflow) {
    super();
    this.bpmnWorkflow = bpmnWorkflow;
    
  }

  public void transform() {
    String workflowId = bpmnWorkflow.getId();
    logger.debug("Transforming BpmnWorkflow with id: {}", workflowId);
    Map<String, Flow> flows = new HashMap<String, Flow>();
    Map<String, Node> nodes = new HashMap<String, Node>();
    List<Node> startEvents = new ArrayList<Node>(); // TODO: simulation hack
    for (BpmnFlow bpmnFlow : bpmnWorkflow.getSequences()) {
      String flowId = bpmnFlow.getId();
      //logger.debug("Transforming flow: {} of type {} ", flowId, bpmnFlow.getType());
      Flow flow = new Flow(flowId);
      flows.put(flowId, flow);
    }

    for (BpmnNode bpmnNode : bpmnWorkflow.getNodes()) {
      String nodeId = bpmnNode.getId();
      String nodeType = bpmnNode.getType();
      //logger.debug("Transforming node: {} of type {} ", nodeId, nodeType);
      if (nodeType.equals(BpmnUtils.START_EVENT)) {
        Flow outFlow = flows.get(bpmnNode.getOutgoingFlows().get(0));
        StartEvent startEvent = new StartEvent(nodeId, outFlow);
        startEvents.add(startEvent);
        nodes.put(nodeId, startEvent);
      } else if (nodeType.equals(BpmnUtils.END_EVENT)) {
        Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
        nodes.put(nodeId, new EndEvent(nodeId, inFlow));
      } else if (nodeType.equals(BpmnUtils.TASK)
          || nodeType.toLowerCase().contains(BpmnUtils.TASK)) {
        Flow outFlow = flows.get(bpmnNode.getOutgoingFlows().get(0));
        Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
        Task task = new Task(nodeId, nodeType);
        task.setIncoming(inFlow);
        task.setOutgoing(outFlow);
        nodes.put(nodeId, task);
      } else if (nodeType.equals(BpmnUtils.EXCLUSIVE_GATEWAY)) {
        Set<Flow> incFlows =
            bpmnNode.getIncomingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());
        Set<Flow> outFlows =
            bpmnNode.getOutgoingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());

        ExclusiveGateway exclusiveGateway = new ExclusiveGateway(nodeId);
        exclusiveGateway.setIncoming(incFlows);
        exclusiveGateway.setOutgoing(outFlows);
        nodes.put(nodeId, exclusiveGateway);

      } else if (nodeType.equals(BpmnUtils.EVENT_BASED_GATEWAY)) {
        Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
        Set<Flow> outFlows =
            bpmnNode.getOutgoingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());

        EventBasedGateway eventBasedGateway = new EventBasedGateway(nodeId, inFlow, outFlows);
        nodes.put(nodeId, eventBasedGateway);
      } else if (nodeType.equals(BpmnUtils.INCLUSIVE_GATEWAY)) {
        Set<Flow> incFlows =
            bpmnNode.getIncomingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());
        Set<Flow> outFlows =
            bpmnNode.getOutgoingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());
        InclusiveGateway inclusiveGateway = new InclusiveGateway(nodeId);
        inclusiveGateway.setIncoming(incFlows);
        inclusiveGateway.setOutgoing(outFlows);
        nodes.put(nodeId, inclusiveGateway);

      } else if (nodeType.equals(BpmnUtils.PARALLEL_GATEWAY)) {

        Set<Flow> incFlows =
            bpmnNode.getIncomingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());
        Set<Flow> outFlows =
            bpmnNode.getOutgoingFlows().stream()
                .map(flowId -> flows.get(flowId))
                .collect(Collectors.toSet());
        ParallelGateway parallelGateway = new ParallelGateway(nodeId, incFlows, outFlows);
        nodes.put(nodeId, parallelGateway);
      } else if (nodeType.equals(BpmnUtils.INTERMEDIATE_CATCH_EVENT)) {
        Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
        Flow outFlow = flows.get(bpmnNode.getOutgoingFlows().get(0));
        IntermediateCatchEvent intermediateCatchEvent =
            new IntermediateCatchEvent(nodeId, inFlow, outFlow, nodeType);
        nodes.put(nodeId, intermediateCatchEvent);
      } else if (nodeType.equals(BpmnUtils.INTERMEDIATE_TIMER_EVENT)) {
        Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
        Flow outFlow = flows.get(bpmnNode.getOutgoingFlows().get(0));
        IntermediateTimerEvent intermediateTimerEvent =
            new IntermediateTimerEvent(nodeId, inFlow, outFlow, 100); // TODO: need to read the timer value
        nodes.put(nodeId, intermediateTimerEvent);
      } else if (nodeType.equals(BpmnUtils.INTERMEDIATE_THROW_EVENT)) {
        Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
        Flow outFlow = flows.get(bpmnNode.getOutgoingFlows().get(0));
        IntermediateThrowEvent intermediateThrowEvent =
            new IntermediateThrowEvent(
                nodeId, inFlow, outFlow, Set.of(),
                nodeType); // TODO: set of CatchEvents (unable to get it from parsing)
        nodes.put(nodeId, intermediateThrowEvent);
      } else if (nodeType.equals(BpmnUtils.BOUNDARY_EVENT)) {
          Flow outFlow = flows.get(bpmnNode.getOutgoingFlows().get(0));
          //Flow inFlow = flows.get(bpmnNode.getIncomingFlows().get(0));
          Task task = new Task(nodeId, nodeType); //TODO: quick hack, no boundary event
          //task.setIncoming(inFlow);
          task.setOutgoing(outFlow);
          nodes.put(nodeId, task);
      } else {
        logger.warn("Unable to handle the node of type: {}, node id: {}", nodeType, nodeId);
      }
    }
    // handle flows again to set the target
    for (BpmnFlow bpmnFlow : bpmnWorkflow.getSequences()) {
      String flowId = bpmnFlow.getId();
      String source = bpmnFlow.getSource();
      String target = bpmnFlow.getTarget();
     
      Flow flow = flows.get(flowId);
   
      if (source != null) {
        flow.setSource(nodes.get(source));
      }
      if (target != null) {
        flow.setTarget(nodes.get(target));
      }
      
    }
    
    logger.debug("Node check - {}", nodes.get("Gateway_16psu82"));

    /*Iterator<String> iterator = nodes.keySet().iterator();
    while (iterator.hasNext()) {
        String id = iterator.next();
        Node node = nodes.get(id);
        if(node instanceof IntermediateThrowEvent) {
        	Flow inFlow = ((IntermediateThrowEvent) node).getOutgoing();
        	Flow outFlow = ((IntermediateThrowEvent) node).getOutgoing();
        	logger.debug("ITE {}, source {}, target {}", node, outFlow.getSource(), outFlow.getTarget());
        }
    }*/

    workflow = new Workflow(workflowId, nodes, flows);
    workflow.setStartEvents(startEvents);
  }

  public Workflow getWorkflow() {
    return this.workflow;
  }
}
