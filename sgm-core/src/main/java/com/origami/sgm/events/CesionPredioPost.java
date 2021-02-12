/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.events;

import java.math.BigInteger;

/**
 *
 * @author Fernando
 */
public class CesionPredioPost {

    private String codigoPredial;
    private String tipo;
    private BigInteger numPredio;

    public CesionPredioPost() {
    }

    public CesionPredioPost(String codigoPredial) {
        this.codigoPredial = codigoPredial;
    }

    public String getCodigoPredial() {
        return codigoPredial;
    }

    public void setCodigoPredial(String codigoPredial) {
        this.codigoPredial = codigoPredial;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigInteger getNumPredio() {
        return numPredio;
    }

    public void setNumPredio(BigInteger numPredio) {
        this.numPredio = numPredio;
    }

}
