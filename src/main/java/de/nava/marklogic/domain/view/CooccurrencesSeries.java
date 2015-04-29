package de.nava.marklogic.domain.view;

import de.nava.marklogic.service.ColorMappingService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hold co-occurrence data in a representation which can be easily
 * consumed by a client for producing stacked column/bar charts.
 *
 * @author Niko Schmuck
 */
public class CooccurrencesSeries {

    public final Set<String> valueNames;

    /**
     * In the same order as the valueNames
     */
    public final Collection<DataPoints> series;

    /**
     * Returns the consolidated values, in case a color mapping is given
     * it will be used to retrieve a color value for the given value key name.
     */
    public CooccurrencesSeries(Map<String, Set<Occurrence1D>> occurrences, Set<String> valueNames,
                               ColorMappingService colorMappingService) {
        this.series = new ArrayList<>();

        // reduce results to [name, [values]]
        for (Map.Entry<String, Set<Occurrence1D>> entry : occurrences.entrySet()) {
            List<Long> values = entry.getValue().stream().map(occ -> occ.count).collect(Collectors.toList());
            series.add(new DataPoints(entry.getKey(),
                    colorMappingService != null ? colorMappingService.getColor(entry.getKey()) : null,
                    values));
        }
        this.valueNames = valueNames;
    }

}
