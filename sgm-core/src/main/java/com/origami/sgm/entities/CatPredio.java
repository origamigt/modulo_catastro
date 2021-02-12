
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.origami.annotations.Report;
import com.origami.annotations.ReportField;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.config.SisVars;
import com.origami.enums.FieldType;
import com.origami.sgm.database.Querys;
import com.origami.sgm.database.SchemasConfig;
import com.origami.sgm.entities.avaluos.SectorValorizacion;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.entities.models.ModelMap;
import com.origami.sgm.util.EjbsCaller;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import javax.persistence.OneToOne;
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
import org.hibernate.Hibernate;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.Where;
import util.HiberUtil;
import util.Utils;

/**
 *
 * @author CarlosLoorVargas
 */
@Entity
@Table(name = "cat_predio", schema = SchemasConfig.APP1, uniqueConstraints = {
    @UniqueConstraint(columnNames = {"num_predio"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CatPredio.findAll", query = "SELECT c FROM CatPredio c"),
    @NamedQuery(name = "CatPredio.findAllByManzana", query = "SELECT p FROM CatPredio p WHERE p.parroquia = :parroquia AND p.zona = :zona AND p.sector = :sector AND p.mz = :mz ORDER BY p.parroquia, p.zona, p.sector, p.mz, p.solar, p.bloque, p.piso, p.unidad"),

    @NamedQuery(name = "CatPredio.findByAvaluoMunicipal", query = "SELECT c FROM CatPredio c WHERE c.avaluoMunicipal = :avaluoMunicipal")})
@SequenceGenerator(name = "cat_predio_id_seq", sequenceName = SchemasConfig.APP1 + ".cat_predio_id_seq", allocationSize = 1)
@org.hibernate.annotations.DynamicUpdate
@Report(description = "Predios")
public class CatPredio extends ModelMap implements Serializable {

    private static final long serialVersionUID = 8799656478674716638L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cat_predio_id_seq")
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    @Expose
    @ReportField(description = "Identificador")
    private Long id;
    @Column(name = "provincia")
    @Expose
    @ReportField(description = "Provincia")
    private Short provincia = SisVars.PROVINCIA;
    @Column(name = "canton")
    @Expose
    @ReportField(description = "Canton")
    private Short canton = SisVars.CANTON;
    @Column(name = "parroquia")
    @Expose
    @ReportField(description = "Parroquia")
    private Short parroquia = new Short("0");
    @Basic(optional = false)
    @NotNull
    @Column(name = "zona", nullable = false)
    @Expose
    @ReportField(description = "Zona")
    private Short zona = new Short("0");
    @Basic(optional = false)
    @NotNull
    @Column(name = "sector", nullable = false)
    @Expose
    @ReportField(description = "Sector")
    private Short sector = new Short("0");
    @Column(name = "mz")
    @Expose
    @ReportField(description = "Manzana")
    private Short mz = new Short("0");
    @Column(name = "mzdiv")
    private Short mzdiv;
    @ReportField(description = "Lote")
    @Column(name = "solar")
    @Expose
    private Short solar = new Short("0");
    @Basic(optional = false)
    @NotNull
    @Column(name = "bloque", nullable = false)
    @Expose
    @ReportField(description = "Bloque")
    private Short bloque;
    @Basic(optional = false)
    @NotNull
    @Column(name = "piso", nullable = false)
    @Expose
    @ReportField(description = "Piso")
    private Short piso = new Short("0");
    @Basic(optional = false)
    @NotNull
    @Column(name = "unidad", nullable = false)
    @Expose
    @ReportField(description = "Unidad")
    private Short unidad = new Short("0");
    @ReportField(description = "Clave Catastral")
    @Size(max = 100)
    @Column(name = "clave_cat", length = 100)
    @Expose
    private String claveCat;
    @ReportField(description = "Clave Anterior 1")
    @Size(max = 150)
    @Column(name = "predialant", length = 150)
    @Expose
    private String predialant;
    @ReportField(description = "Clave Anterior 2")
    @Column(name = "predialant_ant", length = 100)
    @Expose
    private String predialantAnt;
    @ReportField(description = "No. Predio/Código Único")
    @Column(name = "num_predio", columnDefinition = "bigint")
    @Expose
    private BigInteger numPredio;

    @Size(max = 80)
    @Column(name = "nombre_urb", length = 80)
    @Expose
    private String nombreUrb;
    @Column(name = "urb_sec")
    @Expose
    private Short urbSec;
    @Column(name = "num_habitaciones")
    @Expose
    @ReportField(description = "Numero de Habitaciones")
    private Short numHabitaciones;
    @Column(name = "num_dormitorios")
    @Expose
    @ReportField(description = "Numero de Dormitorios")
    private Short numDormitorios;
    @Column(name = "num_espacios_banios")
    @Expose
    @ReportField(description = "Espacios Para Bañarse o Duchas")
    private Short numEspaciosBanios;
    @Column(name = "habitantes")
    @Expose
    @ReportField(description = "Numero de Habitantes")
    private Integer habitantes;
    @Column(name = "num_hogares")
    @Expose
    @ReportField(description = "Numero de Hogares")
    private Short numHogares;
    @Column(name = "num_celulares")
    @Expose
    @ReportField(description = "Numero de Telefonos Celulares")
    private Short numCelulares;
    @Column(name = "viv_cencal_posee_telf_convencional")
    @Expose
    @ReportField(description = "Posee Telefono Convencional")
    private Boolean vivCencalPoseeTelfConvencional;
    @Column(name = "viv_cencal_serv_internet")
    @Expose
    @ReportField(description = "Servicio de Internet")
    private Boolean vivCencalServInternet;
    @Column(name = "soporta_hipoteca")
    @Expose
    private Boolean soportaHipoteca;
    @ReportField(description = "Mz (Plano Aprobado)")
    @Size(max = 20)
    @Column(name = "urb_mz", length = 20)
    @Expose
    private String urbMz;
    @Column(name = "numero_ficha", columnDefinition = "bigint")
    @Expose
    private BigInteger numeroFicha;
    @ReportField(description = "Fecha de creación")
    @Basic(optional = false)
    @NotNull
    @Column(name = "inst_creacion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Expose(serialize = false, deserialize = false)
    private Date instCreacion;
    @ReportField(description = "Lote (Plano Aprobado)")
    @Size(max = 100)
    @Column(name = "urb_solarnew", length = 100)
    @Expose
    @SerializedName(value = "urbSolarnew")
    private String urbSolarnew;
    @Size(max = 50)
    @Column(name = "urb_secnew", length = 50)
    @Expose
    private String urbSecnew;
    @Column(name = "observaciones", length = 5000)
    @Expose
    @ReportField(description = "Observaciones")
    private String observaciones;
    @Size(max = 150)
    @Column(name = "predial97", length = 150)
    private String predial97;

    @Size(max = 150)
    @Column(name = "calle_av", length = 150)
    @Expose
    private String calleAv;

    @Size(max = 150)
    @Column(name = "calle", length = 150)
    @Expose
    @ReportField(description = "Calle Principal")
    private String calle;

    @Size(max = 150)
    @Column(name = "calle_s", length = 150)
    @Expose
    @ReportField(description = "Calle Sec.")
    private String calleS;
    @Size(max = 150)
    @Column(name = "numero_vivienda", length = 150)
    @Expose
    @ReportField(description = "Placa Domiciliaria")
    private String numeroVivienda;

    @Column(name = "alicuota_util", precision = 12, scale = 4)
    @Expose
    private BigDecimal alicuotaUtil;

    @Column(name = "alicuota_const", precision = 12, scale = 4)
    @Expose
    @ReportField(description = "Alicuota construccion")
    private BigDecimal alicuotaConst;
    @Column(name = "area_aumento_cons", precision = 12, scale = 2)
    @Expose
    @ReportField(description = "Area Aumento const.")
    private BigDecimal areaAumentoCons;
    @Column(name = "alicuota_terreno", precision = 12, scale = 2)
    @Expose
    @ReportField(description = "Alicuota terreno(%)")
    private BigDecimal alicuotaTerreno;
    @Column(name = "area_terreno_alicuota", precision = 12, scale = 2)
    @Expose
    @ReportField(description = "Area Terreno de Alicuota(m2)")
    private BigDecimal areaTerrenoAlicuota;
    @ReportField(description = "Descripcion (PH)")
    @Size(max = 150)
    @Column(name = "num_departamento", length = 150)
    @Expose
    private String numDepartamento;
    @ReportField(description = "Nombre del Predio o Edificacion")
    @Size(max = 150)
    @Column(name = "nombre_edificio", length = 150)
    @Expose
    private String nombreEdificio;
    @Size(max = 200)
    @Column(name = "nom_comp_pago", length = 200)
    @Expose
    private String nomCompPago;
    @ReportField(description = "Es Ph")
    @Column(name = "propiedad_horizontal")
    @Expose
    private Boolean propiedadHorizontal = Boolean.FALSE;
    @Column(name = "predio_raiz", columnDefinition = "bigint")
    @Expose
    private BigInteger predioRaiz;
    @ReportField(description = "Estado Predio", filter = "A")
    @Size(max = 1)
    @Column(name = "estado", length = 1)
    @Expose
    private String estado;
    @ReportField(description = "Tipo de Predio")
    @Column(name = "tipo_predio", length = 10)
    @Expose
    private String tipoPredio;
    @ReportField(description = "Avalúo Terreno")
    @Column(name = "avaluo_solar", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoSolar;
    @ReportField(description = "Avalúo Construcción")
    @Column(name = "avaluo_construccion", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoConstruccion;
    @ReportField(description = "Avalúo Obras Complementarias")
    @Column(name = "avaluo_Obra_Complement", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoObraComplement;
    @ReportField(description = "Base Imponible")
    @Column(name = "base_imponible", precision = 15, scale = 4)
    @Expose
    private BigDecimal baseImponible;
    @ReportField(description = "Avalúo Valor de la Propiedad")
    @Column(name = "avaluo_municipal", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoMunicipal;
    @ReportField(description = "Avalúo Cultivos")
    @Column(name = "avaluo_cultivos", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoCultivos;
    @Column(name = "uso_ph")
    @Expose
    private String usoPh;
    @Column(name = "division_urb", length = 200)
    @Expose
    private String divisionUrb;
    @ReportField(description = "Planta (Piso PH)")
    @Column(name = "num_pisos", columnDefinition = "bigint")
    @Expose
    private BigInteger numPisos;
    @ReportField(description = "Coordenadas X (Latitud)")
    @Column(name = "coordx", precision = 14, scale = 4)
    @Expose
    private BigDecimal coordx;
    @ReportField(description = "Coordenada Y (Longitud)")
    @Column(name = "coordy", precision = 14, scale = 4)
    @Expose
    private BigDecimal coordy;
    @ReportField(description = "Fecha de Modificación")
    @Column(name = "fec_mod")
    @Temporal(TemporalType.TIMESTAMP)
    @Expose(serialize = true, deserialize = false)
    private Date fecMod;
    @Size(max = 50)
    @Column(name = "usr_mod", length = 50)
    private String usrMod;
    @Column(name = "revisado")
    @Expose
    private Boolean revisado;
    @Column(name = "procesados")
    @Expose
    private Boolean procesados;
    @Column(name = "nuevo")
    @Expose
    private Boolean nuevo;
    @Column(name = "tipo_vivienda_horizontal")
    @Expose
    private Boolean tipoViviendaHorizontal;
    @Column(name = "ocupacion_viv_horizontal")
    @Expose
    private Boolean ocupacionViviendaHorizontal;
    @OneToOne(mappedBy = "predio", fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Caracteristicas", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CatPredioS4 catPredioS4;
    @OneToOne(mappedBy = "predio", fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Servicios", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CatPredioS6 catPredioS6;
    @ReportField(description = "Espacio Urbano / Rural", type = FieldType.COLLECTION_ONE_TO_ONE)
    @JoinColumn(name = "tipo_conjunto", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CatTipoConjunto tipoConjunto;
    @JoinColumn(name = "responsable_actualizador_predial", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CatEnte responsableActualizadorPredial;
    @JoinColumn(name = "responsable_fiscalizador_predial", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CatEnte responsableFiscalizadorPredial;
    @JoinColumn(name = "ente_horizontal", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CatEnte enteHorizontal;
    @JoinColumn(name = "tenencia", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CatTenenciaItem tenencia;
    @JoinColumn(name = "propiedad", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Tenencia", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem propiedad;
    @JoinColumn(name = "condicion_municipal", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Condición Municipal", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem condicionMunicipal;
    @Column(name = "condicion_municipal_anio")
    @Expose
    @ReportField(description = "Tiempo en Años", type = FieldType.COLLECTION_ONE_TO_ONE)
    private Integer condicionMunicipalAnio;
    @ReportField(description = "Nombre del espacio urbano / rural", type = FieldType.COLLECTION_ONE_TO_ONE)
    @JoinColumn(name = "ciudadela", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CatCiudadela ciudadela;
    @ReportField(description = "Usuario Creador", type = FieldType.COLLECTION_ONE_TO_ONE)
    @JoinColumn(name = "usuario_creador", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private AclUser usuarioCreador;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "predio", fetch = FetchType.LAZY)
    @Expose
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private CatPredioS12 catPredioS12;
    @ReportField(description = "Área Terreno")
    @Column(name = "area_solar", precision = 12, scale = 5)
    @Expose
    private BigDecimal areaSolar;
    @ReportField(description = "Área Obras Complementarias")
    @Column(name = "area_obras", precision = 12, scale = 2)
    @Expose
    private BigDecimal areaObras;
    @ReportField(description = "Área Cultivos")
    @Column(name = "area_cultivos", precision = 12, scale = 2)
    @Expose
    private BigDecimal areaCultivos;
    @ReportField(description = "Área Construcción")
    @Column(name = "area_declarada_const", precision = 12, scale = 2)
    @Expose
    private BigDecimal areaDeclaradaConst = BigDecimal.ZERO;
    @Column(name = "amri", precision = 12, scale = 2)
    @Expose
    private BigDecimal amri;
    @Column(name = "zona_pu")
    @Expose
    private String zonaPu;
    @JoinColumn(name = "forma_solar", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Forma del predio", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem formaSolar;
    @Size(max = 6)
    @Column(name = "cod_categoria", length = 6)
    @Expose
    private String codCategoria;
    @JoinColumn(name = "topografia_solar", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Topografia", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem topografiaSolar;

    @JoinColumn(name = "tipo_via", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Tipo de Vía", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tipovia;

    @JoinColumn(name = "otro_tipo_via", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CtlgItem otroTipovia;
    @JoinColumn(name = "unidad_medida", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CtlgItem unidadMedida;
    @JoinColumn(name = "clasif_horizontal", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private CtlgItem clasificacionViviendaHorizontal;
    @JoinColumn(name = "tipo_suelo", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Caract. Suelo", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tipoSuelo;
    @JoinColumn(name = "tenencia_vivienda", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Tenencia de Vivienda", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tenenciaVivienda;
    @JoinColumn(name = "uso_solar", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose(serialize = true, deserialize = true)
    private CtlgItem usoSolar;
    @JoinColumn(name = "constructividad", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Constructividad", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem constructividad;

    @JoinColumn(name = "subsector", referencedColumnName = "sector")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    private SectorValorizacion subsector;
//    @Column(name = "lote")
//    @Expose
//    private Short lote = new Short("0");

    @Column(name = "area_const_ph", precision = 12, scale = 2)
    @Expose
    private BigDecimal areaConstPh;
    @ReportField(description = "Requiere Perfeccionamiento")
    @Column(name = "requiere_perfeccionamiento")
    @Expose
    private Boolean requierePerfeccionamiento = false;
    @Column(name = "anios_sin_perfeccionamiento")
    @Expose
    @ReportField(description = "Año desde cuando está posesionado")
    private Integer aniosSinPerfeccionamiento;
    @Column(name = "anios_posesion")
    @Expose
    @ReportField(description = "Años en Posesión")
    private Integer aniosPosesion;
    @JoinColumn(name = "tipo_poseedor", referencedColumnName = "id")
    @Expose
    @ManyToOne(fetch = FetchType.LAZY)
    @ReportField(description = "Tipo de Poseedor", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tipoPoseedor;
    @Column(name = "nombre_pueblo_etnia")
    @Expose
    @ReportField(description = "Nombre Pueblo/Etnia")
    private String nombrePuebloEtnia;
    @JoinColumn(name = "clasificacion_suelo", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Clasificación del suelo", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem clasificacionSuelo;

    @Transient
    private Boolean crear;
    @Transient
    private CatParroquia catParroquia;
    @Transient
    private CatPredioEdificacion edificacion;
    @Transient
    @Expose
    private CatEscritura escrituraLinderos;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "predio")
    @Expose
    private CatClaveReordenada claveReordenada;
    @JoinColumn(name = "informante", referencedColumnName = "id")
    @Expose
    @ManyToOne(fetch = FetchType.LAZY)
    private CatEnte informante;
    @ReportField(description = "Es Ficha Madre")
    @Expose
    @Column(name = "ficha_madre")
    private Boolean fichaMadre = Boolean.FALSE;

    @Column(name = "cant_alicuotas")
    @Expose
    private Integer cantAlicuotas;

    @Expose
    @Column(name = "tiene_escritura")
    private Boolean tieneEscritura;

    @Transient
    private CatEscritura escritura;
    // version de ibarra
    @Column(name = "avaluo_plussolar", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoPlussolar;
    @Column(name = "avaluo_plusconstruccion", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoPlusconstruccion;
    @Column(name = "avaluo_plusmunicipal", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoPlusmunicipal;
    @Column(name = "avaluo_pluscultivos", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoPluscultivos;
    @Column(name = "avaluo_Plu_Obra_Complement", precision = 15, scale = 4)
    @Expose
    private BigDecimal avaluoPluObraComplement;
    @JoinColumn(name = "uso_via", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Uso de Vía", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem usoVia;
    @JoinColumn(name = "forma_adquisicion", referencedColumnName = "id")
    @Expose
    @ManyToOne
    @ReportField(description = "Forma de Adquisición o Tenencia", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem formaAdquisicion;

    @JoinColumn(name = "viv_cencal_acabado_piso", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Acabado Piso - Pisos", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem vivCencalAcabadoPiso;
    @JoinColumn(name = "viv_cencal_estado_acabado_piso", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Estado Acabado Piso", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem vivCencalEstadoAcabadoPiso;
    @Column(name = "prop_anterior_predio")
    @Expose
    @ReportField(description = "Nombres (Propietario Anterior)")
    private String propAnteriorPredio;
    @Column(name = "cedula_prop_anterior")
    @Expose
    @ReportField(description = "Número de Identificación (Propietario Anterior)")
    private String cedulaPropAnterior;
    @Column(name = "area_total_cons", precision = 12, scale = 2)
    @Expose
    private BigDecimal areaTotalCons;
    @Column(name = "ci_ruc_informante")
    @Expose
    @ReportField(description = "CI/RUC (INFORMANTE)")
    private String ciRucInformante;
    @Column(name = "nombre_informante")
    @Expose
    @ReportField(description = "Nombres (INFORMANTE)")
    private String nombreInformante;
    @Column(name = "apellidos_informante")
    @Expose
    @ReportField(description = "Apellidos (INFORMANTE)")
    private String apellidosInformante;

    @Expose
    @Column(name = "ciu_actualizador")
    @ReportField(description = "CI/RUC (LEVANTADO POR)")
    private String ciuActualizador;
    @Expose
    @Column(name = "ciu_nombre_actualizador")
    @ReportField(description = "Nombres Completos (LEVANTADO POR)")
    private String ciuNombresActualizador;
    @Expose
    @Column(name = "ciu_fiscalizador")
    @ReportField(description = "CI/RUC (REVISADO POR)")
    private String ciuFiscalizador;
    @Expose
    @Column(name = "ciu_nombre_fiscalizador")
    @ReportField(description = "Nombres Completos (REVISADO POR)")
    private String ciuNombresFiscalizador;
    @Expose
    @Column(name = "ciu_informante")
    @ReportField(description = "Nombres Informante")
    private String ciuInformante;
    @Expose
    @Column(name = "ciu_nombre_informante")
    @ReportField(description = "Nombres Informante")
    private String ciuNombresInformante;
    @Expose
    @Column(name = "nombre_posesionario")
    @ReportField(description = "Nombre Posesionario")
    private String nombrePosesionario;
    @Expose
    @Column(name = "cedula_posesionario")
    @ReportField(description = "Cédula Posesionario")
    private String cedulaPosesionario;

    @Where(clause = "estado = 'A'")
    @OneToMany(mappedBy = "predio", fetch = FetchType.LAZY)
    @Expose
    @ReportField(description = "Tipo de Obra o Inst. Especiales (Obras internas o complementarias)", type = FieldType.COLLECTION_ONE_TO_MANY)
    private Collection<CatPredioObraInterna> catPredioObraInternaCollection;
    @OneToMany(mappedBy = "predio", fetch = FetchType.LAZY)
    @Expose
    @Where(clause = "estado = 'A'")
    @ReportField(description = "Clasificación del suelo rural", type = FieldType.COLLECTION_ONE_TO_MANY)
    private List<CatPredioClasificRural> catPredioClasificRuralCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "predio")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<CatPredioAlicuotaComponente> alicuotaComponentes;
    @OneToMany(mappedBy = "predioColindante", fetch = FetchType.LAZY)
    private List<CatPredioLinderos> catPredioLinderosCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "predio", fetch = FetchType.LAZY)
    @Where(clause = "estado = 'A'")
    @Expose
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @ReportField(description = "Colindantes", type = FieldType.COLLECTION_ONE_TO_MANY)
    private List<CatPredioLinderos> predioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "predio", fetch = FetchType.LAZY)
    @Where(clause = "estado = 'A'")
    @Expose
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @ReportField(description = "Cultivos", type = FieldType.COLLECTION_ONE_TO_MANY)
    private List<CatPredioCultivo> catPredioCultivoCollection;
    @OneToMany(mappedBy = "predio", fetch = FetchType.LAZY)
    private Collection<CatCertificadoAvaluo> catCertificadoAvaluoCollection;
    @OneToMany(mappedBy = "predio", fetch = FetchType.LAZY)
    @Filter(name = "activosString")
    @Expose
    @ReportField(description = "Escrituras", type = FieldType.COLLECTION_ONE_TO_MANY)
    private Collection<CatEscritura> catEscrituraCollection;
    @Where(clause = "estado = 'A'")
    @OneToMany(mappedBy = "predio", fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @Expose
    @ReportField(description = "Propietarios", type = FieldType.COLLECTION_ONE_TO_MANY)
    private Collection<CatPredioPropietario> catPredioPropietarioCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "predio", fetch = FetchType.LAZY)
    @Where(clause = "estado='A'")
    @OrderBy(clause = "no_edificacion ASC")
    @Expose
    @ReportField(description = "Caracteristicas de la construccion/Bloques", type = FieldType.COLLECTION_ONE_TO_MANY)
    private Collection<CatPredioEdificacion> catPredioEdificacionCollection;
    @OneToMany(mappedBy = "predio", fetch = FetchType.LAZY)
    private Collection<HistoricoTramiteDet> historicoTramiteDetCollection;
    @OneToMany(mappedBy = "predio")
    private List<CatPredioAvalHistorico> catPredioAvalHistoricos;
    @Expose
    @Transient
    private List<String> fotos;
    @Expose
    @Transient
    private List<String> fotosData;

    @Column(name = "cambio_nombre")
    @Expose
    private Boolean cambioNombreTitulo;
    @Size(max = 4000)
    @Column(name = "nombre_cambiado", length = 4000)
    @Expose
    private String nombreCambiado;

    @JoinColumn(name = "clasificacion_vivienda", referencedColumnName = "id")
    @Expose
    @ManyToOne
    @ReportField(description = "Clasificacion Vivienda", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem clasificacionVivienda;
    @JoinColumn(name = "tipo_vivienda", referencedColumnName = "id")
    @Expose
    @ManyToOne
    @ReportField(description = "Tipo Vivienda", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem tipoVivienda;
    @JoinColumn(name = "condicion_vivienda", referencedColumnName = "id")
    @Expose
    @ManyToOne
    @ReportField(description = "Condicion Vivienda", type = FieldType.COLLECTION_ONE_TO_ONE)
    private CtlgItem condicionVivienda;
    @Column(name = "ciu_horizontal")
    @Expose
    @ReportField(description = "CI/RUC (JEFE DEL HOGAR)")
    private String ciuHorizontal;
    @JoinColumn(name = "ciu_tipo_identificacion", referencedColumnName = "id")
    @Expose
    @ReportField(description = "Tipo de Identificación (JEFE DEL HOGAR)", type = FieldType.COLLECTION_ONE_TO_ONE)
    @ManyToOne
    private CtlgItem ciuTipoIdentificacion;
    @Column(name = "ciu_nombre_horizontal")
    @Expose
    @ReportField(description = "Nombres Completos (JEFE DEL HOGAR)")
    private String ciuNombresHorizontal;
    @Column(name = "precio_oferta")
    @Expose
    @ReportField(description = "Precio de Oferta del Predio")
    private BigDecimal precioOferta;
    @Column(name = "codigo_inst_nac_pat_cul")
    @Expose
    @ReportField(description = "Código INPC (Instituto Nacional de Patrimonio Cultural)")
    private String codigoInstNacPatCul;

    @Size(max = 500)
    @Column(name = "admin_nombres_apellidos", length = 500)
    @Expose
    private String adminFullName;

    @Size(max = 20)
    @Column(name = "admin_cedula", length = 20)
    @Expose
    private String adminCedula;
    @Size(max = 50)
    @Column(name = "admin_telefono", length = 50)
    @Expose
    private String adminTelefono;
    @Size(max = 50)
    @Column(name = "admin_celular", length = 50)
    @Expose
    private String adminCelular;
    @Column(name = "admin_correo", length = 50)
    @Expose
    private String adminCorreo;
    @Column(name = "admin_direccion", length = 50)
    @Expose
    private String adminDireccion;
    @JoinColumn(name = "notif_tipo_direccion", referencedColumnName = "id")
    @Expose
    @ReportField(description = "Tipo de Dirección", type = FieldType.COLLECTION_ONE_TO_ONE)
    @ManyToOne
    private CtlgItem notifTipoDireccion;
    @JoinColumn(name = "admin_canton", referencedColumnName = "id")
    @Expose
    @ReportField(description = "Cantón", type = FieldType.COLLECTION_ONE_TO_ONE)
    @ManyToOne
    private CatCanton adminCanton;
    @JoinColumn(name = "admin_parroquia", referencedColumnName = "id")
    @Expose
    @ReportField(description = "Parroquia", type = FieldType.COLLECTION_ONE_TO_ONE)
    @ManyToOne
    private CatParroquia adminParroquia;

    @Transient
    private CiuCiudadano ciudadanoActualizador;
    @Transient
    private CiuCiudadano ciudadanoFiscalizador;
    @Column(name = "tiene_aumentoph")
    @Expose
    @ReportField(description = "Tiene aumento PH")
    private Boolean tieneAumentoph;
    @Transient
    private CatProvincia provinciaNot;

    public String getNombrePosesionario() {
        return nombrePosesionario;
    }

    public void setNombrePosesionario(String nombrePosesionario) {
        this.nombrePosesionario = nombrePosesionario;
    }

    public String getCedulaPosesionario() {
        return cedulaPosesionario;
    }

    public void setCedulaPosesionario(String cedulaPosesionario) {
        this.cedulaPosesionario = cedulaPosesionario;
    }

    public CatProvincia getProvinciaNot() {
        if (adminCanton != null) {
            provinciaNot = adminCanton.getIdProvincia();
        }
        return provinciaNot;
    }

    public void setProvinciaNot(CatProvincia provinciaNot) {
        this.provinciaNot = provinciaNot;
    }

    public CiuCiudadano getCiudadanoActualizador() {
        return ciudadanoActualizador;
    }

    public void setCiudadanoActualizador(CiuCiudadano ciudadanoActualizador) {
        this.ciudadanoActualizador = ciudadanoActualizador;
    }

    public CiuCiudadano getCiudadanoFiscalizador() {
        return ciudadanoFiscalizador;
    }

    public void setCiudadanoFiscalizador(CiuCiudadano ciudadanoFiscalizador) {
        this.ciudadanoFiscalizador = ciudadanoFiscalizador;
    }

    public Boolean getTieneAumentoph() {
        return tieneAumentoph;
    }

    public void setTieneAumentoph(Boolean tieneAumentoph) {
        this.tieneAumentoph = tieneAumentoph;
    }

    @Transient
    private CatEnte propietario;

    public CatCanton getAdminCanton() {
        return adminCanton;
    }

    public void setAdminCanton(CatCanton adminCanton) {
        this.adminCanton = adminCanton;
    }

    public CatParroquia getAdminParroquia() {
        return adminParroquia;
    }

    public void setAdminParroquia(CatParroquia adminParroquia) {
        this.adminParroquia = adminParroquia;
    }

    public String getAdminDireccion() {
        return adminDireccion;
    }

    public void setAdminDireccion(String adminDireccion) {
        this.adminDireccion = adminDireccion;
    }

    public String getAdminCorreo() {
        return adminCorreo;
    }

    public void setAdminCorreo(String adminCorreo) {
        this.adminCorreo = adminCorreo;
    }

    public CtlgItem getNotifTipoDireccion() {
        return notifTipoDireccion;
    }

    public void setNotifTipoDireccion(CtlgItem notifTipoDireccion) {
        this.notifTipoDireccion = notifTipoDireccion;
    }

    public CtlgItem getCiuTipoIdentificacion() {
        return ciuTipoIdentificacion;
    }

    public void setCiuTipoIdentificacion(CtlgItem ciuTipoIdentificacion) {
        this.ciuTipoIdentificacion = ciuTipoIdentificacion;
    }

    public String getCodigoInstNacPatCul() {
        return codigoInstNacPatCul;
    }

    public void setCodigoInstNacPatCul(String codigoInstNacPatCul) {
        this.codigoInstNacPatCul = codigoInstNacPatCul;
    }

    public BigDecimal getPrecioOferta() {
        return precioOferta;
    }

    public void setPrecioOferta(BigDecimal precioOferta) {
        this.precioOferta = precioOferta;
    }

    public String getUsoPh() {
        return usoPh;
    }

    public void setUsoPh(String usoPh) {
        this.usoPh = usoPh;
    }

    public String getDivisionUrb() {
        return divisionUrb;
    }

    public void setDivisionUrb(String divisionUrb) {
        this.divisionUrb = divisionUrb;
    }

    public BigInteger getNumPisos() {
        return numPisos;
    }

    public void setNumPisos(BigInteger numPisos) {
        this.numPisos = numPisos;
    }

    public BigDecimal getCoordx() {
        return coordx;
    }

    public void setCoordx(BigDecimal coordx) {
        this.coordx = coordx;
    }

    public BigDecimal getCoordy() {
        return coordy;
    }

    public void setCoordy(BigDecimal coordy) {
        this.coordy = coordy;
    }

    public CtlgItem getCondicionMunicipal() {
        return condicionMunicipal;
    }

    public void setCondicionMunicipal(CtlgItem condicionMunicipal) {
        this.condicionMunicipal = condicionMunicipal;
    }

    public Integer getCondicionMunicipalAnio() {
        return condicionMunicipalAnio;
    }

    public void setCondicionMunicipalAnio(Integer condicionMunicipalAnio) {
        this.condicionMunicipalAnio = condicionMunicipalAnio;
    }

    public Date getFecMod() {
        return fecMod;
    }

    public void setFecMod(Date fecMod) {
        this.fecMod = fecMod;
    }

    public String getUsrMod() {
        return usrMod;
    }

    public void setUsrMod(String usrMod) {
        this.usrMod = usrMod;
    }

    public Boolean getRevisado() {
        return revisado;
    }

    public void setRevisado(Boolean revisado) {
        this.revisado = revisado;
    }

    public Boolean getProcesados() {
        return procesados;
    }

    public void setProcesados(Boolean procesados) {
        this.procesados = procesados;
    }

    public Integer getHabitantes() {
        return habitantes;
    }

    public void setHabitantes(Integer habitantes) {
        this.habitantes = habitantes;
    }

    public Boolean getNuevo() {
        return nuevo;
    }

    public void setNuevo(Boolean nuevo) {
        this.nuevo = nuevo;
    }

    @Transient
    private String propietarios;

    @Transient
    private Boolean tienePermiso = false;

    public CatPredio() {

    }

    public CatPredio(Long id) {
        this.id = id;
    }

    public CatPredio(Long id, Short sector, Short mz, Short mzdiv, Short solar, Short div1, Short div2, Short div3, Short div4, Short div5, Short div6, Short div7, Short div8, Short div9, Short phv, Short phh, Date instCreacion, Short cdla) {
        this.id = id;
        this.sector = sector;
        this.mz = mz;
        this.mzdiv = mzdiv;
        this.solar = solar;
        this.instCreacion = instCreacion;
    }

    public String getCodigoPredial() {

        return Utils.completarCadenaConCeros(this.getProvincia().toString(), 2)
                + Utils.completarCadenaConCeros(this.getCanton().toString(), 2)
                + Utils.completarCadenaConCeros(this.getParroquia().toString(), 2)
                + Utils.completarCadenaConCeros(this.getZona().toString(), 1)
                + Utils.completarCadenaConCeros(this.getSector().toString(), 1)
                + Utils.completarCadenaConCeros(this.getMz().toString(), 3)
                + Utils.completarCadenaConCeros(this.getSolar().toString(), 3)
                + Utils.completarCadenaConCeros(this.getBloque().toString(), 3)
                + Utils.completarCadenaConCeros(this.getPiso().toString(), 2)
                + Utils.completarCadenaConCeros(this.getUnidad().toString(), 3);
    }

    public String getCodigoPredialCompleto() {
        return this.claveCat;
    }

    public String getCodigoPredialCompletoFormatoG() {
        return this.claveCat;
    }

    public String getCodigoPredialCompletoFormatoP() {
        return this.claveCat;
    }

    public String getCodigoPredialCompletoSinFormato() {
        return this.claveCat;
    }

    public void setCodigoPredialCompleto(String codigoPredial) {

    }

    public String getNombrePropietarios() {
        String nombres = "";
        StringBuilder sb = new StringBuilder();

        if (Objects.equals(cambioNombreTitulo, Boolean.TRUE)) {
            if (nombreCambiado != null) {
                if (!nombreCambiado.equals("")) {
                    nombres = nombreCambiado;
                    return nombres.toUpperCase();
                }
            }
        }

        if (this.catPredioPropietarioCollection != null && !this.catPredioPropietarioCollection.isEmpty()) {
            for (CatPredioPropietario cpp : catPredioPropietarioCollection) {
                if (cpp.getEnte() != null) {
                    if (cpp.getEnte().getEsPersona()) {
                        nombres = (cpp.getEnte().getApellidos() == null ? "" : cpp.getEnte().getApellidos())
                                + " " + (cpp.getEnte().getNombres() == null ? "" : cpp.getEnte().getNombres());
                    } else {
                        nombres = (cpp.getEnte().getRazonSocial() == null ? "" : cpp.getEnte().getRazonSocial());
                    }
                    sb.append(nombres).append(" - ");
                } else {
                    if (cpp.getNombresCompletos() != null) {
                        nombres = cpp.getNombresCompletos();
                        sb.append(nombres).append(" - ");
                    }
                }
            }
        }
        if (sb.length() >= 3) {
            sb.delete(sb.length() - 3, sb.length() - 1);
        }
        return sb.toString().toUpperCase();
    }

    public String getPropietarios() {
        return propietarios;
    }

    public void setPropietarios(String propietarios) {
        this.propietarios = propietarios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumDepartamento() {
        return numDepartamento;
    }

    public void setNumDepartamento(String numDepartamento) {
        this.numDepartamento = numDepartamento;
    }

    public String getNombreEdificio() {
        return nombreEdificio;
    }

    public void setNombreEdificio(String nombreEdificio) {
        this.nombreEdificio = nombreEdificio;
    }

    public String getNombreUrb() {
        return nombreUrb;
    }

    public void setNombreUrb(String nombreUrb) {
        this.nombreUrb = nombreUrb;
    }

    public Short getUrbSec() {
        return urbSec;
    }

    public void setUrbSec(Short urbSec) {
        this.urbSec = urbSec;
    }

    public BigInteger getNumPredio() {
        return numPredio;
    }

    public void setNumPredio(BigInteger numPredio) {
        this.numPredio = numPredio;
    }

    public SectorValorizacion getSubsector() {
        return subsector;
    }

    public void setSubsector(SectorValorizacion subsector) {
        this.subsector = subsector;
    }

    public String getClaveCat() {
        return claveCat;
    }

    public void setClaveCat(String claveCat) {
        this.claveCat = claveCat;
    }

    public Boolean getSoportaHipoteca() {
        return soportaHipoteca;
    }

    public void setSoportaHipoteca(Boolean soportaHipoteca) {
        this.soportaHipoteca = soportaHipoteca;
    }

    public String getUrbMz() {
        return urbMz;
    }

    public void setUrbMz(String urbMz) {
        this.urbMz = urbMz;
    }

    public BigInteger getNumeroFicha() {
        return numeroFicha;
    }

    public void setNumeroFicha(BigInteger numeroFicha) {
        this.numeroFicha = numeroFicha;
    }

    public Date getInstCreacion() {
        return instCreacion;
    }

    public void setInstCreacion(Date instCreacion) {
        this.instCreacion = instCreacion;
    }

    public String getUrbSolarnew() {
        return urbSolarnew;
    }

    public void setUrbSolarnew(String urbSolarnew) {
        this.urbSolarnew = urbSolarnew;
    }

    public String getUrbSecnew() {
        return urbSecnew;
    }

    public void setUrbSecnew(String urbSecnew) {
        this.urbSecnew = urbSecnew;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getPredial97() {
        return predial97;
    }

    public void setPredial97(String predial97) {
        this.predial97 = predial97;
    }

    public String getCalleAv() {
        return calleAv;
    }

    public void setCalleAv(String calleAv) {
        this.calleAv = calleAv;
    }

    public String getPredialant() {
        if (predialant == null || predialant == "") {
            predialant = Utils.completarCadenaConCeros(this.provincia + "", 2)
                    + Utils.completarCadenaConCeros(this.canton + "", 2)
                    + Utils.completarCadenaConCeros(this.parroquia + "", 2)
                    + Utils.completarCadenaConCeros(this.zona + "", 2)
                    + Utils.completarCadenaConCeros(this.sector + "", 2)
                    + Utils.completarCadenaConCeros(this.mz + "", 2)
                    + Utils.completarCadenaConCeros(this.solar + "", 3)
                    + Utils.completarCadenaConCeros(this.bloque + "", 3);
        }
        if (predialant.length() > 18) {
            predialant = Utils.completarCadenaConCeros(this.provincia + "", 2)
                    + Utils.completarCadenaConCeros(this.canton + "", 2)
                    + Utils.completarCadenaConCeros(this.parroquia + "", 2)
                    + Utils.completarCadenaConCeros(this.zona + "", 2)
                    + Utils.completarCadenaConCeros(this.sector + "", 2)
                    + Utils.completarCadenaConCeros(this.mz + "", 2)
                    + Utils.completarCadenaConCeros(this.solar + "", 3)
                    + Utils.completarCadenaConCeros(this.bloque + "", 3);
        }
        return predialant;
    }

    public void setPredialant(String predialant) {
        this.predialant = predialant;
    }

    public String getNomCompPago() {
        return nomCompPago;
    }

    public void setNomCompPago(String nomCompPago) {
        this.nomCompPago = nomCompPago;
    }

    public Boolean getPropiedadHorizontal() {
        return propiedadHorizontal;
    }

    public void setPropiedadHorizontal(Boolean propiedadHorizontal) {
        this.propiedadHorizontal = propiedadHorizontal;
    }

    public BigInteger getPredioRaiz() {
        return predioRaiz;
    }

    public void setPredioRaiz(BigInteger predioRaiz) {
        this.predioRaiz = predioRaiz;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTipoPredio() {
        return tipoPredio;
    }

    public void setTipoPredio(String tipoPredio) {
        this.tipoPredio = tipoPredio;
    }

    public BigDecimal getAvaluoSolar() {
        return avaluoSolar;
    }

    public void setAvaluoSolar(BigDecimal avaluoSolar) {
        this.avaluoSolar = avaluoSolar;
    }

    public BigDecimal getAvaluoConstruccion() {
        return avaluoConstruccion;
    }

    public void setAvaluoConstruccion(BigDecimal avaluoConstruccion) {
        this.avaluoConstruccion = avaluoConstruccion;
    }

    public BigDecimal getAvaluoMunicipal() {
        return avaluoMunicipal;
    }

    public void setAvaluoMunicipal(BigDecimal avaluoMunicipal) {
        this.avaluoMunicipal = avaluoMunicipal;
    }

    public BigDecimal getAvaluoCultivos() {
        return avaluoCultivos;
    }

    public void setAvaluoCultivos(BigDecimal avaluoCultivos) {
        this.avaluoCultivos = avaluoCultivos;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<CatEscritura> getCatEscrituraCollection() {
        return catEscrituraCollection;
    }

    public void setCatEscrituraCollection(Collection<CatEscritura> catEscrituraCollection) {
        this.catEscrituraCollection = catEscrituraCollection;
    }

    public CatPredioS4 getCatPredioS4() {
        return catPredioS4;
    }

    public void setCatPredioS4(CatPredioS4 catPredioS4) {
        this.catPredioS4 = catPredioS4;
    }

    public CatPredioS6 getCatPredioS6() {
        return catPredioS6;
    }

    public void setCatPredioS6(CatPredioS6 catPredioS6) {
        this.catPredioS6 = catPredioS6;
    }

    public Collection<CatPredioPropietario> getCatPredioPropietarioCollection() {
        return catPredioPropietarioCollection;
    }

    public void setCatPredioPropietarioCollection(Collection<CatPredioPropietario> catPredioPropietarioCollection) {
        this.catPredioPropietarioCollection = catPredioPropietarioCollection;
    }

    public CatTipoConjunto getTipoConjunto() {
        return tipoConjunto;
    }

    public void setTipoConjunto(CatTipoConjunto tipoConjunto) {
        this.tipoConjunto = tipoConjunto;
    }

    public CatTenenciaItem getTenencia() {
        return tenencia;
    }

    public void setTenencia(CatTenenciaItem tenencia) {
        this.tenencia = tenencia;
    }

    public CtlgItem getPropiedad() {
        return propiedad;
    }

    public void setPropiedad(CtlgItem propiedad) {
        this.propiedad = propiedad;
    }

    public CatCiudadela getCiudadela() {
        return ciudadela;
    }

    public void setCiudadela(CatCiudadela ciudadela) {
        this.ciudadela = ciudadela;
    }

    public AclUser getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(AclUser usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public CatPredioS12 getCatPredioS12() {
        return catPredioS12;
    }

    public void setCatPredioS12(CatPredioS12 catPredioS12) {
        this.catPredioS12 = catPredioS12;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<CatPredioEdificacion> getCatPredioEdificacionCollection() {
        return catPredioEdificacionCollection;
    }

    public void setCatPredioEdificacionCollection(Collection<CatPredioEdificacion> catPredioEdificacionCollection) {
        this.catPredioEdificacionCollection = catPredioEdificacionCollection;
    }

    public Collection<HistoricoTramiteDet> getHistoricoTramiteDetCollection() {
        return historicoTramiteDetCollection;
    }

    public void setHistoricoTramiteDetCollection(Collection<HistoricoTramiteDet> historicoTramiteDetCollection) {
        this.historicoTramiteDetCollection = historicoTramiteDetCollection;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
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
        if (!(object instanceof CatPredio)) {
            return false;
        }
        CatPredio other = (CatPredio) object;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "CatPredio " + id;
    }

    public BigDecimal getAreaSolar() {
        return areaSolar;
    }

    public void setAreaSolar(BigDecimal areaSolar) {
        this.areaSolar = areaSolar;
    }

    public BigDecimal getAreaObras() {
        return areaObras;
    }

    public void setAreaObras(BigDecimal areaObras) {
        this.areaObras = areaObras;
    }

    public BigDecimal getAreaCultivos() {
        return areaCultivos;
    }

    public void setAreaCultivos(BigDecimal areaCultivos) {
        this.areaCultivos = areaCultivos;
    }

    public BigDecimal getAreaDeclaradaConst() {
        if (Objects.isNull(areaDeclaradaConst)) {
            return BigDecimal.ZERO;
        }

        return areaDeclaradaConst;
    }

    public void setAreaDeclaradaConst(BigDecimal areaDeclaradaConst) {
        this.areaDeclaradaConst = areaDeclaradaConst;
    }

    public BigDecimal getAmri() {
        return amri;
    }

    public void setAmri(BigDecimal amri) {
        this.amri = amri;
    }

    public String getZonaPu() {
        return zonaPu;
    }

    public void setZonaPu(String zonaPu) {
        this.zonaPu = zonaPu;
    }

    public CtlgItem getFormaSolar() {
        return formaSolar;
    }

    public void setFormaSolar(CtlgItem formaSolar) {
        this.formaSolar = formaSolar;
    }

    public String getCodCategoria() {
        return codCategoria;
    }

    public void setCodCategoria(String codCategoria) {
        this.codCategoria = codCategoria;
    }

    public CtlgItem getTopografiaSolar() {
        return topografiaSolar;
    }

    public void setTopografiaSolar(CtlgItem topografiaSolar) {
        this.topografiaSolar = topografiaSolar;
    }

    public CtlgItem getTipovia() {
        return tipovia;
    }

    public void setTipovia(CtlgItem tipovia) {
        this.tipovia = tipovia;
    }

    public CtlgItem getTipoSuelo() {
        return tipoSuelo;
    }

    public void setTipoSuelo(CtlgItem tipoSuelo) {
        this.tipoSuelo = tipoSuelo;
    }

    public Collection<CatCertificadoAvaluo> getCatCertificadoAvaluoCollection() {
        return catCertificadoAvaluoCollection;
    }

    public void setCatCertificadoAvaluoCollection(Collection<CatCertificadoAvaluo> catCertificadoAvaluoCollection) {
        this.catCertificadoAvaluoCollection = catCertificadoAvaluoCollection;
    }

    public Boolean getTienePermiso() {
        return tienePermiso;
    }

    public void setTienePermiso(Boolean tienePermiso) {
        this.tienePermiso = tienePermiso;
    }

    public String getCalleS() {
        return calleS;
    }

    public void setCalleS(String calleS) {
        this.calleS = calleS;
    }

    public String getNumeroVivienda() {
        return numeroVivienda;
    }

    public void setNumeroVivienda(String numeroVivienda) {
        this.numeroVivienda = numeroVivienda;
    }

    public BigDecimal getAlicuotaUtil() {
        return alicuotaUtil;
    }

    public void setAlicuotaUtil(BigDecimal alicuotaUtil) {
        this.alicuotaUtil = alicuotaUtil;
    }

    public BigDecimal getAlicuotaConst() {
        return alicuotaConst;
    }

    public void setAlicuotaConst(BigDecimal alicuotaConst) {
        this.alicuotaConst = alicuotaConst;
    }

    public CtlgItem getUsoSolar() {
        return usoSolar;
    }

    public void setUsoSolar(CtlgItem usoSolar) {
        this.usoSolar = usoSolar;
    }

    public CtlgItem getConstructividad() {
        return constructividad;
    }

    public void setConstructividad(CtlgItem constructividad) {
        this.constructividad = constructividad;
    }

    public Short getZona() {
        return zona;
    }

    public void setZona(Short zona) {
        this.zona = zona;
    }

    public Short getLote() {
        return solar;
    }

    public void setLote(Short lote) {
        this.solar = lote;
    }

    public Short getBloque() {
        if (bloque == null) {
            bloque = new Short("0");
        }
        return bloque;
    }

    public void setBloque(Short bloque) {
        this.bloque = bloque;
    }

    public Short getPiso() {
        return piso;
    }

    public void setPiso(Short piso) {
        this.piso = piso;
    }

    public Short getUnidad() {
        return unidad;
    }

    public void setUnidad(Short unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getAreaConstPh() {
        return areaConstPh;
    }

    public void setAreaConstPh(BigDecimal areaConstPh) {
        this.areaConstPh = areaConstPh;
    }

    public Short getSector() {
        return sector;
    }

    public void setSector(Short sector) {
        this.sector = sector;
    }

    public Short getProvincia() {
        return provincia;
    }

    public void setProvincia(Short provincia) {
        this.provincia = provincia;
    }

    public Short getCanton() {
        return canton;
    }

    public void setCanton(Short canton) {
        this.canton = canton;
    }

    public Short getParroquia() {
        return parroquia;
    }

    public void setParroquia(Short parroquia) {
        this.parroquia = parroquia;
    }

    public Short getMz() {
        return mz;
    }

    public void setMz(Short mz) {
        this.mz = mz;
    }

    public Short getMzdiv() {
        return mzdiv;
    }

    public void setMzdiv(Short mzdiv) {
        this.mzdiv = mzdiv;
    }

    public Short getSolar() {
        return solar;
    }

    public void setSolar(Short solar) {
        this.solar = solar;
    }

    public Boolean getCrear() {
        return crear;
    }

    public void setCrear(Boolean crear) {
        this.crear = crear;
    }

    public CtlgItem getOtroTipovia() {
        return otroTipovia;
    }

    public void setOtroTipovia(CtlgItem otroTipovia) {
        this.otroTipovia = otroTipovia;
    }

    public CatEnte getResponsableActualizadorPredial() {
        return responsableActualizadorPredial;
    }

    public void setResponsableActualizadorPredial(CatEnte responsableActualizadorPredial) {
        this.responsableActualizadorPredial = responsableActualizadorPredial;
    }

    public CatEnte getResponsableFiscalizadorPredial() {
        return responsableFiscalizadorPredial;
    }

    public void setResponsableFiscalizadorPredial(CatEnte responsableFiscalizadorPredial) {
        this.responsableFiscalizadorPredial = responsableFiscalizadorPredial;
    }

    public CtlgItem getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(CtlgItem unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public Boolean getTipoViviendaHorizontal() {
        return tipoViviendaHorizontal;
    }

    public void setTipoViviendaHorizontal(Boolean tipoViviendaHorizontal) {
        this.tipoViviendaHorizontal = tipoViviendaHorizontal;
    }

    public Boolean getOcupacionViviendaHorizontal() {
        return ocupacionViviendaHorizontal;
    }

    public void setOcupacionViviendaHorizontal(Boolean ocupacionViviendaHorizontal) {
        this.ocupacionViviendaHorizontal = ocupacionViviendaHorizontal;
    }

    public CatEnte getEnteHorizontal() {
        return enteHorizontal;
    }

    public void setEnteHorizontal(CatEnte enteHorizontal) {
        this.enteHorizontal = enteHorizontal;
    }

    public CtlgItem getClasificacionViviendaHorizontal() {
        return clasificacionViviendaHorizontal;
    }

    public void setClasificacionViviendaHorizontal(CtlgItem clasificacionViviendaHorizontal) {
        this.clasificacionViviendaHorizontal = clasificacionViviendaHorizontal;
    }

    public Short getNumHogares() {
        return numHogares;
    }

    public void setNumHogares(Short numHogares) {
        this.numHogares = numHogares;
    }

    public Short getNumHabitaciones() {
        return numHabitaciones;
    }

    public void setNumHabitaciones(Short numHabitaciones) {
        this.numHabitaciones = numHabitaciones;
    }

    public Short getNumDormitorios() {
        return numDormitorios;
    }

    public void setNumDormitorios(Short numDormitorios) {
        this.numDormitorios = numDormitorios;
    }

    public Short getNumEspaciosBanios() {
        return numEspaciosBanios;
    }

    public void setNumEspaciosBanios(Short numEspaciosBanios) {
        this.numEspaciosBanios = numEspaciosBanios;
    }

    public Short getNumCelulares() {
        return numCelulares;
    }

    public void setNumCelulares(Short numCelulares) {
        this.numCelulares = numCelulares;
    }

    public Boolean getRequierePerfeccionamiento() {
        return requierePerfeccionamiento;
    }

    public void setRequierePerfeccionamiento(Boolean requierePerfeccionamiento) {
        this.requierePerfeccionamiento = requierePerfeccionamiento;
    }

    public Integer getAniosSinPerfeccionamiento() {
        return aniosSinPerfeccionamiento;
    }

    public void setAniosSinPerfeccionamiento(Integer aniosSinPerfeccionamiento) {
        this.aniosSinPerfeccionamiento = aniosSinPerfeccionamiento;
    }

    public Integer getAniosPosesion() {
        return aniosPosesion;
    }

    public void setAniosPosesion(Integer aniosPosesion) {
        this.aniosPosesion = aniosPosesion;
    }

    public String getNombrePuebloEtnia() {
        return nombrePuebloEtnia;
    }

    public void setNombrePuebloEtnia(String nombrePuebloEtnia) {
        this.nombrePuebloEtnia = nombrePuebloEtnia;
    }

    public CtlgItem getTenenciaVivienda() {
        return tenenciaVivienda;
    }

    public void setTenenciaVivienda(CtlgItem tenenciaVivienda) {
        this.tenenciaVivienda = tenenciaVivienda;
    }

    public CtlgItem getClasificacionSuelo() {
        return clasificacionSuelo;
    }

    public void setClasificacionSuelo(CtlgItem clasificacionSuelo) {
        this.clasificacionSuelo = clasificacionSuelo;
    }

    public CatEscritura getEscrituraLinderos() {
        return escrituraLinderos;
    }

    public void setEscrituraLinderos(CatEscritura escrituraLinderos) {
        this.escrituraLinderos = escrituraLinderos;
    }

    public CatClaveReordenada getClaveReordenada() {
        return claveReordenada;
    }

    public void setClaveReordenada(CatClaveReordenada claveReordenada) {
        this.claveReordenada = claveReordenada;
    }

    public CtlgItem getTipoPoseedor() {
        return tipoPoseedor;
    }

    public void setTipoPoseedor(CtlgItem tipoPoseedor) {
        this.tipoPoseedor = tipoPoseedor;
    }

    public CatEnte getInformante() {
        return informante;
    }

    public void setInformante(CatEnte informante) {
        this.informante = informante;
    }

    public String getDireccion() {

        return (this.ciudadela == null ? "" : this.ciudadela.getNombre())
                + (calle == null ? "" : " Calle principal: " + calle)
                + (calleS == null ? "" : " Calle Secudaria: " + calleS);
    }

    public Boolean getFichaMadre() {
        if (fichaMadre == null) {
            fichaMadre = false;
        }
        return fichaMadre;
    }

    public void setFichaMadre(Boolean fichaMadre) {
        this.fichaMadre = fichaMadre;
    }

    public String getAdminFullName() {
        return adminFullName;
    }

    public void setAdminFullName(String adminFullName) {
        this.adminFullName = adminFullName;
    }

    public String getAdminCedula() {
        return adminCedula;
    }

    public void setAdminCedula(String adminCedula) {
        this.adminCedula = adminCedula;
    }

    public String getAdminTelefono() {
        return adminTelefono;
    }

    public void setAdminTelefono(String adminTelefono) {
        this.adminTelefono = adminTelefono;
    }

    public String getAdminCelular() {
        return adminCelular;
    }

    public void setAdminCelular(String adminCelular) {
        this.adminCelular = adminCelular;
    }

    public CatEscritura getEscritura() {
        return escritura;
    }

    public void setEscritura(CatEscritura escritura) {
        this.escritura = escritura;
    }

    public Boolean getTieneEscritura() {
        return tieneEscritura;
    }

    public void setTieneEscritura(Boolean tieneEscritura) {
        this.tieneEscritura = tieneEscritura;
    }

    public Integer getCantAlicuotas() {
        return cantAlicuotas;
    }

    public void setCantAlicuotas(Integer cantAlicuotas) {
        this.cantAlicuotas = cantAlicuotas;
    }

    public List<CatPredioAlicuotaComponente> getAlicuotaComponentes() {
        return alicuotaComponentes;
    }

    public void setAlicuotaComponentes(List<CatPredioAlicuotaComponente> alicuotaComponentes) {
        this.alicuotaComponentes = alicuotaComponentes;
    }

    public List<CatPredioLinderos> getCatPredioLinderosCollection() {
        return catPredioLinderosCollection;
    }

    public void setCatPredioLinderosCollection(List<CatPredioLinderos> catPredioLinderosCollection) {
        this.catPredioLinderosCollection = catPredioLinderosCollection;
    }

    public List<CatPredioLinderos> getPredioCollection() {
        return predioCollection;
    }

    public void setPredioCollection(List<CatPredioLinderos> predioCollection) {
        this.predioCollection = predioCollection;
    }

    public CtlgItem getClasificacionVivienda() {
        return clasificacionVivienda;
    }

    public void setClasificacionVivienda(CtlgItem clasificacionVivienda) {
        this.clasificacionVivienda = clasificacionVivienda;
    }

    public CtlgItem getTipoVivienda() {
        return tipoVivienda;
    }

    public void setTipoVivienda(CtlgItem tipoVivienda) {
        this.tipoVivienda = tipoVivienda;
    }

    public CtlgItem getCondicionVivienda() {
        return condicionVivienda;
    }

    public void setCondicionVivienda(CtlgItem condicionVivienda) {
        this.condicionVivienda = condicionVivienda;
    }

    public BigDecimal getAvaluoObraComplement() {
        return avaluoObraComplement;
    }

    public void setAvaluoObraComplement(BigDecimal avaluoObraComplement) {
        this.avaluoObraComplement = avaluoObraComplement;
    }

    public BigDecimal getAvaluoPlussolar() {
        return avaluoPlussolar;
    }

    public void setAvaluoPlussolar(BigDecimal avaluoPlussolar) {
        this.avaluoPlussolar = avaluoPlussolar;
    }

    public BigDecimal getAvaluoPlusconstruccion() {
        return avaluoPlusconstruccion;
    }

    public void setAvaluoPlusconstruccion(BigDecimal avaluoPlusconstruccion) {
        this.avaluoPlusconstruccion = avaluoPlusconstruccion;
    }

    public BigDecimal getAvaluoPlusmunicipal() {
        return avaluoPlusmunicipal;
    }

    public void setAvaluoPlusmunicipal(BigDecimal avaluoPlusmunicipal) {
        this.avaluoPlusmunicipal = avaluoPlusmunicipal;
    }

    public BigDecimal getAvaluoPluscultivos() {
        return avaluoPluscultivos;
    }

    public void setAvaluoPluscultivos(BigDecimal avaluoPluscultivos) {
        this.avaluoPluscultivos = avaluoPluscultivos;
    }

    public BigDecimal getAvaluoPluObraComplement() {
        return avaluoPluObraComplement;
    }

    public void setAvaluoPluObraComplement(BigDecimal avaluoPluObraComplement) {
        this.avaluoPluObraComplement = avaluoPluObraComplement;
    }

    public BigDecimal getBaseImponible() {
        return baseImponible;
    }

    public void setBaseImponible(BigDecimal baseImponible) {
        this.baseImponible = baseImponible;
    }

    public CtlgItem getUsoVia() {
        return usoVia;
    }

    public void setUsoVia(CtlgItem usoVia) {
        this.usoVia = usoVia;
    }

    public CtlgItem getFormaAdquisicion() {
        return formaAdquisicion;
    }

    public void setFormaAdquisicion(CtlgItem formaAdquisicion) {
        this.formaAdquisicion = formaAdquisicion;
    }

    public CtlgItem getVivCencalAcabadoPiso() {
        return vivCencalAcabadoPiso;
    }

    public void setVivCencalAcabadoPiso(CtlgItem vivCencalAcabadoPiso) {
        this.vivCencalAcabadoPiso = vivCencalAcabadoPiso;
    }

    public CtlgItem getVivCencalEstadoAcabadoPiso() {
        return vivCencalEstadoAcabadoPiso;
    }

    public void setVivCencalEstadoAcabadoPiso(CtlgItem vivCencalEstadoAcabadoPiso) {
        this.vivCencalEstadoAcabadoPiso = vivCencalEstadoAcabadoPiso;
    }

    public Boolean getVivCencalPoseeTelfConvencional() {
        return vivCencalPoseeTelfConvencional;
    }

    public void setVivCencalPoseeTelfConvencional(Boolean vivCencalPoseeTelfConvencional) {
        this.vivCencalPoseeTelfConvencional = vivCencalPoseeTelfConvencional;
    }

    public Boolean getVivCencalServInternet() {
        return vivCencalServInternet;
    }

    public void setVivCencalServInternet(Boolean vivCencalServInternet) {
        this.vivCencalServInternet = vivCencalServInternet;
    }

    public String getPropAnteriorPredio() {
        return propAnteriorPredio;
    }

    public void setPropAnteriorPredio(String propAnteriorPredio) {
        this.propAnteriorPredio = propAnteriorPredio;
    }

    public String getCedulaPropAnterior() {
        return cedulaPropAnterior;
    }

    public void setCedulaPropAnterior(String cedulaPropAnterior) {
        this.cedulaPropAnterior = cedulaPropAnterior;
    }

    public BigDecimal getAreaTerrenoAlicuota() {
        return areaTerrenoAlicuota;
    }

    public void setAreaTerrenoAlicuota(BigDecimal areaTerrenoAlicuota) {
        this.areaTerrenoAlicuota = areaTerrenoAlicuota;
    }

    public BigDecimal getAreaAumentoCons() {
        if (Objects.isNull(areaAumentoCons)) {
            return BigDecimal.ZERO;
        }

        return areaAumentoCons;
    }

    public void setAreaAumentoCons(BigDecimal areaAumentoCons) {
        this.areaAumentoCons = areaAumentoCons;
    }

    public BigDecimal getAreaTotalCons() {
        return areaTotalCons;
    }

    public void setAreaTotalCons(BigDecimal areaTotalCons) {
        this.areaTotalCons = areaTotalCons;
    }

    public BigDecimal getAlicuotaTerreno() {
        return alicuotaTerreno;
    }

    public void setAlicuotaTerreno(BigDecimal alicuotaTerreno) {
        this.alicuotaTerreno = alicuotaTerreno;
    }

    public List<CatPredioCultivo> getCatPredioCultivoCollection() {
        return catPredioCultivoCollection;
    }

    public void setCatPredioCultivoCollection(List<CatPredioCultivo> catPredioCultivoCollection) {
        this.catPredioCultivoCollection = catPredioCultivoCollection;
    }

    public String getCiRucInformante() {
        return ciRucInformante;
    }

    public void setCiRucInformante(String ciRucInformante) {
        this.ciRucInformante = ciRucInformante;
    }

    public String getNombreInformante() {
        return nombreInformante;
    }

    public void setNombreInformante(String nombreInformante) {
        this.nombreInformante = nombreInformante;
    }

    public String getApellidosInformante() {
        return apellidosInformante;
    }

    public void setApellidosInformante(String apellidosInformante) {
        this.apellidosInformante = apellidosInformante;
    }

    public String getCiuActualizador() {
        return ciuActualizador;
    }

    public void setCiuActualizador(String ciuActualizador) {
        this.ciuActualizador = ciuActualizador;
    }

    public String getCiuFiscalizador() {
        return ciuFiscalizador;
    }

    public void setCiuFiscalizador(String ciuFiscalizador) {
        this.ciuFiscalizador = ciuFiscalizador;
    }

    public String getCiuNombresActualizador() {
        return ciuNombresActualizador;
    }

    public void setCiuNombresActualizador(String ciuNombresActualizador) {
        this.ciuNombresActualizador = ciuNombresActualizador;
    }

    public String getCiuNombresFiscalizador() {
        return ciuNombresFiscalizador;
    }

    public void setCiuNombresFiscalizador(String ciuNombresFiscalizador) {
        this.ciuNombresFiscalizador = ciuNombresFiscalizador;
    }

    public String getCiuInformante() {
        return ciuInformante;
    }

    public void setCiuInformante(String ciuInformante) {
        this.ciuInformante = ciuInformante;
    }

    public String getCiuNombresInformante() {
        return ciuNombresInformante;
    }

    public void setCiuNombresInformante(String ciuNombresInformante) {
        this.ciuNombresInformante = ciuNombresInformante;
    }

    public String getCiuHorizontal() {
        return ciuHorizontal;
    }

    public void setCiuHorizontal(String ciuHorizontal) {
        this.ciuHorizontal = ciuHorizontal;
    }

    public String getCiuNombresHorizontal() {
        return ciuNombresHorizontal;
    }

    public void setCiuNombresHorizontal(String ciuNombresHorizontal) {
        this.ciuNombresHorizontal = ciuNombresHorizontal;
    }

    public Collection<CatPredioObraInterna> getCatPredioObraInternaCollection() {
        return catPredioObraInternaCollection;
    }

    public void setCatPredioObraInternaCollection(Collection<CatPredioObraInterna> catPredioObraInternaCollection) {
        this.catPredioObraInternaCollection = catPredioObraInternaCollection;
    }

    public List<CatPredioClasificRural> getCatPredioClasificRuralCollection() {
        return catPredioClasificRuralCollection;
    }

    public void setCatPredioClasificRuralCollection(List<CatPredioClasificRural> catPredioClasificRuralCollection) {
        this.catPredioClasificRuralCollection = catPredioClasificRuralCollection;
    }

    public String getPredialantAnt() {
        return predialantAnt;
    }

    public void setPredialantAnt(String predialantAnt) {
        this.predialantAnt = predialantAnt;
    }

    public List<CatPredioAvalHistorico> getCatPredioAvalHistoricos() {
        return catPredioAvalHistoricos;
    }

    public void setCatPredioAvalHistoricos(List<CatPredioAvalHistorico> catPredioAvalHistoricos) {
        this.catPredioAvalHistoricos = catPredioAvalHistoricos;
    }

    public List<CatPredioPropietario> propietarios() {

        if (this.catPredioPropietarioCollection != null) {
            return (ArrayList<CatPredioPropietario>) catPredioPropietarioCollection;
        }

        return new ArrayList<>();
    }

    public CatParroquia getCatParroquia() {
        if (catParroquia == null) {
            Map<String, Object> pm = new HashMap<>();
            pm.put("codNac", parroquia);
            pm.put("idCanton", EjbsCaller.getTransactionManager().find(Querys.getParroquiasByCanton,
                    new String[]{"codigoNacional", "codNac"}, new Object[]{canton, provincia}));
            catParroquia = EjbsCaller.getTransactionManager().findObjectByParameter(CatParroquia.class, pm);
        }

        return catParroquia;
    }

    public void setCatParroquia(CatParroquia catParroquia) {
        this.catParroquia = catParroquia;
    }

    public List<String> getFotos() {
        return fotos;
    }

    public void setFotos(List<String> fotos) {
        this.fotos = fotos;
    }

    public List<String> getFotosData() {
        return fotosData;
    }

    public void setFotosData(List<String> fotosData) {
        this.fotosData = fotosData;
    }

    public Boolean getCambioNombreTitulo() {
        return cambioNombreTitulo;
    }

    public void setCambioNombreTitulo(Boolean cambioNombreTitulo) {
        this.cambioNombreTitulo = cambioNombreTitulo;
    }

    public String getNombreCambiado() {
        return nombreCambiado;
    }

    public void setNombreCambiado(String nombreCambiado) {
        this.nombreCambiado = nombreCambiado;
    }

    public CatPredioEdificacion getEdificacion() {
        return edificacion;
    }

    public void setEdificacion(CatPredioEdificacion edificacion) {
        this.edificacion = edificacion;
    }

    public String getDireccionPredio() {
        return Utils.isEmpty(getCalle()) + (getCalleS() == null ? "" : " y " + Utils.isEmpty(getCalleS()));
    }

    public void cargarAreaAvaluos() {
        BigDecimal areaBloque = BigDecimal.ZERO;
        BigDecimal avaluoBloque = BigDecimal.ZERO;

        if (Utils.isNotEmpty(this.catPredioEdificacionCollection)) {
            Hibernate.initialize(this.catPredioEdificacionCollection);
            for (CatPredioEdificacion ed : this.catPredioEdificacionCollection) {
                HiberUtil.refreshObject(ed);
                if (ed.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
                    areaBloque = areaBloque.add(ed.getAreaBloque());
                    if (ed.getAvaluo() == null) {
                        ed.setAvaluo(BigDecimal.ZERO);
                    }
                    avaluoBloque = avaluoBloque.add(ed.getAvaluo());
                }
            }
        }
        this.areaDeclaradaConst = areaBloque;
        this.avaluoConstruccion = avaluoBloque;
        if (this.avaluoSolar == null) {
            this.avaluoSolar = BigDecimal.ZERO;
        }
        if (this.avaluoObraComplement == null) {
            this.avaluoObraComplement = BigDecimal.ZERO;
        }
        this.avaluoMunicipal = avaluoBloque.add(this.avaluoSolar).add(this.avaluoObraComplement);
        this.baseImponible = this.avaluoMunicipal;
    }

    public Boolean verificarIngresoPropietario() {
        if (Utils.isNotEmpty(this.catPredioPropietarioCollection)) {
            Double proc = 0.0;
            for (CatPredioPropietario cpp : catPredioPropietarioCollection) {
                if (cpp.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
                    if (cpp.getTipo().getOrden() == 1 && cpp.getPorcentajePosecion().doubleValue() > 0) {
                        return true;
                    }
                    if (cpp.getTipo().getOrden() != 1) {
                        if (cpp.getPorcentajePosecion() == null) {
                            cpp.setPorcentajePosecion(BigDecimal.ZERO);
                        }
                        proc = proc + cpp.getPorcentajePosecion().doubleValue();

                    }
                }
            }
            if (proc < 100) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public CatEnte getPropietario() {
        return propietario;
    }

    public void setPropietario(CatEnte propietario) {
        this.propietario = propietario;
    }

    /**
     * Genera la clave catastral hasta la manzana considerando la clave de 24
     * digitos
     *
     * @return Clave catastral hasta la manzana
     */
    public String getCodigoManzana() {
        return Utils.completarCadenaConCeros(this.provincia + "", 2)
                + Utils.completarCadenaConCeros(this.canton + "", 2)
                + Utils.completarCadenaConCeros(this.parroquia + "", 2)
                + Utils.completarCadenaConCeros(this.zona + "", 2)
                + Utils.completarCadenaConCeros(this.sector + "", 2)
                + Utils.completarCadenaConCeros(this.mz + "", 3);
    }

    /**
     * Genera la clave catastral hasta la manzana considerando la clave de 18
     * digitos
     *
     * @return Clave catastral hasta la manzana
     */
    public String getCodigoManzanaAnt() {
        return Utils.completarCadenaConCeros(this.provincia + "", 2)
                + Utils.completarCadenaConCeros(this.canton + "", 2)
                + Utils.completarCadenaConCeros(this.parroquia + "", 2)
                + Utils.completarCadenaConCeros(this.zona + "", 2)
                + Utils.completarCadenaConCeros(this.sector + "", 2)
                + Utils.completarCadenaConCeros(this.mz + "", 2);
    }

    public Boolean getEstaTarea() {
        if (Utils.isNotEmpty(historicoTramiteDetCollection)) {
            for (HistoricoTramiteDet htd : historicoTramiteDetCollection) {
                if (htd.getTramite() != null) {
                    if (htd.getTramite().getEstado().toUpperCase().contains("FINALIZADO")) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return false;
    }

}
