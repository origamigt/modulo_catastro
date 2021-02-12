/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.procesos;

import com.origami.session.ServletSession;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.Observaciones;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.enums.TipoProceso;
import com.origami.sgm.events.ActAreasLindPost;
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.events.ValorarPredioPost;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.ActualizacionAreasLinderos;
import com.origami.sgmee.catastro.geotx.GeoProcesosException;
import com.origami.sgmee.catastro.geotx.GeoProcesosService;
import com.origami.sgmee.catastro.geotx.ModelPrediosTarea;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.ProcesoUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
 * Controlador para la tarea Actualizacion de areas y linderos, permite realizar
 * la modificacion del area del predio y la agregacion o modificacion de bloque
 * En un predio normal.
 *
 * @author dfcalderio
 */
@Named(value = "updateLinderoAreaFlow")
@ViewScoped
public class ActualizarLinderosAreas extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(ActualizarLinderosAreas.class.getName());

    @Inject
    private ServletSession ss;

    @Inject
    private CatastroServices catastroService;
    @Inject
    private GeoProcesosService geoProcesosService;

    @Inject
    private ActualizacionAreasLinderos serv;

    protected HistoricoTramites ht;

    protected List<CatPredio> predios;
    protected CatPredio predio;
    protected List<HistoricoTramiteDet> detallesTramite;
    protected boolean renderCompletar;
    protected boolean finalizar = false;
    protected boolean volverEdicion = false;
    @Inject
    protected Event<ActAreasLindPost> event;
    @Inject
    protected Event<ModelPrediosTarea> eventVerificarMz;
    @Inject
    protected Event<HistoricoPredioPost> eventHistorico;
    @Inject
    protected Event<ValorarPredioPost> eventValoracion;

    @Inject
    private ProcesoServices procesoServices;

    private Boolean edicionGrafica;

    private Task currentTask;

    protected List<CatPredio> prediosTemp;
    protected GeoProcesoDivision division;

    private List<String> codigos;

    private String observacion;

    private List<Observaciones> observaciones;

    private List<Observaciones> observacionesGenerales;
    protected String detalle;
    protected FormatoReporte formato;

    @PostConstruct
    public void initView() {
        renderCompletar = false;
        observaciones = new LinkedList<>();
        observacionesGenerales = new LinkedList<>();
        codigos = new LinkedList<>();
        predios = new LinkedList<>();
        prediosTemp = new LinkedList<>();
        detallesTramite = new LinkedList<>();
        boolean unificado = false;
        if (session != null && session.getTaskID() != null) {
            ht = new HistoricoTramites();
            setTaskId(session.getTaskID());
            ht = getHistoricoTramiteById(new Long(this.getVariable(session.getTaskID(), "tramite").toString()));
            observacionesGenerales = ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), null);
            predio = catastroService.getPredioNumPredio(ht.getNumPredio().longValue());
            List<Task> tasks = engine.getTasksUserProcessId(session.getName_user(), ht.getIdProceso());
            if (!tasks.isEmpty()) {
                currentTask = tasks.get(0);
                if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {
                    edicionGrafica = Boolean.TRUE;
                } else {
                    edicionGrafica = Boolean.FALSE;
                }
            }
            int cont = 0;
            for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                d.setObservaciones(
                        ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), d.getPredio().getId()));
                detallesTramite.add(d);
                if (d.getEstado().equals(Boolean.TRUE)) {
                    cont++;
                }
            }
            if (cont == detallesTramite.size()) {
                renderCompletar = true;
            }
        } else {
            this.continuar();
        }
    }

    public void editarPredio(CatPredio p) {

        if (p.getId() != null) {
            List<PolygonData> result = serv.getPoligonosActualizados(Arrays.asList(p.getClaveCat()));
            if (Utils.isEmpty(result)) {
                JsfUti.messageError(null, "Info", "No se encontro poligonos dibujado " + p.getClaveCat());
                return;
            }
            ss.instanciarParametros();
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("areaGrafica", result.get(0).getArea());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("tramiteQuipux", this.ht.getCarpetaRep());
            if (currentTask != null) {
                ss.agregarParametro("formEdicion", currentTask.getFormKey());
                ss.agregarParametro("taskName", currentTask.getName());
            }
            ss.agregarParametro("proceso", TipoProceso.ACTIALIZAR_AREAS_LINDEROS);
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void revisarPredio(CatPredio p) {

        if (p.getId() != null) {
            List<PolygonData> result = serv.getPoligonosActualizados(Arrays.asList(p.getClaveCat()));
            if (Utils.isEmpty(result)) {
                JsfUti.messageError(null, "Info", "No se encontro poligonos dibujado " + p.getClaveCat());
                return;
            }
            ss.instanciarParametros();
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("areaGrafica", result.get(0).getArea());
            ss.agregarParametro("edit", false);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("tramiteQuipux", this.ht.getCarpetaRep());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("formRevision", this.getVariable(this.getTaskId(), "formRevision"));
            ss.agregarParametro("proceso", TipoProceso.ACTIALIZAR_AREAS_LINDEROS);
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void completar() {

        if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {
            try {
                detallesTramite = new LinkedList<>();
                predios = new LinkedList<>();
                codigos = new LinkedList<>();
                for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                    detallesTramite.add(d);
                    codigos.add(d.getPredio().getClaveCat());
                }
                List<PolygonData> result = serv.getPoligonosActualizados(codigos);
                if (Utils.isEmpty(result)) {
                    JsfUti.messageError(null, "Info", "No se encontro poligonos.");
                    return;
                }
                for (HistoricoTramiteDet d : detallesTramite) {
                    for (PolygonData poly : result) {
                        if (d.getPredio().getClaveCat().equals(poly.getCodigo())) {
                            CatPredioS4 s4 = catastroService.getPredioS4ByPredio(d.getPredio());
                            if (s4 == null) {
                                s4 = new CatPredioS4();
                                s4.setPredio(d.getPredio());
                                s4 = (CatPredioS4) manager.persist(s4);
                            }
                            if (d.getPredio().getTipoPredio().equalsIgnoreCase("U")) {
                                s4.setAreaGraficaLote(poly.getArea());
                                d.getPredio().setAreaSolar(poly.getArea());
                                this.geoProcesosService.verificarEjeValorGidPredioTx(null, predio, poly.getGid());
                            } else {
                                s4.setAreaGraficaLote(poly.getArea().divide(BigDecimal.valueOf(10000.00)));
                                d.getPredio().setAreaSolar(poly.getArea().divide(BigDecimal.valueOf(10000.00)));
                                // VALIDAMOS LAS CALIDADES DE SUELO 
                                this.geoProcesosService.validarClasesSuelo(poly);
                                // INSERTAMOS LAS CALSES DE SUELO
                                this.geoProcesosService.verificarClasificacionsuelo(d.getPredio(), poly.getGid(), d.getPredio().getCatPredioClasificRuralCollection());
                            }
                            manager.update(s4);
                            manager.update(d.getPredio());
                        }
                    }
                }

            } catch (GeoProcesosException ex) {
                JsfUti.messageError(null, "Info", ex.getMessage());
                return;
            }

        }
        HashMap<String, Object> paramt = new HashMap<>();
        paramt.put("prioridad", 50);
        this.completeTask(session.getTaskID(), paramt);
        procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion,
                currentTask.getName(), null);
        JsfUti.messageInfo(null, "Info!", "Tarea " + currentTask.getName() + " completada con exito.");
        this.continuar();

    }

    public void completarRevision() {
        System.out.println("Entro a completar la tarea de revision");
        try {
            finalizar = true;
            HashMap<String, Object> paramt = new HashMap<>();
            paramt.put("prioridad", 50);
            for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                if (d.getEstado().equals(Boolean.TRUE)) {
                    if (d.getNumTasa().equals(BigInteger.valueOf(2))) {
                        finalizar = false;
                        break;
                    }
                }
            }
            if (finalizar) {
                paramt.put("aprobado", 1);
                ht.setEstado("Finalizado");
                codigos = new LinkedList<>();

                for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                    codigos.add(d.getPredio().getClaveCat());
                    eventValoracion.fire(new ValorarPredioPost(d.getPredio().getClaveCat(), d.getPredio().getPredialant(), 1, d.getPredio().getTipoPredio()));
                    eventValoracion.fire(new ValorarPredioPost(d.getPredio().getClaveCat(), d.getPredio().getPredialant(), 2, d.getPredio().getTipoPredio()));
                }
                manager.persist(ht);

                actualizarEstadoPredio();

                try {
                    ActAreasLindPost even = new ActAreasLindPost();
                    even.setCodigos(codigos);
                    event.fire(even);

                    // Verificar Predios Modificados
                    ModelPrediosTarea eventoVerif = new ModelPrediosTarea();
                    eventoVerif.setCodigos(codigos);
                    eventoVerif.setTecnico(this.getVariable(this.getTaskId(), "dibujante").toString());
                    eventVerificarMz.fire(eventoVerif);
                } catch (Exception e) {
                    return;
                }

            } else {
                paramt.put("aprobado", 0);
            }

            System.out.println("Valor final de aprobado: " + paramt.get("aprobado"));
            this.completeTask(session.getTaskID(), paramt);
            procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion,
                    currentTask.getName(), null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "completarRevision", e);
        }
        JsfUti.messageInfo(null, "Info!", "Tarea " + currentTask.getName() + " completada con exito.");
        this.continuar();

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
            messages = " El proceso volvera a la edición ya que "
                    + (cont == 1 ? "el predio con clave " : "los predios con claves ") + claves
                    + (cont == 1 ? " no fue aprobado en la revisión. " : " no fueron aprobados en la revisón. ");
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
        this.detalle = this.createInforme(detalle, ht, predio, formato);
    }

    public void generarInforme() {
        if (Utils.isNotEmpty(detallesTramite)) {
            for (HistoricoTramiteDet det : detallesTramite) {
                if (det.getPredio() != null) {
                    prediosTemp.add(det.getPredio());
                }
            }
        }
        this.generarInforme(prediosTemp, detalle, formato, ss);
    }

    // <editor-fold defaultstate="collapsed" desc="Getter and Setter">
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

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public FormatoReporte getFormato() {
        return formato;
    }

    public void setFormato(FormatoReporte formato) {
        this.formato = formato;
    }
    // </editor-fold>

}
