package de.nava.marklogic.domain.view;

/**
 * @author Niko Schmuck
 */
public class Link {

    public final String title;
    public final String hovertext;
    public final String href;

    public Link(String title, String href, String hovertext) {
        this.title = title;
        this.href = href;
        this.hovertext = hovertext;
    }

}
