/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.model;

import com.origami.report.enums.WhereComparator;
import com.origami.report.enums.WhereType;
import java.io.Serializable;

/**
 *
 * @author ANGEL NAVARRO
 */
public class ModelWhere implements Serializable {

    private WhereType whereType = WhereType.AND;
    private WhereComparator comparator = WhereComparator.Igual;
    private String value;
    private String value1;

    public ModelWhere() {
    }

    public ModelWhere(String value) {
        this.value = value;
    }

    public WhereType getWhereType() {
        return whereType;
    }

    public void setWhereType(WhereType whereType) {
        this.whereType = whereType;
    }

    public WhereComparator getComparator() {
        return comparator;
    }

    public void setComparator(WhereComparator comparator) {
        this.comparator = comparator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    /**
     * Retorn un string con la sintaxis de la condicion
     *
     * @param columna
     * @return column = valor
     */
    public String getCondition(String columna) {
        switch (comparator) {
            case En:
                String[] values = value.split(",");
                String valuesF = "";
                int count = 0;
                for (String v1 : values) {
                    if (count == 0) {
                        valuesF = "'".concat(v1.trim()).concat("'");
                    } else {
                        valuesF = valuesF + ", '".concat(v1.trim()).concat("'");
                    }
                    count++;
                }
                return columna + " " + comparator.getCodigo() + " (" + valuesF + ") ";
            case Between:
                return columna + " " + comparator.getCodigo() + " '" + value + "' AND '" + value1 + "' ";
            case ISNULL:
                return columna + " " + comparator.getCodigo() + " ";
            case ISNOTNULL:
                return columna + " " + comparator.getCodigo() + " ";
            default:
                return columna + " " + comparator.getCodigo() + " '" + value + "' ";
        }
    }

    @Override
    public String toString() {
        return "ModelWhere{" + "whereType=" + whereType + ", comparator=" + comparator + ", value=" + value + ", value1=" + value1 + '}';
    }

    public Boolean renderedValue1() {
        switch (comparator) {
            case ISNULL:
                return false;
            case ISNOTNULL:
                return false;
            default:
                return true;
        }
    }

    public Boolean renderedValue2() {
        switch (comparator) {
            case Between:
                return true;
            default:
                return false;
        }
    }

    public Boolean renderedValue2(String naneClass) {
        switch (comparator) {
            case Between:
                return true;
            default:
                return false;
        }
    }

    public String getDetail() {
        return comparator.getTitle();
    }
}
