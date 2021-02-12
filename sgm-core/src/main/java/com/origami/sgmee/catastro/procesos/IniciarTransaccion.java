/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.procesos;

import com.origami.catastro.services.impl.SeriveceOtrasBases;
import com.origami.catastroextras.models.Radicado;
import com.origami.censocat.restful.JsonUtils;
import com.origami.config.MainConfig;
import com.origami.config.SisVars;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.entities.GeTipoTramite;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.FusionServ;
import com.origami.sgmee.catastro.geotx.IntegracionParcelariaServ;
import com.origami.sgmee.catastro.geotx.model.ManzanaModel;
import com.origami.sgmee.catastro.geotx.model.TransaccionDataCommand;
import java.util.ArrayList;
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
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 * Controlador para iniciar las tareas. (Todos los predios que sean creados
 * apartir de una tarea seran activas una ves terminada la tarea mistran tanto
 * se mantienen en estado 'P')
 *
 * @author Dairon Freddy
 */
@Named(value = "iniciarTransaccionFlow")
@ViewScoped
public class IniciarTransaccion extends BpmManageBeanBaseRoot {

    private static final Logger LOG = Logger.getLogger(IniciarTransaccion.class.getName());

    @Inject
    private IntegracionParcelariaServ integracionParcelariaServ;

    @Inject
    private FusionServ fusionServ;

    @Inject
    private CatastroServices catastroServices;
    @javax.inject.Inject
    protected SeriveceOtrasBases quipux;

    private CatPredio predio;
    private List<CatPredio> prediosSeleccionados;
    private GeDepartamento dptoDibujante;
    private AclRol rolDibujante;
    private AclUser dibujante;

    private GeDepartamento dptoRevisor;
    private AclRol rolRevisor;
    private AclUser revisor;

    private String transaccionSeleccionada;

    protected CatPredioLazy prediosLazy;

    private ManzanaModel manzanaModel;

    protected String mensaje;
    protected String numTramite;

    private JsonUtils jsonUtils;

    private List<TransaccionDataCommand> transacciones;
    private TransaccionDataCommand trasDataCommand;
    private boolean disabledIniciar;
    private boolean renderListado;
    private List<AclUser> revisores;

    @PostConstruct
    public void initView() {
        renderListado = false;
        prediosLazy = new CatPredioLazy();
        prediosLazy.setEstado("A");
        jsonUtils = new JsonUtils();
        prediosSeleccionados = new LinkedList<>();
        manzanaModel = new ManzanaModel();
        manzanaModel.setCodProvincia(SisVars.PROVINCIA != null ? SisVars.PROVINCIA : 0);
        manzanaModel.setCodCanton(SisVars.CANTON != null ? SisVars.CANTON : 0);
        initTransacciones();
        trasDataCommand = new TransaccionDataCommand();
        disabledIniciar = true;
        dptoDibujante = dptoRevisor = catastroServices.getDptoByName("CATASTRO");
        revisores = manager.findAll(Querys.getListAclUserByRol, new String[]{"idRol"}, new Object[]{new Integer(217)});

    }

    public void iniciarTramite() {

        try {
            GeTipoTramite tipoTramite = this.getGeTipoTramiteByActivitiKey(transaccionSeleccionada);
            if (tipoTramite == null) {
                JsfUti.messageInfo(null, "Error", "Comuniquese con sistemas, proceso no disponible.");
                return;
            }
            if (MainConfig.validarNumTramite) {
                if (!this.existeNumTramite()) {
                    JsfUti.messageInfo(null, "Error", "No existe trámite ingresado.");
                    return;
                }
            }
            List<String> codigos = new LinkedList<>();
            prediosSeleccionados.forEach((p) -> {
                if (this.existePredioEnTramite(p)) {
                    JsfUti.messageInfo(null, "Error", "Existe un trámite ingresado con el predio seleccionado: " + p.getClaveCat());
                    return;
                }
                codigos.add(p.getClaveCat());
            });
            if (predio != null) {
                if (this.existePredioEnTramite(predio)) {
                    JsfUti.messageInfo(null, "Error", "Existe un trámite ingresado con el predio seleccionado: " + predio.getClaveCat());
                    return;
                }
                if (!codigos.contains(predio.getClaveCat())) {
                    codigos.add(predio.getClaveCat());
                }
            }
            if (checkColindantes(codigos)) {
                HistoricoTramites ht = createHistorico(tipoTramite, mensaje, prediosSeleccionados, predio);

                if (ht != null) {
                    HashMap<String, Object> paramts = parameters(tipoTramite, ht);
                    ProcessInstance pro = this.startProcessByDefinitionKey(tipoTramite.getActivitykey(), paramts);
                    if (pro != null) {
                        LOG.log(Level.INFO, "Registrando tramite {0} numero de proceso activiti {1}", new Object[]{tipoTramite.getActivitykey(), pro.getId()});
                        ht.setCarpetaRep(numTramite);
                        ht.setIdProceso(pro.getId());
                        ht.setIdProcesoTemp(pro.getId());
                        this.actualizarHistoricoTramites(ht);
                        mensaje = "Trámite Número " + ht.getId() + " Iniciado Correctamete.";
                    } else {
                        mensaje = "Proceso no iniciado.";
                    }
                } else {
                    mensaje = "Error al iniciar Tramites.";
                }

                JsfUti.messageInfo(null, "Info", mensaje);
                Faces.redirectFaces("/faces/vistaprocesos/catastro/predios.xhtml");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, mensaje, e);
        }

    }

    private void initTransacciones() {
        transacciones = new ArrayList<>();
        transacciones.add(new TransaccionDataCommand("process_crearPredio", "Creación de Predio", false, false,
                "Permite realizar la creacion de un predio nuevo y los datos de la ficha deben de ser ingresados desde cero."));
        transacciones.add(new TransaccionDataCommand("process_updateDatos", "Actualización de Datos", true, false,
                "Permite realizar la edicion de la informacion alfanumerica. Solo en phs hijas permite el ingreso de bloques."));
        transacciones.add(new TransaccionDataCommand("process_divisionPredio", "Fraccionamiento", true, false,
                "Permite realizar el fracionamiento de un predio y pasa al historico la ficha."));
        transacciones.add(new TransaccionDataCommand("process_integracionParcelaria", "Integración Parcelaria", true, true,
                "Integración Parcelaria."));
        transacciones.add(new TransaccionDataCommand("process_fusionarPredios", "Unificación Predial", true, true,
                "Permite realizar la unificacion predial de dos fichas y pasa al historico la ficha unificadas y crea una nueva del resulyado de la unificacion."));
        transacciones.add(new TransaccionDataCommand("process_actualizarLinderosAreas", "Actualización de areas y linderos(exedentes y diferencias)", false, true,
                "Permite realizar la edicion del area de los bloques desde la base grafica, y el ingreso de nuevos bloques ademas la modificacion del area del predio desde la base grafica"));
        transacciones.add(new TransaccionDataCommand("process_afectaciones", "Afectaciones", true, false,
                "Mantiene la clave afectada activa y crea una nueva para la parte afectada."));
        transacciones.add(new TransaccionDataCommand("process_eliminarPredio", "Eliminación Predial", true, false,
                "Permite eliminar el poligono de la base grafica e inactiva la ficha."));
        /**
         * Construcciones:
         */
//        transacciones.add(new TransaccionDataCommand("process_editarConstruccion", "Edición Construcciones", true, false));
        //transacciones.add(new TransaccionDataCommand("process_", "Actualización Clasificación Agrológica", false, false));

        //transacciones.add(new TransaccionDataCommand("process_", "Generación de Propiedad Horizontal", false, false));
//        transacciones.add(new TransaccionDataCommand("process_updateConstruccion", "Actualización Construcciones", true, false));
//        transacciones.add(new TransaccionDataCommand("process_", "Nomenclatura Domiciliaria", false, false));
//        transacciones.add(new TransaccionDataCommand("process_", "Nomenclatura Vial", false, false));
    }

    public void selectTransaccionCommand() {
        boolean esta = false;
        prediosSeleccionados.clear();
        predio = null;
        for (TransaccionDataCommand t : transacciones) {
            if (t.getTransaccionCode().equals(transaccionSeleccionada)) {
                trasDataCommand = t;
                esta = true;
                break;
            }
        }

        if (!esta) {
            trasDataCommand = new TransaccionDataCommand();
        }

        disabledIniciar = deshabilitarIniciarTramite();

    }

    private HistoricoTramites createHistorico(GeTipoTramite tipoTramite, String message, List<CatPredio> predios, CatPredio predio) {
        HistoricoTramites ht = null;

        switch (transaccionSeleccionada) {
            case "process_crearPredio": {
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, null, true);
                String jsonManzana = jsonUtils.generarJsonStaticModel(manzanaModel);
                registrarDetalle(ht, null, jsonManzana);
                break;
            }
            case "process_eliminarPredio": {
                if (!predios.contains(predio)) {
                    predios.add(predio);
                }
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predios);
                break;
            }
            case "process_divisionPredio": {
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predio, true);
                break;
            }
            case "process_editarConstruccion": {
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predio, true);
                break;
            }
            case "process_fusionarPredios": {
                if (!predios.contains(predio)) {
                    predios.add(predio);
                }
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predios);
                break;
            }
            case "process_integracionParcelaria": {
                if (!predios.contains(predio)) {
                    predios.add(predio);
                }
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predios);
                break;
            }
            case "process_afectaciones": {
                if (!predios.contains(predio)) {
                    predios.add(predio);
                }
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predios);
                break;
            }
            case "process_actualizarLinderosAreas": {
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predios);
                break;
            }
            case "process_updateConstruccion": {
                ht = this.registrarHistoricoTramites(tipoTramite, message, message, null, predio, false);
                break;
            }
            case "process_updateDatos": {
                // Se quito la clonacion del predio debido a la confusion y elinacion de datos de las ficha original
//                CatPredio clon = procesoServices.clonarPredio(predio, Short.valueOf("99"), ProcesoUtil.getAllFilesRelationsShip(predio));
                // registramos el predio sin realizar la clonacion de datos.
                ht = this.addHistoricoTramites(tipoTramite, predio, Boolean.FALSE);

                break;
            }

        }
        return ht;
    }

    private HashMap<String, Object> parameters(GeTipoTramite tipoTramite, HistoricoTramites ht) {
        HashMap<String, Object> parametros = new HashMap<>();
        parametros.put("descripcion", tipoTramite.getDescripcion());// NOMBRE DEL TRAMITE
        parametros.put("prioridad", 50);
        parametros.put("tramite", ht.getId());
        parametros.put("dibujante", dibujante.getUsuario());
        parametros.put("revisor", revisor.getUsuario());

        String folder = "";
        switch (transaccionSeleccionada) {

            case "process_crearPredio": {
                folder = "crear";
                break;
            }
            case "process_fusionarPredios": {
                folder = "fusion";
                break;
            }
            case "process_divisionPredio": {
                folder = "division";
                break;
            }
            case "process_eliminarPredio": {
                folder = "eliminar";
                break;
            }
            case "process_integracionParcelaria": {
                folder = "integracionParcelaria";
                break;
            }
            case "process_afectaciones": {
                folder = "afectaciones";
                break;
            }
            case "process_actualizarLinderosAreas": {
                folder = "actualizarAreasLinderos";
                break;
            }
            case "process_updateConstruccion": {
                folder = "actualizarConstrucciones";
                break;
            }
            case "process_updateDatos": {
                folder = "actualizarDatos";
                break;
            }
            case "process_editarConstruccion": {
                folder = "edit-construccion";
                break;
            }

        }
        if (!transaccionSeleccionada.equals("process_updateDatos")) {
            parametros.put("formEdicionGrafica", "/vistaprocesos/catastro/bpm/" + folder + "/edicionFlujo.xhtml");
        }
        parametros.put("formEdicionAlfanumerica", "/vistaprocesos/catastro/bpm/" + folder + "/edicionFlujo.xhtml");
        parametros.put("formRevision", "/vistaprocesos/catastro/bpm/" + folder + "/revisionFlujo.xhtml");
        return parametros;
    }

    public String onFlowProcessWizard(FlowEvent event) {
        if (transaccionSeleccionada != null && predio != null) {
            if (transaccionSeleccionada.contains("process_divisionPredio") && predio.getPropiedadHorizontal()) {
                JsfUti.messageError(null, "Predio esta marcado como Propiedad Horizontal", "");
                return event.getOldStep();
            }
        }
        return event.getNewStep();
    }

    public List<GeDepartamento> departamentosList() {
        return manager.findAllOrdered(GeDepartamento.class, new String[]{"nombre"}, new Boolean[]{true});
    }

    public List<AclRol> rolesDepartamentoListDibujante() {
        if (dptoDibujante != null) {
            return manager.findAll(Querys.getListAclRolByDep, new String[]{"departamento"}, new Object[]{dptoDibujante.getId()});
        } else {
            return null;
        }
    }

    public List<AclUser> userListDibujante() {
        if (rolDibujante != null) {
            return manager.findAll(Querys.getListAclUserByRol, new String[]{"idRol"}, new Object[]{rolDibujante.getId()});
        } else {
            return null;
        }
    }

    public List<AclRol> rolesDepartamentoListRevisor() {
        if (dptoRevisor != null) {
            return manager.findAll(Querys.getListAclRolByDep, new String[]{"departamento"}, new Object[]{dptoRevisor.getId()});
        } else {
            return null;
        }
    }

    public List<AclUser> userListRevisor() {
        if (rolRevisor != null) {
            // return manager.findAll(Querys.getListAclUserByRol, new String[]{"idRol"}, new Object[]{rolRevisor.getId()});
            return manager.findAll(Querys.getListAclUserByRol, new String[]{"idRol"}, new Object[]{new Integer(216)});
        } else {
            return null;
        }
    }

    public void onRowSelectPredio(SelectEvent event) {
        CatPredio p = (CatPredio) event.getObject();
        if (transaccionSeleccionada != null && p != null) {
            if (transaccionSeleccionada.contains("process_divisionPredio") && p.getPropiedadHorizontal()) {
                JsfUti.messageError(null, "Predio esta marcado como Propiedad Horizontal", "");
                return;
            }
        }
        if (this.existePredioEnTramite(p)) {
            JsfUti.messageInfo(null, "Error", "Existe un trámite ingresado con el predio seleccionado: " + p.getClaveCat());
            return;
        }
        predio = p;
        if (trasDataCommand.isRequierePredio()) {
            JsfUti.messageInfo(null, "Info", "Predio " + predio.getClaveCat() + " seleccionado como principal.");
        } else {
            if (trasDataCommand.isRequiereLista()) {
                JsfUti.messageWarning(null, "Info", "El proceso " + trasDataCommand.getTipoTransaccion() + " solo requiere agregar predios.");
                predio = null;
            }
        }

        disabledIniciar = deshabilitarIniciarTramite();
        System.out.println("Disabled rowSelect: " + disabledIniciar);
    }

    public void onRowuNSelectPredio(UnselectEvent event) {
        predio = null;
    }

    public void addPredio(CatPredio p) {
        if (this.existePredioEnTramite(p)) {
            JsfUti.messageInfo(null, "Error", "Existe un trámite ingresado con el predio seleccionado: " + p.getClaveCat());
            return;
        }
        if (trasDataCommand.getTransaccionCode().equals("process_integracionParcelaria")) {
            if (prediosSeleccionados.size() == 1) {
                JsfUti.messageWarning(null, "Info", "Para este proceso solo puede adicionar 1 predio.");
                return;
            }
        }
        if (predio != null) {
            if (!Objects.equals(p.getId(), predio.getId())) {
                if (!prediosSeleccionados.contains(p)) {
                    prediosSeleccionados.add(p);
                    JsfUti.messageInfo(null, "Info", "Predio adicionado con exito.");
                } else {
                    JsfUti.messageWarning(null, "Info", "Predio ya se encuentra adicionado.");
                }
            }
        } else {
            if (!prediosSeleccionados.contains(p)) {
                prediosSeleccionados.add(p);
                JsfUti.messageInfo(null, "Info", "Predio adicionado con exito.");
            } else {
                JsfUti.messageWarning(null, "Info", "Predio ya se encuentra adicionado.");
            }
        }

        disabledIniciar = deshabilitarIniciarTramite();

        System.out.println("Disabled add: " + disabledIniciar);

    }

    public void deletePredio(CatPredio p) {
        if (prediosSeleccionados.contains(p)) {
            prediosSeleccionados.remove(p);
            JsfUti.messageInfo(null, "Info", "Predio eliminado con exito.");
        }
        disabledIniciar = deshabilitarIniciarTramite();

        System.out.println("Disabled delete: " + disabledIniciar);
    }

    private boolean checkColindantes(List<String> codigos) {

        try {
            if (trasDataCommand.getTransaccionCode().equals("process_integracionParcelaria")) {
                codigos.add(predio.getClaveCat());
                integracionParcelariaServ.checkValidPolygons(codigos);
            }
            if (trasDataCommand.getTransaccionCode().equals("process_fusionarPredios")) {
                codigos.add(predio.getClaveCat());
                fusionServ.checkValidPolygons(codigos);
            }

            return true;
        } catch (Exception e) {
            JsfUti.messageError(null, "Info.", "Los predios seleccionados no son colindantes");
            return false;

        }

    }

    public boolean deshabilitarIniciarTramite() {
        boolean requierePredio = false, requiereListado = false;

        if (trasDataCommand.isRequierePredio()) {
            requierePredio = predio != null;

        }
        if (trasDataCommand.isRequiereLista()) {
            requiereListado = !prediosSeleccionados.isEmpty();
        }

        return !((requierePredio == trasDataCommand.isRequierePredio()) && (requiereListado == trasDataCommand.isRequiereLista()));
    }

    /**
     * return true si existe.
     *
     * @return
     */
    public boolean existeNumTramite() {
        if (numTramite == null || numTramite.trim().isEmpty()) {
            JsfUti.messageError(null, "Error", "Debe ingresar el número de trámite");
            return false;
        } else {
            try {
                Radicado find = quipux.getDataQuipux(numTramite);
                return find != null;
//                    return true;
            } catch (Exception e) {
                JsfUti.messageError(null, "Error", "No se pudo verificar si existe el número de trámite");
                return true;
            }
        }
    }

//<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public boolean isRenderListado() {
        renderListado = trasDataCommand.getTransaccionCode().equals("process_fusionarPredios")
                || trasDataCommand.getTransaccionCode().equals("process_integracionParcelaria")
                || trasDataCommand.getTransaccionCode().equals("process_actualizarLinderosAreas");
        return renderListado;
    }

    public void setRenderListado(boolean renderListado) {
        this.renderListado = renderListado;
    }

    public boolean isDisabledIniciar() {

        return disabledIniciar;
    }

    public void setDisabledIniciar(boolean disabledIniciar) {
        this.disabledIniciar = disabledIniciar;
    }

    public TransaccionDataCommand getTrasDataCommand() {
        return trasDataCommand;
    }

    public void setTrasDataCommand(TransaccionDataCommand trasDataCommand) {
        this.trasDataCommand = trasDataCommand;
    }

    public List<TransaccionDataCommand> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<TransaccionDataCommand> transacciones) {
        this.transacciones = transacciones;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public List<CatPredio> getPrediosSeleccionados() {
        return prediosSeleccionados;
    }

    public void setPrediosSeleccionados(List<CatPredio> prediosSeleccionados) {
        this.prediosSeleccionados = prediosSeleccionados;
    }

    public GeDepartamento getDptoDibujante() {
        return dptoDibujante;
    }

    public void setDptoDibujante(GeDepartamento dptoDibujante) {
        this.dptoDibujante = dptoDibujante;
    }

    public AclRol getRolDibujante() {
        return rolDibujante;
    }

    public void setRolDibujante(AclRol rolDibujante) {
        this.rolDibujante = rolDibujante;
    }

    public AclUser getDibujante() {
        return dibujante;
    }

    public void setDibujante(AclUser dibujante) {
        this.dibujante = dibujante;
    }

    public GeDepartamento getDptoRevisor() {
        return dptoRevisor;
    }

    public void setDptoRevisor(GeDepartamento dptoRevisor) {
        this.dptoRevisor = dptoRevisor;
    }

    public AclRol getRolRevisor() {
        return rolRevisor;
    }

    public void setRolRevisor(AclRol rolRevisor) {
        this.rolRevisor = rolRevisor;
    }

    public AclUser getRevisor() {
        return revisor;
    }

    public void setRevisor(AclUser revisor) {
        this.revisor = revisor;
    }

    public String getTransaccionSeleccionada() {
        return transaccionSeleccionada;
    }

    public void setTransaccionSeleccionada(String transaccionSeleccionada) {
        this.transaccionSeleccionada = transaccionSeleccionada;
    }

    public CatPredioLazy getPrediosLazy() {
        return prediosLazy;
    }

    public void setPrediosLazy(CatPredioLazy prediosLazy) {
        this.prediosLazy = prediosLazy;
    }

    public ManzanaModel getManzanaModel() {
        return manzanaModel;
    }

    public void setManzanaModel(ManzanaModel manzanaModel) {
        this.manzanaModel = manzanaModel;
    }

    public String getNumTramite() {
        return numTramite;
    }

    public void setNumTramite(String numTramite) {
        this.numTramite = numTramite;
    }
//</editor-fold>

    private boolean existePredioEnTramite(CatPredio predio) {
        if (Objects.nonNull(predio)) {
            java.util.Map<String, Object> pm = new HashMap<>();
            pm.put("numPredio", predio.getNumPredio());
            pm.put("estado", "Pendiente");
            List<HistoricoTramites> hts = manager.findObjectByParameterList(HistoricoTramites.class, pm);
            if (Utils.isNotEmpty(hts)) {
                return true;
            } else {
                pm = new HashMap<>();
                pm.put("predio", predio);
                List<HistoricoTramiteDet> dets = manager.findObjectByParameterList(HistoricoTramiteDet.class, pm);
                for (HistoricoTramiteDet det : dets) {
                    if (det.getTramite().getEstado().trim().contains("Pendiente")) {
                        return true;
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }

    public List<AclUser> getRevisores() {
        return revisores;
    }

    public void setRevisores(List<AclUser> revisores) {
        this.revisores = revisores;
    }

}
