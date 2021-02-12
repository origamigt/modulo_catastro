/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.ejb;

/**
 * Contiene los querys de consulta a las tablas de ibarra.
 *
 * @author Angel Navarro
 */
public class QuerysDB {

//    public static String getMaxCodigoRestriccion = "SELECT COALESCE(MAX(r.codigoRestriccion), 0) + 1 FROM Restricciones r";
    public static String getMaxCodigoRestriccion = "SELECT MAX(r.codigoRestriccion) FROM Restricciones r";
    public static String getMaxCodigoCiudadano = "SELECT MAX(c.ciuCodigo) FROM CiuCiudadano c";
    public static String getMaxCodigoDireccion = "SELECT MAX(d.diSecuencia) FROM CiuDireccion d";
    public static String getMaxCodigoTelefono = "SELECT MAX(t.teSecuencial) FROM CiuTelefono t";
    public static String getAniosPredio = "SELECT p.anio FROM PredioAnio p WHERE p.codCatastralPredio = :codCatastralPredio";
    public static String getNumeroAutorizacion = "SELECT nextval('sgm_app.numero_autorizacion')";

}
