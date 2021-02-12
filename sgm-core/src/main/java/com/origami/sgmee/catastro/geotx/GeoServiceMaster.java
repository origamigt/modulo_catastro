/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.app.cdi.jpa.hibernate.HibernateFactory;
import com.origami.app.cdi.jpa.hibernate.UnitQualifier;
import javax.inject.Inject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HiberUtil;

/**
 * Clase abstracta que contiene las injecciones de los EJB necesarios para
 * realizar las consultas sobre la base grafica.
 *
 * @author Fernando
 */
public abstract class GeoServiceMaster {

    @Inject
    @UnitQualifier("sgm")
    protected HibernateFactory hf;
    @Inject
    protected PredioTableMetadata ptm;
    @Inject
    protected GeoPredioTempFacade tempFac;
    @Inject
    protected GeoPredioFacade predioFac;
    @Inject
    protected GeoBloqueFacade bloqFac;
    @Inject
    protected TempBloqueFacade bloqTmpFac;
    
    protected Boolean activarCalidadSueloRural = true; // FALSE PARA NO VALIDAR LA CALIDAD DE SUELO

    protected Session sess() {
        return HiberUtil.getSession();
    }

    protected Session sessGis() {
        return HiberUtil.getSession();
    }

    protected Transaction requireTx() {
        return HiberUtil.requireTransaction();
    }

    protected Transaction requireTxGis() {
        return HiberUtil.requireTransaction();
    }

    public HibernateFactory getHf() {
        return hf;
    }

    public void setHf(HibernateFactory hf) {
        this.hf = hf;
    }

    public PredioTableMetadata getPtm() {
        return ptm;
    }

    public void setPtm(PredioTableMetadata ptm) {
        this.ptm = ptm;
    }

    public GeoPredioTempFacade getTempFac() {
        return tempFac;
    }

    public void setTempFac(GeoPredioTempFacade tempFac) {
        this.tempFac = tempFac;
    }

    public Boolean getActivarCalidadSueloRural() {
        return activarCalidadSueloRural;
    }

    public void setActivarCalidadSueloRural(Boolean activarCalidadSueloRural) {
        this.activarCalidadSueloRural = activarCalidadSueloRural;
    }

}
