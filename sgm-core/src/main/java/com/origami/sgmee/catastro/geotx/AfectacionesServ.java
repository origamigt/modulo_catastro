package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.entities.models.Tipo;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptors;

import com.origami.sgm.events.AfectacionPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import java.util.List;
import java.util.Map;
import util.Utils;

/**
 * Contiene los metodos de integracion con la base grafica para el proceso de
 * Afectacion
 *
 * @author Angel Navarro
 */
@Singleton
@Lock(LockType.READ)
@ApplicationScoped
@Interceptors(HibernateEjbInterceptor.class)
public class AfectacionesServ extends BpmServiceMaster {

    public PolygonData getPoligonoAfectado(String codigo) {
        PolygonData pd = tempFac.getPolygon(codigo);
        if (pd == null) {
            throw new GeoProcesosException("No se encontró polígono para afectación.");
        }
        return pd;
    }

    /**
     * Retorna el poligono afectado y la lista de los nuevos poligonos con el
     * campo numeracion
     *
     * @param codigo Clave catastral del poligono afectado
     * @return
     */
    public List<PolygonData> getPoligonosAfectacion(String codigo) {
        List<PolygonData> pd = tempFac.getPolygons(codigo);
        if (Utils.isEmpty(pd)) {
            throw new GeoProcesosException("No se encontró polígono para afectación.");
        }
        return pd;
    }

    /**
     * Obtiene el evento cuando es lanzado por la tarea y envia a eliminar el
     * predio afectado y acentar los predios resultantes de la afectacion
     *
     * @param evt
     */
    public void asentar(@Observes AfectacionPost evt) {
        requireTx();
        predioFac.deleteByCodigo(evt.getCodigo());
        // acentamos el predio afectado
        geoServ.asentarPoligonoTx(tempFac.getPolygon(evt.getCodigo(), Short.valueOf("0")).getGid(), evt.getCodigo());
        // removemos el predio afectado
        evt.getCodigos().remove(evt.getCodigo());
        short numeracion = 1;
        for (Map.Entry<String, Tipo> en : evt.getCodigos().entrySet()) {
            if (en.getValue().getId() == null) {
                geoServ.asentarPoligonoTx(tempFac.getPolygon(evt.getCodigo(), numeracion).getGid(), en.getKey(), en.getValue().getIdb(), evt.getTipo());
            } else {
                geoServ.asentarPoligonoTx(tempFac.getPolygonMz(evt.getCodigo(), numeracion, en.getValue().getId()).getGid(), en.getKey(), en.getValue().getIdb(), evt.getTipo());
            }
            numeracion++;
        }

    }

}
