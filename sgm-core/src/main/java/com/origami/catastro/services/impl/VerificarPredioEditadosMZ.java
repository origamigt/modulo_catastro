/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastro.services.impl;

import com.origami.session.UserSession;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.events.ValorarPredioPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.GeoPredioFacade;
import com.origami.sgmee.catastro.geotx.ModelPrediosTarea;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import util.HiberUtil;
import util.Utils;

/**
 * Verifica Los predios que se editaron en el momento de realizar la tarea
 * grafica, y los envia actualizar.
 *
 * @author Angel Navarro
 */
@Stateless(name = "predioEditadosMz")
@Interceptors(value = {HibernateEjbInterceptor.class})
public class VerificarPredioEditadosMZ {

    @Inject
    private UserSession userSess;

    @Inject
    private GeoPredioFacade geoPredio;

    @Inject
    private CatastroServices services;

    @Inject
    protected Event<ValorarPredioPost> eventValoracion;

    /**
     * Verificamos todos los predios de manzana modificados para actualizar el
     * area del solar
     *
     * @param verificar
     */
    public void verificarMz(@Observes ModelPrediosTarea verificar) {
        if (Utils.isNotEmpty(verificar.getCodigos())) {
            Map<String, String> checkMz = new HashMap<>();
            try {
                for (String codigo : verificar.getCodigos()) {
                    if (checkMz.get(codigo.substring(0, 13)) == null) {
                        checkMz.put(codigo.substring(0, 13), codigo.substring(0, 13));
                        System.out.println("Verificando Manzana >> " + checkMz.get(codigo.substring(0, 13)) + " Tecnico >> " + verificar.getTecnico());
                        if (verificar.getTecnico() == null) {
                            System.out.println("Tecnico es nullo");
                            return;
                        }
                        List<PolygonData> poligonosModif = geoPredio.find_byCodigoModificados("%" + checkMz.get(codigo.substring(0, 13) + "%"),
                                verificar.getTecnico().toLowerCase());
                        if (Utils.isNotEmpty(poligonosModif)) {
                            System.out.println("Predios Modificados >> " + poligonosModif.size());
                            geoPredio.actualizarEstadoPredioTx(poligonosModif);
                            // Recorremos todos los predios encontrados
                            for (PolygonData pd : poligonosModif) {
                                // Verificacmos que el predio no sea el que esta en la tarea
                                if (!pd.getCodigo().equals(codigo)) {
                                    System.out.println("Verificando Clave >> " + pd.getCodigo() + " con area grafica >> " + pd.getArea());
                                    CatPredio pm = services.getPredioByClaveCat(pd.getCodigo());
                                    if (pm != null) {
                                        if (!pm.getEstado().equalsIgnoreCase(EstadosPredio.PENDIENTE)
                                                && !pm.getEstado().equalsIgnoreCase(EstadosPredio.INACTIVO)) {
                                            if (pm.getTipoPredio().equalsIgnoreCase("U")) {
                                                pm.setAreaSolar(pd.getArea());
                                            } else {
                                                pm.setAreaSolar(pd.getArea().divide(BigDecimal.valueOf(10000.00)));
                                            }

                                            CatPredioS4 s4 = pm.getCatPredioS4();
                                            if (s4 != null) {
                                                s4.setAreaGraficaLote(pd.getArea());
                                                services.guardarPredioS4(s4);
                                            }
                                            services.guardarPredio(pm);
                                            HiberUtil.flushAndCommit();
                                            eventValoracion.fire(new ValorarPredioPost(pm.getClaveCat(), pm.getPredialant(), 1, pm.getTipoPredio()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(VerificarPredioEditadosMZ.class.getName()).log(Level.SEVERE,
                        "Ocurrio un error al verificar los predios modificados en la manzana", e);
            }

        }
    }
}
