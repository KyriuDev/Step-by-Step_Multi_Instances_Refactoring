//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * An intermediate throw event represents message emission events. A throw event has a set of target
 * catch events. When the throw event is run, the target catch events are activated, simulating the
 * emission of messages.
 */
public class IntermediateThrowEvent extends IntermediateEvent {

  private static final Logger logger = LoggerFactory.getLogger(IntermediateThrowEvent.class);

  private String message;
  private Set<IntermediateCatchEvent> target;

  public IntermediateThrowEvent(String id) {
    super(id);
  }

  public void setIncoming(Flow f) {
    super.setIncoming(f);
  }

  public void setOutgoing(Flow f) {
    super.setOutgoing(f);
  }

  /** @param target the target to set */
  public void setTarget(Set<IntermediateCatchEvent> target) {
    this.target = target;
  }

  public void setTarget(IntermediateCatchEvent target) {
    this.target = Set.of(target);
  }

  /** @param m the message to set */
  public void setMessage(String m) {
    this.message = message;
  }

  /** Returns the message of the event. */
  public String getMessage() {
    return message;
  }

  /** Returns the node set of the event. */
  public Set<IntermediateCatchEvent> getTarget() {
    return target;
  }

  public IntermediateThrowEvent(
      String id, Flow incoming, Flow outgoing, Set<IntermediateCatchEvent> target, String message) {
    super(id, incoming, outgoing);
    this.target = target;
    this.message = message;
  }

  public IntermediateThrowEvent(
      String id, Flow incoming, Flow outgoing, IntermediateCatchEvent target, String message) {
    this(id, incoming, outgoing, Set.of(target), message);
  }

  /**
   * When a throw event is run, the token moves from its incoming flow to its outgoing one, setting
   * its timer with the delay of the outgoing flow, and ach of the target events is activated.
   */
  @Override
  public void run(Simulation simulation) {
    Token t = simulation.getTokens().poll();
    t.setAt(getOutgoing());
    t.setTimer(getOutgoing().getDelay().sample());
    simulation.getTokens().add(t);
    target.stream().forEach(n -> n.activate(simulation, t.getId()));
  }

  /** A throw event is ready if there is a token in its incoming flow with timer zero. */
  @Override
  public boolean isReady(Simulation simulation) {
    Token t = simulation.getTokens().peek();
    return t.getTimer() == 0 && t.getAt() == getIncoming();
  }
}
