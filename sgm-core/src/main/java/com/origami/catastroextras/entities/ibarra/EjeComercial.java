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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Angel Navarro
 * @date Jan 26, 2018
 */
@Entity
@Table(name = "eje_comercial_2018_2019", schema = "valoracion")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "EjeComercial.findAll", query = "SELECT e FROM EjeComercial e"),
    @NamedQuery(name = "EjeComercial.findByCodigo", query = "SELECT e FROM EjeComercial e WHERE e.codigo = :codigo"),
    @NamedQuery(name = "EjeComercial.findByValor", query = "SELECT e FROM EjeComercial e WHERE e.valor = :valor"),
    @NamedQuery(name = "EjeComercial.findByDescripcion", query = "SELECT e FROM EjeComercial e WHERE e.descripcion = :descripcion"),
    @NamedQuery(name = "EjeComercial.findByEstado", query = "SELECT e FROM EjeComercial e WHERE e.estado = :estado"),
    @NamedQuery(name = "EjeComercial.findById", query = "SELECT e FROM EjeComercial e WHERE e.id = :id")})

public class EjeComercial implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 5)
    @Column(length = 5)
    private String codigo;
    private BigDecimal valor;
    @Size(max = 100)
    @Column(length = 100)
    private String descripcion;
    @Basic(optional = false)
    @NotNull
    @Column(nullable = false)
    private Integer estado = 1;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Long id;
    @Column(name = "observaciones")
    private String observaciones;

    public EjeComercial() {
    }

    public EjeComercial(Long id) {
        this.id = id;
    }

    public EjeComercial(Long id, Integer estado) {
        this.id = id;
        this.estado = estado;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
        if (!(object instanceof EjeComercial)) {
            return false;
        }
        EjeComercial other = (EjeComercial) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EjeComercial[ id=" + id + " ]";
    }

}
