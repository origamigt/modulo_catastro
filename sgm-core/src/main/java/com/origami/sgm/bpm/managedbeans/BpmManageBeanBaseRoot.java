/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans;

import com.origami.censocat.restful.JsonUtils;
import com.origami.config.MainConfig;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.models.DatosTarea;
import com.origami.sgm.bpm.models.DetalleProceso;
import com.origami.sgm.bpm.models.ParametrosProceso;
import com.origami.sgm.bpm.models.ResTareasUsuarios;
import com.origami.sgm.bpm.models.TareaWF;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.EnteCorreo;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.GeRequisitosTramite;
import com.origami.sgm.entities.GeTipoTramite;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.Observaciones;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.services.bpm.BpmBaseEngine;
import com.origami.sgm.services.interfaces.SeqGenMan;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.apache.chemistry.opencmis.client.api.Document;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import util.ApplicationContextUtils;
import util.Archivo;
import util.CmisUtil;
import util.Faces;
import util.JsfUti;
import util.Messages;
import util.MimeTypes;
import util.Utils;

/**
 *
 * @author Fernando
 */
public abstract class BpmManageBeanBaseRoot implements Serializable {

    @javax.inject.Inject
    protected BpmBaseEngine engine;

    @javax.inject.Inject
    protected Entitymanager manager;
    @javax.inject.Inject
    private SeqGenMan secuenciaReporte;

    @Inject
    protected UserSession session;

    protected Map<String, Object> params;

    private ArrayList<Archivo> files = new ArrayList<>();
    private ArrayList<Archivo> cartaAdosamiento = new ArrayList<>();
    private List<GeRequisitosTramite> requisitos;
    private String taskId;
    private HistoricoTramites bpmTramite;
    private List<Attachment> listAttachment = new ArrayList<>();
    private String nuevoCorreo;
    private String correosAdjuntos;

    private CmisUtil cmis;
    private static final long serialVersionUID = 5L;

    /**
     * Llama al metodo que genera el numero de secuencia para los tramites.
     *
     * @return Numero de tramite
     */
    public synchronized Long generarIdTramite() {
        return secuenciaReporte.getSecuenciasTram("SGM");
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ProcessEngine getProcessEngine() {
        return this.engine.getProcessEngine();
    }

    public String obtenerFormKey(String processId) {
        return this.engine.getProcessEngine().getFormService().getStartFormData(processId).getFormKey();
    }

    public String obtenerKeyProceso(String processId) {
        return this.engine.getProcessKey(processId);
    }

    public void loadProcessByClassPath(String path) {
        this.engine.loadSingleProcessByClassPath(path);
    }

    public List<ProcessDefinition> getProcesosDesplegados() {
        return this.engine.getProcessDesployedList();
    }

    public Map<String, String> getProcesos() {
        Map<String, String> lproc = new HashMap<>();
        for (ProcessDefinition p : this.getProcesosDesplegados()) {
            lproc.put(p.getName(), p.getName());
        }
        return lproc;
    }

    public String getCorreosByCatEnte(CatEnte persona) {
        String s = "";
        int n = 1;
        if (persona == null) {
            return null;
        }
        if ((persona.getEnteCorreoCollection() == null) || persona.getEnteCorreoCollection().isEmpty()) {
            return null;
        }
        for (EnteCorreo ec : persona.getEnteCorreoCollection()) {
            if (persona.getEnteCorreoCollection().size() > n) {
                if (ec.getEmail() != "") {
                    s = s + ec.getEmail() + ",";
                }
            } else {
                if (ec.getEmail() != "") {
                    s = s + ec.getEmail();
                }
            }
            n++;
        }
        if (s.equals("")) {
            return null;
        }

        return s;
    }

    public String generadorCeroALaIzquierda(Long n) {
        int cont = 0;
        Long num = n;
        String salida = "";
        while (num > 0) {
            num = num / 10;
            cont++;
        }
        for (int i = 0; i < (6 - cont); i++) {
            salida = salida + "0";
        }
        salida = salida + n;
        return salida;
    }

    // public List<DetalleProceso> getInstanciaProcesos(String key) {
    public List<DetalleProceso> getInstanciaProcesos() {
        List<DetalleProceso> ldet = new ArrayList<>();
        DetalleProceso det;
        HistoricoTramites ht;
        List<HistoricProcessInstance> h = this.engine.getProcessInstanceHistoric();
        try {
            for (HistoricProcessInstance hpi : h) {
                ProcessDefinition p = this.engine.getProcessDataByDefID(hpi.getProcessDefinitionId());
                ProcessInstance pi = this.engine.getProcessInstanceById(hpi.getId());
                det = new DetalleProceso();
                ht = (HistoricoTramites) this.manager.findNoProxy(Querys.getHistoricProceduresByProcId,
                        new String[]{"idprocess"}, new Object[]{hpi.getId()});
                if (ht == null) {
                    ht = (HistoricoTramites) this.manager.findNoProxy(Querys.getHistoricProceduresByProcIdTemp,
                            new String[]{"idprocess"}, new Object[]{hpi.getId()});
                }
                if (ht != null) {
                    if (p.getName() != null) {
                        det.setNombreProceso(p.getName() + " (" + ht.getId() + ")");
                    } else {
                        det.setNombreProceso(p.getKey() + " (" + ht.getId() + ")");
                    }
                    if (pi != null) {
                        det.setInstancia(pi.getId());
                    }
                    if (ht.getId() != null) {
                        det.setIdProceso(ht.getId().toString());
                    }
                    det.setFechaInicio(hpi.getStartTime());
                    det.setFechaFin(hpi.getEndTime());
                    det.setTasks(this.engine.getTaskByProcessInstanceId(hpi.getId()));
                    ldet.add(det);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
        return ldet;
    }

    public ParametrosProceso ObtenerParametrosDelProcesoContratacionPublica(String idtarea) throws Exception {
        ParametrosProceso p = new ParametrosProceso();
        DatosTarea param = this.obternerDatosTareaProcesoContratacionPublica(idtarea);
        p.setNombDepartamento(param.getNomDepartamento());
        p.setNombSolicitante(param.getNombSolicitante());
        p.setNumTramite(param.getNumero_TramiteNuevo());
        p.setObservacionRequerimiento(param.getObservacionRequerimiento());
        return p;
    }

    public DatosTarea obternerDatosTareaProcesoContratacionPublica(String tareaId) {
        RuntimeService runtimeService = this.engine.getProcessEngine().getRuntimeService();
        Task tarea = this.engine.getTaskDataByTaskID(tareaId);

        DatosTarea datos = new DatosTarea();
        String observacionRequerimiento = (String) runtimeService.getVariable(tarea.getProcessInstanceId(),
                "observacionDirector");
        String nombreDepartamento = (String) runtimeService.getVariable(tarea.getProcessInstanceId(), "departamento");
        String nombreSolicitante = (String) runtimeService.getVariable(tarea.getProcessInstanceId(),
                "nombreSolicitante");
        String numeroTramite = (String) runtimeService.getVariable(tarea.getProcessInstanceId(), "numeroTramite");
        datos.setObservacionRequerimiento(observacionRequerimiento);
        datos.setNomDepartamento(nombreDepartamento);
        datos.setNombSolicitante(nombreSolicitante);
        datos.setNumero_TramiteNuevo(numeroTramite);

        return datos;
    }

    public String getProcessInstanceByTareaID(String tareaID) {
        if (tareaID != null) {
            Task tarea = this.engine.getTaskDataByTaskID(tareaID);
            if (tarea != null) {
                return tarea.getProcessInstanceId();
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public Integer getCantidadTareasUser(String usuario) {
        return this.engine.getNumberTasksUser(usuario);
    }

    public List<TareaWF> getListaTareasPersonales(String usuario, String keyTask) {
        ArrayList<Task> tareasAct = (ArrayList<Task>) this.engine.getUsertasksList(usuario, keyTask);
        ArrayList<Task> temp = (ArrayList<Task>) this.engine.getCandidateUsertasksList(usuario);
        for (Task t : temp) {
            tareasAct.add(t);
        }
        List<TareaWF> tareasWF = new ArrayList<>();
        HistoricProcessInstance p;
        HistoricoTramites historicoTramites1;
        try {
            if (tareasAct.size() > 0) {
                for (Task task : tareasAct) {
                    p = this.engine.getHistoricProcessInstanceByInstanceID(task.getProcessInstanceId());
                    if ((p != null) && (p.getSuperProcessInstanceId() != null)) {
                        historicoTramites1 = (HistoricoTramites) this.manager.find(Querys.getHistoricProceduresByProcId,
                                new String[]{"idprocess"}, new Object[]{p.getSuperProcessInstanceId()});
                        if (historicoTramites1 == null) {
                            historicoTramites1 = (HistoricoTramites) this.manager.find(
                                    Querys.getHistoricProceduresByProcIdTemp, new String[]{"idprocess"},
                                    new Object[]{p.getSuperProcessInstanceId()});
                            if (historicoTramites1 != null) {
                                historicoTramites1.setIdProceso(p.getId());
                                historicoTramites1.setCarpetaRep(historicoTramites1.getId() + "-" + p.getId());
                                this.manager.persist(historicoTramites1);
                            }
                        }
                    } else {
                        historicoTramites1 = (HistoricoTramites) this.manager.find(Querys.getHistoricProceduresByProcId,
                                new String[]{"idprocess"}, new Object[]{task.getProcessInstanceId()});

                        if (historicoTramites1 == null) {
                            historicoTramites1 = (HistoricoTramites) this.manager.find(
                                    Querys.getHistoricProceduresByProcIdTemp, new String[]{"idprocess"},
                                    new Object[]{task.getProcessInstanceId()});

                        }
                    }
                    if (historicoTramites1 != null) {
                        TareaWF tareawf = new TareaWF();
                        tareawf.setTarea(task);
                        if (task.getDescription() != null) {
                            if (task.getDescription().length() > 50) {
                                tareawf.setDescripcionTareaMayor50char(true);
                            } else {
                                tareawf.setDescripcionTareaMayor50char(false);
                            }
                        }
                        if (historicoTramites1.getMz() == null) {
                            historicoTramites1.setMz("0");
                        }
                        if (historicoTramites1.getSolar() == null) {
                            historicoTramites1.setSolar("0");
                        }
                        /*
						 * if (historicoTramites1.getFecha() != null) { Calendar calendar =
						 * Calendar.getInstance(); calendar.setTime(historicoTramites1.getFecha());
						 * Integer dia, mes, anio; dia = calendar.get(Calendar.DAY_OF_MONTH); mes =
						 * calendar.get(Calendar.MONTH) + 1; anio = calendar.get(Calendar.YEAR);
						 * historicoTramites1.setFechaCorrecta(dia.toString() + "/" + mes.toString() +
						 * "/" + anio.toString()); }
                         */
                        tareawf.setTramite(historicoTramites1);
                        // tareawf.setTramiteRp(Long.parseLong(serv.find(Querys.getProcedureNumberById,
                        // new String[]{"tramite"}, new
                        // Object[]{historicoTramites1.getId()}).toString()));
                        tareasWF.add(tareawf);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
        return tareasWF;
    }

    public List<Task> obtenerTareasUsuarios(String usuario) {
        try {
            return this.engine.getUsertasksList(usuario, null);
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public List<HistoricTaskInstance> obtenerTareasUsuariosCompletadas() {
        return this.engine.getProcessEngine().getHistoryService().createHistoricTaskInstanceQuery().finished().list();
    }

    public Boolean reasignarTarea(String id, String usuario) throws Exception {
        return this.engine.setAssigneeTask(id, usuario);
    }

    public void asignarTareaPriority(List<Task> tareas, Integer prioridad) throws Exception {
        for (Task t : tareas) {
            this.engine.setTaskPriority(t.getId(), prioridad);
        }
    }

    public Task obtenerTarea(String id) {
        return this.engine.getTaskDataByTaskID(id);
    }

    /**
     * Lista los Archivo que a tenido el flujo durante lo que va del proceso.
     *
     * @param id_proceso
     * @return
     */
    public List<Archivo> documentosAdjuntados(String id_proceso) {
        List<Archivo> archi = (List<Archivo>) this.engine.getProcessEngine().getRuntimeService().getVariable(id_proceso,
                "listaArchivosFinal");
        return archi;
    }

    /**
     * Lista los Archivo que a tenido el flujo, cuando este esta finalizado.
     *
     * @param id_proceso
     * @return
     */
    public List<Archivo> documentosAdjuntadosTramitesFinalizados(String id_proceso) {
        List<Archivo> archi = new ArrayList<>();
        List<HistoricVariableInstance> variableInstanceQuerys = this.engine.getProcessEngine().getHistoryService()
                .createHistoricVariableInstanceQuery().processInstanceId(id_proceso).variableName("listaArchivosFinal")
                .list();
        for (HistoricVariableInstance acc : variableInstanceQuerys) {
            archi = (List<Archivo>) acc.getValue();
        }
        return archi;
    }

    public boolean validarEntradaDeRequisitos() {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        if (!this.files.isEmpty()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Mensaje",
                    "Se ingresaron los requisitos con éxito!");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
            return true;
        } else {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Mensaje",
                    "No ingresó correctamente los requisitos.");
            RequestContext.getCurrentInstance().showMessageInDialog(message);
            return false;
        }
    }

    public Boolean validaFiles() {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        if (this.files.isEmpty()) {
            JsfUti.messageWarning(null, "Debe cargar los Documentos.", "");
            return false;
        } else {
            return true;
        }
    }

    public Boolean validaUnArchivo() {
        if (this.files == null) {
            this.files = new ArrayList<>();
        }
        if (this.files.size() != 1) {
            JsfUti.messageWarning(null, "Debe cargar solo un Documento.", "");
            return false;
        } else {
            return true;
        }
    }

    public void handleFileUpload(FileUploadEvent event) {
        FacesContext.getCurrentInstance();
        try {
            Date d = new Date();
            String rutaArchivo = SisVars.rutaRepotiorioArchivo + d.getTime() + event.getFile().getFileName();
            File file = new File(rutaArchivo);
            InputStream is;
            is = event.getFile().getInputstream();
            OutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            is.close();
            out.close();
            Archivo documento = new Archivo();
            documento.setNombre(d.getTime() + "_" + event.getFile().getFileName());
            if (this.bpmTramite != null /* && this.bpmTramite.getId()!=null && this.bpmTramite.getSolicitante()!=null */) {
                // VERIFICAR QUE NO EXISTAN CARACTERES ESPECIALES PARA LOS ARCHIVOS (PUNTOS,
                // COMAS, DOBLE COMILLAS)
                String nombreSolicitante = "";
                if (this.bpmTramite.getSolicitante().getApellidos() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getApellidos() + "_";
                }
                if (this.bpmTramite.getSolicitante().getNombres() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getNombres() + "_";
                }
                if (this.bpmTramite.getSolicitante().getRazonSocial() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getRazonSocial() + "_";
                }
                if (this.bpmTramite.getSolicitante().getNombreComercial() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getNombreComercial() + "_";
                }
                nombreSolicitante = nombreSolicitante.replace('.', '_');
                nombreSolicitante = nombreSolicitante.replace(',', '_');
                nombreSolicitante = nombreSolicitante.replace('\"', '_');
                documento.setNombre(nombreSolicitante + "_" + d.getTime() + "_" + event.getFile().getFileName());
            }
            documento.setTipo(event.getFile().getContentType());
            documento.setRuta(rutaArchivo);
            MimeTypes.registerMimeType(event.getFile().getContentType(),
                    MimeTypes.getExtension(event.getFile().getFileName()));
            this.files.add(documento);

        } catch (IOException e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void handleFileUploadCarta(FileUploadEvent event) {
        FacesContext.getCurrentInstance();
        try {
            Date d = new Date();
            String rutaArchivo = SisVars.rutaRepotiorioArchivo + d.getTime() + event.getFile().getFileName();
            File file = new File(rutaArchivo);
            InputStream is;
            is = event.getFile().getInputstream();
            OutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            is.close();
            out.close();
            Archivo documento = new Archivo();
            documento.setNombre(d.getTime() + "_" + event.getFile().getFileName());
            if (this.bpmTramite != null) {
                // VERIFICAR QUE NO EXISTAN CARACTERES ESPECIALES PARA LOS ARCHIVOS (PUNTOS,
                // COMAS, DOBLE COMILLAS)
                String nombreSolicitante = "";
                if (this.bpmTramite.getSolicitante().getApellidos() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getApellidos() + "_";
                }
                if (this.bpmTramite.getSolicitante().getNombres() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getNombres() + "_";
                }
                if (this.bpmTramite.getSolicitante().getRazonSocial() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getRazonSocial() + "_";
                }
                if (this.bpmTramite.getSolicitante().getNombreComercial() != null) {
                    nombreSolicitante = nombreSolicitante + this.bpmTramite.getSolicitante().getNombreComercial() + "_";
                }
                nombreSolicitante = nombreSolicitante.replace('.', '_');
                nombreSolicitante = nombreSolicitante.replace(',', '_');
                nombreSolicitante = nombreSolicitante.replace('\"', '_');
                documento.setNombre(nombreSolicitante + "_" + d.getTime() + "_" + event.getFile().getFileName());
            }
            documento.setTipo(event.getFile().getContentType());
            documento.setRuta(rutaArchivo);
            MimeTypes.registerMimeType(event.getFile().getContentType(),
                    MimeTypes.getExtension(event.getFile().getFileName()));
            this.cartaAdosamiento.add(documento);
            JsfUti.messageInfo(null, "Se Agrego Correctamente la carta de adosamiento.", "");

        } catch (IOException e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void agregarCartaAdosamieto() {
        if (Utils.isNotEmpty(this.cartaAdosamiento)) {
            this.files.addAll(this.cartaAdosamiento);
        }
    }

    public List<GeRequisitosTramite> getRequisitos(GeTipoTramite tp, boolean estado, String key) {
        try {
            List<GeRequisitosTramite> list = new ArrayList<>();
            if ((tp != null) && (tp.getGeRequisitosTramiteCollection() != null)) {
                this.requisitos = (List<GeRequisitosTramite>) tp.getGeRequisitosTramiteCollection();
                for (GeRequisitosTramite temp : this.requisitos) {
                    if (temp.getEstado() != null) {
                        if (temp.getEstado().equals("A")) {
                            list.add(temp);
                        }
                    }
                }
                this.requisitos = list;
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
        return this.requisitos;
    }

    public ProcessInstance startProcessByDefinitionKey(String processDefinitionKey,
            HashMap<String, Object> parameters) {
        ProcessInstance f;
        try {
            f = this.engine.startProcessByDefinitionKey(processDefinitionKey, parameters);
        } catch (Exception e) {
            f = null;
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
        return f;
    }

    public Task getTaskDataByTaskID(String taskId) {
        return this.engine.getTaskDataByTaskID(taskId);
    }

    public Task getTaskDataByTaskID() {
        if (this.taskId != null) {
            return this.engine.getTaskDataByTaskID(this.taskId);
        }
        return null;
    }

    public void completeTask(String taskid, HashMap<String, Object> parameters) {
        try {
            this.engine.completeTask(taskid, parameters);
        } catch (Exception ex) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void redirecPag(TareaWF tarea) {
        this.session.setTaskID(tarea.getTarea().getId());
        JsfUti.redirectFaces(tarea.getTarea().getFormKey());
    }

    public void redirectLogin() {
        JsfUti.redirectFaces2(SisVars.urlServidorWorkFlowPublica + "/faces/login.xhtml");
    }

    public void continuar() {
        JsfUti.redirectFaces("/vistaprocesos/dashBoard.xhtml");
    }

    public void eliminarFile(Archivo doc) {
        if (Utils.isNotEmpty(this.files)) {
            this.files.remove(doc);
        }
    }

    public void eliminarCartaAdosamiento(Archivo doc) {
        if (Utils.isNotEmpty(this.cartaAdosamiento)) {
            this.cartaAdosamiento.remove(doc);
        }
    }

    public boolean emptyCartaAdos() {
        return Utils.isEmpty(this.cartaAdosamiento);
    }

    public String getMensaje() {
        if (this.emptyCartaAdos()) {
            return "Esta seguro de guardar la liquidación sin la carta de adosamiento.";
        } else {
            return "Se ha adjuntado carta de adosamiento a la liquidación.";
        }
    }

    public ArrayList<Archivo> getFiles() {
        return this.files;
    }

    public void setFiles(ArrayList<Archivo> files) {
        this.files = files;
    }

    public UserSession getSession() {
        return this.session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    public List<Attachment> getAttachmentsFiles() {
        return this.engine.getAttachmentsFiles(this.getTaskId());
    }

    public List<Attachment> getAttachmentsFiles(String taskId) {
        return this.engine.getAttachmentsFiles(taskId);
    }

    public Object getVariable(String taskid, String varName) {
        return this.engine.getvariable(taskid, varName);
    }

    public Object getVariableByPorcessIntance(String processInstanceId, String varName) {
        return this.engine.getVariableByProcessInstance(processInstanceId, varName);
    }

    public void setVariableByProcessInstance(String processInstanceId, String varName, Object value) {
        this.engine.setVariableProcessInstance(processInstanceId, varName, value);
    }

    public List<HistoricTaskInstance> getEndedUsertasksList(String asignee) {
        return this.engine.getEndedUsertasksList(asignee);
    }

    public ResTareasUsuarios getResumenTareasUsuarios(String usuario, String mail) {
        ResTareasUsuarios res = new ResTareasUsuarios();
        try {
            res.setUser(usuario);
            res.setMailUser(mail);
            res.setCompletedTasks(this.getEndedUsertasksList(usuario).size());
            res.setCurrentTasks(this.obtenerTareasUsuarios(usuario).size());
            res.setTasks(this.getListaTareasPersonales(usuario, null));
        } catch (Exception ex) {
            res = null;
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public ResTareasUsuarios getResumenTareasUsuariosNew(String usuario, String mail) {
        ResTareasUsuarios res = new ResTareasUsuarios();
        try {
            res.setUser(usuario);
            res.setMailUser(mail);
            res.setCompletedTasks(this.getEndedUsertasksList(usuario).size());
            res.setCurrentTasks(this.obtenerTareasUsuarios(usuario).size());
        } catch (Exception ex) {
            res = null;
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public List<HistoricTaskInstance> getTaskByProcessInstanceId(String processInstanceId) {
        return this.engine.getTaskByProcessInstanceId(processInstanceId);
    }

    public List<HistoricTaskInstance> getTaskByProcessInstanceIdMain(String processInstanceId) {
        return this.engine.getTaskByProcessInstanceIdMain(processInstanceId);
    }

    public List<Attachment> getProcessInstanceAttachmentsFiles(String processInstanceId) {
        return this.engine.getProcessInstanceAttachmentsFiles(processInstanceId);
    }

    public List<Attachment> getProcessInstanceAllAttachmentsFiles(String processInstanceId) {
        return this.engine.getAttachmentsFilesByProcessInstanceIdMain(processInstanceId);
    }

    public List<Attachment> getProcessInstanceAttachmentsFiles() {
        if (this.getTaskDataByTaskID() != null) {
            return this.engine.getProcessInstanceAttachmentsFiles(this.getTaskDataByTaskID().getProcessInstanceId());
        } else {
            return null;
        }
    }

    public List<Attachment> getProcessInstanceAllAttachmentsFiles() {
        if (this.getTaskDataByTaskID() != null) {
            Long idHistTram = (Long) this.getVariable(this.session.getTaskID(), "tramite");
            HistoricoTramites ht = (HistoricoTramites) this.manager.find(Querys.getHistoricoTramiteById,
                    new String[]{"id"}, new Object[]{idHistTram});
            return this.engine.getAttachmentsFilesByProcessInstanceIdMain(ht.getIdProceso());
        } else {
            return null;
        }
    }

    public List<Attachment> getProcessInstanceAllAttachments() {
        if (this.getTaskDataByTaskID() != null) {
            Long idHistTram = (Long) this.getVariable(this.session.getTaskID(), "tramite");
            HistoricoTramites ht = (HistoricoTramites) this.manager.find(Querys.getHistoricoTramiteById,
                    new String[]{"id"}, new Object[]{idHistTram});
            return this.engine.getAttachmentsFilesByProcessInstanceIdMain(ht.getIdProcesoTemp());
        } else {
            return null;
        }
    }

    public List<Attachment> getProcessInstanceAllAttachments(Long id) {
        HistoricoTramites ht = (HistoricoTramites) this.manager.find(Querys.getHistoricoTramiteById, new String[]{"id"},
                new Object[]{id});
        return this.engine.getAttachmentsFilesByProcessInstanceIdMain(ht.getIdProcesoTemp());
    }

    public Object getvariableByExecutionId(String taskId, String varName) {
        return this.engine.getvariableByExecutionId(taskId, varName);
    }

    public Document attachDocument(String carpeta, String fileName, String mimetype, byte[] content) {
        CmisUtil cmis;
        try {
            cmis = (CmisUtil) ApplicationContextUtils.getBean("cmisUtil");
            if (cmis != null) {
                return cmis.createDocument(cmis.getFolder(carpeta), fileName, mimetype, content);
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public void showDocument(String url) {
        if ((url != null) && (url.trim().length() > 0)) {
            JsfUti.redirectNewTab(SisVars.urlbase + "/FotosServlet?fotoId=" + url);
        }
    }

    // showDocuments?fotoId=#{adjunto.url}&&type=#{adjunto.type}
    public void showDocuments(String url, String type) {
        if ((url != null) && (url.trim().length() > 0)) {
            JsfUti.redirectNewTab(SisVars.urlbase + "showDocuments?idDoc=" + url + "&type=" + type);
        }
    }

    public void descargarDocumento(String url) {
        try {
            if ((url != null) && (url.trim().length() > 0)) {
                JsfUti.redirectNewTab(SisVars.urlbase + "DescargarDocsRepositorio?idDoc=" + url + "&type=pdf");
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void descargarDocumento(String url, String type) {
        try {
            if ((url != null) && (url.trim().length() > 0)) {
                JsfUti.redirectNewTab(SisVars.urlbase + "DescargarDocsRepositorio?idDoc=" + url + "&type=" + type);
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBase.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void mostrarDocumento(String url) {
        String s[] = url.split("nodeRef=");
        Faces.redirectNewTab(com.origami.config.SisVars.urlbase + "AbrirDocsRepositorio?id=" + s[1]);
    }

    public List<Task> obtenerTareasActivasProcessInstance(String processInstanceId) {
        return this.engine.getListTaskActiveByProcessInstance(processInstanceId);
    }

    public List<IdentityLink> obtenerIdentityLinkByIdTask(String taskId) {
        return this.engine.identityLinkPorTareaId(taskId);
    }

    public List<HistoricIdentityLink> obtenerHistoricIdentityLinkByIdTask(String taskId) {
        return this.engine.HistoricidentityLinkPorTareaId(taskId);
    }

    public List<String> obtenerProcessInstanceByProcessInstaceIdMain(String processInstaceId) {
        return this.engine.getListProcessInstanceIdsByProcessInstanceIdMain(processInstaceId);
    }

    public List<Attachment> getListAttachment() {
        return this.listAttachment;
    }

    public void setListAttachment(List<Attachment> listAttachment) {
        this.listAttachment = listAttachment;
    }

    public List<Attachment> getListAttAttachmentByProcessInstance() {
        String processInstance = this.getProcessInstanceByTareaID(this.taskId);
        return this.getProcessInstanceAllAttachmentsFiles(processInstance);
    }

    public Object getVariables(String taskId, String variable) {
        if (taskId != null) {
            Map<String, Object> v = this.engine.getVar(taskId);
            for (Map.Entry<String, Object> entrySet : v.entrySet()) {
                if (entrySet.getKey().compareTo(variable) == 0) {
                    return entrySet.getValue();
                }
            }
        }

        return null;
    }

    public void deleteProcessInstance(String instance) {
        if (this.engine.getHistoricProcessInstanceByInstanceID(instance).getDurationInMillis() == null) {
            this.engine.deleteProcessInstance(instance, "Eliminado por usuario - " + (new Date()).getTime());
        }
        HistoricoTramites ht = (HistoricoTramites) this.manager.find(Querys.getHistoricProceduresByProcId,
                new String[]{"idprocess"}, new Object[]{instance});
        if (ht != null) {
            ht.setEstado("Eliminado");
            ht.setObservacion(
                    "Eliminado por usuario (" + this.session.getName_user() + ") - " + (new Date()).getTime());
            this.manager.persist(ht);
            if (Utils.isNotEmpty(ht.getHistoricoTramiteDetCollection())) {
                for (HistoricoTramiteDet htd : ht.getHistoricoTramiteDetCollection()) {
                    if (htd.getPredio() != null) {
                        if (htd.getPredio().getEstado().contains(EstadosPredio.PENDIENTE)) {
                            htd.getPredio().setEstado(EstadosPredio.INACTIVO);
                            this.manager.persist(htd.getPredio());
                        }
                    }
                }
            }
        }
        Faces.update("frmProcessAdmin");
    }

    public String getNombrePropietario(CatEnte ente) {
        if (ente != null) {
            if (ente.getEsPersona()) {
                if ((ente.getApellidos() != null) && (ente.getNombres() != null)) {
                    return ente.getApellidos().toUpperCase() + " " + ente.getNombres().toUpperCase();
                } else if ((ente.getApellidos() != null) && (ente.getNombres() == null)) {
                    return ente.getApellidos().toUpperCase();
                } else if ((ente.getApellidos() == null) && (ente.getNombres() != null)) {
                    return ente.getNombres().toUpperCase();
                }
            } else {
                if (ente.getRazonSocial() != null) {
                    return ente.getRazonSocial();
                } else if (ente.getNombreComercial() != null) {
                    return ente.getNombreComercial();
                } else {
                    return "";
                }
            }
        }
        return "";
    }

    public String getCorreos(List<EnteCorreo> enteCorreoCollection) {
        String corroes = null;
        if (!enteCorreoCollection.isEmpty()) {
            for (EnteCorreo c : enteCorreoCollection) {
                if (corroes == null) {
                    corroes = c.getEmail();
                } else {
                    corroes = corroes + "," + c.getEmail();
                }
            }
        }
        return corroes;
    }

    public List<Long> getIdRolList(Collection<AclRol> aclRolCollection) {
        List<Long> roles = new ArrayList<>();
        for (AclRol l : aclRolCollection) {
            if (l.getIsDirector() && l.getEstado()) {
                roles.add(l.getId());
            }
        }
        return roles;
    }

    public byte[] getByteArray(String ruta) {
        File file = new File(ruta);
        return file.getAbsolutePath().getBytes();
    }

    public Task getTaskByProcessId(String processId) {
        return this.engine.getTaskDataByProcessID(processId);
    }

    public Object getExecutionVariable(String taskid, String instanceId, String varName) {
        return this.engine.getvariableByExecutionId(taskid, instanceId, varName);
    }

    public HistoricoTramites getBpmTramite() {
        return this.bpmTramite;
    }

    public void setBpmTramite(HistoricoTramites bpmTramite) {
        this.bpmTramite = bpmTramite;
    }

    public ArrayList<Archivo> getCartaAdosamiento() {
        return this.cartaAdosamiento;
    }

    public void setCartaAdosamiento(ArrayList<Archivo> cartaAdosamiento) {
        this.cartaAdosamiento = cartaAdosamiento;
    }

    public void removerContenido(Object valor, Collection listado) {
        listado.remove(valor);
    }

    public CmisUtil getCmis() {
        this.cmis = (CmisUtil) ApplicationContextUtils.getBean("cmisUtil");
        return this.cmis;
    }

    public byte[] leerArchivo(String ruta) throws Exception {
        Path path = Paths.get(ruta);
        byte[] data = Files.readAllBytes(path);
        return data;
    }

    public void agregarCorreoConcat() {
        if (this.nuevoCorreo == null) {
            JsfUti.messageError(null, "Debe ingresar el correo a adjuntar.", "");
            return;
        }
        if (!Utils.validarEmailConExpresion(this.nuevoCorreo)) {
            JsfUti.messageError(null, Messages.correoInvalido, "Adjuntar Correo");
            return;
        }
        if (this.correosAdjuntos != null) {
            this.correosAdjuntos = this.correosAdjuntos.concat(",").concat(this.nuevoCorreo);
        } else {
            this.correosAdjuntos = this.nuevoCorreo;
        }
        this.nuevoCorreo = null;
    }

    public String getNuevoCorreo() {
        return this.nuevoCorreo;
    }

    public void setNuevoCorreo(String nuevoCorreo) {
        this.nuevoCorreo = nuevoCorreo;
    }

    public String getCorreosAdjuntos() {
        return this.correosAdjuntos;
    }

    public void setCorreosAdjuntos(String correosAdjuntos) {
        this.correosAdjuntos = correosAdjuntos;
    }

    public void validarRegProf(CatEnte tecnico) {
        if (tecnico.getRegProf() == null) {
            JsfUti.messageInfo(null, "Advertencia", "El técnico no tiene un registro profesional registrado.");
            // return;
        }
    }

    protected String verificarPagoLiquidacion(HistoricoTramites ht, String pagado) {
        if ((pagado == null) || !"P".equalsIgnoreCase(pagado)) {
            if (ht != null) {
//                if ((ht.getRenLiquidacionCollection() != null) && (ht.getRenLiquidacionCollection().size() > 0)) {
//                    for (RenLiquidacion l : ht.getRenLiquidacionCollection()) {
//                        if (l.getEstadoLiquidacion() != null) {
//                            if (l.getEstadoLiquidacion().getId().equals(1L)) {
//                                pagado = "P";
//                                break;
//                            }
//                        }
//                    }
//                }
            }
        }
        return pagado;
    }

    public String colorRow(String estado) {
        if (estado == null) {
            return null;
        } else if (estado.equalsIgnoreCase(MainConfig.PREDIENTE)) {
            return "orange";
        } else if (estado.equalsIgnoreCase(MainConfig.INACTIVO)) {
            return "red";
        } else if (estado.equalsIgnoreCase(MainConfig.FINALIZADO)) {
            return "green";
        }
        return null;
    }

    public void formEntes(Long id, Boolean esver) {
        Map<String, List<String>> pms = new HashMap<>();
        List<String> p = new ArrayList<>();
        if (id != null) {
            p.add(id.toString());
            pms.put("idEnte", p);
        }
        p = new ArrayList<>();
        p.add(esver == null ? Boolean.FALSE.toString() : esver.toString());
        pms.put("ver", p);
        Utils.openDialog("/resources/dialog/Entes", pms, "450");
    }

    public void formEntes(Long id, Boolean esver, Boolean esPersona, String ciRuc) {
        Map<String, List<String>> pms = new HashMap<>();
        List<String> p = new ArrayList<>();
        if (id != null) {
            p.add(id.toString());
            pms.put("idEnte", p);
        }
        p = new ArrayList<>();
        p.add(esver == null ? Boolean.FALSE.toString() : esver.toString());
        pms.put("ver", p);
        p = new ArrayList<>();
        p.add(esPersona == null ? Boolean.FALSE.toString() : esPersona.toString());
        pms.put("esPersona", p);
        p = new ArrayList<>();
        if (ciRuc != null) {
            p.add(ciRuc);
            pms.put("ciRuc", p);
        }
        Utils.openDialog("/resources/dialog/Entes", pms, "450");
    }

    public HistoricoTramites registrarHistoricoTramites(GeTipoTramite tipoTramite, String ciSolicitante,
            String NombresSol, CatEnte sol) {
        HistoricoTramites hts = new HistoricoTramites();
        hts.setEstado("Pendiente");
        hts.setFecha(new Date());
        hts.setTipoTramite(tipoTramite);
        hts.setTipoTramiteNombre(tipoTramite.getDescripcion());
        if (sol != null) {
            hts.setSolicitante(sol);
            hts.setCorreos(this.getCorreos(sol.getEnteCorreoCollection()));
            if (!sol.getEnteTelefonoCollection().isEmpty()) {
                hts.setTelefonos(sol.getEnteTelefonoCollection().get(0).getTelefono());
            }
            hts.setNombrePropietario(sol.getNombreCompleto());
        }
        hts.setUserCreador(this.session.getUserId());
        hts.setId(this.generarIdTramite());
        hts = this.guardarHistoricoTranites(hts);
        return hts;
    }

    /**
     * Si id_tramite es nulo envia persistir el registro caso contrario realiza
     * un update
     *
     * @param ht
     * @return
     */
    public HistoricoTramites guardarHistoricoTranites(HistoricoTramites ht) {
        try {
            ht.setLiquidacionAprobada(Boolean.FALSE);
            if (ht.getIdTramite() != null) {
                if (manager.update(ht)) {
                    return ht;
                }
            } else {
                return (HistoricoTramites) manager.persist(ht);
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBaseRoot.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public HistoricoTramiteDet guardarHistoricoTranitesDetalle(HistoricoTramiteDet ht) {
        try {
            if (ht.getId() != null) {
                if (manager.update(ht)) {
                    return ht;
                }
            } else {
                return (HistoricoTramiteDet) manager.persist(ht);
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBaseRoot.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public HistoricoTramites registrarHistoricoTramites(GeTipoTramite tipoTramite, String ciSolicitante,
            String NombresSol, CatEnte sol, List<CatPredio> predios) {
        HistoricoTramites hts = new HistoricoTramites();
        hts.setEstado("Pendiente");
        hts.setFecha(new Date());
        hts.setTipoTramite(tipoTramite);

        hts.setTipoTramiteNombre(tipoTramite.getDescripcion());
        if (sol != null) {
            hts.setSolicitante(sol);
            hts.setCorreos(this.getCorreos(sol.getEnteCorreoCollection()));
            if (!sol.getEnteTelefonoCollection().isEmpty()) {
                hts.setTelefonos(sol.getEnteTelefonoCollection().get(0).getTelefono());
            }
            hts.setNombrePropietario(sol.getNombreCompleto());
        }
        hts.setUserCreador(this.session.getUserId());
        hts.setId(this.generarIdTramite());
        List<HistoricoTramiteDet> detalles = new LinkedList<>();
        if (!predios.isEmpty()) {
            hts.setNumPredio(predios.get(0).getNumPredio());
            for (CatPredio p : predios) {
                HistoricoTramiteDet detalle = new HistoricoTramiteDet();
                detalle.setPredio(p);
                detalle.setTramite(hts);
                detalle.setEstado(Boolean.FALSE);
                detalle.setNumTasa(BigInteger.valueOf(2));
                detalles.add(detalle);
            }
        }
        hts.setHistoricoTramiteDetCollection(detalles);
        hts = this.guardarHistoricoTranites(hts);

        return hts;
    }

    public HistoricoTramites addHistoricoTramites(GeTipoTramite tipoTramite, CatPredio predio, Boolean estado) {
        HistoricoTramites hts = new HistoricoTramites();
        hts.setEstado("Pendiente");
        hts.setFecha(new Date());
        hts.setTipoTramite(tipoTramite);

        hts.setTipoTramiteNombre(tipoTramite.getDescripcion());
        hts.setUserCreador(this.session.getUserId());
        hts.setId(this.generarIdTramite());
        List<HistoricoTramiteDet> detalles = new LinkedList<>();
        HistoricoTramiteDet detalle = new HistoricoTramiteDet();
        detalle.setPredio(predio);
        detalle.setTramite(hts);
        detalle.setEstado(estado);
        detalle.setNumTasa(BigInteger.valueOf(2));
        detalles.add(detalle);

        hts.setHistoricoTramiteDetCollection(detalles);
        hts = this.guardarHistoricoTranites(hts);

        return hts;
    }

    public HistoricoTramites registrarHistoricoTramites(GeTipoTramite tipoTramite, String ciSolicitante,
            String NombresSol, CatEnte sol, CatPredio predio, boolean byPredio) {
        HistoricoTramites hts = new HistoricoTramites();
        hts.setEstado("Pendiente");
        hts.setFecha(new Date());
        hts.setTipoTramite(tipoTramite);
        hts.setTipoTramiteNombre(tipoTramite.getDescripcion());
        if (sol != null) {
            hts.setSolicitante(sol);
            hts.setCorreos(this.getCorreos(sol.getEnteCorreoCollection()));
            if (!sol.getEnteTelefonoCollection().isEmpty()) {
                hts.setTelefonos(sol.getEnteTelefonoCollection().get(0).getTelefono());
            }
            hts.setNombrePropietario(sol.getNombreCompleto());
        }
        hts.setUserCreador(this.session.getUserId());
        hts.setId(this.generarIdTramite());
        if (!byPredio) {
            List<HistoricoTramiteDet> detalles = new LinkedList<>();
            JsonUtils jsonUtil = new JsonUtils();
            if (predio.getCatPredioEdificacionCollection() != null) {

                for (CatPredioEdificacion e : predio.getCatPredioEdificacionCollection()) {
                    HistoricoTramiteDet detalle = new HistoricoTramiteDet();
                    detalle.setPredio(predio);
                    detalle.setTramite(hts);
                    detalle.setJson(jsonUtil.generarJson(e));
                    detalles.add(detalle);
                }
            }

            hts.setHistoricoTramiteDetCollection(detalles);
        } else {
            hts.setNumPredio(predio != null ? predio.getNumPredio() : null);
        }
        hts = this.guardarHistoricoTranites(hts);

        return hts;
    }

    public HistoricoTramiteDet registrarDetalle(HistoricoTramites ht, CatPredio p, String json) {
        HistoricoTramiteDet det = new HistoricoTramiteDet();
        det.setTramite(ht);
        det.setPredio(p);
        det.setJson(json);
        det.setFecCre(new Date());
        det.setCartaAdosamiento(Boolean.FALSE);

        det = this.guardarHistoricoTranitesDetalle(det);

        return det;
    }

    public String createInforme(String detalle, HistoricoTramites ht, CatPredio predio, FormatoReporte formato) {
        try {
            Map<String, Object> pm = new HashMap<>();
            if (ht != null) {
                pm.put("codigo", ht.getTipoTramite().getDescripcionTramite().toUpperCase());
                if (formato == null) {
                    formato = this.manager.findObjectByParameter(FormatoReporte.class, pm);
                }
                if (formato != null) {
                    String tramiteQuipux = ht.getCarpetaRep();
                    String propietario = "";
                    String cedula = "";
                    String claveCat = "";
                    String ubicacion = "";
                    String parroquia = "";
                    String canton = "";
                    if (predio != null) {
                        claveCat = predio.getClaveCat();
                        ubicacion = Utils.isEmpty(predio.getCalle()) + " y " + Utils.isEmpty(predio.getCalleS());
                        CatParroquia cpq = predio.getCatParroquia();
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
                    if (detalle.contains(":calveCat")) {
                        detalle = detalle.replace(":calveCat", claveCat);
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
                    return detalle;
                } else {
                    JsfUti.messageError(null, "Error!", "No se encontro el formato para generar el reporte.");
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBaseRoot.class.getName()).log(Level.SEVERE, "Llenar datos del reporte", e);
        }
        return null;
    }

    public void generarInforme(List<CatPredio> prediosTemp, String detalle, FormatoReporte formato, ServletSession ss) {
        if (Utils.isEmpty(prediosTemp)) {
            JsfUti.messageError(null, "Error!", "No se encontron predio para generar el reporte.");
            return;
        }
        // Inicio de parametros de informe
        System.out.println("Empezando a generar reporte en Word. (generarInforme())");
        ss.instanciarParametros();
        ss.setTieneDatasource(false);
        ss.setNombreSubCarpeta("catastro/informesProcesos");
        ss.setNombreReporte(formato.getReporte());
        ss.agregarParametro("DETALLE", detalle);
        ss.agregarParametro("TITULO", SisVars.NOMBREMUNICIPIO);
        ss.agregarParametro("NOMBRE_CERTIFICADO", formato.getCodigo());
        List<CatPredio> dataSource = new ArrayList<>();
        dataSource.addAll(prediosTemp);
        ss.setDataSource(dataSource);
        // Fin de parametro de informe
        JsfUti.redirectNewTab(SisVars.urlbase + "DocumentoWord"); // Generador de Reporte
    }

    public HistoricoTramites getHistoricoTramiteById(Long idTramite) {
        return (HistoricoTramites) manager.find(Querys.getHistoricoTramiteById, new String[]{"id"},
                new Object[]{idTramite});
    }

    /**
     * Con los parametros recibidos crea una un nuevo objecto de Observaciones y
     * los envia a persitir.
     *
     * @param ht HistoricoTramites
     * @param nameUser Nombre del Usuario Creador.
     * @param observaciones Observaciones realizadas.
     * @param taskDefinitionKey Nombre de la Tarea.
     * @return entity Observaciones.
     */
    public Observaciones guardarObservaciones(HistoricoTramites ht, String nameUser, String observaciones, String taskDefinitionKey) {
        try {
            Observaciones observ = new Observaciones();
            observ.setEstado(true);
            observ.setFecCre(new Date());
            observ.setTarea(taskDefinitionKey);
            observ.setIdTramite(ht);
            observ.setUserCre(nameUser);
            observ.setObservacion(observaciones);
            return (Observaciones) manager.persist(observ);
        } catch (Exception e) {
            Logger.getLogger(BpmManageBeanBaseRoot.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public GeTipoTramite getGeTipoTramiteByActivitiKey(String solicitudServicio) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("activitykey", solicitudServicio);
        return manager.findObjectByParameter(GeTipoTramite.class, paramt);
    }

    public void actualizarHistoricoTramites(HistoricoTramites ht) {
        manager.update(ht);
    }

}
