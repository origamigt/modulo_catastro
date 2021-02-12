/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.google.gson.annotations.Expose;
import com.origami.annotations.Report;
import com.origami.annotations.ReportField;
import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author CarlosLoorVargas
 */
@Report(description = "Espacio Urbano / Rural", isCatatalog = true, showTree = false)
@Entity
@Table(name = "cat_tipo_conjunto", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatTipoConjunto.findAll", query = "SELECT c FROM CatTipoConjunto c")
    ,
    @NamedQuery(name = "CatTipoConjunto.findById", query = "SELECT c FROM CatTipoConjunto c WHERE c.id = :id")
    ,
    @NamedQuery(name = "CatTipoConjunto.findByNombre", query = "SELECT c FROM CatTipoConjunto c WHERE c.nombre = :nombre")})
public class CatTipoConjunto implements Serializable {

    private static final long serialVersionUID = 8799656478674716638L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @ReportField(description = "Nombre")
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 80)
    @Column(name = "nombre", nullable = false, length = 80)
    @Expose
    private String nombre;
    @Column(name = "estado")
    private Boolean estado = true;
    @OneToMany(mappedBy = "codTipoConjunto", fetch = FetchType.LAZY)
    @Expose
    private Collection<CatCiudadela> catCiudadelaCollection;
    @OneToMany(mappedBy = "tipoConjunto", fetch = FetchType.LAZY)
    private Collection<CatPredio> catPredioCollection;

    public CatTipoConjunto() {
    }

    public CatTipoConjunto(Long id) {
        this.id = id;
    }

    public CatTipoConjunto(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
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

    @XmlTransient
    @JsonIgnore
    public Collection<CatCiudadela> getCatCiudadelaCollection() {
        return catCiudadelaCollection;
    }

    public void setCatCiudadelaCollection(Collection<CatCiudadela> catCiudadelaCollection) {
        this.catCiudadelaCollection = catCiudadelaCollection;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<CatPredio> getCatPredioCollection() {
        return catPredioCollection;
    }

    public void setCatPredioCollection(Collection<CatPredio> catPredioCollection) {
        this.catPredioCollection = catPredioCollection;
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
        if (!(object instanceof CatTipoConjunto)) {
            return false;
        }
        CatTipoConjunto other = (CatTipoConjunto) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gob.samborondon.entities.CatTipoConjunto[ id=" + id + " ]";
    }

}
