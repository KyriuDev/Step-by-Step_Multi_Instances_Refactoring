//Copyright 2022 Voyance Systems

/** */
package simulator.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BpmnUtils {

  public static final String START_EVENT = "startEvent";
  public static final String END_EVENT = "endEvent";
  public static final String SEQUENCE_FLOW = "sequenceFlow";
  public static final String MESSAGE_FLOW = "messageFlow";
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String OUTGOING = "outgoing";
  public static final String INCOMING = "incoming";
  public static final String PROCESS = "process";
  public static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
  public static final String INCLUSIVE_GATEWAY = "inclusiveGateway";
  public static final String EVENT_BASED_GATEWAY = "eventBasedGateway";
  public static final String PARALLEL_GATEWAY = "parallelGateway";
  public static final String TASK = "task";
  public static final String INTERMEDIATE_CATCH_EVENT = "intermediateCatchEvent";
  public static final String INTERMEDIATE_THROW_EVENT = "intermediateThrowEvent";
  public static final String INTERMEDIATE_TIMER_EVENT = "intermediateTimerEvent";
  public static final String BOUNDARY_EVENT = "boundaryEvent";
  
  //For collab diagrams
  public static final String DEFINTIONS = "definitions";

  public static Set<String> NodeValues() {
    return new HashSet<>(
        Arrays.asList(
            START_EVENT,
            END_EVENT,
            EXCLUSIVE_GATEWAY,
            INCLUSIVE_GATEWAY,
            PARALLEL_GATEWAY,
            EVENT_BASED_GATEWAY,
            INTERMEDIATE_CATCH_EVENT,
            INTERMEDIATE_THROW_EVENT,
            INTERMEDIATE_TIMER_EVENT,
            BOUNDARY_EVENT));
  }
  
  public static boolean isOfNodeType(String element) {
	    return NodeValues().stream().anyMatch(element::equals);
	}
}
