package de.fraunhofer.iosb.ilt.ams.dao;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import de.fraunhofer.iosb.ilt.ams.AMS;
import de.fraunhofer.iosb.ilt.ams.model.Location;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.spring.dao.RDF4JCRUDDao;
import org.eclipse.rdf4j.spring.dao.support.bindingsBuilder.MutableBindings;
import org.eclipse.rdf4j.spring.support.RDF4JTemplate;
import org.eclipse.rdf4j.spring.util.QueryResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocationDAO extends RDF4JCRUDDao<Location, Location, IRI> {

    public static final GraphPatternNotTriples locationLatitudePattern =
            GraphPatterns.optional(
                    Location.LOCATION_ID.has(iri(AMS.latitude), Location.LOCATION_LATITUDE));

    public static final GraphPatternNotTriples locationLongitudePattern =
            GraphPatterns.optional(
                    Location.LOCATION_ID.has(iri(AMS.longitude), Location.LOCATION_LONGITUDE));
    public static final GraphPatternNotTriples locationStreetPattern =
            GraphPatterns.optional(
                    Location.LOCATION_ID.has(iri(AMS.street), Location.LOCATION_STREET));
    public static final GraphPatternNotTriples locationStreetNumberPattern =
            GraphPatterns.optional(
                    Location.LOCATION_ID.has(
                            iri(AMS.streetNumber), Location.LOCATION_STREET_NUMBER));
    public static final GraphPatternNotTriples locationZipPattern =
            GraphPatterns.optional(
                    Location.LOCATION_ID.has(iri(AMS.zipcode), Location.LOCATION_ZIP));
    public static final GraphPatternNotTriples locationCityPattern =
            GraphPatterns.optional(Location.LOCATION_ID.has(iri(AMS.city), Location.LOCATION_CITY));
    public static final GraphPatternNotTriples locationCountryPattern =
            GraphPatterns.optional(
                    Location.LOCATION_ID.has(iri(AMS.country), Location.LOCATION_COUNTRY));

    @Autowired ObjectRdf4jRepository repo;

    public LocationDAO(RDF4JTemplate rdf4JTemplate) {
        super(rdf4JTemplate);
    }

    @Override
    protected void populateIdBindings(MutableBindings bindingsBuilder, IRI iri) {
        bindingsBuilder.add(Location.LOCATION_ID, iri);
    }

    @Override
    protected NamedSparqlSupplierPreparer prepareNamedSparqlSuppliers(
            NamedSparqlSupplierPreparer preparer) {
        return null;
    }

    @Override
    protected String getReadQuery() {
        return getLocationSelectQuery(null).from(repo.getGraphNameForQuery()).getQueryString();
    }

    @Override
    protected Location mapSolution(BindingSet querySolution) {
        Location location = new Location();
        mapLocation(querySolution, location);
        return location;
    }

    public static SelectQuery getLocationSelectQuery(String iri) {
        SelectQuery selectQuery =
                Queries.SELECT(
                                Location.LOCATION_ID,
                                Location.LOCATION_LATITUDE,
                                Location.LOCATION_LONGITUDE,
                                Location.LOCATION_STREET,
                                Location.LOCATION_STREET_NUMBER,
                                Location.LOCATION_ZIP,
                                Location.LOCATION_CITY,
                                Location.LOCATION_COUNTRY)
                        .where(
                                Location.LOCATION_ID
                                        .isA(iri(AMS.Location))
                                        .and(locationLatitudePattern)
                                        .and(locationLongitudePattern)
                                        .and(locationStreetPattern)
                                        .and(locationStreetPattern)
                                        .and(locationStreetNumberPattern)
                                        .and(locationZipPattern)
                                        .and(locationCityPattern)
                                        .and(locationCountryPattern));
        if (iri != null) {
            return selectQuery
                    .groupBy(
                            Location.LOCATION_ID,
                            Location.LOCATION_LATITUDE,
                            Location.LOCATION_LONGITUDE,
                            Location.LOCATION_STREET,
                            Location.LOCATION_STREET_NUMBER,
                            Location.LOCATION_ZIP,
                            Location.LOCATION_CITY,
                            Location.LOCATION_COUNTRY)
                    .having(Expressions.equals(Location.LOCATION_ID, iri(iri)));
        }
        return selectQuery;
    }

    public static void mapLocation(BindingSet querySolution, Location location) {
        location.setId(QueryResultUtils.getIRI(querySolution, Location.LOCATION_ID));
        location.setLatitude(
                QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_LATITUDE));
        location.setLongitude(
                QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_LONGITUDE));
        location.setStreet(
                QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_STREET));
        location.setStreetNumber(
                QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_STREET_NUMBER));
        location.setZip(QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_ZIP));
        location.setCity(QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_CITY));
        location.setCountry(
                QueryResultUtils.getStringMaybe(querySolution, Location.LOCATION_COUNTRY));
    }
}
