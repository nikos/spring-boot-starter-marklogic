package de.nava.marklogic.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nava.marklogic.utils.CustomObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Niko Schmuck
 */
@Configuration
public class MarkLogicConfiguration {

    @Bean
    public ObjectMapper mapper() {
        return new CustomObjectMapper();
    }

}
