package de.nava.marklogic.domain.view;

/**
 * @author Niko Schmuck
 */
public class Coordinate {

    public final int x;
    public final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("[%d, %d]", x, y);
    }
}
