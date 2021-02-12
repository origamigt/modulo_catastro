/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans.component;

import com.origami.session.UserSession;
import com.origami.sgm.entities.CatPredioClasificRural;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.services.listener.ListenerEvent;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 * Clase encargada de recibir y procesar los datos para el ingreso y la edicion
 * de la clasificacion rural.
 *
 * @author OrigamiSolutions
 */
@Named
@ViewScoped
public class ClasificacionSueloRural implements Serializable {

    private static final Logger LOG = Logger.getLogger(ClasificacionSueloRural.class.getName());

    private Boolean nuevo;
    /**
     * Entity para almacenar y los datos ingresados por el formulario y que
     * despues nos permitira persistirlo en la dase de datos
     */
    private CatPredioClasificRural preClaRu;
    private String idCatClasiSueloRural;
    private String idPredio;
    private String esNuevo;

    @javax.inject.Inject
    private CatastroServices ejb;
    @Inject
    private UserSession us;

    @javax.inject.Inject
    private ListenerEvent listenerEvt;

    /**
     * Metodo encargado de recoger las variables pasadas por el url y realiza la
     * transformacion de tipo de datos para poder realizar la consulta para el
     * caso de la edicion, cuando es nueva simplemente inicializa las variables
     * donde se almacenaran los datos ingresados en el formalario.
     */
    public void initView() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                nuevo = Boolean.valueOf(esNuevo);
                if (nuevo) {
                    if (idPredio == null) {
                        return;
                    }
                    preClaRu = new CatPredioClasificRural();
                    preClaRu.setPredio(ejb.getPredioId(Long.valueOf(idPredio)));
                    preClaRu.setUsuario(us.getName_user());
                    preClaRu.setEstado("A");
                } else {
                    if (idCatClasiSueloRural == null) {
                        return;
                    }
                    if (Long.valueOf(idCatClasiSueloRural) > 0) {
                        preClaRu = ejb.getPredioClasificRuralById(Long.valueOf(idCatClasiSueloRural));
                    } else {
                        preClaRu = (CatPredioClasificRural) Faces.getSessionBean("clasificacionRural");
                        Faces.setSessionBean("clasificacionRural", null);
                        if (preClaRu == null) {
                            return;
                        }
                    }
                }
            }
        } catch (NumberFormatException ne) {
            LOG.log(Level.SEVERE, null, ne);
        }
    }

    /**
     * Envia a llamar la funcion que calcula el valor de la hectarea y envia a
     * realizar a el calculo del valor del terreno
     */
    public void obtenerValorHect() {
        if (preClaRu == null) {
            System.out.println("Entity es nulo >> Clasificacion rural.");
            return;
        }
        if (preClaRu.getSectorHomogeneo() == null) {
            JsfUti.messageError(null, "Error", "Debe seleccionar el sector homogeneo.");
            return;
        }
        if (preClaRu.getCalidadSuelo() == null) {
            JsfUti.messageError(null, "Error", "Debe ingresar la calidad del suelo.");
            return;
        }
        if (preClaRu.getSuperficie() == null) {
            return;
        }

        BigDecimal valor = listenerEvt.valorTerrenoRural(preClaRu);
        if (valor != null || valor.doubleValue() > 0) {
            preClaRu.setValorUnitarioHectareaTerreno(valor);
            preClaRu.setValorTerreno(valor.multiply(preClaRu.getSuperficie()).setScale(4, RoundingMode.HALF_UP));
        }
    }

    /**
     * Registra la clasificacion rural cuando es nueva y cierra el dialogo
     */
    public void agregarClasificSueloRural() {
        try {
            if (preClaRu == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a guardar");
                return;
            }
            if (preClaRu.getSuperficie() == null) {
                JsfUti.messageError(null, "Error", "Ingresar la superficie de la clase del suelo");
                return;
            }
            if (Utils.isNotEmpty(preClaRu.getPredio().getCatPredioClasificRuralCollection())) {
                BigDecimal areatem = BigDecimal.ZERO;
                for (CatPredioClasificRural c : preClaRu.getPredio().getCatPredioClasificRuralCollection()) {
                    areatem = areatem.add(c.getSuperficie());
                }
                if (preClaRu.getPredio().getAreaSolar().compareTo(areatem.add(preClaRu.getSuperficie())) == -1) {
                    Faces.messageInfo(null, "Nota!", "La suma de Areas de las clases de suelo, es mayor al Total del Area del terreno.");
                    return;
                }
            }
            preClaRu.setFecha(new Date());
            RequestContext.getCurrentInstance().closeDialog(ejb.guardarClasificacionSueloRural(preClaRu, us.getName_user()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Asigna el nuevo usuario que realizo la modificacion y asienta laF
     * clasificacion rural.
     */
    public void modificarClasificSueloRural() {
        try {
            if (preClaRu == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a actualizar");
                return;
            }
            preClaRu.setModificado(us.getName_user());
//            BigDecimal valor = listenerEvt.valorTerrenoRural(preClaRu);
//            if (valor != null || valor.doubleValue() > 0) {
//                preClaRu.setValorUnitarioHectareaTerreno(valor);
//                preClaRu.setValorTerreno(valor.multiply(preClaRu.getSuperficie()).setScale(4, RoundingMode.HALF_UP));
//            }
            preClaRu = ejb.guardarClasificacionSueloRural(preClaRu, us.getName_user());
            RequestContext.getCurrentInstance().closeDialog(preClaRu);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Cierra el dialogo y devuelve un null.
     */
    public void cerrar() {
        try {
            RequestContext.getCurrentInstance().closeDialog(null);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cerrar Dialog", e);
        }
    }

    /**
     * Metodos getter and setter que permiter obtener y setter los valores
     * ingresados por el formulario
     *
     * @return
     */
    //<editor-fold defaultstate="collapsed" desc="GETTER AND SETTER">
    /**
     * Permite obtener si es nuevo o no
     *
     * @return true si es nuevo caso contrario false
     */
    public Boolean getNuevo() {
        return nuevo;
    }

    /**
     * Permite asignar el valor a variable
     *
     * @param nuevo true si es nuevo caso constrario false
     */
    public void setNuevo(Boolean nuevo) {
        this.nuevo = nuevo;
    }

    /**
     * Permite obtener los datos de la variable
     *
     * @return la entity donde se guardan los datos
     */
    public CatPredioClasificRural getPreClaRu() {
        return preClaRu;
    }

    /**
     * Permite asignar nuevo valor a la entity donde se almacenan los datos.
     *
     * @param preClaRu nueva valor a asignar
     */
    public void setPreClaRu(CatPredioClasificRural preClaRu) {
        this.preClaRu = preClaRu;
    }

    /**
     * Permite obtener el valor que se encuentra el variable que se recibio por
     * url
     *
     * @return Valor del id de clasificacion rural si no es nuevo caso contrario
     * devuelve nulo
     */
    public String getIdCatClasiSueloRural() {
        return idCatClasiSueloRural;
    }

    /**
     * Permite asignar nuevo valor del id de la tabla CatPredioClasificRural
     *
     * @param idCatClasiSueloRural valor a asignar
     */
    public void setIdCatClasiSueloRural(String idCatClasiSueloRural) {
        this.idCatClasiSueloRural = idCatClasiSueloRural;
    }

    /**
     * Permite obtener el id del predio que se recicbio por URL
     *
     * @return devuelve null si no se recibio este parametro.
     */
    public String getIdPredio() {
        return idPredio;
    }

    /**
     * Permite asignar
     *
     * @param idPredio
     */
    public void setIdPredio(String idPredio) {
        this.idPredio = idPredio;
    }

    /**
     * Obtiene el valor de esta variable que se recibe por URL.
     *
     * @return nulo si no se recibe este valor.
     */
    public String getEsNuevo() {
        return esNuevo;
    }

    /**
     *
     * @param esNuevo
     */
    public void setEsNuevo(String esNuevo) {
        this.esNuevo = esNuevo;
    }

//</editor-fold>
}
