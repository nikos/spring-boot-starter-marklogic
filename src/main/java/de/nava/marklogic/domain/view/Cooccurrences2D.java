package de.nava.marklogic.domain.view;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hold co-occurrence data in a representation which can be easily
 * consumed by a client for producing 2D charts (like highcharts bubbles).
 *
 * @author Niko Schmuck
 */
public class Cooccurrences2D {

    public final List<String> xCategories;

    public final List<String> yCategories;

    public final List<Occurrence2D> occurrences;

    /**
     * Assume: the given maps (x/yCategory) contain their name as key and an 'ID' as value,
     * actually the 'ID' is the sort order it was originally retrieved by the co-occurrency
     * call (and therefore might reflect the frequency order).
     */
    public Cooccurrences2D(final Map<String, Integer> xCategoryMap,
                           final Map<String, Integer> yCategoryMap,
                           final List<Occurrence2D> occurrences) {
        // Sort by value (this pre-condition for proper labeling subsequently)
        this.xCategories = sortByValue(xCategoryMap);
        this.yCategories = sortByValue(yCategoryMap);

        this.occurrences = occurrences;
    }

    private static List<String> sortByValue(Map<String, Integer> valuemap) {
        List<Map.Entry<String, Integer>> list = new LinkedList<>(valuemap.entrySet());

        Collections.sort(list, (o1, o2) -> o1.getValue().compareTo(o2.getValue()));

        return list.stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

}
