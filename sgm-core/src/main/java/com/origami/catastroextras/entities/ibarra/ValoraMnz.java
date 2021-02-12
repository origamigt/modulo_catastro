package com.origami.catastroextras.entities.ibarra;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Angel Navarro
 * @date Jan 18, 2018
 */
@Entity
@Table(name = "valora_mnz_2018_2019", schema = "valoracion")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ValoraMnz.findAll", query = "SELECT v FROM ValoraMnz v"),
    @NamedQuery(name = "ValoraMnz.findByClaveTotal", query = "SELECT v FROM ValoraMnz v WHERE v.claveTotal = :claveTotal"),
    @NamedQuery(name = "ValoraMnz.findByParroquia", query = "SELECT v FROM ValoraMnz v WHERE v.parroquia = :parroquia"),
    @NamedQuery(name = "ValoraMnz.findByZona", query = "SELECT v FROM ValoraMnz v WHERE v.zona = :zona"),
    @NamedQuery(name = "ValoraMnz.findBySector", query = "SELECT v FROM ValoraMnz v WHERE v.sector = :sector"),
    @NamedQuery(name = "ValoraMnz.findByManzana", query = "SELECT v FROM ValoraMnz v WHERE v.manzana = :manzana"),
    @NamedQuery(name = "ValoraMnz.findByTotal", query = "SELECT v FROM ValoraMnz v WHERE v.total = :total"),
    @NamedQuery(name = "ValoraMnz.findByNZona", query = "SELECT v FROM ValoraMnz v WHERE v.nZona = :nZona"),
    @NamedQuery(name = "ValoraMnz.findByCostoZona", query = "SELECT v FROM ValoraMnz v WHERE v.costoZona = :costoZona"),
    @NamedQuery(name = "ValoraMnz.findByCosto", query = "SELECT v FROM ValoraMnz v WHERE v.costo = :costo"),
    @NamedQuery(name = "ValoraMnz.findById", query = "SELECT v FROM ValoraMnz v WHERE v.id = :id")})
public class ValoraMnz implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 12)
    @Column(name = "clave_total", length = 12)
    private String claveTotal;
    @Size(max = 4)
    @Column(length = 4)
    private String parroquia;
    @Size(max = 4)
    @Column(length = 4)
    private String zona;
    @Size(max = 4)
    @Column(length = 4)
    private String sector;
    @Size(max = 4)
    @Column(length = 4)
    private String manzana;
    private BigDecimal total;
    @Column(name = "n_zona")
    private Integer nZona;
    @Column(name = "costo_zona")
    private BigDecimal costoZona;
    private BigDecimal costo;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Integer id;
    @Column(name = "observaciones")
    private String observaciones;

    public ValoraMnz() {
    }

    public ValoraMnz(String claveTotal, String parroquia, String zona, String sector, String manzana, BigDecimal total, Integer nZona, BigDecimal costoZona, BigDecimal costo) {
        this.claveTotal = claveTotal;
        this.parroquia = parroquia;
        this.zona = zona;
        this.sector = sector;
        this.manzana = manzana;
        this.total = total;
        this.nZona = nZona;
        this.costoZona = costoZona;
        this.costo = costo;
    }

    public ValoraMnz(Integer id) {
        this.id = id;
    }

    public String getClaveTotal() {
        return claveTotal;
    }

    public void setClaveTotal(String claveTotal) {
        this.claveTotal = claveTotal;
    }

    public String getParroquia() {
        return parroquia;
    }

    public void setParroquia(String parroquia) {
        this.parroquia = parroquia;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getManzana() {
        return manzana;
    }

    public void setManzana(String manzana) {
        this.manzana = manzana;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getNZona() {
        return nZona;
    }

    public void setNZona(Integer nZona) {
        this.nZona = nZona;
    }

    public BigDecimal getCostoZona() {
        return costoZona;
    }

    public void setCostoZona(BigDecimal costoZona) {
        this.costoZona = costoZona;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
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
        if (!(object instanceof ValoraMnz)) {
            return false;
        }
        ValoraMnz other = (ValoraMnz) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ValoraMnz[ id=" + id + " ]";
    }

}
