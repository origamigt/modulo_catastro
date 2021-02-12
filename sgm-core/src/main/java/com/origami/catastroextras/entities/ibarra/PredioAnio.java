/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;

/**
 *
 * @author Angel Navarro
 */
@Entity
@Table(name = "predio_anio", schema = IbarraSchemas.CATASTRO)

public class PredioAnio implements Serializable {

    private static final long serialVersionUID = 1L;
    @Size(max = 18)
    @Column(name = "cod_catastral_predio", length = 18)
    private String codCatastralPredio;
    @Size(max = 18)
    @Column(name = "cod_anterior_predio", length = 18)
    private String codAnteriorPredio;
    @Size(max = 100)
    @Column(name = "calle_predio", length = 100)
    private String callePredio;
    @Size(max = 6)
    @Column(name = "numero_predio", length = 6)
    private String numeroPredio;
    @Size(max = 60)
    @Column(name = "barrio_predio", length = 60)
    private String barrioPredio;
    @Size(max = 100)
    @Column(name = "prop_anterior_predio", length = 100)
    private String propAnteriorPredio;
    @Column(name = "area_total_predio")
    private BigDecimal areaTotalPredio;
    @Size(max = 7)
    @Column(name = "dominio_predio", length = 7)
    private String dominioPredio;
    @Column(name = "frente_princ_predio")
    private BigDecimal frentePrincPredio;
    @Column(name = "fondo_relat_predio")
    private BigDecimal fondoRelatPredio;
    @Column(name = "valor_terreno_predio")
    private BigDecimal valorTerrenoPredio;
    @Column(name = "valor_edific_predio")
    private BigDecimal valorEdificPredio;
    @Column(name = "valor_elemento_valorizable")
    private BigDecimal valorElementoValorizable;
    @Column(name = "valor_comercial_predio")
    private BigDecimal valorComercialPredio;
    @Column(name = "base_imponible_predio")
    private BigDecimal baseImponiblePredio;
    @Size(max = 6)
    @Column(name = "tipo_predio", length = 6)
    private String tipoPredio;
    @Column(name = "propied_hor_predio")
    private BigDecimal propiedHorPredio;
    @Column(name = "pre_ph")
    private Integer prePh;
    @Size(max = 12)
    @Column(name = "cod_div_pol", length = 12)
    private String codDivPol;
    @Column(name = "area_const_predio")
    private BigDecimal areaConstPredio;
    @Column(name = "numero_bloques_predio")
    private Integer numeroBloquesPredio;
    @Size(max = 13)
    @Column(name = "ced_ident_prop", length = 13)
    private String cedIdentProp;
    @Size(max = 30)
    @Column(name = "otros_prop", length = 30)
    private String otrosProp;
    @Size(max = 50)
    @Column(name = "apellidos_ciudadano", length = 50)
    private String apellidosCiudadano;
    @Size(max = 50)
    @Column(name = "nombres_ciudadano", length = 50)
    private String nombresCiudadano;
    @Column(name = "ter_edad_prop")
    private Integer terEdadProp;
    @Column(name = "cod_finan_prestamo")
    private Integer codFinanPrestamo;
    @Column(name = "monto_prestamo")
    private BigDecimal montoPrestamo;
    @Size(max = 2)
    @Column(name = "plazo_prestamo", length = 2)
    private String plazoPrestamo;
    @Column(name = "fecha_consecion_prestamo")
    @Temporal(TemporalType.DATE)
    private Date fechaConsecionPrestamo;
    @Column(name = "porcentaje_exon_prestamo")
    private Integer porcentajeExonPrestamo;
    @Size(max = 4)
    @Column(name = "num_notaria_actual", length = 4)
    private String numNotariaActual;
    @Size(max = 50)
    @Column(name = "fecha_inscripcion_actual", length = 50)
    private String fechaInscripcionActual;
    @Size(max = 40)
    @Column(name = "lugar_inscripcion_actual", length = 40)
    private String lugarInscripcionActual;
    @Size(max = 6)
    @Column(name = "registro_prop_actual", length = 6)
    private String registroPropActual;
    @Size(max = 50)
    @Column(name = "fecha_registro_actual", length = 50)
    private String fechaRegistroActual;
    @Column(name = "escritura_actual")
    private Integer escrituraActual;
    @Size(max = 70)
    @Column(name = "nombre_notario", length = 70)
    private String nombreNotario;
    @Size(max = 4)
    @Column(length = 4)
    private String anio;
    @Size(max = 50)
    @Column(name = "condicion_propietario", length = 50)
    private String condicionPropietario;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(nullable = false)
    private Long id;

    public PredioAnio() {
    }

    public PredioAnio(Long id) {
        this.id = id;
    }

    public String getCodCatastralPredio() {
        return codCatastralPredio;
    }

    public void setCodCatastralPredio(String codCatastralPredio) {
        this.codCatastralPredio = codCatastralPredio;
    }

    public String getCodAnteriorPredio() {
        return codAnteriorPredio;
    }

    public void setCodAnteriorPredio(String codAnteriorPredio) {
        this.codAnteriorPredio = codAnteriorPredio;
    }

    public String getCallePredio() {
        return callePredio;
    }

    public void setCallePredio(String callePredio) {
        this.callePredio = callePredio;
    }

    public String getNumeroPredio() {
        return numeroPredio;
    }

    public void setNumeroPredio(String numeroPredio) {
        this.numeroPredio = numeroPredio;
    }

    public String getBarrioPredio() {
        return barrioPredio;
    }

    public void setBarrioPredio(String barrioPredio) {
        this.barrioPredio = barrioPredio;
    }

    public String getPropAnteriorPredio() {
        return propAnteriorPredio;
    }

    public void setPropAnteriorPredio(String propAnteriorPredio) {
        this.propAnteriorPredio = propAnteriorPredio;
    }

    public BigDecimal getAreaTotalPredio() {
        return areaTotalPredio;
    }

    public void setAreaTotalPredio(BigDecimal areaTotalPredio) {
        this.areaTotalPredio = areaTotalPredio;
    }

    public String getDominioPredio() {
        return dominioPredio;
    }

    public void setDominioPredio(String dominioPredio) {
        this.dominioPredio = dominioPredio;
    }

    public BigDecimal getFrentePrincPredio() {
        return frentePrincPredio;
    }

    public void setFrentePrincPredio(BigDecimal frentePrincPredio) {
        this.frentePrincPredio = frentePrincPredio;
    }

    public BigDecimal getFondoRelatPredio() {
        return fondoRelatPredio;
    }

    public void setFondoRelatPredio(BigDecimal fondoRelatPredio) {
        this.fondoRelatPredio = fondoRelatPredio;
    }

    public BigDecimal getValorTerrenoPredio() {
        return valorTerrenoPredio;
    }

    public void setValorTerrenoPredio(BigDecimal valorTerrenoPredio) {
        this.valorTerrenoPredio = valorTerrenoPredio;
    }

    public BigDecimal getValorEdificPredio() {
        return valorEdificPredio;
    }

    public void setValorEdificPredio(BigDecimal valorEdificPredio) {
        this.valorEdificPredio = valorEdificPredio;
    }

    public BigDecimal getValorElementoValorizable() {
        return valorElementoValorizable;
    }

    public void setValorElementoValorizable(BigDecimal valorElementoValorizable) {
        this.valorElementoValorizable = valorElementoValorizable;
    }

    public BigDecimal getValorComercialPredio() {
        return valorComercialPredio;
    }

    public void setValorComercialPredio(BigDecimal valorComercialPredio) {
        this.valorComercialPredio = valorComercialPredio;
    }

    public BigDecimal getBaseImponiblePredio() {
        return baseImponiblePredio;
    }

    public void setBaseImponiblePredio(BigDecimal baseImponiblePredio) {
        this.baseImponiblePredio = baseImponiblePredio;
    }

    public String getTipoPredio() {
        return tipoPredio;
    }

    public void setTipoPredio(String tipoPredio) {
        this.tipoPredio = tipoPredio;
    }

    public BigDecimal getPropiedHorPredio() {
        return propiedHorPredio;
    }

    public void setPropiedHorPredio(BigDecimal propiedHorPredio) {
        this.propiedHorPredio = propiedHorPredio;
    }

    public Integer getPrePh() {
        return prePh;
    }

    public void setPrePh(Integer prePh) {
        this.prePh = prePh;
    }

    public String getCodDivPol() {
        return codDivPol;
    }

    public void setCodDivPol(String codDivPol) {
        this.codDivPol = codDivPol;
    }

    public BigDecimal getAreaConstPredio() {
        return areaConstPredio;
    }

    public void setAreaConstPredio(BigDecimal areaConstPredio) {
        this.areaConstPredio = areaConstPredio;
    }

    public Integer getNumeroBloquesPredio() {
        return numeroBloquesPredio;
    }

    public void setNumeroBloquesPredio(Integer numeroBloquesPredio) {
        this.numeroBloquesPredio = numeroBloquesPredio;
    }

    public String getCedIdentProp() {
        return cedIdentProp;
    }

    public void setCedIdentProp(String cedIdentProp) {
        this.cedIdentProp = cedIdentProp;
    }

    public String getOtrosProp() {
        return otrosProp;
    }

    public void setOtrosProp(String otrosProp) {
        this.otrosProp = otrosProp;
    }

    public String getApellidosCiudadano() {
        return apellidosCiudadano;
    }

    public void setApellidosCiudadano(String apellidosCiudadano) {
        this.apellidosCiudadano = apellidosCiudadano;
    }

    public String getNombresCiudadano() {
        return nombresCiudadano;
    }

    public void setNombresCiudadano(String nombresCiudadano) {
        this.nombresCiudadano = nombresCiudadano;
    }

    public Integer getTerEdadProp() {
        return terEdadProp;
    }

    public void setTerEdadProp(Integer terEdadProp) {
        this.terEdadProp = terEdadProp;
    }

    public Integer getCodFinanPrestamo() {
        return codFinanPrestamo;
    }

    public void setCodFinanPrestamo(Integer codFinanPrestamo) {
        this.codFinanPrestamo = codFinanPrestamo;
    }

    public BigDecimal getMontoPrestamo() {
        return montoPrestamo;
    }

    public void setMontoPrestamo(BigDecimal montoPrestamo) {
        this.montoPrestamo = montoPrestamo;
    }

    public String getPlazoPrestamo() {
        return plazoPrestamo;
    }

    public void setPlazoPrestamo(String plazoPrestamo) {
        this.plazoPrestamo = plazoPrestamo;
    }

    public Date getFechaConsecionPrestamo() {
        return fechaConsecionPrestamo;
    }

    public void setFechaConsecionPrestamo(Date fechaConsecionPrestamo) {
        this.fechaConsecionPrestamo = fechaConsecionPrestamo;
    }

    public Integer getPorcentajeExonPrestamo() {
        return porcentajeExonPrestamo;
    }

    public void setPorcentajeExonPrestamo(Integer porcentajeExonPrestamo) {
        this.porcentajeExonPrestamo = porcentajeExonPrestamo;
    }

    public String getNumNotariaActual() {
        return numNotariaActual;
    }

    public void setNumNotariaActual(String numNotariaActual) {
        this.numNotariaActual = numNotariaActual;
    }

    public String getFechaInscripcionActual() {
        return fechaInscripcionActual;
    }

    public void setFechaInscripcionActual(String fechaInscripcionActual) {
        this.fechaInscripcionActual = fechaInscripcionActual;
    }

    public String getLugarInscripcionActual() {
        return lugarInscripcionActual;
    }

    public void setLugarInscripcionActual(String lugarInscripcionActual) {
        this.lugarInscripcionActual = lugarInscripcionActual;
    }

    public String getRegistroPropActual() {
        return registroPropActual;
    }

    public void setRegistroPropActual(String registroPropActual) {
        this.registroPropActual = registroPropActual;
    }

    public String getFechaRegistroActual() {
        return fechaRegistroActual;
    }

    public void setFechaRegistroActual(String fechaRegistroActual) {
        this.fechaRegistroActual = fechaRegistroActual;
    }

    public Integer getEscrituraActual() {
        return escrituraActual;
    }

    public void setEscrituraActual(Integer escrituraActual) {
        this.escrituraActual = escrituraActual;
    }

    public String getNombreNotario() {
        return nombreNotario;
    }

    public void setNombreNotario(String nombreNotario) {
        this.nombreNotario = nombreNotario;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public String getCondicionPropietario() {
        return condicionPropietario;
    }

    public void setCondicionPropietario(String condicionPropietario) {
        this.condicionPropietario = condicionPropietario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(object instanceof PredioAnio)) {
            return false;
        }
        PredioAnio other = (PredioAnio) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PredioAnio[ id=" + id + " ]";
    }

}
