/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi.aniosAnte;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.PredioAnio;
import com.origami.session.ServletSession;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import util.JsfUti;
import util.Utils;

/**
 * Contiene la información de los años anteriores (Opcion no esta terminada
 * aun).
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class AniosAnterioresView implements Serializable {

    @Inject
    protected ServiceDataBaseIb services;
    @Inject
    protected Entitymanager manager;
    @Inject
    protected ServletSession ss;

    protected PredioAnio predioAnio;
    protected String clave;
    protected String anio;
    protected List<String> anios;

    private static final Logger LOG = Logger.getLogger(AniosAnterioresView.class.getName());

    public void initView() {
        try {
            if (clave != null) {
                if (anio == null) {
                    anio = (Utils.getAnio(new Date()) - 1) + "";
                }
                predioAnio = services.findPredioAnio(clave, anio);
                anios = services.findAniosPredio(clave);
            } else {
                this.close();
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "initView", e);
        }
    }

    public void cargarDatosAnio() {
        if (anio != null && clave != null) {
            predioAnio = services.findPredioAnio(clave, anio);
        }
    }

    public void calcular() {
        JsfUti.messageInfo(null, "Info.", "calculando...");
    }

    public void close() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public PredioAnio getPredioAnio() {
        return predioAnio;
    }

    public void setPredioAnio(PredioAnio predioAnio) {
        this.predioAnio = predioAnio;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getAnio() {
        return anio;
    }

    public void setAnio(String anio) {
        this.anio = anio;
    }

    public List<String> getAnios() {
        return anios;
    }

    public void setAnios(List<String> anios) {
        this.anios = anios;
    }

}
