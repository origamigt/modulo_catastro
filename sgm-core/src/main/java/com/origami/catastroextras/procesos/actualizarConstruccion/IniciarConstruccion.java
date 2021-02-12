/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.procesos.actualizarConstruccion;

import com.origami.app.AppConfig;
import com.origami.censocat.restful.JsonUtils;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioEdificacionProp;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.entities.GeTipoTramite;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.ParametrosDisparador;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.predio.models.ModelLockPredio;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.activiti.engine.runtime.ProcessInstance;
import org.primefaces.context.RequestContext;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author dfcalderio
 */
@Named(value = "updateConstruccionView")
@ViewScoped
public class IniciarConstruccion extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(IniciarConstruccion.class.getName());

    @Inject
    private CatastroServices catastroServices;

    @Inject
    protected AppConfig appconfig;

    private CatPredio predio;

    protected String mensaje;

    private GeDepartamento dep;
    private AclRol rol;
    private AclUser responsable;

    @PostConstruct
    public void init() {
        ModelLockPredio get = appconfig.getPrediosedicion().get(this.session.getName_user());
        if (get != null) {
            predio = catastroServices.getPredioId(get.getIdpredio());

            for (CatPredioEdificacion e : predio.getCatPredioEdificacionCollection()) {
                JsonUtils json = new JsonUtils();
                String jsonString = json.generarJson(e);
                System.out.println("Json Edificacion: " + jsonString);

                CatPredioEdificacion edf = json.jsonToObject(jsonString, CatPredioEdificacion.class);

                for (CatPredioEdificacionProp p : edf.getCatPredioEdificacionPropCollection()) {
                    System.out.println("Info catpredioedificacionprop: " + p.getProp().getNombre());
                }
            }
        }
    }

    public void iniciarTramite() {
        try {
            GeTipoTramite tipoTramite = this.getGeTipoTramiteByActivitiKey("actualizarConstruccion");
            if (tipoTramite == null) {
                JsfUti.messageInfo(null, "Error", "Comuniquese con sistemas, proceso no disponible.");
                return;
            }
            if (Utils.isEmpty(tipoTramite.getParametrosDisparadorCollection())) {
                JsfUti.messageInfo(null, "Error", "No se encontraron los parametros del flujo comuniquese con sistemas.");
                return;
            }

            HistoricoTramites ht = this.registrarHistoricoTramites(tipoTramite, mensaje, mensaje, null, predio, false);
            if (ht != null) {
                HashMap<String, Object> paramts = new HashMap<>();
                paramts.put("prioridad", 50);
                paramts.put("descripcion", tipoTramite.getDescripcion());// NOMBRE DEL TRAMITE
                for (ParametrosDisparador pd : tipoTramite.getParametrosDisparadorCollection()) {
                    paramts.put(pd.getVarResp(), pd.getResponsable().getUsuario());
                    if (pd.getVarInterfaz() != null) {
                        paramts.put(pd.getVarInterfaz(), pd.getInterfaz());
                    }
                }
                paramts.put("dibujante", responsable.getUsuario());
                paramts.put("tramite", ht.getId());
                ProcessInstance pro = this.startProcessByDefinitionKey(tipoTramite.getActivitykey(), paramts);
                if (pro != null) {
                    LOG.log(Level.INFO, "Registrando tramite {0} numero de proceso activiti {1}", new Object[]{tipoTramite.getActivitykey(), pro.getId()});
                    ht.setCarpetaRep(ht.getId() + "-" + pro.getId());
                    ht.setIdProceso(pro.getId());
                    ht.setIdProcesoTemp(pro.getId());
                    ht.setNumPredio(predio.getNumPredio());
                    this.actualizarHistoricoTramites(ht);

                    //procesosService.actualizarDataDivision(division);
                    predio.setEstado(EstadosPredio.PENDIENTE);
                    catastroServices.actualizarPredio(predio);
                    mensaje = "Trámite Número " + ht.getId() + " Iniciado Correctamete.";
                } else {
                    mensaje = "Proceso no .";
                }
            } else {
                mensaje = "Error al iniciar Tramites.";
            }

            Faces.messageInfo(null, "Nota!", mensaje);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, mensaje, e);
        }
        JsfUti.messageInfo(null, "Info", mensaje);
        this.close();
    }

    public List<GeDepartamento> getDepartamentos() {
        return manager.findAllOrdered(GeDepartamento.class, new String[]{"nombre"}, new Boolean[]{true});
    }

    public List<AclRol> getRolesDepartamento() {
        if (dep != null) {
            return manager.findAll(Querys.getListAclRolByDep, new String[]{"departamento"}, new Object[]{dep.getId()});
        } else {
            return null;
        }
    }

    public List<AclUser> getUsers() {
        if (rol != null) {
            List<AclUser> us = new ArrayList<>();
            for (AclUser u : rol.getAclUserCollection()) {
                if (u.getSisEnabled()) {
                    us.add(u);
                }
            }
            return us;
        } else {
            return null;
        }
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

    public CatastroServices getCatastroServices() {
        return catastroServices;
    }

    public void setCatastroServices(CatastroServices catastroServices) {
        this.catastroServices = catastroServices;
    }

    public AppConfig getAppconfig() {
        return appconfig;
    }

    public void setAppconfig(AppConfig appconfig) {
        this.appconfig = appconfig;
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

}
