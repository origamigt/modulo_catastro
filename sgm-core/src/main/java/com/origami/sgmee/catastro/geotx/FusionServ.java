package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.events.BloqueFusionData;
import com.origami.sgm.events.FusionPrediosPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.util.Arrays;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import org.hibernate.SQLQuery;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;

/**
 * Contiene los metodos para la validacion de los poligonos que se van fusiones
 * y la ves los metodos para procesos los predios fusionados.
 *
 * @author Fernando
 *
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class FusionServ extends BpmServiceMaster {

    @Inject
    private Entitymanager manager;
    
    public void checkValidPolygons(List<String> codigos) {
        if (!tempFac.checkAdjacentPolygons(tempFac.getPolygons(codigos))) {
            throw new GeoProcesosException("Los polígonos no son adyacentes");
        }
    }

    public String detectCodigoFusionado(List<String> codigos) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT ptx1.codigo, ptx1.gid  FROM " + this.ptm.getTransTableName() + " ptx1 ");
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
        q1.addScalar("codigo", StringType.INSTANCE);
        q1.addScalar("gid", IntegerType.INSTANCE);
        List<Object[]> result = q1.list();
        // si hay mas de un resultado, exception
        if (result.size() > 1) {
            throw new GeoProcesosException("Los códigos no están fusionados en un polígono.");
        }
        if (result.isEmpty()) {
            throw new GeoProcesosException("No se encontró polígono alguno.");
        }
        return (String) result.get(0)[0];
    }

    public void fusionar(@Observes FusionPrediosPost fpevent) {
        requireTxGis();
        System.out.println(fpevent.getCodPrediosFusion() + " Procesando Unificacion de predio clave catastral: " + fpevent.getCodPredioFinal());
        this.manager.executeNativeQuery("SELECT geodata.\"procesarPrediosFusionados\"(?, ?)", new Object[]{fpevent.getCodPredioFinal(), ""});
        geoServ.deshabilitarPredios(fpevent.getCodPrediosFusion());
    }

    private void trasladarBloques(List<BloqueFusionData> bloquesData, String codigoNew) {
        if (bloquesData != null || !bloquesData.isEmpty()) {
            requireTxGis();
            for (BloqueFusionData cbd : bloquesData) {
                bloqFac.updateClaveYNum(cbd.getCodigoOld(), cbd.getNumOld(), codigoNew, cbd.getNumNew());
                bloqTmpFac.updateClaveYNum(cbd.getCodigoOld(), cbd.getNumOld(), codigoNew, cbd.getNumNew());
            }
        }
    }

}
