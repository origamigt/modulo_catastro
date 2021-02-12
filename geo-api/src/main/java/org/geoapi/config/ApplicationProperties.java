package org.geoapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {

    @Value("${geo-api.predioLayer}")
    private String predioLayer;
    @Value("${geo-api.wfsUrl}")
    private String wfsUrl;
    @Value("${geo-api.wmsUrl}")
    private String wmsUrl;
    @Value("${geo-api.claveAttrName}")
    private String claveAttrName;
    @Value("${geo-api.wfsVersion}")
    private String wfsVersion;
    @Value("${geo-api.wmsVersion}")
    private String wmsVersion;
    @Value("${geo-api.envelopeAdd}")
    private Double envelopeAdd;
    @Value("${geo-api.croquisLayer}")
    private String croquisLayer;
    @Value("${geo-api.srid}")
    private String srid;
    @Value("${geo-api.envFilter}")
    private String envFilter;
    @Value("${geo-api.predioLayerColindantes}")
    private String predioLayerColindantes;
    @Value("${geo-api.croquisLayerPh}")
    private String croquisLayerPh;

    public String getPredioLayer() {
        return predioLayer;
    }

    public void setPredioLayer(String predioLayer) {
        this.predioLayer = predioLayer;
    }

    public String getWfsUrl() {
        return wfsUrl;
    }

    public void setWfsUrl(String wfsUrl) {
        this.wfsUrl = wfsUrl;
    }

    public String getWmsUrl() {
        return wmsUrl;
    }

    public void setWmsUrl(String wmsUrl) {
        this.wmsUrl = wmsUrl;
    }

    public String getClaveAttrName() {
        return claveAttrName;
    }

    public void setClaveAttrName(String claveAttrName) {
        this.claveAttrName = claveAttrName;
    }

    public String getWfsVersion() {
        return wfsVersion;
    }

    public void setWfsVersion(String wfsVersion) {
        this.wfsVersion = wfsVersion;
    }

    public String getWmsVersion() {
        return wmsVersion;
    }

    public void setWmsVersion(String wmsVersion) {
        this.wmsVersion = wmsVersion;
    }

    public Double getEnvelopeAdd() {
        return envelopeAdd;
    }

    public void setEnvelopeAdd(Double envelopeAdd) {
        this.envelopeAdd = envelopeAdd;
    }

    public String getCroquisLayer() {
        return croquisLayer;
    }

    public void setCroquisLayer(String croquisLayer) {
        this.croquisLayer = croquisLayer;
    }

    public String getSrid() {
        return srid;
    }

    public void setSrid(String srid) {
        this.srid = srid;
    }

    public String getEnvFilter() {
        return envFilter;
    }

    public void setEnvFilter(String envFilter) {
        this.envFilter = envFilter;
    }

    public String getPredioLayerColindantes() {
        return predioLayerColindantes;
    }

    public void setPredioLayerColindantes(String predioLayerColindantes) {
        this.predioLayerColindantes = predioLayerColindantes;
    }

    public String getCroquisLayerPh() {
        return croquisLayerPh;
    }

    public void setCroquisLayerPh(String croquisLayerPh) {
        this.croquisLayerPh = croquisLayerPh;
    }

}
