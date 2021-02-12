/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import org.hibernate.SQLQuery;
import org.hibernate.type.StandardBasicTypes;
import util.JsfUti;

/**
 * Realiza las validaciones sobre los poligonos
 *
 * @author Fernando
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class GeoValidations extends GeoServiceMaster {

    /**
     * No implementado
     *
     * @param codigos
     */
    public void checkPrediosJuntos(List<String> codigos) {

    }

    /**
     * Verifica que la <code>codigo1</code> y la <code>codigo2</code> sean
     * predios colindantes
     *
     * @param codigo1 Clave catastral del primer predio
     * @param codigo2 Clave catastral del segundo predio con el que se
     * verificara
     */
    public void checkPrediosJuntos(String codigo1, String codigo2) {
        /*
         Ejemplo:
            SELECT ST_Touches( (SELECT geom FROM " + this.ptm.getTransTableName() + " WHERE codigo = '100104040100401300000000'),
            (SELECT geom FROM " + this.ptm.getTransTableName() + " WHERE codigo = '100104040100401200000000') ) ;
         */
        String sql1 = "SELECT ST_Touches( (SELECT geom FROM " + this.ptm.getTransTableName() + " WHERE codigo = '" + codigo1 + "'),"
                + "(SELECT geom FROM " + this.ptm.getTransTableName() + " WHERE codigo = '" + codigo2 + "') ) AS touches";
        SQLQuery q1 = sessGis().createSQLQuery(sql1);
        q1.addScalar("touches", StandardBasicTypes.BOOLEAN);

        Boolean res = (Boolean) q1.uniqueResult();
        if (Boolean.TRUE.equals(res)) {
            throw new GeoProcesosException("Los predios no están anexos.");
        }
    }

    /**
     * Valida que no haya lineas crusadas entre los predios adyacentes
     *
     * @param codigo1 Clave catastral del predio a verificar la adyacencia
     */
    public void checkPredioPolygonConflictosInterseccion(String codigo1) {
        /*
        SELECT sl1.gid, sl1.codigo FROM " + this.ptm.getTablename() + " sl1
            WHERE sl1.codigo <> :codigo AND sl1.habilitado = true
              AND ST_IsEmpty( ST_Intersection( (SELECT ptx2.geom FROM " + this.ptm.getTransTableName() + " ptx2 WHERE ptx2.codigo = :codigo2 ),
               sl1.geom ) ) = false;*/
        String sql1 = "SELECT sl1.gid, sl1.codigo FROM " + this.ptm.getTransTableName() + " sl1 WHERE sl1.codigo <> :codigo AND sl1.habilitado = true "
                + " AND ST_IsEmpty( ST_Intersection( (SELECT ptx2.geom FROM " + this.ptm.getTransTableName() + " ptx2 "
                + "WHERE ptx2.codigo = :codigo2 AND ptx2.numeracion IS NOT NULL),"
                + " sl1.geom ) ) = false;";
        // OBTENEMOS LA PARTE DEL POLIGONO QUE INTERSECTA Y VERIFICAMOS EL AREA 
        //SI EL AREA ES MAYOR A CERO QUIERE DECIR QUE ESTA SOLAPADOS LOS POLIGONOS
        /*String sql1 = "SELECT s2.gid, s2.codigo, s2.numeracion, s2.manzana "
                + "FROM " + this.ptm.getTransTableName() + " s1 INNER JOIN " + this.ptm.getTransTableName() + " s2 ON ST_Intersects(s2.geom, s1.geom) "
                + "WHERE s1.codigo = :codigo2 AND s1.gid <> s2.gid AND ST_Area(ST_Intersection(s2.geom, s1.geom)) > 0";*/
        SQLQuery q1 = sessGis().createSQLQuery(sql1);
        q1.setString("codigo", codigo1);
        q1.setString("codigo2", codigo1);
        List<Object[]> rl = q1.list();
        if (!rl.isEmpty()) {
            throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[1]);
        }
    }

    /**
     * Valida que no haya lineas crusadas entre los predios adyacentes
     *
     * @param codigo1 codigo codigo catastral del predio matrix
     * @param numeracion numeracion del nuevo predio fraccionado
     * @param manzana manzana a la que pertenecera si la hubiera
     */
    public void checkPredioPolygonConflictosInterseccion(String codigo1, Short numeracion, Integer manzana) {
//        String sql1 = "SELECT sl1.gid, sl1.codigo FROM " + this.ptm.getTransTableName() + " sl1 WHERE sl1.codigo ~ :codigoSector AND sl1.habilitado = true "
//                + " AND ST_IsEmpty( ST_Intersection( (SELECT ptx2.geom FROM " + this.ptm.getTransTableName() + " ptx2 "
//                + "WHERE ptx2.codigo = :codigo2 AND ptx2.numeracion  = :numeracion "
//                + "ptx2.manzana = :manzana),"
//                + " sl1.geom ) ) = false;";
        String sql1 = "SELECT s2.gid, s2.codigo, s2.numeracion, s2.manzana "
                + "FROM " + this.ptm.getTransTableName() + " s1 INNER JOIN " + this.ptm.getTransTableName() + " s2 ON ST_Intersects(s2.geom, s1.geom) "
                + "WHERE s1.codigo = :codigo2 AND s1.numeracion  = :numeracion  "
                + "AND s1.manzana = :manzana "
                + " AND s1.gid <> s2.gid AND ST_Area(ST_Intersection(s2.geom, s1.geom)) > 0";
        SQLQuery q1 = sessGis().createSQLQuery(sql1);
//        q1.setString("codigoSector", codigo1.substring(0, 10));
        q1.setString("codigo2", codigo1);
        q1.setShort("numeracion", numeracion);
        q1.setInteger("manzana", manzana);
        List<Object[]> rl = q1.list();
        if (!rl.isEmpty()) {
            System.out.println("El polígono sobrepone/intersecta " + codigo1 + " numer " + numeracion + " manzana " + manzana);
            throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Codigo" + codigo1 + " numeracion " + numeracion + " manzana " + manzana);
        }
    }

    /**
     * Valida que no haya lineas crusadas entre los predios adyacentes
     *
     * @param codigo1 codigo codigo catastral del predio matrix
     * @param numeracion numeracion del nuevo predio fraccionado
     */
    public void checkPredioPolygonConflictosInterseccion(String codigo1, Short numeracion) {
//        String sql1 = "SELECT sl1.gid, sl1.codigo FROM " + this.ptm.getTransTableName() + " sl1 WHERE sl1.codigo ~ :codigoSector AND sl1.habilitado = true "
//                + " AND ST_IsEmpty( ST_Intersection( (SELECT ptx2.geom FROM " + this.ptm.getTransTableName() + " ptx2 "
//                + "WHERE ptx2.codigo = :codigo2 AND ptx2.numeracion = :numeracion AND (ptx2.manzana IS NULL OR ptx2.manzana = 0)),"
//                + " sl1.geom ) ) = false;";
        String sql1 = "SELECT s2.gid, s2.codigo, s2.numeracion, s2.manzana "
                + "FROM " + this.ptm.getTransTableName() + " s1 INNER JOIN " + this.ptm.getTransTableName() + " s2 ON ST_Intersects(s2.geom, s1.geom) "
                + "WHERE s1.codigo = :codigo2 AND s1.numeracion  = :numeracion  "
                + "AND (s1.manzana IS NULL OR s1.manzana = 0) "
                + " AND s1.gid <> s2.gid AND ST_Area(ST_Intersection(s2.geom, s1.geom)) > 0";
        SQLQuery q1 = sessGis().createSQLQuery(sql1);
//        q1.setString("codigoSector", codigo1.substring(0, 10));
        q1.setString("codigo2", codigo1);
        q1.setShort("numeracion", numeracion);
        List<Object[]> rl = q1.list();
        if (!rl.isEmpty()) {
            throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Codigo" + codigo1 + " numeracion " + numeracion);
        }
    }

    /**
     * Validad si los poliginos no estan solapados
     *
     * @param gid clave a Validar
     */
    public void checkPredioPolygonConflictos(Integer gid) {
        SQLQuery q1 = sessGis().createSQLQuery(QueryGeo.checkPredioPolygonConflictosGid);
        q1.setInteger("gid", gid);
        List<Object[]> rl = q1.list();
        if (!rl.isEmpty()) {
            if (rl.get(0)[2] != null) {
                JsfUti.messageError(null, "Error al validar interseccion en poligono", "El" + rl.get(0)[1] + " polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[2]);
                throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[1]);
            }
        }
    }

    /**
     * Validad si los poliginos no estan solapados
     *
     * @param codigo1 clave a Validar
     */
    public void checkPredioPolygonConflictos(String codigo1) {
        SQLQuery q1 = sessGis().createSQLQuery(QueryGeo.checkPredioPolygonConflictos);
        q1.setString("codigo", codigo1);
        List<Object[]> rl = q1.list();
        if (!rl.isEmpty()) {
            if (rl.get(0)[2] != null) {
                JsfUti.messageError(null, "Error al validar interseccion en poligono", "El" + rl.get(0)[1] + " polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[2]);
                throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[1]);
            }
        }
    }

    /**
     * Validad el lostado de claves que no esten solapados
     *
     * @param codigos Lista de codigos a validar;
     */
    public void checkPredioPolygonConflictos(List<String> codigos) {
        for (String codigo : codigos) {
            this.checkPredioPolygonConflictos(codigo);
        }
    }

    /**
     * Validad si los poliginos no estan solapados
     *
     * @param codigo1 clave a Validar
     */
    public void checkPredioPolygonConflictosCalidad(String codigo1) {
//        SQLQuery q1 = sessGis().createSQLQuery(QueryGeo.checkPredioPolygonConflictosCalidad);
//        q1.setString("codigo", codigo1);
//        List<Object[]> rl = q1.list();
//        if (!rl.isEmpty()) {
//            JsfUti.messageError(null, "Error al validar interseccion en poligono tabla ct_calidad_suelo_rural_predio", "El" + rl.get(0)[1] + " polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[2]);
//            throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[1]);
//        }
    }

    /**
     * Validad el lostado de claves que no esten solapados
     *
     * @param codigos Lista de codigos a validar;
     */
    public void checkPredioPolygonConflictosCalidad(List<String> codigos) {
        for (String codigo : codigos) {
            this.checkPredioPolygonConflictosCalidad(codigo);
        }
    }

    /**
     * Validad si los poliginos no estan solapados
     *
     * @param gitPredio gid de predios_tx
     */
    public void checkPredioPolygonConflictosCalidad(Integer gitPredio) {
//        SQLQuery q1 = sessGis().createSQLQuery(QueryGeo.checkPredioPolygonConflictosCalidadGidPT);
//        q1.setInteger("gitPredio", gitPredio);
//        List<Object[]> rl = q1.list();
//        if (!rl.isEmpty()) {
//            JsfUti.messageError(null, "Error al validar interseccion en poligono", "El" + rl.get(0)[1] + " polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[2]);
//            throw new GeoProcesosException("El polígono sobrepone/intersecta con otro polígono existente. Clave Catastral " + rl.get(0)[1]);
//        }
    }

}
