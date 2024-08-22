package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.Capability;

public class CapabilityResponse extends Response {
    private Capability capability;

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capability) {
        this.capability = capability;
    }
}
