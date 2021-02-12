/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.sgm.entities.CtlgDescuentoEmision;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.jboss.logging.Logger;

/**
 *
 * @author Mariuly
 */
@Named
@ViewScoped
public class Descuento implements Serializable {

    public static final long serialVersionUID = 1l;

    @javax.inject.Inject
    private Entitymanager manager;
    protected BaseLazyDataModel<CtlgDescuentoEmision> descuentos;
    protected CtlgDescuentoEmision descuento;

    @PostConstruct
    public void init() {
        try {
            descuentos = new BaseLazyDataModel<CtlgDescuentoEmision>(CtlgDescuentoEmision.class, "numMes", "ASC");
        } catch (Exception e) {
            Logger.getLogger(Descuento.class.getName()).log(Logger.Level.FATAL, null, e);
        }
    }

    public void editar(CtlgDescuentoEmision editar) {
        try {
            this.descuento = editar;
        } catch (Exception e) {
        }
    }

    public void nuevo() {
        descuento = new CtlgDescuentoEmision();
    }

    public void guardar() {
        try {
            if (descuento.getId() == null) {
                descuento.setFechaIngreso(new Date());
            } else {
                descuento.setFechaModificacion(new Date());
            }
            this.manager.persist(descuento);
        } catch (Exception e) {
        }

    }

    public void eliminar(CtlgDescuentoEmision delete) {

        try {
            this.manager.delete(delete);
        } catch (Exception e) {
        }
    }

    public BaseLazyDataModel<CtlgDescuentoEmision> getDescuentos() {
        return descuentos;
    }

    public void setDescuentos(BaseLazyDataModel<CtlgDescuentoEmision> descuentos) {
        this.descuentos = descuentos;
    }

    public CtlgDescuentoEmision getDescuento() {
        return descuento;
    }

    public void setDescuento(CtlgDescuentoEmision descuento) {
        this.descuento = descuento;
    }

}
