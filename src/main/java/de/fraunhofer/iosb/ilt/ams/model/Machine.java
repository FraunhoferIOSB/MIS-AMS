package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Machine extends ProductionResource {

    public static final Variable MACHINE_ID = SparqlBuilder.var("machine_id");
    public static final Variable MACHINE_PROPERTY = SparqlBuilder.var("machine_property");

    private Set<Property> machineProperties = new HashSet<>();

    public Set<Property> getMachineProperties() {
        return machineProperties;
    }

    public boolean addMachineProperty(Property machineProperty) {
        return this.machineProperties.add(machineProperty);
    }
}
