//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An EndEvent has one single incoming flow.
 */
public class EndEvent extends Event {
	
	private static final Logger logger = LoggerFactory.getLogger(EndEvent.class);
	
    /**
     * Incoming flow on the EndEvent
     **/
    private final Flow incoming;

    public EndEvent(String id, Flow incoming) {
        super(id);
        this.incoming = incoming;
        incoming.setTarget(this);
    }

    /**
     * Returns the incoming flow.
     *
     * @return the incoming flow
     */
    public Flow getIncoming() {
        return incoming;
    }

    @Override
    public void run(Simulation simulation) {
        Token t = simulation.getTokens().poll();
        if (t.getAt() == getIncoming()) {
            t.setAt(this);
            simulation.getTokens().add(t);
        } else {
            // if multiple end events, the last one prevails
            simulation.getProcessExecs().put(t.getId(), simulation.getGtime() - simulation.getProcessTstamps().get(t.getId()));
            //logger.trace(t.getId() + ": (" + simulation.getGtime() + ") " + (simulation.getGtime() - simulation.getProcessTstamps().get(t.getId())));
            //logger.trace("{}: ({}) {} ", t.getId(), simulation.getGtime(), (simulation.getGtime() - simulation.getProcessTstamps().get(t.getId())));
        }
    }

    @Override
    public boolean isReady(Simulation simulation) {
        Token t = simulation.getTokens().peek();
        return (t.getAt() == getIncoming() || t.getAt() == this);
    }

    @Override
    public String toString() {
        return "EndEvent{" + "id='" + id + '\'' + ", incoming=" + incoming.getId() + '}';
    }
}
