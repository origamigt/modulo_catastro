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
import com.origami.sgm.events.DivisionPrediosPost;
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.events.ValorarPredioPost;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.services.interfaces.catastro.FusionDivisionServices;
import com.origami.sgmee.catastro.geotx.GeoProcesosService;
import com.origami.sgmee.catastro.geotx.ModelPrediosTarea;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.ProcesoUtil;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
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
 * Controlador para el proceso de Fraccionamiento este proceso deja la clave
 * fraccionada en historico y crea las nuevas claves. (seran activadas una vez
 * ternimada el proceso)
 *
 * @author dfcalderio
 */
@Named(value = "divisionFlow")
@ViewScoped
public class Fraccionamiento extends BpmManageBeanBaseRoot implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = Logger.getLogger(Fraccionamiento.class.getName());

    @Inject
    private ServletSession ss;

    @Inject
    private CatastroServices catastroService;

    @Inject
    protected FusionDivisionServices divisionServices;

    protected HistoricoTramites ht;

    protected List<CatPredio> predios;
    protected CatPredio predio;
    protected List<HistoricoTramiteDet> detallesTramite;
    protected boolean renderCompletar;
    protected boolean finalizar = true;
    protected boolean volverEdicion = false;
    @Inject
    protected Event<DivisionPrediosPost> eventDivision;
    @Inject
    protected Event<HistoricoPredioPost> eventHistorico;
    @Inject
    protected Event<ModelPrediosTarea> eventVerificarMz;
    @Inject
    protected Event<ValorarPredioPost> eventValoracion;

    @Inject
    private GeoProcesosService procesosService;

    private Boolean edicionGrafica;

    private Task currentTask;

    protected List<CatPredio> prediosTemp;
    protected GeoProcesoDivision division;
    private List<Observaciones> observacionesGenerales;

    @Inject
    private ProcesoServices procesos;

    private String observacion;
    protected FormatoReporte formato;
    protected String detalle;

    @PostConstruct
    public void initView() {
        renderCompletar = true;
        predios = new LinkedList<>();
        prediosTemp = new LinkedList<>();
        detallesTramite = new LinkedList<>();
        if (session != null && session.getTaskID() != null) {
            ht = new HistoricoTramites();
            setTaskId(session.getTaskID());
            ht = this.getHistoricoTramiteById(new Long(this.getVariable(session.getTaskID(), "tramite").toString()));
            if (ht == null) {
                System.out.println("No se hayo tramite " + this.getVariable(session.getTaskID(), "tramite").toString());
                this.continuar();
            }
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
                        } else {
                            renderCompletar = true;
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
                    prediosTemp.add(d.getPredio());
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
            List<GeoPrediosDivididos> geoPredio = manager.findObjectByParameterOrderList(GeoPrediosDivididos.class, param, new String[]{"id"}, false);
            if (Utils.isNotEmpty(geoPredio)) {
                ss.agregarParametro("idPrediosTx", geoPredio.get(0).getGid());
            }
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("formEdicion", currentTask.getFormKey());
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void revisarPredio(CatPredio p) {

        if (p.getId() != null) {
            ss.instanciarParametros();
            params = new HashMap<>();
            params.put("codigoNuevo", p.getClaveCat());
            GeoPrediosDivididos geoPredio = this.manager.findObjectByParameter(GeoPrediosDivididos.class, params);
            if (geoPredio != null) {
                ss.agregarParametro("idPrediosTx", geoPredio.getGid());
            }
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("edit", false);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("formRevision", this.getVariable(this.getTaskId(), "formRevision"));
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void completar() {

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
                if (detallesTramite.isEmpty()) {
                    System.out.println("Creacion de predios en Fraccionamiento... " + this.predio.getClaveCat());
                    this.division = this.procesosService.getProcesoDivisionData(this.predio.getClaveCat(), this.predio.getTipoPredio());
                    prediosTemp = procesos.dividirPredios(predio, division.getPredios());
                    if (Utils.isNotEmpty(prediosTemp)) {
                        prediosTemp.stream().map((p) -> {
                            HistoricoTramiteDet detalle1 = new HistoricoTramiteDet();
                            detalle1.setPredio(p);
                            detalle1.setNumTasa(BigInteger.valueOf(2l));
                            return detalle1;
                        }).map((detalle2) -> {
                            detalle2.setTramite(ht);
                            return detalle2;
                        }).forEachOrdered((detalle3) -> {
                            detallesTramite.add(detalle3);
                        });
                        ht.setHistoricoTramiteDetCollection(detallesTramite);
                        manager.update(ht);
                    } else {
                        return;
                    }
                } else {
                    System.out.println("Ingreso rechazo de tarea edicion grafica... " + this.predio.getClaveCat());
                    int index = 0;
                    //Realiza la consulta en los nuevos Polígonos y hace las validaciones de Numeración.
                    //Ademas registra en la tabla GeoProcesoDivision y el detalle.
                    this.division = this.procesosService.getProcesoDivisionData(this.predio.getClaveCat(), this.predio.getTipoPredio());
                    for (HistoricoTramiteDet hdt : detallesTramite) {
                        if (hdt.getPredio() != null) {
                            if (index < this.division.getPredios().size()) {
                                GeoPrediosDivididos predioDiv = this.division.getPredios().get(index);
                                if (hdt.getNumTasa().compareTo(BigInteger.valueOf(2)) == 0) {
                                    CatPredioS4 s4 = catastroService.getPredioS4ByPredio(hdt.getPredio());
                                    s4.setAreaGraficaLote(predioDiv.getArea());
                                    manager.update(s4);

                                    if (hdt.getPredio().getTipoPredio().equalsIgnoreCase("R") || hdt.getPredio().getTipoPredio().contains("R")) {
                                        hdt.getPredio().setAreaSolar(predioDiv.getArea().divide(BigDecimal.valueOf(10000L)));
                                    } else {
                                        hdt.getPredio().setAreaSolar(predioDiv.getArea());
                                    }
                                    this.manager.persist(hdt.getPredio());
                                    if (predioDiv != null) {
                                        predioDiv.setCodigoNuevo(hdt.getPredio().getClaveCat());
                                        this.manager.persist(predioDiv);
                                    }
                                } else if (predioDiv.getCodigoNuevo() == null || predioDiv.getCodigoNuevo().length() > 20) {
                                    predioDiv.setCodigoNuevo(hdt.getPredio().getClaveCat());
                                    this.manager.persist(predioDiv);
                                }
                            }
                        }
                        index++;
//                        this.procesosService.verificarEjeValor(predio, hdt.getPredio());
                    }
                }

            } catch (Exception e) {
                JsfUti.messageError(null, "Info.", "No se ha realizado la division grafica del predio. " + e.getMessage());
                LOG.log(Level.SEVERE, "Dialog Division Predios", e);
                return;
            }

        } // FIN EDITAR GRAFICO
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
            CatPredio predioraiz = catastroService.getPredioNumPredio(ht.getNumPredio().longValue());
            HashMap<String, Object> paramt = new HashMap<>();
            paramt.put("prioridad", 50);
            if (Utils.isEmpty(ht.getHistoricoTramiteDetCollection())) {
                paramt.put("aprobado", 0);
//                ht.setEstado("Finalizado");
//                manager.persist(ht);
            } else {
                if (finalizar) {
                    paramt.put("aprobado", 1);
                    ht.setEstado("Finalizado");
                    manager.persist(ht);
                    eventHistorico.fire(new HistoricoPredioPost(predioraiz));
                    actualizarEstadoPredio();
                    DivisionPrediosPost divPos = new DivisionPrediosPost(predioraiz.getClaveCat());
                    List<String> codigos = new LinkedList<>();
                    Map<String, BigInteger> codigosf = new HashMap<>();
                    for (HistoricoTramiteDet d : ht.getHistoricoTramiteDetCollection()) {
                        codigos.add(d.getPredio().getClaveCat());
                        codigosf.put(d.getPredio().getClaveCat(), d.getPredio().getNumPredio());
                    }

                    divPos.setCodPrediosFinales(codigos);
                    divPos.setCodigos(codigosf);
                    divPos.setTipo(predioraiz.getTipoPredio());
                    eventDivision.fire(divPos);

                    // Verificar Predios Modificados
                    ModelPrediosTarea eventoVerif = new ModelPrediosTarea();
                    eventoVerif.setCodigos(codigos);
                    eventoVerif.setTecnico(this.getVariable(this.getTaskId(), "dibujante").toString());
                    eventVerificarMz.fire(eventoVerif);
                } else {
                    paramt.put("aprobado", 0);
                    for (HistoricoTramiteDet det : ht.getHistoricoTramiteDetCollection()) {
                        eventValoracion.fire(new ValorarPredioPost(det.getPredio().getClaveCat(), det.getPredio().getPredialant(), 3, det.getPredio().getTipoPredio()));
                    }
                }
            }

            this.completeTask(session.getTaskID(), paramt);
            procesos.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
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
        detalle = this.createInforme(detalle, ht, predio, formato);
    }

    public void generarInforme() {
        if (Utils.isEmpty(prediosTemp)) {
            JsfUti.messageError(null, "Error!", "No se encontron predio para generar el reporte.");
            return;
        }
        this.generarInforme(prediosTemp, detalle, formato, ss);
    }

    //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public List<Observaciones> getObservacionesGenerales() {
        return observacionesGenerales;
    }

    public void setObservacionesGenerales(List<Observaciones> observacionesGenerales) {
        this.observacionesGenerales = observacionesGenerales;
    }

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
    public void update() {

    }

}
