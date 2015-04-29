package de.nava.marklogic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Mapping between business value and color code.
 *
 * Note: alternative solution would be to inject MessageSource into CooccurrencesSeries,
 *       but requires runtime weaving with AspectJ.
 *
 * @author Niko Schmuck
 */
@Service
public class ColorMappingService {

    @Autowired
    private MessageSource messages;

    public String getColor(String key) {
        String msgKey = "color." + key;
        String msgVal = messages.getMessage(msgKey, new Object[]{}, Locale.ENGLISH);
        // intentionally return null as default to let Highchart decide the right color scheme
        return msgKey.equals(msgVal) ? null : msgVal;
    }

}
