package de.fraunhofer.iosb.ilt.ams.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHelper {

    private static final Logger logger = LoggerFactory.getLogger("query");
    private static final Logger mutationLogger = LoggerFactory.getLogger("mutation");

    public static void logQuery(String methodName, String graphName) {
        logger.trace(String.format(MESSAGE.LOG_MESSAGE, graphName.substring(4), methodName));
    }

    public static void logMutation(String methodName, String graphName) {
        mutationLogger.trace(String.format(MESSAGE.LOG_MESSAGE, graphName, methodName));
    }
}
