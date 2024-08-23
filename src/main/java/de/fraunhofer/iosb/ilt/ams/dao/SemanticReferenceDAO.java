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
package de.fraunhofer.iosb.ilt.ams.dao;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.spring.dao.RDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.dao.support.sparql.NamedSparqlSupplier;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SemanticReferenceDAO extends RDF4JCRUDDao<SemanticReference, SemanticReference, IRI> {

    public static final GraphPatternNotTriples descriptionPattern =
            GraphPatterns.optional(
                    SemanticReference.SEMANTIC_REFERENCE_ID.has(
                            RDFS.COMMENT, SemanticReference.SEMANTIC_REFERENCE_DESCRIPTION));

    public static final GraphPatternNotTriples labelPattern =
            GraphPatterns.optional(
                    SemanticReference.SEMANTIC_REFERENCE_ID.has(
                            RDFS.LABEL, SemanticReference.SEMANTIC_REFERENCE_LABEL));
    public static final GraphPatternNotTriples sourceIdPattern =
            GraphPatterns.union(
                    SemanticReference.SEMANTIC_REFERENCE_ID.has(
                            AMS.externalIdentifier,
                            SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI),
                    SemanticReference.SEMANTIC_REFERENCE_ID.has(
                            AMS.identifier, SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI));

    @Autowired ObjectRdf4jRepository repo;

    public SemanticReferenceDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(SemanticReference.SEMANTIC_REFERENCE_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected SemanticReference mapSolution(BindingSet querySolution) {
        SemanticReference semanticReference = new SemanticReference();
        mapSemanticReference(querySolution, semanticReference);
        return semanticReference;
    }

    @Override
    protected String getReadQuery() {
        return getSemanticReferenceSelectQuery(null)
                .from(repo.getGraphNameForQuery())
                .getQueryString();
    }

    public SemanticReference getFromId(IRI iri) {
        List<SemanticReference> list =
                this.list().stream()
                        .filter(semref -> semref.getId().equals(iri))
                        .distinct()
                        .collect(Collectors.toList());
        if (list.size() != 1) {
            // TODO
            System.err.println("No semantic reference found");
            return null;
        }
        return list.get(0);
    }

    public static void mapSemanticReference(
            BindingSet querySolution, SemanticReference semanticReference) {
        semanticReference.setId(
                QueryResultUtils.getIRI(querySolution, SemanticReference.SEMANTIC_REFERENCE_ID));
        var label =
                QueryResultUtils.getValueMaybe(
                        querySolution, SemanticReference.SEMANTIC_REFERENCE_LABEL);
        if (label != null) {
            semanticReference.setLabel(label.stringValue());
            if (label.isLiteral() && ((Literal) label).getLanguage().isPresent()) {
                semanticReference.setLabelLanguageCode(((Literal) label).getLanguage().get());
            }
        }
        var description =
                QueryResultUtils.getValueMaybe(
                        querySolution, SemanticReference.SEMANTIC_REFERENCE_DESCRIPTION);
        if (description != null) {
            semanticReference.setDescription(description.stringValue());
            if (description.isLiteral() && ((Literal) description).getLanguage().isPresent()) {
                semanticReference.setDescriptionLanguageCode(
                        ((Literal) description).getLanguage().get());
            }
        }
        semanticReference.setSourceUri(
                URI.create(
                        QueryResultUtils.getString(
                                querySolution, SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI)));
    }

    public static SelectQuery getSemanticReferenceSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                SemanticReference.SEMANTIC_REFERENCE_ID,
                                SemanticReference.SEMANTIC_REFERENCE_LABEL,
                                SemanticReference.SEMANTIC_REFERENCE_DESCRIPTION,
                                SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI)
                        .where(
                                SemanticReference.SEMANTIC_REFERENCE_ID
                                        .isA(AMS.SemanticReference)
                                        .and(labelPattern)
                                        .and(sourceIdPattern)
                                        .and(descriptionPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            SemanticReference.SEMANTIC_REFERENCE_ID,
                            SemanticReference.SEMANTIC_REFERENCE_LABEL,
                            SemanticReference.SEMANTIC_REFERENCE_DESCRIPTION,
                            SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI)
                    .having(Expressions.equals(SemanticReference.SEMANTIC_REFERENCE_ID, iri(iri)));
        }
        return selectQuery;
    }

    @Override
    protected NamedSparqlSupplier getInsertSparql(SemanticReference semanticReference) {
        return NamedSparqlSupplier.of(
                KEY_PREFIX_INSERT,
                () ->
                        Queries.INSERT(
                                        SemanticReference.SEMANTIC_REFERENCE_ID
                                                .isA(iri(AMS.SemanticReference))
                                                // TODO URI
                                                .andHas(
                                                        iri(AMS.identifier),
                                                        SemanticReference
                                                                .SEMANTIC_REFERENCE_SOURCE_URI)
                                                .andHas(
                                                        iri(RDFS.COMMENT),
                                                        SemanticReference
                                                                .SEMANTIC_REFERENCE_DESCRIPTION)
                                                .andHas(
                                                        iri(RDFS.LABEL),
                                                        SemanticReference.SEMANTIC_REFERENCE_LABEL))
                                .getQueryString());
    }

    @Override
    protected void populateBindingsForUpdate(
            MutableBindings bindingsBuilder, SemanticReference semanticReference) {
        populateSemanticReferenceForUpdate(bindingsBuilder, semanticReference, true);
    }

    public static void populateSemanticReferenceForUpdate(
            MutableBindings bindingsBuilder,
            SemanticReference semanticReference,
            boolean isUpdate) {
        if (!isUpdate) {
            bindingsBuilder.add(SemanticReference.SEMANTIC_REFERENCE_ID, semanticReference.getId());
        }
        bindingsBuilder.addMaybe(
                SemanticReference.SEMANTIC_REFERENCE_SOURCE_URI,
                semanticReference.getSourceUri().toString());
        if (semanticReference.getLabel() != null
                && semanticReference.getLabelLanguageCode() != null) {
            var label =
                    Values.literal(
                            semanticReference.getLabel(), semanticReference.getLabelLanguageCode());
            bindingsBuilder.add(SemanticReference.SEMANTIC_REFERENCE_LABEL, label);
        }
        if (semanticReference.getDescription() != null
                && semanticReference.getDescriptionLanguageCode() != null) {
            var description =
                    Values.literal(
                            semanticReference.getDescription(),
                            semanticReference.getDescriptionLanguageCode());
            bindingsBuilder.add(SemanticReference.SEMANTIC_REFERENCE_DESCRIPTION, description);
        }
    }

    @Override
    protected IRI getInputId(SemanticReference semanticReference) {
        if (semanticReference.getId() == null) {
            return getRdf4JTemplate().getNewUUID();
        }
        return semanticReference.getId();
    }
}
