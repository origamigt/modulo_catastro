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
@Table(schema = IbarraSchemas.PCIUDADANO, name = "ciu_relaciones")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CiuRelaciones.findAll", query = "SELECT c FROM CiuRelaciones c"),
    @NamedQuery(name = "CiuRelaciones.findByRlfCiudadano", query = "SELECT c FROM CiuRelaciones c WHERE c.ciuRelacionesPK.rlfCiudadano = :rlfCiudadano"),
    @NamedQuery(name = "CiuRelaciones.findByRlfCedRuc", query = "SELECT c FROM CiuRelaciones c WHERE c.ciuRelacionesPK.rlfCedRuc = :rlfCedRuc"),
    @NamedQuery(name = "CiuRelaciones.findByRlfNombre", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfNombre = :rlfNombre"),
    @NamedQuery(name = "CiuRelaciones.findByRlfRelacion", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfRelacion = :rlfRelacion"),
    @NamedQuery(name = "CiuRelaciones.findByRlfTelefono", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfTelefono = :rlfTelefono"),
    @NamedQuery(name = "CiuRelaciones.findByRlfDireccion", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfDireccion = :rlfDireccion"),
    @NamedQuery(name = "CiuRelaciones.findByRlfGradoConsan", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfGradoConsan = :rlfGradoConsan"),
    @NamedQuery(name = "CiuRelaciones.findByRlfFecha", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfFecha = :rlfFecha"),
    @NamedQuery(name = "CiuRelaciones.findByRlfUsuario", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfUsuario = :rlfUsuario"),
    @NamedQuery(name = "CiuRelaciones.findByRlfEstado", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfEstado = :rlfEstado"),
    @NamedQuery(name = "CiuRelaciones.findByRlfApellidoPat", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfApellidoPat = :rlfApellidoPat"),
    @NamedQuery(name = "CiuRelaciones.findByRlfApellidoMat", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfApellidoMat = :rlfApellidoMat"),
    @NamedQuery(name = "CiuRelaciones.findByRlfNombres", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfNombres = :rlfNombres"),
    @NamedQuery(name = "CiuRelaciones.findByRlfEdad", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfEdad = :rlfEdad"),
    @NamedQuery(name = "CiuRelaciones.findByRlfDocumento", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfDocumento = :rlfDocumento"),
    @NamedQuery(name = "CiuRelaciones.findByRlfMovil", query = "SELECT c FROM CiuRelaciones c WHERE c.rlfMovil = :rlfMovil")})
public class CiuRelaciones implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CiuRelacionesPK ciuRelacionesPK;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 70)
    @Column(name = "rlf_nombre", length = 70)
    private String rlfNombre;
    @Basic(optional = false)
    @NotNull
    @Column(name = "rlf_relacion")
    private Integer rlfRelacion;
    @Size(max = 20)
    @Column(name = "rlf_telefono", length = 20)
    private String rlfTelefono;
    @Size(max = 70)
    @Column(name = "rlf_direccion", length = 70)
    private String rlfDireccion;
    @Column(name = "rlf_grado_consan")
    private Integer rlfGradoConsan;
    @Column(name = "rlf_fecha")
    @Temporal(TemporalType.DATE)
    private Date rlfFecha;
    @Size(max = 25)
    @Column(name = "rlf_usuario", length = 25)
    private String rlfUsuario;
    @Size(max = 2)
    @Column(name = "rlf_estado", length = 2)
    private String rlfEstado;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "rlf_apellido_pat", length = 25)
    private String rlfApellidoPat;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 25)
    @Column(name = "rlf_apellido_mat", length = 25)
    private String rlfApellidoMat;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "rlf_nombres", length = 30)
    private String rlfNombres;
    @Column(name = "rlf_edad")
    private Integer rlfEdad;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "rlf_documento", nullable = false, length = 250)
    private String rlfDocumento;
    @Size(max = 13)
    @Column(name = "rlf_movil", length = 13)
    private String rlfMovil;
    @JoinColumn(name = "rlf_ciudadano", referencedColumnName = "ciu_ced_ruc", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CiuCiudadano ciuCiudadano;

    public CiuRelaciones() {
    }

    public CiuRelaciones(CiuRelacionesPK ciuRelacionesPK) {
        this.ciuRelacionesPK = ciuRelacionesPK;
    }

    public CiuRelaciones(CiuRelacionesPK ciuRelacionesPK, String rlfNombre, Integer rlfRelacion, String rlfApellidoPat, String rlfApellidoMat, String rlfNombres, String rlfDocumento) {
        this.ciuRelacionesPK = ciuRelacionesPK;
        this.rlfNombre = rlfNombre;
        this.rlfRelacion = rlfRelacion;
        this.rlfApellidoPat = rlfApellidoPat;
        this.rlfApellidoMat = rlfApellidoMat;
        this.rlfNombres = rlfNombres;
        this.rlfDocumento = rlfDocumento;
    }

    public CiuRelaciones(String rlfCiudadano, String rlfCedRuc) {
        this.ciuRelacionesPK = new CiuRelacionesPK(rlfCiudadano, rlfCedRuc);
    }

    public CiuRelacionesPK getCiuRelacionesPK() {
        return ciuRelacionesPK;
    }

    public void setCiuRelacionesPK(CiuRelacionesPK ciuRelacionesPK) {
        this.ciuRelacionesPK = ciuRelacionesPK;
    }

    public String getRlfNombre() {
        return rlfNombre;
    }

    public void setRlfNombre(String rlfNombre) {
        this.rlfNombre = rlfNombre;
    }

    public Integer getRlfRelacion() {
        return rlfRelacion;
    }

    public void setRlfRelacion(Integer rlfRelacion) {
        this.rlfRelacion = rlfRelacion;
    }

    public String getRlfTelefono() {
        return rlfTelefono;
    }

    public void setRlfTelefono(String rlfTelefono) {
        this.rlfTelefono = rlfTelefono;
    }

    public String getRlfDireccion() {
        return rlfDireccion;
    }

    public void setRlfDireccion(String rlfDireccion) {
        this.rlfDireccion = rlfDireccion;
    }

    public Integer getRlfGradoConsan() {
        return rlfGradoConsan;
    }

    public void setRlfGradoConsan(Integer rlfGradoConsan) {
        this.rlfGradoConsan = rlfGradoConsan;
    }

    public Date getRlfFecha() {
        return rlfFecha;
    }

    public void setRlfFecha(Date rlfFecha) {
        this.rlfFecha = rlfFecha;
    }

    public String getRlfUsuario() {
        return rlfUsuario;
    }

    public void setRlfUsuario(String rlfUsuario) {
        this.rlfUsuario = rlfUsuario;
    }

    public String getRlfEstado() {
        return rlfEstado;
    }

    public void setRlfEstado(String rlfEstado) {
        this.rlfEstado = rlfEstado;
    }

    public String getRlfApellidoPat() {
        return rlfApellidoPat;
    }

    public void setRlfApellidoPat(String rlfApellidoPat) {
        this.rlfApellidoPat = rlfApellidoPat;
    }

    public String getRlfApellidoMat() {
        return rlfApellidoMat;
    }

    public void setRlfApellidoMat(String rlfApellidoMat) {
        this.rlfApellidoMat = rlfApellidoMat;
    }

    public String getRlfNombres() {
        return rlfNombres;
    }

    public void setRlfNombres(String rlfNombres) {
        this.rlfNombres = rlfNombres;
    }

    public Integer getRlfEdad() {
        return rlfEdad;
    }

    public void setRlfEdad(Integer rlfEdad) {
        this.rlfEdad = rlfEdad;
    }

    public String getRlfDocumento() {
        return rlfDocumento;
    }

    public void setRlfDocumento(String rlfDocumento) {
        this.rlfDocumento = rlfDocumento;
    }

    public String getRlfMovil() {
        return rlfMovil;
    }

    public void setRlfMovil(String rlfMovil) {
        this.rlfMovil = rlfMovil;
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
        hash += (ciuRelacionesPK != null ? ciuRelacionesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuRelaciones)) {
            return false;
        }
        CiuRelaciones other = (CiuRelaciones) object;
        if ((this.ciuRelacionesPK == null && other.ciuRelacionesPK != null) || (this.ciuRelacionesPK != null && !this.ciuRelacionesPK.equals(other.ciuRelacionesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuRelaciones[ ciuRelacionesPK=" + ciuRelacionesPK + " ]";
    }

}
