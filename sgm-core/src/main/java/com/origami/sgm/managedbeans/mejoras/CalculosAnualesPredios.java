/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans.mejoras;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatPredioSumasAnualesUbicacion;
import com.origami.sgm.entities.CatUbicacion;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.JsfUti;

/**
 *
 * @author origami
 */
@Named
@ViewScoped
public class CalculosAnualesPredios implements Serializable {

    private static final long serialVersionUID = 1L;
    @javax.inject.Inject
    private Entitymanager manager;
    private Map<String, Object> parametros = new HashMap<>();

    @Inject
    private ServletSession servletSession;

    protected BaseLazyDataModel<CatPredioSumasAnualesUbicacion> calculosAnuales;
    protected List<CatUbicacion> ubicaciones;
    protected List<CatPredioSumasAnualesUbicacion> calculosAnualesIngreso;
    protected Long anioEmision;
    protected Long numPredios;
    protected BigDecimal totalAvaluos;
    protected BigDecimal totalAreas;
    protected BigDecimal totalAvaluosParroquias;
    protected BigDecimal totalAreasParroquias;
    protected Long anioReporte;

    @PostConstruct
    public void initView() {
        try {
            calculosAnuales = new BaseLazyDataModel<CatPredioSumasAnualesUbicacion>(CatPredioSumasAnualesUbicacion.class, "id");
            parametros = new HashMap<>();
            parametros.put("estado", Boolean.TRUE);
            ubicaciones = manager.findObjectByParameterList(CatUbicacion.class, parametros);
            calculosAnualesIngreso = new ArrayList<>();
            Calendar c = Calendar.getInstance();
            anioEmision = new Long(c.get(Calendar.YEAR));
        } catch (Exception e) {
            Logger.getLogger(CalculosAnualesPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void realizarCalculos() {
        try {
            System.out.println("sizze" + ubicaciones.size());
            calculosAnualesIngreso = new ArrayList<>();
            totalAvaluosParroquias = BigDecimal.ZERO;
            totalAreasParroquias = BigDecimal.ZERO;
            numPredios = (Long) manager.find(Querys.numPrediosActivos);
            totalAvaluos = ((BigDecimal) manager.find(Querys.totalAvaluosMunicipal)).setScale(2, RoundingMode.UP);
            totalAreas = (BigDecimal) manager.find(Querys.totalAreaSolar);
            CatPredioSumasAnualesUbicacion valorAnual;
            for (CatUbicacion ubicacion : ubicaciones) {
                valorAnual = new CatPredioSumasAnualesUbicacion();
                valorAnual.setUbicacion(ubicacion);
                valorAnual.setAvaluosTotales((BigDecimal) manager.find(Querys.totalAvaluosMunicipalByParroquia, new String[]{"idUbicacion"}, new Object[]{ubicacion.getId()}));
                valorAnual.setAreaSolarTotal((BigDecimal) manager.find(Querys.totalAreaSolarByParroquia, new String[]{"idUbicacion"}, new Object[]{ubicacion.getId()}));
                if (valorAnual.getAreaSolarTotal() != null && valorAnual.getAreaSolarTotal().compareTo(BigDecimal.ZERO) > 0 && valorAnual.getAvaluosTotales() != null && valorAnual.getAvaluosTotales().compareTo(BigDecimal.ZERO) > 0) {
                    valorAnual.setAvaluosTotales(valorAnual.getAvaluosTotales().setScale(2, RoundingMode.UP));
                    totalAvaluosParroquias = totalAvaluosParroquias.add(valorAnual.getAvaluosTotales());
                    totalAreasParroquias = totalAreasParroquias.add(valorAnual.getAreaSolarTotal());
                    calculosAnualesIngreso.add(valorAnual);
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CalculosAnualesPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public SelectItem[] getListUbicaciones() {
        int cantRegis = ubicaciones.size();
        SelectItem[] options = new SelectItem[cantRegis + 1];
        options[0] = new SelectItem("", "Seleccione");
        for (int i = 0; i < cantRegis; i++) {
            options[i + 1] = new SelectItem(ubicaciones.get(i).getNombre(), ubicaciones.get(i).getNombre());
        }
        return options;
    }

    public void registrarCalculos() {
        try {
            if (this.anioEmision != null) {
                parametros = new HashMap<>();
                parametros.put("anio", anioEmision);
                List<CatPredioSumasAnualesUbicacion> calculosExistentes = manager.findObjectByParameterList(CatPredioSumasAnualesUbicacion.class, parametros);
                if (calculosExistentes == null || calculosExistentes.isEmpty()) {
                    for (CatPredioSumasAnualesUbicacion calculoAnual : calculosAnualesIngreso) {
                        calculoAnual.setAnio(anioEmision);
                        manager.persist(calculoAnual);
                    }
                    JsfUti.messageInfo(null, "Mensaje.", "Guardado Exitoso.");
                } else {
                    JsfUti.messageInfo(null, "Mensaje.", "Ya se realizarón los calculos para el año ingresado.");
                }
            } else {
                JsfUti.messageInfo(null, "Mensaje.", "Debe ingresar el Año al cual se calcularan las emisiones.");
            }
        } catch (Exception e) {
            Logger.getLogger(CalculosAnualesPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void generarReporte() {
        try {
            servletSession.instanciarParametros();
            servletSession.setTieneDatasource(true);
            servletSession.setNombreReporte("calculosAnualesPrediales");
            servletSession.setNombreSubCarpeta("mejoras");
            servletSession.agregarParametro("LOGO", JsfUti.getRealPath(SisVars.sisLogo1));
            servletSession.agregarParametro("ANIO", this.anioReporte);
            JsfUti.redirectNewTab(SisVars.urlServidorPublica + "/Documento");
        } catch (Exception e) {
            Logger.getLogger(CalculosAnualesPredios.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public ServletSession getServletSession() {
        return servletSession;
    }

    public void setServletSession(ServletSession servletSession) {
        this.servletSession = servletSession;
    }

    public BaseLazyDataModel<CatPredioSumasAnualesUbicacion> getCalculosAnuales() {
        return calculosAnuales;
    }

    public void setCalculosAnuales(BaseLazyDataModel<CatPredioSumasAnualesUbicacion> calculosAnuales) {
        this.calculosAnuales = calculosAnuales;
    }

    public List<CatPredioSumasAnualesUbicacion> getCalculosAnualesIngreso() {
        return calculosAnualesIngreso;
    }

    public void setCalculosAnualesIngreso(List<CatPredioSumasAnualesUbicacion> calculosAnualesIngreso) {
        this.calculosAnualesIngreso = calculosAnualesIngreso;
    }

    public Long getAnioEmision() {
        return anioEmision;
    }

    public void setAnioEmision(Long anioEmision) {
        this.anioEmision = anioEmision;
    }

    public Long getNumPredios() {
        return numPredios;
    }

    public void setNumPredios(Long numPredios) {
        this.numPredios = numPredios;
    }

    public BigDecimal getTotalAvaluos() {
        return totalAvaluos;
    }

    public void setTotalAvaluos(BigDecimal totalAvaluos) {
        this.totalAvaluos = totalAvaluos;
    }

    public BigDecimal getTotalAreas() {
        return totalAreas;
    }

    public void setTotalAreas(BigDecimal totalAreas) {
        this.totalAreas = totalAreas;
    }

    public BigDecimal getTotalAvaluosParroquias() {
        return totalAvaluosParroquias;
    }

    public void setTotalAvaluosParroquias(BigDecimal totalAvaluosParroquias) {
        this.totalAvaluosParroquias = totalAvaluosParroquias;
    }

    public BigDecimal getTotalAreasParroquias() {
        return totalAreasParroquias;
    }

    public void setTotalAreasParroquias(BigDecimal totalAreasParroquias) {
        this.totalAreasParroquias = totalAreasParroquias;
    }

}
