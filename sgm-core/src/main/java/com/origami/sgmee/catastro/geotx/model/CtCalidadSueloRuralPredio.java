/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 *
 * @author ANGEL NAVARRO
 */
public class CtCalidadSueloRuralPredio implements Serializable {

    private Integer gid;
    private String fcode;
    private Integer calidad;
    private BigDecimal area;
    private BigDecimal area1;
    private BigDecimal valor;
    private String codigo;
    private BigInteger numPredio;
    private BigInteger sectorHomogeneo;
    private Integer calidadSuelo;
    private Integer codigoUsoSuelo;

    public CtCalidadSueloRuralPredio() {
    }

    public CtCalidadSueloRuralPredio(Integer gid, String fcode, Integer calidad, BigDecimal area, BigDecimal valor, String codigo, BigInteger numPredio) {
        this.gid = gid;
        this.fcode = fcode;
        this.calidad = calidad;
        this.area = area;
        this.valor = valor;
        this.codigo = codigo;
        this.numPredio = numPredio;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getFcode() {
        return fcode;
    }

    public void setFcode(String fcode) {
        this.fcode = fcode;
    }

    public Integer getCalidad() {
        return calidad;
    }

    public void setCalidad(Integer calidad) {
        this.calidad = calidad;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigInteger getNumPredio() {
        return numPredio;
    }

    public void setNumPredio(BigInteger numPredio) {
        this.numPredio = numPredio;
    }

    public BigInteger getSectorHomogeneo() {
        return sectorHomogeneo;
    }

    public void setSectorHomogeneo(BigInteger sectorHomogeneo) {
        this.sectorHomogeneo = sectorHomogeneo;
    }

    public Integer getCalidadSuelo() {
        return calidadSuelo;
    }

    public void setCalidadSuelo(Integer calidadSuelo) {
        this.calidadSuelo = calidadSuelo;
    }

    public Integer getCodigoUsoSuelo() {
        return codigoUsoSuelo;
    }

    public void setCodigoUsoSuelo(Integer codigoUsoSuelo) {
        this.codigoUsoSuelo = codigoUsoSuelo;
    }

    public BigDecimal getArea1() {
        return area1;
    }

    public void setArea1(BigDecimal area1) {
        this.area1 = area1;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.gid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CtCalidadSueloRuralPredio other = (CtCalidadSueloRuralPredio) obj;
        if (!Objects.equals(this.gid, other.gid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CtCalidadSueloRuralPredio{" + "gid=" + gid + ", calidadSuelo=" + calidadSuelo + '}';
    }

}
