package de.nava.marklogic.domain.view;

/**
 * @author Niko Schmuck
 */
public class Occurrence1D implements Comparable<Occurrence1D> {

    public final String key;
    public final Long count;

    public Occurrence1D(String key, Long count) {
        this.key = key;
        this.count = count;
    }

    @Override
    public int compareTo(Occurrence1D o) {
        return this.key.compareTo(o.key);
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", key, count);
    }

}
