/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.events;

/**
 * Evento se ejucuta para la valoracion del predio, exclusivo para ibarra
 *
 * @author Angel Navarro
 */
public class ValorarPredioPost {

    private String claveCat;
    private String predialant;
    private Integer tipoProcedimiento;
    private String tipoPredio;
    private String estadoPredio;

    /**
     *
     * @param claveCat
     * @param predialant
     * @param tipoProcedimiento <ol>
     * <li> Para indentificar si se va a ejecutar el procedimiento de valor
     * terreno.</li>
     * <li> Para indentificar si se va ejecutar el procedimiento de valor de
     * construccion.</li>
     * <li> Para indentificar si se va valorar todo.</li></ol>
     */
    public ValorarPredioPost(String claveCat, String predialant, Integer tipoProcedimiento) {
        this.claveCat = claveCat;
        this.predialant = predialant;
        this.tipoProcedimiento = tipoProcedimiento;
    }

    /**
     *
     * @param claveCat
     * @param predialant
     * @param tipoProcedimiento <ol>
     * <li> Para indentificar si se va a ejecutar el procedimiento de valor
     * terreno.</li>
     * <li> Para indentificar si se va ejecutar el procedimiento de valor de
     * construccion.</li>
     * <li> Para indentificar si se va valorar todo.</li></ol>
     * @param tipoPredio
     */
    public ValorarPredioPost(String claveCat, String predialant, Integer tipoProcedimiento, String tipoPredio) {
        this.claveCat = claveCat;
        this.predialant = predialant;
        this.tipoProcedimiento = tipoProcedimiento;
        this.tipoPredio = tipoPredio;
    }

    /**
     *
     * @param claveCat clave catastral actual de 24 digitos.
     * @param predialant clave catastral anterio de 18 digitos.
     * @param tipoProcedimiento <ol>
     * <li> Para indentificar si se va a ejecutar el procedimiento de valor
     * terreno.</li>
     * <li> Para indentificar si se va ejecutar el procedimiento de valor de
     * construccion.</li></ol>
     * <li> Para indentificar si se va valorar todo.</li></ol>
     * @param tipoPredio tipo de predio 'U' >> urbano, 'R' rural
     * @param estado estado en el que se encuentra el predio.
     */
    public ValorarPredioPost(String claveCat, String predialant, Integer tipoProcedimiento, String tipoPredio, String estadoPredio) {
        this.claveCat = claveCat;
        this.predialant = predialant;
        this.tipoProcedimiento = tipoProcedimiento;
        this.tipoPredio = tipoPredio;
        this.estadoPredio = estadoPredio;
    }

    public String getClaveCat() {
        return claveCat;
    }

    public void setClaveCat(String claveCat) {
        this.claveCat = claveCat;
    }

    public String getPredialant() {
        return predialant;
    }

    public void setPredialant(String predialant) {
        this.predialant = predialant;
    }

    /**
     * 1.- Para indentificar si se va a ejecutar el procedimiento de valor
     * terreno. 2.- Para indentificar si se va ejecutar el procedimiento de
     * valor de construccion.
     *
     * @return
     */
    public Integer getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(Integer tipoProcedimiento) {
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public String getTipoPredio() {
        return tipoPredio;
    }

    public void setTipoPredio(String tipoPredio) {
        this.tipoPredio = tipoPredio;
    }

    public String getEstadoPredio() {
        return estadoPredio;
    }

    public void setEstadoPredio(String estadoPredio) {
        this.estadoPredio = estadoPredio;
    }

}
