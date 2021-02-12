package com.origami.sgmee.catastro.procesos.construcciones;

import com.origami.app.AppConfig;
import com.origami.censocat.restful.JsonUtils;
import com.origami.config.SisVars;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.entities.GeTipoTramite;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.ParametrosDisparador;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.model.ManzanaModel;
import com.origami.sgmee.catastro.procesos.crear.IniciarCrear;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.activiti.engine.runtime.ProcessInstance;
import org.primefaces.event.FlowEvent;
import util.Faces;
import util.JsfUti;

/**
 * Permiteinicar la tarea de edicion de bloques (No usada)
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class IniciarEdicionConstruccion extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(IniciarCrear.class.getName());

    @Inject
    private CatastroServices catastroService;
    @Inject
    protected AppConfig appconfig;
    private CatPredio predio;
    private List<CatPredio> prediosSeleccionados;

    private short codProvincia;
    private short codCanton;
    private short codParroquia;
    private short codZona;
    private short codSector;
    private short codManzana;

    private ManzanaModel manzanaModel;

    protected String mensaje;

    //@NotNull
    private GeDepartamento dep;
    //@NotNull
    private AclRol rol;
    //@NotNull
    private AclUser responsable;

    private JsonUtils jsonUtils;

    @PostConstruct
    public void initView() {
        jsonUtils = new JsonUtils();
        manzanaModel = new ManzanaModel();
        manzanaModel.setCodProvincia(SisVars.PROVINCIA != null ? SisVars.PROVINCIA : 0);
        manzanaModel.setCodCanton(SisVars.CANTON != null ? SisVars.CANTON : 0);
    }

    public void iniciarTramite() {

        try {
            GeTipoTramite tipoTramite = this.getGeTipoTramiteByActivitiKey("process_editarConstruccion");
            if (tipoTramite == null) {
                JsfUti.messageInfo(null, "Error", "Comuniquese con sistemas, proceso no disponible.");
                return;
            }
//            if (Utils.isEmpty(tipoTramite.getParametrosDisparadorCollection())) {
//                JsfUti.messageInfo(null, "Error", "No se encontraron los parametros del flujo comuniquese con sistemas.");
//                return;
//            }

            HistoricoTramites ht = this.registrarHistoricoTramites(tipoTramite, mensaje, mensaje, null, null, true);
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
                paramts.put("formEdicionGrafica", "/vistaprocesos/catastro/bpm/crear/edicionFlujo.xhtml");
                paramts.put("formEdicionAlfanumerica", "/vistaprocesos/catastro/bpm/crear/edicionFlujo.xhtml");
                paramts.put("formRevision", "/vistaprocesos/catastro/bpm/crear/revisionFlujo.xhtml");
                paramts.put("dibujante", responsable.getUsuario());
                paramts.put("revisor", "BLechon");
                paramts.put("tramite", ht.getId());
                ProcessInstance pro = this.startProcessByDefinitionKey(tipoTramite.getActivitykey(), paramts);
                if (pro != null) {
                    LOG.log(Level.INFO, "Registrando tramite {0} numero de proceso activiti {1}", new Object[]{tipoTramite.getActivitykey(), pro.getId()});
                    ht.setCarpetaRep(ht.getId() + "-" + pro.getId());
                    ht.setIdProceso(pro.getId());
                    ht.setIdProcesoTemp(pro.getId());
                    this.actualizarHistoricoTramites(ht);
                    String jsonManzana = jsonUtils.generarJsonStaticModel(manzanaModel);
                    registrarDetalle(ht, null, jsonManzana);
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
        Faces.redirectFaces("/faces/vistaprocesos/catastro/predios.xhtml");
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

    public AppConfig getAppconfig() {
        return appconfig;
    }

    public void setAppconfig(AppConfig appconfig) {
        this.appconfig = appconfig;
    }

    public short getCodProvincia() {
        return codProvincia;
    }

    public void setCodProvincia(short codProvincia) {
        this.codProvincia = codProvincia;
    }

    public short getCodCanton() {
        return codCanton;
    }

    public void setCodCanton(short codCanton) {
        this.codCanton = codCanton;
    }

    public short getCodParroquia() {
        return codParroquia;
    }

    public void setCodParroquia(short codParroquia) {
        this.codParroquia = codParroquia;
    }

    public short getCodZona() {
        return codZona;
    }

    public void setCodZona(short codZona) {
        this.codZona = codZona;
    }

    public short getCodSector() {
        return codSector;
    }

    public void setCodSector(short codSector) {
        this.codSector = codSector;
    }

    public short getCodManzana() {
        return codManzana;
    }

    public void setCodManzana(short codManzana) {
        this.codManzana = codManzana;
    }

    public ManzanaModel getManzanaModel() {
        return manzanaModel;
    }

    public void setManzanaModel(ManzanaModel manzanaModel) {
        this.manzanaModel = manzanaModel;
    }

}
