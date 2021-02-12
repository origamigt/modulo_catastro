/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import util.JsfUti;
import util.Utils;

/**
 * Contiene los metodos de validacion y consulta de los poligonos de predios
 *
 * @author Fernando
 */
@Singleton
@ApplicationScoped
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class GeoPredioTempFacade extends GeoServiceMaster {

    protected String polygonSqlSelect = "gid, codigo, round(CAST(ST_Area(geom) AS numeric), 2) AS area, habilitado, numeracion";

    public List<PolygonData> getPolygons(String codigoPredial) {
        SQLQuery sql1 = sessGis().createSQLQuery("SELECT " + polygonSqlSelect
                + " FROM " + ptm.getTransTableName() + " WHERE codigo = :codigo ");
        sql1.setString("codigo", codigoPredial);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> polygons = sql1.list();
        return polygons;
    }

    public List<BloqueGeoData> getPolygonsBloque(String codigoPredial) {
        SQLQuery sql1 = sessGis().createSQLQuery("SELECT " + polygonSqlSelect
                + " FROM " + ptm.getTransTableName() + " WHERE codigo = :codigo  ");
        sql1.setString("codigo", codigoPredial);
        sql1.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> polygons = sql1.list();
        return polygons;
    }

    public PolygonData getPolygon(String codigoPredial) {
        SQLQuery sql1 = sessGis().createSQLQuery("SELECT " + polygonSqlSelect
                + " FROM " + ptm.getTransTableName() + " WHERE codigo = :codigo ");
        sql1.setString("codigo", codigoPredial);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> polygons = sql1.list();
        if (polygons == null) {
            JsfUti.messageError(null, "Error en Poligono", "N se encontró polígono con ese código catastral, "
                    + codigoPredial);
            throw new GeoProcesosException("No se encontró polígono con ese código catastral, en tabla pre-transaccional.");
        }
        if (polygons.size() > 1) {
            JsfUti.messageError(null, "Error en Poligono", "Se encontró más de un polígono con ese código catastral, "
                    + "en tabla pre-transaccional. Indicar al tecnico para que realize la correccion.");
            throw new GeoProcesosException("Se encontró más de un polígono con ese código catastral, en tabla pre-transaccional.");
        }
        if (polygons.size() == 0) {
            JsfUti.messageError(null, "Error en Poligono", "N se encontró polígono con ese código catastral, "
                    + codigoPredial);
            throw new GeoProcesosException("No se encontró polígono con ese código catastral, en tabla pre-transaccional.");
        }
        return polygons.get(0);
    }

    public PolygonData getPolygon(String codigo, Short numeracion) {
        SQLQuery sql1 = sessGis().createSQLQuery("SELECT " + polygonSqlSelect
                + " FROM " + ptm.getTransTableName() + " WHERE codigo = :codigo AND numeracion = :num  "
                + "AND (manzana IS NULL OR manzana = :manzana OR manzana = 0)");
        sql1.setString("codigo", codigo);
        sql1.setShort("num", numeracion);
        sql1.setInteger("manzana", Integer.valueOf(codigo.substring(10, 13)));
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> polygons = sql1.list();
        if (Utils.isEmpty(polygons)) {
            throw new GeoProcesosException("No se encontro código catastral, en tabla pre-transaccional. Clave " + codigo + " numeracion " + numeracion);
        }
        if (polygons.size() > 1) {
            throw new GeoProcesosException("Se encontró más de un polígono con ese código catastral, en tabla pre-transaccional.");
        }
        return polygons.get(0);
    }

    public PolygonData getPolygonMz(String codigo, Short numeracion, Integer mz) {
        SQLQuery sql1 = sessGis().createSQLQuery("SELECT " + polygonSqlSelect
                + " FROM " + ptm.getTransTableName() + " WHERE codigo = :codigo AND numeracion = :num AND manzana = :mz");
        sql1.setString("codigo", codigo);
        sql1.setShort("num", numeracion);
        sql1.setInteger("mz", mz);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> polygons = sql1.list();
        if (polygons.size() > 1) {
            System.out.println("Clave repetida " + codigo + " Numeracion " + numeracion + " Mz " + mz);
            throw new GeoProcesosException("Se encontró más de un polígono con el codigo." + codigo + " Numeracion " + numeracion + " Mz " + mz);
        }
        if (Utils.isEmpty(polygons)) {
            return null;
        }
        return polygons.get(0);
    }

    public List<PolygonData> getPolygons(List<String> codigos) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT ptx1.codigo, ptx1.gid, round(CAST(ST_Area(ptx1.geom) AS numeric), 5) AS area, ptx1.habilitado "
                + " FROM " + ptm.getTransTableName() + " ptx1 ");
        sb.append(" WHERE ptx1.codigo IN ( ");
        int i = 0;
        for (String cc : codigos) {
            i++;
            if (i > 1) {
                sb.append(",");
            }
            sb.append(" E'").append(cc).append("' ");
        }
        sb.append(" ) AND ptx1.habilitado = true ; ");
        SQLQuery q1 = sessGis().createSQLQuery(sb.toString());
        q1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> result = q1.list();

        return result;
    }

    /**
     * Validacion de adyacencia poligonal
     *
     * @param polygon1
     * @param others
     * @return true si others son adyacentes a polygon1
     */
    public Boolean checkAdjacentPolygons(PolygonData polygon1, List<PolygonData> others) {
        // ST_Touches(geometry g1, geometry g2)
        // query: obtener conteo de los que no se tocan (NOT ST_Touches)
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT COUNT(pre1.gid) AS conteo ");
        sb.append(" FROM " + getTableName() + " tmp1, " + getTableName() + " pre1 ");
        sb.append(" WHERE tmp1.gid = :gid1 AND NOT ST_Touches(pre1.geom, tmp1.geom) AND pre1.gid IN ( ");
        int i = 1;
        for (PolygonData cp : others) {
            if (i > 1) {
                sb.append(", ");
            }
            sb.append(cp.getGid());
            i++;
        }
        sb.append(" ) ; ");

        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        sql.setInteger("gid1", polygon1.getGid());
        sql.addScalar("conteo", IntegerType.INSTANCE);
        Object[] result = (Object[]) sql.uniqueResult();
        Integer conteoNoTocan = (Integer) result[0];

        return conteoNoTocan.equals(Integer.valueOf(0)) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Validacion de adyacencia poligonal múltiple
     *
     * @param solares
     * @return TRUE si para todos los casos al menos otro polígono es adyacente
     */
    public Boolean checkAdjacentPolygons(List<PolygonData> solares) {
        // query: obtener conteo de los que no se tocan (NOT ST_Touches)
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT COUNT(pt1.gid) AS conteo ");
        sb.append(" FROM " + ptm.getTransTableName() + " pt1 ");
        sb.append(" WHERE pt1.gid IN ");
        checkAdjacentPolygons_genGidInList(sb, solares);
        sb.append(" AND NOT EXISTS( ");
        sb.append(" SELECT pt2.gid FROM " + ptm.getTransTableName() + " pt2 WHERE ST_Touches(pt1.geom, pt2.geom) AND pt2.gid <> pt1.gid "
                + " AND pt2.gid IN ");
        checkAdjacentPolygons_genGidInList(sb, solares);
        sb.append(" ) ; ");

        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        //sql.setInteger("gid1", polygon1.getGid());
        sql.addScalar("conteo", IntegerType.INSTANCE);
        //Object[] result = (Object[]) sql.uniqueResult();
        Integer conteoNoTocan = (Integer) sql.uniqueResult(); //result[0];

        return conteoNoTocan.equals(Integer.valueOf(0)) ? Boolean.TRUE : Boolean.FALSE;
    }

    private void checkAdjacentPolygons_genGidInList(StringBuilder sb, List<PolygonData> solares) {
        sb.append(" ( ");
        int i = 1;
        for (PolygonData cp : solares) {
            if (i > 1) {
                sb.append(", ");
            }
            sb.append(cp.getGid());
            i++;
        }
        sb.append(" ) ");
    }

    public String getTableName() {
        return ptm.getTransTableName();
    }

    public List<BloqueGeoData> localizarBloquesDentro(Integer gid) {
        List<BloqueGeoData> result = new LinkedList<>();
        SQLQuery q1 = sessGis().createSQLQuery(" SELECT " + bloqTmpFac.getSelectTemplate("tb1")
                + " FROM " + bloqTmpFac.getPtm().getTransBloque() + " tb1 INNER JOIN " + tempFac.getTableName() + " px1 "
                + " ON ST_Intersects(ST_Buffer(px1.geom, -2.5), tb1.geom) WHERE px1.gid = :gid AND tb1.piso = 4 "
                + " AND ST_Intersects(px1.geom, tb1.geom)");
        q1.setInteger("gid", gid);
        q1.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        result = q1.list();
        for (BloqueGeoData bg : result) {
            bg.setNiveles(this.bloqTmpFac.listByCodigoNiveles(bg.getCodigo(), bg.getNum()));
        }
        return result;
    }

    public void updateCodNum(BloqueGeoData bdata, String codigo, Short numero) {
        requireTxGis();
        SQLQuery q1 = sessGis().createSQLQuery("UPDATE " + ptm.getTransBloque() + " "
                + " SET blo_cod=:cod, num_bloq=:num "
                + " WHERE blo_cod=:cod2 AND num_bloq=:num2 ; ");
        q1.setString("cod", codigo);
        q1.setShort("num", numero);
        q1.setString("cod2", bdata.getCodigo());
        q1.setShort("num2", bdata.getNum());
        q1.executeUpdate();
        this.updateCodNumCtBloque(bdata, codigo, numero);
    }

    public void updateCodNumCtBloque(BloqueGeoData bdata, String codigo, Short numero) {
        requireTxGis();
        SQLQuery q1 = sessGis().createSQLQuery("UPDATE " + ptm.getGeoBloque() + " "
                + " SET blo_cod=:cod, num_bloq=:num "
                + " WHERE blo_cod=:cod2 AND num_bloq=:num2 ; ");
        q1.setString("cod", codigo);
        q1.setShort("num", numero);
        q1.setString("cod2", bdata.getCodigo());
        q1.setShort("num2", bdata.getNum());
        q1.executeUpdate();
    }

    public void asentarBloques_porCodigo(String codigo) {
        requireTxGis();
        SQLQuery q1 = sessGis().createSQLQuery("SELECT geodata.\"bloquesAsentarPorCod\"(:cod)");
        q1.setString("cod", codigo);
        String resp = (String) q1.uniqueResult();
    }

    /**
     * Realiza la busqueda de los colindantes con a localizacion en coordenas
     * Azimuth esta propiedad lo envie el la propiedad area.
     *
     * @param codigo Clave Catastral del predio a conocer los colindantes
     * @return Lista de {@link PolygonData} con los predios colindantes
     */
    public List<PolygonData> getColindantes(String codigo) {
        requireTxGis();
        StringBuilder query = new StringBuilder();
        query.append("SELECT ptemp.gid, ptemp.codigo, ROUND(CAST(degrees(ST_Azimuth(ST_Centroid(p.geom), ST_Centroid(ptemp.geom))) AS NUMERIC), 4) area")
                .append(" FROM ").append(this.ptm.getTransTableName()).append(" p ")
                .append(" INNER JOIN ").append(this.ptm.getTransTableName()).append(" ptemp ON st_intersects(p.geom, ptemp.geom)")
                .append(" WHERE p.codigo = :cod AND p.codigo <> ptemp.codigo ")
                .append(" ORDER BY area ");
        SQLQuery q1 = sessGis().createSQLQuery(query.toString());
        q1.setString("cod", codigo);
        return q1.list();
    }
}
