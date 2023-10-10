//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An intermediate timer event represents timer events. Timer events are currently associated to event-based gates.
 * When an event-based gate is executed, timers in its outgoing flows are set to their corresponding values. To
 * appropriately trigger the actions associated to these timers, when a timer is set, a token, at the event node, is
 * added to the token queue. When the timer of the token gets to zero, the event node is run, which means that the
 * event is activated. Since messages belong to specific runs, the activation of an event is represented as a boolean
 * value associated to a run.
 */
public class IntermediateTimerEvent extends IntermediateEvent {

    private static final Logger logger = LoggerFactory.getLogger(IntermediateTimerEvent.class);

    private double time;

    /**
     * An intermediate timer event is supposed to be associated to an event-based gate, with both incoming and outgoing
     * flows.
     *
     * @param id       identifier
     * @param incoming incoming flow
     * @param outgoing outgoing flow
     * @param t        time of the timer
     */
    public IntermediateTimerEvent(String id, Flow incoming, Flow outgoing, double t) {
        super(id, incoming, outgoing);
        time = t;
    }

    public double getTime() {
        return time;
    }

//    /**
//     * Timer events can also have no incoming flows, in which case, when the event gets activated, a token is to be
//     * in its outgoing flow, just as for regular tokenless flows. This is not fully supported yet. TODO
//     */
//    public IntermediateTimerEvent(String id, Flow outgoing, String message) {
//        this(id, null, outgoing);
//    }

    /**
     * Set the timer associated to the event. A token is inserted in the token list with the corresponding value as
     * timer, so that when the timer gets to 0, a run message will be sent to the event.
     *
     * @param simulation state of the simulation
     * @param id         identifier of the execution
     */
    public void setTimer(Simulation simulation, int id) {
        simulation.getTokens().add(new Token(id, this, time));
    }

    /**
     * Running a timer event may do two different things depending on where the token firing the action is located at.
     * If it is in the incoming flow of the event, the token just moves to the outgoing flow. If it is in hte event
     * itself, is because it corresponds to the timer associated to the event, in which case its execution just
     * activates the event. Once activated, the event-based gate associated to it will be in charge of placing a token
     * in its incoming flow.
     **/
    @Override
    public void run(Simulation simulation) {
        Token t = simulation.getTokens().poll();
        if (t.getAt() == getIncoming()) {
            t.setAt(getOutgoing());
            t.setTimer(getOutgoing().getDelay().sample());
        } else {
            active.put(t.getId(), true);
        }
    }

    /**
     * A timer event is ready if there is a token with timer zero in its incoming flow and it is actice, or if there
     * is a token with timer zero directly associated to it.
     **/
    @Override
    public boolean isReady(Simulation simulation) {
        Token t = simulation.getTokens().peek();
        return t.getTimer() == 0 && isActive(t.getId());
    }
}
