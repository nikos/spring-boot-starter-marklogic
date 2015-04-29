package de.nava.marklogic.domain.view;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author Niko Schmuck
 */
public class Occurrence2D {

    @JsonUnwrapped
    public final Coordinate coordinate;
    public final Long count;

    public Occurrence2D(Coordinate coordinate, Long count) {
        this.coordinate = coordinate;
        this.count = count;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", coordinate, count);
    }

}
