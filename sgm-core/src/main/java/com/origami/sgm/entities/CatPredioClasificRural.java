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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Report(description = "Clasificación del suelo rural")
@Entity
@Table(name = "cat_predio_clasific_rural", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatPredioClasificRural.findAll", query = "SELECT c FROM CatPredioClasificRural c"),
    @NamedQuery(name = "CatPredioClasificRural.findById", query = "SELECT c FROM CatPredioClasificRural c WHERE c.id = :id"),
    @NamedQuery(name = "CatPredioClasificRural.findByModificado", query = "SELECT c FROM CatPredioClasificRural c WHERE c.modificado = :modificado"),
    @NamedQuery(name = "CatPredioClasificRural.findByEstado", query = "SELECT c FROM CatPredioClasificRural c WHERE c.estado = :estado")})
public class CatPredioClasificRural implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;

    
    @JoinColumn(name = "predio", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @ReportField(description = "Predio", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CatPredio predio;
    @JoinColumn(name = "sector_homogeneo", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Sector Homogéneo", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem sectorHomogeneo;
    @JoinColumn(name = "calidad_suelo", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Calidad del Suelo", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem calidadSuelo;
    @JoinColumn(name = "uso_predio", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Uso del Predio", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem usoPredio;
    @Column(name = "superficie")
    @Expose
    @ReportField(description = "Superficie")
    private BigDecimal superficie;
    @Column(name = "valor_terreno")
    @Expose
    @ReportField(description = "Valor del Terreno")
    private BigDecimal valorTerreno;
    @Column(name = "valor_unitario_hectarea_terreno")
    @Expose
    @ReportField(description = "Valor Unitario por Hectarea")
    private BigDecimal valorUnitarioHectareaTerreno;
    
    
    @Column(name = "observaciones", length = 5000)
    @Expose
    @ReportField(description = "Observaciones Eliminacion")
    private String observaciones;
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
    @Expose
    @ReportField(description = "Usuario Ingreso")
    private String usuario;
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;


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

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public BigDecimal getSuperficie() {
        return superficie;
    }

    public void setSuperficie(BigDecimal superficie) {
        this.superficie = superficie;
    }

    public Date getFecha() {
        return fecha;
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

    public CtlgItem getSectorHomogeneo() {
        return sectorHomogeneo;
    }

    public void setSectorHomogeneo(CtlgItem sectorHomogeneo) {
        this.sectorHomogeneo = sectorHomogeneo;
    }

    public CtlgItem getCalidadSuelo() {
        return calidadSuelo;
    }

    public void setCalidadSuelo(CtlgItem calidadSuelo) {
        this.calidadSuelo = calidadSuelo;
    }

    public CtlgItem getUsoPredio() {
        return usoPredio;
    }

    public void setUsoPredio(CtlgItem usoPredio) {
        this.usoPredio = usoPredio;
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
        if (!(object instanceof CatPredioClasificRural)) {
            return false;
        }
        CatPredioClasificRural other = (CatPredioClasificRural) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CatPredioClasificRural[ id=" + id + " ]";
    }

    public BigDecimal getValorTerreno() {
        return valorTerreno;
    }

    public void setValorTerreno(BigDecimal valorTerreno) {
        this.valorTerreno = valorTerreno;
    }

    public BigDecimal getValorUnitarioHectareaTerreno() {
        return valorUnitarioHectareaTerreno;
    }

    public void setValorUnitarioHectareaTerreno(BigDecimal valorUnitarioHectareaTerreno) {
        this.valorUnitarioHectareaTerreno = valorUnitarioHectareaTerreno;
    }

}
