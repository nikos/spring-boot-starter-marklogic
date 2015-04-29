package de.nava.marklogic.web;

import de.nava.marklogic.service.MarkLogicConnections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Niko Schmuck
 */
@Component
public class MarkLogicAuthInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MarkLogicAuthInterceptor.class);

    @Value("${marklogic.adminUsername}")
    protected String adminUsername;

    @Value("${marklogic.adminPassword}")
    protected String adminPassword;

    @Autowired
    protected MarkLogicConnections markLogicConnections;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        logger.debug("~~~~ Check if user is connected to MarkLogic for request: {}", request.getRequestURI());
        // TODO: should auth fail scenario better throw a specific RuntimeException to allow better error handling?
        return markLogicConnections.isConnected(adminUsername)
            || markLogicConnections.auth(adminUsername, adminPassword);
    }

}
