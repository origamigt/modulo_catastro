/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi;

import com.origami.session.ServletSession;
import com.origami.sgm.entities.CatCanton;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatProvincia;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.enums.TipoProceso;
import com.origami.sgm.events.GenerarHistoricoPredioPost;
import com.origami.sgm.services.ejb.QuerysDB;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.event.Event;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import util.JsfUti;

/**
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class Escrituras implements Serializable {

    @Inject
    protected CatastroServices services;
    @Inject
    protected Entitymanager manager;
    @Inject
    protected ServletSession ss;

    private static final Logger LOG = Logger.getLogger(Escrituras.class.getName());
    protected CatEscritura escr;
    protected CatProvincia provincia;
    protected CatCanton canton;
    private Boolean coopropietarios;
    ///
    protected String idPredio;
    protected String idEscritura;
    protected String esNuevo;
    protected String editar;
    protected Boolean datosTemp = false;
    private TipoProceso proceso;

    @Inject
    protected Event<GenerarHistoricoPredioPost> generarHistoricoEvent;

    public void initView() {
        try {
            if (idEscritura != null) {
                escr = manager.find(CatEscritura.class, Long.valueOf(idEscritura));
                canton = escr.getCanton();
                provincia = escr.getProvincia();
            } else {
                escr = new CatEscritura();
                escr.setPredio(new CatPredio(Long.valueOf(idPredio)));
            }
            if (ss.getParametros() != null && ss.getParametros().get("DatosTemp") != null) {
                datosTemp = Boolean.valueOf(ss.getParametros().get("DatosTemp").toString());
            }
            if (ss.getParametros() != null && ss.getParametros().get("proceso") != null) {
                proceso = (TipoProceso) ss.getParametros().get("proceso");
            }
            if (datosTemp == true) {
                if (escr.getFechaAutorizacion() == null) {
                    escr.setFechaAutorizacion(new Date());
                }
            }
            if (escr.getTipoProtocolizacion() == null) {
                escr.setTipoProtocolizacion(services.getDefaultItem("predio.tipo_protocolizacion"));
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "initView", e);
        }
    }

    public void saveEscritura() {
        try {
            if (this.canton != null
                    && this.escr.getNotaria() != null) {
//                if (!datosTemp) {
//                    if (this.escr.getNumRepertorio() == null) {
//                        JsfUti.messageInfo(null, "Debe Registrar todos los campos Obligatorios (*)", "");
//                        return;
//                    }
//                }
                if (esProcesoActualizacionAreasLind()) {
//                    if (escr.getAreaSolar() == null) {
//                        JsfUti.messageInfo(null, "Debe Registrar todos los campos Obligatorios (*)", "");
//                        return;
//                    }
//                    if (escr.getAreaSolar().doubleValue() <= 0) {
//                        JsfUti.messageInfo(null, "Debe Registrar todos los campos Obligatorios (*)", "");
//                        return;
//                    }
                }
                escr.setCanton(canton);
                escr.setFecCre(new Date());
                if (escr.getEstado() == null) {
                    escr.setEstado(EstadosPredio.ACTIVO);
                }
                System.out.println("Datos Temp >. " + datosTemp);
                escr.setProvincia(provincia);
                if (escr.getIdEscritura() != null) {
                    manager.persist(escr);
                    RequestContext.getCurrentInstance().closeDialog(escr);
                } else {
                    if (datosTemp) {
                        if (escr.getIdEscritura() == null) {
                            escr.setIdEscritura(0L);
                            BigInteger bigInteger = new BigInteger(manager.getNativeQuery(QuerysDB.getNumeroAutorizacion).toString());
                            escr.setNumeroActualizacion(bigInteger);
                            escr.setNumRepertorio(bigInteger.intValue());
                        }
                        RequestContext.getCurrentInstance().closeDialog(escr);
                    } else {
                        RequestContext.getCurrentInstance().closeDialog(manager.persist(escr));
                    }
                }
            } else {
                JsfUti.messageInfo(null, "Debe Registrar todos los campos Obligatorios (*)", "");
            }
        } catch (Exception e) {
            JsfUti.messageInfo(null, "Error al Guardar", e.getMessage());
            LOG.log(Level.SEVERE, "saveEscritura", e);
        }
    }

    public Boolean esProcesoActualizacionAreasLind() {
        if (proceso == null) {
            return false;
        }

        if (proceso.equals(TipoProceso.ACTIALIZAR_AREAS_LINDEROS)) {
            return true;
        } else {
            return false;
        }
    }

    public CatEscritura getEscr() {
        return escr;
    }

    public void setEscr(CatEscritura escr) {
        this.escr = escr;
    }

    public CatProvincia getProvincia() {
        return provincia;
    }

    public void setProvincia(CatProvincia provincia) {
        this.provincia = provincia;
    }

    public CatCanton getCanton() {
        return canton;
    }

    public void setCanton(CatCanton canton) {
        this.canton = canton;
    }

    public Boolean getCoopropietarios() {
        return coopropietarios;
    }

    public void setCoopropietarios(Boolean coopropietarios) {
        this.coopropietarios = coopropietarios;
    }

    public String getIdPredio() {
        return idPredio;
    }

    public void setIdPredio(String idPredio) {
        this.idPredio = idPredio;
    }

    public String getIdEscritura() {
        return idEscritura;
    }

    public void setIdEscritura(String idEscritura) {
        this.idEscritura = idEscritura;
    }

    public String getEsNuevo() {
        return esNuevo;
    }

    public void setEsNuevo(String esNuevo) {
        this.esNuevo = esNuevo;
    }

    public String getEditar() {
        return editar;
    }

    public void setEditar(String editar) {
        this.editar = editar;
    }

    public Boolean getDatosTemp() {
        return datosTemp;
    }

    public void setDatosTemp(Boolean datosTemp) {
        this.datosTemp = datosTemp;
    }

}
