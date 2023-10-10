//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 */
public class ExclusiveGateway extends Gateway {

    private static final Logger logger = LoggerFactory.getLogger(ExclusiveGateway.class);

    /**
     * Each flow has an associated probability. Probability values of the outgoing flows of an exclusive gate are
     * assumed to sum up 1.
     */
    private final Map<Flow, Double> outgoing;
    private final Random rnd = new Random();

    private ExclusiveGateway(String id, Collection<Flow> fs, Map<Flow, Double> m) {
        super(id, fs);
        outgoing = new HashMap<>();
        setOutgoing(m);
    }

    private ExclusiveGateway(String id, Collection<Flow> ifs, Collection<Flow> ofs) {
        super(id, ifs);
        outgoing = new HashMap<>();
        setOutgoing(ofs);
    }

    /**
     * An exclusive gate is created with no incoming or outgoing flows.
     *
     * @param id identifier
     */
    public ExclusiveGateway(String id) {
        this(id, new HashSet<>(), new HashMap<>());
    }

    /**
     * If there is one single outgoing flow, it can be given as a single flow, in which a map associating probability 1
     * to it is created.
     *
     * @param id identifier
     * @param fs incoming flows
     * @param f  outgoing flow
     */
    public ExclusiveGateway(String id, Collection<Flow> fs, Flow f) {
        this(id, fs, Set.of(f));
    }

    public ExclusiveGateway(String id, Flow f, Map<Flow, Double> m) {
        this(id, Set.of(f), m);
    }

    /**
     * Constructor for an exclusive split gateway for which an incoming flow and a set of outgoing flows are provided.
     * Outgoing flows are assumed equiprobable.
     *
     * @param id identifier of the gate
     * @param f  incoming flow
     * @param fs set of outgoing flows
     */
    public ExclusiveGateway(String id, Flow f, Collection<Flow> fs) {
        this(id, Set.of(f), fs);
    }

    /**
     * Sets the provided set of flows as set of incoming flows.
     *
     * @param fs incoming flows
     */
    public void setIncoming(Set<Flow> fs) {
        incoming.clear();
        incoming.addAll(fs);
    }

    /**
     * Sets the provided flow as incoming flow.
     *
     * @param f incoming flows
     */
    public void setIncoming(Flow f) {
        setIncoming(Set.of(f));
    }

    public Collection<Flow> getOutgoing() {
        return outgoing.keySet();
    }

    /**
     * Sets the provided set of flows as set of outgoing flows.
     *
     * @param m outgoing flows
     */
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
            outgoing.put(f, 1.0 / fs.size());
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
     * Picks one outgoing flow.
     *
     * @param s
     * @return
     */
    protected Collection<Flow> choose(Collection<Flow> s) {
        double acum = 0, p = rnd.nextDouble();
        Flow f = null;
        Iterator<Flow> iter = s.iterator();
        //logger.debug("Handling flows: {}", s);
        do {
            f = iter.next(); // there must be at least one outgoing branch TODO: NoSuchElementException from here? Check with Paco
            acum += outgoing.get(f);
        } while (acum < p); // branches' probabilities must sum up 1
        return Set.of(f);
    }

    /**
     * An exclusive gateway is ready if there is a token with timer zero in any of its incoming flows.
     **/
    @Override
    public boolean isReady(Simulation simulation) {
        int exec = simulation.getTokens().peek().getId();
        return getIncoming().stream().anyMatch(f -> f.isFlowReady(exec, simulation.getTokens()));
    }
}
