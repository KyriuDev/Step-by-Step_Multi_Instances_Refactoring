//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 */
public class ParallelGateway extends Gateway {

    private static final Logger logger = LoggerFactory.getLogger(ParallelGateway.class);

    private final Collection<Flow> outgoing;

    public ParallelGateway(String id) {
        this(id, new HashSet<>(), new HashSet<>());
    }

    public ParallelGateway(String id, Collection<Flow> ifs, Collection<Flow> ofs) {
        super(id, ifs);
        outgoing = new HashSet<>();
        setOutgoing(ofs);
    }

    public ParallelGateway(String id, Flow incoming, Collection<Flow> ofs) {
        this(id, Set.of(incoming), ofs);
    }

    public ParallelGateway(String id, Collection<Flow> ifs, Flow f) {
        this(id, ifs, Set.of(f));
    }

    public Collection<Flow> getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Collection<Flow> fs) {
        fs.forEach(f -> {
            f.setSource(this);
            outgoing.add(f);
        });
    }

    @Override
    public void run(Simulation simulation) {
        Integer exec = null; // execution id
        // remove ready tokens in incoming flows
        Set<Flow> flows = new HashSet<>(getIncoming());
        Iterator<Token> it = simulation.getTokens().iterator();
        while (!flows.isEmpty()) {
            Token t = it.next();
            if (exec == null) { // get the execution id from the first token
                exec = t.getId();
            }
            if ((exec.equals(t.getId())) && flows.contains(t.getAt())) {
                it.remove();
                flows.remove(t.getAt());
            }
        }
        // add tokens in outgoing flows
        final int execid = exec; // just for the forEach
        getOutgoing().forEach(f -> simulation.getTokens().add(new Token(execid, f, f.getDelay().sample())));
        // add the sync time and remove the timestamp
        if (getIncoming().size() > 1) {
            simulation.getSyncTimes().get(exec).put(getId(), simulation.getGtime() - simulation.getSyncTimestamps().get(exec).get(id));
            simulation.getSyncTimestamps().get(exec).remove(id);
            simulation.getSyncCounters().put(id, simulation.getSyncCounters().get(id) + 1); // todo could a computeIfPresent be used here?
        }
    }

    @Override
    public boolean isReady(Simulation simulation) {
        int exec = simulation.getTokens().peek().getId();
        if (getIncoming().size() > 1) {
            // if it is the first token to get to the gate, save the timestamp
            simulation.getSyncTimestamps().get(exec).putIfAbsent(id, simulation.getGtime());
            simulation.getSyncCounters().putIfAbsent(id, 0);
        }
        return getIncoming().stream().allMatch(f -> f.isFlowReady(exec, simulation.getTokens()));
    }
}
