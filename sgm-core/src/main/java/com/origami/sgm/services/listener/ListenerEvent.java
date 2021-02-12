/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.listener;

import com.origami.app.AppConfig;
import com.origami.censocat.restful.JsonUtils;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioClasificRural;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.events.EliminacionPredioPost;
import com.origami.sgm.events.GenerarHistoricoPredioPost;
import com.origami.sgm.events.HistoricoPredioPost;
import com.origami.sgm.events.ValorarPredioPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import util.Faces;
import util.HiberUtil;

/**
 * Clase permite escuchar los eventos lanzados en cualquier clase
 *
 * @author Angel Navarro
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class ListenerEvent {

    private static final Logger LOG = Logger.getLogger(ListenerEvent.class.getName());

    @Inject
    protected Entitymanager manager;
    @Inject
    protected CatastroServices catastroServices;
    @Inject
    private AppConfig sSession;
//    @Inject
//    private GeoProcesosService geoService;

    public CatPredio getPredioByCodCat(String clavecat) {
        Map<String, Object> pm = new HashMap<>();
        pm.put("claveCat", clavecat);
        return manager.findObjectByParameter(CatPredio.class, pm);
    }

    public void deshabilitarPredio(@Observes EliminacionPredioPost deshabilitar) {
        if (deshabilitar.getCodPredio() != null) {
            CatPredio p = getPredioByCodCat(deshabilitar.getCodPredio());
            p.setEstado(EstadosPredio.INACTIVO);
            manager.persist(p);
        } else if (deshabilitar.getNumPredio() != null) {
            CatPredio p = catastroServices.getPredioNumPredio(deshabilitar.getNumPredio());
            p.setEstado(EstadosPredio.INACTIVO);
            manager.persist(p);
        } else if (deshabilitar.getPredio() != null) {
            deshabilitar.getPredio().setEstado(EstadosPredio.INACTIVO);
            manager.persist(deshabilitar.getPredio());
        }
        Faces.messageInfo(null, "Eliminación ", " predio " + deshabilitar.getCodPredio() + " eliminado satisfactoriamente.");
    }

    public void HistoricoPredio(@Observes HistoricoPredioPost historico) {
        if (historico.getCodPredio() != null) {
            CatPredio p = getPredioByCodCat(historico.getCodPredio());
            p.setEstado(EstadosPredio.HISTORICO);
            manager.persist(p);
        } else if (historico.getNumPredio() != null) {
            CatPredio p = catastroServices.getPredioNumPredio(historico.getNumPredio());
            p.setEstado(EstadosPredio.HISTORICO);
            manager.persist(p);
        } else if (historico.getPredio() != null) {
            historico.getPredio().setEstado(EstadosPredio.HISTORICO);
            manager.persist(historico.getPredio());
        }
    }

    public void guardarHistoricoPredio(@Observes GenerarHistoricoPredioPost historico) {
        try {
            if (historico.getPredioPost() != null) {
                manager.persist(historico.getPredioPost());
                historico.setPredioPost(this.limpiarCollection(historico.getPredioPost()));
            }
            if (historico.getPredioPost().getTipoConjunto() != null) {
                historico.getPredioPost().getTipoConjunto().setCatCiudadelaCollection(null); // No cargar la collection de Ciudadelas.
            }
            if (historico.getPredioPost().getEscrituraLinderos() != null && historico.getPredioPost().getEscrituraLinderos().getCanton() != null) {
                historico.getPredioPost().getEscrituraLinderos().getCanton().setCatParroquiaCollection(null);// no cargar la collection y parroquias.
            }
            if (historico.getPredio() != null && historico.getPredioPost() != null) {
                JsonUtils js = new JsonUtils();
                historico.setPredio(this.limpiarCollection(historico.getPredio()));
                catastroServices.guardarHistoricoPredio(historico.getPredioPost().getNumPredio().longValue(), js.generarJson(historico.getPredio()), js.generarJson(historico.getPredioPost()),
                        historico.getUser(), historico.getObservacion(), null, null, null, null, null);
            } else {
                catastroServices.guardarHistoricoPredio(historico.getPredioPost().getNumPredio().longValue(), historico.getJsonAnt(), historico.getJsonPost(),
                        historico.getUser(), historico.getObservacion(), null, null, null, null, null);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    /**
     * Permite limpier las listas que no se necesitan cuando se genera el Json
     * del predio
     *
     * @param p Entity CatPredio
     * @return
     */
    public CatPredio limpiarCollection(CatPredio p) {
        if (p != null) {
            if (p.getTipoConjunto() != null) {
                p.getTipoConjunto().setCatCiudadelaCollection(null); // No cargar la collection de Ciudadelas.
            }
            if (p.getEscrituraLinderos() != null && p.getEscrituraLinderos().getCanton() != null) {
                p.getEscrituraLinderos().getCanton().setCatParroquiaCollection(null);// no cargar la collection y parroquias.
            }
            if (p.getCatEscrituraCollection() != null) {
                p.getCatEscrituraCollection().stream().map((catEscritura) -> {
                    if (catEscritura.getCanton() != null) {
                        catEscritura.getCanton().setCatParroquiaCollection(null);
                    }
                    return catEscritura;
                }).filter((catEscritura) -> (catEscritura != null)).filter((catEscritura) -> (catEscritura.getProvincia() != null && catEscritura.getProvincia().getCatCantonCollection() != null)).forEachOrdered((catEscritura) -> {
                    catEscritura.getProvincia().setCatCantonCollection(null);
                });
            }
        }
        return p;
    }

    /**
     * Procesa el evento cuendo este el lanzado desde cuelquier clase, en el
     * modelo de datos ValorarPredioPost debe ingresar la claveCat, o la
     * predialant, tipoProcedimiento, tipoPredio, para poder realiar el
     * proceseso de valoracion:
     * <ul>
     * <li>claveCat: Opcional clave catastral nueva</li>
     * <li>predialant: Obligatoria clave catastral anterior</li>
     * <li>tipoProcedimiento: 1 para terreno y 2 para edificaciones</li>
     * <li>tipoPredio: U para urbano y R para rural</li>
     * </ul>
     *
     * dependiendo del tipo de predio envia a llamar la funcion para valorar el
     * terreno, y de la misma forma dependiendo de tipo de procedimiento envia a
     * llamar la funcion de terreno(1) o de construccion(2)
     *
     * @param post Modelo de datos ValorarPredioPost
     * @return True cuando termina la operacion.
     */
    @Asynchronous
    public Future<Boolean> valorarPredio(@Observes ValorarPredioPost post) {
        String claveCat = null;
        CatPredio predio = null;
        try {
            HiberUtil.flushAndCommit();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        HiberUtil.newTransaction();
        if (post.getClaveCat() != null) {
            claveCat = post.getPredialant();
            if (Objects.nonNull(post.getEstadoPredio())) {
                predio = catastroServices.getPredioByClaveCat(post.getClaveCat(), post.getEstadoPredio());
            } else {
                predio = catastroServices.getPredioByClaveCat(post.getClaveCat());
            }
        } else {
            claveCat = post.getClaveCat();
            if (Objects.nonNull(post.getEstadoPredio())) {
                predio = catastroServices.getPredioByClaveCatAnt(post.getPredialant(), post.getEstadoPredio());
            } else {
                predio = catastroServices.getPredioByClaveCatAnt(post.getPredialant());
            }
        }
        try {
            Boolean fichaVirtual = sSession.getTemp() != null;
            if (predio == null && fichaVirtual) {
                predio = sSession.getTemp();
            }
            List<Object> list = new ArrayList<>();
            list.add("'" + claveCat + "'");
            Object valor = null;
            if (post.getTipoProcedimiento() == 1 || post.getTipoProcedimiento() == 3) {
                try {
                    if (post.getTipoPredio().startsWith("U")) {
                        valor = this.manager.executeFunction("sgm_app.sp_valor_terreno_urbano", list, Boolean.FALSE);
                    } else {
                        valor = this.manager.executeFunction("sgm_app.sp_coef_terreno_rural", list, Boolean.FALSE);
                        BigDecimal aval = BigDecimal.ZERO;
                        for (CatPredioClasificRural cpcr : predio.getCatPredioClasificRuralCollection()) {
                            if (cpcr.getEstado().equalsIgnoreCase("A")) {
                                aval = aval.add(cpcr.getValorTerreno() == null ? BigDecimal.ZERO : cpcr.getValorTerreno());
                            }
                        }
                        if (valor != null) {
                            valor = new BigDecimal(valor.toString()).multiply(aval).setScale(2, RoundingMode.HALF_UP);
//                            predio.setAvaluoConstruccion(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));

                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error al ejecutar terreno con clave " + claveCat, e);
                }
                if (valor != null) {
                    predio.setAvaluoSolar(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));
                }
            } else {
                try {
                    valor = this.manager.executeFunction("sgm_app.sp_valor_edificacion", list, Boolean.FALSE);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error al ejecutar >> sgm_app.sp_valor_edificacion con clave " + claveCat, e);
                }
                if (valor != null) {
                    predio.setAvaluoConstruccion(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));
                }
            }
            if (post.getTipoProcedimiento() == 3) {
                try {
                    valor = this.manager.executeFunction("sgm_app.sp_valor_edificacion", list, Boolean.FALSE);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error al ejecutar >> sgm_app.sp_valor_edificacion con clave " + claveCat, e);
                }
                if (valor != null) {
                    predio.setAvaluoConstruccion(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));
                }
            }
            System.out.println(predio.getTipoPredio() + " Ejecutando Valoracion de "
                    + (post.getTipoProcedimiento() == 3 ? "Todo" : (post.getTipoProcedimiento() == 1 ? "Terreno" : "Edificacion"))
                    + " Clave Catastral: " + claveCat + ", Valor: " + valor + " Aval Solar " + predio.getAvaluoSolar());
            if (valor != null) {
                predio.setAvaluoMunicipal(BigDecimal.ZERO);
                predio.setAvaluoMunicipal(predio.getAvaluoSolar() == null ? BigDecimal.ZERO : predio.getAvaluoSolar()
                        .add(predio.getAvaluoObraComplement() == null ? BigDecimal.ZERO : predio.getAvaluoObraComplement())
                        .add(predio.getAvaluoConstruccion() == null ? BigDecimal.ZERO : predio.getAvaluoConstruccion()));
                predio.setBaseImponible(predio.getAvaluoMunicipal());
                sSession.setTemp(predio);
                if (predio.getId() != null && !fichaVirtual) {
                    manager.persist(predio);
                    HiberUtil.flushAndCommit();
                }

            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al Valoracion clave: " + claveCat, e);
        }
        return null;
    }

    /**
     * LLama el procedimiento donde se calcula el valor por hectarea
     *
     * @param claseRural Entity con los datos a obtener el valor del terreno por
     * hectarea
     * @return Valor del terreno por hectarea
     */
    public BigDecimal valorTerrenoRural(CatPredioClasificRural claseRural) {
        Object valor = null;
        try {
            List<Object> list = new ArrayList<>();
            list.add(claseRural.getSectorHomogeneo().getId());
            list.add(claseRural.getCalidadSuelo().getId());
            list.add(claseRural.getPredio().getAreaSolar());
            valor = this.manager.executeFunction("sgm_app.sp_valor_hec_rural", list, Boolean.FALSE);
            if (valor == null) {
                return BigDecimal.ZERO;
            } else {
                return new BigDecimal(valor.toString()).setScale(4, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al ejecutar >> sgm_app.sp__valor_hec_rural con clave " + claseRural.getPredio().getClaveCat(), e);
            return BigDecimal.ZERO;
        }
    }

}
