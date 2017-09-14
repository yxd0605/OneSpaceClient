package com.eli.oneos.model.oneos;

/**
 * Created by gaoyun@eli-tech.com on 2016/4/20.
 */
public class OneOSInfo {
    private String version = null;
    private String model = null;
    private boolean needsUp = false;
    private String build = null;
    private String product = null;

    public OneOSInfo(String version, String model, boolean needsUp, String product, String build) {
        this.version = version;
        this.model = model;
        this.needsUp = needsUp;
        this.product = product;
        this.build = build;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public boolean isNeedsUp() {
        return needsUp;
    }

    public void setNeedsUp(boolean needsUp) {
        this.needsUp = needsUp;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }
}
