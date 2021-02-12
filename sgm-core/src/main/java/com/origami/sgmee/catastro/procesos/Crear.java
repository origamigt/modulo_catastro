/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.procesos;

import com.origami.censocat.restful.JsonUtils;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatPredioS6;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.Observaciones;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.enums.TipoProceso;
import com.origami.sgm.events.CreacionPredioPost;
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.services.ejbs.SeqGenManEjb;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.CreacionServ;
import com.origami.sgmee.catastro.geotx.GeoProcesosException;
import com.origami.sgmee.catastro.geotx.GeoProcesosService;
import com.origami.sgmee.catastro.geotx.ModelPrediosTarea;
import com.origami.sgmee.catastro.geotx.model.ManzanaModel;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.ProcesoUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.activiti.engine.task.Task;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 * Controlado para el proceso de crear predio.(Todos los predios que sean
 * creados apartir de una tarea seran activas una ves terminada la tarea mistran
 * tanto se mantienen en estado 'P')
 *
 * @author dfcalderio
 */
@Named(value = "crearFlow")
@ViewScoped
public class Crear extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(Crear.class.getName());

    @Inject
    private ServletSession ss;

    @Inject
    private CatastroServices catastroService;

    @Inject
    private CreacionServ creacionServ;

    @Inject
    protected Event<HistoricoPredioPost> eventHistorico;
    @Inject
    protected Event<CreacionPredioPost> eventCrear;

    @Inject
    protected ProcesoServices procesoServices;
    @Inject
    private GeoProcesosService geoProcesosService;

    protected HistoricoTramites ht;

    protected CatPredio predio;
    protected List<HistoricoTramiteDet> detallesTramite;
    protected boolean renderCompletar;
    protected boolean finalizar = false;
    protected boolean volverEdicion = false;

    private Boolean edicionGrafica;

    private Task currentTask;

    protected List<CatPredio> prediosTemp;

    private List<String> codigos;

    private ManzanaModel manzanaModel;

    private JsonUtils jsonUtils;

    private short lastCode;

    private String observacion;

    List<Observaciones> observaciones;

    List<Observaciones> observacionesGenerales;

    List<Observaciones> observacionesCurrentTask;
    protected FormatoReporte formato;
    protected String detalle;
    @Inject
    protected Event<ModelPrediosTarea> eventVerificarMz;

    @PostConstruct
    public void initView() {
        jsonUtils = new JsonUtils();
        observaciones = new LinkedList<>();
        observacionesGenerales = new LinkedList<>();
        observacionesCurrentTask = new LinkedList<>();
        renderCompletar = false;
        codigos = new LinkedList<>();
        prediosTemp = new LinkedList<>();
        detallesTramite = new LinkedList<>();
        boolean unificado = false;
        if (session != null && session.getTaskID() != null) {
            ht = new HistoricoTramites();
            setTaskId(session.getTaskID());
            ht = this.getHistoricoTramiteById(new Long(this.getVariable(session.getTaskID(), "tramite").toString()));
            observacionesGenerales = ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), null);

            List<Task> tasks = engine.getTasksUserProcessId(session.getName_user(), ht.getIdProceso());
            if (!tasks.isEmpty()) {
                currentTask = tasks.get(0);
                observacionesCurrentTask = ProcesoUtil.getObservacionesTask(ht.getObservacionesCollection(), currentTask.getTaskDefinitionKey());
                if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {
                    edicionGrafica = Boolean.TRUE;
                } else {
                    edicionGrafica = Boolean.FALSE;
                }
            }
            for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                manzanaModel = jsonUtils.jsonToObjectStaticModel(d.getJson(), ManzanaModel.class);
                predio = d.getPredio();
                if (predio != null) {
                    d.setObservaciones(ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), d.getPredio().getId()));
                }
                detallesTramite.add(d);
                if (d.getNumTasa().equals(BigInteger.ONE)) {
                    renderCompletar = true;
                }
            }
            if (manzanaModel == null) {
                manzanaModel = new ManzanaModel();
            }
        } else {
            this.continuar();
        }
    }

    public void editarPredio(CatPredio p) {

        if (p.getId() != null) {
            PolygonData poly = creacionServ.detectPoligono(ht.getId().toString());
            ss.instanciarParametros();
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            if (poly != null) {
                ss.agregarParametro("areaGrafica", poly.getArea());
                ss.agregarParametro("idPrediosTx", poly.getGid());
            }

            ss.agregarParametro("edit", true);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("formEdicion", currentTask.getFormKey());
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("proceso", TipoProceso.FUSIONAR_PREDIOS);
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void revisarPredio(CatPredio p) {

        if (p.getId() != null) {
            PolygonData poly = creacionServ.detectPoligono(ht.getId().toString());

            ss.instanciarParametros();
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            if (poly != null) {
                ss.agregarParametro("areaGrafica", poly.getArea());
            }

            ss.agregarParametro("edit", false);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("formRevision", this.getVariable(this.getTaskId(), "formRevision"));
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("proceso", TipoProceso.FUSIONAR_PREDIOS);
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void completar() {

        if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {

            boolean creado = false;
            detallesTramite = new LinkedList<>();

            for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                if (d.getPredio() != null) {
                    creado = true;
                }
                detallesTramite.add(d);
            }
            if (!creado) {
                try {
                    PolygonData poly = creacionServ.detectPoligono(ht.getId().toString());
                    System.out.println("Tipo Predio " + poly.getTipo());
                    predio = new CatPredio();
                    predio.setProvincia(manzanaModel.getCodProvincia());
                    predio.setCanton(manzanaModel.getCodCanton());
                    predio.setParroquia(manzanaModel.getCodParroquia());
                    predio.setZona(manzanaModel.getCodZona());
                    predio.setSector(manzanaModel.getCodSector());
                    predio.setMz(manzanaModel.getCodManzana());
                    predio.setSolar(getLastCode());
                    predio = registrarPredio(poly);

                    detallesTramite.get(0).setPredio(predio);
                    manager.update(detallesTramite.get(0));

                } catch (GeoProcesosException ex) {
                    JsfUti.messageError(null, "Info", ex.getMessage());
                    return;
                }
            }
        }
        procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
        HashMap<String, Object> paramt = new HashMap<>();
        paramt.put("prioridad", 50);
        this.completeTask(session.getTaskID(), paramt);
        JsfUti.messageInfo(null, "Info!", "Tarea " + currentTask.getName() + " completada con exito.");
        this.continuar();

    }

    public void completarRevision() {
        try {
            Map<String, Object> pm = new HashMap<>();
            pm.put("numPredio", ht.getNumPredio());
            CatPredio predioraiz = this.manager.findObjectByParameter(CatPredio.class, pm);

            HashMap<String, Object> paramt = new HashMap<>();
            paramt.put("prioridad", 50);
            for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                if (d.getNumTasa().equals(BigInteger.valueOf(1))) {
                    finalizar = true;
                    break;
                } else {
                    finalizar = false;
                }
                predio = d.getPredio();
            }
            if (finalizar) {
                paramt.put("aprobado", 1);
                ht.setEstado("Finalizado");
                manager.persist(ht);
                eventHistorico.fire(new HistoricoPredioPost(predioraiz));
                actualizarEstadoPredio();
                CreacionPredioPost even = new CreacionPredioPost();
                even.setCodPredio(predio.getClaveCat());
                even.setNumTramite(ht.getId().toString());
                even.setNumPredio(predio.getNumPredio());
                even.setTipo(predio.getTipoPredio());
                eventCrear.fire(even);

                //Enviamos verificar las manzana que se afecto
                ModelPrediosTarea eventoVerif = new ModelPrediosTarea();
                eventoVerif.setCodigos(Arrays.asList(predio.getClaveCat()));
                eventoVerif.setTecnico(this.getVariable(this.getTaskId(), "dibujante").toString());
                eventVerificarMz.fire(eventoVerif);

            } else {
                paramt.put("aprobado", 0);
            }
            this.completeTask(session.getTaskID(), paramt);
            procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
            JsfUti.messageInfo(null, "Info!", "Tarea " + currentTask.getName() + " completada con exito.");
            this.continuar();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "completarRevision", e);
        }

    }

    public CatPredio registrarPredio(PolygonData poly) {
        if (estaDibujado()) {

            if (predio.getNombreEdificio() == null) {
                predio.setNombreEdificio("");
            }
            if (predio.getProvincia() > 0 && predio.getCanton() > 0 && predio.getParroquia() > 0
                    && predio.getZona() > 0 && predio.getSector() > 0 && predio.getMz() > 0
                    && predio.getSolar() > 0) {
                //predio.setUsuarioCreador();
                predio.setInstCreacion(new Date());
                predio.setEstado("P");
                if (poly.getTipo() != null) {
                    if (poly.getTipo().contains("URBANO") || poly.getTipo().equalsIgnoreCase("URBANO")
                            || poly.getTipo().equalsIgnoreCase("URB")) {
                        predio.setTipoPredio("U");
                        predio.setAreaSolar(poly.getArea());
                    } else {
                        predio.setTipoPredio("R");
                        predio.setAreaSolar(poly.getArea().divide(BigDecimal.valueOf(10000L)));
                    }
                } else {
                    predio.setTipoPredio("U");
                    predio.setAreaSolar(poly.getArea());
                }
                predio.setLote(predio.getSolar());
                predio.setUsuarioCreador(new AclUser(this.session.getUserId()));
                predio.setMzdiv(new Short("0"));
                predio.setPropiedadHorizontal(false);

                predio = registrarPredio(predio);
                if (predio.getTipoPredio().contains("U")) {
                    this.geoProcesosService.verificarValoraManzana(predio.getClaveCat().subSequence(0, 13).toString(), null);
                    this.geoProcesosService.verificarEjeValorGidPredioTx(null, predio, poly.getGid());
                } else {
                    // VERIFICACION DE CALIDAD DEL SUELO RURAL
                    // REGISTRA LAS CALIDADES SUELO ENCONTRADAS EN EL PREDIO
                    if (geoProcesosService.getActivarCalidadSueloRural()) {
                        this.geoProcesosService.verificarClasificacionsuelo(predio, poly.getGid(), predio.getCatPredioClasificRuralCollection());
                    }
                }

                CatPredioS4 s4 = new CatPredioS4();
                s4.setAreaGraficaLote(poly.getArea());
                s4.setAreaCalculada(poly.getArea());
                s4.setPredio(predio);
                manager.persist(s4);

                CatPredioS6 s6 = new CatPredioS6();
                s6.setAreaSolar(poly.getArea());
                s6.setPredio(predio);
                manager.persist(s6);

            } else {
                Faces.messageInfo(null, "Datos Invalidos", "Los datos marcados como (*) son Obligatorios");

            }
        } else {
            Faces.messageInfo(null, "No dibujado !", " predio no se ecuentra dibujado.");
        }

        return predio;
    }

    public CatPredio registrarPredio(CatPredio px) {
        CatPredio pred = null;
        try {
            if (px != null) {
                try {
                    px.setClaveCat(claveCatastral(px));
                    px.setNumPredio(generarNumPredio());
                } catch (Exception e) {
                }
                pred = (CatPredio) manager.persist(px);
            }
        } catch (Exception e) {

        }
        return pred;
    }

    public String claveCatastral(CatPredio px) {

        String clave = String.format("%02d%02d%02d%02d%02d%03d%03d%03d%02d%03d",
                px.getProvincia(), px.getCanton(), px.getParroquia(), px.getZona(), px.getSector(),
                px.getMz(), px.getSolar(), px.getBloque(), px.getPiso(), px.getUnidad());

        return clave;

    }

    public BigInteger generarNumPredio() {
        try {

            Object sequence = manager.find(Querys.getMaxCatPredio);
            BigInteger l;
            if (sequence == null) {
                return new BigInteger("1");
            } else {
                l = (BigInteger) sequence;
                l = l.add(new BigInteger("1"));
                return l;
            }

        } catch (Exception e) {
            Logger.getLogger(SeqGenManEjb.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public boolean estaDibujado() {
        return true;
    }

    public String mensajeConfirmarRevision() {
        String claves = "";
        int cont = 0;
        for (HistoricoTramiteDet dt : detallesTramite) {
            if (dt.getNumTasa() != null) {
                if (dt.getNumTasa().compareTo(new BigInteger("2")) == 0) {
                    if (cont == 0) {
                        claves += dt.getPredio().getClaveCat();
                    } else {
                        claves += ", " + dt.getPredio().getClaveCat();
                    }
                    volverEdicion = true;
                    cont++;
                }
            }
        }
        String messages;
        if (!volverEdicion) {
            messages = " El proceso se va a finalizar ya que todos los predios estan aprobados en la revisión. ";
        } else {
            messages = " El proceso volvera a la edición ya que " + (cont == 1 ? "el predio con clave " : "los predios con claves ") + claves + (cont == 1 ? " no fue aprobado en la revisión. " : " no fueron aprobados en la revisón. ");
        }
        return messages;
    }

    public void rechazarEdicion() {

        this.continuar();

    }

    public List<FormatoReporte> getInformes() {
        return catastroService.getFormatosInformes(Boolean.FALSE);
    }

    public void createInforme() {
        try {
            Map<String, Object> pm = new HashMap<>();
            pm.put("codigo", this.ht.getTipoTramite().getDescripcionTramite().trim().toUpperCase());
            if (formato == null) {
                formato = this.manager.findObjectByParameter(FormatoReporte.class, pm);
            }
            if (formato != null) {
                String tramiteQuipux = this.ht.getCarpetaRep();
                String propietario = "";
                String cedula = "";
                String claveCat = "";
                String ubicacion = "";
                String parroquia = "";
                String canton = "";
                CatPredio predio = null;
                if (Utils.isNotEmpty(this.detallesTramite)) {
                    if (this.detallesTramite.get(0).getPredio() != null) {
                        predio = this.detallesTramite.get(0).getPredio();
                    }
                }
                if (predio != null) {
                    claveCat = this.claveCatastral(predio);
                    ubicacion = Utils.isEmpty(predio.getCalle()) + " y " + Utils.isEmpty(predio.getCalleS());
                    CatParroquia cpq = catastroService.getCatParroquia(predio.getParroquia());
                    canton = cpq.getIdCanton().getNombre();
                    parroquia = cpq == null ? "" : cpq.getDescripcion();
                    ubicacion.concat(", ").concat(parroquia);
                    CatPredioPropietario get = Utils.get(predio.getCatPredioPropietarioCollection(), 0);
                    propietario = get.getNombresCompletos()
                            + (get.getOtros() != null ? " " + get.getOtros() : "");
                    cedula = get.getCiuCedRuc();
                }
                detalle = formato.getFormato();
                if (detalle.contains(":predioPropietario")) {
                    detalle = detalle.replace(":predioPropietario", propietario);
                }
                if (detalle.contains(":propietarioPredio")) {
                    detalle = detalle.replace(":propietarioPredio", propietario);
                }
                if (detalle.contains(":numTramite")) {
                    detalle = detalle.replace(":numTramite", tramiteQuipux);
                }
                if (detalle.contains(":solicitante")) {
                    detalle = detalle.replace(":solicitante", propietario);
                }
                if (detalle.contains(":cedula")) {
                    detalle = detalle.replace(":cedula", cedula);
                }
                if (detalle.contains(":claveCat")) {
                    detalle = detalle.replace(":claveCat", claveCat);
                }
                if (detalle.contains(":bienio1")) {
                    detalle = detalle.replace(":bienio1", "");
                }
                if (detalle.contains(":bienio2")) {
                    detalle = detalle.replace(":bienio2", "");
                }
                if (detalle.contains(":anioValorizacion")) {
                    detalle = detalle.replace(":anioValorizacion", "");
                }
                if (detalle.contains(":ubicacion")) {
                    detalle = detalle.replace(":ubicacion", ubicacion);
                }
                if (detalle.contains(":canton")) {
                    detalle = detalle.replace(":canton", canton);
                }
            } else {
                JsfUti.messageError(null, "Error!", "No se encontro el formato para generar el reporte.");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "completarRevision", e);
        }
    }

    public void generarInforme() {
        // Inicio de parametros de informe
        System.out.println("Empezando a generar reporte en Word. (generarInforme())");
        //            byte[] bytes = detalle.getBytes();
//            detalle = new String(bytes, "UTF-8");
//            System.out.println("Detalle >> " + detalle);
        ss.instanciarParametros();
        ss.setTieneDatasource(false);
        ss.setNombreSubCarpeta("catastro/informesProcesos");
        ss.setNombreReporte(formato.getReporte());
        ss.agregarParametro("DETALLE", detalle);
        ss.agregarParametro("TITULO", "");
        ss.agregarParametro("NOMBRE_CERTIFICADO", formato.getCodigo());
        List<CatPredio> dataSource = new ArrayList<>();
        dataSource.add(predio);
        ss.setDataSource(dataSource);
// Fin de parametro de informe
        JsfUti.redirectNewTab(SisVars.urlbase + "DocumentoWord"); // Generador de Reporte
    }

    private void actualizarEstadoPredio() {
        detallesTramite.stream().map((hd) -> {
            hd.getPredio().setEstado(EstadosPredio.ACTIVO);
            return hd;
        }).forEachOrdered((hd) -> {
            manager.persist(hd.getPredio());
        });
    }

    public String getMensajeGrafico() {
        String mensaje = "El Código asignado al nuevo LOTE es: " + getLastCode();
        if (predio != null) {
            mensaje = "El predio ya ha sido creado en la base alfanumerica.";
        }

        return mensaje;
    }

    //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public HistoricoTramites getHt() {
        return ht;
    }

    public void setHt(HistoricoTramites ht) {
        this.ht = ht;
    }

    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

    public boolean isRenderCompletar() {
        return renderCompletar;
    }

    public void setRenderCompletar(boolean renderCompletar) {
        this.renderCompletar = renderCompletar;
    }

    public List<HistoricoTramiteDet> getDetallesTramite() {
        return detallesTramite;
    }

    public void setDetallesTramite(List<HistoricoTramiteDet> detallesTramite) {
        this.detallesTramite = detallesTramite;
    }

    public boolean isVolverEdicion() {
        return volverEdicion;
    }

    public void setVolverEdicion(boolean volverEdicion) {
        this.volverEdicion = volverEdicion;
    }

    public Boolean getEdicionGrafica() {
        return edicionGrafica;
    }

    public void setEdicionGrafica(Boolean edicionGrafica) {
        this.edicionGrafica = edicionGrafica;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    public ManzanaModel getManzanaModel() {
        return manzanaModel;
    }

    public void setManzanaModel(ManzanaModel manzanaModel) {
        this.manzanaModel = manzanaModel;
    }

    public short getLastCode() {
        String[] param = {"parroquia", "zona", "sector", "mz"};
        Object[] values = {manzanaModel.getCodParroquia(), manzanaModel.getCodZona(), manzanaModel.getCodSector(), manzanaModel.getCodManzana()};
        Short max = (Short) manager.find(Querys.getMaxCodeByManzana, param, values);
        lastCode = max != null ? ++max : (short) 1;
        return lastCode;
    }

    public void setLastCode(short lastCode) {
        this.lastCode = lastCode;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public List<Observaciones> getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(List<Observaciones> observaciones) {
        this.observaciones = observaciones;
    }

    public List<Observaciones> getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(List<Observaciones> observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }

    public List<Observaciones> getObservacionesCurrentTask() {
        return observacionesCurrentTask;
    }

    public void setObservacionesCurrentTask(List<Observaciones> observacionesCurrentTask) {
        this.observacionesCurrentTask = observacionesCurrentTask;
    }

    public FormatoReporte getFormato() {
        return formato;
    }

    public void setFormato(FormatoReporte formato) {
        this.formato = formato;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
//</editor-fold>

}
