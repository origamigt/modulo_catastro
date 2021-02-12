/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Modelo de datos para Ct_manzanas
 *
 * @author Angel Navarro
 */
public class CtManzanas implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer gid;
    private String mznCod;
    private String mznFte;
    private String txt;
    private String mznCodAnt;
    private BigDecimal mznArea;
    private Object geom;

    public CtManzanas() {
    }

    public CtManzanas(Integer gid) {
        this.gid = gid;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getMznCod() {
        return mznCod;
    }

    public void setMznCod(String mznCod) {
        this.mznCod = mznCod;
    }

    public String getMznFte() {
        return mznFte;
    }

    public void setMznFte(String mznFte) {
        this.mznFte = mznFte;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getMznCodAnt() {
        return mznCodAnt;
    }

    public void setMznCodAnt(String mznCodAnt) {
        this.mznCodAnt = mznCodAnt;
    }

    public BigDecimal getMznArea() {
        return mznArea;
    }

    public void setMznArea(BigDecimal mznArea) {
        this.mznArea = mznArea;
    }

    public Object getGeom() {
        return geom;
    }

    public void setGeom(Object geom) {
        this.geom = geom;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (gid != null ? gid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CtManzanas)) {
            return false;
        }
        CtManzanas other = (CtManzanas) object;
        if ((this.gid == null && other.gid != null) || (this.gid != null && !this.gid.equals(other.gid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CtManzanas[ gid=" + gid + " ]";
    }

}
