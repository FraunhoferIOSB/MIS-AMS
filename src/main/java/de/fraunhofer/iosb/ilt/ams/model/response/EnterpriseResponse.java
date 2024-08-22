package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.Enterprise;

public class EnterpriseResponse extends Response {
    private Enterprise enterprise;

    public Enterprise getEnterprise() {
        return enterprise;
    }

    public void setEnterprise(Enterprise enterprise) {
        this.enterprise = enterprise;
    }
}
