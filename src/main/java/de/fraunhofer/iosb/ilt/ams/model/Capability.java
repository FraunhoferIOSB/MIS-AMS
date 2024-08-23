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
package de.fraunhofer.iosb.ilt.ams.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Capability {

    public static final Variable CAPABILITY_ID = SparqlBuilder.var("capability_id");
    public static final Variable CAPABILITY_SOURCE_ID = SparqlBuilder.var("capability_source_id");
    public static final Variable CAPABILITY_LABEL = SparqlBuilder.var("capability_label");
    public static final Variable CAPABILITY_DESCRIPTION =
            SparqlBuilder.var("capability_description");
    public static final Variable CAPABILITY_PROPERTY = SparqlBuilder.var("capability_property");
    public static final Variable CAPABILITY_PROCESS = SparqlBuilder.var("capability_process");
    public static final Variable CAPABILITY_CHILD_CAPABILITY =
            SparqlBuilder.var("capability_child_capability");
    public static final Variable CAPABILITY_PARENT_CAPABILITY =
            SparqlBuilder.var("capability_parent_capability");
    public static final Variable CAPABILITY_PRODUCTION_RESOURCE =
            SparqlBuilder.var("capability_production_resource");
    public static final Variable CAPABILITY_SEMANTIC_REFERENCE =
            SparqlBuilder.var("capability_semantic_reference");

    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private Set<Property> properties = new HashSet<>();
    private Set<Process> processes = new HashSet<>();
    private Set<Capability> childCapabilities = new HashSet<>();
    private Set<Capability> parentCapabilities = new HashSet<>();
    private Set<ProductionResource> productionResources = new HashSet<>();
    private Set<SemanticReference> semanticReferences = new HashSet<>();

    public IRI getId() {
        return id;
    }

    public void setId(IRI id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabelLanguageCode() {
        return labelLanguageCode;
    }

    public void setLabelLanguageCode(String labelLanguageCode) {
        this.labelLanguageCode = labelLanguageCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionLanguageCode() {
        return descriptionLanguageCode;
    }

    public void setDescriptionLanguageCode(String descriptionLanguageCode) {
        this.descriptionLanguageCode = descriptionLanguageCode;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }

    public Set<Process> getProcesses() {
        return processes;
    }

    public boolean addProcess(Process process) {
        return this.processes.add(process);
    }

    public Set<Capability> getChildCapabilities() {
        return childCapabilities;
    }

    public boolean addChildCapability(Capability childCapability) {
        return this.childCapabilities.add(childCapability);
    }

    public Set<Capability> getParentCapabilities() {
        return parentCapabilities;
    }

    public boolean addParentCapability(Capability parentCapability) {
        return this.parentCapabilities.add(parentCapability);
    }

    public Set<ProductionResource> getProductionResources() {
        return productionResources;
    }

    public boolean addProductionResource(ProductionResource productionResource) {
        return this.productionResources.add(productionResource);
    }

    public Set<SemanticReference> getSemanticReferences() {
        return semanticReferences;
    }

    public boolean addSemanticReference(SemanticReference semanticReference) {
        return this.semanticReferences.add(semanticReference);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability that = (Capability) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
