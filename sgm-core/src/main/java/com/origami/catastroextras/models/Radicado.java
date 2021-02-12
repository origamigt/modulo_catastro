/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.models;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Angel Navarro
 */
public class Radicado implements Serializable {

    private String radiNumeText;
    private String radiAsunto;
    private Date radiFechOfic;
    private Date radiFechAsig;

    public String getRadiNumeText() {
        return radiNumeText;
    }

    public void setRadiNumeText(String radiNumeText) {
        this.radiNumeText = radiNumeText;
    }

    public String getRadiAsunto() {
        return radiAsunto;
    }

    public void setRadiAsunto(String radiAsunto) {
        this.radiAsunto = radiAsunto;
    }

    public Date getRadiFechOfic() {
        return radiFechOfic;
    }

    public void setRadiFechOfic(Date radiFechOfic) {
        this.radiFechOfic = radiFechOfic;
    }

    public Date getRadiFechAsig() {
        return radiFechAsig;
    }

    public void setRadiFechAsig(Date radiFechAsig) {
        this.radiFechAsig = radiFechAsig;
    }

    @Override
    public String toString() {
        return "NumTramite " + radiNumeText; //To change body of generated methods, choose Tools | Templates.
    }

}
