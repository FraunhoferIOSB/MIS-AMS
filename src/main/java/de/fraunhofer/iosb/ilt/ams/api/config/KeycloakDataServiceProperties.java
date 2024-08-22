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
