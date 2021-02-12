/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 *
 * @author Origami13
 */
@Entity
@Table(name = "restricciones", schema = IbarraSchemas.CATASTRO)
@NamedQueries({
    @NamedQuery(name = "Restricciones.findAll", query = "SELECT r FROM Restricciones r"),
    @NamedQuery(name = "Restricciones.findByCodigoRestriccion", query = "SELECT r FROM Restricciones r WHERE r.codigoRestriccion = :codigoRestriccion"),
    @NamedQuery(name = "Restricciones.findByDescripcionRestriccion", query = "SELECT r FROM Restricciones r WHERE r.descripcionRestriccion = :descripcionRestriccion"),
    @NamedQuery(name = "Restricciones.findByEstadoRestricion", query = "SELECT r FROM Restricciones r WHERE r.estadoRestricion = :estadoRestricion"),
    @NamedQuery(name = "Restricciones.findByExoIpu", query = "SELECT r FROM Restricciones r WHERE r.exoIpu = :exoIpu"),
    @NamedQuery(name = "Restricciones.findByExoBomberos", query = "SELECT r FROM Restricciones r WHERE r.exoBomberos = :exoBomberos"),
    @NamedQuery(name = "Restricciones.findByExoSeguridad", query = "SELECT r FROM Restricciones r WHERE r.exoSeguridad = :exoSeguridad"),
    @NamedQuery(name = "Restricciones.findByExoComentario", query = "SELECT r FROM Restricciones r WHERE r.exoComentario = :exoComentario")})
public class Restricciones implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "codigo_restriccion", nullable = false)
    private Integer codigoRestriccion;
    @Column(name = "descripcion_restriccion")
    private String descripcionRestriccion;
    @Column(name = "estado_restricion")
    private String estadoRestricion;
    @Column(name = "exo_ipu")
    private BigInteger exoIpu;
    @Column(name = "exo_bomberos")
    private BigInteger exoBomberos;
    @Column(name = "exo_seguridad")
    private BigInteger exoSeguridad;
    @Column(name = "exo_comentario")
    private String exoComentario;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "restricciones")
    private List<RestricionPredio> restricionPredioList;

    public Restricciones() {
    }

    public Restricciones(Integer codigoRestriccion) {
        this.codigoRestriccion = codigoRestriccion;
    }

    public Integer getCodigoRestriccion() {
        return codigoRestriccion;
    }

    public void setCodigoRestriccion(Integer codigoRestriccion) {
        this.codigoRestriccion = codigoRestriccion;
    }

    public String getDescripcionRestriccion() {
        return descripcionRestriccion;
    }

    public void setDescripcionRestriccion(String descripcionRestriccion) {
        this.descripcionRestriccion = descripcionRestriccion;
    }

    public String getEstadoRestricion() {
        return estadoRestricion;
    }

    public void setEstadoRestricion(String estadoRestricion) {
        this.estadoRestricion = estadoRestricion;
    }

    public BigInteger getExoIpu() {
        return exoIpu;
    }

    public void setExoIpu(BigInteger exoIpu) {
        this.exoIpu = exoIpu;
    }

    public BigInteger getExoBomberos() {
        return exoBomberos;
    }

    public void setExoBomberos(BigInteger exoBomberos) {
        this.exoBomberos = exoBomberos;
    }

    public BigInteger getExoSeguridad() {
        return exoSeguridad;
    }

    public void setExoSeguridad(BigInteger exoSeguridad) {
        this.exoSeguridad = exoSeguridad;
    }

    public String getExoComentario() {
        return exoComentario;
    }

    public void setExoComentario(String exoComentario) {
        this.exoComentario = exoComentario;
    }

    public List<RestricionPredio> getRestricionPredioList() {
        return restricionPredioList;
    }

    public void setRestricionPredioList(List<RestricionPredio> restricionPredioList) {
        this.restricionPredioList = restricionPredioList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codigoRestriccion != null ? codigoRestriccion.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Restricciones)) {
            return false;
        }
        Restricciones other = (Restricciones) object;
        if ((this.codigoRestriccion == null && other.codigoRestriccion != null) || (this.codigoRestriccion != null && !this.codigoRestriccion.equals(other.codigoRestriccion))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Restricciones[ codigoRestriccion=" + codigoRestriccion + " ]";
    }

}
