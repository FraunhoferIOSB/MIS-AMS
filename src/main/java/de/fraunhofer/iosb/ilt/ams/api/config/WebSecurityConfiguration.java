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

import de.fraunhofer.iosb.ilt.ams.api.keycloakutil.KeycloakJwtAuthenticationConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @Value("${management.endpoints.web.cors.allowed-origins}")
    private String[] allowedOrigins;

    public WebSecurityConfiguration(
            KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter,
            String[] allowedOrigins) {
        this.keycloakJwtAuthenticationConverter = keycloakJwtAuthenticationConverter;
        this.allowedOrigins = allowedOrigins;
    }

    /**
     * Gets the allowed origins of CORS requests.
     *
     * @return A String array of allowed request URLs.
     */
    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // declarative route configuration
                .antMatchers("/token", "/graphiql")
                .permitAll()
                .and()
                .cors(this::configureCors)
                .csrf()
                .disable()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter);
    }

    /**
     * Configures CORS to allow requests from origins defined in application.properties.
     *
     * @param cors mutable cors configuration
     */
    protected void configureCors(CorsConfigurer<HttpSecurity> cors) {

        UrlBasedCorsConfigurationSource defaultUrlBasedCorsConfigSource =
                new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();

        for (String origin : getAllowedOrigins()) {
            corsConfiguration.addAllowedOrigin(origin);
        }
        corsConfiguration.addAllowedMethod(HttpMethod.POST);
        defaultUrlBasedCorsConfigSource.registerCorsConfiguration("/**", corsConfiguration);

        cors.configurationSource(
                req -> {
                    CorsConfiguration config = new CorsConfiguration();

                    config =
                            config.combine(
                                    defaultUrlBasedCorsConfigSource.getCorsConfiguration(req));

                    // check if request Header "origin" is in white-list -> dynamically generate
                    // cors config

                    return config;
                });
    }
}
