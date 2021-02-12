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
public enum WhereComparator {

    Igual(1, "Igual", "="),
    Diferente(2, "Diferente", "<>"),
    Mayor(3, "Mayor", ">"),
    Mayor_O_Igual(4, "Mayor o Igual", ">="),
    Menor(5, "Menor", "<"),
    Menor_O_Igual(5, "Menor o Igual", "<="),
    Between(7, "Between", "Between"),
    En(8, "En", "IN", "Ingrese los valores separados por coma ej: 1, 2, 3"),
    ISNULL(9, "Es null", "IS NULL"),
    ISNOTNULL(10, "No es null", "IS NOT NULL"),
    LIKE(11, "LIKE", "LIKE", "Use el comodines %: %ejem%, %ejem, ejem%");

    private final int code;
    private final String descripcion;
    private final String codigo;
    private String title;

    private WhereComparator(int code, String descripcion, String codigo, String title) {
        this.code = code;
        this.descripcion = descripcion;
        this.codigo = codigo;
        this.title = title;
    }

    private WhereComparator(int code, String descripcion, String codigo) {
        this.code = code;
        this.descripcion = descripcion;
        this.codigo = codigo;
    }

    public int getCode() {
        return code;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitle() {
        return title;
    }

    public WhereComparator byCode(int code) {
        for (WhereComparator value : WhereComparator.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
