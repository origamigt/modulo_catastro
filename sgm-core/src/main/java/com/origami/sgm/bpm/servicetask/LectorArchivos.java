/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.servicetask;

/**
 * Interfaz con método para leer archivos al momento de adjuntar archivos a una
 * tarea
 *
 * @author Max
 */
public interface LectorArchivos {

    byte[] leerArchivo(String ruta) throws Exception;
}
