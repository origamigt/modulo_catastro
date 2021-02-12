package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.events.CesionPredioPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

/**
 * Contiene los metodos de integracion con la base grafica para el proceso de
 * cesion.
 *
 * @author Fernando
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class CesionPredioService extends GeoServiceMaster {

    @Inject
    protected GeoProcesosService procServ;

    public PolygonData getDataNuevoPolygon(String cod) {
        return getTempFac().getPolygon(cod);
    }

    public void ejecutarCesion(@Observes CesionPredioPost cesEvt) {
        requireTx();
        PolygonData pd = getDataNuevoPolygon(cesEvt.getCodigoPredial());
        procServ.deshabilitarPredio(cesEvt.getCodigoPredial());
        procServ.asentarPoligonoTx(pd.getGid(), cesEvt.getCodigoPredial());

    }

}
