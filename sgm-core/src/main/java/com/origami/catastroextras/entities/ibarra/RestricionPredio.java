/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 *
 * @author Origami13
 */
@Entity
@Table(name = "restricion_predio", schema = IbarraSchemas.CATASTRO)
@NamedQueries({
    @NamedQuery(name = "RestricionPredio.findAll", query = "SELECT r FROM RestricionPredio r"),
    @NamedQuery(name = "RestricionPredio.findByCodCatastralPredio", query = "SELECT r FROM RestricionPredio r WHERE r.restricionPredioPK.codCatastralPredio = :codCatastralPredio"),
    @NamedQuery(name = "RestricionPredio.findByCodigoRestriccion", query = "SELECT r FROM RestricionPredio r WHERE r.restricionPredioPK.codigoRestriccion = :codigoRestriccion"),
    })
public class RestricionPredio implements Serializable {

    private static final long serialVersionUID = 1L;
    protected RestricionPredioPK restricionPredioPK;
    @Column(name = "predio")
    private BigInteger predio;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "valor")
    private Integer valor;
    @Column(name = "porcentaje_paga")
    private BigDecimal porcentajePaga;
    @Column(name = "porcentaje_nopaga")
    private BigDecimal porcentajeNopaga;
    @Column(name = "exonerado")
    private Integer exonerado;
    @Size(max = 300)
    @Column(name = "comentario", length = 300)
    private String comentario;
    @Size(max = 30)
    @Column(name = "numero_tramite", length = 30)
    private String numeroTramite;
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.DATE)
    private Date fechaInicio;
    @Column(name = "fecha_final")
    @Temporal(TemporalType.DATE)
    private Date fechaFinal;
    @Size(max = 30)
    @Column(name = "usuario_ingreso", length = 30)
    private String usuarioIngreso;
    @Column(name = "fecha_ingreso")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaIngreso;
    @JoinColumn(name = "codigo_restriccion", referencedColumnName = "codigo_restriccion", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Restricciones restricciones;

    public RestricionPredio() {
    }

    public RestricionPredio(RestricionPredioPK restricionPredioPK) {
        this.restricionPredioPK = restricionPredioPK;
    }

    public RestricionPredio(String codCatastralPredio, int codigoRestriccion) {
        this.restricionPredioPK = new RestricionPredioPK(codCatastralPredio, codigoRestriccion);
    }

    public RestricionPredioPK getRestricionPredioPK() {
        return restricionPredioPK;
    }

    public void setRestricionPredioPK(RestricionPredioPK restricionPredioPK) {
        this.restricionPredioPK = restricionPredioPK;
    }

    public BigInteger getPredio() {
        return predio;
    }

    public void setPredio(BigInteger predio) {
        this.predio = predio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }

    public BigDecimal getPorcentajePaga() {
        return porcentajePaga;
    }

    public void setPorcentajePaga(BigDecimal porcentajePaga) {
        this.porcentajePaga = porcentajePaga;
    }

    public BigDecimal getPorcentajeNopaga() {
        return porcentajeNopaga;
    }

    public void setPorcentajeNopaga(BigDecimal porcentajeNopaga) {
        this.porcentajeNopaga = porcentajeNopaga;
    }

    public Integer getExonerado() {
        return exonerado;
    }

    public void setExonerado(Integer exonerado) {
        this.exonerado = exonerado;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getNumeroTramite() {
        return numeroTramite;
    }

    public void setNumeroTramite(String numeroTramite) {
        this.numeroTramite = numeroTramite;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFinal() {
        return fechaFinal;
    }

    public void setFechaFinal(Date fechaFinal) {
        this.fechaFinal = fechaFinal;
    }

    public String getUsuarioIngreso() {
        return usuarioIngreso;
    }

    public void setUsuarioIngreso(String usuarioIngreso) {
        this.usuarioIngreso = usuarioIngreso;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Restricciones getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(Restricciones restricciones) {
        this.restricciones = restricciones;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (restricionPredioPK != null ? restricionPredioPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RestricionPredio)) {
            return false;
        }
        RestricionPredio other = (RestricionPredio) object;
        if ((this.restricionPredioPK == null && other.restricionPredioPK != null) || (this.restricionPredioPK != null && !this.restricionPredioPK.equals(other.restricionPredioPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RestricionPredio[ restricionPredioPK=" + restricionPredioPK + " ]";
    }

}
