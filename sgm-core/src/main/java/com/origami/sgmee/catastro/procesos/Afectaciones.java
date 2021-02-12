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
import com.origami.sgm.entities.models.Tipo;
import com.origami.sgm.events.AfectacionPost;
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.GeoProcesosException;
import com.origami.sgmee.catastro.geotx.GeoProcesosService;
import com.origami.sgmee.catastro.geotx.ModelPrediosTarea;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.ProcesoUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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
 * Controlador para el proceso de Afectacion que permite crear una ficha nueva
 * sobre el area afectada y actaulizar el area del predio afectado (Mantiene la
 * del predio que sufrio la afectacion.)
 *
 * @author dfcalderio
 */
@Named(value = "afectacionFlow")
@ViewScoped
public class Afectaciones extends BpmManageBeanBaseRoot implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(Afectaciones.class.getName());

    @Inject
    private ServletSession ss;

    @Inject
    private CatastroServices catastroService;

    protected HistoricoTramites ht;

    protected List<CatPredio> predios;
    protected CatPredio predio;
    protected List<HistoricoTramiteDet> detallesTramite;
    protected boolean renderCompletar;
    protected boolean finalizar = true;
    protected boolean volverEdicion = false;
    @Inject
    protected Event<AfectacionPost> event;
    @Inject
    protected Event<HistoricoPredioPost> eventHistorico;
    @Inject
    protected Event<ModelPrediosTarea> eventVerificarMz;

    @Inject
    private GeoProcesosService procesosService;
    @Inject
    private ProcesoServices procesos;

    private Boolean edicionGrafica;

    private Task currentTask;

    protected List<CatPredio> prediosTemp;

    @Inject
    private ProcesoServices procesoServices;

    private String observacion;

    private List<Observaciones> observaciones;

    private List<Observaciones> observacionesGenerales;
    protected String detalle;
    protected FormatoReporte formato;

    @PostConstruct
    public void initView() {
        renderCompletar = true;
        predios = new LinkedList<>();
        prediosTemp = new LinkedList<>();
        observaciones = new LinkedList<>();
        observacionesGenerales = new LinkedList<>();
        detallesTramite = new LinkedList<>();
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
            if (!edicionGrafica) {
                ht.getHistoricoTramiteDetCollection().stream().filter((d) -> (d.getPredio() != null)).map((d) -> {
                    if (d.getEstado() != null) {
                        if (d.getEstado().equals(Boolean.FALSE)) {
                            renderCompletar = false;
                        }
                    } else {
                        d.setEstado(Boolean.FALSE);
                        renderCompletar = false;
                    }
                    return d;
                }).map((d) -> {
                    if (d.getNumTasa() == null) {
                        d.setNumTasa(new BigInteger("2"));
                    }
                    return d;
                }).map((d) -> {
                    if (d.getNumTasa().intValue() == 2) {
                        finalizar = false;
                    }
                    return d;
                }).forEachOrdered((d) -> {
                    d.setObservaciones(ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), d.getPredio().getId()));
                    detallesTramite.add(d);
                });
            }
        } else {
            this.continuar();
        }
    }

    public void editarPredio(CatPredio p) {

        if (p.getId() != null) {
            ss.instanciarParametros();
            Map<String, Object> param = new HashMap<>();
            param.put("codigoNuevo", p.getClaveCat());
            GeoPrediosDivididos geoPredio = this.manager.findObjectByParameter(GeoPrediosDivididos.class, param);
            if (geoPredio != null) {
                ss.agregarParametro("idPrediosTx", geoPredio.getGid());
            }
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("formEdicion", currentTask.getFormKey());
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
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
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    /**
     * Completa la tarea grafica y la alfanumerica
     */
    public void completar() {
        HashMap<String, Object> paramt = new HashMap<>();
        paramt.put("prioridad", 50);
        if (currentTask.getTaskDefinitionKey().equals("editGrafica")) {
            try {
                detallesTramite = new LinkedList<>();
                ht.getHistoricoTramiteDetCollection().stream().filter((d) -> (d.getPredio() != null)).map((d) -> {
                    if (d.getEstado() != null) {
                        if (d.getEstado().equals(Boolean.FALSE)) {
                            renderCompletar = false;
                        }
                    } else {
                        d.setEstado(Boolean.FALSE);
                        renderCompletar = false;
                    }
                    return d;
                }).map((d) -> {
                    if (d.getNumTasa() == null) {
                        d.setNumTasa(new BigInteger("2"));
                    }
                    return d;
                }).map((d) -> {
                    if (d.getNumTasa().intValue() == 2) {
                        finalizar = false;
                    }
                    return d;
                }).forEachOrdered((d) -> {
                    detallesTramite.add(d);
                });
                if (Utils.isEmpty(detallesTramite) || detallesTramite.size() == 1) { // CUANDO NO ES LA PRIMERA TAREA DE EDICION GRAFICA
                    List<HistoricoTramiteDet> detsHt = new ArrayList<>();
                    for (HistoricoTramiteDet d : detallesTramite) {
                        GeoProcesoDivision division = null;
                        try {
                            division = this.procesosService.getProcesoDivisionData(d.getPredio().getClaveCat(), d.getPredio().getTipoPredio());
                        } catch (Exception exception) {
                            JsfUti.messageError(null, "Info.", exception.getMessage());
                            LOG.log(Level.SEVERE, "Dialog Division Predios", exception);
                            return;
                        }
                        GeoPrediosDivididos predioAfectado = null;
                        // recorremos la lista y eliminamos el predio afectado que es que tiene la numeracion 0 o vacia
                        for (GeoPrediosDivididos gpd : division.getPredios()) {
                            if (gpd.getNumeracion() == null || gpd.getNumeracion() == 0) {
                                predioAfectado = gpd;
                                // Actualizamos el area del solar y verifcamos calidades de suelo para el rural y ejes para urbano
                                if (predio.getTipoPredio().equalsIgnoreCase("U")) {
                                    d.getPredio().setAreaSolar(gpd.getArea());
                                    this.procesosService.verificarEjeValorGidPredioTx(predio, predio, gpd.getGid());
                                } else {
                                    d.getPredio().setAreaSolar(gpd.getArea().divide(BigDecimal.valueOf(10000L)));
                                    // VERIFICACION DE CALIDAD DEL SUELO RURAL
                                    // REGISTRA LAS CALIDADES SUELO ENCONTRADAS EN EL PREDIO
                                    if (procesosService.getActivarCalidadSueloRural()) {
                                        this.procesosService.verificarClasificacionsuelo(predio, gpd.getGid(), predio.getCatPredioClasificRuralCollection());
                                    }
                                }
                                // Actualizamos el area grafica
                                CatPredioS4 s4 = catastroService.getPredioS4ByPredio(d.getPredio());
                                s4.setAreaGraficaLote(gpd.getArea());
                                manager.update(s4);

                                manager.update(d.getPredio());
                                // eliminamos el registo que tiene numeracion 0 o nula
                                this.manager.delete(gpd);
                                break;
                            }
                        }
                        // removemos de la lista el predio afectado.
                        division.getPredios().remove(predioAfectado);
                        // predios resultantes afectacion nuevos
                        prediosTemp = procesos.dividirPredios(predio, division.getPredios());
                        // Agregamos los predios resultantes a historico tramites det
                        prediosTemp.stream().map((p) -> {
                            HistoricoTramiteDet detalleht = new HistoricoTramiteDet();
                            detalleht.setPredio(p);
                            detalleht.setNumTasa(BigInteger.valueOf(2L));
                            return detalleht;
                        }).map((detalleht) -> {
                            detalleht.setTramite(ht);
                            return detalleht;
                        }).forEachOrdered((detalleht) -> {
                            detsHt.add(detalleht);
                        });

                    } // FIN FOR LOOP detallesTramite
                    ht.setHistoricoTramiteDetCollection(detsHt);
                    // Actulizamos el historico tramites en cascada.
                    manager.update(ht);
                } else { // RECHAZADO
                    int index = 0;
                    GeoProcesoDivision division = this.procesosService.getProcesoDivisionData(this.predio.getClaveCat(), this.predio.getTipoPredio());
                    for (HistoricoTramiteDet hdt : detallesTramite) {
                        if (hdt.getPredio() != null) {
                            if (hdt.getNumTasa().compareTo(BigInteger.valueOf(2l)) == 0) {
                                GeoPrediosDivididos predioDiv = division.getPredios().get(index);
                                CatPredioS4 s4 = catastroService.getPredioS4ByPredio(hdt.getPredio());
                                s4.setAreaGraficaLote(predioDiv.getArea());
                                manager.update(s4);

                                if (hdt.getPredio().getTipoPredio().equalsIgnoreCase("R") || hdt.getPredio().getTipoPredio().contains("R")) {
                                    hdt.getPredio().setAreaSolar(predioDiv.getArea().divide(BigDecimal.valueOf(10000L)));
                                } else {
                                    hdt.getPredio().setAreaSolar(predioDiv.getArea());
                                }
                                this.manager.persist(hdt.getPredio());
                            }
                        }
                        index++;
                    }
                }
                JsfUti.messageInfo(null, "Info!", "Tarea " + currentTask.getName() + " completada con exito.");
            } catch (GeoProcesosException e) {
                JsfUti.messageError(null, "Info.", e.getMessage());
                LOG.log(Level.SEVERE, "Dialog Division Predios", e);
                return;
            } catch (Exception ex) {
                Logger.getLogger(Afectaciones.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        this.completeTask(session.getTaskID(), paramt);
        procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
        this.continuar();

    }

    /**
     * Completa la tarea de rrevision y asiente los poligonos en la capa
     * geo_solar
     */
    public void completarRevision() {

        try {
            Map<String, Object> pm = new HashMap<>();
            pm.put("numPredio", ht.getNumPredio());
            CatPredio predioraiz = this.manager.findObjectByParameter(CatPredio.class, pm);

            HashMap<String, Object> paramt = new HashMap<>();
            paramt.put("prioridad", 50);
            if (finalizar) {
                paramt.put("aprobado", 1);
                ht.setEstado("Finalizado");
                manager.persist(ht);
                // evento para realizar generar historico
//                eventHistorico.fire(new HistoricoPredioPost(predioraiz));
                actualizarEstadoPredio();
                AfectacionPost ev = new AfectacionPost();
                Map<String, Tipo> codigos = new HashMap<>();
                for (HistoricoTramiteDet hdt : detallesTramite) {
                    Tipo numeroPredioManzana = new Tipo(hdt.getPredio().getNumPredio(), null);
                    if (predio.getClaveCat().contains(hdt.getPredio().getClaveCat().substring(0, 13))) {
                        numeroPredioManzana.setId(null);
                    } else {
                        numeroPredioManzana.setId(Integer.valueOf(hdt.getPredio().getMz()));
                    }
                    codigos.put(hdt.getPredio().getClaveCat(), numeroPredioManzana);
                }
                ev.setCodigo(predio.getClaveCat());
                ev.setNumPredio(predio.getNumPredio());
                ev.setTipo(predio.getTipoPredio());
                ev.setCodigos(codigos);
                event.fire(ev);

                // Verificar Predios Modificados
                ModelPrediosTarea eventoVerif = new ModelPrediosTarea();
                eventoVerif.setCodigos(Arrays.asList(predio.getClaveCat()));
                eventoVerif.setTecnico(this.getVariable(this.getTaskId(), "dibujante").toString());
                eventVerificarMz.fire(eventoVerif);
            } else {
                paramt.put("aprobado", 0);
            }
            this.completeTask(session.getTaskID(), paramt);
            procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
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
        detalle = this.createInforme(detalle, ht, predio, formato);
    }

    /**
     * Genera el reporte en word
     */
    public void generarInforme() {
        if (Utils.isEmpty(detallesTramite)) {
            JsfUti.messageError(null, "Error!", "No se encontron predio para generar el reporte.");
            return;
        }
        if (Utils.isEmpty(prediosTemp)) {

            for (HistoricoTramiteDet det : detallesTramite) {
                if (det.getPredio() != null) {
                    prediosTemp.add(det.getPredio());
                }
            }
        }
        // Inicio de parametros de informe
        try {
            this.generarInforme(prediosTemp, detalle, formato, ss);
        } catch (Exception e) {
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

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
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

//</editor-fold>
}
