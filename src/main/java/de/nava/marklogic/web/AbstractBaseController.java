package de.nava.marklogic.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * @author Niko Schmuck
 */
@Controller
public abstract class AbstractBaseController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${marklogic.adminUsername}")
    protected String defaultUser;

}
