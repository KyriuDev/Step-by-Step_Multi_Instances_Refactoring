// Copyright 2022 Voyance Systems

/** */
package simulator.transform;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import simulator.model.Workflow;
import simulator.transform.intermediate.BpmnFlow;
import simulator.transform.intermediate.BpmnNode;
import simulator.transform.intermediate.BpmnWorkflow;
import simulator.util.BpmnUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** fr.voyance.bpmn.activities.Workflow.workflow} object */
public class BpmnXmlParser {

  private static final Logger logger = LoggerFactory.getLogger(BpmnXmlParser.class);

  private InputStream inputBpmn;
  private BpmnWorkflow outputWorkflow;

  public BpmnXmlParser(InputStream input) {
    this.inputBpmn = input;
  }

  public Workflow handle() {

    try {
      XMLInputFactory2 inputFactory = (XMLInputFactory2) XMLInputFactory2.newInstance();
      XMLStreamReader2 streamReader =
          (XMLStreamReader2) inputFactory.createXMLStreamReader(inputBpmn);

      // BpmnWorkflow workflow;
      String workflowId = UUID.randomUUID().toString();
      List<BpmnNode> nodeList = new ArrayList<BpmnNode>();
      List<BpmnFlow> sequenceList = new ArrayList<BpmnFlow>();

      while (streamReader.hasNext()) {
        int eventType = streamReader.next();
        String elementType = null;
        if (eventType == XMLStreamReader.END_ELEMENT) {
          elementType = streamReader.getLocalName();
          if (elementType.equals(BpmnUtils.DEFINTIONS)) {
            outputWorkflow =
                new BpmnWorkflow(
                    workflowId, verifyAndUpdateNodes(nodeList, sequenceList), sequenceList);
            break;
          }
        } else if (eventType == XMLStreamReader.START_ELEMENT) {
          elementType = streamReader.getLocalName();
          if (BpmnUtils.isOfNodeType(elementType)
              || elementType.toLowerCase().endsWith(BpmnUtils.TASK)
              || elementType.equals(BpmnUtils.TASK)) {
            BpmnNode node = handleNode(streamReader, elementType);
            //logger.debug("Node info: {}", node.toString());
            nodeList.add(node);
          } else if (elementType.equals(BpmnUtils.SEQUENCE_FLOW)
              || elementType.equals(BpmnUtils.MESSAGE_FLOW)) {
            String id = streamReader.getAttributeValue(null, BpmnUtils.ID);
            String name = streamReader.getAttributeValue(null, BpmnUtils.NAME);
            String source = streamReader.getAttributeValue(null, "sourceRef");
            String target = streamReader.getAttributeValue(null, "targetRef");
            // logger.debug("Parsing Sequence id: {} ", id);
            BpmnFlow sequence = new BpmnFlow(id, name, elementType, source, target);
            // logger.debug("Sequence info: {}", sequence.toString());
            sequenceList.add(sequence);
          } else {
            //logger.warn("Parser did not process element: {}", streamReader.getLocalName());
          }
        }
      }

      streamReader.closeCompletely();
    } catch (XMLStreamException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Additional check for handling BPMN XMLs without incoming/outgoing flows
   *
   * @param nodeList
   * @param flowList
   * @return
   */
  private List<BpmnNode> verifyAndUpdateNodes(List<BpmnNode> nodeList, List<BpmnFlow> flowList) {

    List<BpmnNode> revisedNodeList = new ArrayList<BpmnNode>();

    for (BpmnNode node : nodeList) {
      String nodeName = node.getId();
      // BpmnNode updatedNode = node;
      List<String> incomingFlows = new ArrayList<>();
      List<String> outgoingFlows = new ArrayList<>();
      // update flows only if flows are not captured during initial parsing
      if (node.getIncomingFlows() == null || node.getIncomingFlows().isEmpty()) {
        for (BpmnFlow flow : flowList) {
          if (flow.getSource().equals(nodeName)) {
            outgoingFlows.add(flow.getId());
          }
          if (flow.getTarget().equals(nodeName)) {
            incomingFlows.add(flow.getId());
          }
        }
        node.setIncomingFlows(incomingFlows);
        node.setOutgoingFlows(outgoingFlows);
      }
      revisedNodeList.add(node);
    }
    return revisedNodeList;
  }

  private BpmnNode handleNode(XMLStreamReader2 streamReader, String eventName)
      throws XMLStreamException {
    List<String> incomingFlows = new ArrayList<String>();
    List<String> outgoingFlows = new ArrayList<String>();
    BpmnNode node = null;

    while (streamReader.hasNext()) {
      if (streamReader.getEventType() == XMLStreamReader.END_ELEMENT) {
        String elementType = streamReader.getLocalName();
        if (elementType.equals(eventName)) {
          node.setIncomingFlows(incomingFlows);
          node.setOutgoingFlows(outgoingFlows);
          break;
        }
      } else if (streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
        String elementName = streamReader.getLocalName();
        String flowId;
        if (elementName.equals(eventName)) {
          String id = streamReader.getAttributeValue(null, BpmnUtils.ID);
          String name = streamReader.getAttributeValue(null, BpmnUtils.NAME);
          //logger.debug("Parsing {} id: {} ", elementName, id);
          logger.debug("{}, {}, {}", elementName, name, id);
          node = new BpmnNode(id);
          node.setName(name);
          node.setType(eventName);
        } else if (elementName.equals(BpmnUtils.INCOMING)) {
          flowId = readCharacters(streamReader);
          incomingFlows.add(flowId);
        } else if (elementName.equals(BpmnUtils.OUTGOING)) {
          flowId = readCharacters(streamReader);
          outgoingFlows.add(flowId);
        }
      }
      streamReader.next();
    }

    node.setType(eventName);
    // optional - only for a very specific bpmn xml
    if (eventName.equals(BpmnUtils.START_EVENT)) {
      node.setIncomingFlows(null);
    }
    if (eventName.equals(BpmnUtils.END_EVENT)) {
      node.setOutgoingFlows(null);
    }
    return node;
  }

  private String readCharacters(final XMLStreamReader2 streamReader) throws XMLStreamException {
    StringBuilder result = new StringBuilder();
    while (streamReader.hasNext()) {
      int eventType = streamReader.next();
      if (eventType == XMLStreamReader.CHARACTERS) {
        result.append(streamReader.getText());
      } else if (eventType == XMLStreamReader.END_ELEMENT) {
        return result.toString();
      }
    }
    logger.error("Error parsing the file @ {}", streamReader.getLocation().toString());
    throw new XMLStreamException("Error parsing the file");
  }

  public BpmnWorkflow getOutputWorklow() {
    return this.outputWorkflow;
  }
}
