package de.fraunhofer.iosb.ilt.ams.model;

import java.util.Objects;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;

public class Location {

    public static Variable LOCATION_ID = SparqlBuilder.var("location_id");
    public static Variable LOCATION_LATITUDE = SparqlBuilder.var("location_latitude");
    public static Variable LOCATION_LONGITUDE = SparqlBuilder.var("location_longitude");
    public static Variable LOCATION_STREET = SparqlBuilder.var("location_street");
    public static Variable LOCATION_STREET_NUMBER = SparqlBuilder.var("location_street_number");
    public static Variable LOCATION_ZIP = SparqlBuilder.var("location_zip");
    public static Variable LOCATION_CITY = SparqlBuilder.var("location_city");
    public static Variable LOCATION_COUNTRY = SparqlBuilder.var("location_country");

    private IRI id;
    private String latitude;
    private String longitude;
    private String street;
    private String streetNumber;
    private String zip;
    private String city;
    private String country;

    public IRI getId() {
        return id;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public String getStreet() {
        return street;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setId(IRI id) {
        this.id = id;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(getId(), location.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
