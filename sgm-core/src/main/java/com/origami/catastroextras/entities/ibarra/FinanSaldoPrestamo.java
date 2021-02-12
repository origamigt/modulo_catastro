/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
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
 * @author TIC
 */
@Entity
@Table(name = "finan_saldo_prestamo", schema = IbarraSchemas.CATASTRO)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FinanSaldoPrestamo.findAll", query = "SELECT f FROM FinanSaldoPrestamo f"),
    @NamedQuery(name = "FinanSaldoPrestamo.findByCodigoSaldo", query = "SELECT f FROM FinanSaldoPrestamo f WHERE f.codigoSaldo = :codigoSaldo"),
    @NamedQuery(name = "FinanSaldoPrestamo.findByAnio", query = "SELECT f FROM FinanSaldoPrestamo f WHERE f.anio = :anio"),
    @NamedQuery(name = "FinanSaldoPrestamo.findBySaldo", query = "SELECT f FROM FinanSaldoPrestamo f WHERE f.saldo = :saldo")})
public class FinanSaldoPrestamo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "codigo_saldo")
    private Integer codigoSaldo;
    @Size(max = 4)
    @Column(name = "anio")
    private String anio;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "saldo")
    private BigDecimal saldo;
//    @JoinColumn(name = "codigo_prestamo", referencedColumnName = "codigo_prestamo")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private FinanPrestamoPredio codigoPrestamo;

    public FinanSaldoPrestamo() {
    }

    public FinanSaldoPrestamo(Integer codigoSaldo) {
        this.codigoSaldo = codigoSaldo;
    }

    public Integer getCodigoSaldo() {
        return codigoSaldo;
    }

    public void setCodigoSaldo(Integer codigoSaldo) {
        this.codigoSaldo = codigoSaldo;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

//    public FinanPrestamoPredio getCodigoPrestamo() {
//        return codigoPrestamo;
//    }
//
//    public void setCodigoPrestamo(FinanPrestamoPredio codigoPrestamo) {
//        this.codigoPrestamo = codigoPrestamo;
//    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (codigoSaldo != null ? codigoSaldo.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FinanSaldoPrestamo)) {
            return false;
        }
        FinanSaldoPrestamo other = (FinanSaldoPrestamo) object;
        if ((this.codigoSaldo == null && other.codigoSaldo != null) || (this.codigoSaldo != null && !this.codigoSaldo.equals(other.codigoSaldo))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FinanSaldoPrestamo[ codigoSaldo=" + codigoSaldo + " ]";
    }

}
