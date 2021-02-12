/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans;

import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import util.JsfUti;

/**
 *
 * @author Anyelo
 */
@Named
@ViewScoped
public class TramitesIngresados implements Serializable {

    public static final Long serialVersionUID = 1L;

//    private HistoricoTramitesLazy tramitesLazy;
    private BaseLazyDataModel<HistoricoTramites> tramitesLazy;
    private HistoricoTramites ht;

    public void doPreRenderView() {
        if (!JsfUti.isAjaxRequest()) {
            iniView();
        }
    }

    protected void iniView() {
        tramitesLazy = new BaseLazyDataModel<HistoricoTramites>(HistoricoTramites.class, "id", "DESC");
    }

    public void visualizarDatos(HistoricoTramites h) {
        ht = h;
        JsfUti.update("formTramite");
        JsfUti.executeJS("PF('datosTramite').show();");
    }

    public BaseLazyDataModel<HistoricoTramites> getTramitesLazy() {
        return tramitesLazy;
    }

    public void setTramitesLazy(BaseLazyDataModel<HistoricoTramites> tramitesLazy) {
        this.tramitesLazy = tramitesLazy;
    }

    public HistoricoTramites getHt() {
        return ht;
    }

    public void setHt(HistoricoTramites ht) {
        this.ht = ht;
    }

}
