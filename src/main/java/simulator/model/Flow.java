//Copyright 2022 Voyance Systems

package simulator.model;

import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ConstantRealDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * A flow represents a connection between two flow elements in a process. Flows may have a delay.
 */
public class Flow extends FlowElement {
	
	private static final Logger logger = LoggerFactory.getLogger(Flow.class);
	
    /**
     * All flows have target and source nodes, but they are created without them, and then, when the nodes are created,
     * they are connected back.
     **/
    private Node target, source;
    /**
     * The delay value represents the time that a token takes in the flow.
     **/
    private AbstractRealDistribution delay;
    /**
     * Flows are created without source and target nodes, the nodes will set these links upon creation.
     **/
    public Flow(String id, AbstractRealDistribution d) {
        super(id);
        setDelay(d);
    }

    public Flow(String id, Double delay) {
    	 this(id, new ConstantRealDistribution(delay));
    }
    
    public Flow(String id) {
        this(id, new ConstantRealDistribution(0.0));
    }

    public void setDelay(AbstractRealDistribution d) {
        delay = d;
    }

    /**
     * Returns the target of the flow.
     *
     * @return target of the flow
     */
    public Node getTarget() {
        return target;
    }

    /**
     * Sets the target of the flow.
     * @param target Node to set as target.
     */
    public void setTarget(Node target) {
        this.target = target;
    }

    /**
     * Returns the source of the flow.
     *
     * @return source of the flow
     */
    public Node getSource() {
        return source;
    }

    /**
     * Sets the source of the flow.
     * @param source Node to set as source.
     */
    public void setSource(Node source) {
        this.source = source;
    }

    /**
     * Returns the delay of the flow.
     *
     * @return double delay of the flow
     */
    public AbstractRealDistribution getDelay() {
        return delay;
    }

    /**
     * Running a flow means running its target flow element.
     * @param simulation info on the state of the simulation
     */
    @Override
    public void run(Simulation simulation) {
        getTarget().run(simulation);
    }

    @Override
    public String toString() {
        return "Flow{" + "id='" + id + '\'' + ", target='" + target.getId() + "', source='" + source.getId() + "'}";
    }

    public boolean isReady(Simulation simulation) {
    	//logger.trace("Flow isReady: {}: {} --> {} ", this.id, this.source, this.target);
        return getTarget().isReady(simulation);
    }

    /** A flow is ready if there is a token with timer zero in it. This method will be invoked by a gateway when
     * checking whether it is ready or not. Split gates will find the corresponding token in the head of the queue.
     * However, e.g., a parallel merge may need tokens in different positions. Since tokens are ordered by time, we
     * can stop looking when a token with time greater than zero is found.
     *
     * @param exec
     * @param tokens
     * @return
     */
    public boolean isFlowReady(int exec, PriorityQueue<Token> tokens) {
        // Optional<Token> t = tokens.stream().dropWhile(e -> (e.getId() != exec || e.getAt() != this)).findFirst();
        // return t.isPresent() && t.get().getTimer() == 0;
        Iterator<Token> it = tokens.iterator();
        while (it.hasNext()) {
            Token t = it.next();
            if (t.getId() == exec && t.getAt() == this && t.getTimer() == 0) {
                return true;
            }
        }
        return false;
    }
}
