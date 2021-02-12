/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Origami13
 */
@Entity
@Table(schema = IbarraSchemas.PCIUDADANO, name = "ciu_telefono", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"te_ciudadano", "te_numero"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CiuTelefono.findAll", query = "SELECT c FROM CiuTelefono c"),
    @NamedQuery(name = "CiuTelefono.findByTeSecuencial", query = "SELECT c FROM CiuTelefono c WHERE c.teSecuencial = :teSecuencial"),
    @NamedQuery(name = "CiuTelefono.findByTeNumero", query = "SELECT c FROM CiuTelefono c WHERE c.teNumero = :teNumero"),
    @NamedQuery(name = "CiuTelefono.findByTeTipo", query = "SELECT c FROM CiuTelefono c WHERE c.teTipo = :teTipo"),
    @NamedQuery(name = "CiuTelefono.findByTeEstado", query = "SELECT c FROM CiuTelefono c WHERE c.teEstado = :teEstado"),
    @NamedQuery(name = "CiuTelefono.findByTeOperadora", query = "SELECT c FROM CiuTelefono c WHERE c.teOperadora = :teOperadora")})
public class CiuTelefono implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "te_secuencial", nullable = false)
    private Integer teSecuencial;
    @Size(max = 15)
    @Column(name = "te_numero", length = 15)
    private String teNumero;
    @Size(max = 5)
    @Column(name = "te_tipo", length = 5)
    private String teTipo;
    @Column(name = "te_estado")
    private Character teEstado;
    @Size(max = 2)
    @Column(name = "te_operadora", length = 2)
    private String teOperadora;
    @JoinColumn(name = "te_ciudadano", referencedColumnName = "ciu_ced_ruc")
    @ManyToOne
    private CiuCiudadano teCiudadano;

    public CiuTelefono() {
    }

    public CiuTelefono(Integer teSecuencial) {
        this.teSecuencial = teSecuencial;
    }

    public Integer getTeSecuencial() {
        return teSecuencial;
    }

    public void setTeSecuencial(Integer teSecuencial) {
        this.teSecuencial = teSecuencial;
    }

    public String getTeNumero() {
        return teNumero;
    }

    public void setTeNumero(String teNumero) {
        this.teNumero = teNumero;
    }

    public String getTeTipo() {
        return teTipo;
    }

    public void setTeTipo(String teTipo) {
        this.teTipo = teTipo;
    }

    public Character getTeEstado() {
        return teEstado;
    }

    public void setTeEstado(Character teEstado) {
        this.teEstado = teEstado;
    }

    public String getTeOperadora() {
        return teOperadora;
    }

    public void setTeOperadora(String teOperadora) {
        this.teOperadora = teOperadora;
    }

    public CiuCiudadano getTeCiudadano() {
        return teCiudadano;
    }

    public void setTeCiudadano(CiuCiudadano teCiudadano) {
        this.teCiudadano = teCiudadano;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (teSecuencial != null ? teSecuencial.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuTelefono)) {
            return false;
        }
        CiuTelefono other = (CiuTelefono) object;
        if ((this.teSecuencial == null && other.teSecuencial != null) || (this.teSecuencial != null && !this.teSecuencial.equals(other.teSecuencial))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuTelefono[ teSecuencial=" + teSecuencial + " ]";
    }

}
