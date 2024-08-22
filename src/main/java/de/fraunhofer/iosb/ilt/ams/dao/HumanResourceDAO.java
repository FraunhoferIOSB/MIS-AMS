package de.fraunhofer.iosb.ilt.ams.dao;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;

public class HumanResourceDAO extends ProductionResourceDAO {
    public HumanResourceDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        // TODO document why this method is empty
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        return super.getReadQuery();
    }
}
