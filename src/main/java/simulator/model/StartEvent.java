//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class StartEvent extends Event {

    private static final Logger logger = LoggerFactory.getLogger(StartEvent.class);

    private Flow outgoing;

    public StartEvent(String id) {
        super(id);
    }

    public StartEvent(String id, Flow f) {
        super(id);
        setOutgoing(f);
    }

    /**
     * Returns the outgoing SequenceFlow.
     *
     * @return the outgoing SequenceFlow
     */
    public Flow getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Flow f) {
        outgoing = f;
        outgoing.setSource(this);
    }

    @Override
    public void run(Simulation simulation) {
        Token t = simulation.getTokens().poll();
        t.setAt(getOutgoing());
        t.setTimer(getOutgoing().getDelay().sample());
        simulation.getTokens().add(t);
        simulation.getProcessTstamps().put(t.getId(), simulation.getGtime());
        //logger.trace("{}: {},", t.getId(), simulation.getGtime());
    }

    @Override
    public boolean isReady(Simulation simulation) {
        return true;
    }

    @Override
    public String toString() {
        return "StartEvent{" +
                "id='" + id + '\'' +
                ", outgoing=" + outgoing.getId() +
                '}';
    }
}
