/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi;

import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.sgm.lazymodels.BaseLazyDataModelIb;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class Ciudadanos implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(Ciudadanos.class.getName());

    @Inject
    private Entitymanager manager;

    private CiuCiudadano ciudadano;
    private Boolean ver = false;
    private BaseLazyDataModelIb<CiuCiudadano> ciudadanosLazy;

    @PostConstruct
    public void load() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                ciudadanosLazy = new BaseLazyDataModelIb<>(CiuCiudadano.class, "ciuCedRuc");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Init View", e);
        }
    }

    public void nuevo(CiuCiudadano cd) {
        Map<String, List<String>> params = null;
        List<String> p = null;
        if (cd != null) {
            params = new HashMap<>();
            p = new ArrayList<>();
            p.add(cd.getCiuCedRuc());
            params.put("idCiudadano", p);
            if (ver) {
                p = new ArrayList<>();
                p.add(ver.toString());
                params.put("ver", p);
                ver = false;
            }
        } else {

        }
        Utils.openDialog("/dlgs/mantCiudadano", params, "500", "80");
    }

    public void procesarCiudadano(SelectEvent event) {
        try {

            JsfUti.update("frmResponsableDialog");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "procesar ciudadano", e);
        }
    }

    public void onItemSelect(CiuCiudadano ciudadano) {
        RequestContext.getCurrentInstance().closeDialog(ciudadano);
    }

    public void cerrar() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public CiuCiudadano getCiudadano() {
        return ciudadano;
    }

    public void setCiudadano(CiuCiudadano ciudadano) {
        this.ciudadano = ciudadano;
    }

    public BaseLazyDataModelIb<CiuCiudadano> getCiudadanosLazy() {
        return ciudadanosLazy;
    }

    public void setCiudadanosLazy(BaseLazyDataModelIb<CiuCiudadano> ciudadanosLazy) {
        this.ciudadanosLazy = ciudadanosLazy;
    }

    public Boolean getVer() {
        return ver;
    }

    public void setVer(Boolean ver) {
        this.ver = ver;
    }

}
