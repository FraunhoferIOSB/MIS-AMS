package de.fraunhofer.iosb.ilt.ams.model.response;

import de.fraunhofer.iosb.ilt.ams.model.ProductApplication;

public class ProductApplicationResponse extends Response {
    private ProductApplication productApplication;

    public ProductApplication getProductApplication() {
        return productApplication;
    }

    public void setProductApplication(ProductApplication productApplication) {
        this.productApplication = productApplication;
    }
}
