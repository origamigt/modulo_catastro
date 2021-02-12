package com.origami.sgmee.catastro.geotx;

import javax.inject.Inject;

public abstract class BpmServiceMaster extends GeoServiceMaster {

    @Inject
    protected GeoProcesosService geoServ;
    @Inject
    protected PredioTableMetadata predioTM;
    @Inject
    protected GeoValidations valid;

}
