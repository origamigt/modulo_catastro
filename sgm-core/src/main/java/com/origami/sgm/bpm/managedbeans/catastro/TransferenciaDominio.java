/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.censocat.restful.JsonUtils;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.util.ValidField;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatCanton;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatEscrituraPropietario;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatTiposDominio;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.events.GenerarHistoricoPredioPost;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIInput;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.Hibernate;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import util.Faces;
import util.HiberUtil;
import util.JsfUti;
import util.Utils;

/**
 * Permite realizar el proceso de transferecia de dominio en dos pasos el
 * primero imprime la ficha de actualizacion catastral y despues de realiza el
 * acentamiento de la informacion.
 *
 * @author dfcalderio
 * @override Angel Navarro
 */
@Named(value = "transferenciaView")
@ViewScoped
public class TransferenciaDominio implements Serializable {

    /**
     * Variables de session del usuario logeado
     */
    @Inject
    protected UserSession sess;
    /**
     * Ejb transaccional de catastro
     */
    @Inject
    protected CatastroServices catastroService;
    /**
     * Manager Transaccional de hibernate
     */
    @Inject
    protected Entitymanager manager;
    /**
     * Cdi de session para almacenar datos temporales
     */
    @Inject
    protected ServletSession ss;

    /**
     * Evento para generar el historico del cambio de propietario.
     */
    @Inject
    protected Event<GenerarHistoricoPredioPost> eventHistorico;

    protected List<CatPredioPropietario> propietarios;
    protected List<CatPredioPropietario> propietariosNuevos;

    protected CatEscritura escritura;
    protected List<CatCanton> cantones;
    protected List<CatEscrituraPropietario> propietariosEscrituras;
    protected List<CatEscritura> escrituras;

    protected boolean addEscritura;
    protected CatPredio predio;
    protected Long idPredio;
    protected CtlgItem formaAdquisicion;

    protected String codigo;
    protected Integer idFact = null;
    /**
     * VALRIABLE PARA IDENTIFICAR LAS TRANFERENCIAS QUE SOLO FUERON IMPRESAS
     */
    protected Boolean imprimir = false;
    protected Boolean accionesDerechos = false;

    /**
     * Variable para almacenar JSon del predia anterior
     */
    protected String predioAnterior;
    /**
     * Variable para almacenar JSon del predia actual
     */
    protected String predioActual;
    protected String observacion;
    // CLASE PERMITE GENERAR UN JSOON DE   EL PREDIO
    protected JsonUtils js;

    /**
     * Variable para seleccionar al propietario que realiza la venta de acciones
     */
    private CatPredioPropietario propietarioSeleccionado;

    /**
     * Inicializa las variables para realizar el proceso de transferecia de
     * dominio, si la variable 'imprimir' enviada por session es true quiere
     * decir que se va imprimir el formulario de actualizacion catastral, caso
     * contrario inicializa los datos para acentar la transferecnia.
     *
     */
    @PostConstruct
    public void init() {
        if (!JsfUti.isAjaxRequest()) {
            js = new JsonUtils();
            cantones = getCantones();
            try {
                if (sess != null) {

                    if (ss.getParametros() != null && ss.getParametros().get("idPredio") != null) {
                        idPredio = Long.parseLong(ss.getParametros().get("idPredio").toString());
                        if (idPredio == null) {
                            JsfUti.redirectFaces("/vistaprocesos/catastro/predios.xhtml");
                        }
                        // CHEQUEMOS SI LA VARIABLE ES TRUE ENTONCES ES TEMPORAL Y
                        // RECIEN SE VA REALIZAR EL PROCESO DE CAMBIO
                        imprimir = Boolean.valueOf(ss.getParametros().get("imprimir").toString());
                        predio = new CatPredio();
                        predio = catastroService.getPredioId(idPredio);
                        if (predio != null) {
                            predioAnterior = js.generarJson(predio);
                            if (predio.getCatPredioPropietarioCollection() != null) {
                                propietarios = (List<CatPredioPropietario>) manager.findAll(Querys.getCatPropietariosByPredio,
                                        new String[]{"id"}, new Object[]{predio.getId()});
                            }
                            propietariosNuevos = new LinkedList<>();
                            escritura = new CatEscritura();
                            escrituras = manager.findAll(Querys.getEscriturasByPredioDescId, new String[]{"idPredio"}, new Object[]{predio.getId()});
                            if (escrituras == null) {
                                escrituras = new LinkedList<>();
                            }
                            if (!imprimir) {
                                if (ss.tieneParametro("idTranferecnia")) {
                                    List<CatEscritura> temp = manager.findAll(Querys.getEscriturasByPredioDescIdTransf, new String[]{"idTransferecnia"}, new Object[]{ss.getParametro("idTranferecnia")});
                                    if (Utils.isNotEmpty(temp)) {
                                        escrituras.addAll(temp);
                                        escritura = temp.get(0);
                                    }
                                    propietariosNuevos = (List<CatPredioPropietario>) manager.findAll(Querys.getCatPropietariosByPredioTempTransf,
                                            new String[]{"idTransferecnia"}, new Object[]{ss.getParametro("idTranferecnia")});
                                } else {
                                    propietariosNuevos = (List<CatPredioPropietario>) manager.findAll(Querys.getCatPropietariosByPredioTemp,
                                            new String[]{"id"}, new Object[]{predio.getId()});
                                    List<CatEscritura> temp = manager.findFirstAndMaxResult(Querys.getEscriturasByPredioDescIdTemp,
                                            new String[]{"idPredio"}, new Object[]{predio.getId()}, 1, 1);
                                    if (Utils.isNotEmpty(temp)) {
                                        escritura = temp.get(0);
                                    }
                                }

                                for (CatPredioPropietario pn : propietariosNuevos) {
                                    if (Utils.isNotEmpty(pn.getCatEscrituraPropietarioList())) {
                                        formaAdquisicion = pn.getCatEscrituraPropietarioList().get(0).getFormaAdquisicion();
                                    }
                                }
                                if (formaAdquisicion == null) {
                                    formaAdquisicion = predio.getFormaAdquisicion();
                                }

                            }
                        } else {
                            JsfUti.redirectFaces("/vistaprocesos/catastro/predios.xhtml");
                        }
                    } else {
                        JsfUti.redirectFaces("/vistaprocesos/catastro/predios.xhtml");
                    }
                }

            } catch (NumberFormatException e) {
                Logger.getLogger(TransferenciaDominio.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * Procesa las acciones del nuevo propietario si las acciones son iguales al
     * del nuevo propietario entonces se inactivara al vendedor y se activa al
     * nuevo propietario, para el caso de acciones y derechos se raliza la resta
     * de las acciones compradas al propietario seleccionado para el caso que
     * haya mas de uno caso contrario toma al primero
     */
    public void transferenciaDominio() {
        try {
            predio.setFormaAdquisicion(formaAdquisicion);
            if (Utils.isNotEmpty(propietarios)) {
                // SI SOLO HAY UN PROPIETARIO DEJAMOS CORRER NORMAL Y VERIFICAMOS
                //EL PORCENTAJE DE ACCION QUE SE ESTA VENDIENDO PARA PROCESAR AL PROPIETARIO
                BigDecimal sumarAcciones = this.sumarAccionesNuevosPropietarios();
                if (sumarAcciones == null) {
                    JsfUti.messageInfo(null, "Info!", "Unos de los propietarios no tiene porcentaje de aciones.");
                    return;
                }
                if (sumarAcciones.doubleValue() > 100) {
                    JsfUti.messageInfo(null, "Info!", "La suma de las acciones es mayor al 100%.");
                    return;
                }
                /**
                 * Para el caso que no sea acciones tambien ingresara en esta
                 * opcion. Si se compra acciones y hay un solo propietario
                 */
                if (propietarios.size() == 1) {
                    propietarioSeleccionado = propietarios.get(0);
                }

                // controlar que se escoga un propietario para realizar la tranferecnia de dominio
                if (propietarioSeleccionado == null) {
                    JsfUti.messageInfo(null, "Info!", "Debe seleccionar al propietario que realiza la venta de acciones.");
                    return;
                }
                // VALIDACION PARA ACCIONES Y DERECHOS
                if (sumarAcciones.doubleValue() > propietarioSeleccionado.getPorcentajePosecion().doubleValue()) {
                    JsfUti.messageInfo(null, "Info!", "La suma de las acciones es mayor a la acciones del propietario.");
                    return;
                }
                /**
                 * Procesamos las acciones
                 */
                if (this.procesarAcciones(propietarioSeleccionado, sumarAcciones)) {
                    JsfUti.messageInfo(null, "Info!", "Acciones procesadas correctamente.");
                } else {
                    return;
                }
            }
            int numeroEscrituras = escrituras.size();
            // Actualizamos el estado del
            escrituras.stream().map((e) -> {
                if (e.getEstado().equalsIgnoreCase(EstadosPredio.TEMPORAL)) {
                    e.setEstado(EstadosPredio.ACTIVO);
                } else {
                    if (numeroEscrituras > 1) {
                        if (!accionesDerechos) {
                            e.setEstado(EstadosPredio.INACTIVO);
                        }
                    }
                }
                return e;
            }).forEachOrdered((e) -> {
                manager.persist(e);
            });
            /**
             * Actualizamos el estado del propietario que compra
             */
            propietariosNuevos.stream().map((pt) -> {
                pt.setEstado(EstadosPredio.ACTIVO);
                return pt;
            }).forEachOrdered((pt) -> {
                manager.persist(pt);
            });
            // Aqui incluir condicion de tipo de domino: Maly
//            for (CatPredioPropietario propietarioAct : predio.getCatPredioPropietarioCollection()) {
//                if(propietarioAct.getCiuCedRuc() == "1001"){
//                    predio.setPropiedad(new CatPropiedadItem(2l));
//                }
//            }
            /**
             * Actualizamos el predio
             */
            manager.persist(predio);
            /**
             * Generamos el historico del predio
             */
            eventHistorico.fire(new GenerarHistoricoPredioPost(predioAnterior, js.generarJson(predio), "Tranferencia de dominio", sess.getName_user(), predio));
            ss.borrarParametros();
            JsfUti.messageInfo(null, "Nota!", "Transferencia de dominio realizada con éxito.");
            JsfUti.redirectFaces("/faces/vistaprocesos/catastro/predios.xhtml");
        } catch (Exception e) {
            Logger.getLogger(TransferenciaDominio.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Crea los registro de los y los pasa a un estado Temporal 'X'
     */
    public void transferenciaDominioImprimir() {
        try {
            //System.out.println("ROL: "+ sess.getRoles());
            // System.out.println("USUARIO: "+ sess.getName_user());
            if (this.getIdFact() == false) {
                if (!sess.getName_user().equals("VDavila")) {

                    JsfUti.messageInfo(null, "Error", "No tine factura disponible");
                    return;
                }

            }
            // System.out.println("Factura " + this.idFact);

            if (propietariosNuevos.isEmpty()) {
                JsfUti.messageInfo(null, "Error", "No ha ingresado los propietarios");
                return;
            }
            if (escritura.getIdEscritura() == null) {
                JsfUti.messageInfo(null, "Error", "No ha registrado la escritura");
                return;
            }
            Hibernate.initialize(predio.getPredioCollection());
            escrituras.remove(escritura);
            escritura.setPredio(predio);
            escritura.setIdEscritura(null);
            escritura.setEstado(EstadosPredio.TEMPORAL);
            escritura = (CatEscritura) manager.persist(escritura);
            escritura.setPredio(predio);
            // INICIAMOS EL MAPA DE PARAMETROS PARA EL REPORTE
            ss.instanciarParametros();
            //datos del reporte
            ss.setNombreDocumento("actualizacionCatastral" + escritura.getNumeroActualizacion());
            ss.setNombreSubCarpeta("/catastro/certificados/");
            ss.setNombreReporte("certificadoActualizacionCatastral");
            // parametros del reporte
            ss.agregarParametro("LOGO", SisVars.logoReportes);
            ss.agregarParametro("LOGO_1", SisVars.sisLogo1);
            String footer = SisVars.sisLogo1.substring(0, SisVars.sisLogo1.length() - 6) + "-footer.png";
            ss.agregarParametro("LOGO_FOOTER", footer);
            ss.agregarParametro("TITULO", SisVars.NOMBREMUNICIPIO);
            CatEscrituraPropietario ep = null;
            String aFavor = "";
            for (CatPredioPropietario pt : propietariosNuevos) {
                pt.setEstado(EstadosPredio.TEMPORAL);
                pt.setId(null);
                pt = (CatPredioPropietario) manager.persist(pt);
                ep = new CatEscrituraPropietario();
                ep.setEnte(pt.getEnte());
                ep.setEscritura(escritura);
                ep.setPropietario(pt);
                ep.setUsuario(sess.getName_user());
                ep.setFecha(new Date());
                ep.setFormaAdquisicion(formaAdquisicion);
                ep.setObservaciones(observacion);
                if (pt.getCopropietario()) {
                    ep.setCopropietario(Boolean.TRUE);
                    ep.setPorcentajePosecion(pt.getPorcentajePosecion());
                }
                if (aFavor.isEmpty()) {
                    aFavor = aFavor.concat(pt.getNombresCompletos());
                } else {
                    aFavor = aFavor.concat(" Y ").concat(pt.getNombresCompletos());
                }
                manager.persist(ep);
            }
            ss.agregarParametro("A_FAVOR", aFavor);
            escritura.setPredio(predio);
            ep.setEscritura(escritura);

            this.cambiarEstadoFacturaRentas(predio.getClaveCat(), escritura.getNumeroActualizacion());

//            eventHistorico.fire(new GenerarHistoricoPredioPost(predioAnterior, js.generarJson(predio), "Tranferencia de dominio", sess.getName_user(),predio));
            Faces.messageInfo(null, "Nota!", "Impresion de Transferencia de dominio realizada con exito.");
            ss.agregarParametro("SUBREPORT_DIR", SisVars.REPORTES + "/");
            ss.agregarParametro("NOMBRE_CERTIFICADO", "CERTIFICADO DE ACTUALIZACION CATASTRAL");
            ss.agregarParametro("FUNCIONARIO", sess.getNombrePersonaLogeada());
            ss.setTieneDatasource(Boolean.FALSE);
            ss.setDataSource(Arrays.asList(ep));
            Faces.redirectFacesNewTab("/Documento");
            Faces.redirectFaces("/faces/vistaprocesos/catastro/transferenciaDominio/transferenciasDominios.xhtml");
        } catch (Exception e) {
            Logger.getLogger(TransferenciaDominio.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Envia los datos al formulario de propietarios para realizar la edicion o
     * ingreso nuevo
     *
     * @param propietario Nulo si es nuevo
     * @param editar Opcion para indicar si va realizar una edicion sobre el
     * propietario
     */
    public void propietario(CatPredioPropietario propietario, Boolean editar) {
        if (ss.estaVacio()) {
            ss.instanciarParametros();
        }
        ss.agregarParametro("DatosTemp", true);
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (propietario != null && propietario.getId() != null) {
            p.add(propietario.getId().toString());
        }
        params.put("idCatPredioPro", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        params.put("nuevo", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        p = new ArrayList<>();
        p.add(editar.toString());
        params.put("editar", p);

        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("width", "85%");
        options.put("height", "450");
        options.put("closable", true);
        options.put("contentWidth", "100%");
        RequestContext.getCurrentInstance().openDialog("/resources/dialog/propietarios_1", options, params);
    }

    /**
     * Recive el evento de propietario que se edito o ingreso en la formulario
     * de propietarios
     *
     * @param event
     */
    public void procesarPropietario(SelectEvent event) {
        CatPredioPropietario propietario = (CatPredioPropietario) event.getObject();

        if (propietario != null) {
            propietario.setId((long) (propietariosNuevos.size() + 1));
            if (!propietariosNuevos.contains(propietario)) {
                propietariosNuevos.add(propietario);
            } else {
                propietariosNuevos.set(propietariosNuevos.indexOf(propietario), propietario);
            }
            imprimir = Boolean.valueOf(ss.getParametros().get("imprimir").toString());
//            this.setFichaEdifAnt(this.getFichaEdifAct());
//            catast.guardarHistoricoPredio(predio.getNumPredio().longValue(), this.getPredioAnt(), gson2.toJson(predio), sess.getName_user(), "Actualizacion Informacion de propietarios ", getFichaEdifAnt(), getFichaEdifAct(), getFichaModelAnt(), getFichaModelAct(), getDocumento());
            Faces.messageInfo(null, "Nota!", "Propietarios actualizados satisfactoriamente");
        }
    }

    /**
     * Inactiva el propietario
     *
     * @param propietario CatPredioPropietario a inactivar
     */
    public void eliminarPropietario(CatPredioPropietario propietario) {
        propietario.setEstado(EstadosPredio.INACTIVO);
        propietario.setModificado(sess.getName_user());
        //propietario = catastroService.guardarPropietario(propietario, sess.getName_user());

        propietariosNuevos.remove(propietario);
        JsfUti.messageInfo(null, "Propietario", "Propietario eliminado.");
    }

    /**
     * Inicializa la lista de cantones
     */
    public void saveEscrituraControl() {
        cantones = manager.findAllEntCopy(CatCanton.class);
    }

    /**
     * Agrega a la lista de escrituras, e inactivas las otras
     */
    public void saveEscritura() {
        String msg = "adicionada";
        if (escrituras.contains(escritura)) {
            escrituras.remove(escritura);
            msg = "editada";
        }
        escritura.setIdEscritura(new Long("0"));
        escritura.setFecCre(new Date());
        escritura.setEstado(EstadosPredio.ACTIVO);
        escrituras.forEach((e) -> {
            e.setEstado(EstadosPredio.INACTIVO);
        });
        escrituras.add(0, escritura);

        JsfUti.messageInfo(null, "Info !", "Escritura " + msg + " satisfactoriamente.");
        JsfUti.executeJS("PF('dlgEscritura').hide()");
        try {

            //this.predio.setCatEscrituraCollection(escriturasConsulta);
//                if (saveHistoric(predio, "ACTUALIZACION DE ESCRITURAS", getFichaEdifAnt(), getFichaEdifAct(), getFichaModelAnt(), getFichaModelAct())) {
//                    JsfUti.messageInfo(null, "Exito", "Datos grabados Satisfactoriamente");
//                } else {
//                    JsfUti.messageInfo(null, "Exito", "Datos grabados Satisfactoriamente");
//                }
        } catch (Exception e) {
            JsfUti.messageInfo(null, "Error al Guardar", "");
        }
    }

    /**
     * Habre el dialogFramework de las escrituras para ingresar o editar las
     * escrituras
     *
     * @param propietario
     */
    public void saveEscrituraControl(CatEscritura propietario) {
        if (ss.estaVacio()) {
            ss.instanciarParametros();
        }
        ss.agregarParametro("imprimir", imprimir);
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (propietario != null && propietario.getIdEscritura() != null) {
            p.add(propietario.getIdEscritura().toString());
        }
        ss.getParametros().remove("DatosTemp");
        params.put("idEscritura", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
            ss.agregarParametro("DatosTemp", true);
        } else {
            p.add("false");
        }
        params.put("nuevo", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        p = new ArrayList<>();
        params.put("editar", p);
        Utils.openDialog("/resources/dialog/escritura", params, "350", "80");
    }

    /**
     * Obtiene los datos enviados por el dialogFramework de escrituras
     *
     * @param event
     */
    public void procesarEscritura(SelectEvent event) {
        imprimir = Boolean.valueOf(ss.getParametros().get("imprimir").toString());
        escritura = (CatEscritura) event.getObject();
        int index = escrituras.indexOf(escritura);
        if (index >= 0) {
            escrituras.set(index, escritura);
        } else {
            escrituras.add(0, escritura);
        }
//        if (escritura.getEstado().equalsIgnoreCase(EstadosPredio.TEMPORAL)
//                && (escritura.getIdEscritura() != null && escritura.getIdEscritura() > 0)) {
//            imprimir = true;
//        }
    }

    /**
     * Pasa los datos al dialogo para realizar la edicion de la escritura.
     *
     * @param e Escritura a editar
     */
    public void editEscritura(CatEscritura e) {
        escritura = e;

        try {

        } catch (Exception ex) {
            JsfUti.messageInfo(null, "Error al Guardar", "");
        }
    }

    /**
     * Quita la escritura de la lista
     *
     * @param e
     */
    public void deleteEscritura(CatEscritura e) {
        escrituras.remove(e);
        escritura = new CatEscritura();

        try {

        } catch (Exception ex) {
            JsfUti.messageInfo(null, "Error al Guardar", "");
        }
    }

    /**
     * obtiene la lista por el tipo de catalogo que se le pase
     *
     * @param argumento Nombre del catalogo a obtener
     * @return Lista de CtlgItem.
     */
    public List<CtlgItem> getListado(String argumento) {
        HiberUtil.newTransaction();
        List<CtlgItem> ctlgItem = (List<CtlgItem>) manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{argumento});
        return ctlgItem;
    }

    /**
     * Consulta todas los tipo de transferecia de domino que hay
     *
     * @return Lista de CatTiposDominio
     */
    public List<CatTiposDominio> getDominios() {
        return manager.findAllObjectOrder(CatTiposDominio.class,
                new String[]{"nombre"}, true);
    }

    /**
     * Validacion de campos del formulario
     *
     * @param event
     */
    public void validarFormulario(ComponentSystemEvent event) {

        FacesContext fc = FacesContext.getCurrentInstance();

        UIComponent components = event.getComponent();
        UIForm form = (UIForm) components.findComponent("frmDlgEscr");

        Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_UNRENDERED);
        ValidField visitor = new ValidField();

        form.visitTree(VisitContext.createVisitContext(fc, null, hints), visitor);

        int errores = 0;

        UIInput uiCanton = (UIInput) components.findComponent("cmbCanton");
        String canton = uiCanton.getLocalValue() == null ? ""
                : uiCanton.getLocalValue().toString();

        UIInput uiTipoPro = (UIInput) components.findComponent("tipoProtclz");
        String tipoPro = uiTipoPro.getLocalValue() == null ? ""
                : uiTipoPro.getLocalValue().toString();

        if (canton.equals("")) {
            errores++;
            uiCanton.setValid(false);
        }
        if (tipoPro.equals("")) {
            errores++;
            uiTipoPro.setValid(false);
        }

        errores += visitor.getInvalidFields();

        if (errores != 0) {
            FacesMessage msg = new FacesMessage();
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            msg.setDetail("Existen errores en el formulario");
            fc.addMessage(null, msg);
        }

        fc.renderResponse();
    }

    /**
     * Verifica si la escritura debe ser deshabilitada
     *
     * @param e CatEscritura a verificar
     * @return true si se debe desahabilitar caso contrario false
     */
    public Boolean disabledAdd(CatEscritura e) {
        return (e.getIdEscritura() != 0 || e.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO));
    }

    /**
     * Verifica si solo se va imprimir la tranferencia o se la va acentar en la
     * base
     *
     * @return
     */
    public Boolean disabledBtnTransferencia() {
        return !(escritura.getIdEscritura() != null && !propietariosNuevos.isEmpty()) && !imprimir;
    }

    //<editor-fold defaultstate="collapsed" desc="GETTER AND SETTER">
    /**
     * Obtiene los propietarios cargados en inicio
     *
     * @return
     */
    public List<CatPredioPropietario> getPropietarios() {
        return propietarios;
    }

    /**
     * Permite sobreescribir los propietarios cargados en el inicio
     *
     * @param propietarios Lista de nuevos propietarios
     */
    public void setPropietarios(List<CatPredioPropietario> propietarios) {
        this.propietarios = propietarios;
    }

    /**
     * Datos de la escritura nueva
     *
     * @return
     */
    public CatEscritura getEscritura() {
        return escritura;
    }

    public void setEscritura(CatEscritura escritura) {
        this.escritura = escritura;
    }

    public List<CatEscrituraPropietario> getPropietariosEscrituras() {
        return propietariosEscrituras;
    }

    public void setPropietariosEscrituras(List<CatEscrituraPropietario> propietariosEscrituras) {
        this.propietariosEscrituras = propietariosEscrituras;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public Long getIdPredio() {
        return idPredio;
    }

    public void setIdPredio(Long idPredio) {
        this.idPredio = idPredio;
    }

    public List<CatEscritura> getEscrituras() {
        return escrituras;
    }

    public void setEscrituras(List<CatEscritura> escrituras) {
        this.escrituras = escrituras;
    }

    public boolean isAddEscritura() {
        return addEscritura;
    }

    public void setAddEscritura(boolean addEscritura) {
        this.addEscritura = addEscritura;
    }

    public List<CatPredioPropietario> getPropietariosNuevos() {
        return propietariosNuevos;
    }

    public void setPropietariosNuevos(List<CatPredioPropietario> propietariosNuevos) {
        this.propietariosNuevos = propietariosNuevos;
    }

    public List<CatCanton> getCantones() {
        return cantones;
    }

    public void setCantones(List<CatCanton> cantones) {
        this.cantones = cantones;
    }

    public String getPredioAnterior() {
        return predioAnterior;
    }

    public void setPredioAnterior(String predioAnterior) {
        this.predioAnterior = predioAnterior;
    }

    public String getPredioActual() {
        return predioActual;
    }

    public void setPredioActual(String predioActual) {
        this.predioActual = predioActual;
    }

    public CtlgItem getFormaAdquisicion() {
        return formaAdquisicion;
    }

    public void setFormaAdquisicion(CtlgItem formaAdquisicion) {
        this.formaAdquisicion = formaAdquisicion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Boolean getImprimir() {
        return imprimir;
    }

    public void setImprimir(Boolean imprimir) {
        this.imprimir = imprimir;
    }

    public CatPredioPropietario getPropietarioSeleccionado() {
        return propietarioSeleccionado;
    }

    public void setPropietarioSeleccionado(CatPredioPropietario propietarioSeleccionado) {
        this.propietarioSeleccionado = propietarioSeleccionado;
    }
//</editor-fold>

    /**
     * Suma todas las acciones del nuevo propietario.
     *
     * @return
     */
    private BigDecimal sumarAccionesNuevosPropietarios() {
        BigDecimal sumaAcciones = BigDecimal.ZERO;
        if (Utils.isNotEmpty(propietariosNuevos)) {
            for (CatPredioPropietario propietario : propietariosNuevos) {
                if (propietario.getPorcentajePosecion() != null) {
                    sumaAcciones = sumaAcciones.add(propietario.getPorcentajePosecion());
                } else {
                    return null;
                }
            }
        }
        return sumaAcciones;
    }

    /**
     * Procesa las acciones que se venden el proietario si son iguales las
     * acciones a la del vendedor entonces se inactiva el propietario que esta
     * vendiendo caso contrario solo realiza la resta de las acciones.
     *
     * @param pt Propietario que esta vendiendo.
     * @param sumarAcciones Acciones del propietario que compra
     * @return True si todo se realizo con normalidad, caso contrario retorna
     * false.
     */
    private Boolean procesarAcciones(CatPredioPropietario pt, BigDecimal sumarAcciones) {
        try {
            /**
             * Para el caso que el porcentaje de posesion sea igual al del nuevo
             * propietario entonces se desactivara el proietario que vende
             */
            System.out.println("Acciones son iguales " + sumarAcciones.compareTo(pt.getPorcentajePosecion()));
            System.out.println("Acciones son iguales " + (sumarAcciones.compareTo(pt.getPorcentajePosecion()) == 0));
            System.out.println("Acciones son iguales " + sumarAcciones);
            System.out.println("Acciones son iguales " + pt.getPorcentajePosecion());
            if (sumarAcciones.compareTo(pt.getPorcentajePosecion()) == 0) {
                System.out.println("Propietario anterio sera inactivado.");
                pt.setEstado(EstadosPredio.INACTIVO);
                predio.setCedulaPropAnterior(pt.getCiuCedRuc());
                predio.setPropAnteriorPredio(pt.getNombresCompletos());
            } else { // Caso contrario se hara la resta al propietario que vende
                accionesDerechos = true;
                pt.setPorcentajePosecion(pt.getPorcentajePosecion().subtract(sumarAcciones));
                System.out.println("Restando acciones a Propietario seleccionado: " + sumarAcciones);
            }
            /**
             * Guardamos los cambios realizado sobre el propietario que esta
             * vendiendo.
             */
            manager.persist(pt);
            return true;
        } catch (Exception e) {
            JsfUti.messageError(null, "Error al procesar acciones.", e.getMessage());
            return false;
        }
    }

    private boolean getIdFact() {
        String sqlGetId = "SELECT be_id as id FROM rentas.ren_bandeja_especies "
                + "WHERE (be_contribuyente_facrtua= '%s' OR be_factura= '%s') AND "
                + "(be_estado='F' OR be_estado='X') AND be_especie=8 LIMIT 1";
        Integer id = (Integer) manager.getNativeQuery(String.format(sqlGetId, codigo, codigo));
        if (id != null) {
            idFact = id;
            return true;
        } else {
            System.out.println("No se puedo obtener el id de la especie usuario " + this.sess.getName_user());
        }
        return false;
    }

    private Boolean cambiarEstadoFacturaRentas(String claveCat, BigInteger numCert) {
        System.out.println("Cambiar estado Factura" + idFact);
        if (idFact != null) {
            System.out.println("Actualizando fac" + idFact);
            String update = "UPDATE rentas.ren_bandeja_especies SET be_estado = 'I', "
                    + "be_contribuyente = ?, be_clave_catastral = ?, "
                    + "be_usuario_imprime = ?, be_numero_copia = ?,be_fecha_imprime=? "
                    + "WHERE be_id = " + idFact;
            try {
                AclUser user = manager.find(AclUser.class, sess.getUserId());
                return manager.executeNativeQuery(update, new Object[]{propietarios.get(0).getCiuCedRuc(), claveCat,
                    (user.getUsuarioSigm() == null ? sess.getName_user() : user.getUsuarioSigm()), numCert,
                    new Date()});
            } catch (Exception e) {
                System.out.println("Error el actualizar factura cambiarEstadoFacturaRentas: " + e.getMessage());
            }
            //idFact = null;
            this.getIdFact();
            System.out.println("Siguiente Fact: " + idFact);
        } else {
            System.out.println("No se puedo cambiar el estado de la factura del certificado... cambiarEstadoFacturaRentas");
        }
        return false;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

}
