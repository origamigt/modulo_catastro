/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

/**
 * Modelo de datos para listar las tareas disponibles en el sistema.
 *
 * @author Dairon Freddy
 */
public class TransaccionDataCommand {

    private String transaccionCode;
    private String tipoTransaccion;
    private String toolTip;
    private boolean requierePredio;
    private boolean requiereLista;

    public TransaccionDataCommand() {
    }

    public TransaccionDataCommand(String transaccionCode, String tipoTransaccion, boolean requierePredio, boolean requiereLista) {
        this.transaccionCode = transaccionCode;
        this.tipoTransaccion = tipoTransaccion;
        this.requierePredio = requierePredio;
        this.requiereLista = requiereLista;
    }

    public TransaccionDataCommand(String transaccionCode, String tipoTransaccion, boolean requierePredio, boolean requiereLista, String toolTip) {
        this.transaccionCode = transaccionCode;
        this.tipoTransaccion = tipoTransaccion;
        this.requierePredio = requierePredio;
        this.requiereLista = requiereLista;
        this.toolTip = toolTip;
    }

    public String getTransaccionCode() {
        return transaccionCode;
    }

    public void setTransaccionCode(String transaccionCode) {
        this.transaccionCode = transaccionCode;
    }

    public String getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(String tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    public boolean isRequierePredio() {
        return requierePredio;
    }

    public void setRequierePredio(boolean requierePredio) {
        this.requierePredio = requierePredio;
    }

    public boolean isRequiereLista() {
        return requiereLista;
    }

    public void setRequiereLista(boolean requiereLista) {
        this.requiereLista = requiereLista;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

}
