/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.procesos;

import com.origami.censocat.restful.JsonUtils;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.entities.CatEdificacionPisosDet;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 * Controlador para la tarea de Actualizacion de construciones(Proceso no
 * utilidado actualmente.)
 *
 * @author dfcalderio
 */
@Named(value = "actualizarConstruccionFlow")
@ViewScoped
public class ActualizarConstrucciones extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(ActualizarConstrucciones.class.getName());

    @Inject
    private ServletSession ss;

    @EJB
    private Entitymanager manager;

    protected HistoricoTramites ht;

    private CatPredio predio;
    private List<CatPredioEdificacion> edificaciones;

    private JsonUtils jsonUtil;

    private Long tempID;
    protected String detalle;
    protected FormatoReporte formato;
    @Inject
    private CatastroServices catastroService;

    @PostConstruct
    public void init() {
        tempID = Long.MIN_VALUE;
        jsonUtil = new JsonUtils();
        edificaciones = new LinkedList<>();
        if (session != null && session.getTaskID() != null) {
            ht = new HistoricoTramites();
            setTaskId(session.getTaskID());
            ht = this.getHistoricoTramiteById(new Long(this.getVariable(session.getTaskID(), "tramite").toString()));
            for (HistoricoTramiteDet detalle : ht.getHistoricoTramiteDetCollection()) {

                CatPredioEdificacion edf = jsonUtil.jsonToObject(detalle.getJson(), CatPredioEdificacion.class);
                edf.setIdDetalleTramite(detalle.getId());
                edificaciones.add(edf);
                if (edf.getId() < 0) {
                    if (edf.getId() > tempID) {
                        tempID = edf.getId();
                    }
                }
                predio = detalle.getPredio();
            }
        } else {
            this.continuar();
        }
    }

    public void formEdificacion(CatPredioEdificacion edificacion) {
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predio.getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (edificacion != null && edificacion.getId() != null) {
            p.add(edificacion.getId().toString());
            params.put("idCatPredioBloq", p);
        }
        Boolean nuevo = edificacion == null;
        p = new ArrayList<>();
        p.add(Boolean.toString((nuevo)));
        params.put("nuevo", p);
        p = new ArrayList<>();
        p.add("false");
        params.put("ver", p);
        p = new ArrayList<>();
        p.add("1");
        params.put("transitorio", p);
        if (!nuevo) {
            p = new ArrayList<>();
            p.add(edificacion.getIdDetalleTramite().toString());
        }
        Utils.openDialog("/resources/dialog/edificacionesPredio", params, "500");
    }

    public void procesarEdificacion(SelectEvent event) {
        CatPredioEdificacion bloque = (CatPredioEdificacion) event.getObject();

        if (bloque != null) {
            if (bloque.getId() == null) {
                System.out.println("Nuevo bloque");
                bloque.setId(++tempID);
                System.out.println("Id temporal: " + bloque.getId());
            }
            BigDecimal areaTotal = BigDecimal.ZERO;
            for (CatEdificacionPisosDet p : bloque.getCatEdificacionPisosDetCollection()) {
                areaTotal = areaTotal.add(p.getArea());
            }
            bloque.setAreaBloque(areaTotal);

            if (!edificaciones.contains(bloque)) {
                System.out.println("No contiene al bloque: " + bloque.getId());

                HistoricoTramiteDet det = new HistoricoTramiteDet();
                det.setTramite(ht);
                det.setPredio(predio);
                det.setJson(jsonUtil.generarJson(bloque));
                det.setFecCre(new Date());
                det.setCartaAdosamiento(Boolean.FALSE);
                det = (HistoricoTramiteDet) super.manager.persist(det);

                bloque.setIdDetalleTramite(det.getId());

                det.setJson(jsonUtil.generarJson(bloque));
                super.manager.update(det);
                edificaciones.add(bloque);
            } else {
                for (int i = 0; i < edificaciones.size(); i++) {
                    if (edificaciones.get(i).getId().equals(bloque.getId())) {
                        edificaciones.remove(i);
                        edificaciones.add(i, bloque);
                        HistoricoTramiteDet dt = super.manager.find(HistoricoTramiteDet.class, bloque.getIdDetalleTramite());
                        dt.setJson(jsonUtil.generarJson(bloque));
                        super.manager.update(dt);
                    }
                }
            }

            Faces.messageInfo(null, "Nota!", "Bloques actualizados satisfactoriamente");

        }
    }

    public void eliminarEdificacion(CatPredioEdificacion edificacion) {
        HistoricoTramiteDet det = null;
        for (HistoricoTramiteDet dt : ht.getHistoricoTramiteDetCollection()) {
            if (dt.getId().equals(edificacion.getIdDetalleTramite())) {
                det = dt;
                break;
            }
        }
        super.manager.delete(det);
        edificaciones.remove(edificacion);
    }

    public void revisionEdificacionAlfanumerica(CatPredioEdificacion edificacion) {
        if (edificacion.getId() != null) {
            ss.instanciarParametros();
            ss.agregarParametro("idEdificacion", edificacion.getId());
            ss.agregarParametro("idPredio", edificacion.getPredio().getId());
            ss.agregarParametro("edit", true);
            ss.agregarParametro("taskId", this.getTaskId());
            ss.agregarParametro("idTramite", this.ht.getIdTramite());
            ss.agregarParametro("formEdicion", this.getVariable(this.getTaskId(), "formEdicion"));
            Faces.redirectFaces("/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml");
        }
    }

    public void completarEdicion(boolean geografica) {

        HashMap<String, Object> paramt = new HashMap<>();
        paramt.put("prioridad", 50);
        this.completeTask(session.getTaskID(), paramt);

        this.continuar();
    }

    public void completarRevision() {

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

    //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

    public HistoricoTramites getHt() {
        return ht;
    }

    public void setHt(HistoricoTramites ht) {
        this.ht = ht;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public Collection<CatPredioEdificacion> getEdificaciones() {
        return edificaciones;
    }

    public void setEdificaciones(List<CatPredioEdificacion> edificaciones) {
        this.edificaciones = edificaciones;
    }

    public JsonUtils getJsonUtil() {
        return jsonUtil;
    }

    public void setJsonUtil(JsonUtils jsonUtil) {
        this.jsonUtil = jsonUtil;
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
