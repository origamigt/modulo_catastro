/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Origami13
 */
@Entity
@Table(schema = IbarraSchemas.PCIUDADANO, name = "ciu_referencias_personal")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CiuReferenciasPersonal.findAll", query = "SELECT c FROM CiuReferenciasPersonal c"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpCiudadano", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.ciuReferenciasPersonalPK.rpCiudadano = :rpCiudadano"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpCedRuc", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.ciuReferenciasPersonalPK.rpCedRuc = :rpCedRuc"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpNombre", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpNombre = :rpNombre"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpRelacion", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpRelacion = :rpRelacion"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpTelefono", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpTelefono = :rpTelefono"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpDireccion", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpDireccion = :rpDireccion"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpUsuario", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpUsuario = :rpUsuario"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpFecha", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpFecha = :rpFecha"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpEstado", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpEstado = :rpEstado"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpDocumento", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpDocumento = :rpDocumento"),
    @NamedQuery(name = "CiuReferenciasPersonal.findByRpMovil", query = "SELECT c FROM CiuReferenciasPersonal c WHERE c.rpMovil = :rpMovil")})
public class CiuReferenciasPersonal implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CiuReferenciasPersonalPK ciuReferenciasPersonalPK;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "rp_nombre", nullable = false, length = 70)
    private String rpNombre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "rp_relacion")
    private Integer rpRelacion;
    @Size(max = 20)
    @Column(name = "rp_telefono", length = 20)
    private String rpTelefono;
    @Size(max = 70)
    @Column(name = "rp_direccion", length = 70)
    private String rpDireccion;
    @Size(max = 25)
    @Column(name = "rp_usuario", length = 25)
    private String rpUsuario;
    @Column(name = "rp_fecha")
    @Temporal(TemporalType.DATE)
    private Date rpFecha;
    @Size(max = 2)
    @Column(name = "rp_estado", length = 2)
    private String rpEstado;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "rp_documento", length = 250)
    private String rpDocumento;
    @Size(max = 13)
    @Column(name = "rp_movil", length = 13)
    private String rpMovil;
    @JoinColumn(name = "rp_ciudadano", referencedColumnName = "ciu_ced_ruc", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CiuCiudadano ciuCiudadano;

    public CiuReferenciasPersonal() {
    }

    public CiuReferenciasPersonal(CiuReferenciasPersonalPK ciuReferenciasPersonalPK) {
        this.ciuReferenciasPersonalPK = ciuReferenciasPersonalPK;
    }

    public CiuReferenciasPersonal(CiuReferenciasPersonalPK ciuReferenciasPersonalPK, String rpNombre, Integer rpRelacion, String rpDocumento) {
        this.ciuReferenciasPersonalPK = ciuReferenciasPersonalPK;
        this.rpNombre = rpNombre;
        this.rpRelacion = rpRelacion;
        this.rpDocumento = rpDocumento;
    }

    public CiuReferenciasPersonal(String rpCiudadano, String rpCedRuc) {
        this.ciuReferenciasPersonalPK = new CiuReferenciasPersonalPK(rpCiudadano, rpCedRuc);
    }

    public CiuReferenciasPersonalPK getCiuReferenciasPersonalPK() {
        return ciuReferenciasPersonalPK;
    }

    public void setCiuReferenciasPersonalPK(CiuReferenciasPersonalPK ciuReferenciasPersonalPK) {
        this.ciuReferenciasPersonalPK = ciuReferenciasPersonalPK;
    }

    public String getRpNombre() {
        return rpNombre;
    }

    public void setRpNombre(String rpNombre) {
        this.rpNombre = rpNombre;
    }

    public Integer getRpRelacion() {
        return rpRelacion;
    }

    public void setRpRelacion(Integer rpRelacion) {
        this.rpRelacion = rpRelacion;
    }

    public String getRpTelefono() {
        return rpTelefono;
    }

    public void setRpTelefono(String rpTelefono) {
        this.rpTelefono = rpTelefono;
    }

    public String getRpDireccion() {
        return rpDireccion;
    }

    public void setRpDireccion(String rpDireccion) {
        this.rpDireccion = rpDireccion;
    }

    public String getRpUsuario() {
        return rpUsuario;
    }

    public void setRpUsuario(String rpUsuario) {
        this.rpUsuario = rpUsuario;
    }

    public Date getRpFecha() {
        return rpFecha;
    }

    public void setRpFecha(Date rpFecha) {
        this.rpFecha = rpFecha;
    }

    public String getRpEstado() {
        return rpEstado;
    }

    public void setRpEstado(String rpEstado) {
        this.rpEstado = rpEstado;
    }

    public String getRpDocumento() {
        return rpDocumento;
    }

    public void setRpDocumento(String rpDocumento) {
        this.rpDocumento = rpDocumento;
    }

    public String getRpMovil() {
        return rpMovil;
    }

    public void setRpMovil(String rpMovil) {
        this.rpMovil = rpMovil;
    }

    public CiuCiudadano getCiuCiudadano() {
        return ciuCiudadano;
    }

    public void setCiuCiudadano(CiuCiudadano ciuCiudadano) {
        this.ciuCiudadano = ciuCiudadano;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ciuReferenciasPersonalPK != null ? ciuReferenciasPersonalPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuReferenciasPersonal)) {
            return false;
        }
        CiuReferenciasPersonal other = (CiuReferenciasPersonal) object;
        if ((this.ciuReferenciasPersonalPK == null && other.ciuReferenciasPersonalPK != null) || (this.ciuReferenciasPersonalPK != null && !this.ciuReferenciasPersonalPK.equals(other.ciuReferenciasPersonalPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuReferenciasPersonal[ ciuReferenciasPersonalPK=" + ciuReferenciasPersonalPK + " ]";
    }

}
