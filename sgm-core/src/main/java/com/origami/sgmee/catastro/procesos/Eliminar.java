/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.procesos;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.Observaciones;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.enums.TipoProceso;
import com.origami.sgm.events.EliminacionPredioPost;
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.services.interfaces.catastro.FusionDivisionServices;
import com.origami.sgmee.catastro.geotx.EliminacionServ;
import com.origami.sgmee.catastro.geotx.GeoProcesosException;
import com.origami.sgmee.catastro.geotx.GeoProcesosService;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.ProcesoUtil;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
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
 * Controlador para el proceso de eliminacion de predio.
 *
 * @author dfcalderio
 */
@Named(value = "eliminarFlow")
@ViewScoped
public class Eliminar extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(Eliminar.class.getName());

    @Inject
    private ServletSession ss;

    @Inject
    private CatastroServices catastroService;

    @Inject
    protected FusionDivisionServices divisionServices;

    @Inject
    private EliminacionServ eliminacionServ;

    protected HistoricoTramites ht;

    protected List<CatPredio> predios;
    protected CatPredio predio;
    protected List<HistoricoTramiteDet> detallesTramite;
    protected boolean renderCompletar;
    protected boolean finalizar = false;
    protected boolean volverEdicion = false;
    @Inject
    protected Event<EliminacionPredioPost> event;
    @Inject
    protected Event<HistoricoPredioPost> eventHistorico;

    @Inject
    private GeoProcesosService procesosService;

    private Boolean edicionGrafica;

    private Task currentTask;

    protected List<CatPredio> prediosTemp;
    protected GeoProcesoDivision division;

    private List<String> codigos;

    @Inject
    private ProcesoServices procesoServices;

    private String observacion;

    private List<Observaciones> observaciones;

    private List<Observaciones> observacionesGenerales;
    protected FormatoReporte formato;
    protected String detalle;

    @PostConstruct
    public void initView() {
        renderCompletar = false;
        codigos = new LinkedList<>();
        predios = new LinkedList<>();
        prediosTemp = new LinkedList<>();
        detallesTramite = new LinkedList<>();
        observaciones = new LinkedList<>();
        observacionesGenerales = new LinkedList<>();
        boolean unificado = false;
        if (session != null && session.getTaskID() != null) {
            ht = new HistoricoTramites();
            setTaskId(session.getTaskID());
            ht = this.getHistoricoTramiteById(new Long(this.getVariable(session.getTaskID(), "tramite").toString()));
            predio = catastroService.getPredioNumPredio(ht.getNumPredio().longValue());
            observacionesGenerales = ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), null);
            List<Task> tasks = engine.getTasksUserProcessId(session.getName_user(), ht.getIdProceso());
            if (!tasks.isEmpty()) {
                currentTask = tasks.get(0);
                if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {
                    edicionGrafica = Boolean.TRUE;
                } else {
                    edicionGrafica = Boolean.FALSE;
                }
            }
            for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                d.setObservaciones(ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), d.getPredio().getId()));
                detallesTramite.add(d);
                if (d.getEstado().equals(Boolean.TRUE)) {
                    renderCompletar = true;
                }

            }
        } else {
            this.continuar();
        }
    }

    public void eliminar(CatPredio p) {

        if (p.getId() != null) {
            p.setEstado(EstadosPredio.INACTIVO);
            manager.update(p);
            detallesTramite.get(0).setPredio(p);
            detallesTramite.get(0).setNumTasa(BigInteger.valueOf(1));
            detallesTramite.get(0).setEstado(Boolean.TRUE);
            JsfUti.messageInfo(null, "Info", "Predio eliminado con exito");
            JsfUti.update("registroPHForm");
        }

    }

    public void revisarPredio(CatPredio p) {

        if (p.getId() != null) {
            ss.instanciarParametros();
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("edit", false);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("formRevision", this.getVariable(this.getTaskId(), "formRevision"));
            ss.agregarParametro("proceso", TipoProceso.FUSIONAR_PREDIOS);
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void completar() {

        if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {
            try {

                detallesTramite = new LinkedList<>();
                predios = new LinkedList<>();
                for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                    detallesTramite.add(d);
                }
                if (!detallesTramite.isEmpty()) {
                    eliminacionServ.checkPoligonoEliminado(detallesTramite.get(0).getPredio().getClaveCat());
                } else {
                    JsfUti.messageError(null, "Info", "Predio no encontrado en el tramite");
                    return;
                }

            } catch (GeoProcesosException ex) {
                JsfUti.messageError(null, "Info", ex.getMessage());
                return;
            }

        } else {

        }

        HashMap<String, Object> paramt = new HashMap<>();
        paramt.put("prioridad", 50);
        this.completeTask(session.getTaskID(), paramt);
        procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
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
            }
            if (finalizar) {
                paramt.put("aprobado", 1);
                ht.setEstado("Finalizado");
                codigos = new LinkedList<>();
                String clave = null;
                for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                    clave = d.getPredio().getClaveCat();
                    break;
                }
                manager.persist(ht);
                eventHistorico.fire(new HistoricoPredioPost(predioraiz));
                actualizarEstadoPredio();
                EliminacionPredioPost even = new EliminacionPredioPost();
                even.setCodPredio(clave);

                event.fire(even);
            } else {
                paramt.put("aprobado", 0);
            }
            procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
            this.completeTask(session.getTaskID(), paramt);
            JsfUti.messageInfo(null, "Info!", "Tarea " + currentTask.getName() + " completada con exito.");
            this.continuar();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "completarRevision", e);
        }

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
            messages = " El proceso volvera a la edición ya que " + (cont == 1 ? "el predio con clave " : "los predios con claves ") + claves + (cont == 1 ? "no fue aprobado en la revisión. " : " no fueron aprobados en la revisón. ");
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
                    claveCat = predio.getClaveCat();
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
        try {
            byte[] bytes = detalle.getBytes();
            detalle = new String(bytes, "UTF-8");
            System.out.println("Detalle >> " + detalle);
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

        } catch (UnsupportedEncodingException e) {
            LOG.log(Level.SEVERE, "generarInforme", e);
        }
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

    public List<CatPredio> getPredios() {
        return predios;
    }

    public void setPredios(List<CatPredio> predios) {
        this.predios = predios;
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

    private void actualizarEstadoPredio() {
        detallesTramite.stream().map((hd) -> {
            hd.getPredio().setEstado(EstadosPredio.ACTIVO);
            return hd;
        }).forEachOrdered((hd) -> {
            manager.persist(hd.getPredio());
        });
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
