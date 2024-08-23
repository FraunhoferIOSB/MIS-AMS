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

import de.fraunhofer.iosb.ilt.ams.api.keycloakutil.KeycloakGrantedAuthoritiesConverter;
import de.fraunhofer.iosb.ilt.ams.api.keycloakutil.KeycloakJwtAuthenticationConverter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtSecurityConfiguration {

    /**
     * Configures a decoder with the specified validators (validation key fetched from JWKS endpoint)
     *
     * @param validators validators for the given key
     * @param properties key properties (provides JWK location)
     * @return the decoder bean
     */
    @Bean
    JwtDecoder jwtDecoder(
            List<OAuth2TokenValidator<Jwt>> validators, OAuth2ResourceServerProperties properties) {

        NimbusJwtDecoder jwtDecoder =
                NimbusJwtDecoder //
                        .withJwkSetUri(properties.getJwt().getJwkSetUri()) //
                        .jwsAlgorithms(
                                algs ->
                                        algs.addAll(
                                                Set.of(
                                                        SignatureAlgorithm.RS256,
                                                        SignatureAlgorithm.ES256))) //
                        .build();

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));

        return jwtDecoder;
    }

    /**
     * Configures the token validator. Specifies two additional validation constraints:
     * <p>
     * * Timestamp on the token is still valid
     * * The issuer is the expected entity
     *
     * @param properties JWT resource specification
     * @return token validator
     */
    @Bean
    OAuth2TokenValidator<Jwt> defaultTokenValidator(OAuth2ResourceServerProperties properties) {

        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new JwtIssuerValidator(properties.getJwt().getIssuerUri()));

        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    @Bean
    KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter(
            Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter) {
        return new KeycloakJwtAuthenticationConverter(authoritiesConverter);
    }

    @Bean
    Converter<Jwt, Collection<GrantedAuthority>> keycloakGrantedAuthoritiesConverter(
            GrantedAuthoritiesMapper authoritiesMapper,
            KeycloakDataServiceProperties keycloakDataServiceProperties) {
        return new KeycloakGrantedAuthoritiesConverter(
                keycloakDataServiceProperties.getJwt().getClientId(), authoritiesMapper);
    }

    @Bean
    GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {

        SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
        mapper.setConvertToUpperCase(true);
        return mapper;
    }
}
