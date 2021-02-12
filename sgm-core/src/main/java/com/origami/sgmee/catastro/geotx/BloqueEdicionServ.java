package com.origami.sgmee.catastro.geotx;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptors;

import com.origami.sgm.events.EdicionBloquesPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Metodos de integracion de bloques, No usada en nigun proceso.
 *
 * @author Angel Navarro
 */
@Singleton
@Lock(LockType.READ)
@ApplicationScoped
@Interceptors(HibernateEjbInterceptor.class)
public class BloqueEdicionServ extends BpmServiceMaster {

    public List<BloqueGeoData> polygonsData(String codigo) {
        List<BloqueGeoData> bloquesTemp = bloqTmpFac.detectTxList(codigo);
        if (bloquesTemp.isEmpty()) {
            throw new GeoProcesosException("No se encontraton bloques para tal predio.");
        }
        return bloquesTemp;
    }

    public void asentar(@Observes EdicionBloquesPost evt) {
        try {
            requireTx();
            bloqFac.delete(bloqTmpFac.detectEliminados(evt.getCodigo()));
            bloqFac.updateGeom(bloqTmpFac.detectEnModificacion(evt.getCodigo()));
            bloqFac.create(bloqTmpFac.detectNuevos(evt.getCodigo()));
        } catch (Exception ex) {
            Logger.getLogger(BloqueEdicionServ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
