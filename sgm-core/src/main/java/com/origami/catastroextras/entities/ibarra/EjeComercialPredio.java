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
@Table(name = "eje_comercial_predio_2018_2019", schema = "valoracion")
@XmlRootElement
public class EjeComercialPredio implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "cod_catastral_predio")
    private String codCatastralPredio;
    @Size(max = 18)
    @Column(name = "codigo_eje_comercial", length = 18)
    private String codigoEjeComercial;
    @Column(name = "afectacion_predio")
    private BigDecimal afectacionPredio;
    private Integer estado;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Long id;
    @Column(name = "observaciones")
    private String observaciones;

    public EjeComercialPredio() {
    }

    public EjeComercialPredio(Long id) {
        this.id = id;
    }

    public String getCodCatastralPredio() {
        return codCatastralPredio;
    }

    public void setCodCatastralPredio(String codCatastralPredio) {
        this.codCatastralPredio = codCatastralPredio;
    }

    public String getCodigoEjeComercial() {
        return codigoEjeComercial;
    }

    public void setCodigoEjeComercial(String codigoEjeComercial) {
        this.codigoEjeComercial = codigoEjeComercial;
    }

    public BigDecimal getAfectacionPredio() {
        return afectacionPredio;
    }

    public void setAfectacionPredio(BigDecimal afectacionPredio) {
        this.afectacionPredio = afectacionPredio;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        if (!(object instanceof EjeComercialPredio)) {
            return false;
        }
        EjeComercialPredio other = (EjeComercialPredio) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EjeComercialPredio[ id=" + id + " ]";
    }

}
