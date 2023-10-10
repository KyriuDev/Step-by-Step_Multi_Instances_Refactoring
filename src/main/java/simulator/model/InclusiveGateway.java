//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 */
public class InclusiveGateway extends Gateway {
	
	private static final Logger logger = LoggerFactory.getLogger(InclusiveGateway.class);
	
    private final Map<Flow, Double> outgoing;
    private final Random rnd = new Random();

    protected InclusiveGateway(String id, Collection<Flow> ifs, Map<Flow, Double> m) {
        super(id, ifs);
        outgoing = new HashMap<>();
        setOutgoing(m);
    }

    protected InclusiveGateway(String id, Collection<Flow> ifs, Collection<Flow> ofs) {
        super(id, ifs);
        outgoing = new HashMap<>();
        setOutgoing(ofs);
    }

    public InclusiveGateway(String id) {
        this(id, new HashSet<>(), new HashMap<>());
    }

    public InclusiveGateway(String id, Collection<Flow> fs, Flow f) {
        this(id, fs, Set.of(f));
    }

    public InclusiveGateway(String id, Flow f, Map<Flow, Double> m) {
        this(id, Set.of(f), m);
    }

    public InclusiveGateway(String id, Flow f, Collection<Flow> fs) {
        this(id, Set.of(f), fs);
    }

    public Collection<Flow> getOutgoing() {
        return outgoing.keySet();
    }

    public void setOutgoing(Map<Flow, Double> m) {
        outgoing.clear();
        m.entrySet().forEach(e -> {
            e.getKey().setSource(this);
            outgoing.put(e.getKey(), e.getValue());
        });
    }

    /**
     * Sets the provided set of flows as set of outgoing flows.
     *
     * @param fs outgoing flows
     */
    public void setOutgoing(Collection<Flow> fs) {
        outgoing.clear();
        fs.forEach(f -> {
            f.setSource(this);
            outgoing.put(f, 0.0); // by defaults, the prob of taking that path is 0
        });
    }

    /**
     * Sets the provided flow as outgoing flow.
     *
     * @param f outgoing flows
     */
    public void setOutgoing(Flow f) {
        setOutgoing(Set.of(f));
    }

    /**
     * Picks a subset of outgoing flows
     *
     * @param s
     * @return
     */
    protected Collection<Flow> choose(Collection<Flow> s) {
        Set<Flow> set = new HashSet<>();
        s.stream().filter(f -> (outgoing.get(f) > rnd.nextDouble())).forEach(f -> set.add(f));
        return set;
    }

    @Override
    public boolean isReady(Simulation simulation) {
        int exec = simulation.getTokens().peek().getId();
        return getIncoming().stream().anyMatch(f -> f.isFlowReady(exec, simulation.getTokens()));
    }
}
