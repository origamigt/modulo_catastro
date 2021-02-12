package com.origami.sgmee.catastro.geotx;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.hibernate.SQLQuery;

import com.origami.sgm.events.EliminacionPredioPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;

/**
 * Contiene los metodos necesarios para la eliminacion de un predio.
 *
 * @author Angel Navarro
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class EliminacionServ extends GeoServiceMaster {

    @Inject
    protected GeoProcesosService geoServ;

    public Boolean checkPoligonoEliminado(String codigo) {
        SQLQuery q1 = sessGis().createSQLQuery("SELECT codigo FROM geodata.predios_tx WHERE codigo = :cod ");
        q1.setString("cod", codigo);
        List<Object[]> result = q1.list();
        if (result.isEmpty()) {
            return true;
        } else {
            throw new GeoProcesosException("No se ha eliminado el polígono.");
        }
    }

    public void eliminarPoligono(@Observes EliminacionPredioPost elimEvt) {
        requireTxGis();
        String cod = elimEvt.getCodPredio();
        geoServ.deshabilitarPredio(cod);
    }

}
