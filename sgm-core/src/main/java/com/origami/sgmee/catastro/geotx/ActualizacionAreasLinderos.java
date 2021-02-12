package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.events.ActAreasLindPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptors;

/**
 * Provee los metodos de consulta para la integracion con la base grafica para
 * el proceso de Actualizacion de areas y linderos.
 *
 * @author Angel Navarro
 */
@Singleton
@Lock(LockType.READ)
@ApplicationScoped
@Interceptors(HibernateEjbInterceptor.class)
public class ActualizacionAreasLinderos extends BpmServiceMaster {

    public List<PolygonData> getPoligonosActualizados(List<String> codigos) {
        this.valid.checkPredioPolygonConflictos(codigos);
        return tempFac.getPolygons(codigos);
    }

    public List<BloqueGeoData> getPoligonosBloqueNuevo(String codigo) {
        return tempFac.getPolygonsBloque(codigo);
    }

    public void asentar(@Observes ActAreasLindPost evt) {
        requireTx();
        predioFac.deletePolygons(evt.getCodigos());
        for (String cc : evt.getCodigos()) {
            geoServ.asentarPoligonoTx(tempFac.getPolygon(cc).getGid(), cc);
            this.tempFac.asentarBloques_porCodigo(cc);
            this.tempFac.getColindantes(cc);
        }
    }

    public List<BloqueGeoData> getBloquesPredio(GeoPrediosDivididos geoPredio) {
        return this.tempFac.localizarBloquesDentro(geoPredio.getGid());
    }

}
