package de.nava.marklogic.domain.view;

import java.util.Collection;

/**
 * Hold Graph data in a representation which can be easily
 * consumed by a client for producing navigation charts.
 *
 * @author Geert Josten
 */
public class GraphData {

    public final Collection<Node> nodes;
    public final Collection<Edge> edges;

    public GraphData(Collection<Node> nodes, Collection<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

}
