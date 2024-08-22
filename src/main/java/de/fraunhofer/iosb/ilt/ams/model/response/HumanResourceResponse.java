package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.HumanResource;

public class HumanResourceResponse extends Response {
    private HumanResource humanResource;

    public HumanResource getHumanResource() {
        return humanResource;
    }

    public void setHumanResource(HumanResource humanResource) {
        this.humanResource = humanResource;
    }
}
