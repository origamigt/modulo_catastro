/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.enums;

/**
 *
 * @author ANGEL NAVARRO
 */
public enum OrderType {

    /**
     * Ordenamiento ascendente
     */
    ASC(1, "ASC", "ORDENA DE MENOR A MAYOR"),
    /**
     * Ordenamiento descendente
     */
    DESC(2, "DESC", "ORDENA DE MAYOR A MENOR");

    /**
     * Codigo deidentificacion de la propiedad
     */
    private final int code;
    /**
     * Valor de la propiedad
     */
    private final String value;
    /**
     * Descripcion de la propiedad
     */
    private final String descripcion;

    private OrderType(int code, String value, String descripcion) {
        this.code = code;
        this.value = value;
        this.descripcion = descripcion;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
