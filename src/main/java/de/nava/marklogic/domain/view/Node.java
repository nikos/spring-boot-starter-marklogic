package de.nava.marklogic.domain.view;

/**
 * @author Geert Josten
 */
public class Node {

    public final String id;
    public final String type;
    public final String label;
    public final Boolean top;

    public Node(String id, String type, String label, Boolean top) {
        this.id = id;
        this.type = type;
        this.label = label;
        this.top = top;
    }

}
