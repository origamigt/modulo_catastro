/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.models;

import com.origami.sgm.entities.CatPredio;

/**
 *
 * @author origami
 */
public class CatPredioRuralModel {

    private static final Long serialVersionUID = 1L;

    private CatPredio predioRustico;

    public CatPredioRuralModel() {
    }

    public CatPredioRuralModel(CatPredio predioRustico) {
        this.predioRustico = predioRustico;
    }

    public CatPredio getPredioRustico() {
        return predioRustico;
    }

    public void setPredioRustico(CatPredio predioRustico) {
        this.predioRustico = predioRustico;
    }

}
