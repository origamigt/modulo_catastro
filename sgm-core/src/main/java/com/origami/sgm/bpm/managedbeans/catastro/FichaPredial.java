package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.catastroextras.entities.ibarra.Restricciones;
import com.origami.catastroextras.entities.ibarra.RestricionPredio;
import com.origami.catastroextras.entities.ibarra.RestricionPredioPK;
import com.origami.censocat.restful.JsonUtils;
import com.origami.config.ConfigFichaPredial;
import com.origami.config.MainConfig;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.event.FichaPredialOnLoadEvent;
import com.origami.sgm.database.Querys;
import com.origami.sgm.database.SchemasConfig;
import com.origami.sgm.entities.CatCanton;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioAvalHistorico;
import com.origami.sgm.entities.CatPredioClasificRural;
import com.origami.sgm.entities.CatPredioCultivo;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioEdificacionProp;
import com.origami.sgm.entities.CatPredioFusionDivision;
import com.origami.sgm.entities.CatPredioObraInterna;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatPredioS12;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatPredioS6;
import com.origami.sgm.entities.CatProvincia;
import com.origami.sgm.entities.CatTipoConjunto;
import com.origami.sgm.entities.CatTiposDominio;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.GeDocumentos;
import com.origami.sgm.entities.PeInspeccionFinal;
import com.origami.sgm.entities.avaluos.SectorValorizacion;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.enums.TipoProceso;
import com.origami.sgm.events.GenerarHistoricoPredioPost;
import com.origami.sgm.geo.GeodataService;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.lazymodels.CatEnteLazy;
import com.origami.sgm.reportes.ReportesView;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.ejbs.censocat.UploadDocumento;
import com.origami.sgm.services.ejbs.censocat.UploadFotoBean;
import com.origami.sgm.services.interfaces.catastro.FusionDivisionServices;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.GeoVias;
import com.origami.sgmee.catastro.services.ProcesoServices;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.HibernateException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import util.EntityBeanCopy;
import util.Faces;
import util.FilesUtil;
import util.HiberUtil;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author CarlosLoorVargas
 * @override Angel Navarro
 */
@Named
@ViewScoped
public class FichaPredial extends PredioUtil implements Serializable {

    private static final long serialVersionUID = 8799656478674716638L;
    @Inject
    protected ServletSession ss;
    @Inject
    protected ReportesView reportes;
    @Inject
    protected OmegaUploader fserv;
    @Inject
    protected UploadFotoBean fotoBean;
    @Inject
    protected UploadDocumento documentoBean;
    @javax.inject.Inject
    protected GeodataService geodataService;
    @Inject
    protected Event<FichaPredialOnLoadEvent> loadEvent;
    @Inject
    protected FusionDivisionServices divisionServices;
    @Inject
    protected Event<GenerarHistoricoPredioPost> generarHistoricoEvent;
    @Inject
    protected ServiceDataBaseIb dataBaseIb;
    @Inject
    protected ProcesoServices procesoServices;

    protected Boolean editable = false;
    protected CatEscritura escr;
    protected CatCanton canton;
    protected Long predioLink;

    private CatPredioS12 usos;
    protected CatEscritura escritura = new CatEscritura();
    protected BaseLazyDataModel<CatEscritura> escrituras;
    protected List<CatPredioPropietario> propietarios;
    private static final Logger LOG = Logger.getLogger(FichaPredial.class.getName());
    protected CtlgItem tipoPrototipos;
    protected List<FotoPredio> fotos;
    protected List<SectorValorizacion> subsectores;
    protected CatParroquia parroquia;
    protected List<CatCanton> cantones;
    protected List<CatEscritura> escriturasConsulta;
    protected Boolean controlAddUp;
    protected CatEnteLazy responsablesLazy;
    protected CatEnte actualizadorPredio;
    protected CatEnte fiscalizadorPredio;
    protected CatEnte enteHorizontal;
    protected Integer tipoEnte;
    protected Boolean esPersona = true;
    protected String controlResponsable;
    protected String elementosConstuctivos;
    protected CatCanton cantonId;

    protected String predioId;
    protected String claveCat;
    protected Boolean coopropietarios;
    protected Boolean ph = Boolean.FALSE;
//variaables para cuando el predio fue dividido y se necesita mostrar la raiz
    protected CatPredioFusionDivision catPredioFusionDivision;

    protected String observacionEliminar;
    protected String updateTableDocumento;
    protected String observacionRestriccion;
    protected List<String> observacionRestricciones;
    protected Object objectoEliminar;
    protected Integer tipoEliminar;

    protected Boolean esTarea = false;
    protected Boolean esTareaAp = false;
    protected List<CatPredioAvalHistorico> avaluosHistoricosPredio;

    protected Short codProvincia = SisVars.PROVINCIA;

    protected String observaciones;
    protected int tipoTarea = 1;
//    @PostConstruct

    private BigDecimal valorMetro2;

    protected String updateScripture;
    protected String updateLinderos;
    protected Boolean editarClave = false;

    // VARIABLES INTEGRACION GRAFICA
//    private Integer tipoEliminar;
//    private Boolean esTarea = false;
    private String tipoDocumentoAdj;

    // private CatPredioClasificRural clasificaciones;
    private List<CatPredioObraInterna> obrasInternas;
    private List<CatPredioClasificRural> clasificaciones;
    private List<CatPredioCultivo> cultivos;
    private BaseLazyDataModel<GeDocumentos> documentos;
    private CatProvincia provincia;

    private String taskKey;
    private List<BloqueGeoData> bloqueGis;
    private List<BloqueGeoData> bloqueGisMod;
    private List<BloqueGeoData> bloqueGisFinal;
    private BloqueGeoData geoBloqueSeleccionado;
    private final String keyGeoBloque = "geoBloque";
    private String jsTarea = null;
    private List<GeoVias> geoVias;
    private TipoProceso proceso;

//    @PostConstruct
    public void load() {
        try {
            codProvincia = SisVars.PROVINCIA;
            if (!JsfUti.isAjaxRequest()) {
                cargarQueryPar();
                this.cargarDatos();
                if (esProcesoActualizacionAreasLind()) {
                    this.getSumaAreaPredioDesmenbraciones();
                    this.getCalcularExcedenteDiferencia();
                    this.getPorcentajeExcedenteDiferencia();
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, usr, e);
        }
    }

    public Boolean esProcesoActualizacionAreasLind() {
        if (proceso == null) {
            if (ss.getParametros() != null && ss.getParametros().get("proceso") != null) {
                proceso = (TipoProceso) ss.getParametros().get("proceso");
            }
            if (proceso == null) {
                return false;
            }
        }

        return proceso.equals(TipoProceso.ACTIALIZAR_AREAS_LINDEROS);
    }

    public void cargarQueryPar() {
        if (predioId != null) {
            predioLink = Long.valueOf(predioId);
        }

    }

    protected void cargarDatos() {
        try {
            if (sess != null && ss.getParametros() != null) {
                if (ss.getParametros().get("taskId") != null && !Boolean.valueOf(ss.getParametros().get("edit").toString())) {
                    esTareaAp = true;
                }
                if (ss.getParametros().get("taskId") != null && Boolean.valueOf(ss.getParametros().get("edit").toString())) {
                    esTarea = true;
                    sess.setTaskID(ss.getParametros().get("taskId").toString());
                }
                if (ss.tieneParametro("taskName")) {
                    taskKey = ss.getParametro("taskName").toString();
                } else {
                    taskKey = "Edicion Alfanumerico";
                }
                this.init();
                Long numeroPredio = null;
                Long id = null;
                if (claveCat != null) {
                    if (claveCat.length() < 24) {
                        predio = catas.getPredioByClaveCatAnt(claveCat);
                    } else {
                        predio = catas.getPredioByClaveCat(claveCat);
                    }
                } else {
                    if (predioLink == null) {
                        if (ss.tieneParametro("idPredio")) {
                            id = Long.parseLong(ss.getParametro("idPredio").toString());
                        }
                        if (ss.tieneParametro("numPredio")) {
                            numeroPredio = Long.parseLong(ss.getParametro("numPredio").toString());
                        }
                        if (ss.tieneParametro("edit")) {
                            editable = Boolean.parseBoolean(ss.getParametro("edit").toString());
                        }
                    } else {
                        editable = false;
                    }
                    if (id == null) {
                        JsfUti.redirectFaces("/vistaprocesos/catastro/predios.xhtml");
                    } else if (numeroPredio != null) {
                        predio = EntityBeanCopy.initializeAndUnproxy(catas.getPredioNumPredio(numeroPredio));
                    } else {
                        predio = catas.getPredioId(id);
                    }
                }
                if (predio != null) {
                    System.out.println("Usuario " + sess.getName_user() + " editando Predio con id " + predio.getId()
                            + " y clave " + predio.getClaveCat() + ", numero Predio " + predio.getNumPredio());
                    if (predio.getCiudadela() != null && predio.getTipoConjunto() == null) {
                        if (predio.getCiudadela().getCodTipoConjunto() != null) {
                            predio.setTipoConjunto(
                                    (CatTipoConjunto) EntityBeanCopy.clone(predio.getCiudadela().getCodTipoConjunto()));
                        }
                    }
                    if (predio.getCatPredioS4() != null) {
                        caracteristicas = predio.getCatPredioS4();
                    } else {
                        caracteristicas = new CatPredioS4();
                        caracteristicas.setPredio(predio);
                    }
                    predio.setCatPredioS4(caracteristicas);
                    if (predio.getCatPredioS6() != null) {
                        servicios = predio.getCatPredioS6();
                    } else {
                        servicios = new CatPredioS6();
                        servicios.setPredio(predio);
                    }
                    predio.setCatPredioS6(servicios);

                    this.setUsr(sess.getName_user());
                    escritura = (CatEscritura) EntityBeanCopy.clone(catas.getEscritura(Querys.getCatEscrituraByPredioUltima,
                            new String[]{"predio"}, new Object[]{predio}));
                    if (escritura == null) {
                        escritura = new CatEscritura();
                        escritura.setPredio(predio);
                        escritura.setEstado("A");
                        escritura.setSecuencia(new BigInteger("1"));
                    }
                    bloques = (List<CatPredioEdificacion>) predio.getCatPredioEdificacionCollection();
                    if (bloques == null) {
                        bloques = new ArrayList<>();
                    }
                    if (!this.getMainConfig().getFichaPredial().getRedenerFichaIb()) { // Carga los datos del ficha V1
                        if (predio.getResponsableActualizadorPredial() != null) {
                            actualizadorPredio = predio.getResponsableActualizadorPredial();
                        } else {
                            actualizadorPredio = new CatEnte();
                        }
                        vias = new ArrayList<>();
                        instalacionesEspeciales = new ArrayList<>();
                        listarViaseInstalacionesEspeciales();
                        catPredioFusionDivision = (CatPredioFusionDivision) em.find(Querys.getPredioRaizDivision,
                                new String[]{"predioResultante"}, new Object[]{predio.getId()});
                    } else { // Carga los datos de la ficha de Ibarra
                        propietarios = this.getCiudadano((List<CatPredioPropietario>) predio.getCatPredioPropietarioCollection());
                        clasificaciones = predio.getCatPredioClasificRuralCollection();
                        obrasInternas = (List<CatPredioObraInterna>) predio.getCatPredioObraInternaCollection();
                        this.cargarDoc();
                        this.getDatosBloques();
                        cargarPrestamos();
                        usosA = new ArrayList<>();
                        if (this.esPh()) {
//                            this.calcularAreaConsAlic();
//                            this.calcularAreaTerrAlic();
                        }
                    }
                    if (propietarios != null && propietarios.size() > 0) {
//                        for (CatPredioPropietario pp : propietarios) {
//                            if (pp.getEstado().equalsIgnoreCase("A")) {
//                                if (pp.getCopropietario() != null && pp.getCopropietario()) {
//                                    coopropietarios = true;
//                                    break;
//                                }
//                            }
//                        }
                    } else {
                        propietarios = new ArrayList<>();
                    }
                    this.getLinderosEscritura();
                    this.cargarFotos();
                    if (editable) {
                        try {
                            this.defaultCatalogos();
                            this.setPredioAnt(generarJson(predio));
                        } catch (HibernateException e) {
                            LOG.log(Level.WARNING, null, e);
                        }
                    }
                    listarUsos();
                    this.verificarRestriccion();
                    if (this.predio.getPropiedadHorizontal() == false || this.predio.getPropiedadHorizontal() == null) {
                        this.predio.setAreaDeclaradaConst(this.sumarAreaConst());
                    }
                    // Verificamos Si hay Bloque nuevo en la edicion de una tarea
                    if (ss.tieneParametro("idTramite")) {
                        this.jsTarea = getPredioAnt();
                        BigDecimal areaTemp = BigDecimal.ZERO;
                        if (ss.tieneParametro("areaGrafica")) {
                            areaTemp = new BigDecimal(ss.getParametro("areaGrafica").toString());
                            if (this.predio.getTipoPredio().equalsIgnoreCase("U")) {
                                this.predio.setAreaSolar(areaTemp.setScale(2, RoundingMode.HALF_UP));
                                this.caracteristicas.setAreaGraficaLote(areaTemp.setScale(2, RoundingMode.HALF_UP));
                            } else {
                                this.caracteristicas.setAreaGraficaLote(areaTemp.setScale(5, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(10000.00)));
                                this.predio.setAreaSolar(areaTemp.divide(BigDecimal.valueOf(10000.00)).setScale(5, RoundingMode.HALF_UP));
                            }
                        }
                        this.verificarBloqueDibujo();
                    }
                    // Cargamos las vias
                    if (this.predio.getEstado().equals(EstadosPredio.PENDIENTE)) {
                        if (this.predio.getPredioRaiz() != null) {
                            CatPredio predioRaiz = getPredioRaiz();
                            geoVias = dataBaseIb.getViasPredio(predioRaiz.getClaveCat());
                        }
                    } else {
                        if (this.ss.getParametros().containsKey("idPrediosTx")) {
                            geoVias = dataBaseIb.getViasPredio(Integer.valueOf(this.ss.getParametros().get("idPrediosTx").toString()));
                        } else {
                            if (this.predio.getPropiedadHorizontal()) {
                                geoVias = dataBaseIb.getViasPredio(this.getPredioRaiz().getClaveCat());
                            } else {
                                geoVias = dataBaseIb.getViasPredio(this.predio.getClaveCat());
                            }
                        }
                    }
                    // cargamos los documentos
                    documentos = new BaseLazyDataModel<>(GeDocumentos.class, new String[]{"raiz"}, new Object[]{BigInteger.valueOf(predio.getId())}, "fechaCreacion", "DESC");
                } else {
                    JsfUti.redirectFaces("/vistaprocesos/catastro/predios.xhtml");
                }
            } else {
                JsfUti.redirectFaces("/vistaprocesos/catastro/predios.xhtml");
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Calcula la diferencia o el excendete
     *
     * @return diferencia o el excendete
     */
    public BigDecimal getCalcularExcedenteDiferencia() {
        if (escritura == null) {
            return BigDecimal.ZERO;
        }
        if (escritura.getAreaSolar() == null && !esPosesion()) {
            JsfUti.messageError(null, "Advertencia", "El area de escritura debe ser ingresado.");
            return BigDecimal.ZERO;
        }
        if (escritura.getAreaSolar() == null) {
            escritura.setAreaSolar(BigDecimal.ZERO);
        }
        // Validamos que el area de la escritura sea ingresada.
        // Restamos el area de escritura menos la suma de (area plano + desmenbraciones)
        caracteristicas.setSuperfice(this.getSumaAreaPredioDesmenbraciones().subtract(escritura.getAreaSolar()));
        return caracteristicas.getSuperfice();
    }

    /**
     * Calcula el porcentaje de diferencia o excendete
     *
     * @return porcentaje de diferencia o excendete
     */
    public BigDecimal getPorcentajeExcedenteDiferencia() {
        if (!esPosesion()) {
            if (escritura.getAreaSolar() == null) {
                JsfUti.messageError(null, "Advertencia", "El area de escritura debe ser ingresado.");
                escritura.setAreaSolar(BigDecimal.ZERO);
                return BigDecimal.ZERO;
            }
            // Validamos que el area de la escritura sea ingresada.
            if (escritura.getAreaSolar().compareTo(BigDecimal.ZERO) <= 0) {
                JsfUti.messageWarning(null, "Advertencia", "El area de escritura debe ser mayor a cero.");
                return BigDecimal.ZERO;
            }
        }
        if (caracteristicas.getSuperfice() == null) {
            caracteristicas.setSuperfice(BigDecimal.ZERO);
        }
        // Restamos el area de escritura menos la suma de (area plano + desmenbraciones)
        if (escritura.getAreaSolar().doubleValue() > 0 && caracteristicas.getSuperfice().doubleValue() > 0) {
            caracteristicas.setFrenteTotal((caracteristicas.getSuperfice().divide(escritura.getAreaSolar(), 5, RoundingMode.HALF_UP)).multiply(BigDecimal.valueOf(100.00)));
        }
        if (caracteristicas.getFrenteTotal() == null) {
            return BigDecimal.ZERO;
        }
        return caracteristicas.getFrenteTotal();
    }

    /**
     * Realiza la suma del area de plano mas el area de desmenbaciones
     *
     * @return area plano + desmenbraciones
     */
    public BigDecimal getSumaAreaPredioDesmenbraciones() {
        if (escritura.getAreaConstruccion() == null) {
            escritura.setAreaConstruccion(BigDecimal.ZERO);
        }
        // Sumamos las area plano + desmenbraciones
        return predio.getAreaSolar().add(escritura.getAreaConstruccion());
    }

    public void verificarRestriccion() {
        List<RestricionPredio> rest = dataBaseIb.findRestriccionPredio(predio.getClaveCat());
        if (rest != null) {
            observacionRestricciones = new ArrayList<>();
            rest.forEach((rt) -> {
                observacionRestricciones.add("Tipo de Restricción " + rt.getRestricciones().getDescripcionRestriccion()
                        + ", " + Utils.isEmpty(rt.getComentario()));
            });
        } else {
            observacionRestriccion = null;
        }
    }

    @Override
    public void verificarBloqueDibujo() {
        if (this.predio.getPropiedadHorizontal()) {
            bloqueGis = dataBaseIb.getBloquesGisPH(this.claveCatastralCompleta());
            bloqueGisMod = dataBaseIb.getBloquesGisEditadosPH(this.claveCatastralCompleta());
            System.out.println(this.claveCatastralCompleta() + " Es Ph " + this.predio.getPropiedadHorizontal());
        } else {
            Integer gid = (Integer) this.ss.getParametros().get("idPrediosTx");
            if (gid != null) {
                bloqueGis = dataBaseIb.getBloquesGis(gid);
                bloqueGisMod = dataBaseIb.getBloquesGisEditados(this.claveCatastralCompleta());
            } else {
                bloqueGis = dataBaseIb.getBloquesGis(this.claveCatastralCompleta());
                bloqueGisMod = dataBaseIb.getBloquesGisEditados(this.claveCatastralCompleta());
            }
        }
//        System.out.println(bloqueGis);
        if (Utils.isNotEmpty(bloqueGis) && Utils.isNotEmpty(bloques)) {
            bloques.forEach((bloque) -> {
                int index = bloqueGis.indexOf(new BloqueGeoData(bloque.getNoEdificacion()));
                // SI EXISTE AGREGAMOS A LISTA DE LOS BLOQUES MODIFICADOS;
                if (index > -1) {
                    BloqueGeoData get = bloqueGis.get(index);
                    if (!bloqueGisMod.contains(get)) {
                        bloqueGisMod.add(get);
                        bloqueGis.remove(index);
                    }
                }
            });
        }
//        System.out.println(bloqueGisMod);
        if (Utils.isNotEmpty(bloqueGisMod)) {
            List<BloqueGeoData> temp = new ArrayList<>();
            bloqueGisMod.forEach((bgd) -> {
                CatPredioEdificacion bloqueTemp = null;
                for (CatPredioEdificacion bloque : bloques) {
                    if (bgd.getNum() == bloque.getNoEdificacion()) {
                        bloqueTemp = bloque;
                        bloqueTemp.setEdicionGrafica(true);
                        break;
                    }
                }
                if (bloqueTemp == null) { // Si no ecxxiste lo agregamos como nuevo
                    if (!bloqueGis.contains(bgd)) {
                        bloqueGis.add(bgd);
                    }
                    temp.add(bgd);
                }
            });
            if (Utils.isNotEmpty(temp)) {
                bloqueGisMod.removeAll(temp);
            }
        }
        JsfUti.messageInfo(null, "Advertencia", "Se ha detectado "
                + (Utils.isEmpty(bloqueGis) ? 0 : bloqueGis.size()) + " bloque(s) nuevo(s) y "
                + (Utils.isEmpty(bloqueGisMod) ? 0 : bloqueGisMod.size()) + " editados(s)");
    }

    public String claveCatastralCompleta() {
        if (predio == null) {
            return null;
        }
        String clave = predio.getClaveCat();
//        System.out.println("Clave : " + clave);
        String prov = clave.substring(0, 2);
        if (prov.equals("99")) {
            prov = String.format("%02d", SisVars.PROVINCIA);
        }
        String a = clave.substring(2, clave.length());
        a = prov + a;
        return a;
    }

    public void getAvalHistorico() {
        CatPredioAvalHistorico valorSuelo = null;
        avaluosHistoricosPredio = em.findAll(Querys.getAvaluosHistoricosPorPredios, new String[]{"predio"}, new Object[]{predio.getId()});
        if (!avaluosHistoricosPredio.isEmpty()) {
            if (avaluosHistoricosPredio.size() >= 1) {
                valorSuelo = avaluosHistoricosPredio.get(avaluosHistoricosPredio.size() - 1);
                if (valorSuelo != null) {
                    if (valorSuelo.getValorBaseM2() != null) {
                        valorMetro2 = valorSuelo.getValorBaseM2();
                    }
                }
            }

        }

    }

    /**
     * Obtiene los linderos del predio
     */
    public void getLinderosEscritura() {
        try {
            if (escritura != null) {
                predio.getPredioCollection().size();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public void imprimirLiquidaciónGuardado(PeInspeccionFinal inspeccion) {
    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            System.out.println("Subiendo foto: " + event.getFile().getFileName());
            Long fileId = fserv.uploadFile(event.getFile().getInputstream(), event.getFile().getFileName(), event.getFile().getContentType());
//            Long fileId = fserv.uploadFile(FilesUtil.copyFileServer1(event), event.getFile().getFileName(), event.getFile().getContentType());
            System.out.println("Foto: " + event.getFile().getFileName() + " oid " + fileId);
            fotoBean.setNombre(event.getFile().getFileName());
            fotoBean.setPredioId(predio.getNumPredio().longValue());
            fotoBean.setIdPredio(predio.getId());
            fotoBean.setContentType(event.getFile().getContentType());
            fotoBean.setFileId(fileId);
            fotoBean.saveFoto();
            System.out.println("Foto Guardada...");
            cargarFotos();
            Faces.messageInfo(null, "Nota1", "Foto guardada satisfactoriamente");
        } catch (IOException e) {
            Logger.getLogger(FichaPredial.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void handleFileDocumentBySave(FileUploadEvent event) {
        try {
            System.out.println("Subiendo foto: " + event.getFile().getFileName());
            Long documentoId = fserv.uploadFile(FilesUtil.copyFileServer1(event), event.getFile().getFileName(), event.getFile().getContentType());
            System.out.println("Foto " + event.getFile().getFileName() + " oid " + documentoId);
            documentoBean.setFechaCreacion(new Date());
            documentoBean.setNombre(event.getFile().getFileName());
            documentoBean.setRaiz(predio.getId());
            documentoBean.setContentType(event.getFile().getContentType());
            documentoBean.setDocumentoId(documentoId);
            documentoBean.setIdentificacion("Datos Prediales");
            this.setDocumento(documentoBean.saveDocumento());
            System.out.println("Foto Guardada...");
            Faces.messageInfo(null, "Nota1", "Archivo cargado Satisfactoriamente");
        } catch (IOException e) {
            Logger.getLogger(FichaPredial.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    protected void listarUsos() {
        try {
            if (predio.getCatPredioS6() != null) {
                if (predio.getCatPredioS6().getUsosList() != null) {
                    if (Utils.isNotEmpty(predio.getCatPredioS6().getUsosList())) {
                        predio.getCatPredioS6().getUsosList().forEach((ci) -> {
                            usosA.add(ci);
                        });
                    }
                    this.predio.getCatPredioS6().setUsosList(usosA);
                } else {
                    usosA = new ArrayList<>();
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }

    }

    protected void defaultCatalogos() {
        if (this.predio != null) {
            if (this.predio.getClasificacionSuelo() == null) {
                this.predio.setClasificacionSuelo(this.catas.getDefaultItem("predio.clasificacion_suelo"));
            }
            if (this.predio.getConstructividad() == null) {
                this.predio.setConstructividad(this.catas.getDefaultItem("predio.constructividad_suelo"));
            }
            if (this.predio.getTipovia() == null) {
                this.predio.setTipovia(this.catas.getDefaultItem("predio.vias"));
            }

            if (this.caracteristicas != null) {
                if (this.caracteristicas.getCoberturaPredominante() == null) {
                    this.caracteristicas
                            .setCoberturaPredominante(this.catas.getDefaultItem("predio.cobertura_predominante"));
                }
                if (this.caracteristicas.getEcosistemaRelevante() == null) {
                    this.caracteristicas
                            .setEcosistemaRelevante(this.catas.getDefaultItem("predio.ecosistema_relevante"));
                }
            }
            if (this.servicios != null) {
                if (this.servicios.getAbastAgua() == null) {
                    this.servicios.setAbastAgua(this.catas.getDefaultItem("predio.abastecimiento_agua"));
                }
                if (this.servicios.getAbastAguaRecibe() == null) {
                    this.servicios.setAbastAguaRecibe(this.catas.getDefaultItem("predio.agua_recibe"));
                }
                if (this.servicios.getAbasteElectrico() == null) {
                    this.servicios.setAbasteElectrico(this.catas.getDefaultItem("energia.electrica.proviene"));
                }
                if (this.servicios.getEvacAguasServ() == null) {
                    this.servicios.setEvacAguasServ(this.catas.getDefaultItem("predio.evac_aguas_serv"));
                }
                if (this.servicios.getRecolBasura() == null) {
                    this.servicios.setRecolBasura(this.catas.getDefaultItem("predio.recol_basura_medio"));
                }
            }
        } else {
            System.out.println("No se puede cargar catalogos por default...");
        }
    }

    public void cargarPrestamos() {
        if (this.predio != null) {
//            prestamoPredio = new FinanPrestamoPredio();
//            prestamoPredio = (FinanPrestamoPredio) em.find(Querys.getFinanciamientoPredio, new String[]{"predio"},
//                    new Object[]{predio.getId()});
            cultivos = predio.getCatPredioCultivoCollection();
        }
    }

    protected void listarViaseInstalacionesEspeciales() {
        Collection<CtlgItem> xvias;
        Collection<CtlgItem> xinstalaciones;
        if (predio.getCatPredioS6() != null) {
            xvias = predio.getCatPredioS6().getCtlgItemCollection();
            xinstalaciones = predio.getCatPredioS6().getCtlgItemCollectionInstalacionEspecial();
            if (xvias != null && !xvias.isEmpty()) {

                for (Iterator<CtlgItem> iterator = xvias.iterator(); iterator.hasNext();) {
                    CtlgItem v = iterator.next();
                    if (!vias.contains(v)) {
                        vias.add(v);
                    }
                }

//                for (CtlgItem v : viasClone) {
//                    if (!vias.contains(v)) {
//                        vias.add(v);
//                    }
//
//                }
            } else {
                servicios.setCtlgItemCollection(vias);
            }
            if (xinstalaciones != null && !xinstalaciones.isEmpty()) {
                for (CtlgItem v : xinstalaciones) {
                    instalacionesEspeciales.add(v);
                }
            } else {
                servicios.setCtlgItemCollectionInstalacionEspecial(instalacionesEspeciales);
            }
        }
    }

    public void cargarFotos() {
        if (predio == null) {
            return;
        }
        fotos = em.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getId()});
        fotos.size();

    }

    public void onCellEdit(CellEditEvent event) {
        Object newValue = event.getNewValue();
        if (newValue == null) {
            Faces.messageWarning(null, "Advertencia!", "El dato ingresado es incorrecto");
        }
    }

    public List<CtlgItem> getListado(String argumento) {
        HiberUtil.newTransaction();
        List<CtlgItem> ctlgItem = (List<CtlgItem>) em.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{argumento});
        return ctlgItem;
    }

    public void propietario(CatPredioPropietario propietario) {
        if (!seccion1()) {
            return;
        }
        if (propietario == null) {
            if (this.verificarPorcentajePosession()) {
                JsfUti.messageError(null, "Info.", "No se puede agregar propietario ya esta completo el 100 % de acciones.");
                return;
            }
        }
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (propietario != null) {
            if (propietario.getId() != null) {
                p.add(propietario.getId().toString());
            } else {
                JsfUti.messageError(null, "Error.", "Propietario no puede ser editado.");
                return;
            }
        }
        params.put("idCatPredioPro", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        params.put("nuevo", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        p = new ArrayList<>();
        p.add(editable.toString());
        params.put("editar", p);
        p = new ArrayList<>();
        p.add("false");
        params.put("persistir", p);
        this.ss.agregarParametro("propietarios", this.propietarios);
        Utils.openDialog("/resources/dialog/propietarios_1", params, "350", "80");
    }

    public void procesarPropietario(SelectEvent event) {
        CatPredioPropietario propietario = (CatPredioPropietario) event.getObject();
        if (propietario != null) {
            BigDecimal porcentaje = BigDecimal.valueOf(100.00);
            if (propietario.getId() != null) {
                if (!propietarios.contains(propietario)) {
                    CiuCiudadano ciu = (CiuCiudadano) em.getNativeQueryPropietarios(Querys.getCiudadano(propietario.getCiuCedRuc()));
                    propietario.setCiudadano(ciu);
                    propietarios.add(propietario);
                } else {
                    propietarios.set(propietarios.indexOf(propietario), propietario);
                }
            } else {
                propietarios.add(propietario);
            }
            for (CatPredioPropietario p : propietarios) {
                if (p.getTipo().getOrden() != 1) {
                    porcentaje = porcentaje.subtract(p.getPorcentajePosecion());
                }
            }
            for (CatPredioPropietario pp : propietarios) {
                if (pp.getTipo().getOrden() == 1) {
                    pp.setPorcentajePosecion(porcentaje);
                }
                if (pp.getCopropietario() != null) {
                    if (pp.getEstado().equalsIgnoreCase("A") && pp.getCopropietario()) {
                        coopropietarios = true;
//                        break;
                    }
                }
            }

            JsfUti.update("tdatos:frmEscrituras:dtPropietarios");
        }
    }

    public void eliminarPropietario(CatPredioPropietario propietario) {
        if (propietario.getId() == null) {
            BigDecimal porcentaje = BigDecimal.valueOf(100.00);
            for (CatPredioPropietario p : propietarios) {
                if (p.getTipo().getOrden() != 1) {
                    porcentaje = porcentaje.subtract(p.getPorcentajePosecion());
                }
            }
            int index = -1;
            int indexPP = -1;
            for (CatPredioPropietario pp : propietarios) {
                if (pp.getTipo().getOrden() == 1) {
                    pp.setPorcentajePosecion(porcentaje);
                }
                index++;
                if (propietario.getCiuCedRuc().equalsIgnoreCase(pp.getCiuCedRuc())) {
                    indexPP = index;
                }
            }
            if (indexPP > -1) {
                propietarios.remove(indexPP);
            }
            this.predio.setCatPredioPropietarioCollection(propietarios);
        } else {
            if (propietario.getTipo().getOrden() == 1) {
                JsfUti.messageError(null, "Propietario", "Titular no puede ser eliminado.");
                return;
            }
            propietario.setEstado("I");
            propietario.setModificado(sess.getName_user());
            for (CatPredioPropietario prop : this.propietarios) {
                if (prop.getTipo().getOrden() == 1) {
                    prop.getPorcentajePosecion().add(propietario.getPorcentajePosecion());
                }
            }
            catas.guardarPropietarioCiudadano(propietario, sess.getName_user());
            propietarios.remove(propietario);
            this.predio.setCatPredioPropietarioCollection(propietarios);
            this.em.persist(predio);
        }
        JsfUti.messageInfo(null, "Propietario", "Propietario eliminado.");
        JsfUti.update("tdatos:frmEscrituras:dtPropietarios");
    }

    private void getDatosBloques() {
        if (predio.getVivCencalEstadoAcabadoPiso() == null || predio.getVivCencalAcabadoPiso() == null) {
            if (Utils.isNotEmpty(predio.getCatPredioEdificacionCollection())) {
                CatPredioEdificacion get = Utils.get(predio.getCatPredioEdificacionCollection(), 0);
                if (predio.getVivCencalEstadoAcabadoPiso() == null) {
                    predio.setVivCencalEstadoAcabadoPiso(get.getEstadoConservacion());
                }
                if (predio.getVivCencalAcabadoPiso() == null) {
                    for (CatPredioEdificacionProp b : get.getCatPredioEdificacionPropCollection()) {
                        if (b.getProp().getCategoria().getGuiOrden() == 9) {
                            predio.setVivCencalAcabadoPiso(
                                    catas.getItemByCatalagoOrder("predio.bloque.revestpiso", b.getProp().getOrden()));
                            break;
                        }
                    }
                }
            }
        }
    }

    public void limpiarJefeHogar() {
        this.predio.setCiuTipoIdentificacion(null);
        this.predio.setCiuHorizontal(null);
        this.predio.setCiuNombresHorizontal(null);
        JsfUti.update("frmVivienda:pngJefeHogar");
    }

    /**
     * Carga los documentos adjuntados al predio
     */
    private void cargarDoc() {
        if (predio != null) {
            documentos = new BaseLazyDataModel<GeDocumentos>(GeDocumentos.class, new String[]{"raiz"}, new Object[]{predio.getId()}, "fechaCreacion", "DESC");
        }
    }

    /**
     * Carga los datos de la tabla Ciudadanos
     *
     * @param propietariosTemp
     * @return
     */
    public List<CatPredioPropietario> getCiudadano(List<CatPredioPropietario> propietariosTemp) {
        List<CatPredioPropietario> props = new ArrayList<>();
        for (CatPredioPropietario cpp : propietariosTemp) {
            cpp.setCiudadano((CiuCiudadano) this.dataBaseIb.getCiudadano(cpp.getCiuCedRuc()));
            props.add(cpp);
        }
        return props;

    }

    /**
     * Metodo para indicar si es un predio rural.
     *
     * @return
     */
    public Boolean esPredioRural() {
        return predio.getTipoPredio().equalsIgnoreCase("R") || predio.getTipoPredio().startsWith("R")
                || predio.getTipoPredio().contains("R");
    }

    public void clasificacionSueloRural(CatPredioClasificRural clasificSueloRural) {
        if (!seccion1()) {
            return;
        }
        if (!seccion2()) {
            return;
        }

        if (Utils.isNotEmpty(clasificaciones)) {
            BigDecimal areatem = BigDecimal.ZERO;
            for (CatPredioClasificRural c : clasificaciones) {
                areatem = areatem.add(c.getSuperficie());
            }
            if (clasificSueloRural == null) {
                if (areatem.compareTo(predio.getAreaSolar()) == 0) {
                    Faces.messageInfo(null, "Nota!", "La suma de Areas de las clases de suelo, es igual al Total del Area del terreno.");
                    // TODO: descomentar cuando se pase a produccion
                    return;
                }
            }
        }

        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        p.add(Boolean.toString((clasificSueloRural == null)));
        params.put("nuevo", p);
        p = new ArrayList<>();
        if (clasificSueloRural != null) {
            if (clasificSueloRural.getId() != null) {
                p.add(clasificSueloRural.getId().toString());
                params.put("idCatClasiSueloRural", p);
            }
        }
        p = new ArrayList<>();
        p.add(ver.toString());
        params.put("ver", p);
        ver = false;
        Utils.openDialog("/resources/dialog/clasificacionSueloRural", params, "200");
    }

    public void cultivo(CatPredioCultivo cultivo) {
        if (!seccion1()) {
            return;
        }
        if (!seccion2()) {
            return;
        }
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (cultivo != null && cultivo.getId() != null) {
            p.add(cultivo.getId().toString());
        }
        params.put("idCatPredioCult", p);
        p = new ArrayList<>();
        p.add(Boolean.toString((cultivo == null)));

        params.put("nuevo", p);
        p = new ArrayList<>();
        p.add(ver.toString());
        params.put("ver", p);
        ver = false;
        Utils.openDialog("/resources/dialog/cultivos", params, "400");
    }

    public void procesarClasificSueloRural(SelectEvent event) {
        CatPredioClasificRural clasificacion = (CatPredioClasificRural) event.getObject();
        if (clasificaciones == null) {
            clasificaciones = new ArrayList<>();
        }
        if (clasificacion != null) {
            if (!clasificaciones.contains(clasificacion)) {
                clasificaciones.add(clasificacion);
            } else {
                clasificaciones.set(clasificaciones.indexOf(clasificacion), clasificacion);
            }
            predio.setCatPredioClasificRuralCollection(clasificaciones);
            predio.getCatPredioS6().setUsosList(usosA);
            if (this.saveHistoric(predio, "ACTUALIZACION INFORMACION DE CARACTERISTICAS", null, null, null, null)) {
                Faces.messageInfo(null, "Nota!", "Clasificación actualizada satisfactoriamente.");
            }
        }
        HiberUtil.refreshObject(predio);
        JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:avalSolar");
        JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:avalMunicipal");
        JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:avalBaseImp");
    }

    public void procesarCultivo(SelectEvent event) {
        CatPredioCultivo cultivo = (CatPredioCultivo) event.getObject();
        if (cultivos == null) {
            cultivos = new ArrayList<>();
        }
        if (cultivo != null) {
            if (!cultivos.contains(cultivo)) {
                cultivos.add(cultivo);
            } else {
                cultivos.set(cultivos.indexOf(cultivo), cultivo);
            }

            predio.setCatPredioCultivoCollection(cultivos);
            predio.getCatPredioS6().setUsosList(usosA);
            predio.setAreaCultivos(this.sumarCultivos());
            predio = catas.savePredio(predio);
            if (this.saveHistoric(predio, "ACTUALIZACION INFORMACION DE CARACTERISTICAS", null, null, null, null)) {
                Faces.messageInfo(null, "Nota!", "Cultivo Agregado correctamente.");
            }
        }
    }

    public void eliminarCultivo(CatPredioCultivo cultivo) {
        cultivo.setEstado("I");
        cultivo.setModificado(sess.getName_user());
        catas.guardarCultivo(cultivo, sess.getName_user());
        cultivos.remove(cultivo);
        predio.setCatPredioCultivoCollection(cultivos);
        predio.setAreaCultivos(this.sumarCultivos());
        predio = catas.savePredio(predio);
        if (this.saveHistoric(predio, "ACTUALIZACION INFORMACION DE CARACTERISTICAS", null, null, null, null)) {
            JsfUti.messageInfo(null, "Cultivo", "Cultivo eliminado.");
            JsfUti.update("tdatos:frmCaracSolar:dtCultivos");
        }

    }

    private BigDecimal sumarCultivos() {
        try {
            if (Utils.isEmpty(cultivos)) {
                return BigDecimal.ZERO;
            }
            BigDecimal area = BigDecimal.ZERO;
            for (CatPredioCultivo c : cultivos) {
                if (c.getArea() == null) {
                    c.setArea(BigDecimal.ZERO);
                }
                area = area.add(c.getArea());
            }
            return area;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return BigDecimal.ZERO;
    }

    public void eliminarClasificRural(CatPredioClasificRural clasificacion) {
        clasificacion.setEstado("I");
        clasificacion.setModificado(sess.getName_user());
        catas.guardarClasificacionSueloRural(clasificacion, sess.getName_user());
        clasificaciones.remove(clasificacion);
        predio.setCatPredioClasificRuralCollection(clasificaciones);
        if (this.saveHistoric(predio, "ACTUALIZACION INFORMACION DE CARACTERISTICAS", null, null, null, null)) {
            JsfUti.messageInfo(null, "Clasificación", "Clasificación eliminada.");
        }
    }

    /**
     * Abrimos el dialogo para agreger o editar un bloque.
     *
     * <p>
     * Si es nuevo verificamos que se seleccione un bloque gis, caso contrario
     * no le dejamos agregar el bloque
     * </p>
     *
     * @param bloque
     */
    @Override
    public void bloque(CatPredioEdificacion bloque) {
        if (!this.estaEdificado()) {
            if (caracteristicas.getEstadoSolar() == null) {
                if (!seccion3()) {
                    return;
                }
            }
            JsfUti.messageWarning(null, "Advertencia",
                    "El predio tiene Ocupacion: ." + caracteristicas.getEstadoSolar().getValor());
            return;
        }
        if (!seccion1()) {
            return;
        }
        if (!seccion2()) {
            return;
        }
        if (!seccion3()) {
            return;
        }

        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (bloque != null && bloque.getId() != null) {
            p.add(bloque.getId().toString());
            params.put("idCatPredioBloq", p);
            // Si el predio fue editado en el grafico buscamos el registro del bloque
            if (bloque.getEdicionGrafica()) {
                for (BloqueGeoData bdg : this.bloqueGisMod) {
                    if (Objects.equals(bdg.getNum(), bloque.getNoEdificacion())) {
                        ss.agregarParametro(keyGeoBloque, bdg);
                        bdg.setHabilitado(false);
                    }
                }
            }
        } else {
            // verificamos que se haya seleccionado un bloque gis para poder dejar ingresar
            // el predio
//            if (this.geoBloqueSeleccionado == null) {
//                JsfUti.messageError(null, "Error", "Debe Seleccionar el bloque a ingresar en la tabla 'Bloque Nuevos'");
//                return;
//            }
//            ss.agregarParametro(keyGeoBloque, this.geoBloqueSeleccionado);
        }

        p = new ArrayList<>();
        p.add(Boolean.toString((bloque == null)));
        params.put("nuevo", p);
        p = new ArrayList<>();
        p.add(ver.toString());
        params.put("ver", p);
        ver = false;

        Utils.openDialog("/resources/dialog/edificacionesPredio", params, "480");
    }

    /**
     * Procesamos la respuesta del dialogo de bloque.
     *
     * @param event Respuesta del dialogo de bloque.
     */
    @Override
    public void procesarBloque(SelectEvent event) {
        CatPredioEdificacion bloque = (CatPredioEdificacion) event.getObject();
        if (bloque != null) {
            if (!bloques.contains(bloque)) {
                bloques.add(bloque);
            } else {
                bloques.set(bloques.indexOf(bloque), bloque);
            }
            predio.setAreaDeclaradaConst(this.sumarAreaConst());
            if (this.esPh()) {
                this.calcularAreaConsAlic();
            }
            predio.setCatPredioEdificacionCollection(bloques);
            predio = (CatPredio) this.em.persist(predio);

            try {
                HiberUtil.flushAndCommit();
            } catch (Exception ex) {
                Logger.getLogger(FichaPredial.class.getName()).log(Level.SEVERE, null, ex);
            }
            // removemos el parametro para dejar libre el ingreso de bloque
            if (ss.tieneParametro(keyGeoBloque)) {
                ss.getParametros().remove(keyGeoBloque);
            }
            // Verificamos que haya seleccionado un bloque gis
            if (geoBloqueSeleccionado != null) {
                if (bloqueGisFinal == null) {
                    bloqueGisFinal = new ArrayList<>();
                }
                bloqueGisFinal.add(geoBloqueSeleccionado);
                bloqueGis.remove(geoBloqueSeleccionado);
                geoBloqueSeleccionado = null;
            }
            if (this.saveHistoric(predio, "ACTUALIZACION CARACTERIZACION Y ELEMENTOS CONSTRUCTIVOS DE LA EDIFICACION",
                    null, null, null, null)) {
                Faces.messageInfo(null, "Nota!", "Bloques actualizados satisfactoriamente");
                if (this.esPh()) {
                    JsfUti.update("tdatos:frmCaracSolar:panelGridPhsHijas");
                }
                JsfUti.update("tdatos:frmDatosPredio:tvUbicacion");
                JsfUti.update("tdatos:frmEdificaciones:tvEdificaciones:dtBloques");
                JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:fdsAvaluos");
                JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:pgAvaluos");
            }
        }
    }

    public void referenciarTipo(CtlgItem prot) {
        if (prot != null) {
            prototipos = em.findAllEntCopy(Querys.getCtlgItemByParent, new String[]{"padre"}, new Object[]{prot.getId()});
        } else {
            Faces.messageWarning(null, "Advertencia!", "Elija el tipo de prototipo arquitectonico");
        }
    }

    public void selecctionarEdif(CatPredioEdificacion edf) {
        this.setEdif(edf);
    }

    public void getDatosPrototipos() {
        for (CatPredioEdificacion e : this.getBloques()) {
            if (e.getId().equals(this.getEdif().getId())) {
                e = this.getEdif();
            }
        }
    }

    public String ubicacionPredio() {
        try {
            String claveCat = null;
            if (predio.getPropiedadHorizontal()) {
                if (predio.getPredioRaiz() != null) {
                    CatPredio predioRaiz = em.find(CatPredio.class, predio.getPredioRaiz().longValue());
                    if (predioRaiz != null) {
                        return geodataService.getUrlPredioImagePH(predioRaiz.getClaveCat(), predio.getClaveCat());
                    }
                }
            } else {
                if (predio.getClaveReordenada() != null) {
                    claveCat = predio.getClaveReordenada().getClaveCat();
                } else {
                    claveCat = predio.getClaveCat();
                }
            }
            try {
                return geodataService.getUrlPredioImage(claveCat, null, null);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
                return "/css/homeIconsImages/reselladoPlanos.png";
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return "/css/homeIconsImages/reselladoPlanos.png";
    }

    public String getCroquis() {
        String urlPredio = "";
        if (predio != null) {
            switch (SchemasConfig.DB_ENGINE) {
                case ORACLE:
                    urlPredio = SisVars.URLPLANOIMAGENPREDIO + this.claveCroquis(predio);
                    //urlPredio = "http://200.112.216.17/geoapi-libertad/rest/predio/croquis/" + this.claveCroquis(predio);
                    break;
                case POSTGRESQL:
                    if (predio.getPredioRaiz() != null) {
                        CatPredio predioRaiz = (CatPredio) em.find(Querys.getPrediosById, new String[]{"predioID"}, new Object[]{predio.getPredioRaiz()});
                        urlPredio = SisVars.URLPLANOIMAGENPREDIO + predioRaiz.getNumPredio();
                    } else {
                        urlPredio = SisVars.URLPLANOIMAGENPREDIO + predio.getNumPredio();
                    }
                    break;
                default:
                    break;
            }
        }
        return urlPredio;
    }

    public String colindantesPredio() {
        try {
            String claveCat = null;
            if (!predio.getPropiedadHorizontal()) {
                claveCat = predio.getClaveCat();
                if (predio.getClaveReordenada() != null) {
                    claveCat = predio.getClaveReordenada().getClaveCat();
                } else {
                    claveCat = predio.getClaveCat();
                }
            } else {
                if (predio.getPredioRaiz() != null) {
                    CatPredio predioRaiz = em.find(CatPredio.class, predio.getPredioRaiz().longValue());
                    if (predioRaiz.getClaveReordenada() != null) {
                        claveCat = predioRaiz.getClaveReordenada().getClaveCat();
                    } else {
                        claveCat = predioRaiz.getClaveCat();
                    }
                }
            }
            if (claveCat == null) {
                return "/css/homeIconsImages/reselladoPlanos.png";
            }
            return geodataService.getUrlColindantesImage(claveCat, null, null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return "/css/homeIconsImages/reselladoPlanos.png";
    }

    public void saveEscritura() {
        try {
            Boolean controlEstado = false;
            if (this.predio.getId() != null && this.canton != null && /*this.escr.getFecEscritura() != null
                    &&*/ this.escr.getFecInscripcion() != null && this.escr.getNumRepertorio() != null
                    && this.escr.getNotaria() != null && this.escr.getAreaSolar() != null) {

                Map<String, Object> paramt = new HashMap<>();
                paramt.put("predio", predio);
                escriturasConsulta = em.findAll(Querys.getEscriturasByPredio, new String[]{"predio"}, new Object[]{predio});

                if ((escriturasConsulta.isEmpty() || escriturasConsulta == null) && escr.getEstado().equals("I")) {
                    JsfUti.messageInfo(null, "La primera escritura para el predio no debe estar Inactiva", "");
                    return;
                }
                if (escr.getEstado().equals("A") && !escriturasConsulta.isEmpty() && escriturasConsulta != null) {
//                    for (CatEscritura escri : escriturasConsulta) {
//                        escri.setEstado("I");
//                        if(escritura.getIdEscritura() != escri.getIdEscritura())
//                            em.persist(escri);
//                    }
                } else if (escr.getEstado().equals("I") && !escriturasConsulta.isEmpty() && escriturasConsulta != null) {
                    for (CatEscritura escri : escriturasConsulta) {
                        if (escri.getEstado().equals("A")) {
                            controlEstado = true;
                        }
                    }
                    if (controlEstado == true) {
                        JsfUti.messageInfo(null, "Las escrituras no pueden tener Estado Inactivo", "");
                        return;
                    }
                }
                escr.setPredio(predio);
                escr.setCanton(canton);
                escr.setFecCre(new Date());

                if (controlAddUp == true) {
                    em.saveAll(this.escr);
                }
                if (controlAddUp == false) {
                    em.persist(escr);
                }
                this.canton = new CatCanton();
                this.escr = new CatEscritura();
                escriturasConsulta = new ArrayList<>();
                escriturasConsulta = em.findAll(Querys.getEscriturasByPredio, new String[]{"predio"}, new Object[]{predio});
                this.predio.setCatEscrituraCollection(escriturasConsulta);
                if (saveHistoric(predio, "ACTUALIZACION DE ESCRITURAS", getFichaEdifAnt(), getFichaEdifAct(), getFichaModelAnt(), getFichaModelAct())) {
                    JsfUti.messageInfo(null, "Exito", "Datos grabados Satisfactoriamente");
                } else {
                    JsfUti.messageInfo(null, "Exito", "Datos grabados Satisfactoriamente");
                }
            } else {
                JsfUti.messageInfo(null, "Debe Registrar todos los campos Obligatorios", "");
            }
        } catch (Exception e) {
            JsfUti.messageInfo(null, "Error al Guardar", "");
        }
    }

    public void saveEscrituraControl() {
        cantones = em.findAllEntCopy(CatCanton.class);
        controlAddUp = true;
    }

    public void updateEscrituraControl(CatEscritura e) {
        controlAddUp = false;
        cantones = em.findAllEntCopy(CatCanton.class);
        this.canton = new CatCanton();
        this.escr = new CatEscritura();
        this.escr = e;
        this.canton = e.getCanton();
    }

    public void cambioTipoPersona() {
        if (tipoEnte == 1) {
            esPersona = true;
        }
        if (tipoEnte == 2) {
            esPersona = false;
        }
        responsablesLazy = new CatEnteLazy(esPersona);
        JsfUti.update("frmResponsableDialog:dtresponsable");
    }

    public void loadResponsablesPredio(ActionEvent event) {
        try {
            if (event.getComponent().getId().equals("btnActualizadorResponsable")) {
                this.controlResponsable = "actualizador";
            }
            if (event.getComponent().getId().equals("btnFiscalizadorResponsable")) {
                this.controlResponsable = "fiscalizador";
            }
            if (event.getComponent().getId().equals("btnEnteHorizontal")) {
                this.controlResponsable = "enteHorizontal";
            }
            if (event.getComponent().getId().equals("btnInformante")) {
                this.controlResponsable = "informante";
            }
            // Utils.openDialog("/META-INF/resources/dlgs/Ciudadanos", null);
            Utils.openDialog("/resources/dialog/Ciudadanos", null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, controlResponsable, e);
        }
    }

    public void selectedResponsable(CatEnte responsable) {

        if (controlResponsable.equals("fiscalizador")) {
            this.setFiscalizadorPredio(responsable);
            JsfUti.update(":tdatos:frmResponsables:actPredial");
        }
        if (controlResponsable.equals("actualizador")) {
            this.setActualizadorPredio(responsable);
            JsfUti.update(":tdatos:frmResponsables:actFisc");
        }
        if (controlResponsable.equals("enteHorizontal")) {
            this.setEnteHorizontal(responsable);
            JsfUti.update(":tdatos:frmViviendaCensal");
        }
        if (controlResponsable.equals("informante")) {
            setInformante(responsable);
            predio.setInformante(informante);
            JsfUti.update(":tdatos:tadicionales:frmObservaciones");
        }
    }

    public void listarPredios(Integer linderos) {
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getParroquia().toString());
        params.put("parroquia", p);
        p = new ArrayList<>();
        p.add(predio.getZona().toString());
        params.put("zona", p);
        p = new ArrayList<>();
        p.add(predio.getSector().toString());
        params.put("sector", p);
        p = new ArrayList<>();
        p.add(predio.getMz().toString());
        params.put("mz", p);
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("width", "70%");
        options.put("height", "90%");
        options.put("closable", true);
        options.put("contentWidth", "100%");
        RequestContext.getCurrentInstance().openDialog("/resources/dialog/predios", options, params);
    }

    @Override
    public void openDialog(ActionEvent event) {
        this.idActionListenner = event.getComponent().getId();
        if (validaciones()) {
            JsfUti.update(":frmSubirDocumentosDialog");
            JsfUti.executeJS("PF('dlgSubirDocumento').show()");
        }
    }

    @Override
    public Boolean validaciones() {
        switch (this.idActionListenner) {
            case "btnS1": // IDENTIFICACION Y UBICACION DEL PREDIO
                if (!seccion1()) {
                    return false;
                }
                break;
            case "btnS2": // IDENTIFICACION LEGAL
                if (!seccion1()) {
                    return false;
                }
                if (!seccion2()) {
                    return false;
                }
                break;
            case "btnS3": // CARACTERIZACION DEL LOTE
                if (!seccion1()) {
                    return false;
                }
                if (!seccion2()) {
                    return false;
                }
                if (!seccion3()) {
                    return false;
                }
//                this.calcularFondoRelativo();
                break;
            case "btnS7": // LINDEROS
                if (!seccion1()) {
                    return false;
                }
                if (!seccion2()) {
                    return false;
                }
                if (!seccion3()) {
                    return false;
                }
                break;
            case "btnS10": // VIVIENDA CENSAL
                if (!seccion1()) {
                    return false;
                }
                if (!seccion3()) {
                    return false;
                }
                break;
            case "btnS11": // VIVIENDA CENSAL
                if (!seccion1()) {
                    return false;
                }
                if (!seccion2()) {
                    return false;
                }
                if (!seccion3()) {
                    return false;
                }
                if (!seccion5()) {
                    return false;
                }
                break;
            case "btnS8": // responsables
                if (!seccion1()) {
                    return false;
                }
                if (!seccion2()) {
                    return false;
                }
                if (!seccion3()) {
                    return false;
                }
                break;

            default:
                break;
        }
        return true;
    }

    @Override
    public Boolean seccion1() {
        if (appconfig.isLocked(sess.getIpClient() + ":" + usr, predio.getId())) {
            Faces.messageWarning(null, "Advertencia!",
                    "El predio esta siendo editado por otro usuario no se puede realizar el guardado!");
            return false;
        }
        if (!this.ver) {
            if (Utils.isEmpty(usosA)) {
                JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar por lo menos un uso del predio.");
                return false;
            }
//            if (Objects.isNull(predio.getTipoConjunto())) {
//                JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar el Espacio Urbano / Rural.");
//                return false;
//            }
//            if (Objects.isNull(predio.getCiudadela())) {
//                JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar el Nombre del espacio urbano / rural.");
//                return false;
//            }
        }
        return true;
    }

    @Override
    public Boolean seccion2() {
        if (Objects.isNull(predio.getPropiedad())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Tenencia.");
            return false;
        }
        if (Objects.isNull(predio.getFormaAdquisicion())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Forma de Adquisición o Tenencia.");
            return false;
        }
        if (Utils.isNotEmpty(propietarios)) {
            BigDecimal porcentajePosession = BigDecimal.ZERO;
            boolean existeTitular = false;
            int count = 0;
            for (CatPredioPropietario propietario : propietarios) {
                if (propietario.getEstado().equalsIgnoreCase("A")) {
                    porcentajePosession = porcentajePosession.add(propietario.getPorcentajePosecion());
                    if (propietario.getTipo().getOrden() == 1) {
                        existeTitular = true;
                        count++;
                    }
                }
            }
//            if (!existeTitular) {
//                JsfUti.messageWarning(null, "Advertencia!", "Debe existir por lo menos un titular.");
//                return false;
//            }
//            if (count > 1) {
//                JsfUti.messageWarning(null, "Advertencia!", "Existe mas de un titular.");
//                return false;
//            }
//            if (porcentajePosession.doubleValue() > 100 /* && porcentajePosession.doubleValue() < 100 */) {
//                JsfUti.messageWarning(null, "Advertencia!", "El Porcentaje Posesión de los propietarios debe sumar el 100%.");
//                return false;
//            }
        }
        if (ss.getParametros().get("proceso") != null) {
            TipoProceso proceso = (TipoProceso) ss.getParametros().get("proceso");
            if (proceso.equals(TipoProceso.ACTIALIZAR_AREAS_LINDEROS)) {
//                if (Utils.isEmpty(predio.getCatEscrituraCollection()) && !esPosesion()) {
//                    JsfUti.messageWarning(null, "Advertencia!", "Debe Ingresar la escritura.");
//                    return false;
//                }
            }
        }
        return true;
    }

    @Override
    public Boolean seccion3() {
        for (CatPredioPropietario propietario : propietarios) {
            if (propietario.getEstado().equalsIgnoreCase("A")) {
                if (propietario.getId() == null) {
                    JsfUti.messageWarning(null, "Advertencia!", "Debe guardar los datos de propietarios en la seccion 2.");
                    return false;
                }
            }
        }
        if (Objects.isNull(caracteristicas.getLocManzana())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Localizacion en Mz.");
            return false;
        }
//        if (!this.estaEdificado() && Utils.isNotEmpty(predio.getCatPredioEdificacionCollection())) {
//            JsfUti.messageWarning(null, "Advertencia!", "Debe eliminar los bloques para continuar.");
//            return false;
//        }
        if (Objects.isNull(caracteristicas.getEstadoSolar())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Ocupacion.");
            return false;
        }
        if (Objects.isNull(predio.getTipoSuelo())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Caract. Suelo.");
            return false;
        }
        if (Objects.isNull(predio.getTopografiaSolar())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Topografia.");
            return false;
        }
        if (Objects.isNull(predio.getFormaSolar())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Forma del predio.");
            return false;
        }
        if (Objects.isNull(predio.getConstructividad())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Constructividad.");
            return false;
        }
        if (Utils.isNotEmpty(predio.getCatPredioClasificRuralCollection())) {
            for (CatPredioClasificRural cpc : predio.getCatPredioClasificRuralCollection()) {
                if (cpc.getValorTerreno() == null) {
                    JsfUti.messageWarning(null, "Error!", "Debe Valorar la calidad del suelo del predio.");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Boolean seccion5() {
        if (Objects.isNull(predio.getClasificacionVivienda())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la  Clasificacion Vivienda.");
            return false;
        }
        if (Objects.isNull(predio.getTipoVivienda())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Tipo Vivienda.");
            return false;
        }
        if (Objects.isNull(predio.getCondicionVivienda())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar la Condicion Vivienda.");
            return false;
        }
        if (Objects.isNull(servicios.getAbastAgua())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar Agua Proviene.");
            return false;
        }
        if (Objects.isNull(servicios.getAbastAguaRecibe())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar Agua Recibe.");
            return false;
        }
        if (Objects.isNull(servicios.getEvacAguasServ())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe seleccionar Eliminacion Excretas.");
            return false;
        }
        if (Objects.isNull(predio.getNumHabitaciones())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe ingresar el Numero de Habitaciones.");
            return false;
        }
        if (Objects.isNull(predio.getNumDormitorios())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe ingresar el Numero de Dormitorios.");
            return false;
        }
        if (Objects.isNull(predio.getNumEspaciosBanios())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe ingressar los Espacios Para Bañarse o Duchas.");
            return false;
        }
        if (Objects.isNull(predio.getHabitantes())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe ingresar el Numero de Habitantes.");
            return false;
        }
        if (Objects.isNull(predio.getNumHogares())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe ingresar el Numero de Hogares.");
            return false;
        }
        if (Objects.isNull(predio.getNumCelulares())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe ingresar el Numero de Telefonos Celulares .");
            return false;
        }
        if (!this.estaEdificado() && Utils.isNotEmpty(predio.getCatPredioEdificacionCollection())) {
            JsfUti.messageWarning(null, "Advertencia!", "Debe eliminar los bloques para continuar.");
            return false;
        }
        return true;
    }

    @Override
    public String generarJson(CatPredio p) {
        if (p == null) {
            return null;
        }
        if (p.getTipoConjunto() != null) {
            p.getTipoConjunto().setCatCiudadelaCollection(null); // No cargar la collection de Ciudadelas.
        }
        if (p.getEscrituraLinderos() != null && p.getEscrituraLinderos().getCanton() != null) {
            p.getEscrituraLinderos().getCanton().setCatParroquiaCollection(null);// no cargar la collection y
            // parroquias.
        }
        if (p.getCatEscrituraCollection() != null) {
            p.getCatEscrituraCollection().stream().map((catEscritura) -> {
                if (catEscritura.getCanton() != null) {
                    catEscritura.getCanton().setCatParroquiaCollection(null);
                }
                return catEscritura;
            }).filter((catEscritura) -> (catEscritura != null))
                    .filter((catEscritura) -> (catEscritura.getProvincia() != null
                    && catEscritura.getProvincia().getCatCantonCollection() != null))
                    .forEachOrdered((catEscritura) -> {
                        catEscritura.getProvincia().setCatCantonCollection(null);
                    });
        }
        JsonUtils jsonUtils = new JsonUtils();
        return jsonUtils.generarJson(p);
    }

    public void selectedResponsableCiudadano(SelectEvent event) {
        CiuCiudadano responsable = (CiuCiudadano) event.getObject();
        switch (controlResponsable) {
            case "fiscalizador":
                this.predio.setCiuFiscalizador(responsable.getCiuCedRuc());
                this.predio.setCiuNombresFiscalizador(responsable.getCiuNombreCompleto());
                JsfUti.update(":tdatos:frmResponsables:actFisc");
                break;
            case "actualizador":
                this.predio.setCiuActualizador(responsable.getCiuCedRuc());
                this.predio.setCiuNombresActualizador(responsable.getCiuNombreCompleto());
                JsfUti.update(":tdatos:frmResponsables:actPredial");
                break;
            case "enteHorizontal":
                this.predio.setCiuHorizontal(responsable.getCiuCedRuc());
                this.predio.setCiuNombresHorizontal(responsable.getCiuApellidoPat() + " " + responsable.getCiuApellidoMat()
                        + " " + responsable.getCiuPrimerNombre() + " " + responsable.getCiuSegundoNombre());
                JsfUti.update("tdatos:frmViviendaCensal:pngJefeHogar");
                break;
            case "informante":
                predio.setCiuInformante(responsable.getCiuCedRuc());
                predio.setCiuNombresInformante(responsable.getCiuNombreCompleto());
                JsfUti.update("tdatos:frmViviendaCensal:pngObsVn");
                break;
            default:
                break;
        }
    }

    public void calcularAreaConsAlic() {
        if (esPh()) {
            if (predio.getAlicuotaConst() == null) {
                JsfUti.messageInfo(null, "Debe ingresar la alicuita de construccion.", "");
                return;
            }
            if (predio.getPredioRaiz() == null) {
                JsfUti.messageInfo(null, "No tiene asociado un predio matriz.", "");
                return;
            }
            CatPredio pr = (CatPredio) EntityBeanCopy.clone(catas.getPredioId(predio.getPredioRaiz().longValue()));

            this.predio.setAreaDeclaradaConst(this.getAreaConstSegunAlicuota(pr));
            this.predio.setAvaluoConstruccion((this.predio.getAlicuotaConst().divide(BigDecimal.valueOf(100.00))).multiply(pr.getAvaluoConstruccion()));
            this.predio.setAvaluoMunicipal(this.predio.getAvaluoConstruccion().add(this.predio.getAvaluoSolar()));
            this.predio.setBaseImponible(this.predio.getAvaluoConstruccion().add(this.predio.getAvaluoSolar()));
            this.verificarAreaAumento();
        }
    }

    public BigDecimal getAreaConstSegunAlicuota(CatPredio pr) {
        return (predio.getAlicuotaConst().divide(BigDecimal.valueOf(100.00))).multiply(pr.getAreaDeclaradaConst());
    }

    public void verificarAreaAumento() {
        BigDecimal areaAumento = BigDecimal.ZERO;
        if (this.esPh()) {
            areaAumento = this.sumarAreaConst();
            if (areaAumento.doubleValue() > 0) {
                predio.setAreaAumentoCons(areaAumento.subtract(predio.getAreaDeclaradaConst()));
                predio.setAreaTotalCons(areaAumento);
            } else {
                predio.setAreaTotalCons(predio.getAreaDeclaradaConst());
            }
        }
    }

    public void seleccionarPredio(SelectEvent event) {
        try {
            if (event != null) {
                if (event.getObject() == null) {
                    Faces.messageWarning(null, "Advertencia!", "Debe seleccionar el predio...");
                    return;
                }

                List<CatPredio> temp = (List<CatPredio>) event.getObject();
                if (temp != null && temp.size() > 0) {
                    CatPredio cp = temp.get(0);
                    if (Objects.equals(cp.getParroquia(), predio.getParroquia())
                            && Objects.equals(cp.getZona(), predio.getZona())
                            && Objects.equals(cp.getSector(), predio.getSector())
                            && Objects.equals(cp.getMz(), predio.getMz())) {
                        predioColind = cp;
                        nombreLindero = cp.getClaveCat();
                        JsfUti.update(":tdatos:frmLinderos");
                        JsfUti.messageInfo(null, "Información", "El predio Seleccionado: " + cp.getClaveCat());
                    } else {
                        JsfUti.messageInfo(null, "Información", "El predio no corresponde a la misma parroquia, zona, sector o manzana.");
                    }
                } else {
                    JsfUti.messageInfo(null, "Información", "No ha seleccionado ningun predio.");
                }

            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, predioId, e);
        }
    }

    public void reporteDetalleCalculos(CatPredioAvalHistorico avalHistorico) {
        try {
            if (predio != null) {
                ss.borrarDatos();
                ss.instanciarParametros();
                String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                ss.setTieneDatasource(Boolean.TRUE);
                ss.setNombreSubCarpeta("catastro/avaluos");
                if (dataBaseConnect()) {
                    ss.setNombreReporte("detalleCalculoEmision");
                } else {
                    ss.setNombreReporte("detalleCalculoEmisionOracle");
                    ss.agregarParametro("ANIO_INICIO", avalHistorico.getAnioInicio());
                    ss.agregarParametro("ANIO_FIN", avalHistorico.getAnioFin());
                }

                ss.agregarParametro("LOGO_FOOTER", path + SisVars.sisLogo1);
                ss.agregarParametro("LOGO", path + SisVars.logoReportes);
                ss.agregarParametro("LOGO2", path + SisVars.sisLogo1);
                ss.agregarParametro("SUBREPORT_DIR", Faces.getRealPath("/reportes/"));
                ss.agregarParametro("ID_AVAL_HISTORICO", avalHistorico.getId());
                ss.agregarParametro("USUARIO", sess.getName_user());
                JsfUti.redirectNewTab("/sgmEE/Documento");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    public void redirectFichaPredial(CatPredio predio) {
        if (predio == null) {
            JsfUti.messageInfo(null, "Informacion", "No se encontro predio.");
        }
        if (predio.getId() == null) {
            JsfUti.messageInfo(null, "Informacion", "No se encontro predio.");
        }

        JsfUti.redirectNewTab(SisVars.urlbaseFaces + "vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml?predio=" + predio.getId());
//        JsfUti.redirectNewTab(SisVars.urlbaseFaces + "vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml?claveCat=" + predio.getClaveCat());
    }

    public void redirecGeoportal() {
        try {
            String url = "http://gisimi.ibarra.gob.ec/GsVisor.php/sfGuardAuth/IngresoExternoEmb?username=SISAC&password=SISAC&clave_catastral="
                    + this.predio.getClaveCat();
            JsfUti.redirectNewTab(url);
        } catch (Exception ex) {
            Logger.getLogger(FichaPredial.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateTableDocument() {
        ConfigFichaPredial cfp = new ConfigFichaPredial();
        if (cfp.getRedenerFichaIb()) {
            updateTableDocumento = "tdatos:docAdj";
        } else {
            updateTableDocumento = "";
        }

    }

    public void actualizarAreasBloques() {
        if (this.esPh()) {
            this.calcularAreaConsAlic();
        } else {
            predio.setAreaDeclaradaConst(this.sumarAreaConst());
        }
        if (Utils.isEmpty(bloques) && !this.predio.getPropiedadHorizontal()
                && !this.predio.getFichaMadre()) {
            this.predio.setAvaluoConstruccion(BigDecimal.ZERO);
            this.predio.setAvaluoMunicipal(this.predio.getAvaluoSolar());
        }
    }

    public void aniosAnteriores() {
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getPredialant());
        params.put("clave", p);
        Utils.openDialog("/mantenimientos/aniosAnteriores", params, "420");
    }

    public void saveCensalService() {
        try {
            obs = "Vivienda censal.";
            if (predio.getCatPredioS6() != null) {
//                this.servicios = predio.getCatPredioS6();
                this.servicios.setPredio(predio);
            } else {
                this.servicios = new CatPredioS6();
                this.servicios.setPredio(predio);
            }
        } catch (NumberFormatException ne) {
            LOG.log(Level.SEVERE, "Obtener datos ficha saveCensalService", ne);
        }
        try {
//            this.guardarServicios(this.servicios, null, this.instalacionesEspeciales, this.enteHorizontal);
            this.guardarDatosPredio(obs, informante);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Guardar saveCensalService", e);
        }

    }

    @Override
    public Boolean guardarDatosPredio(String observacion, CatEnte informante) {
        try {
            if (observacion == null || observacion.equals("")) {
                observacion = "Actualizacion Informacion de Datos catastrales ";
            }
            if (predio != null) {
                if (!seccion1()) {
                    return false;
                }
                try {
                    predio.setClaveCat(claveCatastral(predio));
                } catch (Exception e) {
                }
                if (servicios != null) {
                    if (Utils.isNotEmpty(usosA)) {
                        servicios.setUsosList(usosA);
                        predio.setCatPredioS6(this.catas.guardarPredioS6(servicios));
                    }
                }
                this.actualizarAreasBloques();
                if (predio != null) {
                    predio.setCatPredioS4(this.catas.guardarPredioS4(caracteristicas));
                    predio = catas.guardarPredio(predio);
                    this.predio.setCatPredioPropietarioCollection(propietarios);
                    if (saveHistoric(predio, observacion, null, null, null, null)) {
                        servicios = predio.getCatPredioS6();
                        this.propietarios = (List<CatPredioPropietario>) predio.getCatPredioPropietarioCollection();
                        System.out.println("this.propietarios " + this.propietarios);
                        return true;
                    } else {
                        Faces.messageWarning(null, "Advertencia", "Problemas al guardar Datos Historicos del Predio");
                        return false;
                    }
                } else {
                    Faces.messageWarning(null, "Advertencia!",
                            "Ha ocurrido un error al actualizar la informacion predial, verifique que los campos esten ingresados correctamente");
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public void guardarServicios(CatPredioS6 s6, List<CtlgItem> vias, List<CtlgItem> instalaciones,
            CatEnte enteHorizontal) {
        HiberUtil.newTransaction();
        if (this.getRemotteCommand().equalsIgnoreCase("rc10")) {
            obs = "ACTUALIZACION DE INFORMACION DE VIVIENDA CENSAL";
        } else {
            obs = "Actualizacion Informacion de Servicios";
        }
        if (enteHorizontal != null) {
            if (enteHorizontal.getId() != null) {
                this.saveResponsable(null, null, enteHorizontal);
            }
            if (this.validarServicios(s6) == false) {
                return;
            }
        }
        try {
            if (this.guardarDatosPredio(this.obs, informante)) {
                CatPredioS6 s6Temp = catas.guardarPredioS6(s6, vias, instalaciones);
                if (s6Temp != null) {
                    predio.setCatPredioS6(s6Temp);
                    this.servicios = s6Temp;
                    if (this.guardarDatosPredio(obs, informante) == true) {
                        Faces.messageInfo(null, "Nota!", "Datos Actualizados satisfactoriamente");
                    } else {
                        Faces.messageWarning(null, "Advertencia!",
                                "Ha ocurrido un error al Actualizar, verifique que los campos esten ingresados correctamente");
                    }
                } else {
                    Faces.messageWarning(null, "Advertencia!",
                            "Ha ocurrido un error al actualizar los servicios prediales, verifique que los campos esten ingresados correctamente");
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    public void saveEscrituraControl(CatEscritura propietario) {
        if (!seccion1()) {
            return;
        }
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (propietario != null && propietario.getIdEscritura() != null) {
            p.add(propietario.getIdEscritura().toString());
        }
        params.put("idEscritura", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        params.put("nuevo", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        p = new ArrayList<>();
        p.add(editable.toString());
        params.put("editar", p);
        Utils.openDialog("/resources/dialog/escritura", params, "350", "80");
    }

    public void procesarEscritura(SelectEvent event) {
        escriturasConsulta = em.findAll(Querys.getEscriturasByPredio, new String[]{"predio"},
                new Object[]{predio});
        this.predio.setCatEscrituraCollection(escriturasConsulta);
        escritura = (CatEscritura) EntityBeanCopy.clone(event.getObject());
        if (saveHistoric(predio, "ACTUALIZACION DE IDENTIFICACION LEGAL", getFichaEdifAnt(), getFichaEdifAct(),
                getFichaModelAnt(), getFichaModelAct())) {
            JsfUti.messageInfo(null, "Exito", "Datos grabados Satisfactoriamente");
        } else {
            JsfUti.messageInfo(null, "Exito", "Datos grabados Satisfactoriamente");
        }
        if (esProcesoActualizacionAreasLind()) {
            this.getSumaAreaPredioDesmenbraciones();
            this.getCalcularExcedenteDiferencia();
            this.getPorcentajeExcedenteDiferencia();
            JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:infAdcActAreasLinderos");
            JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:infAdcActAreasLinderos");
            JsfUti.update("tdatos:frmDatosPredio:tvUbicacion:infAdcActAreasLinderos");
        }
    }

    public void guardarCaracteristicas(CatPredioS4 s4, CatPredio p, CatPredioS6 s6) {
        try {
            obs = "ACTUALIZACION INFORMACION DE CARACTERISTICAS";
            servicios = s6;
            if (this.esPh()) {
                this.calcularAreaConsAlic();
                // this.calcularAreaTerrAlic();
            }
            if (s4 != null) {
                caracteristicas = catas.guardarPredioS4(caracteristicas);
                predio.setCatPredioS4(caracteristicas);
            }
            if (this.guardarDatosPredio(obs, informante)) {
                JsfUti.update("tdatos:frmVivienda");
                JsfUti.update("tdatos:frmVivienda:fieldJH");
                JsfUti.update("tdatos:frmVivienda:fieldNV");
                JsfUti.update("tdatos:frmVivienda:pngObsVn");
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, null, e);
        }
    }

    public List<CatProvincia> getProvincias() {
        return (List<CatProvincia>) EntityBeanCopy.clone(this.catas.provincias());
    }

    public CtlgItem listadoItemsCultivos(CtlgItem tipo) {
        if (tipo == null) {
            return null;
        }
        if (tipo.getHijo() != null) {
            return (CtlgItem) EntityBeanCopy.clone(em.find(Querys.getCtlgItemaByCultivosHijos, new String[]{"hijo"},
                    new Object[]{tipo.getHijo().longValue()}));
        }
        return null;
    }

    public Boolean esPh() {
        return predio.getPropiedadHorizontal();
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
    }

    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public List<CatPredioPropietario> getPropietarios() {
        return propietarios;
    }

    public void setPropietarios(List<CatPredioPropietario> propietarios) {
        this.propietarios = propietarios;
    }

    public CatPredioS12 getUsos() {
        return usos;
    }

    public void setUsos(CatPredioS12 usos) {
        this.usos = usos;
    }

    public List<CtlgItem> getUsosA() {
        return usosA;
    }

    public void setUsosA(List<CtlgItem> usosA) {
        this.usosA = usosA;
    }

    public List<FotoPredio> getFotos() {
        return fotos;
    }

    public void setFotos(List<FotoPredio> fotos) {
        this.fotos = fotos;
    }

    public CatEscritura getEscritura() {
        return escritura;
    }

    public void setEscritura(CatEscritura escritura) {
        this.escritura = escritura;
    }

    public CtlgItem getTipoPrototipos() {
        return tipoPrototipos;
    }

    public void setTipoPrototipos(CtlgItem tipoPrototipos) {
        this.tipoPrototipos = tipoPrototipos;
    }

    public List<CtlgItem> getPrototipos() {
        return prototipos;
    }

    public void setPrototipos(List<CtlgItem> prototipos) {
        this.prototipos = prototipos;
    }

    public List<SectorValorizacion> getSubsectores() {
        return subsectores;
    }

    public void setSubsectores(List<SectorValorizacion> subsectores) {
        this.subsectores = subsectores;
    }

    public CatParroquia getParroquia() {
        return parroquia;
    }

    public void setParroquia(CatParroquia parroquia) {
        this.parroquia = parroquia;
    }

    public List<CtlgItem> getVias() {
        return vias;
    }

    public void setVias(List<CtlgItem> vias) {
        this.vias = vias;
    }

    public BaseLazyDataModel<CatEscritura> getEscrituras() {
        return escrituras;
    }

    public void setEscrituras(BaseLazyDataModel<CatEscritura> escrituras) {
        this.escrituras = escrituras;
    }

    public List<CatCanton> getCantones() {
        return cantones;
    }

    public void setCantones(List<CatCanton> cantones) {
        this.cantones = cantones;
    }

    public CatCanton getCanton() {
        if (canton == null) {
            canton = new CatCanton();
        }

        return canton;
    }

    public void setCanton(CatCanton canton) {
        this.canton = canton;
    }

    public CatEscritura getEscr() {
        return escr;
    }

    public void setEscr(CatEscritura escr) {
        this.escr = escr;
    }

    public Boolean getControlAddUp() {
        return controlAddUp;
    }

    public void setControlAddUp(Boolean controlAddUp) {
        this.controlAddUp = controlAddUp;
    }

    public List<CtlgItem> getInstalacionesEspeciales() {
        return instalacionesEspeciales;
    }

    public void setInstalacionesEspeciales(List<CtlgItem> instalacionesEspeciales) {
        this.instalacionesEspeciales = instalacionesEspeciales;
    }

    public CatEnteLazy getResponsablesLazy() {
        return responsablesLazy;
    }

    public void setResponsablesLazy(CatEnteLazy responsablesLazy) {
        this.responsablesLazy = responsablesLazy;
    }

    public CatEnte getActualizadorPredio() {
        if (actualizadorPredio == null) {
            if (predio.getResponsableActualizadorPredial() != null) {
                actualizadorPredio = predio.getResponsableActualizadorPredial();
            } else {
                actualizadorPredio = new CatEnte();
            }
        }
        return actualizadorPredio;
    }

    public void setActualizadorPredio(CatEnte actualizadorPredio) {
        this.actualizadorPredio = actualizadorPredio;
    }

    public CatEnte getFiscalizadorPredio() {
        if (fiscalizadorPredio == null) {
            if (predio.getResponsableFiscalizadorPredial() != null) {
                fiscalizadorPredio = predio.getResponsableFiscalizadorPredial();
            } else {
                fiscalizadorPredio = new CatEnte();
            }
        }
        return fiscalizadorPredio;
    }

    private BigDecimal sumarObras() {
        try {
            if (Utils.isEmpty(obrasInternas)) {
                return BigDecimal.ZERO;
            }
            BigDecimal area = BigDecimal.ZERO;
            for (CatPredioObraInterna obrasInterna : obrasInternas) {
                if (obrasInterna.getArea() == null) {
                    obrasInterna.setArea(BigDecimal.ZERO);
                }
                area = area.add(obrasInterna.getArea());
            }
            return area;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, usr, e);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal sumarAvaluoObras() {
        try {
            if (Utils.isEmpty(obrasInternas)) {
                return BigDecimal.ZERO;
            }
            BigDecimal avaluo = BigDecimal.ZERO;
            for (CatPredioObraInterna obrasInterna : obrasInternas) {
                if (obrasInterna.getAvaluo() == null) {
                    obrasInterna.setAvaluo(BigDecimal.ZERO);
                }
                avaluo = avaluo.add(obrasInterna.getAvaluo());
            }
            return avaluo;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, usr, e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Si no Existe bloques ingresados retorna 0.
     *
     * @return Suma de los bloques si existe, caso contrario 0.
     */
    private BigDecimal sumarAreaConst() {
        try {
            if (Utils.isEmpty(bloques)) {
                return BigDecimal.ZERO;
            }
            BigDecimal areaConst = BigDecimal.ZERO;
            for (CatPredioEdificacion b : bloques) {
                if (b.getAreaBloque() == null) {
                    b.setAreaBloque(BigDecimal.ZERO);
                }
                if (b.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
                    areaConst = areaConst.add(b.getAreaBloque());
                }
            }
            return areaConst;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return BigDecimal.ZERO;
    }

    public void setFiscalizadorPredio(CatEnte fiscalizadorPredio) {
        this.fiscalizadorPredio = fiscalizadorPredio;
    }

    public Integer getTipoEnte() {
        return tipoEnte;
    }

    public void setTipoEnte(Integer tipoEnte) {
        this.tipoEnte = tipoEnte;
    }

    public Boolean getEsPersona() {
        return esPersona;
    }

    public void setEsPersona(Boolean esPersona) {
        this.esPersona = esPersona;
    }

    public CatEnte getEnteHorizontal() {
        if (enteHorizontal == null) {
            if (predio.getEnteHorizontal() != null) {
                enteHorizontal = predio.getEnteHorizontal();
            } else {
                enteHorizontal = new CatEnte();
            }
        }
        return enteHorizontal;
    }

    public void setEnteHorizontal(CatEnte enteHorizontal) {
        this.enteHorizontal = enteHorizontal;
    }

    public String getElementosConstuctivos() {
        return elementosConstuctivos;
    }

    public void setElementosConstuctivos(String elementosConstuctivos) {
        this.elementosConstuctivos = elementosConstuctivos;
    }

    public String getPredioId() {
        return predioId;
    }

    public void setPredioId(String predioId) {
        this.predioId = predioId;
    }

    public CatPredio getPredioRaiz() {
        if (predio == null) {
            return null;
        }
        if (predio.getPredioRaiz() != null) {
            return (CatPredio) EntityBeanCopy.clone(em.find(CatPredio.class, predio.getPredioRaiz().longValue()));
        }
        return null;
    }

    public List<CatTiposDominio> getDominios() {
        return em.findAllObjectOrder(CatTiposDominio.class, new String[]{"nombre"}, true);
    }

    public Boolean getCoopropietarios() {
        return coopropietarios;
    }

    public void setCoopropietarios(Boolean coopropietarios) {
        this.coopropietarios = coopropietarios;
    }

    public CatPredioFusionDivision getCatPredioFusionDivision() {
        return catPredioFusionDivision;
    }

    public void setCatPredioFusionDivision(CatPredioFusionDivision catPredioFusionDivision) {
        this.catPredioFusionDivision = catPredioFusionDivision;
    }

    public Boolean getPh() {
        return ph;
    }

    public void setPh(Boolean ph) {
        this.ph = ph;
    }

    @Override
    public void setMainConfig(MainConfig mainConfig) {
        super.setMainConfig(mainConfig); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MainConfig getMainConfig() {
        return super.getMainConfig(); //To change body of generated methods, choose Tools | Templates.
    }

    public void updatePredio() {
        System.out.println("Info:" + predio.getTipoPoseedor().getValor());
    }

    public String getObservacionEliminar() {
        return observacionEliminar;
    }

    public void setObservacionEliminar(String observacionEliminar) {
        this.observacionEliminar = observacionEliminar;
    }

    public Object getObjectoEliminar() {
        return objectoEliminar;
    }

    public void setObjectoEliminar(Object objectoEliminar) {
        this.objectoEliminar = objectoEliminar;
    }

    public void eliminarObs() {

        if (observacionEliminar == null) {
            JsfUti.messageWarning(null, "Advertencia", "Debe ingresar la observación");
            return;
        }
        switch (this.tipoEliminar) {
            case 1: // propeitarios
                CatPredioPropietario pp = (CatPredioPropietario) getObjectoEliminar();
                pp.setObservaciones(observacionEliminar);
                this.eliminarPropietario(pp);
                JsfUti.update("tdatos:frmEscrituras:dtPropietarios");
                break;
            case 2:// cultivo
                CatPredioCultivo cp = (CatPredioCultivo) getObjectoEliminar();
                cp.setObservaciones(observacionEliminar);
                this.eliminarCultivo(cp);
                JsfUti.update("tdatos:frmCaracSolar:dtCultivos");
                break;
            case 3:// clasificación suelo rural
                CatPredioClasificRural ccp = (CatPredioClasificRural) getObjectoEliminar();
                ccp.setObservaciones(observacionEliminar);
                this.eliminarClasificRural(ccp);
                JsfUti.update("tdatos:frmCaracSolar:dtSueloRural");
                break;
            case 4:// bloque
                CatPredioEdificacion cb = (CatPredioEdificacion) getObjectoEliminar();
                cb.setObservaciones(observacionEliminar);
                this.eliminarBloque(cb);
                JsfUti.update("tdatos:frmEdificaciones:tvEdificaciones:dtBloques");
                JsfUti.update("tdatos:frmViviendaCensal:btnS10");
//                JsfUti.update("tdatos:frmViviendaCensal:field1");
                JsfUti.update("tdatos:frmViviendaCensal:fieldJH");
                JsfUti.update("tdatos:frmViviendaCensal:fieldNV");
                JsfUti.update("tdatos:frmViviendaCensal:pngObsVn");
                break;
            case 5:// obraInterna
                CatPredioObraInterna coi = (CatPredioObraInterna) getObjectoEliminar();
                coi.setObservaciones(observacionEliminar);
                eliminarObraCompl(coi);
                JsfUti.update("tdatos:frmEdificaciones:tvEdificaciones:dtObrasInternas");
                break;
            default:
                break;
        }
        JsfUti.executeJS("PF('dlgConfirmarEliminacion').hide()");
    }

    public void obraInterna(CatPredioObraInterna obraInterna) {
        if (!seccion1()) {
            return;
        }
        if (!seccion2()) {
            return;
        }
        if (!seccion3()) {
            return;
        }
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (obraInterna != null && obraInterna.getId() != null) {
            p.add(obraInterna.getId().toString());
            params.put("idCatPredioObraInterna", p);
        }

        p = new ArrayList<>();
        p.add(Boolean.toString((obraInterna == null)));
        params.put("nuevo", p);

        p = new ArrayList<>();
        p.add(ver.toString());
        params.put("ver", p);
        ver = false;
        Utils.openDialog("/resources/dialog/obraInterna", params);
    }

    public void procesarObraInterna(SelectEvent event) {
        CatPredioObraInterna obraInterna = (CatPredioObraInterna) event.getObject();
        if (Utils.isEmpty(obrasInternas)) {
            obrasInternas = new ArrayList<>();
        }
        if (obraInterna != null) {
            if (!obrasInternas.contains(obraInterna)) {
                obrasInternas.add(obraInterna);
            } else {
                obrasInternas.set(obrasInternas.indexOf(obraInterna), obraInterna);
            }
            if (this.predio.getAvaluoObraComplement() == null) {
                this.predio.setAvaluoObraComplement(BigDecimal.ZERO);
            }
            this.predio.setAreaObras(this.sumarObras());
            this.predio.setAvaluoObraComplement(this.sumarAvaluoObras());
            this.predio.setCatPredioObraInternaCollection(obrasInternas);
            if (this.saveHistoric(predio, "ACTUALIZACION CARACTERIZACION Y ELEMENTOS CONSTRUCTIVOS DE LA EDIFICACION",
                    null, null, null, null)) {
                Faces.messageInfo(null, "Nota!", "Obras Internas actualizadas satisfactoriamente");
            } else {
                Faces.messageInfo(null, "Nota!", "Ocurrio un error al guardar Obras Internas");
            }
        }
    }

    public void eliminarObraCompl(CatPredioObraInterna obra) {
        obra.setEstado("I");
        obra.setModificado(sess.getName_user());
        obra = catas.guardarObraInterna(obra, null);
        obrasInternas.remove(obra);
        if (this.predio.getAvaluoObraComplement() == null) {
            this.predio.setAvaluoObraComplement(BigDecimal.ZERO);
        }
        this.predio.setAreaObras(this.sumarObras());
        this.predio.setAvaluoObraComplement(this.sumarAvaluoObras());
        predio.setCatPredioObraInternaCollection(obrasInternas);
        if (this.saveHistoric(predio, "Bloque", null, null, null, null)) {
            JsfUti.messageInfo(null, "Edificacion", "Edificacion eliminada.");
        }
        JsfUti.messageInfo(null, "Nota!", "Obra Interna o Complementaria eliminada.");
    }

    public void observacionesEliminar(Integer tipoEliminar) {
        this.tipoEliminar = tipoEliminar;
        if (this.tipoEliminar == 1) {
            if (this.propietarios.size() == 1) {
                JsfUti.messageError(null, "Propietario", "No se pueden eliminar todos los propietarios.");
                return;
            }
        }
        this.observacionEliminar = null;
        JsfUti.executeJS("PF('dlgConfirmarEliminacion').show()");
        JsfUti.update("frmConfirmarEliminacion");
    }

    public void calcularFondoRelativo() {
        if ((this.predio.getTipoPredio().equalsIgnoreCase("U") || this.predio.getTipoPredio().contains("U"))
                && !this.predio.getPropiedadHorizontal()) {
            if (caracteristicas == null) {
                System.out.println("caracteristicas es nullo.");
                return;
            }
            if (caracteristicas.getFrente1() == null) {
                JsfUti.messageInfo(null, "Debe ingresar el frente.", "");
                return;
            }
            if (caracteristicas.getFrente1().compareTo(BigDecimal.ZERO) == 0) {
                JsfUti.messageInfo(null, "Debe ingresar el frente.", "");
                return;
            }
            if (predio.getAreaSolar() == null) {
                System.out.println("El area del solar es cero.");
                return;
            }
            if (predio.getAreaSolar().compareTo(BigDecimal.ZERO) == 0) {
                System.out.println("El area del solar es cero.");
                return;
            }
            caracteristicas.setFondo1(predio.getAreaSolar().divide(caracteristicas.getFrente1(), RoundingMode.HALF_UP));
        }
    }

    public void calcularAreaTerrAlic() {
        if (esPh()) {
            if (predio.getAlicuotaTerreno() == null) {
                JsfUti.messageInfo(null, "Debe ingresar la alicuita de terreno.", "");
                return;
            }
            if (predio.getPredioRaiz() == null) {
                JsfUti.messageInfo(null, "No tiene asociado un predio matriz.", "");
                return;
            }
            CatPredio pr = (CatPredio) EntityBeanCopy.clone(catas.getPredioId(predio.getPredioRaiz().longValue()));
            if (pr.getAreaDeclaradaConst() == null) {
                pr.setAreaDeclaradaConst(BigDecimal.ZERO);
            }
            predio.setAreaSolar((predio.getAlicuotaTerreno().multiply(pr.getAreaSolar())).divide(BigDecimal.valueOf(100.00)));
//            predio.setAreaTerrenoAlicuota((predio.getAlicuotaTerreno().multiply(pr.getAreaSolar())).divide(BigDecimal.valueOf(100.00)));
            predio.setAvaluoSolar((predio.getAlicuotaTerreno().multiply(pr.getAvaluoSolar())).divide(BigDecimal.valueOf(100.00)));
            this.predio.setAvaluoMunicipal(this.predio.getAvaluoConstruccion().add(this.predio.getAvaluoSolar()));
            this.predio.setBaseImponible(this.predio.getAvaluoConstruccion().add(this.predio.getAvaluoSolar()));
        }
    }

    private Boolean esPosesion() {
        return predio.getFormaAdquisicion() != null && (predio.getFormaAdquisicion().getCodename().equals("posesion")
                || predio.getFormaAdquisicion().getCodename().equals("mostrenco"));
    }

    public void completarEdicion() {
        String tipo = null;
        if (this.ss.getParametros() != null) {
            if (!seccion1() || !seccion2() || !seccion3()) {
                return;
            }
            if (estaEdificado()) {
                if (!seccion5()) {
                    return;
                }
            }

            if (ss.getParametros().get("idTramite") != null) {
                TipoProceso proceso = TipoProceso.DIVIDIR_PREDIO;
                if (ss.getParametros().get("proceso") != null) {
                    proceso = (TipoProceso) ss.getParametros().get("proceso");
                }
                // Validamos si hay excedente o diferencia solo para ACTIALIZAR_AREAS_LINDEROS
                if (proceso.equals(TipoProceso.ACTIALIZAR_AREAS_LINDEROS)) {
                    if (!esPosesion()) {
                        int compareTo = getPorcentajeExcedenteDiferencia().compareTo(BigDecimal.TEN);
                        // Verificamos si hay excedente
                        String comentario = "";
                        if (compareTo >= 0) {
                            JsfUti.messageInfo(null, "Advertencia", "Existe excedente entre el area de escritura y la suma de area de plano mas area de desmenbraciones");
                            comentario = "Predio debe regularizar el excedente";
                            tipo = "excedente";
                            this.registrarRestriccion(comentario);
                        }
                        compareTo = getPorcentajeExcedenteDiferencia().compareTo(BigDecimal.valueOf(-0.10));
                        // Verificamos si hay diferencia
                        if (compareTo <= 0) {
                            JsfUti.messageInfo(null, "Advertencia", "Existe diferencia entre el area de escritura y la suma de area de plano mas area de desmenbraciones");
                            comentario = "Predio debe regularizar la diferencia";
                            tipo = "diferencia";
                            this.registrarRestriccion(comentario);
                        }
                    }
                }
//                predio.setAreaSolar(caracteristicas.getAreaGraficaLote());
                divisionServices.actualizarDetalleTramite(Long.valueOf(ss.getParametros().get("idTramite").toString()),
                        predio, true, BigInteger.ONE, proceso);
                Long idTramite = Long.valueOf(ss.getParametros().get("idTramite").toString());
                Long idPredio = Long.valueOf(ss.getParametros().get("idPredio").toString());
                saveObservation(idTramite, idPredio, taskKey, observaciones, sess.getName_user());
                this.setPredioAnt(this.jsTarea);
                this.guardarDatosPredio("TAREA", informante);
                dataBaseIb.crearBloqueGis(bloqueGisFinal);
                JsfUti.messageInfo(null, "Info", "Tarea completada con exito.");
                // Mostramos el reporte
                if (tipo != null) {
                    this.generarReporteDiferenciaExcedente(tipo);
                }
                JsfUti.redirectFaces(ss.getParametros().get("formEdicion").toString());

            }
            ss.getParametros().remove("areaGrafica");

        }
    }

    /**
     * Registro de restriccion de predio
     *
     * @param comentario Comentario para la restriccion.
     */
    private void registrarRestriccion(String comentario) {
        // Asignamos la clave compuesta
        RestricionPredioPK restricionPredioPK = new RestricionPredioPK(predio.getClaveCat(), 5);
        RestricionPredio rest = this.dataBaseIb.findRestricciones(restricionPredioPK);
        if (rest != null) {
            // Asginamos el comentario
            rest.setComentario(comentario);
            // Asignamos la fecha de inicio
            rest.setFechaInicio(new Date());
            // Asignamos el tramite con el que creo la actualizacion de areas y linderos
            rest.setNumeroTramite(ss.getParametros().get("tramiteQuipux") == null ? null : (String) ss.getParametros().get("tramiteQuipux"));
            // Asignamos el id del predio
            rest.setPredio(BigInteger.valueOf(predio.getId()));
            this.dataBaseIb.getManager().persist(rest);
        } else {
            //Creamos una nueva instancia de una RestricionPredio
            rest = new RestricionPredio(restricionPredioPK);
            // Asginamos el comentario
            rest.setComentario(comentario);
            // Asignamos la fecha de inicio
            rest.setFechaInicio(new Date());
            // Asignamos el tramite con el que creo la actualizacion de areas y linderos
            rest.setNumeroTramite(ss.getParametros().get("tramiteQuipux") == null ? null : (String) ss.getParametros().get("tramiteQuipux"));
            // Asignamos el id del predio
            rest.setPredio(BigInteger.valueOf(predio.getId()));
            // Asignamos la restriccion
            rest.setRestricciones(new Restricciones(5));
            // Guardamos la nueva restriccion para este predio
            this.dataBaseIb.saveUpdateRestriccionPredio(rest);
        }
    }

    public void generarReporteDiferenciaExcedente(String tipo) {
        // AGREGAMOS LOS PARAMETROS QUE NESECITA EL REPORTE
        ss.agregarParametro("NUM_TRAMITE", ss.getParametros().get("tramiteQuipux").toString());
        ss.agregarParametro("TIPO", tipo);
        // Indicamos al reporte que no nesecitamos conexion a la base de datos
        ss.setTieneDatasource(false);
        // Nombre del reporte
        ss.setNombreReporte("diferenciaExcedente");
        //Indicamos la ruta donde esta el reporte
        ss.setNombreSubCarpeta("catastro/informesProcesos");
        // pasamos los datos para el reporte
        ss.setDataSource(Arrays.asList(this.predio));
        // Ejecutamos el reporte
        JsfUti.redirectFacesNewTab2(SisVars.urlbase + "Documento");
    }

    public void cambiarEstadoPredioTarea(Integer estado) {
        if (this.ss.getParametros() != null) {

            sess.setTaskID(ss.getParametro("taskId").toString());
            if (ss.getParametro("idTramite") != null) {
                TipoProceso proceso = TipoProceso.DIVIDIR_PREDIO;
                if (ss.getParametros().get("proceso") != null) {
                    proceso = (TipoProceso) ss.getParametro("proceso");

                }
                BigDecimal areaTemp = BigDecimal.ZERO;
                if (ss.tieneParametro("areaGrafica")) {
                    areaTemp = new BigDecimal(ss.getParametro("areaGrafica").toString());
                    if (this.predio.getTipoPredio().equalsIgnoreCase("U")) {
                        this.predio.setAreaSolar(areaTemp.setScale(2, RoundingMode.HALF_UP));
                        this.caracteristicas.setAreaGraficaLote(areaTemp.setScale(2, RoundingMode.HALF_UP));
                    } else {
                        this.caracteristicas.setAreaGraficaLote(areaTemp);
                        this.predio.setAreaSolar(areaTemp.divide(BigDecimal.valueOf(10000.00)).setScale(5, RoundingMode.HALF_UP));
                    }
                    this.em.persist(this.predio);
                    this.em.persist(this.caracteristicas);
                }

                Long idTramite = Long.valueOf(ss.getParametro("idTramite").toString());
                Long idPredio = Long.valueOf(ss.getParametro("idPredio").toString());
                String taskKey = ss.getParametro("taskName").toString();
                saveObservation(idTramite, idPredio, taskKey, observaciones, sess.getName_user());

                divisionServices.actualizarDetalleTramite(idTramite, predio, true, BigInteger.valueOf(estado), proceso);

            }
            JsfUti.messageInfo(null, "Info", "Tarea " + (estado == 1 ? "aprobada " : "rechazada ") + "con exito.");
            if (ss.getParametros().get("formRevision") != null) {
                JsfUti.redirectFaces(ss.getParametros().get("formRevision").toString());
            } else {
                JsfUti.redirectFaces("/faces/vistaprocesos/catastro/predios.xhtml");
            }
            ss.getParametros().remove("areaGrafica");
        }
    }

    public void buscarCiudadano(ActionEvent event) {
        try {
            CiuCiudadano ciudadanoEnte = null;
            switch (event.getComponent().getId()) {
                case "btnFiscalizadorResponsable":
                    if (predio.getCiuFiscalizador() == null) {
                        JsfUti.messageError(null, "Error", "debe Ingresar el numero de identificacion");
                        return;
                    }
                    ciudadanoEnte = (CiuCiudadano) em
                            .getNativeQueryPropietarios(Querys.getCiudadano(predio.getCiuFiscalizador()));
                    if (ciudadanoEnte == null) {
                        JsfUti.messageError(null, "Error", "No se encontro ciudadano");
                        return;
                    }
                    this.predio.setCiuFiscalizador(ciudadanoEnte.getCiuCedRuc());
                    this.predio.setCiuNombresFiscalizador(ciudadanoEnte.nombresCompletos());
                    JsfUti.update(":tdatos:frmResponsables:actFisc");
                    break;
                case "btnActualizadorResponsable":
                    if (predio.getCiuActualizador() == null) {
                        JsfUti.messageError(null, "Error", "debe Ingresar el numero de identificacion");
                        return;
                    }
                    ciudadanoEnte = (CiuCiudadano) em
                            .getNativeQueryPropietarios(Querys.getCiudadano(predio.getCiuActualizador()));
                    if (ciudadanoEnte == null) {
                        JsfUti.messageError(null, "Error", "No se encontro ciudadano");
                        return;
                    }
                    this.predio.setCiuActualizador(ciudadanoEnte.getCiuCedRuc());
                    this.predio.setCiuNombresActualizador(ciudadanoEnte.nombresCompletos());
                    JsfUti.update(":tdatos:frmResponsables:actPredial");
                    break;
                case "btnEnteHorizontal":
                    if (predio.getCiuHorizontal() == null) {
                        JsfUti.messageError(null, "Error", "debe Ingresar el numero de identificacion");
                        return;
                    }
                    ciudadanoEnte = (CiuCiudadano) em
                            .getNativeQueryPropietarios(Querys.getCiudadano(predio.getCiuHorizontal()));
                    if (ciudadanoEnte == null) {
                        JsfUti.messageError(null, "Error", "No se encontro ciudadano");
                        return;
                    }
                    this.predio.setCiuHorizontal(ciudadanoEnte.getCiuCedRuc());
                    this.predio.setCiuNombresHorizontal(ciudadanoEnte.nombresCompletos());
                    JsfUti.update("tdatos:frmVivienda:pngJefeHogar");
                    break;
                case "pngObsVn":
                    if (predio.getCiuInformante() == null) {
                        JsfUti.messageError(null, "Error", "debe Ingresar el numero de identificacion");
                        return;
                    }
                    ciudadanoEnte = (CiuCiudadano) em
                            .getNativeQueryPropietarios(Querys.getCiudadano(predio.getCiuInformante()));
                    if (ciudadanoEnte == null) {
                        JsfUti.messageError(null, "Error", "No se encontro ciudadano");
                        return;
                    }
                    predio.setCiuInformante(ciudadanoEnte.getCiuCedRuc());
                    predio.setCiuNombresInformante(ciudadanoEnte.nombresCompletos());
                    JsfUti.update("tdatos:frmViviendaCensal:pngObsVn");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public void aprobar() {
        this.cambiarEstadoPredioTarea(1);
    }

    public void rechazar() {
        this.cambiarEstadoPredioTarea(2);
    }

    public Boolean getEsTareaAp() {
        return esTareaAp;
    }

    public void setEsTareaAp(Boolean esTareaAp) {
        this.esTareaAp = esTareaAp;
    }

    public String getClaveCat() {
        return claveCat;
    }

    public void setClaveCat(String claveCat) {
        this.claveCat = claveCat;
    }

    public String getUpdateTableDocumento() {
        return updateTableDocumento;
    }

    public void setUpdateTableDocumento(String updateTableDocumento) {
        this.updateTableDocumento = updateTableDocumento;
    }

    public Short getCodProvincia() {
        return codProvincia;
    }

    public void setCodProvincia(Short codProvincia) {
        this.codProvincia = codProvincia;
    }

    public String getObservacionRestriccion() {
        return observacionRestriccion;
    }

    public void setObservacionRestriccion(String observacionRestriccion) {
        this.observacionRestriccion = observacionRestriccion;
    }

    public List<String> getObservacionRestricciones() {
        return observacionRestricciones;
    }

    public void setObservacionRestricciones(List<String> observacionRestricciones) {
        this.observacionRestricciones = observacionRestricciones;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public int getTipoTarea() {
        return tipoTarea;
    }

    public void setTipoTarea(int tipoTarea) {
        this.tipoTarea = tipoTarea;
    }

    public void guardarObservaciones() {
        switch (this.tipoTarea) {
            case 1: {
                rechazar();
                break;
            }
            case 2: {
                aprobar();
                break;
            }
            case 3: {
                completarEdicion();
                break;
            }
        }
    }

    private void saveObservation(Long idTramite, Long idPredio, String taskName, String obs, String user) {
        procesoServices.guardarObservaciones(idTramite, user, obs, taskName, idPredio);
    }

    public void showDialogObservaciones(int tipoTarea) {
        this.tipoTarea = tipoTarea;
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('obs').show();");
    }

    public String getUpdateScripture() {
        return ":tdatos:frmEscrituras";
    }

    public String getUpdateLinderos() {
        return "tdatos:frmLinderos";
    }

    public void setUpdateScripture(String valor) {

    }

    public void updateNombreTitulo() {
        System.out.println("Nombre del titulo: " + (predio.getNombreCambiado() != null ? predio.getNombreCambiado() : ""));
        if (!Objects.equals(predio.getCambioNombreTitulo(), Boolean.TRUE)) {
            predio.setNombreCambiado(null);
        }
    }

    public BigDecimal getValorMetro2() {
        return valorMetro2;
    }

    public void setValorMetro2(BigDecimal valorMetro2) {
        this.valorMetro2 = valorMetro2;
    }

    public Boolean getEditarClave() {
        Long verificador = 174l;
        if (Utils.isEmpty(sess.getRoles())) {
            return false;
        }
        if (editarClave) {
            if (sess.getRoles().contains(verificador)) {
                editarClave = false;
            }
        }
        return editarClave;
    }

    public void setEditarClave(Boolean editarClave) {
        this.editarClave = editarClave;
    }

    public BigDecimal getAreaDeclaradaConst() {
        if (predio.getAreaAumentoCons() == null && predio.getAreaDeclaradaConst() == null) {
            return BigDecimal.ZERO;
        }
        if (predio.getAreaAumentoCons() == null) {
            predio.setAreaAumentoCons(BigDecimal.ZERO);
        }
        if (predio.getAreaDeclaradaConst() == null) {
            predio.setAreaDeclaradaConst(BigDecimal.ZERO);
        }
        return predio.getAreaDeclaradaConst().subtract(predio.getAreaAumentoCons());
    }

    public List<CatPredioAvalHistorico> getAvaluosHistoricosPredio() {
        return avaluosHistoricosPredio;
    }

    public void setAvaluosHistoricosPredio(List<CatPredioAvalHistorico> avaluosHistoricosPredio) {
        this.avaluosHistoricosPredio = avaluosHistoricosPredio;
    }

    public Boolean getEsTarea() {
        return esTarea;
    }

    public void setEsTarea(Boolean esTarea) {
        this.esTarea = esTarea;
    }

    public GeodataService getGeodataService() {
        return geodataService;
    }

    public void setGeodataService(GeodataService geodataService) {
        this.geodataService = geodataService;
    }

    public Long getPredioLink() {
        return predioLink;
    }

    public void setPredioLink(Long predioLink) {
        this.predioLink = predioLink;
    }

    public List<CatEscritura> getEscriturasConsulta() {
        return escriturasConsulta;
    }

    public void setEscriturasConsulta(List<CatEscritura> escriturasConsulta) {
        this.escriturasConsulta = escriturasConsulta;
    }

    public String getControlResponsable() {
        return controlResponsable;
    }

    public void setControlResponsable(String controlResponsable) {
        this.controlResponsable = controlResponsable;
    }

    public CatCanton getCantonId() {
        return cantonId;
    }

    public void setCantonId(CatCanton cantonId) {
        this.cantonId = cantonId;
    }

    public Integer getTipoEliminar() {
        return tipoEliminar;
    }

    public void setTipoEliminar(Integer tipoEliminar) {
        this.tipoEliminar = tipoEliminar;
    }

    public String getTipoDocumentoAdj() {
        return tipoDocumentoAdj;
    }

    public void setTipoDocumentoAdj(String tipoDocumentoAdj) {
        this.tipoDocumentoAdj = tipoDocumentoAdj;
    }

    public List<CatPredioObraInterna> getObrasInternas() {
        return obrasInternas;
    }

    public void setObrasInternas(List<CatPredioObraInterna> obrasInternas) {
        this.obrasInternas = obrasInternas;
    }

    public List<CatPredioClasificRural> getClasificaciones() {
        return clasificaciones;
    }

    public void setClasificaciones(List<CatPredioClasificRural> clasificaciones) {
        this.clasificaciones = clasificaciones;
    }

    public List<CatPredioCultivo> getCultivos() {
        return cultivos;
    }

    public void setCultivos(List<CatPredioCultivo> cultivos) {
        this.cultivos = cultivos;
    }

    public BaseLazyDataModel<GeDocumentos> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(BaseLazyDataModel<GeDocumentos> documentos) {
        this.documentos = documentos;
    }

    public CatProvincia getProvincia() {
        return provincia;
    }

    public void setProvincia(CatProvincia provincia) {
        this.provincia = provincia;
    }

    public Event<GenerarHistoricoPredioPost> getGenerarHistoricoEvent() {
        return generarHistoricoEvent;
    }

    public void setGenerarHistoricoEvent(Event<GenerarHistoricoPredioPost> generarHistoricoEvent) {
        this.generarHistoricoEvent = generarHistoricoEvent;
    }

    public String getTaskKey() {
        return taskKey;
    }

    public void setTaskKey(String taskKey) {
        this.taskKey = taskKey;
    }

    public List<BloqueGeoData> getBloqueGis() {
        return bloqueGis;
    }

    public void setBloqueGis(List<BloqueGeoData> bloqueGis) {
        this.bloqueGis = bloqueGis;
    }

    public List<BloqueGeoData> getBloqueGisMod() {
        return bloqueGisMod;
    }

    public void setBloqueGisMod(List<BloqueGeoData> bloqueGisMod) {
        this.bloqueGisMod = bloqueGisMod;
    }

    public List<BloqueGeoData> getBloqueGisFinal() {
        return bloqueGisFinal;
    }

    public void setBloqueGisFinal(List<BloqueGeoData> bloqueGisFinal) {
        this.bloqueGisFinal = bloqueGisFinal;
    }

    public BloqueGeoData getGeoBloqueSeleccionado() {
        return geoBloqueSeleccionado;
    }

    public void setGeoBloqueSeleccionado(BloqueGeoData geoBloqueSeleccionado) {
        this.geoBloqueSeleccionado = geoBloqueSeleccionado;
    }

    public String getJsTarea() {
        return jsTarea;
    }

    public void setJsTarea(String jsTarea) {
        this.jsTarea = jsTarea;
    }

    public List<GeoVias> getGeoVias() {
        return geoVias;
    }

    public void setGeoVias(List<GeoVias> geoVias) {
        this.geoVias = geoVias;
    }

    private boolean verificarPorcentajePosession() {
        if (Utils.isNotEmpty(this.propietarios)) {
            for (CatPredioPropietario pp : this.propietarios) {
                if (pp.getTipo().getOrden() == 1) {
                    return pp.getPorcentajePosecion().doubleValue() == 0;
                }
            }
        }
        return false;
    }

    public BigDecimal getPorcentajePosession() {
        BigDecimal suma = BigDecimal.ZERO;
        if (Utils.isNotEmpty(this.propietarios)) {
            for (CatPredioPropietario pp : this.propietarios) {
                if (pp.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
                    suma = suma.add(pp.getPorcentajePosecion());
                }
            }
        }
        return suma;
    }
}
