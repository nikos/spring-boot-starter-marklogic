package de.nava.marklogic.domain.view;

import java.util.List;

/**
 * @author Niko Schmuck
 */
public class DataPoints {

    public final String name;
    public final String color;
    public final List<Long> data;

    public DataPoints(String name, String color, List<Long> values) {
        this.name = name;
        this.color = color;
        this.data = values;
    }

}
