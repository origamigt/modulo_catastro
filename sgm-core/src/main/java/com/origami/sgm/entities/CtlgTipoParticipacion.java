/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author CarlosLoorVargas
 */
@Entity
@Table(name = "ctlg_tipo_participacion", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CtlgTipoParticipacion.findAll", query = "SELECT c FROM CtlgTipoParticipacion c"),
    @NamedQuery(name = "CtlgTipoParticipacion.findById", query = "SELECT c FROM CtlgTipoParticipacion c WHERE c.id = :id"),
    @NamedQuery(name = "CtlgTipoParticipacion.findByNombre", query = "SELECT c FROM CtlgTipoParticipacion c WHERE c.nombre = :nombre"),
    @NamedQuery(name = "CtlgTipoParticipacion.findByEstado", query = "SELECT c FROM CtlgTipoParticipacion c WHERE c.estado = :estado")})
public class CtlgTipoParticipacion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;
    @Size(max = 255)
    @Column(name = "nombre", length = 255)
    private String nombre;
    @Column(name = "estado")
    private Boolean estado;

    public CtlgTipoParticipacion() {
    }

    public CtlgTipoParticipacion(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CtlgTipoParticipacion)) {
            return false;
        }
        CtlgTipoParticipacion other = (CtlgTipoParticipacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gob.samborondon.entities.CtlgTipoParticipacion[ id=" + id + " ]";
    }

}
