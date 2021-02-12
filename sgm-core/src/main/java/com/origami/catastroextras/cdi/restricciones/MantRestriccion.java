/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi.restricciones;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.Restricciones;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import util.JsfUti;

/**
 * Controlar para el dialogo de edicion y agregacion de restricciones.
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class MantRestriccion implements Serializable {

    private static final Logger LOG = Logger.getLogger(MantRestriccion.class.getName());

    @Inject
    private ServiceDataBaseIb service;

    private Restricciones restriccion;
    private Boolean esVer;
    private String ver;
    private String idRestriccion;
    private String noPersist;

//    @PostConstruct
    public void initView() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                if (idRestriccion != null) {
                    restriccion = service.findRestricciones(idRestriccion);
                } else {
                    restriccion = new Restricciones();
                }
            }
            esVer = Boolean.valueOf(ver);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Init View", e);
        }
    }

    public void guardar() {
        if (restriccion == null) {
            JsfUti.messageError(null, "Error", "Hubo un error al procesar el registro.");
            return;
        }
        if (restriccion.getDescripcionRestriccion() == null) {
            JsfUti.messageError(null, "Error", "Debe ingresar la descripción.");
            return;
        }
        try {
            RequestContext.getCurrentInstance().closeDialog(service.saveUpdateRestriccion(restriccion));
        } catch (Exception e) {
            JsfUti.messageError(null, "Error", "Ocurrio un error en al intentar Guardar.");
            LOG.log(Level.SEVERE, "Guarda Restriccion", e);
        }
    }

    public void cerrar() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getNoPersist() {
        return noPersist;
    }

    public void setNoPersist(String noPersist) {
        this.noPersist = noPersist;
    }

    public Restricciones getRestriccion() {
        return restriccion;
    }

    public void setRestriccion(Restricciones restriccion) {
        this.restriccion = restriccion;
    }

    public String getIdRestriccion() {
        return idRestriccion;
    }

    public void setIdRestriccion(String idRestriccion) {
        this.idRestriccion = idRestriccion;
    }

    public Boolean getEsVer() {
        return esVer;
    }

    public void setEsVer(Boolean esVer) {
        this.esVer = esVer;
    }

}
