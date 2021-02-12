/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.events;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Fernando
 */
public class DivisionPrediosPost {

    protected String codPredioDividir;
    private String tipo;
    protected List<String> codPrediosFinales;
    private Map<String, BigInteger> codigos;

    public DivisionPrediosPost() {
    }

    public DivisionPrediosPost(String codPredioDividir) {
        this.codPredioDividir = codPredioDividir;
    }

    public DivisionPrediosPost(String codPredioDividir, List<String> codPrediosFinales) {
        this.codPredioDividir = codPredioDividir;
        this.codPrediosFinales = codPrediosFinales;
    }

    public String getCodPredioDividir() {
        return codPredioDividir;
    }

    public void setCodPredioDividir(String codPredioDividir) {
        this.codPredioDividir = codPredioDividir;
    }

    public List<String> getCodPrediosFinales() {
        return codPrediosFinales;
    }

    public void setCodPrediosFinales(List<String> codPrediosFinales) {
        this.codPrediosFinales = codPrediosFinales;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Map<String, BigInteger> getCodigos() {
        return codigos;
    }

    public void setCodigos(Map<String, BigInteger> codigos) {
        this.codigos = codigos;
    }

}
