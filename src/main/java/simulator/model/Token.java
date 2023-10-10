//Copyright 2022 Voyance Systems

package simulator.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Token implements Comparable<Token> {
	
	private static final Logger logger = LoggerFactory.getLogger(Token.class);
	
    private double timer;
    private FlowElement at;
    private int id;

    public Token(int id, FlowElement at, double timer) {
        this.timer = timer;
        this.at = at;
        this.id = id;
    }

    public double getTimer() {
        return timer;
    }

    public void setTimer(double timer) {
        this.timer = timer;
    }

    public FlowElement getAt() {
        return at;
    }

    public void setAt(FlowElement at) {
        this.at = at;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token t = (Token) o;
        return Double.compare(t.timer, timer) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(timer);
    }

    @Override
    public int compareTo(Token o) {
        return Double.compare(timer, o.getTimer());
    }

    @Override
    public String toString() {
        return "Token{" + "id='" + id + '\'' + ", at=" + at.getId() + ", timer=" + getTimer() + '}';
    }
}
