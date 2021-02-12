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
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.DefineQuerys;
import com.origami.sgmee.catastro.util.ProcesoUtil;
import java.io.Serializable;
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
 * Controlador para la tarea de Actualizacion de datos, que permite ingresar los
 * Bloque en una Ph hija.
 *
 * @author dfcalderio
 */
@Named(value = "updateDataFlow")
@ViewScoped
public class ActualizarDatos extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(ActualizarDatos.class.getName());
    @Inject
    private ServletSession ss;

    @Inject
    private CatastroServices catastroService;

    @Inject
    private ProcesoServices procesoServices;

    protected HistoricoTramites ht;

    protected CatPredio predio;
    protected List<HistoricoTramiteDet> detallesTramite;
    protected boolean renderCompletar;
    protected boolean finalizar = true;
    protected boolean volverEdicion = false;
    @Inject
    protected Event<HistoricoPredioPost> eventHistorico;

    private Boolean edicionGrafica;

    private Task currentTask;

    protected List<CatPredio> prediosTemp;
    protected GeoProcesoDivision division;

    private String observacion;
    protected String detalle;
    protected FormatoReporte formato;

    private List<Observaciones> observaciones;

    private List<Observaciones> observacionesGenerales;

    @PostConstruct
    public void initView() {
        renderCompletar = false;
        prediosTemp = new LinkedList<>();
        detallesTramite = new LinkedList<>();
        observaciones = new LinkedList<>();
        observacionesGenerales = new LinkedList<>();
        if (session != null && session.getTaskID() != null) {
            ht = new HistoricoTramites();
            setTaskId(session.getTaskID());
            ht = this.getHistoricoTramiteById(new Long(this.getVariable(session.getTaskID(), "tramite").toString()));

            observacionesGenerales = ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), null);

            List<Task> tasks = engine.getTasksUserProcessId(session.getName_user(), ht.getIdProceso());
            if (!tasks.isEmpty()) {
                currentTask = tasks.get(0);

                edicionGrafica = Boolean.FALSE;
            }
            ht.getHistoricoTramiteDetCollection().stream().filter((d) -> (d.getPredio() != null)).map((d) -> {
                renderCompletar = (d.getNumTasa().equals(BigInteger.ONE) && d.getEstado());
                predio = d.getPredio();
                return d;
            }).map((d) -> {
                if (d.getNumTasa().intValue() == 2) {
                    finalizar = false;
                }
                predio = d.getPredio();
                return d;
            }).forEachOrdered((d) -> {
                d.setObservaciones(ProcesoUtil.getObservacionesDetalle(ht.getObservacionesCollection(), d.getPredio().getId()));
                detallesTramite.add(d);
                predio = d.getPredio();
            });
            this.createInforme();
        } else {
            this.continuar();
        }
    }

    public void editarPredio(CatPredio p) {

        if (p.getId() != null) {
            ss.instanciarParametros();
            ss.agregarParametro("numPredio", p.getNumPredio());
            ss.agregarParametro("idPredio", p.getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("proceso", TipoProceso.ACTUALIZAR_DATOS);
            ss.agregarParametro("formEdicion", currentTask.getFormKey());
            ss.agregarParametro("taskName", currentTask.getName());
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
            ss.agregarParametro("proceso", TipoProceso.ACTUALIZAR_DATOS);
            ss.agregarParametro("taskName", currentTask.getName());
            ss.agregarParametro("formRevision", this.getVariable(this.getTaskId(), "formRevision"));
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }

    }

    public void completar() {

        HashMap<String, Object> paramt = new HashMap<>();
        paramt.put("prioridad", 50);
        this.completeTask(session.getTaskID(), paramt);
        ht.setObservacion(detalle);
        this.manager.persist(ht);
        procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
        JsfUti.messageInfo(null, "Info", "Tarea completada con exito.");

        this.continuar();

    }

    public void completarRevision() {
        HashMap<String, Object> paramt = new HashMap<>();
        try {
            paramt.put("prioridad", 50);
            if (finalizar) {
                paramt.put("aprobado", true);
                ht.setEstado("Finalizado");
                manager.persist(ht);
                if (predio == null) {
                    System.out.println("Predio es Nullo...");
                }

                CatPredio p = procesoServices.asentarCambiosData(predio, ProcesoUtil.getAllFilesRelationsShip(predio));
                // DEJAMOS ESTA VALIDACION PARA LOS PROCCESOS INICIADOS Y QUE AUN ESTAN PENDIENTES.
                if (p != null) {
                    procesoServices.prepararUsosPredio(predio);
                    for (HistoricoTramiteDet det : ht.getHistoricoTramiteDetCollection()) {
                        System.out.println("Id predio en el tramite antes de update: " + det.getPredio().getId());
                        det.setPredio(p);
                        det = procesoServices.updateDetalle(det);
                        System.out.println("Id predio en el tramite despues de update: " + det.getPredio().getId());
                    }
                    manager.executeNativeQuery(DefineQuerys.deleteEscritura, new Object[]{predio.getId()});
                    manager.executeNativeQuery(DefineQuerys.deleteEscrituraPropietario, new Object[]{predio.getId()});
                    if (predio.getCatPredioS6() != null) {
                        manager.executeNativeQuery(DefineQuerys.deleteUsos, new Object[]{predio.getCatPredioS6()});
                    }
                    predio.setCatPredioEdificacionCollection(null);
                    predio.setCatPredioPropietarioCollection(null);
                    manager.executeNativeQuery(DefineQuerys.deletePredioProppietarios, new Object[]{predio.getId()});
                    if (predio.getCatPredioS4() != null) {
                        manager.executeNativeQuery(DefineQuerys.deletePredioS4, new Object[]{predio.getId()});
                        predio.setCatPredioS4(null);
                    }
                    if (predio.getCatPredioS6() != null) {
                        manager.executeNativeQuery(DefineQuerys.deletePredioS6, new Object[]{predio.getId()});
                        predio.setCatPredioS6(null);
                    }
                    if (predio.getCatPredioS12() != null) {
                        manager.delete(predio.getCatPredioS12());
                    }
                    manager.delete(predio);
                }
            } else {
                paramt.put("aprobado", false);
            }
            this.completeTask(session.getTaskID(), paramt);
            procesoServices.guardarObservaciones(ht.getIdTramite(), session.getName_user(), observacion, currentTask.getName(), null);
            JsfUti.messageInfo(null, "Info", "Tarea completada con exito.");
            this.continuar();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "completarRevision", e);
            JsfUti.messageError(null, "Error", "Error al procesar la informacion..");
        }

    }

    public String mensajeConfirmarRevision() {
        String claves = "";
        int cont = 0;
        for (HistoricoTramiteDet dt : detallesTramite) {
            if (dt.getNumTasa() != null) {
                if (dt.getNumTasa().compareTo(new BigInteger("2")) == 0) {
                    if (cont == 0) {
                        claves += claveVerificada(dt.getPredio());
                    } else {
                        claves += ", " + claveVerificada(dt.getPredio());
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
                claveCat = this.claveCatastral(claveCat);
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

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "generarInforme", e);
        }
    }

    public void rechazarEdicion() {

        this.continuar();

    }

    public String claveCatastral(String clave) {
        String b = clave.replaceFirst("99", "10");
        return b;
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

    public String claveVerificada(CatPredio p) {
        return ProcesoUtil.claveCatastralCompleta(predio);
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
