/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.util;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Modelo de datos para almacenar el tipo de via que tiene un predio.
 *
 * @author Dairon Freddy
 */
public class CatPredioS6Vias implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigInteger s6;

    private BigInteger ctlg;

    public CatPredioS6Vias() {
    }

    public CatPredioS6Vias(BigInteger s6, BigInteger ctlg) {
        this.s6 = s6;
        this.ctlg = ctlg;
    }

    public BigInteger getS6() {
        return s6;
    }

    public void setS6(BigInteger s6) {
        this.s6 = s6;
    }

    public BigInteger getCtlg() {
        return ctlg;
    }

    public void setCtlg(BigInteger ctlg) {
        this.ctlg = ctlg;
    }

}
