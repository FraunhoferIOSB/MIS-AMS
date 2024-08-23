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
package de.fraunhofer.iosb.ilt.ams.api.keycloakutil;

import java.io.Serializable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
class DefaultPermissionEvaluator implements PermissionEvaluator {

    // TODO: Implement a proper logger.
    @Override
    public boolean hasPermission(
            Authentication auth, Object targetDomainObject, Object permission) {
        System.out.println(
                String.format(
                        "check permission user={%s} target={%s} permission={%s}",
                        auth.getName(), targetDomainObject.toString(), permission.toString()));

        // TODO implement sophisticated permission check here

        return true;
    }

    @Override
    public boolean hasPermission(
            Authentication auth, Serializable targetId, String targetType, Object permission) {
        DomainObjectReference dor = new DomainObjectReference(targetType, targetId.toString());
        return hasPermission(auth, dor, permission);
    }
}
