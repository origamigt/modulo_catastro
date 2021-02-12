/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans.catastro;

import com.origami.session.UserSession;
import com.origami.sgm.bpm.models.Mensajes;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author Henry Pilco
 */
@Named
@ViewScoped
public class EmisionesPrediales implements Serializable {

    public static final Long serialVersionUID = 1L;

    @javax.inject.Inject
    private Entitymanager manager;
    @Inject
    private UserSession session;
    private Map<String, Object> parametros;
    protected AclUser usuario;
    protected Long anioEmision;
    protected Boolean habilitaProceso = Boolean.FALSE;
    protected Boolean incluirMejoras = Boolean.TRUE;

    protected CatPredioLazy prediosUrbanos = new CatPredioLazy();
    protected CatPredio predio;
    protected CatPredioLazy prediosRurales = new CatPredioLazy();
    protected CatPredio predioRural;

    protected List<Mensajes> mensajes;
    protected Long totalEmisiones;

    @PostConstruct
    public void initView() {
        try {
            if (session != null) {
                parametros = new HashMap<>();
                parametros.put("usuario", session.getName_user());
                usuario = (AclUser) manager.findObjectByParameter(AclUser.class, parametros);
                anioEmision = new Long(Calendar.getInstance().get(Calendar.YEAR));
            }
        } catch (Exception e) {
            Logger.getLogger(EmisionesPrediales.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void inicializarVariablesExoAuto() {
        this.inicializarVariables();
        anioEmision = Utils.getAnio(new Date()).longValue();
    }

    /*
    public void generarExoneraciones(){
        if(this.anioEmision == null){
            JsfUti.messageInfo(null, "Info", "Debe ingresar el año a generar las solicitudes");
            return;
        }
        FnSolicitudExoneracionAutomatica exoAuto;
        List buscarExoneraciones = manager.findAll(QuerysFinanciero.getExoneracionesAutoByAnio, new String[]{"anio"}, new Object[]{this.anioEmision});

        try{
            if(buscarExoneraciones == null || buscarExoneraciones.isEmpty()){
                List exoneracionesDisponibles = manager.findAll(QuerysFinanciero.getExoneracionesDisponibles, new String[]{"anio"}, new Object[]{this.anioEmision});
                for(Object temp : exoneracionesDisponibles){
                    exoAuto = new FnSolicitudExoneracionAutomatica();
                    exoAuto.setFechaIngreso(new Date());
                    exoAuto.setSolicitudExoneracion((FnSolicitudExoneracion)temp);
                    exoAuto.setAnio(this.anioEmision.intValue());
                    exoAuto.setUsuarioIngreso(session.getName_user());
                    exoAuto.setEstado(new FnEstadoExoneracion(2L));
                    manager.persist(exoAuto);
                }
            }else{
                JsfUti.messageInfo(null, "Info", "Ya se generaron las exoneraciones del año ingresado");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
     */
    public void inicializarVariables() {
        habilitaProceso = Boolean.FALSE;
        parametros = new HashMap<>();
        mensajes = new ArrayList<>();
        totalEmisiones = 0L;
    }

    public void generarInformacion(Long l) {
        try {
            habilitaProceso = Boolean.FALSE;
            Long cantidadEmisiones;
            Long cantidadObras;
            Long cantidadCalculosAnuales;
            Long cantidadAvaluosAnuales;
            parametros = new HashMap<>();
            mensajes = new ArrayList<>();
            switch (l.intValue()) {
                case 1://URBANO
                    parametros.put("tipoLiquidacion", 13L);
                    parametros.put("anio", anioEmision);
                    cantidadEmisiones = (Long) manager.findObjectByParameter(Querys.emisionesByTipoAnio, parametros);
                    mensajes.add(new Mensajes("AÑO DE EMISION", cantidadEmisiones == 0 ? "POR GENERAR" : "GENERADO", cantidadEmisiones == 0));
                    mensajes.add(new Mensajes("PROCESO DE MEJORAS", incluirMejoras ? "GENERAR RUBROS DE MEJORAS" : "EMISIONES SIN MEJORAS", Boolean.TRUE));
                    parametros = new HashMap<>();
                    parametros.put("anio", anioEmision);
                    cantidadObras = (Long) manager.findObjectByParameter(Querys.cantidadObrasByAnio, parametros);
                    cantidadCalculosAnuales = (Long) manager.findObjectByParameter(Querys.cantidadSumasAnuales, parametros);
                    //cantidadAvaluosAnuales=(Long) manager.findObjectByParameter(Querys.rangosAnualesUrb, parametros);
                    cantidadAvaluosAnuales = ((BigInteger) manager.getNativeQuery(Querys.rangosAnualesUrb, new Object[]{anioEmision})).longValue();
                    mensajes.add(new Mensajes("RANGO AVALUOS", cantidadAvaluosAnuales > 0 ? "RANGOS DE AVALUOS REGISTRADOS" : "RANGOS DE AVALUOS NO REGISTRADOS", cantidadAvaluosAnuales > 0));
                    if (incluirMejoras) {
                        mensajes.add(new Mensajes("OBRAS", cantidadObras > 0 ? "OBRAS REGISTRADAS" : "NO SE HAN REGISTRADOS OBRAS", cantidadObras > 0));
                        mensajes.add(new Mensajes("CALCULOS ANUALES", cantidadCalculosAnuales > 0 ? "CALCULOS REGISTRADOS" : "NO SE HA REALIZADO EL CALCULO ANUAL", cantidadCalculosAnuales > 0));
                    }
                    habilitaProceso = cantidadEmisiones == 0 && (!incluirMejoras || (cantidadObras > 0 && cantidadCalculosAnuales > 0)) && cantidadAvaluosAnuales > 0;
                    break;
                case 2://RURAL
                    parametros.put("tipoLiquidacion", 7L);
                    parametros.put("anio", anioEmision);
                    cantidadEmisiones = (Long) manager.findObjectByParameter(Querys.emisionesByTipoAnio, parametros);
                    mensajes.add(new Mensajes("AÑO DE EMISION", cantidadEmisiones == 0 ? "POR GENERAR" : "GENERADO", cantidadEmisiones == 0));
                    parametros = new HashMap<>();
                    parametros.put("anio", anioEmision);
                    //cantidadAvaluosAnuales=(Long) manager.findObjectByParameter(Querys.rangosAnualesRur, parametros);
                    cantidadAvaluosAnuales = ((BigInteger) manager.getNativeQuery(Querys.rangosAnualesRur, new Object[]{anioEmision})).longValue();
                    mensajes.add(new Mensajes("RANGO AVALUOS", cantidadAvaluosAnuales > 0 ? "RANGOS DE AVALUOS REGISTRADOS" : "RANGOS DE AVALUOS NO REGISTRADOS", cantidadAvaluosAnuales > 0));
                    habilitaProceso = cantidadEmisiones == 0 && cantidadAvaluosAnuales > 0;
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Logger.getLogger(EmisionesPrediales.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void generarEmisiones(Long l) {
        try {
            generarInformacion(l);
            parametros = new HashMap<>();
            parametros.put("anio", anioEmision);
            switch (l.intValue()) {
                case 1://URBANO
                    if (habilitaProceso) {
                        parametros.put("mejora", incluirMejoras ? 1 : 0);
//                        recaudacion.grabarEmisionGlobal(parametros);
                    }
                    parametros.put("tipoLiquidacion", 13L);
                    break;
                case 2://RURAL
                    if (habilitaProceso) {
//                        recaudacion.grabarEmisionRuralGlobal(parametros);
                    }
                    parametros.put("tipoLiquidacion", 7L);
                    break;
                default:
                    break;
            }
            totalEmisiones = (Long) manager.findObjectByParameter(Querys.emisionesByTipoAnio, parametros);
        } catch (Exception e) {
            JsfUti.messageInfo(null, "Info", "Error al generar Proceso");
            Logger.getLogger(EmisionesPrediales.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void cargarPredio(CatPredio predio) {
        this.predio = predio;
    }

    public UserSession getSession() {
        return session;
    }

    public void setSession(UserSession session) {
        this.session = session;
    }

    public Long getAnioEmision() {
        return anioEmision;
    }

    public void setAnioEmision(Long anioEmision) {
        this.anioEmision = anioEmision;
    }

    public Boolean getHabilitaProceso() {
        return habilitaProceso;
    }

    public void setHabilitaProceso(Boolean habilitaProceso) {
        this.habilitaProceso = habilitaProceso;
    }

    public Boolean getIncluirMejoras() {
        return incluirMejoras;
    }

    public void setIncluirMejoras(Boolean incluirMejoras) {
        this.incluirMejoras = incluirMejoras;
    }

    public CatPredioLazy getPrediosUrbanos() {
        return prediosUrbanos;
    }

    public void setPrediosUrbanos(CatPredioLazy prediosUrbanos) {
        this.prediosUrbanos = prediosUrbanos;
    }

    public CatPredio getPredio() {
        return predio;
    }

    public void setPredio(CatPredio predio) {
        this.predio = predio;
    }

    public CatPredioLazy getPrediosRurales() {
        return prediosRurales;
    }

    public void setPrediosRurales(CatPredioLazy prediosRurales) {
        this.prediosRurales = prediosRurales;
    }

    public CatPredio getPredioRural() {
        return predioRural;
    }

    public void setPredioRural(CatPredio predioRural) {
        this.predioRural = predioRural;
    }

    public List<Mensajes> getMensajes() {
        return mensajes;
    }

    public void setMensajes(List<Mensajes> mensajes) {
        this.mensajes = mensajes;
    }

    public Long getTotalEmisiones() {
        return totalEmisiones;
    }

    public void setTotalEmisiones(Long totalEmisiones) {
        this.totalEmisiones = totalEmisiones;
    }

}
