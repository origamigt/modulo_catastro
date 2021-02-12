/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

/**
 * Excepcion que se lanza cuando hay algun error en cualquier tarea.
 *
 * @author Fernando
 */
public class GeoProcesosException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
//	private static final long serialVersionUID = 1L;
    public GeoProcesosException(String message) {
        super(message);
    }

}
