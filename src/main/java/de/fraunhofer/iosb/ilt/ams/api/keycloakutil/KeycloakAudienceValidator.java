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
