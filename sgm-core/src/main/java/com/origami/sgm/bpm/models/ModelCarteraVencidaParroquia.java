/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

/**
 *
 * @author Angel Navarro
 */
public class ModelCarteraVencidaParroquia implements Serializable {

    private static final long serialVersionUID = 1L;
    private String parroquia;
    private Short codParroquia;
    private BigDecimal totalEmitido;
    private BigDecimal totalCobrado;
    private BigDecimal totalCartera;
    private List<ModelCarteraVencida> carteraVencidas = new ArrayList<>();

    // VARIABLES PARA CREAR GRAFICOS
    private PieChartModel pieModelTemp;
    private List<PieChartModel> pieModelList;

    private BigDecimal axisY = BigDecimal.ZERO;
    private BigDecimal axisX = BigDecimal.ZERO;
    private BarChartModel barModelTemp;
    private List<BarChartModel> barModelList;

    public ModelCarteraVencidaParroquia() {
    }

    public String getParroquia() {
        return parroquia;
    }

    public void setParroquia(String parroquia) {
        this.parroquia = parroquia;
    }

    public Short getCodParroquia() {
        return codParroquia;
    }

    public List<ModelCarteraVencida> getCarteraVencidas() {
        return carteraVencidas;
    }

    public void setCarteraVencidas(List<ModelCarteraVencida> carteraVencidas) {
        this.carteraVencidas = carteraVencidas;
    }

    public void setCodParroquia(Short codParroquia) {
        this.codParroquia = codParroquia;
    }

    public BigDecimal getTotalEmitido() {
        return totalEmitido;
    }

    public void setTotalEmitido(BigDecimal totalEmitido) {
        this.totalEmitido = totalEmitido;
    }

    public BigDecimal getTotalCobrado() {
        return totalCobrado;
    }

    public void setTotalCobrado(BigDecimal totalCobrado) {
        this.totalCobrado = totalCobrado;
    }

    public BigDecimal getTotalCartera() {
        return totalCartera;
    }

    public void setTotalCartera(BigDecimal totalCartera) {
        this.totalCartera = totalCartera;
    }

    @Override
    public String toString() {
        return this.parroquia + "[ " + this.codParroquia + "] " + "[ List " + this.carteraVencidas.size() + "] "; //To change body of generated methods, choose Tools | Templates.
    }

    public PieChartModel getPieModelTemp() {
        createPieModel();
        return pieModelTemp;
    }

    public void setPieModelTemp(PieChartModel pieModelTemp) {
        this.pieModelTemp = pieModelTemp;
    }

    public List<PieChartModel> getPieModelList() {
        return pieModelList;
    }

    public void setPieModelList(List<PieChartModel> pieModelList) {
        this.pieModelList = pieModelList;
    }

    public BigDecimal getAxisY() {
        return axisY;
    }

    public void setAxisY(BigDecimal axisY) {
        this.axisY = axisY;
    }

    public BigDecimal getAxisX() {
        return axisX;
    }

    public void setAxisX(BigDecimal axisX) {
        this.axisX = axisX;
    }

    public BarChartModel getBarModelTemp() {
        createBarModel();
        return barModelTemp;
    }

    public void setBarModelTemp(BarChartModel barModelTemp) {
        this.barModelTemp = barModelTemp;
    }

    public List<BarChartModel> getBarModelList() {
        return barModelList;
    }

    public void setBarModelList(List<BarChartModel> barModelList) {
        this.barModelList = barModelList;
    }

    private void createPieModel() {
        pieModelTemp = new PieChartModel();
        pieModelTemp.set("Total Cobrado", totalCobrado);
        pieModelTemp.set("Total Cartera", totalCartera);
        pieModelTemp.setTitle(parroquia);
        pieModelTemp.setLegendPosition("w");
    }

    private BarChartModel initBarModel() {
        BarChartModel model = new BarChartModel();

        if (getTotalEmitido() != null && getTotalEmitido().compareTo(BigDecimal.ZERO) > 0) {
            if (axisY.compareTo(getTotalEmitido()) < 0) {
                axisY = getTotalEmitido();
            }

            // Chart Series cartera vencida
            ChartSeries cart = new ChartSeries();
            cart.setLabel("Total en Cartera " + getParroquia());
            cart.set(getParroquia(), getTotalCartera());
            // Chart Series Valores cobrados
            ChartSeries cobr = new ChartSeries();
            cobr.setLabel("Total Cobrado " + getParroquia());
            cobr.set(getParroquia(), getTotalCobrado());
            model.addSeries(cart);
            model.addSeries(cobr);
        }
        return model;
    }

    private void createBarModel() {
        barModelTemp = initBarModel();

        barModelTemp.setTitle("Cartera Vencida");
        barModelTemp.setLegendPosition("ne");

        Axis xAxis = barModelTemp.getAxis(AxisType.X);
        xAxis.setLabel("Parroquias");

        Axis yAxis = barModelTemp.getAxis(AxisType.Y);
        yAxis.setLabel("Valor en dolares");
        yAxis.setMin(0);
        yAxis.setMax(axisY);
    }
}
