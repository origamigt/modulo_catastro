package com.origami.sgmee.catastro.geotx;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptors;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import com.origami.sgm.events.IntegracionParcelariaPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.PolygonData;

/**
 * Contine los metodos transacionales para el proceso de integracion parcelaria.
 *
 * @author Angel Navarro
 */
@Singleton
@ApplicationScoped
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class IntegracionParcelariaServ extends BpmServiceMaster {

    public void checkValidPolygons(List<String> codigos) {
        if (!tempFac.checkAdjacentPolygons(tempFac.getPolygons(codigos))) {
            throw new GeoProcesosException("Los polígonos no son adyacentes.");
        }
    }

    /**
     * Trae los polígonos en edicion
     *
     * @param codigos
     * @return
     */
    public List<PolygonData> detectPoligonos(List<String> codigos) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT ptx1.codigo, ptx1.gid, round(CAST(ST_Area(ptx1.geom) AS numeric), 2) AS area, ptx1.habilitado  "
                + "FROM geodata.predios_tx ptx1 ");
        sb.append(" WHERE ptx1.codigo IN ( ");
        int i = 0;
        for (String cc : codigos) {
            i++;
            if (i > 1) {
                sb.append(",");
            }
            sb.append(" E'").append(cc).append("' ");
        }
        sb.append(" ); ");
        SQLQuery q1 = sessGis().createSQLQuery(sb.toString());
        q1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> result = q1.list();

        return result;
    }

    public void asentarIntegracion(@Observes IntegracionParcelariaPost evt) {
        requireTxGis();
        predioFac.deletePolygons(evt.getCodigos());
        for (String cc : evt.getCodigos()) {
            geoServ.asentarPoligonoTx(tempFac.getPolygon(cc).getGid(), cc);
        }
    }

}
