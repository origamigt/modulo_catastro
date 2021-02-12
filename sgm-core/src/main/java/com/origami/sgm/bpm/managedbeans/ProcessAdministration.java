package com.origami.sgm.bpm.managedbeans;

import com.origami.sgm.bpm.models.DetalleProceso;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.TareasAll;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.task.Attachment;
import org.activiti.engine.task.Task;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import util.JsfUti;

/**
 *
 * @author CarlosLoorVargas
 */
@Named
@ViewScoped
public class ProcessAdministration extends BpmManageBeanBaseRoot implements Serializable {

    public static final Long serialVerisonUID = 1L;

    private String process = null;
    private boolean showDetail = false;
    List<HistoricTaskInstance> listTareas;
    private int prioridad;
    private HistoricoTramites historicoTramites;
    private List<Attachment> documentos;
    private LazyDataModel<GeDepartamento> departamentosLazy;
    private LazyDataModel<TareasAll> tareasAlls;
    private LazyDataModel<TareasAll> tareasAllsFinalizadas;
    private LazyDataModel<TareasAll> tareasAllsEliminadas;
    private HistoricTaskInstance tareaSeleccionada;

    public void doPreRenderView() {
        if (!JsfUti.isAjaxRequest()) {
            iniView();
        }
    }

    private void iniView() {
        try {
            process = new String();
            departamentosLazy = new BaseLazyDataModel<>(GeDepartamento.class, "nombre");
            tareasAlls = new BaseLazyDataModel<>(TareasAll.class, new String[]{"estado"}, new Object[]{"Pendiente"}, "numeroSegimiento", "DESC");
            tareasAllsFinalizadas = new BaseLazyDataModel<>(TareasAll.class, new String[]{"estado"}, new Object[]{"Finalizado"}, "numeroSegimiento", "DESC");
            tareasAllsEliminadas = new BaseLazyDataModel<>(TareasAll.class, new String[]{"estado"}, new Object[]{"Eliminado"}, "numeroSegimiento", "DESC");
        } catch (Exception e) {
            Logger.getLogger(ProcessManagement.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void load() {
        try {
            if (process != null) {
                showDetail = true;
            } else {
                showDetail = false;
            }
        } catch (Exception e) {
            Logger.getLogger(ProcessManagement.class.getName()).log(Level.SEVERE, null, e);
        }
        JsfUti.update("frmProcessAdmin:tdatos");
    }

    public void onRowToggle(ToggleEvent event) {
        try {
            if (event.getData() != null) {
                listTareas = ((DetalleProceso) event.getData()).getTasks();
            }
        } catch (Exception e) {
            Logger.getLogger(ProcessManagement.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void listaTareas(TareasAll dp) {
        historicoTramites = dp.getIdTramite();
        listTareas = this.getTaskByProcessInstanceIdMain(historicoTramites.getIdProcesoTemp());
    }

    public void listarVariables(DetalleProceso dp) {
        historicoTramites = (HistoricoTramites) manager.find(Querys.getHistoricoTramiteById, new String[]{"id"}, new Object[]{new Long(dp.getIdProceso())});
    }

    public void selectPrioridad(TareasAll dp) {
        historicoTramites = dp.getIdTramite();
        prioridad = (int) this.getVariableByPorcessIntance(historicoTramites.getIdProceso(), "prioridad");
    }

    public void actualizarPrioridad() {
        try {
            List<String> listadoIdsProcessInstace = this.obtenerProcessInstanceByProcessInstaceIdMain(historicoTramites.getIdProceso());
            for (String listadoIdsProcessInstace1 : listadoIdsProcessInstace) {
                this.setVariableByProcessInstance(listadoIdsProcessInstace1, "prioridad", prioridad);
                List<Task> tareasActivas = this.obtenerTareasActivasProcessInstance(listadoIdsProcessInstace1);
                this.asignarTareaPriority(tareasActivas, prioridad);
            }
        } catch (Exception ex) {
            Logger.getLogger(ProcessAdministration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void selectObservaciones(TareasAll dp) {
        historicoTramites = dp.getIdTramite();
    }

    public void selectDocumentos(TareasAll dp) {
        historicoTramites = dp.getIdTramite();
        documentos = this.getProcessInstanceAllAttachmentsFiles(historicoTramites.getIdProceso());
    }

    public void seleccionarTareaReasignacion(HistoricTaskInstance ta) {
        tareaSeleccionada = ta;
    }

    public void reasignarTarea(AclUser usuario) {
        try {
            this.guardarObservaciones(historicoTramites, session.getName_user(), "TAREA: " + tareaSeleccionada.getName() + "USUARIO ANTERIOR: " + tareaSeleccionada.getAssignee() + ". USUARIO ACTUAL: " + usuario.getUsuario(), "REASIGNACION DE USUARIO");
            this.reasignarTarea(tareaSeleccionada.getId(), usuario.getUsuario());
            Map<String, Object> v = this.engine.getvariables(tareaSeleccionada.getProcessInstanceId());
            for (Map.Entry<String, Object> entrySet : v.entrySet()) {
                if (entrySet.getValue() != null && entrySet.getValue().equals(tareaSeleccionada.getAssignee())) {
                    this.setVariableByProcessInstance(tareaSeleccionada.getProcessInstanceId(), entrySet.getKey(), usuario.getUsuario());
                    break;
                }
            }
            listTareas = this.getTaskByProcessInstanceIdMain(historicoTramites.getIdProcesoTemp());
        } catch (Exception ex) {
            Logger.getLogger(ProcessAdministration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public boolean getShowDetail() {
        return showDetail;
    }

    public void setShowDetail(boolean showDetail) {
        this.showDetail = showDetail;
    }

    public List<HistoricTaskInstance> getListTareas() {
        return listTareas;
    }

    public void setListTareas(List<HistoricTaskInstance> listTareas) {
        this.listTareas = listTareas;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public HistoricoTramites getHistoricoTramites() {
        return historicoTramites;
    }

    public void setHistoricoTramites(HistoricoTramites historicoTramites) {
        this.historicoTramites = historicoTramites;
    }

    public List<Attachment> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Attachment> documentos) {
        this.documentos = documentos;
    }

    public LazyDataModel<GeDepartamento> getDepartamentosLazy() {
        return departamentosLazy;
    }

    public void setDepartamentosLazy(LazyDataModel<GeDepartamento> departamentosLazy) {
        this.departamentosLazy = departamentosLazy;
    }

    public HistoricTaskInstance getTareaSeleccionada() {
        return tareaSeleccionada;
    }

    public void setTareaSeleccionada(HistoricTaskInstance tareaSeleccionada) {
        this.tareaSeleccionada = tareaSeleccionada;
    }

    public LazyDataModel<TareasAll> getTareasAlls() {
        return tareasAlls;
    }

    public void setTareasAlls(LazyDataModel<TareasAll> tareasAlls) {
        this.tareasAlls = tareasAlls;
    }

    public LazyDataModel<TareasAll> getTareasAllsFinalizadas() {
        return tareasAllsFinalizadas;
    }

    public void setTareasAllsFinalizadas(LazyDataModel<TareasAll> tareasAllsFinalizadas) {
        this.tareasAllsFinalizadas = tareasAllsFinalizadas;
    }

    public LazyDataModel<TareasAll> getTareasAllsEliminadas() {
        return tareasAllsEliminadas;
    }

    public void setTareasAllsEliminadas(LazyDataModel<TareasAll> tareasAllsEliminadas) {
        this.tareasAllsEliminadas = tareasAllsEliminadas;
    }

}
