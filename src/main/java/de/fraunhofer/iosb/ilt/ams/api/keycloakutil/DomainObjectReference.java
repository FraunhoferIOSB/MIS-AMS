package de.fraunhofer.iosb.ilt.ams.api.keycloakutil;

import java.util.Objects;

/**
 * Defines a single domain object by a type and name to look up
 */
public class DomainObjectReference {

    private final String type;

    private final String id;

    public DomainObjectReference(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "DomainObjectReference{" + "type='" + type + '\'' + ", id='" + id + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DomainObjectReference that = (DomainObjectReference) o;
        return Objects.equals(type, that.type) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }
}
