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

import java.net.URI;
import java.util.Objects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class SemanticReference {
    private IRI id;
    private URI sourceUri;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;

    public static final Variable SEMANTIC_REFERENCE_ID = SparqlBuilder.var("semantic_reference_id");
    public static final Variable SEMANTIC_REFERENCE_SOURCE_URI =
            SparqlBuilder.var("semantic_reference_source_id");
    public static final Variable SEMANTIC_REFERENCE_LABEL =
            SparqlBuilder.var("semantic_reference_label");
    public static final Variable SEMANTIC_REFERENCE_DESCRIPTION =
            SparqlBuilder.var("semantic_reference_description");

    public IRI getId() {
        return id;
    }

    public void setId(IRI id) {
        this.id = id;
    }

    public URI getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemanticReference that = (SemanticReference) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
