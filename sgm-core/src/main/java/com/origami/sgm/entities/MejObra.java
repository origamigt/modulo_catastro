/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Henry Pilco
 */
@Entity
@Table(name = "mej_obra", schema = SchemasConfig.MEJORAS)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MejObra.findAll", query = "SELECT m FROM MejObra m")})
public class MejObra implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "anio")
    private Long anio;
    @Size(max = 2147483647)
    @Column(name = "concepto")
    private String concepto;
    @Size(max = 150)
    @Column(name = "base_legal")
    private String baseLegal;
    @Size(max = 80)
    @Column(name = "cuenta_contable")
    private String cuentaContable;
    @JoinColumn(name = "ubicacion", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CatUbicacion ubicacion;
    @Column(name = "fecha_conclusion_obra")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaConclusionObra;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "costo_total")
    private BigDecimal costoTotal;
    @Column(name = "subsidio")
    private BigDecimal subsidio;
    @Column(name = "valor_subcidiado")
    private BigDecimal valorSubcidiado;
    @Column(name = "valor_recuperar")
    private BigDecimal valorRecuperar;
    @Column(name = "plazo")
    private BigInteger plazo;
    @Column(name = "valor_emision_anual")
    private BigDecimal valorEmisionAnual;
    @JoinColumn(name = "tipo_obra", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private MejTipoObra tipoObra;
    @OneToMany(mappedBy = "obra", fetch = FetchType.LAZY)
    private Collection<MejValoresObraUbicacion> valoresObraUbicacionsCollection;
    @OneToMany(mappedBy = "mejora", fetch = FetchType.LAZY)
    private List<MejObraUbicacion> mejObraUbicacions;

    public MejObra() {
    }

    public MejObra(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAnio() {
        return anio;
    }

    public void setAnio(Long anio) {
        this.anio = anio;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public String getBaseLegal() {
        return baseLegal;
    }

    public void setBaseLegal(String baseLegal) {
        this.baseLegal = baseLegal;
    }

    public String getCuentaContable() {
        return cuentaContable;
    }

    public void setCuentaContable(String cuentaContable) {
        this.cuentaContable = cuentaContable;
    }

    public CatUbicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(CatUbicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Date getFechaConclusionObra() {
        return fechaConclusionObra;
    }

    public void setFechaConclusionObra(Date fechaConclusionObra) {
        this.fechaConclusionObra = fechaConclusionObra;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public BigDecimal getSubsidio() {
        return subsidio;
    }

    public void setSubsidio(BigDecimal subsidio) {
        this.subsidio = subsidio;
    }

    public BigDecimal getValorSubcidiado() {
        return valorSubcidiado;
    }

    public void setValorSubcidiado(BigDecimal valorSubcidiado) {
        this.valorSubcidiado = valorSubcidiado;
    }

    public BigDecimal getValorRecuperar() {
        return valorRecuperar;
    }

    public void setValorRecuperar(BigDecimal valorRecuperar) {
        this.valorRecuperar = valorRecuperar;
    }

    public BigInteger getPlazo() {
        return plazo;
    }

    public void setPlazo(BigInteger plazo) {
        this.plazo = plazo;
    }

    public BigDecimal getValorEmisionAnual() {
        return valorEmisionAnual;
    }

    public void setValorEmisionAnual(BigDecimal valorEmisionAnual) {
        this.valorEmisionAnual = valorEmisionAnual;
    }

    public MejTipoObra getTipoObra() {
        return tipoObra;
    }

    public void setTipoObra(MejTipoObra tipoObra) {
        this.tipoObra = tipoObra;
    }

    public Collection<MejValoresObraUbicacion> getValoresObraUbicacionsCollection() {
        return valoresObraUbicacionsCollection;
    }

    public void setValoresObraUbicacionsCollection(Collection<MejValoresObraUbicacion> valoresObraUbicacionsCollection) {
        this.valoresObraUbicacionsCollection = valoresObraUbicacionsCollection;
    }

    public List<MejObraUbicacion> getMejObraUbicacions() {
        return mejObraUbicacions;
    }

    public void setMejObraUbicacions(List<MejObraUbicacion> mejObraUbicacions) {
        this.mejObraUbicacions = mejObraUbicacions;
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
        if (!(object instanceof MejObra)) {
            return false;
        }
        MejObra other = (MejObra) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MejObra[ id=" + id + " ]";
    }

}
