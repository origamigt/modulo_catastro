/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi.aniosAnte;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.PredioAnio;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.Utils;

/**
 *
 * @author PC
 */
@Named
@ViewScoped
public class CatastroPorAnio implements Serializable {

    private static final Logger LOG = Logger.getLogger(CatastroPorAnio.class.getName());

    @Inject
    protected ServiceDataBaseIb services;
    protected String anio;
    protected List<String> anios;
    protected String clave;

    private BaseLazyDataModel<PredioAnio> prediosAnios;

    @PostConstruct
    public void initView() {
        try {

            if (anio == null) {
                anio = (Utils.getAnio(new Date()) - 1) + "";
            }
            anios = services.findAniosPredio(clave);
            prediosAnios = new BaseLazyDataModel<>(PredioAnio.class, new String[]{"anio"},
                    new Object[]{anio}, "codCatastralPredio");
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "initView", e);
        }
    }

    private void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public BaseLazyDataModel<PredioAnio> getPrediosAnios() {
        return prediosAnios;
    }

    public void setPrediosAnios(BaseLazyDataModel<PredioAnio> prediosAnios) {
        this.prediosAnios = prediosAnios;
    }

}
