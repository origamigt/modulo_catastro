/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.database;

/**
 * Permite especificar cual es el esquema de las diferentes tablas definidas en
 * el sistema
 *
 * @author Fernando
 */
public abstract class SchemasConfig {

    public static final DatabaseEngine DB_ENGINE = DatabaseEngine.POSTGRESQL;
    public static final String LONG_TEXT_TYPE = "text";

    public static final String PREFIJO = "catastro";

    public static final String APP1 = PREFIJO + "";
    public static final String FINANCIERO = APP1; //PREFIJO + "financiero";
    public static final String FLOW = APP1; //PREFIJO + "flow";
    public static final String SECUENCIAS = APP1; //PREFIJO + "secuencias";
    public static final String BITACORA = APP1; //PREFIJO + "bitacora";
    public static final String BANCOS = APP1; //PREFIJO + "bancos";
    public static final String HISTORICO = APP1; //PREFIJO + "historico";
    public static final String LEGACY = APP1; //"legacy";
    public static final String MEJORAS = APP1; //PREFIJO + "mejoras";
    public static final String TEMPORAL = APP1; //PREFIJO + "temporal";
    public static final String AGENDA = APP1; //PREFIJO + "agenda";
    public static final String APPLICATION = APP1; //PREFIJO + "application";

    public static final String APPUNISEQ_DB = "app_uni_seq";
    public static final String APPUNISEQ_ORM = "app_uni_seq";

    public static final String FILTER_ESTADO = "'TRUE'";

    public static final Boolean SEARCH_ESTADO = Boolean.TRUE;
}
