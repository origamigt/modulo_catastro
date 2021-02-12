/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.google.gson.annotations.Expose;
import com.origami.annotations.Report;
import com.origami.annotations.ReportField;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.enums.FieldType;
import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigDecimal;
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
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author CarlosLoorVargas
 */
@Report(description = "Propietarios Predio")
@Entity
@Table(name = "cat_predio_propietario", schema = SchemasConfig.APP1)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatPredioPropietario.findAll", query = "SELECT c FROM CatPredioPropietario c"),
    @NamedQuery(name = "CatPredioPropietario.findById", query = "SELECT c FROM CatPredioPropietario c WHERE c.id = :id"),
    @NamedQuery(name = "CatPredioPropietario.findByEsResidente", query = "SELECT c FROM CatPredioPropietario c WHERE c.esResidente = :esResidente"),
    @NamedQuery(name = "CatPredioPropietario.findByModificado", query = "SELECT c FROM CatPredioPropietario c WHERE c.modificado = :modificado"),
    @NamedQuery(name = "CatPredioPropietario.findByEstado", query = "SELECT c FROM CatPredioPropietario c INNER JOIN c.predio p INNER JOIN c.ente e WHERE p.id = :idPredio AND e.id = :idEnte AND c.estado = :estado"),
    @NamedQuery(name = "CatPredioPropietario.findPredioByEnte", query = "SELECT c.predio FROM CatPredioPropietario c WHERE c.ente = :ente AND c.estado = 'A'")})
public class CatPredioPropietario implements Serializable {

    private static final long serialVersionUID = 8799656478674716638L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @ReportField(description = "Es Residente")
    @Column(name = "es_residente")
    private Boolean esResidente;
    @Size(max = 20)
    @Column(name = "modificado", length = 20)
    @ReportField(description = "Usuario Modificacion")
    private String modificado;
    @Size(max = 1)
    @Column(name = "estado", length = 1)
    @ReportField(description = "Estado", filter = "A")
    private String estado = "A";
    @Column(name = "usuario", length = 100)
    private String usuario;

    @ReportField(description = "Fecha")
    @Column(name = "fecha")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    @ReportField(description = "Tipo Propietario", type = FieldType.COLLECTION_ONE_TO_ONE)
    @JoinColumn(name = "tipo", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose(serialize = true, deserialize = true)
    private CtlgItem tipo;

    @ReportField(description = "Predio", type = FieldType.COLLECTION_ONE_TO_ONE)
    @JoinColumn(name = "predio", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private CatPredio predio;

    @JoinColumn(name = "ente", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose(serialize = true, deserialize = true)
    private CatEnte ente;
    @ReportField(description = "Porcentaje Posesion")
    @Column(name = "porcentaje_posecion")
    @Expose
    private BigDecimal porcentajePosecion = BigDecimal.ZERO;
    @Column(name = "observacion_coop")
    @Expose
    private String observacionCoop;
    @ReportField(description = "Copropietario")
    @Column(name = "copropietario")
    @Expose
    private Boolean copropietario = false;

    @OneToMany(mappedBy = "propietario")
    private List<CatEscritura> propietarioCollection;
    @Column(name = "observaciones", columnDefinition = SchemasConfig.LONG_TEXT_TYPE)
    @Expose
    private String observaciones;
    @ReportField(description = "Numero de Documento")
    @Column(name = "ciu_ced_ruc", columnDefinition = SchemasConfig.LONG_TEXT_TYPE)
    @Expose
    private String ciuCedRuc;

    @Expose
    @OneToMany(mappedBy = "propietario")
    private List<CatEscrituraPropietario> catEscrituraPropietarioList;

    @ReportField(description = "Nombres")
    @Column(name = "nombres_completos")
    @Expose
    private String nombresCompletos;

    @Transient
    private CiuCiudadano ciudadano;

    @ReportField(description = "Observación coopropietario")
    @Column(name = "otros")
    @Expose
    private String otros;

    public String getNombresCompletos() {
        return nombresCompletos;
    }

    public void setNombresCompletos(String nombresCompletos) {
        this.nombresCompletos = nombresCompletos;
    }

    public CiuCiudadano getCiudadano() {
        return ciudadano;
    }

    public void setCiudadano(CiuCiudadano ciudadano) {
        this.ciudadano = ciudadano;
    }

    public String getOtros() {
        return otros;
    }

    public void setOtros(String otros) {
        this.otros = otros;
    }

    @Transient
    private BigDecimal porcentajeAvalDivision;

    @Transient
    private BigDecimal totalPorcentajeAvalDivision;

    public BigDecimal getPorcentajePosecion() {
        return porcentajePosecion;
    }

    public void setPorcentajePosecion(BigDecimal porcentajePosecion) {
        this.porcentajePosecion = porcentajePosecion;
    }

    public Boolean getCopropietario() {
        return copropietario;
    }

    public void setCopropietario(Boolean copropietario) {
        this.copropietario = copropietario;
    }

    public CatPredioPropietario() {
    }

    public CatPredioPropietario(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEsResidente() {
        return esResidente;
    }

    public void setEsResidente(Boolean esResidente) {
        this.esResidente = esResidente;
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

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public CtlgItem getTipo() {
        return tipo;
    }

    public void setTipo(CtlgItem tipo) {
        this.tipo = tipo;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public CatEnte getEnte() {
        return ente;
    }

    public void setEnte(CatEnte ente) {
        this.ente = ente;
    }

    public List<CatEscritura> getPropietarioCollection() {
        return propietarioCollection;
    }

    public void setPropietarioCollection(List<CatEscritura> propietarioCollection) {
        this.propietarioCollection = propietarioCollection;
    }

    public String getObservacionCoop() {
        return observacionCoop;
    }

    public void setObservacionCoop(String observacionCoop) {
        this.observacionCoop = observacionCoop;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getCiuCedRuc() {
        return ciuCedRuc;
    }

    public void setCiuCedRuc(String ciuCedRuc) {
        this.ciuCedRuc = ciuCedRuc;
    }

    public List<CatEscrituraPropietario> getCatEscrituraPropietarioList() {
        return catEscrituraPropietarioList;
    }

    public void setCatEscrituraPropietarioList(List<CatEscrituraPropietario> catEscrituraPropietarioList) {
        this.catEscrituraPropietarioList = catEscrituraPropietarioList;
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
        if (!(object instanceof CatPredioPropietario)) {
            return false;
        }
        CatPredioPropietario other = (CatPredioPropietario) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    public BigDecimal getPorcentajeAvalDivision() {
        return porcentajeAvalDivision;
    }

    public void setPorcentajeAvalDivision(BigDecimal porcentajeAvalDivision) {
        this.porcentajeAvalDivision = porcentajeAvalDivision;
    }

    public BigDecimal getTotalPorcentajeAvalDivision() {
        return totalPorcentajeAvalDivision;
    }

    public void setTotalPorcentajeAvalDivision(BigDecimal totalPorcentajeAvalDivision) {
        this.totalPorcentajeAvalDivision = totalPorcentajeAvalDivision;
    }

    @Override
    public String toString() {
        return "gob.entities.CatPredioPropietario[ id=" + id + " ]";
    }

}
