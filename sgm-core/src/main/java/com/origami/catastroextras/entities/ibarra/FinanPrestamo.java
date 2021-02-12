/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.annotations.ReportField;
import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author TIC
 */
@Entity
@Table(name = "finan_prestamo", schema = IbarraSchemas.CATASTRO)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FinanPrestamo.findAll", query = "SELECT f FROM FinanPrestamo f")
    ,
    @NamedQuery(name = "FinanPrestamo.findByCodFinanPrestamo", query = "SELECT f FROM FinanPrestamo f WHERE f.codFinanPrestamo = :codFinanPrestamo")
    ,
    @NamedQuery(name = "FinanPrestamo.findByDescripcionFinanPrestamo", query = "SELECT f FROM FinanPrestamo f WHERE f.descripcionFinanPrestamo = :descripcionFinanPrestamo")
    ,
    @NamedQuery(name = "FinanPrestamo.findByEstadoFinanPrestamo", query = "SELECT f FROM FinanPrestamo f WHERE f.estadoFinanPrestamo = :estadoFinanPrestamo")})
public class FinanPrestamo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "cod_finan_prestamo")
    private Integer codFinanPrestamo;
    @Size(max = 50)
    @Column(name = "descripcion_finan_prestamo")
    @ReportField(description = "Descripción")
    private String descripcionFinanPrestamo;
    @Size(max = 50)
    @Column(name = "estado_finan_prestamo")
    private String estadoFinanPrestamo;

    public FinanPrestamo() {
    }

    public FinanPrestamo(Integer codFinanPrestamo) {
        this.codFinanPrestamo = codFinanPrestamo;
    }

    public Integer getCodFinanPrestamo() {
        return codFinanPrestamo;
    }

    public void setCodFinanPrestamo(Integer codFinanPrestamo) {
        this.codFinanPrestamo = codFinanPrestamo;
    }

    public String getDescripcionFinanPrestamo() {
        return descripcionFinanPrestamo;
    }

    public void setDescripcionFinanPrestamo(String descripcionFinanPrestamo) {
        this.descripcionFinanPrestamo = descripcionFinanPrestamo;
    }

    public String getEstadoFinanPrestamo() {
        return estadoFinanPrestamo;
    }

    public void setEstadoFinanPrestamo(String estadoFinanPrestamo) {
        this.estadoFinanPrestamo = estadoFinanPrestamo;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codFinanPrestamo != null ? codFinanPrestamo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FinanPrestamo)) {
            return false;
        }
        FinanPrestamo other = (FinanPrestamo) object;
        if ((this.codFinanPrestamo == null && other.codFinanPrestamo != null) || (this.codFinanPrestamo != null && !this.codFinanPrestamo.equals(other.codFinanPrestamo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FinanPrestamo[ codFinanPrestamo=" + codFinanPrestamo + " ]";
    }

}
