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
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author OrigamiSolutions
 */
@Report(description = "Tipo de Obra o Inst. Especiales (Obras internas o complementarias)")
@Entity
@Table(name = "cat_predio_obra_interna", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatPredioObraInterna.findAll", query = "SELECT c FROM CatPredioObraInterna c"),
    @NamedQuery(name = "CatPredioObraInterna.findById", query = "SELECT c FROM CatPredioObraInterna c WHERE c.id = :id"),
    @NamedQuery(name = "CatPredioObraInterna.findByModificado", query = "SELECT c FROM CatPredioObraInterna c WHERE c.modificado = :modificado"),
    @NamedQuery(name = "CatPredioObraInterna.findByEstado", query = "SELECT c FROM CatPredioObraInterna c WHERE c.estado = :estado")})
public class CatPredioObraInterna implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;

    @Size(max = 20)
    @Column(name = "modificado", length = 20)
    @Expose
    private String modificado;
    @Size(max = 1)
    @Column(name = "estado", length = 1)
    @Expose
    @ReportField(description = "Estado", filter = "A")
    private String estado = "A";
    @Column(name = "usuario", length = 100)
    @ReportField(description = "Usuario Ingreso")
    private String usuario;
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @JoinColumn(name = "predio", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ReportField(description = "Predio", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CatPredio predio;
    @Column(name = "avaluo", precision = 12, scale = 2)
    @Expose
    @ReportField(description = "Avaluo")
    private BigDecimal avaluo;
    @JoinColumn(name = "tipo", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Tipo de Obra", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tipo;
    @JoinColumn(name = "material", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Material", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem material;
    @Column(name = "area", precision = 12, scale = 2)
    @Expose
    @ReportField(description = "Area(m2)")
    private BigDecimal area;
    @Column(name = "cantidad")
    @Expose
    @ReportField(description = "Cantidad(u)")
    private BigDecimal cantidad;
    @JoinColumn(name = "conservacion", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Estado de Conservación", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem conservacion;
    @JoinColumn(name = "edad", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Edad", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem edad;
    @Column(name = "observaciones", length = 5000)
    @Expose
    @ReportField(description = "Observaciones")
    private String observaciones;
    @Column(name = "altura")
    @Expose
    private BigDecimal altura;

    @JoinColumn(name = "tipo_grafico", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Tipo", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tipoGrafico;
    @JoinColumn(name = "etapa_construccion", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Etapa de Construcción de la Mejora", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem etapaConstruccion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModificado() {
        return modificado;
    }

    public void setModificado(String modificado) {
        this.modificado = modificado;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsuario() {
        return usuario;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public CtlgItem getTipo() {
        return tipo;
    }

    public void setTipo(CtlgItem tipo) {
        this.tipo = tipo;
    }

    public CtlgItem getMaterial() {
        return material;
    }

    public void setMaterial(CtlgItem material) {
        this.material = material;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public CtlgItem getConservacion() {
        return conservacion;
    }

    public void setConservacion(CtlgItem conservacion) {
        this.conservacion = conservacion;
    }

    public CtlgItem getEdad() {
        return edad;
    }

    public void setEdad(CtlgItem edad) {
        this.edad = edad;
    }

    public BigDecimal getAltura() {
        return altura;
    }

    public void setAltura(BigDecimal altura) {
        this.altura = altura;
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
        if (!(object instanceof CatPredioObraInterna)) {
            return false;
        }
        CatPredioObraInterna other = (CatPredioObraInterna) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CatPredioObraInterna[ id=" + id + " ]";
    }

    public BigDecimal getAvaluo() {
        return avaluo;
    }

    public void setAvaluo(BigDecimal avaluo) {
        this.avaluo = avaluo;
    }

    public CtlgItem getTipoGrafico() {
        return tipoGrafico;
    }

    public void setTipoGrafico(CtlgItem tipoGrafico) {
        this.tipoGrafico = tipoGrafico;
    }

    public CtlgItem getEtapaConstruccion() {
        return etapaConstruccion;
    }

    public void setEtapaConstruccion(CtlgItem etapaConstruccion) {
        this.etapaConstruccion = etapaConstruccion;
    }

}
