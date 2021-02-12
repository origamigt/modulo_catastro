/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.app.AppConfig;
import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.catastroextras.entities.ibarra.RestricionPredio;
import com.origami.config.MainConfig;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.models.CatPredioModel;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.historic.Predio;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.events.EliminacionPredioPost;
import com.origami.sgm.geo.GeodataService;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.reportes.PdfReporte;
import com.origami.sgm.reportes.ReportesView;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.services.interfaces.catastro.FusionDivisionServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.engine.spi.SessionImplementor;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.SelectEvent;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author CarlosLoorVargas
 */
@Named
@ViewScoped
public class GestionPredios extends PredioUtil implements Serializable {

    private static final Logger LOG = Logger.getLogger(GestionPredios.class.getName());
    @Inject
    protected UserSession sess;
    @javax.inject.Inject
    protected CatastroServices catast;
    @Inject
    protected AppConfig appconfig;

    @Inject
    protected ServletSession ss;
    @Inject
    protected ReportesView reportes;
    protected PdfReporte reporte;
    protected int optFiltro;
    protected Boolean filtro, act = false;
    protected CatCiudadela ciudadela;
    protected List<CatCiudadela> ciudadelas;
    protected CatPredio predio;
    protected CatPredio p;
    protected Date fecha;
    protected Date fecha_hasta;
    protected CatPredioLazy predios;
    protected BaseLazyDataModel<Predio> historico;
    protected String nombres, apellidos, rsocial, ciruc;
    protected HashMap<String, Object> params;
    protected List<CatPredio> predSel;
    @javax.inject.Inject
    protected Entitymanager manager;
    protected Integer minimo, maximo;
    protected CatPredioModel predioModel = new CatPredioModel();
    protected BaseLazyDataModel<CatPredioPropietario> propietarios;
    protected CatPredioPropietario propietario;
    protected CatEnte contribuyenteConsulta;
    protected static final long serialVersionUID = 1L;
    protected CatParroquia parroquia;
    protected Integer anio = Utils.getAnio(new Date());
    protected Integer tipoReporte;
    protected List<AclUser> usuarios;
    protected AclUser user;
    private Boolean soloActivos = false;
    protected CiuCiudadano responsable;

    protected Long canton;
    //VARIABLES DIVISIO DE PREDIOS
    protected ArrayList<CatPredio> predioDivision, prediosResultantes;
    protected BigDecimal areaSolar = null;
    protected Integer solar, unidad;

    @Inject
    protected OmegaUploader omegaUploader;
    @javax.inject.Inject
    protected GeodataService geodataService;
    @javax.inject.Inject
    protected FusionDivisionServices fusionDivisionEjb;
    @Inject
    protected ServiceDataBaseIb dataBaseIb;

    protected boolean accionDivisionFusion;
    protected MainConfig config;
    protected String tipoPredio = null;

    protected String messageConfirm;
    protected Long numPrediosActivos, numPredios, numPrediosInactivos, numPrediosHistorico, numPrediosPrivados, numPrediosPublicos;
    protected BigDecimal avaluosTerrenos, avaluosConstruccion, avaluosPropiedad;
    protected Long conteoPrediosUrbanos, conteoPrediosRurales;

    @Inject
    protected Event<EliminacionPredioPost> eventEliminarPredio;

    @PostConstruct
    protected void load() {
        if (!JsfUti.isAjaxRequest()) {
            messageConfirm = " ¿ Esta seguro que desea eliminar el predio seleccionado ? ";
            try {
                if (sess != null) {
                    solar = 0;
                    unidad = 0;
                    params = new HashMap<>();
                    optFiltro = 4;
                    filtro = true;
                    ss.instanciarParametros();
                    reporte = new PdfReporte();
                    p = new CatPredio();
                    usuarios = catast.getUserActivos();
                    fecha = new Date();
                    fecha_hasta = new Date();
                    predioDivision = new ArrayList<>();
                    prediosResultantes = new ArrayList<>();
                    accionDivisionFusion = true;
                    config = new MainConfig();
                    predios = new CatPredioLazy();
                    predios.setTipoPredio(tipoPredio);
                    // 9 ADMIN
                    // 210 analista_catastro
                    // 211 responsable
                    //218 listar_predios_historicos
                    if (this.sess.getRoles().contains(9L)
                            || this.sess.getRoles().contains(210L)
                            || this.sess.getRoles().contains(211L)
                            || this.sess.getRoles().contains(218L)) {
                    } else {
                        predios.setColunmEstado("estado");
                        predios.setValueEstados(EstadosPredio.ACTIVOS_TEMPORALES);
                        predios.setSoloActivos(true);
                        soloActivos = true;
                    }
                    parroquia = new CatParroquia();
                    getTotalesPredios();
                }
            } catch (Exception e) {
                Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    public void getTotalesPredios() {
        numPredios = (Long) manager.find(Querys.numPredios);
        numPrediosActivos = (Long) manager.find(Querys.numPrediosActivos);
        numPrediosInactivos = (Long) manager.find(Querys.numPrediosInactivos);
        numPrediosHistorico = (Long) manager.find(Querys.numPrediosHistorico);
        avaluosTerrenos = (BigDecimal) manager.find(Querys.getTotalesAvaluosTerrenos);
        avaluosConstruccion = (BigDecimal) manager.find(Querys.getTotalesAvaluosConstruccion);
        conteoPrediosUrbanos = (Long) manager.find(Querys.getTotalesPrediosUrbanosPorTipoPredio, new String[]{"tipoPredio"}, new Object[]{"U"});
        conteoPrediosRurales = (Long) manager.find(Querys.getTotalesPrediosUrbanosPorTipoPredio, new String[]{"tipoPredio"}, new Object[]{"R"});

        avaluosPropiedad = (BigDecimal) manager.find(Querys.getTotalesAvaluosPropiedad);
        numPrediosPrivados = (Long) manager.find(Querys.getTotalesPrediosPrivados, new String[]{"nombre"}, new Object[]{"PRIVADO" + "%"});
        numPrediosPublicos = (Long) manager.find(Querys.getTotalesPrediosPublicos, new String[]{"nombre"}, new Object[]{"PUBLICO" + "%"});
    }

    public void filtrar() {
        filtro = optFiltro == 1;
        predios.setModel(null);
        predios.setPropietarios(null);
    }

    public void predioCiudadeleas() {
        if (ciudadela != null) {
            try {
                ss.instanciarParametros();
                ss.agregarParametro("id_ciudadela", ciudadela.getId());
                ss.agregarParametro("SUBREPORT_DIR", Faces.getRealPath("/") + "reportes/catastro/");
                ss.setTieneDatasource(Boolean.TRUE);
                ss.setNombreReporte("/catastro/predios_por_ciudadela");
                Faces.redirectNewTab(com.origami.config.SisVars.urlbase + "Documento");
            } catch (Exception ex) {
                Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void imprimirCatastroPredial1() throws IOException {
        ss.instanciarParametros();
        ss.agregarParametro("codigoMz", predioModel.getCodigoPredial());
        JsfUti.redirectNewTab(SisVars.urlbase + "FichaDownload");
    }

    public void imprimirCatastroPredial() throws IOException {
        List<CatPredio> getListPredios = null;
        if (predioModel.getCodigoPredial() != null) {
            if (predioModel.getCodigoPredial().length() > 0) {
                getListPredios = manager.findAllEntCopy(String.format(Querys.getFichaCatastralMz, ("'%" + predioModel.getCodigoPredial() + "%'")));
            } else {
                getListPredios = manager.findAllEntCopy(Querys.getFichaCatastral);
            }
        } else {
            getListPredios = manager.findAllEntCopy(Querys.getFichaCatastral);
        }
        HttpServletResponse response = reportes.getResponse();
        int x = 1;
        CatPredio x1 = getListPredios.get(0);
        String rutaRepo = "/servers_files/Fichas/SECTOR " + x1.getSector().toString() + "/" + (x1.getSector().toString() + "_MZ_" + Utils.completarCadenaConCeros(x1.getMz() + "", 3));
        String slash = "/";
        Path path = Paths.get(rutaRepo.endsWith("/") ? rutaRepo.concat("/") : rutaRepo);
        Files.createDirectories(path);
        for (CatPredio pre : getListPredios) {
            System.out.println("Generando ficha : " + Utils.completarCadenaConCeros(pre.getSolar() + "", 3));
            File ruta = new File(rutaRepo);
            if (!ruta.exists()) {
                if (ruta.mkdir()) {
                    System.out.println("Creado Directorio Correctamente " + ruta.getAbsolutePath());
                }
            }
            imprimirFicha(pre, response, ruta.toString() + slash);
        }
    }

    public void imprimirFicha(CatPredio p, HttpServletResponse response, String ruta) {
        try {
            if (p != null) {
                try {
                    if (p != null) {
                        ss.instanciarParametros();
                        String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                        int numFotos = 1;
                        List<FotoPredio> fotos = null;
                        if (p.getPredioRaiz() == null) {
                            fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{p.getId()});
                        } else {
                            fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{p.getPredioRaiz().longValue()});
                        }
                        if (Utils.isNotEmpty(fotos)) {
                            for (FotoPredio foto : fotos) {
                                switch (numFotos) {
                                    case 1:
                                        ss.agregarParametro("FachadaFrontal", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                    case 2:
                                        ss.agregarParametro("FachadaIzquierda", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                    case 3:
                                        ss.agregarParametro("FachadaDerecha", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                    case 4:
                                        ss.agregarParametro("FachadaPosterior", omegaUploader.streamFile(foto.getFileOid()));
                                        break;
                                }
                                numFotos++;
                            }
                        }

                        ss.agregarParametro("IMAGEN_PREDIO", appconfig.formatUrlPredio(p.getClaveCat(), p.getPropiedadHorizontal()));
                        ss.agregarParametro("COLINDANTES", appconfig.formatUrlPredioColindates(p.getClaveCat()));
                        ss.agregarParametro("MUNICIPIO", SisVars.NOMBREMUNICIPIO);
                        ss.agregarParametro("predio", p.getId());
                        ss.agregarParametro("LOGO", JsfUti.getRealPath(SisVars.sisLogo));
                        ss.agregarParametro("LOGO2", JsfUti.getRealPath(SisVars.sisLogo1));
                        ss.setNombreReporte("Ficha predial " + p.getNumPredio());
                        ss.agregarParametro("SUBREPORT_DIR", JsfUti.getRealPath("reportes/catastro/Ibarra/"));
                        ss.agregarParametro("SUBREPORT_DIR_TITLE", JsfUti.getRealPath("reportes/"));
                        ss.setAgregarReporte(true);
                        // Parametros para agregar un reporte al final del primero
                        Map<String, Object> reporteadd = new HashMap<>();
                        ss.setAgregarReporte(Boolean.FALSE);
                        Collection<CatPredioEdificacion> bloques = this.catas.getEdificaciones(p);
                        if (bloques != null && Utils.isNotEmpty(bloques)) {
                            if (ss.getReportes() != null) {
                                ss.getReportes().clear();
                            }
                            int count = 0;
                            String edificaciones = "";
                            ss.setAgregarReporte(Boolean.TRUE);
                            for (CatPredioEdificacion edif1 : bloques) {
                                count++;
                                edificaciones += edif1.getId().toString() + ",";
                                if ((count % 4) == 0 || (bloques.size() - count) == 0) {
                                    edificaciones = edificaciones.substring(0, edificaciones.length() - 1);
                                    reporteadd = new HashMap<>();
                                    reporteadd.put("nombreSubCarpeta", "catastro/Ibarra/");
                                    reporteadd.put("nombreReporte", "fichaMiduviBloques");
                                    reporteadd.put("predio", p.getId());
                                    reporteadd.put("edificaciones", edificaciones);
                                    ss.addParametrosReportes(reporteadd);
                                    edificaciones = "";
                                }
                            }
                        }
                        ss.setNombreSubCarpeta("catastro/Ibarra");
                        ss.setNombreReporte("fichaMiduvi");
                        ss.setNombreDocumento("MZ_" + Utils.completarCadenaConCeros(p.getMz() + "", 3) + "_" + Utils.completarCadenaConCeros(p.getSolar() + "", 3));
                        ss.setTieneDatasource(Boolean.TRUE);
                        ss.setReportePDF(reporte.generarPdf(JsfUti.getRealPath("/reportes/catastro/Ibarra/fichaMiduvi.jasper"), ss.getParametros(), ss));
                    }
                } catch (Exception e) {
                    Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
                }
                System.out.println("Ruta " + ruta);
                reportes.downloadPDFarregloBytesConsecutive(ss.getReportePDF(), response, ruta);
            }
        } catch (IOException e) {
            Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void imprimirFicha(Boolean controlCroquis) {
        try {
            if (predio != null) {
                ss.instanciarParametros();
                String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                int numFotos = 1;
                List<FotoPredio> fotos = null;
                if (predio.getPredioRaiz() == null) {
                    fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getId()});
                } else {
                    fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getPredioRaiz().longValue()});
                }
                if (Utils.isNotEmpty(fotos)) {
                    for (FotoPredio foto : fotos) {
                        switch (numFotos) {
                            case 1:
                                ss.agregarParametro("FachadaFrontal", omegaUploader.streamFile(foto.getFileOid()));
                                break;
                            case 2:
                                ss.agregarParametro("FachadaIzquierda", omegaUploader.streamFile(foto.getFileOid()));
                                break;
                            case 3:
                                ss.agregarParametro("FachadaDerecha", omegaUploader.streamFile(foto.getFileOid()));
                                break;
                            case 4:
                                ss.agregarParametro("FachadaPosterior", omegaUploader.streamFile(foto.getFileOid()));
                                break;
                        }
                        numFotos++;
                    }
                }

                ss.agregarParametro("IMAGEN_PREDIO", appconfig.formatUrlPredio(predio.getClaveCat(), predio.getPropiedadHorizontal()));
                ss.agregarParametro("COLINDANTES", appconfig.formatUrlPredioColindates(predio.getClaveCat()));
                ss.agregarParametro("MUNICIPIO", SisVars.NOMBREMUNICIPIO);
                ss.agregarParametro("predio", predio.getId());
                ss.agregarParametro("LOGO", SisVars.sisLogo);
                ss.agregarParametro("LOGO2", SisVars.sisLogo1);
                ss.setNombreReporte("Ficha predial " + predio.getNumPredio());
                ss.agregarParametro("SUBREPORT_DIR", path + "reportes/catastro/Ibarra/");
                ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes/");
                // Parametros para agregar un reporte al final del primero
                Map<String, Object> reporteadd = new HashMap<>();
                ss.setAgregarReporte(Boolean.FALSE);
                if (predio.getCatPredioEdificacionCollection() != null && !Utils.isEmpty(predio.getCatPredioEdificacionCollection())) {
                    int count = 0;
                    String edificaciones = "";
                    ss.setAgregarReporte(Boolean.TRUE);
                    for (CatPredioEdificacion edif : predio.getCatPredioEdificacionCollection()) {
                        count++;
                        edificaciones += edif.getId().toString() + ",";
                        if ((count % 4) == 0 || (predio.getCatPredioEdificacionCollection().size() - count) == 0) {
                            edificaciones = edificaciones.substring(0, edificaciones.length() - 1);
                            reporteadd = new HashMap<>();
                            reporteadd.put("nombreSubCarpeta", "/catastro/Ibarra");
                            reporteadd.put("nombreReporte", "fichaMiduviBloques");
                            reporteadd.put("predio", predio.getId());
                            reporteadd.put("edificaciones", edificaciones);
                            ss.addParametrosReportes(reporteadd);
                            edificaciones = "";
                        }
                    }
                }
                ss.setNombreSubCarpeta("catastro/Ibarra");
                ss.setNombreReporte("fichaMiduvi");
                ss.setNombreDocumento("fichaPredial-" + predio.getId() + "-(" + new Date() + ")");
                ss.setTieneDatasource(Boolean.TRUE);

                JsfUti.redirectNewTab(SisVars.urlbase + "Documento");
            }
        } catch (Exception e) {
            Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void fichaReducida() {
        try {
            // INICIAMOS EL MAPA DE PARAMETROS DEL SERVLET SESSION
            ss.instanciarParametros();
            // Obtenemos la imagen del predio
            ss.agregarParametro("IMAGEN_PREDIO", appconfig.formatUrlPredio(predio.getClaveCat(), predio.getPropiedadHorizontal()));
            // Llenamos los parametros que nesecita el reporte
            ss.agregarParametro("SUBREPORT_DIR", JsfUti.getRealPath("/reportes/catastro/Ibarra/"));
            ss.agregarParametro("SUBREPORT_DIR_TITLE", JsfUti.getRealPath("/reportes/"));
            ss.agregarParametro("LOGO", SisVars.sisLogo);
            ss.agregarParametro("LOGO2", SisVars.sisLogo1);
            ss.agregarParametro("USUARIO", sess.getName_user());
            ss.agregarParametro("NOMBRES_USUARIOS", sess.getNombrePersonaLogeada());
            // Pasamos la conexion para el la cabecera del reporte
            ss.agregarParametro("REPORT_CONNECTION", ((SessionImplementor) manager.getSession()).getJdbcConnectionAccess().obtainConnection());
            // Pasamos la carpeta donde esta el reporte
            ss.setNombreSubCarpeta("catastro/Ibarra");
            // Indicamos al reporte que no nesecitamos conexion a la base de datos
            ss.setTieneDatasource(false);
            // Nombre del reporte
            ss.setNombreReporte("fichaReducida");
            // Nombre del archivo
            ss.setNombreDocumento("fichaReducida");
            // Pasamos el modelo de datos del predio
            ss.setDataSource(Arrays.asList(predio));

            // EJECUTAMOS EL SERVLET QUE MUESTRA EL REPORTE
            JsfUti.redirectNewTab(SisVars.urlbase + "Documento");
        } catch (SQLException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void generarReportePrediosByUsos(Boolean excel) throws IOException, SQLException {
        if (p != null) {
            ss.borrarDatos();
            ss.instanciarParametros();
            ss.setTieneDatasource(Boolean.TRUE);
            ss.setNombreSubCarpeta("catastro//");
            String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
            List<Short> codParroquiaList = new ArrayList<>();
            if (parroquia == null) {
                for (CatParroquia codParroquia : this.getParroquias()) {
                    codParroquiaList.add(codParroquia.getCodigoParroquia().shortValue());
                }
                ss.agregarParametro("PARROQUIACOLL", codParroquiaList);
            } else {
                if (parroquia.getCodigoParroquia() == null) {
                    codParroquiaList.add(parroquia.getCodNac());
                    ss.agregarParametro("PARROQUIACOLL", codParroquiaList);
                } else {
                    codParroquiaList.add(parroquia.getCodigoParroquia().shortValue());
                    ss.agregarParametro("PARROQUIACOLL", codParroquiaList);
                }
            }

            if (p != null) {
                List<Short> zonaList = new ArrayList<>();
                List<Short> mzList = new ArrayList<>();
                List<Short> sectorList = new ArrayList<>();
                if (p.getZona() != null) {
                    zonaList.clear();
                    if (p.getZona() == 0) {
                        zonaList = manager.findAll(Querys.getZonas);
                    } else {
                        zonaList.add(p.getZona());
                        ss.agregarParametro("ZONACOLL", zonaList);
                    }
                } else {
                    Faces.messageWarning(null, "Advertencia!", "Debe Ingresar el valor de la Zona");
                }
                if (p.getSector() != null) {
                    sectorList.clear();
                    if (p.getSector() == 0) {
                        sectorList = manager.findAll(Querys.getSector);
                    } else {

                        sectorList.add(p.getSector());
                        ss.agregarParametro("SECTORCOLL", sectorList);
                    }

                } else {
                    Faces.messageWarning(null, "Advertencia!", "Debe Ingresar el valor del Sector");
                }
                if (p.getMz() != null) {
                    mzList.clear();
                    if (p.getMz() == 0) {
                        mzList = manager.findAll(Querys.getMz);
                        ss.agregarParametro("MZLIST", mzList);
                    } else {
                        mzList.add(p.getMz());
                        ss.agregarParametro("MZLIST", mzList);
                    }
                } else {
                    Faces.messageWarning(null, "Advertencia!", "Debe Ingresar el valor de la Manzana");
                }

            }
            ss.agregarParametro("LOGO", path + SisVars.logoReportes);
            ss.agregarParametro("LOGO2", path + SisVars.sisLogo1);
            ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes/");
            ss.setNombreReporte("informePrediosByUsos");
//            ss.setReportePDF(reporte.generarPdf("/reportes/catastro/San Vicente/informePredios.jasper", ss.getParametros()));

            //reportes.descargarPDFarregloBytes(ss.getReportePDF());
            if (excel) {
                Faces.redirectNewTab(com.origami.config.SisVars.urlbase + "DocumentoExcel");

            } else {
                JsfUti.redirectNewTab(com.origami.config.SisVars.urlbase + "Documento");

            }
//            JsfUti.redirectNewTab("/sgmEE/Documento");
        }
    }

    public void generarReportePredios(Boolean excel) throws IOException, SQLException {
        if (p != null) {
            ss.borrarDatos();
            ss.instanciarParametros();
            ss.setTieneDatasource(Boolean.TRUE);
            ss.setNombreSubCarpeta("catastro/San Vicente/");
            String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
            List<Short> codParroquiaList = new ArrayList<>();
            if (parroquia == null) {
                for (CatParroquia codParroquia : this.getParroquias()) {
                    codParroquiaList.add(codParroquia.getCodigoParroquia().shortValue());
                }
                ss.agregarParametro("PARROQUIACOLL", codParroquiaList);
            } else {
                if (parroquia.getCodigoParroquia() == null) {
                    codParroquiaList.add(parroquia.getCodNac());
                    ss.agregarParametro("PARROQUIACOLL", codParroquiaList);
                } else {
                    codParroquiaList.add(parroquia.getCodigoParroquia().shortValue());
                    ss.agregarParametro("PARROQUIACOLL", codParroquiaList);
                }
            }

            if (p != null) {
                List<Short> zonaList = new ArrayList<>();
                List<Short> mzList = new ArrayList<>();
                List<Short> sectorList = new ArrayList<>();
                if (p.getZona() != null) {
                    zonaList.clear();
                    if (p.getZona() == 0) {
                        zonaList = manager.findAll(Querys.getZonas);
                    } else {
                        zonaList.add(p.getZona());
                        ss.agregarParametro("ZONACOLL", zonaList);
                    }
                } else {
                    Faces.messageWarning(null, "Advertencia!", "Debe Ingresar el valor de la Zona");
                }
                if (p.getSector() != null) {
                    sectorList.clear();
                    if (p.getSector() == 0) {
                        sectorList = manager.findAll(Querys.getSector);
                    } else {

                        sectorList.add(p.getSector());
                        ss.agregarParametro("SECTORCOLL", sectorList);
                    }

                } else {
                    Faces.messageWarning(null, "Advertencia!", "Debe Ingresar el valor del Sector");
                }
                if (p.getMz() != null) {
                    System.out.println("getMz" + p.getMz());
                    mzList.clear();
                    if (p.getMz() == 0) {
                        mzList = manager.findAll(Querys.getMz);
                        ss.agregarParametro("MZLIST", mzList);
                    } else {
                        mzList.add(p.getMz());
                        ss.agregarParametro("MZLIST", mzList);
                    }
                } else {
                    Faces.messageWarning(null, "Advertencia!", "Debe Ingresar el valor de la Manzana");
                }

            }
            ss.agregarParametro("LOGO", path + SisVars.logoReportes);
            ss.agregarParametro("LOGO2", path + SisVars.sisLogo1);
            ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes/");
            ss.setNombreReporte("informePredios");
//            ss.setReportePDF(reporte.generarPdf("/reportes/catastro/San Vicente/informePredios.jasper", ss.getParametros()));

            //reportes.descargarPDFarregloBytes(ss.getReportePDF());
            if (excel) {
                Faces.redirectNewTab(com.origami.config.SisVars.urlbase + "DocumentoExcel");

            } else {
                JsfUti.redirectNewTab(com.origami.config.SisVars.urlbase + "Documento");

            }
//            JsfUti.redirectNewTab("/sgmEE/Documento");
        }
    }

    public void reporteModificacionesxUsuario() {

        try {
            if (p != null) {
                ss.borrarDatos();
                ss.instanciarParametros();
                String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                if (user == null) {
                    ss.agregarParametro("USUARIO", 0);
                } else {
                    ss.agregarParametro("USUARIO", Integer.parseInt(user.getId().toString()));
                }
                ss.setNombreReporte("informePrediosxUsuario");
                ss.agregarParametro("DESDE", fecha);
                ss.agregarParametro("HASTA", fecha_hasta);

                ss.agregarParametro("LOGO", Faces.getRealPath(SisVars.logoReportes));
                ss.agregarParametro("LOGO2", Faces.getRealPath(SisVars.sisLogo1));
                ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes/");
                ss.agregarParametro("SUBREPORT_DIR", path + "reportes/");
                reporte.setServletSession(ss);
                ss.setReportePDF(reporte.generarPdf("/reportes/catastro/Ibarra/informePrediosxUsuario.jasper", ss.getParametros(), ss));
                reportes.descargarPDFarregloBytes(ss.getReportePDF());

            }
        } catch (IOException | NumberFormatException | SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public void reporteModificacionesxFecha() {
        try {

            if (p != null) {

                ss.instanciarParametros();
                String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                if (user == null) {
                    ss.agregarParametro("USUARIO", 0);
                } else {
                    ss.agregarParametro("USUARIO", Integer.parseInt(user.getId().toString()));
                }
                ss.agregarParametro("DESDE", fecha);
                ss.agregarParametro("HASTA", fecha_hasta);
                ss.agregarParametro("LOGO", Faces.getRealPath(SisVars.sisLogo));
                ss.agregarParametro("LOGO2", Faces.getRealPath(SisVars.sisLogo1));
                ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes/");
                ss.setNombreReporte("informeModificacionesxFecha");
                ss.setAgregarReporte(Boolean.FALSE);
                reporte.setServletSession(ss);
                ss.setReportePDF(reporte.generarPdf("/reportes/catastro/Ibarra/informePrediosMods.jasper", ss.getParametros(), ss));
                reportes.descargarPDFarregloBytes(ss.getReportePDF());
            }
        } catch (IOException | SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public String nombresPropietarios(CatPredio pt) {
        if (pt == null) {
            return null;
        }
        String nombres = "";
        StringBuilder sb = new StringBuilder();
        int num = 0;
        if (pt.getCatPredioPropietarioCollection() != null && !pt.getCatPredioPropietarioCollection().isEmpty()) {
            for (CatPredioPropietario cpp : pt.getCatPredioPropietarioCollection()) {
                num++;
                if (cpp.getNombresCompletos() != null) {
                    nombres = cpp.getNombresCompletos();
                    sb.append(nombres).append(" - ");
                } else {
                    sb.append(nombres).append(" - ");
                }
            }
        }
        if (sb.length() >= 3) {
            sb.delete(sb.length() - 3, sb.length() - 1);
        }
        return sb.toString().toUpperCase();
    }

    public void imprimirReporte() {
        switch (this.tipoReporte) {
            case 1:
                this.reportePredioUsuario();
                break;
            case 2:
                this.reporteModificacionesxUsuario();
                break;
            case 3:
                this.reporteModificacionesxFecha();
                break;
            case 4:
                this.reporteModificacionesxFecha();
                break;
        }
    }

    public void reporteCatastro() {
        try {
            if (p != null) {

                ss.instanciarParametros();
                String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                if (parroquia == null) {
                    ss.agregarParametro("PARROQUIA", null);
                } else {
                    ss.agregarParametro("PARROQUIA", parroquia.getCodigoParroquia().shortValue());
                }
                ss.agregarParametro("SECTOR", p.getSector());
                ss.agregarParametro("MZ", p.getMz());
                ss.agregarParametro("ANIO", anio);
                ss.agregarParametro("LOGO", Faces.getRealPath(SisVars.sisLogo));
                ss.agregarParametro("LOGO2", Faces.getRealPath(SisVars.sisLogo1));
                ss.setNombreReporte("ReporteCatastro" + "");
                ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes//");
                ss.setReportePDF(reporte.generarPdf("/reportes/catastro/San Miguel/informeCatastroPrediosUrb.jasper", ss.getParametros(), ss));
                reportes.descargarPDFarregloBytes(ss.getReportePDF());

            }
        } catch (SQLException | IOException e) {
            Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void reportePredioUsuario() {
        if (tipoReporte == null) {
            JsfUti.messageError(null, "Error", "Debe seleccionar el tipo de reporte a generar.");
            return;
        }
        switch (tipoReporte) {
            case 1:
                try {
                if (p != null) {
                    ss.borrarDatos();
                    ss.instanciarParametros();
                    ss.setNombreSubCarpeta("catastro//Ibarra");
                    String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                    if (user == null) {
                        ss.agregarParametro("USUARIO", 0);
                    } else {
                        ss.agregarParametro("USUARIO", Integer.parseInt(user.getId().toString()));
                    }
                    ss.agregarParametro("DESDE", fecha);
                    ss.agregarParametro("LOGO", Faces.getRealPath(SisVars.sisLogo));
                    ss.agregarParametro("LOGO2", Faces.getRealPath(SisVars.sisLogo1));
                    ss.setNombreReporte("informePrediosporUsuario");
                    ss.setAgregarReporte(Boolean.FALSE);
                    ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes//");
                    ss.setTieneDatasource(true);
                    Faces.redirectNewTab(com.origami.config.SisVars.urlbase + "DocumentoExcel");
                }
            } catch (NumberFormatException e) {
                LOG.log(Level.SEVERE, null, e);
            }
            break;
            default:
                this.imprimirReporte(tipoReporte);
                break;
        }

    }

    public SelectItem[] getLisUrbanizaciones() {
        int cantRegis = ciudadelas.size();
        SelectItem[] options = new SelectItem[cantRegis + 1];
        options[0] = new SelectItem("", "Seleccione");
        for (int i = 0; i < cantRegis; i++) {
            options[i + 1] = new SelectItem(ciudadelas.get(i).getNombre(), ciudadelas.get(i).getNombre());
        }
        return options;
    }

    public void descargarDocumento(String url) {
        try {
            if (url != null && url.trim().length() > 0) {
                JsfUti.redirectNewTab(SisVars.urlbase + "DescargarDocsRepositorio?idDoc=" + url + "&type=pdf");
            } else {
                Faces.messageWarning(null, "No Existen Documentos para el Predio Seleccionado", "");
            }
        } catch (Exception e) {
            Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void ver(boolean edt) {
        if (predio != null) {
            ss.agregarParametro("numPredio", predio.getNumPredio());
            ss.agregarParametro("idPredio", predio.getId());
            ss.agregarParametro("edit", edt);
            if (edt) {
                if (!this.predioActivo() && !this.sess.getIsAdmin()) {
                    return;
                }
            }
            predioModel = new CatPredioModel();
            predios.setModel(predioModel);
            Faces.redirectFacesNewTab("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }
    }

    public void fusionar() {
        if (predio != null) {
            if (!this.predioActivo()) {
                return;
            }
            if (this.poseeDeudas(predio)) {
                Faces.messageWarning(null, "Advertencia!", "El predio seleccionado posee deudas");
            } else {
                ss.agregarParametro("numPredio", predio.getNumPredio());
                ss.agregarParametro("idPredio", predio.getId());
                Faces.redirectFaces("/faces/vistaprocesos/catastro/fusionarPredios.xhtml");
            }

        }
    }

    public void transferenciaDominio() {
        if (predio != null) {
            if (!this.predioActivo()) {
                return;
            }
            if (this.poseeDeudas(predio)) {
                Faces.messageWarning(null, "Advertencia!", "El predio seleccionado posee deudas");
            } else {
                ss.agregarParametro("numPredio", predio.getNumPredio());
                ss.agregarParametro("idPredio", predio.getId());
                ss.agregarParametro("imprimir", true);
                Faces.redirectFaces("/faces/vistaprocesos/catastro/transferenciaDominio/transferenciaDominio.xhtml");
            }

        }
    }

    public void openDialogDividePredio() {
        try {
            if (this.predio == null) {
                JsfUti.messageError(null, "Info.", "Debe seleccionar el predio a dividir.");
                return;
            }
            if (!this.predioActivo()) {
                return;
            }
//            try {
//                GeoProcesoDivision diviones = geoService.getProcesoDivisionData(predio.getClaveCat());
//            } catch (Exception e) {
//                JsfUti.messageError(null, "Info.", e.getMessage());
//                LOG.log(Level.SEVERE, "Dialog Division Predios", e);
//                Faces.redirectFaces("/faces/vistaprocesos/catastro/predios.xhtml");
//                return;
//            }

//            Lanza el evento com el predio pra proder recibirlo en el dialogo
//            event.fire(new FichaPredialOnLoadEvent(predio));
            Utils.openDialog("/vistaprocesos/catastro/bpm/division/iniciarDivisionPredio", null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Dialog Division Predios", e);
        }
    }

    public void openDialogBPMActualizarConstrucciones() {
        try {
            if (this.predio == null) {
                JsfUti.messageError(null, "Info.", "Debe seleccionar el predio a actualizar construcciones.");
                return;
            }
            if (!this.predioActivo()) {
                return;
            }

//            Lanza el evento com el predio pra proder recibirlo en el dialogo
//            event.fire(new FichaPredialOnLoadEvent(predio));
            Utils.openDialog("/vistaprocesos/catastro/bpm/actualizarConstrucciones/iniciarTramite", null);
        } catch (Exception ex) {

        }
    }

    public void verHistorial() {
        if (predio != null) {
            historico = new BaseLazyDataModel<Predio>(Predio.class, new String[]{"predio"}, new Object[]{predio.getNumPredio().longValue()});
        }
    }

    public void verFichaHistorico(Predio px) {
        if (px != null) {
            Faces.redirectNewTab(SisVars.urlbase + "/faces/vistaprocesos/catastro/historicoPredio.xhtml?val=" + px.getId());
        } else {
            Faces.messageWarning(null, "Advertencia", "Debe seleccionar un predio del historico");
        }
    }

    public Boolean seleccionarContribuyente() {
        if (this.propietario != null) {
            if (this.propietario.getEstado().equalsIgnoreCase("A")) {
                this.setContribuyenteConsulta(this.propietario.getEnte());
                this.getPredioModel().setNumPredio(this.propietario.getPredio().getNumPredio());
                return true;
            } else {
                Faces.messageWarning(null, "Advertencia!", "El propietario referenciado se encuentra inactivo");
                return false;
            }
        }
        return false;
    }

    public void consultar() {
        try {
//            predios.setSoloActivos(soloActivos);
            switch (optFiltro) {
                case 1:
                    if (predioModel.getNumPredio() != null) {
                        predios.setModel(predioModel);
                    } else {
                        Faces.messageWarning(null, "Advertenia!", "La matricula inmobiliaria del predio es obligatoria");
                    }
                    break;
                case 2:
                    if (this.seleccionarContribuyente()) {
                        predios.setModel(predioModel);
                    } else {
                        Faces.messageWarning(null, "Advertenia!", "El contribuyente seleccionado no cuenta con predios registrados, o se encuentra inactivo");
                    }
                    break;
                case 6:
                    predios.setEsPropiedadHorizontal(true);
                default:
                    predios.setModel(predioModel);
            }
            predioModel = new CatPredioModel();

        } catch (Exception e) {
            Logger.getLogger(GestionPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void limpiarVariablesConsulta() {
        predioModel = new CatPredioModel();
    }

    public void buscarRes() {
        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("width", "75%");
        options.put("closable", true);
        options.put("contentWidth", "100%");
        RequestContext.getCurrentInstance().openDialog("/resources/dialog/listadoPropietarios", options, null);
    }

    public void consultarPredios() {
        ss.instanciarParametros();
        if (parroquia == null) {
            ss.agregarParametro("PARROQUIA", null);
            ss.setNombreDocumento("Todas las Parroquia ");
        } else {
            ss.agregarParametro("PARROQUIA", parroquia.getCodigoParroquia());
            ss.setNombreDocumento("Parroquia " + parroquia.getDescripcion());
        }
        ss.agregarParametro("LOGO", Faces.getRealPath(SisVars.sisLogo));
        ss.agregarParametro("LOGO2", Faces.getRealPath(SisVars.sisLogo1));
        ss.setNombreReporte("reportesPrediosUrbanizacion");
        String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
        ss.agregarParametro("SUBREPORT_DIR", path + "reportes//");
        ss.setNombreSubCarpeta("catastro");
        ss.setTieneDatasource(true);
        JsfUti.redirectNewTab(SisVars.urlbase + "Documento");
    }

    public List<CatParroquia> getParroquias() {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("idCanton", manager.find(Querys.getParroquiasByCanton, new String[]{"codigoNacional", "codNac"}, new Object[]{SisVars.CANTON, SisVars.PROVINCIA}));
        return manager.findObjectByParameterOrderList(CatParroquia.class, paramt, new String[]{"idCanton"}, true);
//        return serv.findAllEntCopy(CatParroquia.class) ;
    }

    public void addDivisionPredio() {

        if (predio != null) {
            if (predio.getPropiedadHorizontal() != null && predio.getPropiedadHorizontal() == Boolean.FALSE) {
                if (solar == 0) {
                    this.solar = catast.getSolarMaxPredio(predio);
                }
            } else {
                if (unidad == 0) {
                    this.unidad = catast.getUnidadMaxPredio(predio);
                }
            }
            CatPredio p = new CatPredio();
            p.setParroquia(predio.getParroquia());
            p.setZona(predio.getZona());
            p.setSector(predio.getSector());
            p.setMz(predio.getMz());
            p.setBloque(predio.getBloque());
            p.setPiso(predio.getPiso());
            p.setAreaSolar(new BigDecimal("0.00"));

            if (areaSolar == null) {
                areaSolar = predio.getAreaSolar();
            }
            if (predioDivision.isEmpty()) {
                if (predio.getPropiedadHorizontal() != null && predio.getPropiedadHorizontal()) {
                    p.setUnidad((short) (unidad + 1));
                } else {
                    p.setLote((short) (solar + 1));
                    p.setSolar((short) (solar + 1));
                    p.setUnidad(predio.getUnidad());
                }
                predioDivision.add(p);
            } else {
                CatPredio pTemp = new CatPredio();
                pTemp = predioDivision.get(predioDivision.size() - 1);
                if (predio.getPropiedadHorizontal() != null && predio.getPropiedadHorizontal()) {
                    p.setUnidad((short) (pTemp.getUnidad() + 1));
                } else {
                    p.setLote((short) (pTemp.getSolar() + 1));
                    p.setSolar((short) (pTemp.getSolar() + 1));
                    p.setUnidad(predio.getUnidad());
                }
                predioDivision.add(p);
            }

        }

    }

    public void removePredioDivision(Short solar) {
        try {
            int index = -1;
            if (solar != null) {
                for (CatPredio cp : predioDivision) {
                    index++;
                    if (cp.getSolar().equals(solar)) {
                        predioDivision.remove(index);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Nada que Mostrar ;) ");
        }

    }

    public void validateAreaSolarDivisionCellRow(CellEditEvent event) {
        int index = -1;
        BigDecimal newSolar = (BigDecimal) event.getNewValue();
        try {
            if (newSolar.compareTo(areaSolar) > 0) {
                for (CatPredio cp : predioDivision) {
                    index++;
                    if (cp.getAreaSolar().equals(newSolar)) {
                        Faces.messageWarning(null, "Advertencia!", "El Area de Solar asignada no debe superar al Area Original");
                        cp.setAreaSolar((new BigDecimal("0.00")));
                        predioDivision.set(index, cp);
                    }
                }
            }
            sumAreaValidate("");
        } catch (Exception e) {
        }

    }

    public Boolean sumAreaValidate(String control) {
        int index = -1;
        BigDecimal acum = new BigDecimal("0.00");
        for (CatPredio cp : predioDivision) {
            acum = acum.add(cp.getAreaSolar());
            index++;
            if (acum.compareTo(areaSolar) > 0) {
                Faces.messageWarning(null, "Advertencia!", "La Sumatoria de las areas no debe ser mayor al Area Original");
                cp.setAreaSolar((new BigDecimal("0.00")));
                predioDivision.set(index, cp);
            }
            if ((acum.compareTo(BigDecimal.ZERO) == 0) && control.equals("save")) {
                return true;
            }
            if ((acum.compareTo(areaSolar) == 1) && control.equals("save")) {
                return true;
            }
            if ((cp.getAreaSolar().compareTo(BigDecimal.ZERO) == 0) && control.equals("save")) {
                return true;
            }
        }
        return false;
    }

    public void clearDivisionPredio() {
        predioDivision = new ArrayList<CatPredio>();
    }

    public String claveCatastral(CatPredio px) {
        return Utils.completarCadenaConCeros(px.getProvincia().toString(), 2)
                + Utils.completarCadenaConCeros(px.getCanton().toString(), 2)
                + Utils.completarCadenaConCeros(px.getParroquia().toString(), 2)
                + Utils.completarCadenaConCeros(px.getZona().toString(), 2)
                + Utils.completarCadenaConCeros(px.getSector().toString(), 2)
                + Utils.completarCadenaConCeros(px.getMz().toString(), 3)
                + Utils.completarCadenaConCeros(px.getSolar().toString(), 3)
                + Utils.completarCadenaConCeros(px.getBloque().toString(), 2)
                + Utils.completarCadenaConCeros(px.getPiso().toString(), 3)
                + Utils.completarCadenaConCeros(px.getUnidad().toString(), 3);
    }

    public void saveDivisionPredio() {
        try {
            if (predioDivision.size() != 0) {
                if (!sumAreaValidate("save")) {
                    this.prediosResultantes = fusionDivisionEjb.saveDivisionPredio(predio, predioDivision);
                    clearDivisionPredio();
                    JsfUti.executeJS("PF('dlgDivision').hide()");
                    JsfUti.executeJS("PF('dlgMatricula').show()");
                } else {
                    Faces.messageWarning(null, "Advertencia!", "Para poder Fraccionar un predio la suma de las Areas deben ser mayor a 0 e igual al Area del Solar Original");
                }
            } else {
                Faces.messageWarning(null, "Advertencia!", "El numero de fracciones para el predio debe de ser mas de uno y tener el area de solar ser mayor a cero ");
            }

        } catch (Exception e) {
        }

    }

    public boolean predioActivo() {
        if (!predio.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
            Faces.messageWarning(null, "Advertencia", "El predio no se encuentra Activo.");
            return false;
        }
        if (appconfig.isLocked(sess.getIpClient() + ":" + sess.getName_user(), predio.getId())) {
            Faces.messageWarning(null, "Advertencia", "El predio está siendo editado por otro usuario");
            return false;
        }
        appconfig.lockPredio(sess.getIpClient() + ":" + sess.getName_user(), predio.getId());
        return true;
    }

    public void continuar(CatPredio pre) {
        if (pre.getId() != null) {
            ss.instanciarParametros();
            ss.agregarParametro("idPredio", pre.getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("numPredio", pre.getNumPredio());
            Faces.redirectFacesNewTab("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
            predio = new CatPredio();
        } else {
            Faces.messageWarning(null, "Advertencia!", "El predio no registra ninguna matricula inmobiliaria, revise que los datos ingresados sean correctos");
        }
    }

    public String claveCroquis(CatPredio px) {
        return Utils.completarCadenaConCeros(px.getZona().toString(), 2) + "-"
                + Utils.completarCadenaConCeros(px.getSector().toString(), 3) + "-"
                + Utils.completarCadenaConCeros(px.getMz().toString(), 3) + "-"
                + Utils.completarCadenaConCeros(px.getSolar().toString(), 2);
    }

    public CatParroquia getParroquia() {
        return parroquia;
    }

    public void setParroquia(CatParroquia parroquia) {
        this.parroquia = parroquia;
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public int getOptFiltro() {
        return optFiltro;
    }

    public void setOptFiltro(int optFiltro) {
        this.optFiltro = optFiltro;
    }

    public boolean getFiltro() {
        return filtro;
    }

    public void setFiltro(boolean filtro) {
        this.filtro = filtro;
    }

    public List<CatCiudadela> getCiudadelas() {
        return ciudadelas;
    }

    public void setCiudadelas(List<CatCiudadela> ciudadelas) {
        this.ciudadelas = ciudadelas;
    }

    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

    public ReportesView getReportes() {
        return reportes;
    }

    public void setReportes(ReportesView reportes) {
        this.reportes = reportes;
    }

    public PdfReporte getReporte() {
        return reporte;
    }

    public void setReporte(PdfReporte reporte) {
        this.reporte = reporte;
    }

    public BaseLazyDataModel<Predio> getHistorico() {
        return historico;
    }

    public void setHistorico(BaseLazyDataModel<Predio> historico) {
        this.historico = historico;
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

    public String getRsocial() {
        return rsocial;
    }

    public void setRsocial(String rsocial) {
        this.rsocial = rsocial;
    }

    public String getCiruc() {
        return ciruc;
    }

    public void setCiruc(String ciruc) {
        this.ciruc = ciruc;
    }

    public CatCiudadela getCiudadela() {
        return ciudadela;
    }

    public void setCiudadela(CatCiudadela ciudadela) {
        this.ciudadela = ciudadela;
    }

    public List<CatPredio> getPredSel() {
        return predSel;
    }

    public void setPredSel(List<CatPredio> predSel) {
        this.predSel = predSel;
    }

    public Boolean getAct() {
        return act;
    }

    public void setAct(Boolean act) {
        this.act = act;
    }

    public Integer getMinimo() {
        return minimo;
    }

    public void setMinimo(Integer minimo) {
        this.minimo = minimo;
    }

    public Integer getMaximo() {
        return maximo;
    }

    public void setMaximo(Integer maximo) {
        this.maximo = maximo;
    }

    public CatPredioModel getPredioModel() {
        return predioModel;
    }

    public void setPredioModel(CatPredioModel predioModel) {
        this.predioModel = predioModel;
    }

    public BaseLazyDataModel<CatPredioPropietario> getPropietarios() {
        return propietarios;
    }

    public void setPropietarios(BaseLazyDataModel<CatPredioPropietario> propietarios) {
        this.propietarios = propietarios;
    }

    public CatPredioPropietario getPropietario() {
        return propietario;
    }

    public void setPropietario(CatPredioPropietario propietario) {
        this.propietario = propietario;
    }

    public CatEnte getContribuyenteConsulta() {
        return contribuyenteConsulta;
    }

    public void setContribuyenteConsulta(CatEnte contribuyenteConsulta) {
        this.contribuyenteConsulta = contribuyenteConsulta;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public CatPredio getP() {
        return p;
    }

    public void setP(CatPredio p) {
        this.p = p;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(Integer tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public List<AclUser> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<AclUser> usuarios) {
        this.usuarios = usuarios;
    }

    public AclUser getUser() {
        return user;
    }

    public void setUser(AclUser user) {
        this.user = user;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Long getCanton() {
        return canton;
    }

    public void setCanton(Long canton) {
        this.canton = canton;
    }

    public void gestionarAlicuotas() {
        if (predio != null) {
            ss.agregarParametro("numPredio", predio.getNumPredio());
            ss.agregarParametro("idPredio", predio.getId());
            Faces.redirectFaces("/faces/vistaprocesos/catastro/gestionarPH.xhtml");
        }
    }

    public ArrayList<CatPredio> getPredioDivision() {
        return predioDivision;
    }

    public void setPredioDivision(ArrayList<CatPredio> predioDivision) {
        this.predioDivision = predioDivision;
    }

    public ArrayList<CatPredio> getPrediosResultantes() {
        return prediosResultantes;
    }

    public void setPrediosResultantes(ArrayList<CatPredio> prediosResultantes) {
        this.prediosResultantes = prediosResultantes;
    }

    public boolean isAccionDivisionFusion() {
        return accionDivisionFusion;
    }

    public void setAccionDivisionFusion(boolean accionDivisionFusion) {
        this.accionDivisionFusion = accionDivisionFusion;
    }

    public void checkEstado() {

        if (predio != null) {
            if (predio.getEstado().equals("I") || predio.getEstado().equals("H")) {
                accionDivisionFusion = false;
            } else {
                accionDivisionFusion = true;
            }
        }
    }

    public String getTipoPredio() {
        return tipoPredio;
    }

    public void setTipoPredio(String tipoPredio) {
        this.tipoPredio = tipoPredio;
    }

    public void consultaTipoPredio() {
        predios.setTipoPredio(tipoPredio);
    }

    public Date getFecha_hasta() {
        return fecha_hasta;
    }

    public void setFecha_hasta(Date fecha_hasta) {
        this.fecha_hasta = fecha_hasta;
    }

    public void eliminarPredio() {
        if (!this.predioActivo()) {
            Faces.update("frmPredios");
        } else {
            EliminacionPredioPost ev = new EliminacionPredioPost();
            ev.setCodPredio(predio.getClaveCat());
            ev.setNumPredio(predio.getNumPredio().longValue());
            ev.setPredio(predio);
            eventEliminarPredio.fire(ev);
            Faces.redirectFaces("/faces/vistaprocesos/catastro/predios.xhtml");
        }
    }

    public void cession() {
        if (!this.predioActivo()) {
            Faces.update("frmPredios");
        } else {
            throw new RuntimeException("Opcion no se encuentra implementada.");
        }
    }

    public void loadResponsablesPredio() {
        try {
            Utils.openDialog("/resources/dialog/Ciudadanos", null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public void selectedResponsableCiudadano(SelectEvent event) {
        this.responsable = (CiuCiudadano) event.getObject();
        if (this.responsable != null) {
            List<CatPredioPropietario> pps = this.catas.getPropietarios(this.responsable.getCiuCedRuc());
            if (Utils.isNotEmpty(pps)) {
                this.predios.setPropietarios(pps);
            } else {
                JsfUti.messageError(null, "Error", "Ciudadano no contiene predios.");
            }
        } else {
            JsfUti.messageError(null, "Error", "Ciudadano no seleccionado.");
        }
    }

    public CiuCiudadano getResponsable() {
        return responsable;
    }

    public void setResponsable(CiuCiudadano responsable) {
        this.responsable = responsable;
    }

    public void imprimirReporte(int opcion) {
        switch (opcion) {
            case 1:
                this.reportePredioUsuario();
                break;
            case 2:
                this.reporteModificacionesxUsuario();
                break;
            case 3:
                this.reporteModificacionesxFecha();
                break;
            default:
                this.reporteModificacionesxFecha();
        }
    }

    public List<String> verificarRestriccion(String predialant) {
        List<RestricionPredio> rest = dataBaseIb.findRestriccionPredio(predialant);
        List<String> observacionRestricciones = null;
        if (rest != null) {
            observacionRestricciones = new ArrayList<>();
            for (RestricionPredio rt : rest) {
                observacionRestricciones.add("Tipo de Restricción " + rt.getRestricciones().getDescripcionRestriccion()
                        + ", " + Utils.isEmpty(rt.getComentario()));
            }
        }
        return observacionRestricciones;
    }

    public String getMessageConfirm() {
        return messageConfirm;
    }

    public void setMessageConfirm(String messageConfirm) {
        this.messageConfirm = messageConfirm;
    }

    public void updateMessage(String message) {
        System.out.println("Entro al update message");
        this.messageConfirm = message;
    }

    public Long getNumPrediosActivos() {
        return numPrediosActivos;
    }

    public void setNumPrediosActivos(Long numPrediosActivos) {
        this.numPrediosActivos = numPrediosActivos;
    }

    public Long getNumPredios() {
        return numPredios;
    }

    public void setNumPredios(Long numPredios) {
        this.numPredios = numPredios;
    }

    public Long getNumPrediosInactivos() {
        return numPrediosInactivos;
    }

    public void setNumPrediosInactivos(Long numPrediosInactivos) {
        this.numPrediosInactivos = numPrediosInactivos;
    }

    public Long getNumPrediosHistorico() {
        return numPrediosHistorico;
    }

    public void setNumPrediosHistorico(Long numPrediosHistorico) {
        this.numPrediosHistorico = numPrediosHistorico;
    }

    public BigDecimal getAvaluosTerrenos() {
        return avaluosTerrenos;
    }

    public void setAvaluosTerrenos(BigDecimal avaluosTerrenos) {
        this.avaluosTerrenos = avaluosTerrenos;
    }

    public BigDecimal getAvaluosConstruccion() {
        return avaluosConstruccion;
    }

    public void setAvaluosConstruccion(BigDecimal avaluosConstruccion) {
        this.avaluosConstruccion = avaluosConstruccion;
    }

    public BigDecimal getAvaluosPropiedad() {
        return avaluosPropiedad;
    }

    public void setAvaluosPropiedad(BigDecimal avaluosPropiedad) {
        this.avaluosPropiedad = avaluosPropiedad;
    }

    public Long getNumPrediosPrivados() {
        return numPrediosPrivados;
    }

    public void setNumPrediosPrivados(Long numPrediosPrivados) {
        this.numPrediosPrivados = numPrediosPrivados;
    }

    public Long getNumPrediosPublicos() {
        return numPrediosPublicos;
    }

    public void setNumPrediosPublicos(Long numPrediosPublicos) {
        this.numPrediosPublicos = numPrediosPublicos;
    }

    public Long getConteoPrediosUrbanos() {
        return conteoPrediosUrbanos;
    }

    public void setConteoPrediosUrbanos(Long conteoPrediosUrbanos) {
        this.conteoPrediosUrbanos = conteoPrediosUrbanos;
    }

    public Long getConteoPrediosRurales() {
        return conteoPrediosRurales;
    }

    public void setConteoPrediosRurales(Long conteoPrediosRurales) {
        this.conteoPrediosRurales = conteoPrediosRurales;
    }

}
