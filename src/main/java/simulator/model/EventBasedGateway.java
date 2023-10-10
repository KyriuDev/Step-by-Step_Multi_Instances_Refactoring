//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * An event-based gateway has one single incoming flow and a set of outgoing flows, which have as target event nodes.
 * These event nodes may correspond to catch events or o timer events. When there is a token with timer zero in the
 * incoming flow of an event-based gate, the token is moved to the gate, and the timer events in its outgoing flows
 * get set. When one of its outgoing flows gets activated (a catch event gets a message or a timer events reaches
 * zero), the token is moved to the corresponding flow.
 */
public class EventBasedGateway extends Gateway {

    private static final Logger logger = LoggerFactory.getLogger(EventBasedGateway.class);

    private final Set<Flow> outgoing;
    private Map<Integer, Set<Token>> timers;

    /**
     * Generates an event-based gateway. We assume we only have split event-based gateways. Merges are represented
     * as exclusive gates.
     *
     * @param id  identifier
     * @param inf
     * @param fs
     */
    public EventBasedGateway(String id, Flow inf, Set<Flow> fs) {
        super(id, Set.of(inf));
        outgoing = new HashSet<>();
        setOutgoing(fs);
        timers = new HashMap<>();
    }

    public Set<Flow> getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Collection<Flow> fs) {
        outgoing.clear();
        fs.forEach(f -> {
            f.setSource(this);
            outgoing.add(f);
        });
    }

    /**
     * An event-based gateway is ready if there is a token with timer zero in its incoming flow and any of the
     * intermediate events connected to its outgoing flows is active.
     **/
    @Override
    public boolean isReady(Simulation simulation) {
        Token t = simulation.getTokens().peek();
        int exec = t.getId();
        return t.getAt() == getIncoming().stream().findFirst().orElseThrow() || (t.getAt() == this && getOutgoing().stream().anyMatch(f -> ((IntermediateEvent) f.getTarget()).isActive(exec)));
    }

    public void run(Simulation simulation) {
        Token t = simulation.getTokens().peek();
        if (t.getAt() == getIncoming().stream().findFirst().orElseThrow()) {
            // if there is a token in the incoming flow of an event-based split gate, the token is moved to the gate
            // and timers associated to all outgoing flows are set
            t = simulation.getTokens().poll();
            t.setAt(this);
            simulation.getTokens().add(t);
            timers.put(t.getId(), new HashSet<>());
            for (Flow f : getOutgoing())
                if (f.getTarget() instanceof IntermediateTimerEvent) {
                    IntermediateTimerEvent te = (IntermediateTimerEvent) f.getTarget();
                    Token nt = new Token(t.getId(), te, te.getTime());
                    simulation.getTokens().add(nt);
                    timers.get(t.getId()).add(nt);
                }
        } else {
            // if the token is in the gate itself, then, since we know there is a ready outgoing flow, we just need
            // to find it and move the token to it
            Iterator<Flow> it = getOutgoing().iterator();
            boolean found = false;
            Flow f = null;
            while (!found && it.hasNext()) {
                f = it.next();
                found = f.isReady(simulation);
            }
            t = simulation.getTokens().poll();
            // remove timers in other outgoing flows
            for (Token tt : timers.getOrDefault(t.getId(), new HashSet<>()))
                if (f != tt.getAt()) simulation.getTokens().remove(tt);
            // set the token in the outgoing flow
            t.setAt(f);
            t.setTimer(f.getDelay().sample());
            simulation.getTokens().add(t);
        }
    }
}
