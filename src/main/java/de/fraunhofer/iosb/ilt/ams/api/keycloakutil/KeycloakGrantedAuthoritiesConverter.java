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

import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.CollectionUtils;

public class KeycloakGrantedAuthoritiesConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Converter<Jwt, Collection<GrantedAuthority>>
            JWT_SCOPE_GRANTED_AUTHORITIES_CONVERTER = new JwtGrantedAuthoritiesConverter();

    private final String clientId;

    private final GrantedAuthoritiesMapper authoritiesMapper;

    public KeycloakGrantedAuthoritiesConverter(
            String clientId, GrantedAuthoritiesMapper authoritiesMapper) {
        this.clientId = clientId;
        this.authoritiesMapper = authoritiesMapper;
    }

    @Override
    public Collection<GrantedAuthority> convert(@NotNull Jwt jwt) {

        Collection<GrantedAuthority> authorities =
                mapKeycloakRolesToAuthorities( //
                        getRealmRolesFrom(jwt), //
                        getClientRolesFrom(jwt, clientId) //
                        );

        Collection<GrantedAuthority> scopeAuthorities =
                JWT_SCOPE_GRANTED_AUTHORITIES_CONVERTER.convert(jwt);
        if (!CollectionUtils.isEmpty(scopeAuthorities)) {
            authorities.addAll(scopeAuthorities);
        }

        return authorities;
    }

    protected Collection<GrantedAuthority> mapKeycloakRolesToAuthorities(
            Set<String> realmRoles, Set<String> clientRoles) {

        List<GrantedAuthority> combinedAuthorities = new ArrayList<>();

        combinedAuthorities.addAll(
                authoritiesMapper.mapAuthorities(
                        realmRoles.stream() //
                                .map(SimpleGrantedAuthority::new) //
                                .collect(Collectors.toList())));

        combinedAuthorities.addAll(
                authoritiesMapper.mapAuthorities(
                        clientRoles.stream() //
                                .map(SimpleGrantedAuthority::new) //
                                .collect(Collectors.toList())));

        return combinedAuthorities;
    }

    protected Set<String> getRealmRolesFrom(Jwt jwt) {

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (CollectionUtils.isEmpty(realmAccess)) {
            return Collections.emptySet();
        }

        @SuppressWarnings("unchecked")
        Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");
        if (CollectionUtils.isEmpty(realmRoles)) {
            return Collections.emptySet();
        }

        return realmRoles.stream().map(this::normalizeRole).collect(Collectors.toSet());
    }

    protected Set<String> getClientRolesFrom(Jwt jwt, String clientId) {

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");

        if (CollectionUtils.isEmpty(resourceAccess)) {
            return Collections.emptySet();
        }

        @SuppressWarnings("unchecked")
        Map<String, List<String>> clientAccess =
                (Map<String, List<String>>) resourceAccess.get(clientId);
        if (CollectionUtils.isEmpty(clientAccess)) {
            return Collections.emptySet();
        }

        List<String> clientRoles = clientAccess.get("roles");
        if (CollectionUtils.isEmpty(clientRoles)) {
            return Collections.emptySet();
        }

        return clientRoles.stream().map(this::normalizeRole).collect(Collectors.toSet());
    }

    private String normalizeRole(String role) {
        return role.replace('-', '_');
    }
}
