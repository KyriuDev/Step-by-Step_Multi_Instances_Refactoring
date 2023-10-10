//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An intermediate event is an event in a process, a flow element, with incoming and outgoing flows. If an initiating
 * event, an event which starts a process or process lane, it has no incoming flow, in which case it is set to null.
 */
public abstract class IntermediateEvent extends Event {

    private static final Logger logger = LoggerFactory.getLogger(IntermediateEvent.class);
    /**
     * A boolean value is associated to each execution. An id-boolean entry is created when the event gets activated.
     * Therefore, for an execution id, a non-active event is either false or there is no entry (null).
     **/
    protected Map<Integer, Boolean> active;
    private Flow incoming, outgoing;

    public IntermediateEvent(String id) {
        super(id);
        active = new HashMap<>();
    }

    public IntermediateEvent(String id, Flow incoming, Flow outgoing) {
        this(id);
        setIncoming(incoming);
        setOutgoing(outgoing);
    }

    public IntermediateEvent(String id, Flow outgoing) {
        this(id);
        setOutgoing(outgoing);
    }

    /**
     * Returns the incoming flow of the event.
     *
     * @return Flow incoming flow
     */
    public Flow getIncoming() {
        return incoming;
    }

    public void setIncoming(Flow incoming) {
        if (incoming == null) throw new IllegalArgumentException("null incoming flow");
        this.incoming = incoming;
        incoming.setTarget(this);
    }

    /**
     * Returns the outgoing flow of the event.
     *
     * @return Flow outgoing flow
     */
    public Flow getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Flow outgoing) {
        if (outgoing == null) throw new IllegalArgumentException("null outgoing flow");
        this.outgoing = outgoing;
        outgoing.setSource(this);
    }

    /**
     * The value associated to an execution is set to true.
     *
     * @param simulation state of the simulation
     * @param id         identifier of the execution
     */
    public void activate(Simulation simulation, int id) {
        active.put(id, true);
    }

    /**
     * For an execution id, an event may be active or non-active (false or with no associated entry).
     *
     * @param id identifier of the execution
     * @return activation value
     */
    public boolean isActive(int id) {
        return active.get(id) != null && active.get(id);
    }

}
