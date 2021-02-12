/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.entities.ibarra;

import com.origami.extras.util.IbarraSchemas;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Where;

/**
 *
 * @author Origami13
 */
@Entity
@Table(schema = IbarraSchemas.PCIUDADANO, name = "ciu_ciudadano", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ciu_codigo"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CiuCiudadano.findAll", query = "SELECT c FROM CiuCiudadano c"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCodigo", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCodigo = :ciuCodigo"),
    @NamedQuery(name = "CiuCiudadano.findByCiuTipoIdentificacion", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuTipoIdentificacion = :ciuTipoIdentificacion"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCedRuc", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCedRuc = :ciuCedRuc"),
    @NamedQuery(name = "CiuCiudadano.findByCiuApellidoPat", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuApellidoPat = :ciuApellidoPat"),
    @NamedQuery(name = "CiuCiudadano.findByCiuApellidoMat", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuApellidoMat = :ciuApellidoMat"),
    @NamedQuery(name = "CiuCiudadano.findByCiuPrimerNombre", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuPrimerNombre = :ciuPrimerNombre"),
    @NamedQuery(name = "CiuCiudadano.findByCiuSegundoNombre", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuSegundoNombre = :ciuSegundoNombre"),
    @NamedQuery(name = "CiuCiudadano.findByCiuNombreCompleto", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuNombreCompleto = :ciuNombreCompleto"),
    @NamedQuery(name = "CiuCiudadano.findByCiuFechaNacimiento", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuFechaNacimiento = :ciuFechaNacimiento"),
    @NamedQuery(name = "CiuCiudadano.findByCiuEstadoCivil", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuEstadoCivil = :ciuEstadoCivil"),
    @NamedQuery(name = "CiuCiudadano.findByCiuPais", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuPais = :ciuPais"),
    @NamedQuery(name = "CiuCiudadano.findByCiuSexo", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuSexo = :ciuSexo"),
    @NamedQuery(name = "CiuCiudadano.findByCiuEstado", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuEstado = :ciuEstado"),
    @NamedQuery(name = "CiuCiudadano.findByCiuTipo", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuTipo = :ciuTipo"),
    @NamedQuery(name = "CiuCiudadano.findByCiuFechaDefuncion", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuFechaDefuncion = :ciuFechaDefuncion"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCapacidadDiferente", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCapacidadDiferente = :ciuCapacidadDiferente"),
    @NamedQuery(name = "CiuCiudadano.findByCiuObservaciones", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuObservaciones = :ciuObservaciones"),
    @NamedQuery(name = "CiuCiudadano.findByCiuFechaIngreso", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuFechaIngreso = :ciuFechaIngreso"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCerVotacion", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCerVotacion = :ciuCerVotacion"),
    @NamedQuery(name = "CiuCiudadano.findByCiuPorcentajeDiscapacidad", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuPorcentajeDiscapacidad = :ciuPorcentajeDiscapacidad"),
    @NamedQuery(name = "CiuCiudadano.findByCiuDescripcionDiscapacidad", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuDescripcionDiscapacidad = :ciuDescripcionDiscapacidad"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCuidadosDiscapacidad", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCuidadosDiscapacidad = :ciuCuidadosDiscapacidad"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCarnetDiscapacidad", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCarnetDiscapacidad = :ciuCarnetDiscapacidad"),
    @NamedQuery(name = "CiuCiudadano.findByCiuFechaDiscapacidad", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuFechaDiscapacidad = :ciuFechaDiscapacidad"),
    @NamedQuery(name = "CiuCiudadano.findByCiuIdRepresentante", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuIdRepresentante = :ciuIdRepresentante"),
    @NamedQuery(name = "CiuCiudadano.findByCiuNombreRepresentante", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuNombreRepresentante = :ciuNombreRepresentante"),
    @NamedQuery(name = "CiuCiudadano.findByCiuFechaConstitucion", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuFechaConstitucion = :ciuFechaConstitucion"),
    @NamedQuery(name = "CiuCiudadano.findByCiuFolio", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuFolio = :ciuFolio"),
    @NamedQuery(name = "CiuCiudadano.findByCiuEmail", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuEmail = :ciuEmail"),
    @NamedQuery(name = "CiuCiudadano.findByCiuMovil", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuMovil = :ciuMovil"),
    @NamedQuery(name = "CiuCiudadano.findByCiuDocumento", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuDocumento = :ciuDocumento"),
    @NamedQuery(name = "CiuCiudadano.findByCiuGenero", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuGenero = :ciuGenero"),
    @NamedQuery(name = "CiuCiudadano.findByCiuCuidaDiscapacitado", query = "SELECT c FROM CiuCiudadano c WHERE c.ciuCuidaDiscapacitado = :ciuCuidaDiscapacitado")})
public class CiuCiudadano implements Serializable {

    private static final long serialVersionUID = 1L;
    @Basic(optional = false)
    @Column(name = "ciu_codigo", nullable = false)
    private Integer ciuCodigo;
    @Column(name = "ciu_tipo_identificacion")
    private Character ciuTipoIdentificacion;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 13)
    @Column(name = "ciu_ced_ruc", nullable = false, length = 13)
    private String ciuCedRuc;
    @Size(max = 50)
    @Column(name = "ciu_apellido_pat", length = 50)
    private String ciuApellidoPat;
    @Size(max = 50)
    @Column(name = "ciu_apellido_mat", length = 50)
    private String ciuApellidoMat;
    @Size(max = 50)
    @Column(name = "ciu_primer_nombre", length = 50)
    private String ciuPrimerNombre;
    @Size(max = 50)
    @Column(name = "ciu_segundo_nombre", length = 50)
    private String ciuSegundoNombre;
    @Size(max = 150)
    @Column(name = "ciu_nombre_completo", length = 150)
    private String ciuNombreCompleto;
    @Column(name = "ciu_fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date ciuFechaNacimiento;
    @Size(max = 1)
    @Column(name = "ciu_estado_civil", length = 1)
    private String ciuEstadoCivil;
    @Size(max = 3)
    @Column(name = "ciu_pais", length = 3)
    private String ciuPais;
    @Size(max = 1)
    @Column(name = "ciu_sexo", length = 1)
    private String ciuSexo;
    @Column(name = "ciu_estado")
    private Character ciuEstado;
    @Size(max = 2)
    @Column(name = "ciu_tipo", length = 2)
    private String ciuTipo;
    @Column(name = "ciu_fecha_defuncion")
    @Temporal(TemporalType.DATE)
    private Date ciuFechaDefuncion;
    @Column(name = "ciu_capacidad_diferente")
    private Character ciuCapacidadDiferente;
    @Size(max = 100)
    @Column(name = "ciu_observaciones", length = 100)
    private String ciuObservaciones;
    @Column(name = "ciu_fecha_ingreso")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ciuFechaIngreso;
    @Size(max = 20)
    @Column(name = "ciu_cer_votacion", length = 20)
    private String ciuCerVotacion;
    @Column(name = "ciu_porcentaje_discapacidad")
    private Integer ciuPorcentajeDiscapacidad;
    @Size(max = 50)
    @Column(name = "ciu_descripcion_discapacidad", length = 50)
    private String ciuDescripcionDiscapacidad;
    @Size(max = 13)
    @Column(name = "ciu_cuidados_discapacidad", length = 13)
    private String ciuCuidadosDiscapacidad;
    @Size(max = 15)
    @Column(name = "ciu_carnet_discapacidad", length = 15)
    private String ciuCarnetDiscapacidad;
    @Column(name = "ciu_fecha_discapacidad")
    @Temporal(TemporalType.DATE)
    private Date ciuFechaDiscapacidad;
    @Size(max = 13)
    @Column(name = "ciu_id_representante", length = 13)
    private String ciuIdRepresentante;
    @Size(max = 150)
    @Column(name = "ciu_nombre_representante", length = 150)
    private String ciuNombreRepresentante;
    @Column(name = "ciu_fecha_constitucion")
    @Temporal(TemporalType.DATE)
    private Date ciuFechaConstitucion;
    @Column(name = "ciu_folio")
    private Integer ciuFolio;
    @Size(max = 100)
    @Column(name = "ciu_email", length = 100)
    private String ciuEmail;
    @Size(max = 13)
    @Column(name = "ciu_movil", length = 13)
    private String ciuMovil;
    @Size(max = 250)
    @Column(name = "ciu_documento", length = 250)
    private String ciuDocumento;
    @Size(max = 1)
    @Column(name = "ciu_genero", length = 1)
    private String ciuGenero;
    @Column(name = "ciu_cuida_discapacitado")
    private Character ciuCuidaDiscapacitado;
    @OneToMany(mappedBy = "diCiudadano")
    private List<CiuDireccion> ciuDireccionList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ciuCiudadano")
    private List<CiuRelaciones> ciuRelacionesList;
    @OneToMany(mappedBy = "teCiudadano")
    @Where(clause = "te_estado = 'V'")
    private List<CiuTelefono> ciuTelefonoList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ciuCiudadano")
    @Where(clause = "rp_estado = 'V'")
    private List<CiuReferenciasPersonal> ciuReferenciasPersonalList;

    public CiuCiudadano() {
    }

    public CiuCiudadano(String ciuCedRuc) {
        this.ciuCedRuc = ciuCedRuc;
    }

    public CiuCiudadano(String ciuCedRuc, Integer ciuCodigo) {
        this.ciuCedRuc = ciuCedRuc;
        this.ciuCodigo = ciuCodigo;
    }

    public Integer getCiuCodigo() {
        return ciuCodigo;
    }

    public void setCiuCodigo(Integer ciuCodigo) {
        this.ciuCodigo = ciuCodigo;
    }

    public Character getCiuTipoIdentificacion() {
        return ciuTipoIdentificacion;
    }

    public void setCiuTipoIdentificacion(Character ciuTipoIdentificacion) {
        this.ciuTipoIdentificacion = ciuTipoIdentificacion;
    }

    public String getCiuCedRuc() {
        return ciuCedRuc;
    }

    public void setCiuCedRuc(String ciuCedRuc) {
        this.ciuCedRuc = ciuCedRuc;
    }

    public String getCiuApellidoPat() {
        return ciuApellidoPat;
    }

    public void setCiuApellidoPat(String ciuApellidoPat) {
        this.ciuApellidoPat = ciuApellidoPat;
    }

    public String getCiuApellidoMat() {
        return ciuApellidoMat;
    }

    public void setCiuApellidoMat(String ciuApellidoMat) {
        this.ciuApellidoMat = ciuApellidoMat;
    }

    public String getCiuPrimerNombre() {
        return ciuPrimerNombre;
    }

    public void setCiuPrimerNombre(String ciuPrimerNombre) {
        this.ciuPrimerNombre = ciuPrimerNombre;
    }

    public String getCiuSegundoNombre() {
        return ciuSegundoNombre;
    }

    public void setCiuSegundoNombre(String ciuSegundoNombre) {
        this.ciuSegundoNombre = ciuSegundoNombre;
    }

    public String getCiuNombreCompleto() {
        return ciuNombreCompleto;
    }

    public void setCiuNombreCompleto(String ciuNombreCompleto) {
        this.ciuNombreCompleto = ciuNombreCompleto;
    }

    public Date getCiuFechaNacimiento() {
        return ciuFechaNacimiento;
    }

    public void setCiuFechaNacimiento(Date ciuFechaNacimiento) {
        this.ciuFechaNacimiento = ciuFechaNacimiento;
    }

    public String getCiuEstadoCivil() {
        return ciuEstadoCivil;
    }

    public void setCiuEstadoCivil(String ciuEstadoCivil) {
        this.ciuEstadoCivil = ciuEstadoCivil;
    }

    public String getCiuPais() {
        return ciuPais;
    }

    public void setCiuPais(String ciuPais) {
        this.ciuPais = ciuPais;
    }

    public String getCiuSexo() {
        return ciuSexo;
    }

    public void setCiuSexo(String ciuSexo) {
        this.ciuSexo = ciuSexo;
    }

    public Character getCiuEstado() {
        return ciuEstado;
    }

    public void setCiuEstado(Character ciuEstado) {
        this.ciuEstado = ciuEstado;
    }

    public String getCiuTipo() {
        return ciuTipo;
    }

    public void setCiuTipo(String ciuTipo) {
        this.ciuTipo = ciuTipo;
    }

    public Date getCiuFechaDefuncion() {
        return ciuFechaDefuncion;
    }

    public void setCiuFechaDefuncion(Date ciuFechaDefuncion) {
        this.ciuFechaDefuncion = ciuFechaDefuncion;
    }

    public Character getCiuCapacidadDiferente() {
        return ciuCapacidadDiferente;
    }

    public void setCiuCapacidadDiferente(Character ciuCapacidadDiferente) {
        this.ciuCapacidadDiferente = ciuCapacidadDiferente;
    }

    public String getCiuObservaciones() {
        return ciuObservaciones;
    }

    public void setCiuObservaciones(String ciuObservaciones) {
        this.ciuObservaciones = ciuObservaciones;
    }

    public Date getCiuFechaIngreso() {
        return ciuFechaIngreso;
    }

    public void setCiuFechaIngreso(Date ciuFechaIngreso) {
        this.ciuFechaIngreso = ciuFechaIngreso;
    }

    public String getCiuCerVotacion() {
        return ciuCerVotacion;
    }

    public void setCiuCerVotacion(String ciuCerVotacion) {
        this.ciuCerVotacion = ciuCerVotacion;
    }

    public Integer getCiuPorcentajeDiscapacidad() {
        return ciuPorcentajeDiscapacidad;
    }

    public void setCiuPorcentajeDiscapacidad(Integer ciuPorcentajeDiscapacidad) {
        this.ciuPorcentajeDiscapacidad = ciuPorcentajeDiscapacidad;
    }

    public String getCiuDescripcionDiscapacidad() {
        return ciuDescripcionDiscapacidad;
    }

    public void setCiuDescripcionDiscapacidad(String ciuDescripcionDiscapacidad) {
        this.ciuDescripcionDiscapacidad = ciuDescripcionDiscapacidad;
    }

    public String getCiuCuidadosDiscapacidad() {
        return ciuCuidadosDiscapacidad;
    }

    public void setCiuCuidadosDiscapacidad(String ciuCuidadosDiscapacidad) {
        this.ciuCuidadosDiscapacidad = ciuCuidadosDiscapacidad;
    }

    public String getCiuCarnetDiscapacidad() {
        return ciuCarnetDiscapacidad;
    }

    public void setCiuCarnetDiscapacidad(String ciuCarnetDiscapacidad) {
        this.ciuCarnetDiscapacidad = ciuCarnetDiscapacidad;
    }

    public Date getCiuFechaDiscapacidad() {
        return ciuFechaDiscapacidad;
    }

    public void setCiuFechaDiscapacidad(Date ciuFechaDiscapacidad) {
        this.ciuFechaDiscapacidad = ciuFechaDiscapacidad;
    }

    public String getCiuIdRepresentante() {
        return ciuIdRepresentante;
    }

    public void setCiuIdRepresentante(String ciuIdRepresentante) {
        this.ciuIdRepresentante = ciuIdRepresentante;
    }

    public String getCiuNombreRepresentante() {
        return ciuNombreRepresentante;
    }

    public void setCiuNombreRepresentante(String ciuNombreRepresentante) {
        this.ciuNombreRepresentante = ciuNombreRepresentante;
    }

    public Date getCiuFechaConstitucion() {
        return ciuFechaConstitucion;
    }

    public void setCiuFechaConstitucion(Date ciuFechaConstitucion) {
        this.ciuFechaConstitucion = ciuFechaConstitucion;
    }

    public Integer getCiuFolio() {
        return ciuFolio;
    }

    public void setCiuFolio(Integer ciuFolio) {
        this.ciuFolio = ciuFolio;
    }

    public String getCiuEmail() {
        return ciuEmail;
    }

    public void setCiuEmail(String ciuEmail) {
        this.ciuEmail = ciuEmail;
    }

    public String getCiuMovil() {
        return ciuMovil;
    }

    public void setCiuMovil(String ciuMovil) {
        this.ciuMovil = ciuMovil;
    }

    public String getCiuDocumento() {
        return ciuDocumento;
    }

    public void setCiuDocumento(String ciuDocumento) {
        this.ciuDocumento = ciuDocumento;
    }

    public String getCiuGenero() {
        return ciuGenero;
    }

    public void setCiuGenero(String ciuGenero) {
        this.ciuGenero = ciuGenero;
    }

    public Character getCiuCuidaDiscapacitado() {
        return ciuCuidaDiscapacitado;
    }

    public void setCiuCuidaDiscapacitado(Character ciuCuidaDiscapacitado) {
        this.ciuCuidaDiscapacitado = ciuCuidaDiscapacitado;
    }

    @XmlTransient
    @JsonIgnore
    public List<CiuDireccion> getCiuDireccionList() {
        return ciuDireccionList;
    }

    public void setCiuDireccionList(List<CiuDireccion> ciuDireccionList) {
        this.ciuDireccionList = ciuDireccionList;
    }

    @XmlTransient
    @JsonIgnore
    public List<CiuRelaciones> getCiuRelacionesList() {
        return ciuRelacionesList;
    }

    public void setCiuRelacionesList(List<CiuRelaciones> ciuRelacionesList) {
        this.ciuRelacionesList = ciuRelacionesList;
    }

    @XmlTransient
    @JsonIgnore
    public List<CiuTelefono> getCiuTelefonoList() {
        return ciuTelefonoList;
    }

    public void setCiuTelefonoList(List<CiuTelefono> ciuTelefonoList) {
        this.ciuTelefonoList = ciuTelefonoList;
    }

    @XmlTransient
    @JsonIgnore
    public List<CiuReferenciasPersonal> getCiuReferenciasPersonalList() {
        return ciuReferenciasPersonalList;
    }

    public void setCiuReferenciasPersonalList(List<CiuReferenciasPersonal> ciuReferenciasPersonalList) {
        this.ciuReferenciasPersonalList = ciuReferenciasPersonalList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ciuCedRuc != null ? ciuCedRuc.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        //  : Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CiuCiudadano)) {
            return false;
        }
        CiuCiudadano other = (CiuCiudadano) object;
        if ((this.ciuCedRuc == null && other.ciuCedRuc != null) || (this.ciuCedRuc != null && !this.ciuCedRuc.equals(other.ciuCedRuc))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CiuCiudadano[ ciuCedRuc=" + ciuCedRuc + " ]";
    }

    public String nombresCompletos() {
        return util.Utils.isEmpty(this.getCiuApellidoPat()) + " " + util.Utils.isEmpty(this.getCiuApellidoMat()) + " " + util.Utils.isEmpty(this.getCiuPrimerNombre()) + " " + util.Utils.isEmpty(this.getCiuSegundoNombre());
    }
}
