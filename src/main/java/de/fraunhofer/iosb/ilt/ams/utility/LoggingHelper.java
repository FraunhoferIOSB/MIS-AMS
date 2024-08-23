/*
 * Copyright (c) 2024 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
