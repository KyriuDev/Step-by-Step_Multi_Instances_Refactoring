//Copyright 2022 Voyance Systems

package simulator.model;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class Task extends Node {

    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    private final String description;
    private Flow incoming, outgoing;
    private Map<String, Integer> reqResources;
    private AbstractRealDistribution duration; // TODO should be a stochastic expression

    public Task(String id, String description, Flow inf, Flow of, AbstractRealDistribution duration, Map<String, Integer> reqResources) {
        this(id, description);
        setIncoming(inf);
        setOutgoing(of);
        this.duration = duration;
        this.reqResources = reqResources;
    }
    
    public Task(String id, String description, Flow inf, Flow of, Double duration, Map<String, Integer> reqResources) {
        this(id, description);
        setIncoming(inf);
        setOutgoing(of);
        this.duration = new ConstantRealDistribution(duration);
        this.reqResources = reqResources;
    }

    public Task(String id, String description) {
        super(id);
        this.description = description;
        duration = new ConstantRealDistribution(1.0); //TODO: keep it 1 or 0
        reqResources = new HashMap<>();
    }

    /**
     * Returns the incoming flow.
     *
     * @return the incoming flow
     */
    public Flow getIncoming() {
        return incoming;
    }

    public void setIncoming(Flow inf) {
        incoming = inf;
        inf.setTarget(this);
    }
    
    /**
     * Set the required resources
     * @param reqResources
     */
    public void setRequiredResources(Map<String, Integer> reqResources) {
    	this.reqResources = reqResources;
    }

    /**
     * Returns the outgoing flow.
     *
     * @return the outgoing flow
     */
    public Flow getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Flow of) {
        outgoing = of;
        of.setSource(this);
    }

    /**
     * Returns the duration of the task.
     *
     * @return the outgoing SequenceFlow
     */
    public AbstractRealDistribution getDuration() {
        return duration;
    }

    public void setDuration(AbstractRealDistribution d) {
        duration = d;
    }

    @Override
    public void run(Simulation simulation) {
        Token t = simulation.getTokens().poll();
        if (t.getAt() == getIncoming()) {
            simulation.getResources().grabResources(reqResources, simulation.getGtime());
            t.setAt(this);
            t.setTimer(getDuration().sample());
        } else {
            simulation.getResources().releaseResources(reqResources, simulation.getGtime());
            t.setAt(getOutgoing());
            t.setTimer(getOutgoing().getDelay().sample());
        }
        // t.setTimer(getDuration());
        simulation.getTokens().add(t);
    }

    @Override
    public boolean isReady(Simulation simulation) {
        Token t = simulation.getTokens().peek();
        return t.getTimer() == 0
                && (t.getAt() == getIncoming() && allResourcesAvailable(simulation.getResources())
                ||
                t.getAt() == this);
    }

    private boolean allResourcesAvailable(ResourceBank resources) {
        return reqResources.entrySet().stream().allMatch(e -> (resources.getNumInstances(e.getKey()) >= e.getValue()));
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", incoming=" + incoming.getId() +
                ", outgoing=" + outgoing.getId() +
                ", resources=" + reqResources +
                ", duration=" + duration +
                '}';
    }
}
