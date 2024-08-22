package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.SemanticReference;

public class SemanticReferenceResponse extends Response {
    private SemanticReference semanticReference;

    public SemanticReference getSemanticReference() {
        return semanticReference;
    }

    public void setSemanticReference(SemanticReference semanticReference) {
        this.semanticReference = semanticReference;
    }
}
