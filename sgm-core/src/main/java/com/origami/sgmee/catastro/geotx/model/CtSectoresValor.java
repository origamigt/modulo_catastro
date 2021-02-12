/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Angel Navarro
 */
public class CtSectoresValor implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer gid;
    private String nZona;
    private BigDecimal areaZona;
    private BigDecimal costoDeZ;
    private BigDecimal totalZona;
    private Object geom;

    public CtSectoresValor() {
    }

    public CtSectoresValor(Integer gid) {
        this.gid = gid;
    }

    public Integer getGid() {
        return gid;
    }

    public void setGid(Integer gid) {
        this.gid = gid;
    }

    public String getNZona() {
        return nZona;
    }

    public void setNZona(String nZona) {
        this.nZona = nZona;
    }

    public BigDecimal getAreaZona() {
        return areaZona;
    }

    public void setAreaZona(BigDecimal areaZona) {
        this.areaZona = areaZona;
    }

    public BigDecimal getCostoDeZ() {
        return costoDeZ;
    }

    public void setCostoDeZ(BigDecimal costoDeZ) {
        this.costoDeZ = costoDeZ;
    }

    public BigDecimal getTotalZona() {
        return totalZona;
    }

    public void setTotalZona(BigDecimal totalZona) {
        this.totalZona = totalZona;
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
        if (!(object instanceof CtSectoresValor)) {
            return false;
        }
        CtSectoresValor other = (CtSectoresValor) object;
        if ((this.gid == null && other.gid != null) || (this.gid != null && !this.gid.equals(other.gid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.origami.sgmee.catastro.geotx.entity.CtSectoresValor[ gid=" + gid + " ]";
    }

}
