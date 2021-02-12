/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi.restricciones;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.Restricciones;
import com.origami.catastroextras.entities.ibarra.RestricionPredio;
import com.origami.catastroextras.entities.ibarra.RestricionPredioPK;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import util.JsfUti;

/**
 * Controlador para dar mantenimiento a las restricciones de los predios,
 * permite agregar y eliminar las restriciones.
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class MantRestriccionPredio implements Serializable {

    private static final Logger LOG = Logger.getLogger(MantRestriccionPredio.class.getName());

    @Inject
    private ServiceDataBaseIb service;
    @Inject
    private CatastroServices catastroServices;

    private RestricionPredio restriccion;
    private CatPredio predio;
    private CatPredioLazy predioLazy;
    private String claveCat;
    private Boolean esVer;
    private String ver;
    private String idRestriccion;
    private String noPersist;
    private List<CtlgItem> listexonerado;

    /**
     * Inicializa los datos
     */
//    @PostConstruct
    public void initView() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                if (idRestriccion != null) {
                    restriccion = service.findRestricionPredio(idRestriccion);
                    if (restriccion != null) {
                        if (restriccion.getPredio() != null) {
                            this.predio = catastroServices.getPredioId(restriccion.getPredio().longValue());
                        } else {
                            this.predio = catastroServices.getPredioByClaveCatAnt(restriccion.getRestricionPredioPK().getCodCatastralPredio());
                        }
                        if (predio != null) {
                            this.claveCat = this.predio.getClaveCat();
                        } else {
                            JsfUti.messageInfo(null, "No se encontro predio", "Favor verifique clave");
                        }
                    } else {
                        JsfUti.messageInfo(null, "No se encontro registro", "Favor verifique Registro");
                        restriccion = new RestricionPredio();
                    }
                } else {
                    restriccion = new RestricionPredio();
                }
                listexonerado = catastroServices.getItemsByCatalogo("restriccion.sne.tipo_restricci");
            }
            esVer = Boolean.valueOf(ver);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Init View", e);
        }
    }

    /**
     * Guardado de Restriccion de predio
     */
    public void guardar() {
        if (restriccion == null) {
            JsfUti.messageError(null, "Error", "Hubo un error al procesar el registro.");
            return;
        }
        if (restriccion.getRestricciones() == null) {
            JsfUti.messageError(null, "Error", "Debe seleccionar el tipo de restricción.");
            return;
        }
        if (predio == null) {
            JsfUti.messageError(null, "Error", "Debe ingresar la Clave Catastral del predio o Hacer clic en el boton buscar.");
            return;
        }
        if (predio.getId() == null) {
            JsfUti.messageError(null, "Error", "Debe ingresar la Clave Catastral del predio o Hacer clic en el boton buscar.");
            return;
        }
        try {
            restriccion.setPredio(BigInteger.valueOf(predio.getId()));
            if (restriccion.getRestricciones() != null) {
                restriccion.setRestricionPredioPK(new RestricionPredioPK(predio.getCodigoPredialCompleto(), restriccion.getRestricciones().getCodigoRestriccion()));
            } else {
                restriccion.getRestricionPredioPK().setCodigoRestriccion(restriccion.getRestricciones().getCodigoRestriccion());
            }
            if (service.findRestricciones(restriccion.getRestricionPredioPK()) != null) {
                JsfUti.messageError(null, "Error", "Ya existe restriccion agregada.");
                return;
            }
            System.out.println("Valor exonerado: " + restriccion.getExonerado());
            RequestContext.getCurrentInstance().closeDialog(service.saveUpdateRestriccionPredio(restriccion));
        } catch (Exception e) {
            JsfUti.messageError(null, "Error", e.getMessage());
            LOG.log(Level.SEVERE, "Guarda Restriccion", e);
        }
    }

    /**
     * Cerrar dialog framework
     */
    public void cerrar() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    /**
     * Listado de Restricciones
     *
     * @return Listado Restricciones
     */
    public List<Restricciones> getListRestricciones() {
        return service.listRestricciones();
    }

    /**
     * Busqueda de Clave Catastral
     */
    public void verificarPredio() {
        try {
            CatPredio temp = catastroServices.getPredioByClaveCat(claveCat);
            if (temp != null) {
                this.predio = temp;
                claveCat = this.predio.getClaveCat();
                JsfUti.messageInfo(null, "Clave Encontrada", predio.getClaveCat());
                if (restriccion.getRestricciones() != null) {
                    restriccion.setRestricionPredioPK(new RestricionPredioPK(predio.getPredialant(), restriccion.getRestricciones().getCodigoRestriccion()));
                } else {
                    restriccion.setRestricionPredioPK(new RestricionPredioPK(predio.getPredialant(), 0));
                }
            } else {
                JsfUti.messageError(null, "Error", "No se encontro Predio, Hagale clic al botón buscar para que seleccione el predio.");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar Predio", e);
        }
    }

    /**
     * Busqueda de Clave Catastral
     */
    public void buscarPredio() {
        try {
            predioLazy = new CatPredioLazy("A");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar Predio", e);
        }
    }

    /**
     * Setea el predio que se selecciono en el facelet
     *
     * @param predio Predio seleccionado en el facelet
     */
    public void seleccionarPredio(CatPredio predio) {
        try {
            this.predio = predio;
            claveCat = this.predio.getClaveCat();
            JsfUti.messageInfo(null, "Clave Encontrada", predio.getClaveCat());
            predioLazy = null;
            if (restriccion.getRestricciones() != null) {
                restriccion.setRestricionPredioPK(new RestricionPredioPK(predio.getPredialant(), restriccion.getRestricciones().getCodigoRestriccion()));
            } else {
                restriccion.setRestricionPredioPK(new RestricionPredioPK(predio.getPredialant(), 0));
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar Predio", e);
        }
    }

    //Getter an Setter
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

    public RestricionPredio getRestriccion() {
        return restriccion;
    }

    public void setRestriccion(RestricionPredio restriccion) {
        this.restriccion = restriccion;
    }

    public String getIdRestriccion() {
        return idRestriccion;
    }

    public void setIdRestriccion(String idRestriccion) {
        this.idRestriccion = idRestriccion;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public CatPredioLazy getPredioLazy() {
        return predioLazy;
    }

    public void setPredioLazy(CatPredioLazy predioLazy) {
        this.predioLazy = predioLazy;
    }

    public String getClaveCat() {
        return claveCat;
    }

    public void setClaveCat(String claveCat) {
        this.claveCat = claveCat;
    }

    public Boolean getEsVer() {
        return esVer;
    }

    public void setEsVer(Boolean esVer) {
        this.esVer = esVer;
    }

    public List<CtlgItem> getListexonerado() {
        return listexonerado;
    }

    public void setListexonerado(List<CtlgItem> listexonerado) {
        this.listexonerado = listexonerado;
    }

}
