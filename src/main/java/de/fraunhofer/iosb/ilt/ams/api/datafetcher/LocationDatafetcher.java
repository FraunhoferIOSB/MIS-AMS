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
package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.Location;
import de.fraunhofer.iosb.ilt.ams.model.filter.LocationFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.LocationInput;
import de.fraunhofer.iosb.ilt.ams.model.response.LocationResponse;
import de.fraunhofer.iosb.ilt.ams.repository.LocationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class LocationDatafetcher {

    @Autowired LocationRepository locationRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'location')")
    @DgsQuery
    public List<Location> location(@InputArgument LocationFilter filter) {
        return this.locationRepository.getLocations(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'location')")
    @DgsMutation
    public LocationResponse createLocation(@InputArgument LocationInput location) {
        return this.locationRepository.createLocation(location);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'location')")
    @DgsMutation
    public LocationResponse updateLocation(
            @InputArgument String locationId, @InputArgument LocationInput location) {
        return this.locationRepository.updateLocation(locationId, location);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'location')")
    @DgsMutation
    public LocationResponse deleteLocation(@InputArgument String locationId) {
        return this.locationRepository.deleteLocation(locationId);
    }
}
