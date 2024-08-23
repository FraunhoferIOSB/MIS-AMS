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

public final class MESSAGE {
    private MESSAGE() throws Exception {
        throw new Exception("Private constructor");
    }

    public static final String LANGUAGE_ERROR = "Field %s also needs a language code.";
    public static final String ENTITY_NOT_FOUND =
            "Entity with id: %s not found."
                    + System.lineSeparator()
                    + "Possible reasons: "
                    + System.lineSeparator()
                    + "1. Entity with this id does not exist."
                    + System.lineSeparator()
                    + "2. Entity with this id is not of the claimed type.";
    public static final String DELETED = "Deleted %s";
    public static final String NOT_ENOUGH_ARGUMENTS =
            "The entity %s should either have a semantic reference "
                    + "or a combination of label and description";

    public static final String NOT_ENOUGH_ARGUMENTS_GENERIC =
            "This entity should either have a semantic reference "
                    + "or a combination of label and description";
    public static final String SUCCESS = "Success";

    public static final String ENTITY_NOT_CONTAINED = "Entity %s does not contain %s";

    public static final int LOOP_DETECTED = 205;

    public static final String LOOP_STRING = "Query %s";

    public static final String LOG_MESSAGE = "User from group %s called method %s.";
}
