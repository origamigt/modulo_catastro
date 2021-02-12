package com.origami.sgmee.catastro.geotx;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.hibernate.SQLQuery;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;

/**
 * Permite realizar la validacion de los predios que intersectan entre si.
 *
 * @author Angel Navarro
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class GeoValidationsHelper extends GeoServiceMaster {

    public void testPredioIntersects(String codigo1) {
        String sql1 = "SELECT sl1.gid, sl1.codigo FROM " + this.ptm.getTablename() + " sl1, " + this.ptm.getTransTableName() + " ptx2 "
                + " WHERE sl1.codigo <> :codigo AND sl1.habilitado = true AND ptx2.codigo = :codigo2 "
                + " AND ST_IsEmpty( ST_Intersection(sl1.geom, ptx2.geom) ) = false ";
        SQLQuery q1 = sessGis().createSQLQuery(sql1);
        q1.setString("codigo", codigo1);
        q1.setString("codigo2", codigo1);
        List<Object[]> rl = q1.list();
        if (!rl.isEmpty()) {
            throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente.");
        }
    }

}
