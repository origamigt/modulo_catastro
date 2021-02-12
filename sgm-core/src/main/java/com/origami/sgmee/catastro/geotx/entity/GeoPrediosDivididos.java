/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.entity;

import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Entiti que permite almacenar los datos necesarios de los nuevos poligonos
 * creados a pertir de un fracionamiento o afectacion.
 *
 * @author Fernando
 *
 */
@SequenceGenerator(name = "geo_predios_divididos_id_seq", sequenceName = SchemasConfig.APP1 + ".geo_predios_divididos_id_seq", allocationSize = 1)
@Entity
@Table(name = "geo_predios_divididos", schema = SchemasConfig.APP1)
public class GeoPrediosDivididos implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "geo_predios_divididos_id_seq")
    private Long id;

    @Column(name = "numeracion")
    private Short numeracion;

    @Column(name = "area")
    private BigDecimal area;

    @Column(name = "gid")
    private Integer gid;

    @JoinColumn(name = "procesodiv", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private GeoProcesoDivision procesoDiv;

    @Column(name = "codigo_nuevo", length = 45)
    private String codigoNuevo;

    @Transient
    private BigInteger numPredio;

    @Transient
//    @Column(name = "predio", columnDefinition = "bigint")
    private Long predio;

    public GeoPrediosDivididos() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Short getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(Short numeracion) {
        this.numeracion = numeracion;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public GeoProcesoDivision getProcesoDiv() {
        return procesoDiv;
    }

    public void setProcesoDiv(GeoProcesoDivision procesoDiv) {
        this.procesoDiv = procesoDiv;
    }

    public String getCodigoNuevo() {
        return codigoNuevo;
    }

    public void setCodigoNuevo(String codigoNuevo) {
        this.codigoNuevo = codigoNuevo;
    }

    public BigInteger getNumPredio() {
        return numPredio;
    }

    public void setNumPredio(BigInteger numPredio) {
        this.numPredio = numPredio;
    }

    public Long getPredio() {
        return predio;
    }

    public void setPredio(Long predio) {
        this.predio = predio;
    }

    @Override
    public int compareTo(Object o) {
        Short compareNumeracion = ((GeoPrediosDivididos) o).getNumeracion();
        return this.numeracion - compareNumeracion;

    }

    @Override
    public String toString() {
        return "Codigo: " + codigoNuevo + " Num: " + numeracion;
    }

}
