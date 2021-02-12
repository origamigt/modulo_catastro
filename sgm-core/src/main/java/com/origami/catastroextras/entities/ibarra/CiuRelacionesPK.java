/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author Origami13
 */
@Embeddable
public class CiuRelacionesPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 13)
    @Column(name = "rlf_ciudadano", nullable = false, length = 13)
    private String rlfCiudadano;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 13)
    @Column(name = "rlf_ced_ruc", nullable = false, length = 13)
    private String rlfCedRuc;

    public CiuRelacionesPK() {
    }

    public CiuRelacionesPK(String rlfCiudadano, String rlfCedRuc) {
        this.rlfCiudadano = rlfCiudadano;
        this.rlfCedRuc = rlfCedRuc;
    }

    public String getRlfCiudadano() {
        return rlfCiudadano;
    }

    public void setRlfCiudadano(String rlfCiudadano) {
        this.rlfCiudadano = rlfCiudadano;
    }

    public String getRlfCedRuc() {
        return rlfCedRuc;
    }

    public void setRlfCedRuc(String rlfCedRuc) {
        this.rlfCedRuc = rlfCedRuc;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (rlfCiudadano != null ? rlfCiudadano.hashCode() : 0);
        hash += (rlfCedRuc != null ? rlfCedRuc.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuRelacionesPK)) {
            return false;
        }
        CiuRelacionesPK other = (CiuRelacionesPK) object;
        if ((this.rlfCiudadano == null && other.rlfCiudadano != null) || (this.rlfCiudadano != null && !this.rlfCiudadano.equals(other.rlfCiudadano))) {
            return false;
        }
        if ((this.rlfCedRuc == null && other.rlfCedRuc != null) || (this.rlfCedRuc != null && !this.rlfCedRuc.equals(other.rlfCedRuc))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuRelacionesPK[ rlfCiudadano=" + rlfCiudadano + ", rlfCedRuc=" + rlfCedRuc + " ]";
    }

}
