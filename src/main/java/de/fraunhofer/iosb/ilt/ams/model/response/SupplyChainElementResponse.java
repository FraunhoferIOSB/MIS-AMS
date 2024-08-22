package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.SupplyChainElement;

public class SupplyChainElementResponse extends Response {
    private SupplyChainElement supplyChainElement;

    public SupplyChainElement getSupplyChainElement() {
        return supplyChainElement;
    }

    public void setSupplyChainElement(SupplyChainElement supplyChainElement) {
        this.supplyChainElement = supplyChainElement;
    }
}
