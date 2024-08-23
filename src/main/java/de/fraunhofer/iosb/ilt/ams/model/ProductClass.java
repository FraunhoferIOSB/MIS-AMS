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

public class ProductClass {

    public static final Variable PRODUCT_CLASS_ID = SparqlBuilder.var("product_class_id");
    public static final Variable PRODUCT_CLASS_SOURCE_ID =
            SparqlBuilder.var("product_class_source_id");
    public static final Variable PRODUCT_CLASS_LABEL = SparqlBuilder.var("product_class_label");
    public static final Variable PRODUCT_CLASS_DESCRIPTION =
            SparqlBuilder.var("product_class_description");
    public static final Variable PRODUCT_CLASS_PARENT_CLASSES =
            SparqlBuilder.var("product_class_parent_classes");
    public static final Variable PRODUCT_CLASS_SEMANTIC_REFERENCES =
            SparqlBuilder.var("product_class_semantic_references");
    public static final Variable PRODUCT_CLASS_CHILD_CLASSES =
            SparqlBuilder.var("product_class_child_classes");
    public static final Variable PRODUCT_CLASS_PRODUCTS =
            SparqlBuilder.var("product_class_products");

    private IRI id;
    private String sourceId;
    private String label;
    private String labelLanguageCode;
    private String description;
    private String descriptionLanguageCode;
    private Set<SemanticReference> semanticReferences = new HashSet<>();
    private Set<ProductClass> parentClasses = new HashSet<>();
    private Set<ProductClass> childClasses = new HashSet<>();
    private Set<Product> products = new HashSet<>();

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

    public Set<SemanticReference> getSemanticReferences() {
        return semanticReferences;
    }

    public boolean addSemanticReference(SemanticReference semanticReference) {
        return this.semanticReferences.add(semanticReference);
    }

    public Set<ProductClass> getParentClasses() {
        return parentClasses;
    }

    public boolean addParentClass(ProductClass productClass) {
        return this.parentClasses.add(productClass);
    }

    public Set<ProductClass> getChildClasses() {
        return childClasses;
    }

    public boolean addChildClass(ProductClass childClass) {
        return this.childClasses.add(childClass);
    }

    public Set<Product> getProducts() {
        return products;
    }

    public boolean addProduct(Product product) {
        return this.products.add(product);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductClass that = (ProductClass) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
