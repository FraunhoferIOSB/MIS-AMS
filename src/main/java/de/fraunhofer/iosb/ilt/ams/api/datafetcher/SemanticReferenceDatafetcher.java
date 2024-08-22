package de.fraunhofer.iosb.ilt.ams.api.datafetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;
import de.fraunhofer.iosb.ilt.ams.model.filter.SemanticReferenceFilter;
import de.fraunhofer.iosb.ilt.ams.model.input.SemanticReferenceInput;
import de.fraunhofer.iosb.ilt.ams.model.response.SemanticReferenceResponse;
import de.fraunhofer.iosb.ilt.ams.repository.SemanticReferenceRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;

@DgsComponent
public class SemanticReferenceDatafetcher {

    @Autowired SemanticReferenceRepository semanticReferenceRepository;

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsQuery
    public List<SemanticReference> semanticReference(
            @InputArgument SemanticReferenceFilter filter) {
        return semanticReferenceRepository.getSemanticReferences(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsQuery
    public List<SemanticReference> getAllSemanticReferences(
            @InputArgument SemanticReferenceFilter filter) {
        return semanticReferenceRepository.getSemanticReferences(filter);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsMutation
    public SemanticReferenceResponse createSemanticReference(
            @InputArgument SemanticReferenceInput semanticReference) {
        return semanticReferenceRepository.createSemanticReference(semanticReference);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsMutation
    public SemanticReferenceResponse updateSemanticReference(
            @InputArgument String semanticReferenceId,
            @InputArgument SemanticReferenceInput semanticReference) {
        return semanticReferenceRepository.updateSemanticReference(
                semanticReferenceId, semanticReference);
    }

    @PreAuthorize("@datafetcherSecurity.hasPermission(authentication, 'semanticReference')")
    @DgsMutation
    public SemanticReferenceResponse deleteSemanticReference(
            @InputArgument String semanticReferenceId) {
        return semanticReferenceRepository.deleteSemanticReference(semanticReferenceId);
    }
}
