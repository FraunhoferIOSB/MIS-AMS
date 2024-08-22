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
