/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi.restricciones;

import com.origami.catastroextras.entities.ibarra.Restricciones;
import com.origami.catastroextras.entities.ibarra.RestricionPredio;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigInteger;
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
import org.primefaces.event.SelectEvent;
import util.JsfUti;
import util.Utils;

/**
 * Controlador muestra el listado del catalogo de las restriciones y el listado
 * de restriciones de predios.
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class RestriccionesView implements Serializable {

    private static final Logger LOG = Logger.getLogger(RestriccionesView.class.getName());

    @Inject
    private Entitymanager manager;
    @Inject
    private CatastroServices catastroServices;

    private Boolean ver = false;
    private BaseLazyDataModel<Restricciones> restriccionesLazy;
    private BaseLazyDataModel<RestricionPredio> restricionPredios;

    /**
     * Inicializa los datos de la vista
     */
    @PostConstruct
    public void load() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                restriccionesLazy = new BaseLazyDataModel<>(Restricciones.class, "codigoRestriccion");
                restricionPredios = new BaseLazyDataModel<>(RestricionPredio.class, "numeroTramite");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Init View", e);
        }
    }

    /**
     * habre el dialog framework para la creacion, edicion, o vista del catalogo
     * restriccion
     *
     * @param cd Restricciones
     */
    public void nuevo(Restricciones cd) {
        Map<String, List<String>> params = null;
        List<String> p = null;
        if (cd != null) {
            params = new HashMap<>();
            p = new ArrayList<>();
            p.add(cd.getCodigoRestriccion().toString());
            params.put("idRestriccion", p);
            if (ver) {
                p = new ArrayList<>();
                p.add(ver.toString());
                params.put("ver", p);
                ver = false;
            }
        } else {

        }
        Utils.openDialog("/restricciones/restriccion", params, "350", "55");
    }

    public void eliminar(RestricionPredio cd) {
        try {
            manager.delete(cd);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "procesar ciudadano", e);
        }
    }

    /**
     * habre el dialog framework para la creacion, edicion, o vista de una
     * RestriccionPredio
     *
     * @param cd RestricionPredio
     */
    public void nuevaRestriccion(RestricionPredio cd) {
        Map<String, List<String>> params = null;
        List<String> p = null;
        if (cd != null) {
            params = new HashMap<>();
            p = new ArrayList<>();
            p.add(cd.getId() + "");
            params.put("idRestriccion", p);
            if (ver) {
                p = new ArrayList<>();
                p.add(ver.toString());
                params.put("ver", p);
                ver = false;
            }
        } else {

        }
        Utils.openDialog("/restricciones/restriccionPredio", params, "500", "80");
    }

    /**
     * Metodo que recibe el request cuando se cierra el dialog framework de
     * cualquiera de los dialog de arriba
     *
     * @param event Evento de primefaces
     */
    public void procesarReturnDialog(SelectEvent event) {
        try {

            if (event.getObject() instanceof Restricciones) {
                if (event.getObject() != null) {
                    System.out.println("Procesando >> Restricciones " + ((Restricciones) event.getObject()).getCodigoRestriccion());
                } else {
                    System.out.println("Procesando >> Restricciones ");
                }
            } else {
                if (event.getObject() != null) {
                    System.out.println("Procesando >> RestricionPredio " + ((RestricionPredio) event.getObject()).getId());
                } else {
                    System.out.println("Procesando >> RestricionPredio ");
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "procesar ciudadano", e);
        }
    }

    /**
     * Busca el registro del predio en la base de datos por la clave primaria
     *
     * @param idPredio
     * @return
     */
    public CatPredio getPredio(BigInteger idPredio) {
        if (idPredio != null) {
            return catastroServices.getPredioId(idPredio.longValue());
        }
        return null;
    }

    public Boolean getVer() {
        return ver;
    }

    public void setVer(Boolean ver) {
        this.ver = ver;
    }

    public BaseLazyDataModel<Restricciones> getRestriccionesLazy() {
        return restriccionesLazy;
    }

    public void setRestriccionesLazy(BaseLazyDataModel<Restricciones> restriccionesLazy) {
        this.restriccionesLazy = restriccionesLazy;
    }

    public BaseLazyDataModel<RestricionPredio> getRestricionPredios() {
        return restricionPredios;
    }

    public void setRestricionPredios(BaseLazyDataModel<RestricionPredio> restricionPredios) {
        this.restricionPredios = restricionPredios;
    }

}
