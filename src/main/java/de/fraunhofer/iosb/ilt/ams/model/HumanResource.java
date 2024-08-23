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
import java.util.Set;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class HumanResource extends ProductionResource {
    public static final Variable HUMAN_RESOURCE_ID = SparqlBuilder.var("human_resource_id");
    public static final Variable HUMAN_RESOURCE_CERTIFICATES =
            SparqlBuilder.var("human_resource_certificates");
    public static final Variable HUMAN_RESOURCE_PROPERTIES =
            SparqlBuilder.var("human_resource_properties");

    private Set<Property> certificates = new HashSet<>();
    private Set<Property> properties = new HashSet<>();

    public Set<Property> getCertificates() {
        return certificates;
    }

    public boolean addCertificate(Property certificate) {
        return this.certificates.add(certificate);
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }
}
