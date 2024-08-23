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

public abstract class ProductionResource {
    public static Variable PRODUCTION_RESOURCE_ID = SparqlBuilder.var("production_resource_id");
    public static Variable PRODUCTION_RESOURCE_SOURCE_ID =
            SparqlBuilder.var("production_resource_source_id");
    public static Variable PRODUCTION_RESOURCE_LABEL =
            SparqlBuilder.var("production_resource_label");
    public static Variable PRODUCTION_RESOURCE_DESCRIPTION =
            SparqlBuilder.var("production_resource_description");
    public static Variable PRODUCTION_RESOURCE_PROVIDING_PROCESS =
            SparqlBuilder.var("production_resource_provided_process");
    public static Variable PRODUCTION_RESOURCE_USING_PROCESS =
            SparqlBuilder.var("production_resource_using_process");
    public static Variable PRODUCTION_RESOURCE_PROVIDED_CAPABILITY =
            SparqlBuilder.var("production_resource_provided_capability");

    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private Set<Process> providedProcesses = new HashSet<>();
    private Set<Process> usingProcesses = new HashSet<>();
    private Set<Capability> providedCapabilities = new HashSet<>();

    protected ProductionResource() {}

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

    public Set<Process> getProvidedProcesses() {
        return providedProcesses;
    }

    public boolean addProvidedProcess(Process providedProcess) {
        return this.providedProcesses.add(providedProcess);
    }

    public Set<Process> getUsingProcesses() {
        return usingProcesses;
    }

    public boolean addUsingProcess(Process usingProcess) {
        return this.usingProcesses.add(usingProcess);
    }

    public Set<Capability> getProvidedCapabilities() {
        return providedCapabilities;
    }

    public boolean addProvidedCapability(Capability providedCapability) {
        return this.providedCapabilities.add(providedCapability);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductionResource that = (ProductionResource) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
