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
public enum JoinType {
    INNER_JOIN(1, "INNER JOIN", "La sentencia INNER JOIN es el sentencia JOIN por defecto, y consiste en combinar cada fila de una tabla con cada fila de la otra tabla, seleccionado aquellas filas que cumplan una determinada condición."),
    LEFT_JOIN(2, "LEFT JOIN", "La sentencia LEFT JOIN combina los valores de la primera tabla con los valores de la segunda tabla. Siempre devolverá las filas de la primera tabla, incluso aunque no cumplan la condición."),
    LEFT_OUTER_JOIN(3, "LEFT OUTER JOIN", "La sentencia LEFT JOIN combina los valores de la primera tabla con los valores de la segunda tabla. Siempre devolverá las filas de la primera tabla, incluso aunque no cumplan la condición."),
    RIGTH_JOIN(4, "RIGHT JOIN", "La sentencia RIGHT JOIN combina los valores de la primera tabla con los valores de la segunda tabla. Siempre devolverá las filas de la segunda tabla, incluso aunque no cumplan la condición."),
    RIGTH_OUTER_JOIN(5, "RIGHT OUTER JOIN", "La sentencia RIGHT JOIN combina los valores de la primera tabla con los valores de la segunda tabla. Siempre devolverá las filas de la segunda tabla, incluso aunque no cumplan la condición."),
    FULL_JOIN(6, "FULL JOIN", "La sentencia FULL JOIN combina los valores de la primera tabla con los valores de la segunda tabla. Siempre devolverá las filas de las dos tablas, aunque no cumplan la condición");

    private final String nameJoin;
    private final int codigo;
    private final String descripcion;

    private JoinType(int codigo, String nameJoin, String descripcion) {
        this.nameJoin = nameJoin;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getNameJoin() {
        return nameJoin;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
