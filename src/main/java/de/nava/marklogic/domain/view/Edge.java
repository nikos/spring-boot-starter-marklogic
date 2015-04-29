package de.nava.marklogic.domain.view;

/**
 * @author Geert Josten
 */
public class Edge extends Node {

    public final String from;
    public final String to;

    public Edge(String id, String label, String from, String to) {
        super(id, null, label, false);
        this.from = from;
        this.to = to;
    }

}
