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

public class ProductApplication {

    public static final Variable PRODUCT_APP_ID = SparqlBuilder.var("product_app_id");
    public static final Variable PRODUCT_APP_SOURCE_ID = SparqlBuilder.var("product_app_source_id");
    public static final Variable PRODUCT_APP_PRODUCT = SparqlBuilder.var("product_app_product");
    public static final Variable PRODUCT_APP_QUANTITY = SparqlBuilder.var("product_app_quantity");
    public static final Variable PRODUCT_APP_PROPERTY = SparqlBuilder.var("product_app_property");

    private IRI id;
    private String sourceId;
    private Product product;
    private Property quantity;
    private Set<Property> properties = new HashSet<>();

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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Property getQuantity() {
        return quantity;
    }

    public void setQuantity(Property quantity) {
        this.quantity = quantity;
    }

    public Set<Property> getProperties() {
        return properties;
    }

    public boolean addProperty(Property property) {
        return this.properties.add(property);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductApplication that = (ProductApplication) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
