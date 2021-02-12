package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.events.CreacionPredioPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptors;
import util.JsfUti;

/**
 * Contiene los metodos para la integracion con la base grafica al momento de
 * realizar el proceso de Creacion de predio.
 *
 * @author Angel Navarro
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class CreacionServ extends BpmServiceMaster {

    public void agregarPredio(@Observes CreacionPredioPost creEvt) {
        requireTxGis();
        // el nuevo predio viene agregado en predio_tx; con el codigo, geom nuevos; habilitado=true
        PolygonData npdata = geoServ.findNewPolygon("#" + creEvt.getNumTramite());
        geoServ.asentarPoligonoTx(npdata.getGid(), creEvt.getCodPredio(), creEvt.getNumPredio(), creEvt.getTipo());
    }

    public PolygonData detectPoligono(String numTramite) {
        PolygonData pol = geoServ.findNewPolygon("#" + numTramite);
        if (pol == null) {
            JsfUti.messageError(null, "Error al completar tarea", "No se encontró el polígono.");
            throw new GeoProcesosException("No se encontró el polígono.");
        }

        // REALIZAMOS LAS VALIDACIONES DE LAS CLASE DE SUELO
        if (activarCalidadSueloRural) {
            if (pol.getTipo().toUpperCase().startsWith("RUR")) {
                geoServ.validarClasesSuelo(pol);
            }
        }

        return pol;
    }

}
