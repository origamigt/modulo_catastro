/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.procesos.eliminar;

import com.origami.app.AppConfig;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.entities.GeTipoTramite;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.ParametrosDisparador;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.predio.models.ModelLockPredio;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.services.interfaces.catastro.FusionDivisionServices;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.activiti.engine.runtime.ProcessInstance;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FlowEvent;
import util.JsfUti;

/**
 *
 * @author dfcalderio
 */
@Named(value = "iniciarEliminarFlow")
@ViewScoped
public class IniciarEliminacion extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(IniciarEliminacion.class.getName());

    @Inject
    private CatastroServices catastroService;
    @Inject
    protected FusionDivisionServices divisionServices;
    @Inject
    protected AppConfig appconfig;
    @Inject
    private UserSession sess;
    @Inject
    private ServletSession ss;

    private CatPredio predio;
    private List<CatPredio> prediosSeleccionados;
    private Long idPredio;
    private Short nuevoCodigo;
    protected String mensaje;

    private GeDepartamento dep;
    private AclRol rol;
    private AclUser responsable;

    protected CatPredioLazy predios;

    @PostConstruct
    public void initView() {

        ModelLockPredio get = appconfig.getPrediosedicion().get(this.session.getName_user());
        if (get != null) {
            this.predio = catastroService.getPredioId(get.getIdpredio());
        }

        prediosSeleccionados = new LinkedList<>();

    }

    public void iniciarTramite() {

        try {
            GeTipoTramite tipoTramite = this.getGeTipoTramiteByActivitiKey("process_eliminarPredio");
            if (tipoTramite == null) {
                JsfUti.messageInfo(null, "Error", "Comuniquese con sistemas, proceso no disponible.");
                return;
            }
//            if (Utils.isEmpty(tipoTramite.getParametrosDisparadorCollection())) {
//                JsfUti.messageInfo(null, "Error", "No se encontraron los parametros del flujo comuniquese con sistemas.");
//                return;
//            }
            prediosSeleccionados.add(predio);
            HistoricoTramites ht = this.registrarHistoricoTramites(tipoTramite, mensaje, mensaje, null, prediosSeleccionados);
            if (ht != null) {
                HashMap<String, Object> paramts = new HashMap<>();
                paramts.put("descripcion", tipoTramite.getDescripcion());// NOMBRE DEL TRAMITE
                paramts.put("prioridad", 50);
                for (ParametrosDisparador pd : tipoTramite.getParametrosDisparadorCollection()) {
                    paramts.put(pd.getVarResp(), pd.getResponsable().getUsuario());
                    if (pd.getVarInterfaz() != null) {
                        paramts.put(pd.getVarInterfaz(), pd.getInterfaz());
                    }
                }
                paramts.put("formEdicionGrafica", "/vistaprocesos/catastro/bpm/eliminar/edicionFlujo.xhtml");
                paramts.put("formEdicionAlfanumerica", "/vistaprocesos/catastro/bpm/eliminar/edicionFlujo.xhtml");
                paramts.put("formRevision", "/vistaprocesos/catastro/bpm/eliminar/revisionFlujo.xhtml");
                paramts.put("dibujante", responsable.getUsuario());
                paramts.put("revisor", "BLechon");
                paramts.put("tramite", ht.getId());
                ProcessInstance pro = this.startProcessByDefinitionKey(tipoTramite.getActivitykey(), paramts);
                if (pro != null) {
                    LOG.log(Level.INFO, "Registrando tramite {0} numero de proceso activiti {1}", new Object[]{tipoTramite.getActivitykey(), pro.getId()});
                    ht.setCarpetaRep(ht.getId() + "-" + pro.getId());
                    ht.setIdProceso(pro.getId());
                    ht.setIdProcesoTemp(pro.getId());
                    ht.setNumPredio(predio.getNumPredio());
                    this.actualizarHistoricoTramites(ht);
                    for (CatPredio p : prediosSeleccionados) {
                        p.setEstado(EstadosPredio.PENDIENTE);
                        catastroService.actualizarPredio(p);
                    }
                    mensaje = "Trámite Número " + ht.getId() + " Iniciado Correctamete.";
                } else {
                    mensaje = "Proceso no .";
                }
            } else {
                mensaje = "Error al iniciar Tramites.";
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, mensaje, e);
        }

        JsfUti.messageInfo(null, "Info", mensaje);
        this.close();
    }

    public void close() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public boolean validacionesDiv() {
        return true;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public GeDepartamento getDep() {
        return dep;
    }

    public void setDep(GeDepartamento dep) {
        this.dep = dep;
    }

    public AclRol getRol() {
        return rol;
    }

    public void setRol(AclRol rol) {
        this.rol = rol;
    }

    public AclUser getResponsable() {
        return responsable;
    }

    public void setResponsable(AclUser responsable) {
        this.responsable = responsable;
    }

    public List<GeDepartamento> departamentosList() {
        return manager.findAllOrdered(GeDepartamento.class, new String[]{"nombre"}, new Boolean[]{true});
    }

    public List<AclRol> rolesDepartamentoList() {
        if (dep != null) {
            return manager.findAll(Querys.getListAclRolByDep, new String[]{"departamento"}, new Object[]{dep.getId()});
        } else {
            return null;
        }
    }

    public List<AclUser> userList() {
        if (rol != null) {
            return manager.findAll(Querys.getListAclUserByRol, new String[]{"idRol"}, new Object[]{rol.getId()});

        } else {
            return null;
        }
    }

    public void addPredio(CatPredio p) {
        if (!Objects.equals(p.getId(), predio.getId())) {
            if (!prediosSeleccionados.contains(p)) {
                prediosSeleccionados.add(p);
                JsfUti.messageInfo(null, "Info", "Predio adicionado con exito.");
            } else {
                JsfUti.messageWarning(null, "Info", "Predio ya se encuentra adicionado.");
            }
        }

    }

    public void deletePredio(CatPredio p) {
        if (prediosSeleccionados.contains(p)) {
            prediosSeleccionados.remove(p);
            JsfUti.messageInfo(null, "Info", "Predio eliminado con exito.");
        }

    }

    public String onFlowProcess(FlowEvent event) {

        return event.getNewStep();
    }

    public List<CatPredio> getPrediosSeleccionados() {
        return prediosSeleccionados;
    }

    public void setPrediosSeleccionados(List<CatPredio> prediosSeleccionados) {
        this.prediosSeleccionados = prediosSeleccionados;
    }

    public Short getNuevoCodigo() {
        return nuevoCodigo;
    }

    public void setNuevoCodigo(Short nuevoCodigo) {
        this.nuevoCodigo = nuevoCodigo;
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

}
