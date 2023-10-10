//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public abstract class Node extends FlowElement {
	
	private static final Logger logger = LoggerFactory.getLogger(Node.class);
	
    public Node(String id) {
        super(id);
    }
}
