package de.nava.marklogic.service.health;

import de.nava.marklogic.service.MarkLogicConnections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom Spring Boot actuate health (see section 40.3.2 in Spring Boot Reference)
 * for validating if the middle-tier is able to communicate with MarkLogic
 * database server.
 *
 * @author Niko Schmuck
 * @since 01.04.2015
 */
@Component
public class MarkLogicConnectionHealth implements HealthIndicator {

    @Autowired
    protected MarkLogicConnections connections;

    @Value("${marklogic.adminUsername}")
    private String adminUsername;

    @Value("${marklogic.adminPassword}")
    private String adminPassword;

    @Override
    public Health health() {
        Health.Builder mlHealth;
        try {
            long start = System.currentTimeMillis();
            boolean authenticated = connections.auth(adminUsername, adminPassword);
            long elapsed = System.currentTimeMillis() - start;
            if (!authenticated) {
                mlHealth = Health.down()
                        .withDetail("error", "Unable to connect to MarkLogic: " + connections.getConnectionInfo());
            } else {
                mlHealth = Health.up();
            }
            mlHealth = mlHealth.withDetail("authenticate-test", authenticated);
            mlHealth = mlHealth.withDetail("db-connect-time", elapsed);
        } catch (Exception e) {
            mlHealth = Health.down(e);
        } finally {
            connections.release(adminUsername);
        }
        return mlHealth.build();
    }

}
