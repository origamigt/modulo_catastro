/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.model;

import com.origami.report.enums.JoinType;
import java.io.Serializable;

/**
 *
 * @author ANGEL NAVARRO
 */
public class ModelJoins implements Serializable {

    private JoinType joinType = JoinType.LEFT_JOIN;
    private String nameColumn;
    private String nameReferencesColumn;
    private Integer nivel;

    public ModelJoins(String nameColumn, String nameReferencesColumn, JoinType joinType) {
        this.nameColumn = nameColumn;
        this.nameReferencesColumn = nameReferencesColumn;
        this.joinType = joinType;
    }

    public ModelJoins(String nameColumn, String nameReferencesColumn) {
        this.nameColumn = nameColumn;
        this.nameReferencesColumn = nameReferencesColumn;
    }

    public ModelJoins(String nameColumn, String nameReferencesColumn, Integer nivel) {
        this.nameColumn = nameColumn;
        this.nameReferencesColumn = nameReferencesColumn;
        this.nivel = nivel;
    }

    public ModelJoins() {

    }

    public ModelJoins(Integer nivel) {
        this.nivel = nivel;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public String getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(String nameColumn) {
        this.nameColumn = nameColumn;
    }

    public String getNameReferencesColumn() {
        return nameReferencesColumn;
    }

    public void setNameReferencesColumn(String nameReferencesColumn) {
        this.nameReferencesColumn = nameReferencesColumn;
    }

    public Boolean isJoin() {
        return this.nameColumn != null && this.nameReferencesColumn != null;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }

    public Integer isJoinT() {
        if (this.nameColumn != null && this.nameReferencesColumn != null) {
            return 0;
        }
        return -1;
    }

    @Override
    public String toString() {
        return "ModelJoins{" + "joinType=" + joinType + ", nameColumn=" + nameColumn + ", nameReferencesColumn=" + nameReferencesColumn + '}';
    }
}
