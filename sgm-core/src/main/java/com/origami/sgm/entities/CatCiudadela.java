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
import java.math.BigDecimal;
import javax.persistence.Basic;
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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author CarlosLoorVargas
 */
@Report(description = "Espacio urbano / rural", isCatatalog = true, showTree = false)
@Entity
@Table(name = "cat_ciudadela", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatCiudadela.findAll", query = "SELECT c FROM CatCiudadela c"),
    @NamedQuery(name = "CatCiudadela.findById", query = "SELECT c FROM CatCiudadela c WHERE c.id = :id"),
    @NamedQuery(name = "CatCiudadela.findByCodigo", query = "SELECT c FROM CatCiudadela c WHERE c.codigo = :codigo"),
    @NamedQuery(name = "CatCiudadela.findByNombre", query = "SELECT c FROM CatCiudadela c WHERE c.nombre = :nombre"),
    @NamedQuery(name = "CatCiudadela.findByEstado", query = "SELECT c FROM CatCiudadela c WHERE c.estado = :estado")})

public class CatCiudadela implements Serializable {

    @JoinColumn(name = "ubicacion", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CatUbicacion ubicacion;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "fondo_tipo", precision = 15, scale = 2)
    private BigDecimal fondoTipo;
    @Column(name = "frente_tipo", precision = 15, scale = 2)
    private BigDecimal frenteTipo;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @Column(name = "codigo")
    private Short codigo;
    @ReportField(description = "Nombre")
    @Column(name = "nombre", length = 80)
    @Expose
    private String nombre;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "estado")
    private Boolean estado = true;
    @JoinColumn(name = "cod_tipo_conjunto", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CatTipoConjunto codTipoConjunto;
    @JoinColumn(name = "cod_parroquia", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CatParroquia codParroquia;

    public CatCiudadela() {
    }

    public CatCiudadela(Long id) {
        this.id = id;
    }

    public CatCiudadela(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Short getCodigo() {
        return codigo;
    }

    public void setCodigo(Short codigo) {
        this.codigo = codigo;
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

    public CatTipoConjunto getCodTipoConjunto() {
        return codTipoConjunto;
    }

    public void setCodTipoConjunto(CatTipoConjunto codTipoConjunto) {
        this.codTipoConjunto = codTipoConjunto;
    }

    public CatParroquia getCodParroquia() {
        return codParroquia;
    }

    public void setCodParroquia(CatParroquia codParroquia) {
        this.codParroquia = codParroquia;
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
        if (!(object instanceof CatCiudadela)) {
            return false;
        }
        CatCiudadela other = (CatCiudadela) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "" + id + " ";
    }

    public BigDecimal getFondoTipo() {
        return fondoTipo;
    }

    public void setFondoTipo(BigDecimal fondoTipo) {
        this.fondoTipo = fondoTipo;
    }

    public BigDecimal getFrenteTipo() {
        return frenteTipo;
    }

    public void setFrenteTipo(BigDecimal frenteTipo) {
        this.frenteTipo = frenteTipo;
    }

    public CatUbicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(CatUbicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

}
