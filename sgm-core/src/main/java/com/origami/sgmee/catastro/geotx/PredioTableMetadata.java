package com.origami.sgmee.catastro.geotx;

import javax.enterprise.context.ApplicationScoped;

/**
 * Contiene en nombre de las tablas de la base grafica.
 *
 * @author Fernando
 */
@ApplicationScoped
public class PredioTableMetadata {

    private final String schema = "geodata";
    private String tablename = schema + ".geo_solar";
    private String geomColumn = "geom";
    private String codigoColumn = "codigo";
    private String enableColumn = "habilitado";
    private Integer srid = 32717;
    private String geomType = "MULTIPOLYGON";

    private String TransTableName = schema + ".predios_tx";
    private String TransTableIdColumn = "gid";
    private String TransTableProcessColumn = "process_id";

    // TABLAS DE BLOQUES
    private String transBloque = schema + ".temp_bloque";
    private String geoBloque = schema + ".ct_bloque_constructivo";

    // MANZANAS 
    private String ctManzanas = schema + ".ct_manzanas";
    private String ctSectoresValor = schema + ".ct_sectores_valor";

    public PredioTableMetadata() {
    }

    /**
     * Devuelve el nombre de la tabla definitiva de los predios.
     *
     * @return <code>tablename</code>
     */
    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getGeomColumn() {
        return geomColumn;
    }

    public void setGeomColumn(String geomColumn) {
        this.geomColumn = geomColumn;
    }

    public String getCodigoColumn() {
        return codigoColumn;
    }

    public void setCodigoColumn(String codigoColumn) {
        this.codigoColumn = codigoColumn;
    }

    public String getEnableColumn() {
        return enableColumn;
    }

    public void setEnableColumn(String enableColumn) {
        this.enableColumn = enableColumn;
    }

    public Integer getSrid() {
        return srid;
    }

    public void setSrid(Integer srid) {
        this.srid = srid;
    }

    public String getGeomType() {
        return geomType;
    }

    public void setGeomType(String geomType) {
        this.geomType = geomType;
    }

    /**
     * Retorna el nombre de la clase transaccional del predio.
     *
     * @return
     */
    public String getTransTableName() {
        return TransTableName;
    }

    public void setTransTableName(String TransTableName) {
        this.TransTableName = TransTableName;
    }

    public String getTransTableIdColumn() {
        return TransTableIdColumn;
    }

    public void setTransTableIdColumn(String TransTableIdColumn) {
        this.TransTableIdColumn = TransTableIdColumn;
    }

    public String getTransTableProcessColumn() {
        return TransTableProcessColumn;
    }

    public void setTransTableProcessColumn(String TransTableProcessColumn) {
        this.TransTableProcessColumn = TransTableProcessColumn;
    }

    public String getTransBloque() {
        return transBloque;
    }

    public void setTransBloque(String transBloque) {
        this.transBloque = transBloque;
    }

    public String getGeoBloque() {
        return geoBloque;
    }

    public void setGeoBloque(String geoBloque) {
        this.geoBloque = geoBloque;
    }

    public String getCtManzanas() {
        return ctManzanas;
    }

    public void setCtManzanas(String ctManzanas) {
        this.ctManzanas = ctManzanas;
    }

    public String getCtSectoresValor() {
        return ctSectoresValor;
    }

    public void setCtSectoresValor(String ctSectoresValor) {
        this.ctSectoresValor = ctSectoresValor;
    }

}
