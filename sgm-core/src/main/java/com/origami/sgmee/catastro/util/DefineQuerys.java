/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.util;

import com.origami.sgm.database.SchemasConfig;

/**
 * Queris para las eliminacion de un predio.
 *
 * @author Dairon Freddy
 */
public class DefineQuerys {

    /*cat_predio_s4*/
    public static String deleteAccesibilidad = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_s4_has_accesibilidad WHERE predio_s4 = ?;";

    public static String updateAccesibilidad = "UPDATE " + SchemasConfig.APP1 + ".cat_predio_s4_has_accesibilidad SET predio_s4 = ? WHERE predio_s4 = ?;";

    /*cat_predio_s6*/
    public static String deleteUsos = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_s6_has_usos WHERE predio_s6 = ?;";

    public static String updateUsos = "UPDATE " + SchemasConfig.APP1 + ".cat_predio_s6_has_usos SET predio_s6 = ? WHERE predio_s6 = ?;";

    public static String getUsos = "SELECT predio_s6 as s6, uso as ctlg FROM " + SchemasConfig.APP1 + ".cat_predio_s6_has_usos WHERE predio_s6 = ?;";

    public static String insertUso = "INSERT INTO " + SchemasConfig.APP1 + ".cat_predio_s6_has_usos(predio_s6, uso) VALUES (?, ?);";

    public static String deleteInstalaciones = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_s6_has_instalacion_especial WHERE predio_s6 = ?;";

    public static String updateInstalaciones = "UPDATE " + SchemasConfig.APP1 + ".cat_predio_s6_has_instalacion_especial SET predio_s6 = ? WHERE predio_s6 = ?;";

    public static String deleteVias = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_s6_has_vias WHERE predio_s6 = ?;";

    public static String updateVias = "UPDATE " + SchemasConfig.APP1 + ".cat_predio_s6_has_vias SET predio_s6 = ? WHERE predio_s6 = ?;";

    public static String deletePredio = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio WHERE id = ?;";
    public static String deletePredioProppietarios = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_propietario WHERE predio = ?;";

    /*cat_predio_s4*/
    public static String deleteEscritura = "DELETE FROM " + SchemasConfig.APP1 + ".cat_escritura WHERE predio = ?;";
    public static String deletePredioS4 = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_s4 WHERE predio = ?;";
    public static String deletePredioS6 = "DELETE FROM " + SchemasConfig.APP1 + ".cat_predio_s6 WHERE predio = ?;";

    public static String deleteEscrituraPropietario = "DELETE FROM " + SchemasConfig.APP1 + ".cat_escritura_propietario WHERE escritura = ?;";

    public static String updateEscrituras = "UPDATE " + SchemasConfig.APP1 + ".cat_escritura_propietario SET escritura = ? WHERE escritura = ?;";
}
