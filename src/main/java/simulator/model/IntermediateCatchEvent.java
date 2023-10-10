//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An intermediate catch event represents message reception events. When an event node receives a message, it gets
 * activated, so that the token in its incoming flow may be moved to its outgoing flow. If the event has no incoming
 * flow, a token is directly placed in its outgoing flow upon the reception of a message. Since messages belong to
 * specific runs, the activation of an event is represented as a boolean value associated to a run.
 */
public class IntermediateCatchEvent extends IntermediateEvent {

    private static final Logger logger = LoggerFactory.getLogger(IntermediateCatchEvent.class);

    // private Set<Node> source;
    /**
     * Message description. Not used for execution, just for display.
     **/
    private String message;

    public IntermediateCatchEvent(String id, Flow incoming, Flow outgoing, String message) {
        super(id, incoming, outgoing);
        this.message = message;
    }

    public IntermediateCatchEvent(String id, Flow outgoing, String message) {
        super(id, outgoing);
        this.message = message;
    }

    /**
     * Return the message of the event.
     **/
    public String getMessage() {
        return message;
    }

    /**
     * The value associated to an execution is set to true.
     * For events with no incoming flows (tokenless flows), a token in the outgoing flow is directly placed, and no
     * entry in the map is put. There is no entry in the map for these events, but they are run directly. Notice that
     * since they have no incoming flows, no token would ever produce its execution otherwise.
     *
     * @param simulation state of the simulation
     * @param id         identifier of the execution
     **/
    public void activate(Simulation simulation, int id) {
        if (getIncoming() == null) { // tokenless start
            simulation.getTokens().add(new Token(id, getOutgoing(), getOutgoing().getDelay().sample()));
            active.put(id, false);
        } else {
            super.activate(simulation, id);
        }
    }

    /**
     * When a run is received, the token at the incoming flow of the event is placed in its outgoing flow with the
     * corresponding delay. Since the node is assumed to be activated, the event is set to false for the corresponding
     * run.
     **/
    @Override
    public void run(Simulation simulation) {
        Token t = simulation.getTokens().poll();
        t.setAt(getOutgoing());
        t.setTimer(getOutgoing().getDelay().sample());
        active.put(t.getId(), false);
        simulation.getTokens().add(t);
    }

    /**
     * An event is ready if the timer of the token in its incoming flow is zero and the event message has already
     * arrived.
     */
    @Override
    public boolean isReady(Simulation simulation) {
        Token t = simulation.getTokens().peek();
        return t.getTimer() == 0 && isActive(t.getId());
    }
}
