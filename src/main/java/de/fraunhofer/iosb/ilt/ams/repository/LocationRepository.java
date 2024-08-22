package de.fraunhofer.iosb.ilt.ams.repository;

import de.fraunhofer.iosb.ilt.ams.dao.LocationDAO;
import de.fraunhofer.iosb.ilt.ams.model.Location;
import de.fraunhofer.iosb.ilt.ams.model.ObjectRdf4jRepository;
import de.fraunhofer.iosb.ilt.ams.model.filter.LocationFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.LocationInput;
import de.fraunhofer.iosb.ilt.ams.model.response.LocationResponse;
import de.fraunhofer.iosb.ilt.ams.utility.MESSAGE;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.util.Values;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LocationRepository {

    @Autowired LocationDAO locationDAO;

    @Autowired ObjectRdf4jRepository repo;

    public List<Location> getLocations(LocationFilter locationFilter) {
        repo.emptyProcessedIds();
        List<Location> locations =
                locationDAO.list().stream().distinct().collect(Collectors.toList());
        if (locationFilter != null) {
            if (locationFilter.getId() != null) {
                locations =
                        locations.stream()
                                .filter(l -> Values.iri(locationFilter.getId()).equals(l.getId()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getLatitude() != null) {
                locations =
                        locations.stream()
                                .filter(l -> locationFilter.getLatitude().equals(l.getLatitude()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getLongitude() != null) {
                locations =
                        locations.stream()
                                .filter(l -> locationFilter.getLongitude().equals(l.getLongitude()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getStreet() != null) {
                locations =
                        locations.stream()
                                .filter(l -> locationFilter.getStreet().equals(l.getStreet()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getStreetNumber() != null) {
                locations =
                        locations.stream()
                                .filter(
                                        l ->
                                                locationFilter
                                                        .getStreetNumber()
                                                        .equals(l.getStreetNumber()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getZip() != null) {
                locations =
                        locations.stream()
                                .filter(l -> locationFilter.getZip().equals(l.getZip()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getCity() != null) {
                locations =
                        locations.stream()
                                .filter(l -> locationFilter.getCity().equals(l.getCity()))
                                .collect(Collectors.toList());
            }

            if (locationFilter.getCountry() != null) {
                locations =
                        locations.stream()
                                .filter(l -> locationFilter.getCountry().equals(l.getCountry()))
                                .collect(Collectors.toList());
            }
        }
        return locations;
    }

    public LocationResponse createLocation(LocationInput locationInput) {
        LocationResponse locationResponse = new LocationResponse();

        locationResponse.setLocation(repo.createAndInsertLocation(locationInput));
        locationResponse.setCode(200);
        locationResponse.setMessage(MESSAGE.SUCCESS);
        locationResponse.setSuccess(true);
        return locationResponse;
    }

    public LocationResponse updateLocation(String locationId, LocationInput locationInput) {
        LocationResponse response = new LocationResponse();
        IRI iri = null;
        try {
            iri = Values.iri(locationId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        Location location = repo.getLocationById(iri);
        if (location == null) {
            response.setMessage(String.format(MESSAGE.ENTITY_NOT_FOUND, iri));
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setLocation(repo.updateLocation(iri, locationInput));
        response.setCode(200);
        response.setSuccess(true);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }

    public LocationResponse deleteLocation(String locationId) {
        LocationResponse response = new LocationResponse();
        IRI iri = null;
        try {
            iri = Values.iri(locationId);
        } catch (IllegalArgumentException iae) {
            response.setMessage(iae.getMessage());
            response.setSuccess(false);
            response.setCode(500);
            return response;
        }
        response.setSuccess(repo.deleteLocation(iri));
        response.setCode(200);
        response.setMessage(MESSAGE.SUCCESS);
        return response;
    }
}
