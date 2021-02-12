/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.procesos.actualizarConstruccion;

import com.origami.censocat.restful.JsonUtils;
import com.origami.session.ServletSession;
import com.origami.sgm.bpm.managedbeans.BpmManageBeanBaseRoot;
import com.origami.sgm.entities.CatEdificacionPisosDet;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
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
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import util.Faces;
import util.Utils;

/**
 *
 * @author dfcalderio
 */
@Named(value = "procesarActualizarConstruccion")
@ViewScoped
public class ProcesarTramite extends BpmManageBeanBaseRoot implements Serializable {

    private static final Logger LOG = Logger.getLogger(ProcesarTramite.class.getName());

    @Inject
    private ServletSession ss;

    @EJB
    private Entitymanager manager;

    protected HistoricoTramites ht;

    private CatPredio predio;
    private List<CatPredioEdificacion> edificaciones;

    private JsonUtils jsonUtil;

    private Long tempID;

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

}
