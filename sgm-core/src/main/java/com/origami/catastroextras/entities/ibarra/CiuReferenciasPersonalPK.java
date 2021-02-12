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
public class CiuReferenciasPersonalPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 13)
    @Column(name = "rp_ciudadano", nullable = false, length = 13)
    private String rpCiudadano;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 13)
    @Column(name = "rp_ced_ruc", nullable = false, length = 13)
    private String rpCedRuc;

    public CiuReferenciasPersonalPK() {
    }

    public CiuReferenciasPersonalPK(String rpCiudadano, String rpCedRuc) {
        this.rpCiudadano = rpCiudadano;
        this.rpCedRuc = rpCedRuc;
    }

    public String getRpCiudadano() {
        return rpCiudadano;
    }

    public void setRpCiudadano(String rpCiudadano) {
        this.rpCiudadano = rpCiudadano;
    }

    public String getRpCedRuc() {
        return rpCedRuc;
    }

    public void setRpCedRuc(String rpCedRuc) {
        this.rpCedRuc = rpCedRuc;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (rpCiudadano != null ? rpCiudadano.hashCode() : 0);
        hash += (rpCedRuc != null ? rpCedRuc.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuReferenciasPersonalPK)) {
            return false;
        }
        CiuReferenciasPersonalPK other = (CiuReferenciasPersonalPK) object;
        if ((this.rpCiudadano == null && other.rpCiudadano != null) || (this.rpCiudadano != null && !this.rpCiudadano.equals(other.rpCiudadano))) {
            return false;
        }
        if ((this.rpCedRuc == null && other.rpCedRuc != null) || (this.rpCedRuc != null && !this.rpCedRuc.equals(other.rpCedRuc))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuReferenciasPersonalPK[ rpCiudadano=" + rpCiudadano + ", rpCedRuc=" + rpCedRuc + " ]";
    }

}
