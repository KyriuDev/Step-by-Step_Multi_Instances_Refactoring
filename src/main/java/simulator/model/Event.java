//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An event represents an event flow node.
 */
public abstract class Event extends Node {
	
	private static final Logger logger = LoggerFactory.getLogger(Event.class);
	
    public Event(String id) {
        super(id);
    }
}
