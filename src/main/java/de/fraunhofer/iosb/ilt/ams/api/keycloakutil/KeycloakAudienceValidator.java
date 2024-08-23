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

import de.fraunhofer.iosb.ilt.ams.api.config.KeycloakDataServiceProperties;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
class KeycloakAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private final OAuth2Error ERROR_INVALID_AUDIENCE =
            new OAuth2Error("invalid_token", "Invalid audience", null);

    private final KeycloakDataServiceProperties keycloakDataServiceProperties;

    KeycloakAudienceValidator(KeycloakDataServiceProperties keycloakDataServiceProperties) {
        this.keycloakDataServiceProperties = keycloakDataServiceProperties;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {

        String authorizedParty = jwt.getClaimAsString("azp");

        if (!keycloakDataServiceProperties
                .getJwt()
                .getAllowedAudiences()
                .contains(authorizedParty)) {
            return OAuth2TokenValidatorResult.failure(ERROR_INVALID_AUDIENCE);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
