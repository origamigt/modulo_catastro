/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.service.ordenes;

import com.origami.censocat.entity.ordentrabajo.OrdenDet;
import com.origami.censocat.entity.ordentrabajo.OrdenTrabajo;
import com.origami.censocat.models.FotosModel;
import com.origami.censocat.models.ResumenOrdenes;
import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.sgm.database.Querys;
import com.origami.sgm.database.SchemasConfig;
import com.origami.sgm.entities.CatEdificacionPisosDet;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioClasificRural;
import com.origami.sgm.entities.CatPredioCultivo;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioEdificacionProp;
import com.origami.sgm.entities.CatPredioLinderos;
import com.origami.sgm.entities.CatPredioObraInterna;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatPredioS6;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgmee.catastro.util.DefineQuerys;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import util.HiberUtil;
import util.Utils;

/**
 *
 * @author CarlosLoorVargas
 */
@Stateless
@Interceptors(value = {HibernateEjbInterceptor.class})
@Dependent
public class OrdenEjb implements OrdenService {

    private static final Logger LOG = Logger.getLogger(OrdenEjb.class.getName());

    @Inject
    private Entitymanager manager;
    @Inject
    private CatastroServices catastroServices;

    @Override
    public OrdenTrabajo guardarOrden(OrdenTrabajo ot, List<OrdenDet> dt) {
        try {
            if (ot == null) {
                return null;
            }
            Object orden = this.manager.getNativeQuery("select nextval('" + SchemasConfig.FLOW + ".num_orden_seq')");
            if (orden != null) {
                ot.setNumOrden(Long.parseLong(orden.toString()));
            } else {
                LOG.warning("No se pudo generar los el numero de orden");
            }
            OrdenTrabajo o = (OrdenTrabajo) this.manager.persist(ot);
            if (o != null) {
                if (dt != null) {
                    for (OrdenDet d : dt) {
                        d.setOrden(o);
                        this.manager.persist(d);
                    }
                }
                Hibernate.initialize(o);
                return o;
            }
        } catch (NumberFormatException | HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public OrdenDet guardarDetOrden(OrdenDet d) {
        OrdenDet det = null;
        try {
            if (d.getId() == null) {
                det = (OrdenDet) this.manager.persist(d);
            } else {
                this.manager.persist(d);
            }
            return det;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return det;
    }

    @Override
    public List<ResumenOrdenes> getResumenGeneral(String query, int tipo) {
        ResumenOrdenes r = null;
        List<ResumenOrdenes> lr;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date c;
        try {
            List<Object[]> x = this.manager.getSqlQuery(query);
            if (x != null) {
                lr = new ArrayList<>();
                for (Object[] i : x) {
                    switch (tipo) {
                        case 1:
                            r = new ResumenOrdenes(Long.parseLong(i[0].toString()), i[1].toString(),
                                    new Short(i[2].toString()), new Short(i[3].toString()), sdf.parse(i[4].toString()));
                            break;
                        case 2:
                            r = new ResumenOrdenes(i[0].toString(), new Short(i[1].toString()), new Short(i[2].toString()));
                            break;
                        case 3:
                            r = new ResumenOrdenes(i[0].toString(), new Short(i[1].toString()), new Short(i[2].toString()),
                                    new Short(i[3].toString()));
                            break;
                    }
                    lr.add(r);
                }
                return lr;
            }
        } catch (NumberFormatException | ParseException e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public List<OrdenTrabajo> getOrdenesTrabajo(String user) {
        List<OrdenTrabajo> ordenes = this.manager.findAll(Querys.getOrdenes("e.responsable.username=:username"),
                new String[]{"username"}, new Object[]{user});
        return ordenes;
    }

    @Override
    public OrdenDet getOrdenDet(Long idOrdenDet) {
        try {
            return this.manager.find(OrdenDet.class, idOrdenDet);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "getOrdenDet", e);
            return null;
        }
    }

    @Override
    public OrdenTrabajo guardarOrden(OrdenTrabajo orden) {
        try {
            if (orden == null) {
                return null;
            }
            if (orden.getId() == null) {
                orden = (OrdenTrabajo) this.manager.persist(orden);
            } else {
                this.manager.persist(orden);
            }
            return orden;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "guardarOrden", e);
            return null;
        }
    }

    @Override
    public Boolean procesarPredio(CatPredio predioCenso, CatPredio predioActS, List<FotosModel> fotosModel) {
        try {
            if (predioCenso != null) {
                CatPredio predioAct = this.copyDataPredio(predioCenso, predioActS,
                        Arrays.asList("catPredioS6", "catPredioS4", "catPredioEdificacionCollection",
                                "catPredioObraInternaCollection", "id", "instCreacion", "usuarioCreador", "solar",
                                "ciudadela", "fotos", "catPredioClasificRuralCollection", "lote",
                                "finanPrestamoPredioCollection", "catPredioPropietarioCollection",
                                "catPredioCultivoCollection", "predioCollection"));
                // COPIAMOS LOS DATOS DE LAS ENTITIES
                List<String> fotosPredio = predioCenso.getFotos();
                Collection<CatPredioObraInterna> obrasInternas = predioAct.getCatPredioObraInternaCollection();
                // PERSISTIMOS EL PREDIO...
                try {
                    predioAct = this.inicializeForeingkey(predioAct);
                    predioAct.setFecMod(new Date());
                    this.manager.persist(predioAct);
                    HiberUtil.flushAndCommit();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Predio ", e);
                }
                // SE ENVIA GUARDAR LAS FOTOS DEL PREDIO
                if (Utils.isNotEmpty(fotosPredio)) {
                    for (String foto : fotosPredio) {
                        int indexOf = fotosModel.indexOf(new FotosModel(foto));
                        if (indexOf > -1) {
                            FotosModel get = fotosModel.get(indexOf);
                            if (get != null) {
                                Boolean subida = this.catastroServices.subirFotoPredio(
                                        new ByteArrayInputStream(get.getImageFromBase64()), get.getId().concat(".jpg"),
                                        "image/jpg", predioAct);
                                System.out.println("Subida foto predio " + subida);
                            }
                        }
                    }
                    System.out.println("Haciendo commit de Predio y fotos...");
                    HiberUtil.flushAndCommit();
                }
                // PROCESAMOS LOS DETALLES DE LOS PREDIOS...
                // PERSISITIMOS CATPREDIOS6
                CatPredioS6 s6 = predioAct.getCatPredioS6();
                try {
                    if (s6 == null) {
                        s6 = new CatPredioS6();
                        s6.setPredio(predioAct);
                    }
                    s6 = this.copyDataPredio(predioCenso.getCatPredioS6(), s6, Arrays.asList("predio", "id"));
                    s6 = this.inicializeForeingkey(s6);
                    if (s6.getId() == null) {
                        s6 = (CatPredioS6) this.manager.persist(s6);
                    } else {
                        this.manager.persist(s6);
                    }
                    System.out.println("Haciendo commit de s6...");
                    HiberUtil.flushAndCommit();
                    if (predioCenso.getCatPredioS6() != null) {
                        if (Utils.isNotEmpty(predioCenso.getCatPredioS6().getUsosList())) {
                            for (CtlgItem item : predioCenso.getCatPredioS6().getUsosList()) {
                                if (item.getId() != null) {
                                    manager.executeNativeQuery(DefineQuerys.insertUso, new Object[]{s6.getId().intValue(), item.getId()});
                                }
                            }
                        }
                    }
                    HiberUtil.flushAndCommit();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "S6 ", e);
                }
                // OERSISTIMOS CATPREDIOS4
                CatPredioS4 s4 = predioAct.getCatPredioS4();
                try {
                    if (s4 == null) {
                        s4 = new CatPredioS4();
                        s4.setPredio(predioAct);
                    }
                    s4 = this.copyDataPredio(predioCenso.getCatPredioS4(), s4, Arrays.asList("predio", "id"));
                    s4 = this.inicializeForeingkey(s4);
                    this.manager.persist(s4);
                    System.out.println("Haciendo commit de s4...");
                    HiberUtil.flushAndCommit();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "S4 ", e);
                }
                // PERSITIMOS LAS EDIFICACIONES

                List<CatPredioEdificacion> edificaciones = (List<CatPredioEdificacion>) predioAct.getCatPredioEdificacionCollection();
                try {
                    if (Utils.isNotEmpty(predioCenso.getCatPredioEdificacionCollection())) {
                        for (CatPredioEdificacion edificacion : predioCenso.getCatPredioEdificacionCollection()) {
                            edificacion.setPredio(predioAct);
                            edificacion = this.inicializeForeingkey(edificacion);
                            if (edificacion.getId() == 0l) {
                                edificacion.setId(null);
                            } else {
                                if (Utils.isNotEmpty(edificaciones)) {
                                    int index = edificaciones.indexOf(edificaciones);
                                    if (index > 0) {
                                        CatPredioEdificacion get = edificaciones.get(index);
                                        if (get != null) {
                                            edificacion = this.copyDataPredio(edificacion, get,
                                                    Arrays.asList("id", "predio",
                                                            "catEdificacionPisosDetCollection", "catPredioEdificacionPropCollection"));
                                        }
                                    }
                                }
                            }
                            if (Utils.isNotEmpty(edificacion.getCatEdificacionPisosDetCollection())) {
                                List<CatEdificacionPisosDet> temp = new ArrayList<>();
                                for (CatEdificacionPisosDet piso : edificacion.getCatEdificacionPisosDetCollection()) {
                                    if (Objects.equals(piso.getId(), 0L)) {
                                        piso.setId(null);
                                    }
                                    piso = this.inicializeForeingkey(piso);
                                    piso.setEdificacion(edificacion);
                                    temp.add(piso);
                                }
                                edificacion.setCatEdificacionPisosDetCollection(temp);
                            }
                            if (Utils.isNotEmpty(edificacion.getCatPredioEdificacionPropCollection())) {
                                List<CatPredioEdificacionProp> temp = new ArrayList<>();
                                for (CatPredioEdificacionProp prop : edificacion
                                        .getCatPredioEdificacionPropCollection()) {
                                    if (Objects.equals(prop.getId(), 0L)) {
                                        prop.setId(null);
                                    }
                                    prop = this.inicializeForeingkey(prop);
                                    prop.setEdificacion(edificacion);
                                    temp.add(prop);
                                }
                                edificacion.setCatPredioEdificacionPropCollection(temp);
                            }
                            if (edificacion.getId() == null) {
                                edificacion = (CatPredioEdificacion) this.manager.persist(edificacion);
                            } else {
                                this.manager.persist(edificacion);
                            }
                            if (Utils.isNotEmpty(edificacion.getFotos())) {
                                for (String foto : edificacion.getFotos()) {
                                    FotosModel get = fotosModel.get(fotosModel.indexOf(new FotosModel(foto)));
                                    Boolean subida = this.catastroServices.subirFotoBloque(
                                            new ByteArrayInputStream(get.getImageFromBase64()),
                                            get.getId().concat(".jpg"), "image/jpg", edificacion);
                                    System.out.println("Subida foto bloque " + subida);
                                }
                            }
                        }
                        System.out.println("Haciendo commit de las edificaciones...");
                        HiberUtil.flushAndCommit();
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Edificaciones ", e);
                }
                // PERSISTIMOS LA OBRAS INTERNAS
                try {
                    if (Utils.isNotEmpty(obrasInternas)) {
                        for (CatPredioObraInterna obra : obrasInternas) {
                            if (Objects.equals(obra.getId(), 0L)) {
                                obra.setId(null);
                            }
                            obra.setPredio(predioAct);
                            obra = this.inicializeForeingkey(obra);
                            this.manager.persist(obra);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Obras ", e);
                }
                // Linderos del predio
                List<CatPredioLinderos> linderos = predioAct.getPredioCollection();
                try {
                    if (Utils.isNotEmpty(predioCenso.getPredioCollection())) {
                        for (CatPredioLinderos lindero : predioCenso.getPredioCollection()) {
                            if (Objects.equals(lindero.getId(), 0L)) {
                                lindero.setId(null);
                            } else {
                                if (Utils.isNotEmpty(linderos)) {
                                    int index = linderos.indexOf(lindero);
                                    if (index > 0) {
                                        CatPredioLinderos get = linderos.get(index);
                                        if (get != null) {
                                            lindero = this.copyDataPredio(lindero, get,
                                                    Arrays.asList("id", "predio"));
                                        }
                                    }
                                }
                            }
                            lindero = this.inicializeForeingkey(lindero);
                            lindero.setPredio(predioAct);
                            this.manager.persist(lindero);
                        }
                        HiberUtil.flushAndCommit();
                        System.out.println("Commit Linderos...");
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Linderos ", e);
                }
                // Cultivos
                List<CatPredioCultivo> cultivos = predioAct.getCatPredioCultivoCollection();
                try {
                    if (Utils.isNotEmpty(predioCenso.getCatPredioCultivoCollection())) {
                        for (CatPredioCultivo cultivo : predioCenso.getCatPredioCultivoCollection()) {
                            if (Objects.equals(cultivo.getId(), 0L)) {
                                cultivo.setId(null);
                            } else {
                                if (Utils.isNotEmpty(cultivos)) {
                                    int index = cultivos.indexOf(cultivo);
                                    if (index > 0) {
                                        CatPredioCultivo get = cultivos.get(index);
                                        if (get != null) {
                                            cultivo = this.copyDataPredio(cultivo, get,
                                                    Arrays.asList("id", "predio"));
                                        }
                                    }
                                }
                            }
                            cultivo.setPredio(predioAct);
                            cultivo = this.inicializeForeingkey(cultivo);
                            this.manager.persist(cultivo);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Cultivos ", e);
                }

                List<CatPredioClasificRural> ClasificacionSuelo = predioAct.getCatPredioClasificRuralCollection();
                try {
                    if (Utils.isNotEmpty(predioCenso.getCatPredioClasificRuralCollection())) {
                        for (CatPredioClasificRural clasifRu : predioCenso.getCatPredioClasificRuralCollection()) {
                            if (Objects.equals(clasifRu.getId(), 0L)) {
                                clasifRu.setId(null);
                            } else {
                                if (Utils.isNotEmpty(ClasificacionSuelo)) {
                                    int index = -1;
                                    int count = 0;
                                    for (CatPredioClasificRural ccr : ClasificacionSuelo) {
                                        if (ccr.getCalidadSuelo().equals(clasifRu.getCalidadSuelo())
                                                && ccr.getSectorHomogeneo().equals(clasifRu.getSectorHomogeneo())) {
                                            index = count;
                                        }
                                        count++;
                                    }
//                                    int index = ClasificacionSuelo.indexOf(clasifRu);
                                    if (index > 0) {
                                        CatPredioClasificRural get = ClasificacionSuelo.get(index);
                                        if (get != null) {
                                            clasifRu = this.copyDataPredio(clasifRu, get,
                                                    Arrays.asList("id", "predio"));
                                        }
                                    }
                                }
                            }
                            clasifRu.setPredio(predioAct);
                            clasifRu = this.inicializeForeingkey(clasifRu);
                            this.manager.persist(clasifRu);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "ClasificacionSuelo ", e);
                }
                System.out.println("Finalizando copia de predios...");
                HiberUtil.flushAndCommit();
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "procesarPredio ", e);
            return false;
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
                            // System.out.println("Set null field " + field.getName() + " from " +
                            // obj.getClass().getName());
                        } else {
                            System.out.println("INICIALIZE " + field.getName() + " VAL " + campo + " ID " + idFromEntity
                                    + " IS NULL " + (idFromEntity == null));
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
                        System.out.println("Inicializando extras");
                        this.inicializeForeingkey(campo);
                    }
                }
            }
            return obj;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "<T> T inicializeForeingkey ", e);
            return null;
        }
    }

    private <T> T setNullCollection(T obj) {
        List<Field> fields = ReflexionEntity.getFields(obj.getClass());
        for (Field field : fields) {
            if ((field.isAnnotationPresent(OneToMany.class)
                    && !(field.isAnnotationPresent(JoinTable.class) || field.isAnnotationPresent(JoinColumn.class)))
                    || (field.isAnnotationPresent(OneToOne.class) && !(field.isAnnotationPresent(JoinTable.class)
                    || field.isAnnotationPresent(JoinColumn.class)))
                    || (field.isAnnotationPresent(ManyToMany.class) && !(field.isAnnotationPresent(JoinTable.class)
                    || field.isAnnotationPresent(JoinColumn.class)))
                    || (field.isAnnotationPresent(ManyToOne.class) && !(field.isAnnotationPresent(JoinTable.class)
                    || field.isAnnotationPresent(JoinColumn.class)))) {
                ReflexionEntity.setCampo(obj, field.getName(), null);
            } else if (field.isAnnotationPresent(Embedded.class)) {
                T campo = ReflexionEntity.getCampo(obj, field.getName());
                if (campo != null) {
                    this.setNullCollection(campo);
                }
            }
        }
        return obj;
    }

    /**
     * Copia los datos modificados en la table a el objecto que se encuentra en
     * el sistema.
     *
     * @param <T> Se ajusta a cualquier tipo de dato
     * @param objCenso Objecto que viene desde la tablet
     * @param objActS Objecto disponible en el sistema
     * @return Objecto con los datos actualizados.
     */
    private <T> T copyDataPredio(T objCenso, T objActS, List<String> excludeFields) {
        if (HiberUtil.isProxy(objActS)) {
            objActS = HiberUtil.unproxy(objActS);
        }
        if (objActS != null) {
            System.out.println("COPY DATA FROM "
                    + objActS.getClass().getName().substring(objActS.getClass().getName().lastIndexOf(".") + 1));
            if (Utils.isEmpty(excludeFields)) {
                excludeFields = new ArrayList<>();
            }

            List<Field> fields = ReflexionEntity.getFields(objActS.getClass());
            if (objCenso != null) {
                for (Field field : fields) {
                    if (!excludeFields.contains(field.getName())) {
                        if (field.getName().contains("serialVersionUID") || (field.getName() == null)
                                || field.getName().contains("methods") || field.getName().contains("handler")
                                || field.getName().contains("filter_signature")) {
                        } else {
                            Object campo = ReflexionEntity.getCampo(objCenso, field.getName());
                            Object campoActS = ReflexionEntity.getCampo(objActS, field.getName());
                            if (HiberUtil.isProxy(campoActS)) {
                                campoActS = HiberUtil.unproxy(campoActS);
                            }
                            Boolean showInfo = Objects.equals(campoActS, campo);
                            if (Objects.nonNull(campo) && Objects.nonNull(campoActS)) {
                                if ((campo instanceof BigDecimal) || (campo instanceof BigInteger)) {
                                    showInfo = new BigDecimal(campo.toString())
                                            .compareTo(new BigDecimal(campoActS.toString())) == 0;
                                }
                            }
                            if (!showInfo && Objects.nonNull(campo)) {
                                ReflexionEntity.setCampo(objActS, field.getName(), campo);
                            }
                        }
                    } else {
                        // System.out.println("Exclude field: " + field.getName());
                    }
                }
            }
            System.out.println("ENDING COPY DATA FROM "
                    + objActS.getClass().getName().substring(objActS.getClass().getName().lastIndexOf(".") + 1));
            return objActS;
        }
        return null;
    }

}
