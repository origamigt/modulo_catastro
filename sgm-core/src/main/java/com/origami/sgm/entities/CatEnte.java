/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.google.gson.annotations.Expose;
import com.origami.sgm.database.SchemasConfig;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;
import util.Utils;

/**
 *
 * @author CarlosLoorVargas
 */
@Entity
@Table(name = "cat_ente", schema = SchemasConfig.APP1, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ci_ruc"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatEnte.findAll", query = "SELECT c FROM CatEnte c"),
    @NamedQuery(name = "CatEnte.findById", query = "SELECT c FROM CatEnte c WHERE c.id = :id"),
    @NamedQuery(name = "CatEnte.findByCiRuc", query = "SELECT c FROM CatEnte c WHERE c.ciRuc = :ciRuc"),
    @NamedQuery(name = "CatEnte.findByNombres", query = "SELECT c FROM CatEnte c WHERE c.nombres = :nombres"),
    @NamedQuery(name = "CatEnte.findByApellidos", query = "SELECT c FROM CatEnte c WHERE c.apellidos = :apellidos"),
    @NamedQuery(name = "CatEnte.findByEsPersona", query = "SELECT c FROM CatEnte c WHERE c.esPersona = :esPersona"),
    @NamedQuery(name = "CatEnte.findByDireccion", query = "SELECT c FROM CatEnte c WHERE c.direccion = :direccion"),
    @NamedQuery(name = "CatEnte.findByRepresentanteLegal", query = "SELECT c FROM CatEnte c WHERE c.representanteLegal = :representanteLegal"),
    @NamedQuery(name = "CatEnte.findByFechaNacimiento", query = "SELECT c FROM CatEnte c WHERE c.fechaNacimiento = :fechaNacimiento"),
    @NamedQuery(name = "CatEnte.findByEstado", query = "SELECT c FROM CatEnte c WHERE c.estado = :estado"),
    @NamedQuery(name = "CatEnte.findByUserCre", query = "SELECT c FROM CatEnte c WHERE c.userCre = :userCre"),
    @NamedQuery(name = "CatEnte.findByFechaCre", query = "SELECT c FROM CatEnte c WHERE c.fechaCre = :fechaCre"),
    @NamedQuery(name = "CatEnte.findByUserMod", query = "SELECT c FROM CatEnte c WHERE c.userMod = :userMod"),
    @NamedQuery(name = "CatEnte.findByFechaMod", query = "SELECT c FROM CatEnte c WHERE c.fechaMod = :fechaMod"),
    @NamedQuery(name = "CatEnte.findByNombreComercial", query = "SELECT c FROM CatEnte c WHERE c.nombreComercial = :nombreComercial"),
    @NamedQuery(name = "CatEnte.findByRazonSocial", query = "SELECT c FROM CatEnte c WHERE c.razonSocial = :razonSocial")})
public class CatEnte implements Serializable {

    private static final long serialVersionUID = 8799656478674716638L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SchemasConfig.APPUNISEQ_ORM)
    @SequenceGenerator(name = SchemasConfig.APPUNISEQ_ORM, sequenceName = SchemasConfig.APP1 + "." + SchemasConfig.APPUNISEQ_DB, allocationSize = 1)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    private Long id;
    @Column(name = "ci_ruc")
    @Expose
    private String ciRuc;
    @Size(max = 500)
    @Column(name = "nombres", length = 500)
    @Expose
    private String nombres;
    @Size(max = 500)
    @Column(name = "apellidos", length = 500)
    @Expose
    private String apellidos;
    @Column(name = "direccion", length = 500)
    private String direccion;
    @Column(name = "representante_legal")
    private BigInteger representanteLegal;
    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaNacimiento;
    @Column(name = "fecha_defuncion")
    @Temporal(TemporalType.DATE)
    private Date fechaDefuncion;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1)
    @Column(name = "estado", nullable = false, length = 1)
    private String estado = "A";
    @Size(max = 100)
    @Column(name = "user_cre", length = 100)
    private String userCre;
    @Column(name = "fecha_cre")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCre;
    @Size(max = 100)
    @Column(name = "user_mod", length = 100)
    private String userMod;
    @Column(name = "fecha_mod")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaMod;
    @Size(max = 500)
    @Column(name = "nombre_comercial", length = 500)
    private String nombreComercial;
    @Size(max = 500)
    @Column(name = "razon_social", length = 500)
    @Expose
    private String razonSocial;
    @Size(max = 20)
    @Column(name = "reg_prof", length = 20)
    private String regProf;
    @Size(max = 80)
    @Column(name = "titulo_prof", length = 80)
    private String tituloProf;
    @Column(name = "excepcionales")
    private Boolean excepcionales = false;
    @Column(name = "estado_correccion", length = 1)
    private String estadoCorrecion;
    @Basic(optional = false)
    @NotNull
    @Column(name = "es_persona", nullable = false)
    @Expose
    private Boolean esPersona = Boolean.TRUE;
    @JoinColumn(name = "firma", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private PeFirma firma;
    @Column(name = "cod_usuario")
    private BigInteger codUsuario;

    @Transient
    private String telefono;
    @Transient
    private String correo;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ente", fetch = FetchType.LAZY)
    private List<EnteCorreo> enteCorreoCollection = new ArrayList<>();
    @OneToMany(mappedBy = "ente", fetch = FetchType.LAZY)
    private List<CatPredioPropietario> catPredioPropietarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "ente", fetch = FetchType.LAZY)
    private List<EnteTelefono> enteTelefonoCollection = new ArrayList<>();
    @Column(name = "es_tercera_edad")
    private Boolean esTerceraEdad = false;
    @JoinColumn(name = "discapacidad", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CtlgItem discapacidad;
    @Column(name = "porcentaje", scale = 2, precision = 3)
    private BigDecimal porcentaje;
    @JoinColumn(name = "nacionalidad", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CatNacionalidad nacionalidad;
    @JoinColumn(name = "pais", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CatPais pais;
    @JoinColumn(name = "estado_civil", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CtlgItem estadoCivil;
    @JoinColumn(name = "tipo_documento", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private CtlgItem tipoDocumento;
    @Column(name = "personeria_juridica")
    private Boolean personeriaJuridica = false;
    @Column(name = "conyuge")
    private BigInteger conyuge;
    @Column(name = "lleva_contabilidad")
    private Boolean llevaContabilidad;

    public CatEnte() {
    }

    public CatEnte(Long id) {
        this.id = id;
    }

    public CatEnte(Long id, Boolean esPersona, String estado) {
        this.id = id;
        this.esPersona = esPersona;
        this.estado = estado;
    }

    public Date getFechaDefuncion() {
        return fechaDefuncion;
    }

    public void setFechaDefuncion(Date fechaDefuncion) {
        this.fechaDefuncion = fechaDefuncion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCiRuc() {
        return ciRuc;
    }

    public void setCiRuc(String ciRuc) {
        this.ciRuc = ciRuc;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Boolean getEsPersona() {
        return esPersona;
    }

    public void setEsPersona(Boolean esPersona) {
        this.esPersona = esPersona;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public BigInteger getRepresentanteLegal() {
        return representanteLegal;
    }

    public void setRepresentanteLegal(BigInteger representanteLegal) {
        this.representanteLegal = representanteLegal;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUserCre() {
        return userCre;
    }

    public void setUserCre(String userCre) {
        this.userCre = userCre;
    }

    public Date getFechaCre() {
        return fechaCre;
    }

    public void setFechaCre(Date fechaCre) {
        this.fechaCre = fechaCre;
    }

    public String getUserMod() {
        return userMod;
    }

    public void setUserMod(String userMod) {
        this.userMod = userMod;
    }

    public Date getFechaMod() {
        return fechaMod;
    }

    public void setFechaMod(Date fechaMod) {
        this.fechaMod = fechaMod;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRegProf() {
        return regProf;
    }

    public void setRegProf(String regProf) {
        this.regProf = regProf;
    }

    public String getTituloProf() {
        return tituloProf;
    }

    public void setTituloProf(String tituloProf) {
        this.tituloProf = tituloProf;
    }

    public Boolean getExcepcionales() {
        return excepcionales;
    }

    public void setExcepcionales(Boolean excepcionales) {
        this.excepcionales = excepcionales;
    }

    @XmlTransient
    @JsonIgnore
    public List<EnteCorreo> getEnteCorreoCollection() {
        return enteCorreoCollection;
    }

    public void setEnteCorreoCollection(List<EnteCorreo> enteCorreoCollection) {
        this.enteCorreoCollection = enteCorreoCollection;
    }

    @XmlTransient
    @JsonIgnore
    public List<CatPredioPropietario> getCatPredioPropietarioCollection() {
        return catPredioPropietarioCollection;
    }

    public void setCatPredioPropietarioCollection(List<CatPredioPropietario> catPredioPropietarioCollection) {
        this.catPredioPropietarioCollection = catPredioPropietarioCollection;
    }

    @XmlTransient
    @JsonIgnore
    public List<EnteTelefono> getEnteTelefonoCollection() {
        return enteTelefonoCollection;
    }

    public void setEnteTelefonoCollection(List<EnteTelefono> enteTelefonoCollection) {
        this.enteTelefonoCollection = enteTelefonoCollection;
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
        if (!(object instanceof CatEnte)) {
            return false;
        }
        CatEnte other = (CatEnte) object;
        return !((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "gob.samborondon.entities.CatEnte[ id=" + id + " ]";
    }

    public BigInteger getCodUsuario() {
        return codUsuario;
    }

    public void setCodUsuario(BigInteger codUsuario) {
        this.codUsuario = codUsuario;
    }

    public String getEstadoCorrecion() {
        return estadoCorrecion;
    }

    public void setEstadoCorrecion(String estadoCorrecion) {
        this.estadoCorrecion = estadoCorrecion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Devuelve los Nombres si es una persona natural y la razon social si es
     * juridica
     *
     * @return Nombres del ente
     */
    public String getNombreCompleto() {
        if (esPersona) {
            return Utils.isEmpty(apellidos) + " " + Utils.isEmpty(nombres);
        } else {
            return Utils.isEmpty(razonSocial);
        }
    }

    public String getEdad() {
        if (fechaNacimiento != null) {
            Calendar c = Calendar.getInstance();
            c.setTime(fechaNacimiento);
            Calendar c1 = Calendar.getInstance();
            c1.setTime(new Date());
            int diff = c1.get(Calendar.YEAR) - c.get(Calendar.YEAR);
            if (c.get(Calendar.MONTH) > c1.get(Calendar.MONTH)
                    || (c.get(Calendar.MONTH) == c1.get(Calendar.MONTH) && c.get(Calendar.DATE) > c1.get(Calendar.DATE))) {
                diff--;
            }
            if (estado.equalsIgnoreCase("F")) {
                return "Fallecido";
            } else {
                return "" + diff;
            }
        } else {
            return "0";
        }
    }

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    public PeFirma getFirma() {
        return firma;
    }

    public void setFirma(PeFirma firma) {
        this.firma = firma;
    }

    public String getTelefonos() {
        if (enteTelefonoCollection == null) {
            return "";
        }
        String s = "";
        Integer i = 0;

        for (EnteTelefono temp : enteTelefonoCollection) {
            i++;
            if (i != enteTelefonoCollection.size()) {
                s = s + temp.getTelefono() + "; ";
            } else {
                s = s + temp.getTelefono();
            }
        }
        return s;
    }

    public String getEmails() {
        if (enteCorreoCollection == null) {
            return "";
        }
        String s = "";
        Integer i = 0;

        for (EnteCorreo temp : enteCorreoCollection) {
            i++;
            if (i != enteTelefonoCollection.size()) {
                s = s + temp.getEmail() + ", ";
            } else {
                s = s + temp.getEmail();
            }
        }
        return s;
    }

    public Boolean getEsTerceraEdad() {
        return esTerceraEdad;
    }

    public void setEsTerceraEdad(Boolean esTerceraEdad) {
        this.esTerceraEdad = esTerceraEdad;
    }

    public CtlgItem getDiscapacidad() {
        return discapacidad;
    }

    public void setDiscapacidad(CtlgItem discapacidad) {
        this.discapacidad = discapacidad;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public CatNacionalidad getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(CatNacionalidad nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public CatPais getPais() {
        return pais;
    }

    public void setPais(CatPais pais) {
        this.pais = pais;
    }

    public CtlgItem getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(CtlgItem estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public CtlgItem getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(CtlgItem tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Boolean getPersoneriaJuridica() {
        return personeriaJuridica;
    }

    public void setPersoneriaJuridica(Boolean personeriaJuridica) {
        this.personeriaJuridica = personeriaJuridica;
    }

    public BigInteger getConyuge() {
        return conyuge;
    }

    public void setConyuge(BigInteger conyuge) {
        this.conyuge = conyuge;
    }

    public Boolean getLlevaContabilidad() {
        return llevaContabilidad;
    }

    public void setLlevaContabilidad(Boolean llevaContabilidad) {
        this.llevaContabilidad = llevaContabilidad;
    }

}
