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
import java.math.BigInteger;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author CarlosLoorVargas
 */
@Report(description = "Catalogo", isCatatalog = true)
@Entity
@Table(name = "ctlg_item", schema = SchemasConfig.APP1, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"catalogo", "codename"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CtlgItem.findAll", query = "SELECT c FROM CtlgItem c"),
    @NamedQuery(name = "CtlgItem.findById", query = "SELECT c FROM CtlgItem c WHERE c.id = :id"),
    @NamedQuery(name = "CtlgItem.findByValor", query = "SELECT c FROM CtlgItem c WHERE c.valor = :valor"),
    @NamedQuery(name = "CtlgItem.findByCodename", query = "SELECT c FROM CtlgItem c WHERE c.codename = :codename")})
@SequenceGenerator(name = "ctlg_item_id_seq", sequenceName = SchemasConfig.APP1 + ".ctlg_item_id_seq", allocationSize = 1)
public class CtlgItem implements Serializable {

    private static final long serialVersionUID = 8799656478674716638L;

    @Column(name = "referencia", columnDefinition = "bigint")
    private BigInteger referencia;
    @Column(name = "padre", columnDefinition = "bigint")
    @Expose
    private BigInteger padre;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ctlg_item_id_seq")
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @ReportField(description = "Descripcion")
    @Basic(optional = false)
    @NotNull
    @Column(name = "valor", nullable = false)
    @Expose(serialize = true, deserialize = true)
    private String valor;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40)
    @Column(name = "codename", nullable = false, length = 40)
    @Expose
    private String codename;
    @ReportField(description = "Factor")
    @Column(name = "factor", precision = 10, scale = 4)
    private BigDecimal factor;
    @Column(name = "rango_desde", precision = 19, scale = 5)
    private BigDecimal rangoDesde;
    @Column(name = "rango_hasta", precision = 19, scale = 5)
    private BigDecimal rangoHasta;
    @ReportField(description = "Orden")
    @Column(name = "orden")
    @Expose
    private Integer orden;
    @Column(name = "hijo", columnDefinition = "bigint")
    @Expose
    private BigInteger hijo;
    @Column(name = "is_default")
    private Boolean isDefault = false;
    @JoinColumn(name = "catalogo", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CtlgCatalogo catalogo;

    @ManyToMany(mappedBy = "accesibilidadList", fetch = FetchType.LAZY)
    private Collection<CatPredioS4> catPredioS4Collection;
    @ManyToMany(mappedBy = "ctlgItemCollection", fetch = FetchType.LAZY)
    private Collection<CatPredioS6> catPredioS6Collection;

    public CtlgItem() {
    }

    public CtlgItem(Long id) {
        this.id = id;
    }

    public CtlgItem(Long id, String valor, String codename) {
        this.id = id;
        this.valor = valor;
        this.codename = codename;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public CtlgCatalogo getCatalogo() {
        return catalogo;
    }

    public void setCatalogo(CtlgCatalogo catalogo) {
        this.catalogo = catalogo;
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
        if (!(object instanceof CtlgItem)) {
            return false;
        }
        CtlgItem other = (CtlgItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return orden != null ? orden.toString() : "";
    }

    public BigInteger getReferencia() {
        return referencia;
    }

    public void setReferencia(BigInteger referencia) {
        this.referencia = referencia;
    }

    public BigInteger getPadre() {
        return padre;
    }

    public void setPadre(BigInteger padre) {
        this.padre = padre;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public BigDecimal getRangoDesde() {
        return rangoDesde;
    }

    public void setRangoDesde(BigDecimal rangoDesde) {
        this.rangoDesde = rangoDesde;
    }

    public BigDecimal getRangoHasta() {
        return rangoHasta;
    }

    public void setRangoHasta(BigDecimal rangoHasta) {
        this.rangoHasta = rangoHasta;
    }

    public BigInteger getHijo() {
        return hijo;
    }

    public void setHijo(BigInteger hijo) {
        this.hijo = hijo;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Collection<CatPredioS4> getCatPredioS4Collection() {
        return catPredioS4Collection;
    }

    public void setCatPredioS4Collection(Collection<CatPredioS4> catPredioS4Collection) {
        this.catPredioS4Collection = catPredioS4Collection;
    }

    public Collection<CatPredioS6> getCatPredioS6Collection() {
        return catPredioS6Collection;
    }

    public void setCatPredioS6Collection(Collection<CatPredioS6> catPredioS6Collection) {
        this.catPredioS6Collection = catPredioS6Collection;
    }

}
