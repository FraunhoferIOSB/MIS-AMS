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
package de.fraunhofer.iosb.ilt.ams.api.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "springkeycloak.auth")
public class KeycloakDataServiceProperties {

    private KeycloakJwtProperties jwt = new KeycloakJwtProperties();

    public KeycloakJwtProperties getJwt() {
        return jwt;
    }

    public void setJwt(KeycloakJwtProperties jwt) {
        this.jwt = jwt;
    }

    /**
     * Specifies JWT client ID, issuer URI and allowed audiences
     * for validation
     */
    public static class KeycloakJwtProperties {

        private String clientId;

        private String issuerUri;

        private List<String> allowedAudiences;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public List<String> getAllowedAudiences() {
            return allowedAudiences;
        }

        public void setAllowedAudiences(List<String> allowedAudiences) {
            this.allowedAudiences = allowedAudiences;
        }
    }
}
