package com.origami.sgmee.catastro.geotx;

import java.util.List;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;

import org.hibernate.SQLQuery;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;

/**
 * Contiene los metodos para actualizar las geometrias en la capa final de
 * bloques, y ademas hereda los metodos de la clase
 * {@link GeoMasterBloqueFacade}
 *
 * @author Angel Navarro
 */
@Singleton
@Lock(LockType.READ)
@ApplicationScoped
@Interceptors(HibernateEjbInterceptor.class)
public class GeoBloqueFacade extends GeoMasterBloqueFacade {

    @Override
    public String getTableName() {
        return "geodata.ct_bloque_constructivo";
    }

    /**
     * Actualiza las geometrías de todos los pisos para cada bloque, desde capa
     * temporal
     *
     * @param bloques
     */
    public void updateGeom(List<BloqueGeoData> bloques) {
        requireTx();
        for (BloqueGeoData cb : bloques) {
            StringBuilder sb = new StringBuilder();
            sb.append(" WITH subquery AS ( SELECT gid, codigo, geom FROM " + bloqTmpFac.getTableName()
                    + " WHERE cod = :cod AND num = :num )");
            sb.append(" UPDATE " + getTableName() + " bl1 SET geom = subquery.geom ");
            sb.append(" FROM subquery ");
            sb.append(" WHERE bl1.codigo = subquery.codigo AND bl1.num = subquery.num AND bl1.piso = subquery.piso ; ");
            // hibernate sqlquery:
            SQLQuery sql = sessGis().createSQLQuery(sb.toString());
            sql.setString("cod", cb.getCodigo());
            sql.setShort("num", cb.getNum());
            sql.executeUpdate();
        }
    }

}
