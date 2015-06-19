package de.nava.marklogic.service;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.admin.QueryOptionsManager;
import com.marklogic.client.admin.ServerConfigurationManager;
import com.marklogic.client.document.BinaryDocumentManager;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.document.TextDocumentManager;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.query.QueryManager;
import de.nava.marklogic.utils.extension.SPARQLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide access to connections via the MarkLogic API based on user accounts.
 *
 * @author Niko Schmuck
 */
@Service
public class MarkLogicConnections {

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicConnections.class);

    @Value("${marklogic.host:localhost}")
    private String host;

    @Value("${marklogic.port:8040}")
    private int port;

    @Value("${marklogic.contentDbName:demo-content}")
    private String contentDbName;

    @Value("${search.pagelength:10}")
    private int defaultPageLength;

    private ConcurrentHashMap<String, DatabaseClient> clients = new ConcurrentHashMap<>();

    /**
     * Authenticate the given user with MarkLogic database, returns false
     * if unsuccessful since user has no permissions to access the database.
     */
    public synchronized boolean auth(String username, String password) {
        logger.info("Setting up connection to MarkLogic server {}:{} for user {} ...", host, port, username);
        try {
            DatabaseClient client = DatabaseClientFactory.newClient(host, port, username, password,
                    DatabaseClientFactory.Authentication.DIGEST);
            // ~~
            testQuery(client);
            logger.info("Successfully logged in {}", username);
            clients.putIfAbsent(username, client);
            return true;
        } catch (Exception e) {
            logger.warn("Unable to login user {}: {}", username, e.getMessage());
            return false;
        }
    }

    /**
     * Provokes an exception in case a user is not authorized to access a test document.
     */
    private void testQuery(DatabaseClient client) {
        JSONDocumentManager jsonDocMgr = client.newJSONDocumentManager();
        jsonDocMgr.exists("/profiles/4711.json");  // TODO: we might want to retrieve a user profile
    }

    public String getConnectionInfo() {
        return String.format("%s:%d db=%s", host, port, contentDbName);
    }

    /**
     * Throws an IllegalArgumentException in case no database client can be found
     * for the given username.
     */
    public DatabaseClient getDatabaseClient(String username) {
        DatabaseClient dbClient = clients.get(username);
        Assert.notNull(dbClient, "No database client for '" + username + "' found, better auth first");
        return dbClient;
    }

    public boolean isConnected(String username) {
        return clients.containsKey(username);
    }

    public synchronized void release(String username) {
        clients.remove(username);
    }

    // ~~

    public ServerConfigurationManager getServerConfigManager(String username) {
        return getDatabaseClient(username).newServerConfigManager();
    }

    @Cacheable("mlConnections")
    public QueryManager getQueryManager(String username) {
        QueryManager queryManager = getDatabaseClient(username).newQueryManager();
        queryManager.setPageLength(defaultPageLength);
        return queryManager;
    }

    public QueryOptionsManager getQueryOptionManager(String username) {
        return getServerConfigManager(username).newQueryOptionsManager();
    }

    public XMLDocumentManager getXMLDocumentManager(String username) {
        return getDatabaseClient(username).newXMLDocumentManager();
    }

    public JSONDocumentManager getJSONDocumentManager(String username) {
        return getDatabaseClient(username).newJSONDocumentManager();
    }

    public SPARQLManager getSPARQLManager(String username) {
        return new SPARQLManager(getDatabaseClient(username));
    }

    public TextDocumentManager getTextDocumentManager(String username) {
        return getDatabaseClient(username).newTextDocumentManager();
    }

    public BinaryDocumentManager getBinaryDocumentManager(String username) {
        return getDatabaseClient(username).newBinaryDocumentManager();
    }

}
