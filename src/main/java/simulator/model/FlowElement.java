//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All FlowElement understand the run and isReady messages, elements may get activated when reached by tokens, and when
 * activated, they can move tokens forward.
 */
public abstract class FlowElement extends Element {
	
	private static final Logger logger = LoggerFactory.getLogger(FlowElement.class);
	
    public FlowElement(String id) {
        super(id);
    }

    /**
     * This method executes the action activated by the token at the front of the queue. A flow element is executed
     * only i fit is ready. The action will result in different things depending on the specific elements executing it.
     * E.g., a parallel merge will remove the tokens in each of its incoming flows and will place a new token in its
     * outgoing flow.
     *
     * @param simulation info on the state of the simulation
     */
    public abstract void run(Simulation simulation);

    /**
     * This method checks whether the element on which the token at the front of the input queue is ready. A flow
     * element is ready if there is a token on it with timer zero. Additional restrictions may apply to some flow
     * elements. E.g., a parallel merge may require active tokens on each of its incoming flows. A task may require
     * enough resources. The method requires info on the current state of the simulation, including the queue of current
     * tokens and the accumulated info on the state of resources.
     *
     * @param simulation info on the state of the simulation
     * @return boolean value indicating whether the token at the front of the queue is active
     */
    public abstract boolean isReady(Simulation simulation);
}
