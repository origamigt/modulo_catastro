/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.managedbeans.ordenes;

import com.origami.app.AppConfig;
import com.origami.censocat.entity.ordentrabajo.OrdenDet;
import com.origami.censocat.entity.ordentrabajo.OrdenTrabajo;
import com.origami.censocat.querys.Querys;
import com.origami.censocat.restful.EstadoMovil;
import com.origami.censocat.restful.JsonUtils;
import com.origami.censocat.service.ordenes.OrdenService;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.models.CatPredioModel;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioCultivo;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.Faces;
import util.Utils;
import util.managedbeans.OtsUtil;

/**
 * Controlador que permite la seleccion de predios que seran agregados a la
 * orden de trabajo.
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class Ordenes extends OtsUtil implements Serializable {

    private static final long serialVersionUID = 1L;
    protected static final Logger LOG = Logger.getLogger(Ordenes.class.getName());

    protected OrdenTrabajo orden;
    protected OrdenDet det;
    protected Collection<OrdenDet> dets, pendientes;
    protected CatPredioLazy predios;
    protected String zona, manzana, sector;
    @javax.inject.Inject
    protected Entitymanager manager;
    @javax.inject.Inject
    protected OrdenService ots;
    @javax.inject.Inject
    protected CatastroServices catas;
    protected BaseLazyDataModel<OrdenTrabajo> ordenes;
    protected List<CatCiudadela> urbanizaciones;
    protected List<CatParroquia> parroquias;
    protected List<String> ciudadelas;
    protected List<CatPredio> spredios = null;
    protected List<AclUser> usuarios;
    protected JsonUtils json;
    @Inject
    protected UserSession sess;
    @Inject
    protected AppConfig config;

    @PostConstruct
    protected void load() {
        if (sess != null) {
            ordenes = new BaseLazyDataModel<>(OrdenTrabajo.class, "numOrden", "DESC");
            ordenes.setColunmEstado("estadoOt");
            ordenes.setValueEstado(EstadoMovil.INACTIVO);
            ordenes.setEqualsEstado(Boolean.FALSE);
            usuarios = catas.getUser(new GeDepartamento(2L));
//            parroquias = manager.findAll(CatParroquia.class);
            parroquias = catas.getProrroquias(null);
        }
    }

    public void buscar() {
        if (zona != null && !zona.isEmpty() && sector != null && !sector.isEmpty() && manzana != null && !manzana.isEmpty() && orden.getParroquia() != null) {
//            predios = new PrediosLegacyLazy(zona, sector, manzana, String.valueOf(orden.getParroquia().getCodNac()));
            try {
                CatPredioModel model = new CatPredioModel();
                model.setZona(Short.valueOf(zona));
                model.setSector(Short.valueOf(sector));
                model.setMz(Short.valueOf(manzana));
                model.setParroquiaShort(orden.getParroquia().getCodNac());
                predios = new CatPredioLazy(model);
                predios.setSoloActivos(Boolean.TRUE);
                predios.setColunmEstado("estado");
                predios.setValueEstados(Arrays.asList("A"));
            } catch (NumberFormatException numberFormatException) {
                LOG.log(Level.SEVERE, "Buscar Predios", numberFormatException);
            }
        } else {
            Faces.messageError(null, "Error", "Debe seleccionar los datos de ubicacion respectivos");
        }
    }

    public void reasignar() {
        try {
            if (orden.getResponsable() != null) {
                if (manager.persist(orden) != null) {
                    Faces.messageInfo(null, "Nota!", "Orden No. " + orden.getNumOrden() + ", reasignada a " + orden.getResponsable().getUsuario());
                }
            }
        } catch (Exception e) {
            Faces.messageError(null, "Error", "Ha ocurrido un error al reasignar la orden " + e.getMessage());
        }
    }

    public void ver(OrdenTrabajo ot) {
        if (ot != null) {
            orden = ot;
            orden.setFecAct(new Date());
        } else {
            orden = new OrdenTrabajo();
            orden.setSector("0");
            orden.setMz("0");
            orden.setCdla("0");
            orden.setSolar("0");
            orden.setMzdiv("0");
            orden.setEstado(Boolean.TRUE);
            orden.setFecCre(new Date());
            orden.setFecIni(new Date());
            orden.setFecFin(Utils.sumarRestarDiasFecha(new Date(), 5));
            det = new OrdenDet();
            det.setFecCre(new Date());
            det.setEstado(Boolean.TRUE);
            dets = new ArrayList<>();
            if (spredios != null) {
                spredios.clear();
            }
        }
    }

    public void selecPredios() {
        json = new JsonUtils();
        try {
            if (Utils.isEmpty(spredios)) {
                Faces.messageWarning(null, "Advertencia", "Debe seleccionar los predios que seran parte de la Orden");
                return;
            }
            int cont = 0;
            AclUser sup = null;
            if (orden.getFecIni() != null && orden.getFecFin() != null && orden.getResponsable() != null && orden.getParroquia() != null) {
                if (sess.getUserId() != null) {
                    sup = manager.find(AclUser.class, sess.getUserId());
                    if (sup == null) {
                        Faces.messageWarning(null, "Advertencia", "El supervisor de la orden debe estar asociado");
                        return;
                    } else {
                        orden.setSupervisor(sup);
                    }
                    orden.setUsrCre(sess.getName_user());
                    if (org.apache.commons.lang3.math.NumberUtils.isNumber(zona)) {
                        orden.setZona(zona);
                    }
                    if (org.apache.commons.lang3.math.NumberUtils.isNumber(sector)) {
                        orden.setSector(sector);
                    }
                    if (org.apache.commons.lang3.math.NumberUtils.isNumber(manzana)) {
                        orden.setMz(manzana);
                    }
                }
                orden.setEstadoOt(EstadoMovil.PENDIENTE);
                for (CatPredio p : spredios) {
                    if (existe(p)) {
                        cont++;
                        Faces.messageWarning(null, "Advertencia", "El predio " + p.getNumPredio() + ", esta asignado a una orden de trabajo");
                        return;
                    }
                    if (p.getTipoConjunto() != null) {
                        p.getTipoConjunto().setCatCiudadelaCollection(null); // No cargar la collection de Ciudadelas.
                    }
                    if (p.getEscrituraLinderos() != null && p.getEscrituraLinderos().getCanton() != null) {
                        p.getEscrituraLinderos().getCanton().setCatParroquiaCollection(null);// no cargar la collection y parroquias.
                    }
                    if (p.getCatEscrituraCollection() != null) {
                        List<CatEscritura> escTemp = new ArrayList<>();
                        for (CatEscritura ce : p.getCatEscrituraCollection()) {
                            if (ce.getCanton() != null && ce.getCanton().getCatParroquiaCollection() != null) {
                                ce.getCanton().setCatParroquiaCollection(null);
                            }
                            if (ce.getProvincia() != null) {
                                ce.getProvincia().setCatCantonCollection(null);
                            }
                            escTemp.add(ce);
                        }
                        p.setCatEscrituraCollection(escTemp);// no cargar la collection y parroquias.
                    }
                    det = new OrdenDet();
                    det.setEstado(Boolean.TRUE);
                    det.setFecCre(new Date());
                    List<CatPredioPropietario> pps = new LinkedList<>();
                    p.getCatPredioPropietarioCollection().stream().map((pp) -> {
                        if (pp.getEnte() != null) {
                            pp.setEnte(null);
                        }
                        return pp;
                    }).forEachOrdered((pp) -> {
                        pps.add(pp);
                    });
                    p.setCatPredioPropietarioCollection(pps);
                    List<CatPredioEdificacion> edfs = new ArrayList<>();
                    if (Utils.isNotEmpty(p.getCatPredioEdificacionCollection())) {
                        p.getCatPredioEdificacionCollection().stream().map((pp) -> {
                            if (Utils.isNotEmpty(pp.getCatPredioEdificacionPropCollection())) {
                                pp.getCatPredioEdificacionPropCollection().stream().filter((cpEdfp) -> (cpEdfp.getProp().getCategoria() != null)).forEachOrdered((cpEdfp) -> {
                                    cpEdfp.setIdCategoria(cpEdfp.getProp().getCategoria().getId());
                                });
                            }
                            return pp;
                        }).forEachOrdered((pp) -> {
                            edfs.add(pp);
                        });
                    }
                    p.setCatPredioEdificacionCollection(edfs);
                    List<CatPredioCultivo> cults = new ArrayList<>();
                    if (Utils.isNotEmpty(p.getCatPredioCultivoCollection())) {
                        p.getCatPredioCultivoCollection().stream().filter((cc) -> (cc.getTipo() != null && cc.getTipo().getHijo() != null)).map((cc) -> {
                            CtlgItem item = manager.find(CtlgItem.class, cc.getTipo().getHijo().longValue());
                            if (item != null) {
                                cc.getTipo().setPadre(BigInteger.valueOf(item.getId()));
                            }
                            return cc;
                        }).forEachOrdered((cc) -> {
                            cults.add(cc);
                        });
                    }
                    p.setCatPredioCultivoCollection(cults);
                    det.setDatoRef(json.generarJson(p));
                    det.setNumPredio(p.getNumPredio().longValue());
                    det.setPredio(p);
                    det.setEstadoDet(EstadoMovil.PENDIENTE);
                    dets.add(det);
                }
                OrdenTrabajo ot = ots.guardarOrden(orden, (List<OrdenDet>) dets);
                if (ot != null) {
                    Faces.messageInfo(null, "Nota", "Orden # " + ot.getNumOrden() + ", creada satisfactoriamente");
                    predios = null;
                    spredios.clear();
                    Faces.update("frmOrdenes");
                    Faces.executeJS("PF('dlgNuevo').hide()");
                }
            } else {
                Faces.messageWarning(null, "Advertencia", "Los campos marcados con * son obligatorios");
            }
        } catch (Exception e) {
            Logger.getLogger(Ordenes.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void verOrdenesInterviniente() {
        if (orden.getResponsable() != null) {
            pendientes = manager.findAll(Querys.getDetOrdenes("orden.responsable.usuario", ":usuario"), new String[]{"usuario"}, new Object[]{orden.getResponsable().getUsuario()});
        } else {
            pendientes = null;
        }
    }

    public void eliminarOrden(OrdenTrabajo ot) {
        if (ot != null) {
            try {
                for (OrdenDet ordenDet : ot.getOrdenDetCollection()) {
                    ordenDet.setEstado(Boolean.FALSE);
                    ordenDet.setEstadoDet(EstadoMovil.INACTIVO);
                    ordenDet.setObservaciones("Order Eliminada.");
                    this.manager.persist(ordenDet);
                }
                ot.setEstado(Boolean.FALSE);
                ot.setEstadoOt(EstadoMovil.INACTIVO);
                this.manager.persist(ot);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Orden Nula");
        }
    }

    public void rechazarOrden(OrdenTrabajo ot) {
        if (ot != null) {
            try {
                for (OrdenDet ordenDet : ot.getOrdenDetCollection()) {
                    ordenDet.setEstadoDet(EstadoMovil.PENDIENTE);
                    ordenDet.setDatoRef(ordenDet.getDatoAct());
                    this.manager.persist(ordenDet);
                }
                ot.setEstadoOt(EstadoMovil.PENDIENTE);
                ot.setFecCre(new Date());
                this.manager.persist(ot);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Orden Nula");
        }
    }

    public OrdenTrabajo getOrden() {
        return orden;
    }

    public void setOrden(OrdenTrabajo orden) {
        this.orden = orden;
    }

    public OrdenDet getDet() {
        return det;
    }

    public void setDet(OrdenDet det) {
        this.det = det;
    }

    public Collection<OrdenDet> getDets() {
        return dets;
    }

    public void setDets(Collection<OrdenDet> dets) {
        this.dets = dets;
    }

    public BaseLazyDataModel<OrdenTrabajo> getOrdenes() {
        return ordenes;
    }

    public void setOrdenes(BaseLazyDataModel<OrdenTrabajo> ordenes) {
        this.ordenes = ordenes;
    }

    public List<CatCiudadela> getUrbanizaciones() {
        return urbanizaciones;
    }

    public void setUrbanizaciones(List<CatCiudadela> urbanizaciones) {
        this.urbanizaciones = urbanizaciones;
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public List<String> getCiudadelas() {
        return ciudadelas;
    }

    public void setCiudadelas(List<String> ciudadelas) {
        this.ciudadelas = ciudadelas;
    }

    public List<CatPredio> getSpredios() {
        return spredios;
    }

    public void setSpredios(List<CatPredio> spredios) {
        this.spredios = spredios;
    }

    public List<AclUser> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<AclUser> usuarios) {
        this.usuarios = usuarios;
    }

    public Collection<OrdenDet> getPendientes() {
        return pendientes;
    }

    public void setPendientes(Collection<OrdenDet> pendientes) {
        this.pendientes = pendientes;
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public String getManzana() {
        return manzana;
    }

    public void setManzana(String manzana) {
        this.manzana = manzana;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public List<CatParroquia> getParroquias() {
        return parroquias;
    }

    public void setParroquias(List<CatParroquia> parroquias) {
        this.parroquias = parroquias;
    }

}
