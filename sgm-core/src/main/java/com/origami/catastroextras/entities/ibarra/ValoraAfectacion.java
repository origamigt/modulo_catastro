/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Angel Navarro
 * @date Jan 18, 2018
 */
@Entity
@Table(name = "valora_afectacion_2018_2019", schema = "valoracion")
@XmlRootElement
public class ValoraAfectacion implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "cod_catastral_predio")
    private String codCatastralPredio;
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
    @Column(name = "valor_calculado")
    private BigDecimal valorCalculado;
    private BigDecimal afectacion;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Long id;
    private BigDecimal costo;
    @Column(name = "observaciones")
    private String observaciones;

    public ValoraAfectacion() {
    }

    public ValoraAfectacion(Long id) {
        this.id = id;
    }

    public String getCodCatastralPredio() {
        return this.codCatastralPredio;
    }

    public void setCodCatastralPredio(String codCatastralPredio) {
        this.codCatastralPredio = codCatastralPredio;
    }

    public String getParroquia() {
        return this.parroquia;
    }

    public void setParroquia(String parroquia) {
        this.parroquia = parroquia;
    }

    public String getZona() {
        return this.zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getSector() {
        return this.sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getManzana() {
        return this.manzana;
    }

    public void setManzana(String manzana) {
        this.manzana = manzana;
    }

    public BigDecimal getValorCalculado() {
        return this.valorCalculado;
    }

    public void setValorCalculado(BigDecimal valorCalculado) {
        this.valorCalculado = valorCalculado;
    }

    public BigDecimal getAfectacion() {
        return this.afectacion;
    }

    public void setAfectacion(BigDecimal afectacion) {
        this.afectacion = afectacion;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCosto() {
        return this.costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
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
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ValoraAfectacion)) {
            return false;
        }
        ValoraAfectacion other = (ValoraAfectacion) object;
        if (((this.id == null) && (other.id != null)) || ((this.id != null) && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ValoraAfectacion[ id=" + this.id + " ]";
    }

}
