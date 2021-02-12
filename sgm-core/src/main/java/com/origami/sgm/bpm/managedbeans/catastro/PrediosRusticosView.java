/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.lazymodels.CatEnteLazy;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import util.JsfUti;

/**
 *
 * @author Joao Sanga
 */
@Named
@ViewScoped
public class PrediosRusticosView implements Serializable {

    private static final long serialVersionUID = 1L;

    @javax.inject.Inject
    private Entitymanager services;

    private CatPredioLazy predios;
    private List<CatCiudadela> ciudadelas;
    private CatPredio predioR;
    private CatEnteLazy enteListLazy;
    private CatEnte ente;

    @PostConstruct
    public void initView() {
        predios = new CatPredioLazy();
        ciudadelas = services.findAllOrdered(CatCiudadela.class, new String[]{"nombre"}, new Boolean[]{true});
        enteListLazy = new CatEnteLazy();
    }

    public void enteSeleccionado(CatEnte ente) {
        this.ente = ente;
        this.predioR.setPropietario(ente);
        if (ente.getEsPersona()) {
            JsfUti.messageInfo(null, "Info", "El ente propietario es: " + ente.getNombres() + " " + ente.getApellidos());
        } else {
            JsfUti.messageInfo(null, "Info", "El ente propietario es: " + ente.getRazonSocial());
        }
    }

    public void actualizarPredioR() {
        try {
            services.update(this.predioR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public List<CatCiudadela> getCiudadelas() {
        return ciudadelas;
    }

    public void setCiudadelas(List<CatCiudadela> ciudadelas) {
        this.ciudadelas = ciudadelas;
    }

    public CatPredio getPredioR() {
        return predioR;
    }

    public void setPredioR(CatPredio predioR) {
        this.predioR = predioR;
    }

    public CatEnteLazy getEnteListLazy() {
        return enteListLazy;
    }

    public void setEnteListLazy(CatEnteLazy enteListLazy) {
        this.enteListLazy = enteListLazy;
    }

    public CatEnte getEnte() {
        return ente;
    }

    public void setEnte(CatEnte ente) {
        this.ente = ente;
    }

}
