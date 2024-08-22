package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("datafetcherSecurity")
public class DatafetcherSecurity {
    public static final String BACKSLASH_REGEX = "\\\\";
    public static final String QUOTATION_REGEX = "\"";
    public static final String BRACKETS_REGEX = "[\\[\\]]";
    public static final String SLASH_REGEX = "/";
    public static final String EMPTY_STRING = "";
    public static final String GROUP_CLAIM = "group";
    public static final String SCOPE_CLAIM = "scope";
    public static final String REALM_ACCESS_CLAIM = "realm_access";
    public static final String SUPPLIER_KNOWLEDGE_ADMIN = "SupplierKnowledgeAdmin";
    public static final String COMMA = ",";
    public static final String SPACE = " ";

    private String groupName;
    private boolean canRead;

    @Value("${sfw-iosb.graph.group.scope}")
    private String scope;

    @Value("${sfw-iosb.security.rbac.roles.admin}")
    private String[] adminPermissions;

    @Value("${sfw-iosb.security.rbac.roles.user}")
    private String[] userPermissions;

    @Value("${sfw-iosb.security.rbac.admin-string}")
    private String adminString;

    @Value("${sfw-iosb.security.rbac.user-string}")
    private String userString;

    public String getGroupName() {
        return groupName;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public boolean hasPermission(Authentication authentication, String method) {
        this.setGroupName(authentication);
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            if (adminString.equals(auth.getAuthority())) {
                for (String permission : adminPermissions) {
                    if (permission.equals(method)) {
                        return true;
                    }
                }
            } else if (auth.getAuthority().equals(userString)) {
                for (String permission : userPermissions) {
                    if (permission.equals(method)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setGroupName(Authentication authentication) {
        var scopes =
                ((Jwt) authentication.getCredentials())
                        .getClaim(SCOPE_CLAIM)
                        .toString()
                        .split(SPACE);
        this.canReadAllGraphs(authentication);
        String[] nonNormalGroups =
                ((Jwt) authentication.getCredentials())
                        .getClaim(GROUP_CLAIM)
                        .toString()
                        .split(COMMA);
        String[] groups = new String[nonNormalGroups.length];
        for (int i = 0; i < nonNormalGroups.length; i++) {
            // Normalize the group name.
            String group =
                    nonNormalGroups[i]
                            .replaceAll(QUOTATION_REGEX, EMPTY_STRING)
                            .replaceAll(BRACKETS_REGEX, EMPTY_STRING)
                            .replaceAll(BACKSLASH_REGEX, EMPTY_STRING)
                            .replaceFirst(SLASH_REGEX, EMPTY_STRING);
            groups[i] = group;
        }
        for (String sc : scopes) {
            if (sc.equals(this.scope) && groups.length > 1) {
                throw new AccessDeniedException("Member of too many groups for this service.");
            }
        }

        groupName = groups[0];
    }

    public void canReadAllGraphs(Authentication authentication) {
        var realmAccess =
                ((Jwt) authentication.getCredentials())
                        .getClaimAsMap(REALM_ACCESS_CLAIM)
                        .get("roles")
                        .toString();
        realmAccess = realmAccess.substring(1, realmAccess.length() - 1);
        var rolesArray = realmAccess.split(",");

        for (String role : rolesArray) {
            if (role.substring(1, role.length() - 1).equals(SUPPLIER_KNOWLEDGE_ADMIN)) {
                canRead = true;
            }
        }
    }
}
