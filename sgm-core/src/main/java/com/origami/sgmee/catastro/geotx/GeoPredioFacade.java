/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 * Contiene los metodos transacionales y de consulta sobre los poligonos de los
 * predios.
 *
 * @author Fernando
 */
@Singleton
@ApplicationScoped
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class GeoPredioFacade extends GeoServiceMaster {

    public void deletePolygons(List<String> codigos) {
        requireTxGis();
        StringBuilder sb = new StringBuilder();
        sb.append(" DELETE FROM " + this.ptm.getTablename() + " WHERE codigo IN ( ");
        int i = 0;
        for (String cc1 : codigos) {
            i++;
            if (i > 1) {
                sb.append(", ");
            }
            sb.append(" E'").append(cc1).append("' ");
        }
        sb.append(" ); ");
        SQLQuery sql1 = sessGis().createSQLQuery(sb.toString());
        sql1.executeUpdate();
    }

    public void deleteByCodigo(String codigo) {
        requireTxGis();
        StringBuilder sb = new StringBuilder();
        sb.append(" DELETE FROM " + this.ptm.getTablename() + " WHERE codigo = :cod1 ; ");
        SQLQuery sql1 = sessGis().createSQLQuery(sb.toString());
        sql1.setString("cod1", codigo);
        sql1.executeUpdate();
    }

    public PolygonData find_byCodigo(String codigo) {
        SQLQuery sql1 = sessGis()
                .createSQLQuery("SELECT gid, codigo, round(CAST(ST_Area(geom) AS numeric), 2) AS area, habilitado"
                        + " FROM " + this.ptm.getTablename() + " WHERE habilitado = true AND codigo = :codigo ORDER BY gid DESC");
        sql1.setString("codigo", codigo);
        sql1.setMaxResults(1);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        return (PolygonData) sql1.uniqueResult();
    }

    public List<PolygonData> find_byCodigoModificados(String codigoMz, String usuario) {
        SQLQuery sql1 = sessGis().createSQLQuery(QueryGeo.checkPolygonUpdateByUserTask);
//                .createSQLQuery("SELECT gid, codigo, round(CAST(ST_Area(geom) AS numeric), 2) AS area, habilitado"
//                        + " FROM " + this.ptm.getTransTableName()
//                        + " WHERE habilitado = true AND codigo LIKE :codigo AND (external_edit LIKE '%in%' OR external_edit LIKE '%up%') AND "
//                        + " usuario = :usuario ORDER BY gid DESC");
        
        sql1.setString("codigo", "%".concat(codigoMz).concat("%"));
//        sql1.setString("external_edit", "up");
        sql1.setString("usuario", usuario);
        sql1.setMaxResults(1);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        return sql1.list();
    }

    public void actualizarEstadoPredioTx(List<PolygonData> poligonosModif) {
        requireTxGis();
        for (PolygonData polygonData : poligonosModif) {
            SQLQuery q1 = sessGis().createSQLQuery("SELECT geodata.\"actualizarModificados\"( :cod ) ;");
            q1.setString("cod", polygonData.getCodigo());
//            q1.executeUpdate();
            this.sessGis().flush();
        }

    }

}
