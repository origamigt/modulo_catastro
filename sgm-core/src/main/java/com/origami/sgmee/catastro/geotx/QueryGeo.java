/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

/**
 *
 * @author ANGEL NAVARRO
 */
public class QueryGeo {

//    public static String consultarCalidadSuelo = "SELECT DISTINCT csr.gid, csr.fcode, CAST(csr.calidad AS Integer) calidad, CAST(ST_AREA(csr.geom) AS NUMERIC) area, csr.valor, csr.codigo, csr.num_predio \"numPredio\" FROM geodata.ct_calidad_suelo_rural_predio csr INNER JOIN geodata.predios_tx tx ON ST_INTERSECTS(ST_BUFFER(csr.geom, -2), tx.geom ) WHERE tx.gid = ? AND ST_INTERSECTS(csr.geom, tx.geom )" ;
    /**
     * Obtiene las calidades de suelo pertenecientes al predio
     */
    public static String consultarCalidadSuelo = "SELECT DISTINCT csr.gid, csr.fcode, CAST(csr.calidad AS Integer) calidad, CAST(ST_AREA(csr.geom) AS NUMERIC) area, csr.valor, csr.codigo, csr.num_predio \"numPredio\", sector_homogeneo \"sectorHomogeneo\" , calidad_suelo \"calidadSuelo\", codigo_uso_suelo \"codigoUsoSuelo\" FROM geodata.ct_calidad_suelo_rural_predio csr INNER JOIN geodata.predios_tx tx ON ST_INTERSECTS(ST_BUFFER(csr.geom, -2), tx.geom ) INNER JOIN geodata.ct_sector_homogeneo sh ON ST_INTERSECTS(ST_BUFFER(csr.geom, -2), sh.geom ) WHERE tx.gid = ? AND ST_INTERSECTS(csr.geom, tx.geom );";
    /**
     * Obtiene las calidades de suelo pertenecientes al predio por el gid de la
     * tabla
     */
    public static String intersectionCalidadSuelo = "SELECT * FROM geodata.predios_intersection_calidad( ? ) WHERE area1 > 0;";

//    public static String consultarCalidadSueloByClave = "SELECT DISTINCT csr.gid, csr.fcode, CAST(csr.calidad AS Integer) calidad, CAST(ST_AREA(csr.geom) AS NUMERIC) area, csr.valor, csr.codigo, csr.num_predio \"numPredio\", sector_homogeneo \"sectorHomogeneo\" , calidad_suelo \"calidadSuelo\", codigo_uso_suelo \"codigoUsoSuelo\" FROM geodata.ct_calidad_suelo_rural_predio csr INNER JOIN geodata.predios_tx tx ON ST_INTERSECTS(ST_BUFFER(csr.geom, -2), tx.geom ) INNER JOIN geodata.ct_sector_homogeneo sh ON ST_INTERSECTS(ST_BUFFER(csr.geom, -2), sh.geom ) WHERE tx.codigo = ? AND ST_INTERSECTS(csr.geom, tx.geom );";
    /**
     * Obtiene las calidades de suelo pertenecientes al predio por el gid de la
     * tabla
     */
    public static String consultarCalidadSueloByClave = "SELECT * FROM geodata.predios_intersection_calidad( ? ) WHERE area1 > 0;";
    public static String updateCtCalidadSueloRuralPredio = "UPDATE geodata.ct_calidad_suelo_rural_predio csr SET codigo = ?, num_predio = ? WHERE csr.gid = ? ";

    // VALIDACIONES 
    // PREDIOS TX
    /**
     * Validad la solapacion entres los predios de la misma capa pasandolo la
     * clave catastral
     */
    public static String checkPredioPolygonConflictos = "SELECT sl1.gid, sl1.codigo, sl2.codigo codigo_inst FROM geodata.predios_tx sl1 INNER JOIN geodata.predios_tx sl2 "
            + " ON ST_Relate(sl1.geom, sl2.geom, 'T********') WHERE sl1.codigo = :codigo AND ST_Equals(sl1.geom, sl2.geom) = false AND ST_Intersects(sl1.geom, sl2.geom) ;";
    /**
     * Validad la solapacion entres los predios de la misma capa pasandolo el
     * gid
     */
    public static String checkPredioPolygonConflictosGid = "SELECT sl1.gid, sl1.codigo, sl2.codigo codigo_inst FROM geodata.predios_tx sl1 INNER JOIN geodata.predios_tx sl2 "
            + " ON ST_Relate(sl1.geom, sl2.geom, 'T********') WHERE sl1.gid = :gid AND sl1.gid <> sl2.gid AND ST_Intersects(sl1.geom, sl2.geom) ;";
    /**
     * Validad la solapacion entres los predios de la misma capa pasandolo el
     * gid
     */
    public static String checkPredioPolygonConflictosGidNue = "SELECT sl1.gid, sl1.codigo, sl2.codigo codigo_inst FROM geodata.predios_tx sl1 INNER JOIN geodata.predios_tx sl2 "
            + " ON ST_Relate(sl1.geom, sl2.geom, 'T********') WHERE sl1.gid = :gid AND sl1.gid <> sl2.gid AND ST_Intersects(sl1.geom, sl2.geom) AND ROUND(CAST(ST_AREA(ST_Intersection(sl1.geom, sl2.geom)) AS NUMERIC), 2) > 0;";

    // CALIDAD DE SUELO RURAL
    /**
     * Validad la solapacion entres las calidades de suelo de la misma capa
     * pasandolo la clave catastral
     */
    static String checkPredioPolygonConflictosCalidad = "SELECT sl1.gid, sl1.codigo, sl2.codigo codigo_inst, sl2.gid gid_ins FROM geodata.ct_calidad_suelo_rural_predio sl1 INNER JOIN geodata.ct_calidad_suelo_rural_predio sl2 ON ST_Relate(sl1.geom, sl2.geom, 'T********') "
            + "INNER JOIN geodata.predios_tx tx ON ST_Intersects(tx.geom, ST_Buffer(sl1.geom, -5)) WHERE tx.codigo = :codigo AND ST_Equals(sl1.geom, sl2.geom) = false AND ST_Intersects(sl1.geom, sl2.geom) AND ST_Intersects(tx.geom, sl1.geom) ;";
    /**
     * Validad la solapacion entres las calidades de suelo de la misma capa
     * pasandolo el gid
     */
    static String checkPredioPolygonConflictosCalidadGidPT = "SELECT sl1.gid, sl1.codigo, sl2.codigo codigo_inst, sl2.gid gid_ins FROM geodata.ct_calidad_suelo_rural_predio sl1 INNER JOIN geodata.ct_calidad_suelo_rural_predio sl2 ON ST_Relate(sl1.geom, sl2.geom, 'T********') "
            + "INNER JOIN geodata.predios_tx tx ON ST_Intersects(tx.geom, ST_Buffer(sl1.geom, -5)) WHERE tx.gid = :gitPredio AND ST_Equals(sl1.geom, sl2.geom) = false AND ST_Intersects(sl1.geom, sl2.geom) AND ST_Intersects(tx.geom, sl1.geom) ;";

    static String checkPolygonUpdateByUserTask = "SELECT DISTINCT gid, codigo, round(CAST(ST_Area(geom) AS numeric), 4) AS area, habilitado FROM geodata.predios_tx_log "
            + "WHERE habilitado = true AND codigo LIKE :codigo AND (transaccion LIKE '%in%' OR transaccion LIKE '%up%')  AND usuario = :usuario AND asentado = false ORDER BY gid DESC ";

}
