//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

/**
 * Gateways may have incoming and outgoing flows, but the class Gateway only have a variable incoming. Different
 * subclasses may represent outgoing flows in different ways (an exclusive gate may have a map from flow to double
 * representing the proabiblity of choosing each flow), but all have get methods getIncoming() and getOutgoing()
 * returning sets (or collections) of flows.
 */
public abstract class Gateway extends Node {

    private static final Logger logger = LoggerFactory.getLogger(Gateway.class);

    protected final Collection<Flow> incoming;

    public Gateway(String id, Collection<Flow> incoming) {
        super(id);
        this.incoming = incoming;
        incoming.forEach(f -> f.setTarget(this));
    }

    public Gateway(String id) {
        super(id);
        this.incoming = new HashSet<>();
    }

    public Collection<Flow> getIncoming() {
        return incoming;
    }

    public void setIncoming(Collection<Flow> incoming) {
        this.incoming.clear();
        incoming.forEach(f -> {
            f.setTarget(this);
            this.incoming.add(f);
        });
    }

    public abstract Collection<Flow> getOutgoing();

    public abstract void setOutgoing(Collection<Flow> outgoing);

    /**
     * Returns all outgoing flows. Subclasses will redefine this method depending on the type of split:
     * for parallel splits no redefinition is needed; for exclusive split only one flow is returned; and for
     * inclusive splits a subset of flows is returned.
     *
     * @param s a collection of flows from which to choose one or several flows
     * @return collections of selected flows
     */
    protected Collection<Flow> choose(Collection<Flow> s) {
        return s;
    }


    @Override
    public void run(Simulation simulation) {
        Token t = simulation.getTokens().poll();
        choose(getOutgoing()).forEach(f -> simulation.getTokens().add(new Token(t.getId(), f, f.getDelay().sample())));
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" + "id='" + id + '\'' + ", incoming={" + getIncoming().stream().map(a -> a.getId()).reduce("", (a, b) -> a + " " + "'" + b + "'") + '}' + ", outgoing={" + getOutgoing().stream().map(a -> a.getId()).reduce("", (a, b) -> a + " " + "'" + b + "'") + '}' + '}';
    }
}
