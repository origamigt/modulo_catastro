/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastro.services.impl;

import com.origami.session.UserSession;
import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatEdfProp;
import com.origami.sgm.entities.CatEdificacionPisosDet;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatEscrituraPropietario;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioAlicuotaComponente;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioEdificacionProp;
import com.origami.sgm.entities.CatPredioFusionDivision;
import com.origami.sgm.entities.CatPredioLinderos;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatPredioS6;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.Observaciones;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.events.FusionPrediosPost;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.ejbs.censocat.UploadFotoBean;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.geotx.ActualizacionAreasLinderos;
import com.origami.sgmee.catastro.geotx.GeoProcesosException;
import com.origami.sgmee.catastro.geotx.GeoProcesosService;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import com.origami.sgmee.catastro.services.ProcesoServices;
import com.origami.sgmee.catastro.util.CatPredioS6Uso;
import com.origami.sgmee.catastro.util.DefineQuerys;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import util.HiberUtil;
import util.Utils;

/**
 * Implementa los metodos de la interfaz {@link ProcesoServices} que provee los
 * metodos para las tareas e integracion con la base grafica.
 *
 * @author dfcalderio
 */
@Stateless(name = "ProcesoEJB")
@Interceptors(value = {HibernateEjbInterceptor.class})
public class ProcesoEJB implements ProcesoServices {

    @Inject
    private Entitymanager manager;

    @Inject
    private CatastroServices catas;
    @Inject
    private UploadFotoBean fotoBean;

    @Inject
    protected Event<FusionPrediosPost> event;

    @Inject
    private UserSession user;
    @Inject
    private ActualizacionAreasLinderos linderos;
    @Inject
    private GeoProcesosService procesosService;
    private static final Logger LOG = Logger.getLogger(ProcesoEJB.class.getName());

    @Override
    public CatPredio registrarPredio(CatPredio px) {
        CatPredio pred = null;
        try {
            if (px != null) {
                try {
                    px.setClaveCat(claveCatastral(px));
                    px.setPredialant(claveCatastralAnt(px));
                } catch (Exception e) {
                }
                pred = (CatPredio) manager.persist(px);
                if (px.getCatPredioS6() != null) {
                    px.getCatPredioS6().setPredio(pred);
                    px.getCatPredioS6().setId(null);
                    px.setCatPredioS6((CatPredioS6) manager.persist(px.getCatPredioS6()));
                }

                if (px.getCatPredioS4() != null) {
                    px.getCatPredioS4().setPredio(pred);
                    px.getCatPredioS4().setId(null);
                    px.setCatPredioS4((CatPredioS4) manager.persist(px.getCatPredioS4()));
                }
                if (px.getCatPredioPropietarioCollection() != null) {
                    List<CatPredioPropietario> lp = new ArrayList<>();
                    for (CatPredioPropietario ccp : px.getCatPredioPropietarioCollection()) {
                        ccp.setPredio(pred);
                        ccp.setId(null);
                        lp.add((CatPredioPropietario) manager.persist(ccp));
                    }
                    px.setCatPredioPropietarioCollection(lp);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "registrarPredio", e);
        }
        return pred;
    }

    @Override
    public CatPredioEdificacion registrarEdificacion(CatPredioEdificacion edificacion) {
        CatPredioEdificacion edf = null;
        try {
            if (edificacion != null) {

                edf = (CatPredioEdificacion) manager.persist(edificacion);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "registrarPredio", e);
        }

        return edf;
    }

    @Override
    public CatPredioEdificacion clonarEdificacion(CatPredioEdificacion edificacion, CatPredio predio, Short nroEdificacion, boolean save) {
        CatPredioEdificacion clon = new CatPredioEdificacion();
        BeanUtils.copyProperties(edificacion, clon);
        clon.setId(null);
        clon.setPredio(predio);
        clon.setNoEdificacion(nroEdificacion);
        if (save) {
            clon = registrarEdificacion(clon);
        }
        return clon;
    }

    @Override
    public CatPredioEdificacionProp registrarEdificacionProp(CatPredioEdificacionProp detalle) {
        CatPredioEdificacionProp det = null;
        try {
            if (detalle != null) {

                det = (CatPredioEdificacionProp) manager.persist(detalle);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "registrarPredio", e);
        }
        return det;
    }

    @Override
    public CatPredioEdificacionProp clonarEdificacionProp(CatPredioEdificacionProp detalle, CatPredioEdificacion edificacion, CatEdfProp prop, boolean save) {
        CatPredioEdificacionProp clon = new CatPredioEdificacionProp();
        BeanUtils.copyProperties(detalle, clon);
        clon.setId(null);
        clon.setEdificacion(edificacion);
        clon.setProp(prop);
        //prop.getCatPredioEdificacionPropCollection().add(clon);

        if (save) {
            clon = registrarEdificacionProp(clon);
        }
        return clon;
    }

    @Override
    public CatPredioS4 registrarPredioS4(CatPredioS4 predioS4) {
        CatPredioS4 s4 = null;
        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            s4 = (CatPredioS4) sess.merge(predioS4); // RETORNA EL OBJETO PERSISTIDO
//            sess.flush();
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
        }

        return s4;
    }

    @Override
    public CatPredioS4 clonarPredioS4(CatPredioS4 predioS4, CatPredio predio, boolean save, Long id, String... ignore) {
        CatPredioS4 clon = new CatPredioS4();

        BeanUtils.copyProperties(predioS4, clon, ignore);
        clon.setId(id);
        clon.setPredio(predio);
        predio.setCatPredioS4(clon);
        if (save) {
            clon = registrarPredioS4(clon);
        }

        return clon;
    }

    @Override
    public CatPredioS4 clonarPredioS4(CatPredioS4 predioS4, CatPredio predio, Collection<CtlgItem> accesibilidad, boolean save, Long id, String... ignore) {
        CatPredioS4 clon = new CatPredioS4();
        System.out.println("Predio " + predio);
        BeanUtils.copyProperties(predioS4, clon, ignore);
        clon.setId(id);
        clon.setPredio(predio);
        predio.setCatPredioS4(clon);
        if (accesibilidad != null) {
            for (CtlgItem item : accesibilidad) {
                item.getCatPredioS4Collection().add(clon);
            }
            clon.setAccesibilidadList(accesibilidad);
        }
        if (save) {
            clon = registrarPredioS4(clon);
        }

        return clon;
    }

    @Override
    public CatPredioS6 registrarPredioS6(CatPredioS6 predioS6) {
        CatPredioS6 s6 = null;

        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            s6 = (CatPredioS6) sess.merge(predioS6); // RETORNA EL OBJETO PERSISTIDO
            sess.flush();
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
        }

        return s6;
    }

    @Override
    public CatPredioS6 registrarPredioS6(CatPredioS6 predioS6, Collection<CtlgItem> vias, Collection<CtlgItem> instalaciones) {
        CatPredioS6 s6 = null;
        try {
            if (predioS6 != null) {

                for (CtlgItem item : vias) {
                    item.getCatPredioS6Collection().add(s6);
                }
                s6 = (CatPredioS6) manager.persist(predioS6);

            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "registrarPredio", e);
        }

        return s6;
    }

    @Override
    public CatPredioS6 clonarPredioS6(CatPredioS6 predioS6, CatPredio predio, Collection<CtlgItem> vias, Collection<CtlgItem> instalaciones, boolean save, Long id, String... ignore) {

        CatPredioS6 clon = new CatPredioS6();
        BeanUtils.copyProperties(predioS6, clon, ignore);
        clon.setId(id);

        clon.setPredio(predio);
        predio.setCatPredioS6(predioS6);
        for (CtlgItem item : vias) {
            item.getCatPredioS6Collection().add(clon);
        }
//        for (CtlgItem item : instalaciones) {
//            item.getCatPredioS6CollectionInstalaciones().add(clon);
//        }
        clon.setCtlgItemCollection(vias);
        clon.setCtlgItemCollectionInstalacionEspecial(instalaciones);
        if (save) {
            clon = registrarPredioS6(clon);
        }

        return clon;
    }

    @Override
    public CatPredioS6 clonarPredioS6(CatPredioS6 predioS6, CatPredio predio, Collection<CtlgItem> vias, Collection<CtlgItem> instalaciones, List<CatPredioS6Uso> usos, boolean save, Long id, String... ignore) {
        CatPredioS6 clon = new CatPredioS6();
        BeanUtils.copyProperties(predioS6, clon, ignore);
        clon.setId(id);

        clon.setPredio(predio);
        predio.setCatPredioS6(predioS6);
        for (CtlgItem item : vias) {
            item.getCatPredioS6Collection().add(clon);
        }
//        for (CtlgItem item : instalaciones) {
//            item.getCatPredioS6CollectionInstalaciones().add(clon);
//        }

        clon.setCtlgItemCollection(vias);
        clon.setCtlgItemCollectionInstalacionEspecial(instalaciones);
        if (save) {
            clon.setUsosList(new ArrayList<>());
            if (usos != null) {
                for (CatPredioS6Uso pu : usos) {
                    clon.getUsosList().add(new CtlgItem(pu.getCtlg().longValue()));
                    System.out.println("Uso " + pu.getCtlg() + " clon.getId() " + clon.getId());
//                    manager.executeNativeQuery(DefineQuerys.insertUso, new Object[]{clon.getId(), pu.getCtlg()});
                }
            }
            clon = registrarPredioS6(clon);

        }

        return clon;
    }

    @Override
    public CatPredioPropietario clonarPropietario(CatPredioPropietario propietario, CatPredio predio) {
        CatPredioPropietario clon = new CatPredioPropietario();
        BeanUtils.copyProperties(propietario, clon);
        clon.setId(null);
        clon.setPredio(predio);
        try {
            clon = (CatPredioPropietario) manager.persist(clon);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "registrarPredio", e);
        }

        return clon;
    }

    @Override
    public Collection<FotoPredio> cambiarPredio(CatPredio old, CatPredio predio) {
        String[] param = {"idPredio"};
        Object[] value = {old.getId()};
        Collection<FotoPredio> photos = manager.findAll(Querys.getFotosByPredio, param, value);
        Collection<FotoPredio> fotos = new ArrayList<>();
        if (photos != null) {
            photos.stream().map((fp) -> {
                FotoPredio clon = new FotoPredio();
                BeanUtils.copyProperties(fp, clon);
                return clon;
            }).map((clon) -> {
                clon.setIdPredio(predio.getId());
                return clon;
            }).map((clon) -> {
                clon.setCodPredio(Long.parseLong(predio.getNumPredio().toString()));
                return clon;
            }).map((clon) -> {
                clon.setNombre(predio.getNumPredio().toString());
                return clon;
            }).map((clon) -> (FotoPredio) manager.persist(clon)).forEachOrdered((f) -> {
                fotos.add(f);
            });
        }
        return fotos;
    }

    @Override
    public CatPredio clonarPredio(CatPredio predio, Short codProv, String[] ingnore) {

        CatPredio nuevo = new CatPredio();
        BeanUtils.copyProperties(predio, nuevo, ingnore);

        nuevo.setId(null);
        nuevo.setProvincia(codProv);
        nuevo.setPredialant(predio.getClaveCat());

        nuevo.setEstado(EstadosPredio.TEMPORAL);
        nuevo.setPredioRaiz(BigInteger.valueOf(predio.getId()));
        nuevo = this.registrarPredio(nuevo);

        for (CatEscritura escritura : predio.getCatEscrituraCollection()) {
            clonarEscrituras(escritura, nuevo, escritura.getCatEscrituraPropietarioList(), null);
        }

        CatPredioS4 predioS4 = catas.getPredioS4ByPredio(predio);
        if (predioS4 != null) {
            this.clonarPredioS4(predioS4, nuevo, predioS4.getAccesibilidadList(), true, null);
        }
        CatPredioS6 predioS6 = catas.getPredioS6ByPredio(predio);
        if (predioS6 != null) {
            List<CatPredioS6Uso> usos = manager.nativeQuery(DefineQuerys.getUsos, new Object[]{predioS6.getId()}, CatPredioS6Uso.class);
            this.clonarPredioS6(predioS6, nuevo, predio.getCatPredioS6().getCtlgItemCollection(), predio.getCatPredioS6().getCtlgItemCollectionInstalacionEspecial(), usos, true, null);
        }
        String[] param = {"idEdificacion"};
        Object[] values = new Object[2];
        Collection<CatPredioEdificacionProp> detallesEdificacion;
        Collection<CatPredioEdificacion> edificacionesActuales = manager.findAll(Querys.edificacionesByPredio, new String[]{"idPredio"}, new Object[]{predio.getId()});
        CatPredioEdificacion edificacion;
        short numeroEdificacion = (short) 1;
        if (edificacionesActuales != null) {
            for (CatPredioEdificacion cpe : edificacionesActuales) {
                edificacion = this.clonarEdificacion(cpe, nuevo, numeroEdificacion++, true);
                values[0] = cpe.getId();
                detallesEdificacion = manager.findAll(Querys.getDetallesEdificacion, param, values);
                String[] param1 = {"idEdificacion", "idProp"};
                Object[] values1 = new Object[2];
                if (detallesEdificacion != null) {
                    for (CatPredioEdificacionProp det : detallesEdificacion) {
                        values1[0] = cpe.getId();
                        values1[1] = det.getId();
                        CatEdfProp prop = (CatEdfProp) manager.find(Querys.getProps, param1, values1);
                        if (prop != null) {
                            this.clonarEdificacionProp(det, edificacion, prop, true);
                        }

                    }
                }
            }
        }

        Collection<CatPredioPropietario> duenos = predio.getCatPredioPropietarioCollection();
        if (duenos != null) {
            for (CatPredioPropietario cpp : duenos) {
                this.clonarPropietario(cpp, nuevo);
            }
        }

        this.cambiarPredio(predio, nuevo);

        return nuevo;
    }

    @Override
    public String claveCatastral(CatPredio px) {
        return Utils.completarCadenaConCeros(px.getProvincia().toString(), 2)
                + Utils.completarCadenaConCeros(px.getCanton().toString(), 2)
                + Utils.completarCadenaConCeros(px.getParroquia().toString(), 2)
                + Utils.completarCadenaConCeros(px.getZona().toString(), 2)
                + Utils.completarCadenaConCeros(px.getSector().toString(), 2)
                + Utils.completarCadenaConCeros(px.getMz().toString(), 3)
                + Utils.completarCadenaConCeros(px.getSolar().toString(), 3)
                + Utils.completarCadenaConCeros(px.getBloque().toString(), 3)
                + Utils.completarCadenaConCeros(px.getPiso().toString(), 2)
                + Utils.completarCadenaConCeros(px.getUnidad().toString(), 3);
    }

    public String claveCatastralAnt(CatPredio px) {
        return Utils.completarCadenaConCeros(px.getProvincia().toString(), 2)
                + Utils.completarCadenaConCeros(px.getCanton().toString(), 2)
                + Utils.completarCadenaConCeros(px.getParroquia().toString(), 2)
                + Utils.completarCadenaConCeros(px.getZona().toString(), 2)
                + Utils.completarCadenaConCeros(px.getSector().toString(), 2)
                + Utils.completarCadenaConCeros(px.getMz().toString(), 2)
                + Utils.completarCadenaConCeros(px.getSolar().toString(), 3)
                + Utils.completarCadenaConCeros(px.getBloque().toString(), 3);
    }

    @Override
    public BigInteger generarNumPredio() {
        try {

            Object sequence = manager.find(Querys.getMaxCatPredio);
            BigInteger l;
            if (sequence == null) {
                return new BigInteger("1");
            } else {
                l = (BigInteger) sequence;
                l = l.add(new BigInteger("1"));
                return l;
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public void saveEdificacionesPredio(CatPredio predioRaiz, CatPredio predioResultante) {
        try {
            String[] param1 = {"idEdificacion", "idProp"};
            String[] param = {"idEdificacion"};
            Collection<CatPredioEdificacion> edificacionesActuales = catas.getEdificaciones(predioRaiz);
            Collection<CatPredioEdificacionProp> detallesEdificacion;
            CatPredioEdificacion edificacion;
            Object[] values = new Object[2];
            Object[] values1 = new Object[2];
            short numeroEdificacion = (short) 1;

            for (CatPredioEdificacion cpe : edificacionesActuales) {
                edificacion = this.clonarEdificacion(cpe, predioResultante, numeroEdificacion++, true);
                //values[0] = predioRaiz.getId();
                values[0] = cpe.getId();
                values1[0] = cpe.getId();
                detallesEdificacion = manager.findAllEntCopy(Querys.getDetallesEdificacion, param, values);
                if (detallesEdificacion != null && !detallesEdificacion.isEmpty()) {
                    for (CatPredioEdificacionProp det : detallesEdificacion) {
                        values1[1] = det.getId();
                        CatEdfProp prop = (CatEdfProp) manager.find(Querys.getProps, param1, values1);
                        this.clonarEdificacionProp(det, edificacion, prop, true);
                        // this.clonarEdificacionProp(det, edificacion, true);
                    }
                }
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "saveEdificacionesPredio", e);
        }

    }

    @Override
    public void saveFotosPredio(CatPredio predioRaiz, CatPredio predioResultante) {
        try {
            List<FotoPredio> fotos;
            fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predioRaiz.getId()});
            if (fotos != null) {
                for (FotoPredio fp : fotos) {
                    fotoBean.setNombre(fp.getNombre());
                    fotoBean.setPredioId(predioResultante.getNumPredio().longValue());
                    fotoBean.setIdPredio(predioResultante.getId());
                    fotoBean.setContentType(fp.getContentType());
                    fotoBean.setFileId(fp.getFileOid());
                    fotoBean.saveFoto();
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "saveFotosPredio", e);
        }

    }

    @Override
    public CatPredio asentarCambiosData(CatPredio actualizado, String[] propertyIgnores) {

        CatPredio old = null;
        try {
            // SOLO INGRESA CUANDO ESTA CREADA LA COPIA TEMPORAL.
            if (actualizado.getClaveCat().startsWith("99")) {
                old = catas.getPredioId(actualizado.getPredioRaiz().longValue());
                if (old == null) {
                    return null;
                }

                String claveOld = old.getClaveCat();
                // ELIMINAMOS LOS PROPIETARIOS ANTERIORES
                if (Utils.isNotEmpty(old.getCatPredioPropietarioCollection())) {
                    manager.deleteList(old.getCatPredioPropietarioCollection());
                }
                // ELIMINAMOS LAS EDIFICACIONES ANTERIORES
                if (Utils.isNotEmpty(old.getCatPredioEdificacionCollection())) {
                    manager.deleteList(old.getCatPredioEdificacionCollection());
                }
                BeanUtils.copyProperties(actualizado, old, propertyIgnores);
                actualizado.setCatPredioEdificacionCollection(null);
                actualizado.setCatPredioPropietarioCollection(null);

                old.setId(actualizado.getPredioRaiz().longValue());
                old.setClaveCat(claveOld);
                old.setEstado(EstadosPredio.ACTIVO);
                old.setCatPredioS4(null);
                old.setCatPredioS6(null);
                old.setCatPredioS12(null);
                if (Utils.isNotEmpty(old.getCatPredioEdificacionCollection())) {
                    for (CatPredioEdificacion cpe : old.getCatPredioEdificacionCollection()) {
                        cpe.setPredio(old);
                    }
                }
                if (Utils.isNotEmpty(old.getCatPredioPropietarioCollection())) {
                    for (CatPredioPropietario cpp : old.getCatPredioPropietarioCollection()) {
                        cpp.setPredio(old);
                    }
                }
                old = (CatPredio) manager.persist(old);

                CatPredioS4 predioS4Update = catas.getPredioS4ByPredio(actualizado);
                CatPredioS4 predioS4Old = catas.getPredioS4ByPredio(old);
                Collection<CtlgItem> accesibilidadListOld = null;
                if (predioS4Old != null) {
                    accesibilidadListOld = predioS4Old.getAccesibilidadList();
                }

                if (predioS4Update != null) {
                    String[] propertiesIgnoresS4 = new String[]{"accesibilidadList"};
                    if (predioS4Old != null) {
                        Long id = predioS4Old.getId();
                        BeanUtils.copyProperties(predioS4Update, predioS4Old, propertiesIgnoresS4);
                        predioS4Old.setId(id);
                        predioS4Old.setPredio(old);
                        old.setCatPredioS4(predioS4Old);
                        Collection<CtlgItem> accesibilidadList = predioS4Update.getAccesibilidadList();

                        if (accesibilidadListOld != null) {
                            manager.executeNativeQuery(DefineQuerys.deleteAccesibilidad, new Object[]{id});
                        }
                        if (accesibilidadList != null) {
                            manager.executeNativeQuery(DefineQuerys.updateAccesibilidad, new Object[]{id, predioS4Update.getId()});
                        }
                        manager.persist(predioS4Old);
                    }
                }

                CatPredioS6 predioS6Update = catas.getPredioS6ByPredio(actualizado);
                CatPredioS6 predioS6Old = catas.getPredioS6ByPredio(old);
                if (predioS6Update != null) {
                    String[] propertiesIgnoresS6 = new String[]{"ctlgItemCollection", "ctlgItemCollectionInstalacionEspecial", "extras"};
                    if (predioS6Old != null) {
                        Collection<CtlgItem> viasOld = predioS6Old.getCtlgItemCollection();
                        Collection<CtlgItem> installOld = predioS6Old.getCtlgItemCollectionInstalacionEspecial();
                        Long id = predioS6Old.getId();
                        predioS6Update = catas.getPredioS6ByPredio(actualizado);
                        BeanUtils.copyProperties(predioS6Update, predioS6Old, propertiesIgnoresS6);
                        predioS6Old.setId(id);
                        predioS6Old.setPredio(old);
                        old.setCatPredioS6(predioS6Old);
                        predioS6Old.setAbastEnergia(predioS6Update.getAbastEnergia());
                        predioS6Old.setCercaniaPoblado(predioS6Update.getCercaniaPoblado());
                        predioS6Old.setDisponiblidadRiego(predioS6Update.getDisponiblidadRiego());
                        predioS6Old.setMetodoRiego(predioS6Update.getMetodoRiego());
                        Collection<CtlgItem> vias = predioS6Update.getCtlgItemCollection();
                        if (viasOld != null) {
                            manager.executeNativeQuery(DefineQuerys.deleteVias, new Object[]{id});
                        }
                        if (vias != null) {
                            manager.executeNativeQuery(DefineQuerys.updateVias, new Object[]{id, predioS6Update.getId()});
                        }
                        Collection<CtlgItem> instalaciones = predioS6Update.getCtlgItemCollectionInstalacionEspecial();
                        if (installOld != null) {
                            manager.executeNativeQuery(DefineQuerys.deleteInstalaciones, new Object[]{id});
                        }
                        if (instalaciones != null) {
                            manager.executeNativeQuery(DefineQuerys.updateInstalaciones, new Object[]{id, predioS6Update.getId()});
                        }
                        manager.persist(predioS6Old);
                    }
                }

                for (CatEscritura esc : actualizado.getCatEscrituraCollection()) {
                    esc.setPredio(old);
                    manager.persist(esc);
                }

                HiberUtil.flushAndCommit();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return old;
    }

    @Override
    public void prepararUsosPredio(CatPredio actualizado) {

        CatPredio old = catas.getPredioId(actualizado.getPredioRaiz().longValue());

        CatPredioS6 predioS6Update = catas.getPredioS6ByPredio(actualizado);
        CatPredioS6 predioS6Old = catas.getPredioS6ByPredio(old);
        if (predioS6Old != null) {
            List<CatPredioS6Uso> usosOld = manager.nativeQuery(DefineQuerys.getUsos, new Object[]{predioS6Old.getId()}, CatPredioS6Uso.class);
            if (usosOld != null) {
                manager.executeNativeQuery(DefineQuerys.deleteUsos, new Object[]{predioS6Old.getId()});
            }
        }
        if (predioS6Update != null) {
            List<CatPredioS6Uso> usosUpdate = manager.nativeQuery(DefineQuerys.getUsos, new Object[]{predioS6Update.getId()}, CatPredioS6Uso.class);
            if (usosUpdate != null) {
                for (CatPredioS6Uso u : usosUpdate) {
                    if (predioS6Old != null) {
                        manager.executeNativeQuery(DefineQuerys.insertUso, new Object[]{predioS6Old.getId(), u.getCtlg()});
                    }
                }
                manager.executeNativeQuery(DefineQuerys.deleteUsos, new Object[]{predioS6Update.getId()});
            }
        }
        if (old.getCatEscrituraCollection() != null) {
            manager.executeNativeQuery(DefineQuerys.deleteEscritura, new Object[]{old.getId()});
        }

    }

    @Override
    public CatPredio asentarCambiosConstruccion(CatPredio predio, CatPredio actualizado) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Set<String> getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
//        for (PropertyDescriptor pd : pds) {
//            Object srcValue = src.getPropertyValue(pd.getName());
//            if (srcValue == null) {
//                emptyNames.add(pd.getName());
//            }
//        }
        return emptyNames;
    }

    @Override
    public CatPredioAlicuotaComponente clonarAlicuotaComponente(CatPredioAlicuotaComponente alicuota, CatPredio predio, boolean save, Long id, String... ignore) {
        return null;
    }

    @Override
    public CatPredioAlicuotaComponente registrarAlicuotaComponente(CatPredioAlicuotaComponente alicuota) {
        CatPredioAlicuotaComponente comp = null;

        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            comp = (CatPredioAlicuotaComponente) sess.merge(alicuota); // RETORNA EL OBJETO PERSISTIDO
            sess.flush();
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
        }

        return comp;
    }

    @Override
    public String generarClaveCatastral(CatPredio predio) {
        String clave = predio.getClaveCat();
        String a = clave.substring(2, clave.length());
        a = String.format("%02d", predio.getProvincia()) + a;
        return a;
    }

    @Override
    public boolean eliminarPredioCompleto(CatPredio predio) {

        return true;
    }

    /**
     * Realiza la Creqcion de los nuevos predios en el alfanumerico
     *
     * @param predio Predio matriz en proceso de division
     * @param geoPredios Predios Divididos
     * @return Lista de nuevas claves Creadas.
     */
    @Override
    public List<CatPredio> dividirPredios(CatPredio predio, List<GeoPrediosDivididos> geoPredios) {

        try {
            predio.setCatPredioS6(null);
            predio.setCatPredioS4(null);
            predio.setCatPredioPropietarioCollection(null);
            predio.setCatPredioEdificacionCollection(null);
            predio = this.inicializeForeingkey(predio);
            List<CatPredio> predios = new ArrayList<>();
            BigInteger numPredio = generarNumPredio();
            Integer ultimo = catas.getSolarMaxPredio(predio);
            ultimo++;

            Collections.sort(geoPredios);
            String[] ignoresProperties = new String[]{"catPredioS4", "catPredioS6", "catPredioPropietarioCollection", "ordenDetEspecialCollection", "catPredioEdificacionCollection"};

            for (GeoPrediosDivididos geoPredio : geoPredios) {
                try {
                    CatPredio temp = clonarPredioDiv(predio, geoPredio, ultimo.shortValue(), numPredio, ignoresProperties);
                    predios.add(temp);
                    ultimo++;
                    numPredio = numPredio.add(BigInteger.ONE);
                    geoPredio.setCodigoNuevo(temp.getClaveCat());
                    System.out.println("GID PREDIO " + geoPredio.getGid() + " PROCESADO " + geoPredio.getCodigoNuevo() + " " + geoPredio.getNumeracion());
                    this.manager.persist(geoPredio);
                } catch (BeansException be) {
                    LOG.log(Level.SEVERE, "Generar divissiones", be);
                    throw new RuntimeException(be);
                }

            }
            return predios;
        } catch (GeoProcesosException e) {
            LOG.log(Level.SEVERE, null, e);
            throw new GeoProcesosException("dividirPredios " + e);
        }
    }

    /**
     * Clona los datos de la ficha de la cual se divide
     *
     * @param predio Predio Matriz
     * @param geoPredio
     * @param codigo Numeracion de solar nueva
     * @param numPredio numero de predio
     * @param ingnore Atributos que no seran clonados
     * @return CatPredio nuevo
     */
    private CatPredio clonarPredioDiv(CatPredio predio, GeoPrediosDivididos geoPredio, Short codigo, BigInteger numPredio, String[] ingnore) {

        CatPredio nuevo = null;
        try {
            nuevo = new CatPredio();
            Short numer = null;
            if (geoPredio.getCodigoNuevo() != null) {
                numer = Short.valueOf(geoPredio.getCodigoNuevo());
                if (numer == 0) {
                    numer = null;
                }
            }
            Integer ultimo = null;
            if (geoPredio.getCodigoNuevo() != null) {
                System.out.println(nuevo.getProvincia() + ":" + nuevo.getCanton() + ":"
                        + nuevo.getParroquia() + ":" + nuevo.getZona() + ":" + nuevo.getSector() + ":" + nuevo.getMz());
                ultimo = catas.getSolarMaxPredio(nuevo);
                ultimo++;

                if (ultimo >= geoPredio.getNumeracion()) {

                } else {
                    ultimo = geoPredio.getNumeracion().intValue();
                }
                nuevo.setSolar(ultimo.shortValue());
                nuevo.setLote(ultimo.shortValue());
            } else {
                ultimo = codigo.intValue();
            }
            BigDecimal areaLote = BigDecimal.ZERO;
            if (predio.getTipoPredio().equalsIgnoreCase("R")) {
                nuevo.setAreaSolar(geoPredio.getArea().divide(BigDecimal.valueOf(10000.00)));
                areaLote = geoPredio.getArea().divide(BigDecimal.valueOf(10000.00));
            } else {
                nuevo.setAreaSolar(geoPredio.getArea());
                areaLote = geoPredio.getArea();
            }
            BigInteger idPredio = null;
            try {
                idPredio = (BigInteger) manager.getNativeQuery("SELECT sgm_app.clonar_datos_predio(?, ?, ?, ?, ?, ?, ?);",
                        new Object[]{geoPredio.getArea(), areaLote, ((numer != null) ? numer : predio.getMz()), ultimo.shortValue(), numPredio.longValue(), user.getUserId(), predio.getId()});
            } catch (Exception e) {
                throw new GeoProcesosException(e.getMessage());
            }
            nuevo = this.manager.find(CatPredio.class, idPredio.longValue());
            nuevo.setPredialant(nuevo.getPredialant());
            nuevo.setClaveCat(nuevo.getCodigoPredial());
            this.manager.persist(nuevo);

            if (nuevo.getTipoPredio().equalsIgnoreCase("U")) {
                this.procesosService.verificarEjeValorGidPredioTx(predio, nuevo, geoPredio.getGid());
            } else {
                // VERIFICACION DE CALIDAD DEL SUELO RURAL
                // REGISTRA LAS CALIDADES SUELO ENCONTRADAS EN EL PREDIO
                if (procesosService.getActivarCalidadSueloRural()) {
                    CtlgItem get = null;
                    if (Utils.isNotEmpty(predio.getCatPredioClasificRuralCollection())) {
                        get = predio.getCatPredioClasificRuralCollection().get(0).getUsoPredio();
                    }
                    this.procesosService.verificarClasificacionsuelo1(nuevo, geoPredio.getGid(), get);
                }
            }
            manager.getNativeQuery("SELECT geodata.\"analizarBloqueEnPredio\"(?, ?, ?);", new Object[]{geoPredio.getGid(), nuevo.getCodigoPredial(), ""});
            List<BloqueGeoData> bloques = this.linderos.getBloquesPredio(geoPredio);
            if (Utils.isNotEmpty(bloques)) {
                String[] param = {"idEdificacion"};
                Object[] values = new Object[2];
                Collection<CatPredioEdificacionProp> detallesEdificacion;
                Collection<CatPredioEdificacion> edificacionesActuales = manager.findAll(Querys.edificacionesByPredio, new String[]{"idPredio"}, new Object[]{predio.getId()});
                CatPredioEdificacion edificacion;
                Short numeroEdfOrg = Short.valueOf("1");
                System.out.println("Geo Bloque: " + bloques);
                for (BloqueGeoData b : bloques) {
                    if (edificacionesActuales != null) {
                        for (CatPredioEdificacion cpe : edificacionesActuales) {
                            // Verificamos que la edificacion se igual a la de tempBloque
                            if (cpe.getNoEdificacion() == b.getNum()) {
                                cpe.setCatEdificacionPisosDetCollection(null);
                                edificacion = this.clonarEdificacion(cpe, nuevo, numeroEdfOrg, true);
                                if (Utils.isNotEmpty(b.getNiveles())) {
                                    if (Utils.isEmpty(edificacion.getCatEdificacionPisosDetCollection())) {
                                        edificacion.setCatEdificacionPisosDetCollection(new ArrayList<>());
                                    }
                                    for (Map.Entry<Short, BigDecimal> en : b.getNumPisos().entrySet()) {
                                        Short key = en.getKey();
                                        BigDecimal value = en.getValue();
                                        CatEdificacionPisosDet det = new CatEdificacionPisosDet();
                                        det.setArea(value);
                                        det.setAnio(0);
                                        det.setEstado(EstadosPredio.ACTIVO);
                                        det.setEstructura(Boolean.FALSE);
                                        det.setFecCre(new Date());
                                        det.setPiso(key);
                                        det.setNivel(this.catas.getItemByCatalagoOrder("bloque.nivel", BigInteger.valueOf(key)));
                                        edificacion.getCatEdificacionPisosDetCollection().add(det);
                                    }
                                }
                                // ACTUALIZAMOS EL GEOBLOQUE
                                edificacion.setAreaBloque(b.getAreaBloque());
//                                this.linderos.getTempFac().updateCodNum(b, nuevo.getCodigoPredial(), numeroEdfOrg);
                                this.manager.persist(edificacion);
                                //COPIAMOS LAS CARACTERISTICAS DE LA EDIFIACION
                                values[0] = cpe.getId();
                                detallesEdificacion = manager.findAll(Querys.getDetallesEdificacion, param, values);
                                String[] param1 = {"idEdificacion", "idProp"};
                                Object[] values1 = new Object[2];
                                if (detallesEdificacion != null) {
                                    for (CatPredioEdificacionProp det : detallesEdificacion) {
                                        values1[0] = cpe.getId();
                                        values1[1] = det.getId();
                                        CatEdfProp prop = (CatEdfProp) manager.find(Querys.getProps, param1, values1);
                                        if (prop != null) {
                                            this.clonarEdificacionProp(det, edificacion, prop, true);
                                        }

                                    }
                                }
                            }// fin comparacion bloque iguales.
                        }
                    }
                    numeroEdfOrg++;
                }
            }
            this.cambiarPredio(predio, nuevo);
        } catch (BeansException | NumberFormatException beansException) {
            throw new GeoProcesosException(beansException.getMessage());
        } catch (RuntimeException ex) {
            LOG.log(Level.SEVERE, "", ex);
            throw new GeoProcesosException(ex.getMessage());
        }
        return nuevo;
    }

    @Override
    public CatPredio clonarPredioFusion(CatPredio predio, List<CatPredio> predios, Short codLote) {
        try {
            BigDecimal areaTotal = BigDecimal.ZERO;
            PolygonData polygonData = null;
            if (predio.getAreaSolar() != null) {
                areaTotal = predio.getAreaSolar();
            }
            for (CatPredio p : predios) {
                List<PolygonData> pd = linderos.getPoligonosActualizados(Arrays.asList(p.getClaveCat()));
                if (Utils.isNotEmpty(pd)) {
                    if (pd.get(0).getArea() != null) {
                        areaTotal = pd.get(0).getArea();
                        polygonData = pd.get(0);
                    }
                }
            }

            if (predio.getTipoPredio().equalsIgnoreCase("R")) {
                areaTotal = areaTotal.divide(BigDecimal.valueOf(10000));
            }

            CatPredio nuevo = new CatPredio();
            BeanUtils.copyProperties(predio, nuevo, new String[]{"catPredioS4", "catPredioS6", "catPredioPropietarioCollection"});
            nuevo.setId(null);
            nuevo.setNumPredio(this.generarNumPredio());
            nuevo.setNumeroFicha(nuevo.getNumPredio());
            nuevo.setInstCreacion(new Date());
            nuevo.setAreaSolar(areaTotal);
            nuevo.setSolar(codLote);
            nuevo.setLote(codLote);
            nuevo.setPredialant(predio.getClaveCat());
            nuevo.setPredioRaiz(BigInteger.valueOf(predio.getId()));
            nuevo.setEstado(EstadosPredio.PENDIENTE);
            nuevo = this.registrarPredio(nuevo);

            for (CatEscritura escritura : predio.getCatEscrituraCollection()) {
                clonarEscrituras(escritura, nuevo, escritura.getCatEscrituraPropietarioList(), null);
            }
            if (nuevo.getTipoPredio().equalsIgnoreCase("U")) {
                this.procesosService.verificarEjeValorGidPredioTx(predio, nuevo, polygonData.getGid());
            } else {
                // VERIFICACION DE CALIDAD DEL SUELO RURAL
                // REGISTRA LAS CALIDADES SUELO ENCONTRADAS EN EL PREDIO
                if (procesosService.getActivarCalidadSueloRural()) {
                    this.procesosService.verificarClasificacionsuelo(nuevo, polygonData.getGid(), predio.getCatPredioClasificRuralCollection());
                }
            }
            CatPredioS4 predioS4 = catas.getPredioS4ByPredio(predio);
            if (predioS4 != null) {
                CatPredioS4 clonarPredioS4 = this.clonarPredioS4(predioS4, nuevo, predioS4.getAccesibilidadList(), true, null);
                clonarPredioS4.setAreaGraficaLote(polygonData.getArea());
                manager.persist(clonarPredioS4);
            }
            CatPredioS6 predioS6 = catas.getPredioS6ByPredio(predio);
            if (predioS6 != null) {
                List<CatPredioS6Uso> usos = manager.nativeQuery(DefineQuerys.getUsos, new Object[]{predioS6.getId()}, CatPredioS6Uso.class);
                this.clonarPredioS6(predioS6, nuevo, predio.getCatPredioS6().getCtlgItemCollection(), predio.getCatPredioS6().getCtlgItemCollectionInstalacionEspecial(), usos, true, null);
            }
            if (polygonData != null) {
                System.out.println("polygonData.getGid() " + polygonData.getGid() + " nuevo.getCodigoPredial() " + nuevo.getCodigoPredial());
                this.manager.getNativeQuery("SELECT geodata.\"analizarBloqueEnPredio\"(?, ?, ?);", new Object[]{polygonData.getGid(), nuevo.getCodigoPredial(), ""});
                HiberUtil.flushAndCommit();
            }
            String[] param = {"idEdificacion"};
            Object[] values = new Object[2];
            Collection<CatPredioEdificacionProp> detallesEdificacion;
            Collection<CatPredioEdificacion> edificacionesActuales = manager.findAll(Querys.edificacionesByPredio, new String[]{"idPredio"}, new Object[]{predio.getId()});
            CatPredioEdificacion edificacion;
            short numeroEdificacion = (short) 1;
            for (CatPredioEdificacion cpe : edificacionesActuales) {
                edificacion = this.clonarEdificacion(cpe, nuevo, numeroEdificacion++, true);
                values[0] = cpe.getId();
                detallesEdificacion = manager.findAll(Querys.getDetallesEdificacion, param, values);
                String[] param1 = {"idEdificacion", "idProp"};
                Object[] values1 = new Object[2];
                if (detallesEdificacion != null) {
                    for (CatPredioEdificacionProp det : detallesEdificacion) {
                        values1[0] = cpe.getId();
                        values1[1] = det.getId();
                        CatEdfProp prop = (CatEdfProp) manager.find(Querys.getProps, param1, values1);
                        if (prop != null) {
                            this.clonarEdificacionProp(det, edificacion, prop, true);
                        }

                    }
                }
            }
            values = new Object[2];
            for (CatPredio cp : predios) {
                if (cp.getId() != predio.getId()) {
                    Collection<CatPredioEdificacion> edificacionesNuevas = manager.findAll(Querys.edificacionesByPredio, new String[]{"idPredio"}, new Object[]{cp.getId()});
                    for (CatPredioEdificacion cpe : edificacionesNuevas) {
                        edificacion = this.clonarEdificacion(cpe, nuevo, numeroEdificacion++, true);
                        values[0] = cpe.getId();
                        detallesEdificacion = manager.findAllEntCopy(Querys.getDetallesEdificacion, param, values);
                        String[] param1 = {"idEdificacion", "idProp"};
                        Object[] values1 = new Object[2];
                        for (CatPredioEdificacionProp det : detallesEdificacion) {
                            values1[0] = cpe.getId();
                            values1[1] = det.getId();
                            CatEdfProp prop = (CatEdfProp) manager.find(Querys.getProps, param1, values1);
                            if (prop != null) {
                                this.clonarEdificacionProp(det, edificacion, prop, true);
                            }
                        }
                    }
                }
            }
            Collection<CatPredioPropietario> duenos = predio.getCatPredioPropietarioCollection();
            for (CatPredioPropietario cpp : duenos) {
                this.clonarPropietario(cpp, nuevo);
            }
            predios.add(predio);
            for (CatPredio pp : predios) {
                this.cambiarPredio(pp, nuevo);
                this.savePredioFusionDivision(pp, nuevo, "FUSION");
                pp.setEstado("H");
                pp.setCatPredioClasificRuralCollection(null);
                manager.persist(pp);
            }
            return nuevo;
        } catch (Exception e) {
            throw new GeoProcesosException(e.getMessage());
        }
    }

    private void savePredioFusionDivision(CatPredio predioRaiz, CatPredio predioResultante, String tipo) {
        try {
            if (predioRaiz != null) {
                CatPredioFusionDivision pdf = new CatPredioFusionDivision();
                pdf.setPredioRaiz(predioRaiz);
                pdf.setPredioResultante(predioResultante);
                pdf.setTipo(getTipoFraccionPredio(tipo));
                pdf.setEstado(Boolean.TRUE);
                manager.persist(pdf);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "savePredioFusionDivision", e);
        }
    }

    private CtlgItem getTipoFraccionPredio(String tipo) {
        try {
            return (CtlgItem) manager.find(Querys.getCtlgItemByCatalogoValor, new String[]{"catalogo", "valor"}, new Object[]{"predio.fusion_division", tipo});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "getTipoFraccionPredio", e);
        }
        return null;
    }

    @Override
    public HistoricoTramiteDet updateDetalle(HistoricoTramiteDet detalle) {
        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            detalle = (HistoricoTramiteDet) sess.merge(detalle); // RETORNA EL OBJETO PERSISTIDO
            sess.flush();
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return detalle;
    }

    @Override
    public CatEscritura clonarEscrituras(CatEscritura escritura, CatPredio predio, List<CatEscrituraPropietario> catEscrituraPropietarioList, Long id, String... ignore) {
        CatEscritura clon = new CatEscritura();
        if (escritura != null) {
            BeanUtils.copyProperties(escritura, clon, ignore);
            clon.setIdEscritura(id);
            clon.setPredio(predio);

            clon = registrarEscritura(clon);
            for (CatEscrituraPropietario ep : catEscrituraPropietarioList) {
                clonarEscrituraPropietario(clon, ep, null);
            }
            return clon;
        }
        return null;
    }

    @Override
    public CatEscritura registrarEscritura(CatEscritura escritura) {
        CatEscritura e = null;
        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            e = (CatEscritura) sess.merge(escritura); // RETORNA EL OBJETO PERSISTIDO
            sess.flush();
        } catch (HibernateException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return e;
    }

    public CatEscrituraPropietario clonarEscrituraPropietario(CatEscritura escritura, CatEscrituraPropietario ep, Long id) {
        CatEscrituraPropietario clon = new CatEscrituraPropietario();

        BeanUtils.copyProperties(ep, clon);
        clon.setId(id);
        clon.setEscritura(escritura);

        clon = registrarEscrituraPropietario(clon);

        return clon;
    }

    public CatEscrituraPropietario registrarEscrituraPropietario(CatEscrituraPropietario ep) {
        CatEscrituraPropietario eps = null;
        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            eps = (CatEscrituraPropietario) sess.merge(ep); // RETORNA EL OBJETO PERSISTIDO
//            sess.flush();
        } catch (HibernateException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return eps;
    }

    @Override
    public Observaciones guardarObservaciones(Long idTramite, String nameUser, String observaciones, String taskName, Long idPredio) {
        try {
            if (observaciones != null) {
                String temp = observaciones.replaceAll(" ", "");
                if (temp.equals("")) {
                    return null;
                }
            }
            Observaciones obs = new Observaciones();
            obs.setEstado(true);
            obs.setFecCre(new Date());
            obs.setTarea(taskName);
            obs.setIdTramite(new HistoricoTramites(idTramite));
            obs.setUserCre(nameUser);
            obs.setObservacion(observaciones);
            obs = (Observaciones) manager.persist(obs);
            if (idPredio != null) {
                obs.setIdProceso(BigInteger.valueOf(idPredio));
            }
            return obs;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            return null;
        }
    }

    public List<CatPredioLinderos> getLinderosGraficos(CatPredio predio) {
        if (predio != null) {
            List<PolygonData> poligonosActualizados = linderos.getTempFac().getColindantes(predio.getClaveCat());
            for (PolygonData lind : poligonosActualizados) {
                if (lind.getArea().doubleValue() <= 10 || lind.getArea().doubleValue() >= 350) {
                    System.out.println("Norte...");
                } else if (lind.getArea().doubleValue() > 10 && lind.getArea().doubleValue() < 80) {
                    System.out.println("Noreste...");
                } else if (lind.getArea().doubleValue() >= 80 && lind.getArea().doubleValue() <= 100) {
                    System.out.println("Este...");
                } else if (lind.getArea().doubleValue() > 100 && lind.getArea().doubleValue() < 170) {
                    System.out.println("Sureste..");
                } else if (lind.getArea().doubleValue() >= 170 && lind.getArea().doubleValue() <= 190) {
                    System.out.println("Sur...");
                } else if (lind.getArea().doubleValue() > 190 && lind.getArea().doubleValue() < 260) {
                    System.out.println("Suroeste..");
                } else if (lind.getArea().doubleValue() >= 260 && lind.getArea().doubleValue() <= 280) {
                    System.out.println("Oeste...");
                } else if (lind.getArea().doubleValue() > 280 && lind.getArea().doubleValue() < 350) {
                    System.out.println("Noroeste..");
                }
            }
            return null;
        } else {
            return null;
        }
    }

    private <T> T inicializeForeingkey(T obj) {
        try {
            List<Field> fields = ReflexionEntity.getFields(obj.getClass());
            for (Field field : fields) {
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    T campo = ReflexionEntity.getCampo(obj, field.getName());
                    Object idFromEntity = null;
                    boolean proxy = HiberUtil.isProxy(campo);
                    if (proxy) {
                        idFromEntity = HiberUtil.getProxyId(campo);
                        campo = HiberUtil.unproxy(campo);
                    } else {
                        idFromEntity = ReflexionEntity.getIdFromEntity(campo);
                    }
                    if (idFromEntity != null) {
                        if (proxy) {
                            ReflexionEntity.setCampo(obj, field.getName(), campo);
                        } else {
                            ReflexionEntity.setCampo(obj, field.getName(),
                                    this.manager.find(field.getType(), idFromEntity));
                        }
                    } else {
                        if ((campo == null) || (idFromEntity == null)) {
                            ReflexionEntity.setCampo(obj, field.getName(), null);
                        } else {
                            if (Hibernate.isPropertyInitialized(obj, field.getName())) {
                                Hibernate.initialize(campo);
                                ReflexionEntity.setCampo(obj, field.getName(), campo);
                            }
                        }
                    }
                } else if (field.isAnnotationPresent(JoinTable.class)) {
                    T campo = ReflexionEntity.getCampo(obj, field.getName());
                    if (campo == null) {
                        ReflexionEntity.setCampo(obj, field.getName(), null);
                    }
                    if (campo instanceof Collection) {
                        Collection<Object> listTemp = (Collection) campo;
                        Collection<Object> list = new ArrayList<>();
                        try {
                            if (Utils.isNotEmpty(listTemp)) {
                                for (Object object : listTemp) {
                                    Object idFromEntity = ReflexionEntity.getIdFromEntity(object);
                                    if (idFromEntity != null) {
                                        list.add(this.manager.find(object.getClass(), idFromEntity));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Inicializar Collection", e);
                        }
                        if (Utils.isNotEmpty(list)) {
                            ReflexionEntity.setCampo(obj, field.getName(), list);
                        } else {
                            ReflexionEntity.setCampo(obj, field.getName(), null);
                        }
                    }
                } else if (field.isAnnotationPresent(Embedded.class)) {
                    T campo = ReflexionEntity.getCampo(obj, field.getName());
                    if (campo == null) {
                        ReflexionEntity.setCampo(obj, field.getName(), null);
                    } else {
                        this.inicializeForeingkey(campo);
                    }
                }
            }
            return obj;
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, "<T> T inicializeForeingkey ", e);
            return null;
        }
    }

}
