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
public enum NivelTree {
    /**
     * Indica el nivel en el arbol
     */
    FIRST(1, "FIRST"),
    SECOND(2, "SECOND"),
    THIRD(3, "THIRD"),
    FOURTH(4, "FOURTH"),
    FIFTH(5, "FIFTH"),
    SIXTH(6, "SIXTH"),
    SEVENTH(7, "SEVENTH"),
    MORE(8, "MORE"),
    NONE(0, "NONE");

    private int code;
    private String description;

    private NivelTree(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NivelTree byCode(int code) {
        for (NivelTree value : NivelTree.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
