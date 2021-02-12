/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.util;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Modelo da datos para almacenar temporalmente los datos las accesibilidad de
 * un predio.
 *
 * @author Dairon Freddy
 */
public class CatPredioS4Accesibilidad implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigInteger s4;

    private BigInteger ctlg;

    public CatPredioS4Accesibilidad() {
    }

    public CatPredioS4Accesibilidad(BigInteger s4, BigInteger ctlg) {
        this.s4 = s4;
        this.ctlg = ctlg;
    }

    public BigInteger getS4() {
        return s4;
    }

    public void setS4(BigInteger s4) {
        this.s4 = s4;
    }

    public BigInteger getCtlg() {
        return ctlg;
    }

    public void setCtlg(BigInteger ctlg) {
        this.ctlg = ctlg;
    }

}
