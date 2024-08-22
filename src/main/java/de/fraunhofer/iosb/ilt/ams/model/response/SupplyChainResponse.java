package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.SupplyChain;

public class SupplyChainResponse extends Response {
    private SupplyChain supplyChain;

    public SupplyChain getSupplyChain() {
        return supplyChain;
    }

    public void setSupplyChain(SupplyChain supplyChain) {
        this.supplyChain = supplyChain;
    }
}
