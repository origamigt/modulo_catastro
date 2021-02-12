/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.google.gson.annotations.Expose;
import com.origami.annotations.Report;
import com.origami.annotations.ReportField;
import com.origami.enums.FieldType;
import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
import org.hibernate.annotations.OrderBy;

/**
 *
 * @author CarlosLoorVargas
 */
@Report(description = "Característica")
@Entity
@Table(name = "cat_edf_prop", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatEdfProp.findAll", query = "SELECT c FROM CatEdfProp c"),
    @NamedQuery(name = "CatEdfProp.findById", query = "SELECT c FROM CatEdfProp c WHERE c.id = :id"),
    @NamedQuery(name = "CatEdfProp.findByNombre", query = "SELECT c FROM CatEdfProp c WHERE c.nombre = :nombre"),
    @NamedQuery(name = "CatEdfProp.findByPeso", query = "SELECT c FROM CatEdfProp c WHERE c.peso = :peso")})
public class CatEdfProp implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @ReportField(description = "Nombre caracteristica")
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(name = "nombre", nullable = false, length = 60)
    @Expose
    private String nombre;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "peso", precision = 15, scale = 5)
    @ReportField(description = "Factor caracteristica")
    private BigDecimal peso;
    @ReportField(description = "Orden caracteristica")
    @Column(name = "orden", columnDefinition = "bigint")
    @Expose
    private BigInteger orden;
    @Column(name = "tipo_estruc")
    @Expose
    private String tipoEstruc;
    @ReportField(description = "Categoria", type = FieldType.COLLECTION_ONE_TO_ONE)
    @Expose
    @JoinColumn(name = "categoria", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @OrderBy(clause = "gui_orden ASC")
    private CatEdfCategProp categoria;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "prop", fetch = FetchType.LAZY)
    private Collection<CatPredioEdificacionProp> catPredioEdificacionPropCollection;

    public CatEdfProp() {
    }

    public CatEdfProp(Long id) {
        this.id = id;
    }

    public CatEdfProp(Long id, String nombre) {
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

    public BigDecimal getPeso() {
        return peso;
    }

    public void setPeso(BigDecimal peso) {
        this.peso = peso;
    }

    public CatEdfCategProp getCategoria() {
        return categoria;
    }

    public void setCategoria(CatEdfCategProp categoria) {
        this.categoria = categoria;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<CatPredioEdificacionProp> getCatPredioEdificacionPropCollection() {
        return catPredioEdificacionPropCollection;
    }

    public void setCatPredioEdificacionPropCollection(Collection<CatPredioEdificacionProp> catPredioEdificacionPropCollection) {
        this.catPredioEdificacionPropCollection = catPredioEdificacionPropCollection;
    }

    public BigInteger getOrden() {
        return orden;
    }

    public void setOrden(BigInteger orden) {
        this.orden = orden;
    }

    public String getTipoEstruc() {
        return tipoEstruc;
    }

    public void setTipoEstruc(String tipoEstruc) {
        this.tipoEstruc = tipoEstruc;
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
        if (!(object instanceof CatEdfProp)) {
            return false;
        }
        CatEdfProp other = (CatEdfProp) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gob.samborondon.entities.CatEdfProp[ id=" + id + " ]";
    }

}
