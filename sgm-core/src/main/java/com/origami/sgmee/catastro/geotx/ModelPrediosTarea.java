/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import java.io.Serializable;
import java.util.List;

/**
 * Modelo de datos que permite almacenar temporalmente los poligono que ha
 * modificado el tecnico que esta realizando la tarea.
 *
 * @author Angel Navarro
 */
public class ModelPrediosTarea implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<String> codigos;
    private String tecnico;

    public List<String> getCodigos() {
        return codigos;
    }

    public void setCodigos(List<String> codigos) {
        this.codigos = codigos;
    }

    public String getTecnico() {
        return tecnico;
    }

    public void setTecnico(String tecnico) {
        this.tecnico = tecnico;
    }

}
