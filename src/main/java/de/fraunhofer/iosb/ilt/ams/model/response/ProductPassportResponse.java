package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.ProductPassport;

public class ProductPassportResponse extends Response {
    ProductPassport productPassport;

    public ProductPassport getProductPassport() {
        return productPassport;
    }

    public void setProductPassport(ProductPassport productPassport) {
        this.productPassport = productPassport;
    }
}
