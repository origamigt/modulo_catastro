package com.origami.sgmee.catastro.geotx;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.EjeComercialPredio;
import com.origami.catastroextras.entities.ibarra.ValoraAfectacion;
import com.origami.catastroextras.entities.ibarra.ValoraMnz;
import com.origami.session.UserSession;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioClasificRural;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.events.CreacionPredioPost;
import com.origami.sgm.events.DivisionPrediosPost;
import com.origami.sgm.events.EliminacionPredioPost;
import com.origami.sgm.events.FusionPrediosPost;
import com.origami.sgm.geo.CtEjesValor;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.ejbs.ServiceLists;
import com.origami.sgm.services.listener.ListenerEvent;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import com.origami.sgmee.catastro.geotx.model.CtCalidadSueloRuralPredio;
import com.origami.sgmee.catastro.geotx.model.CtManzanas;
import com.origami.sgmee.catastro.geotx.model.CtSectoresValor;
import com.origami.sgmee.catastro.geotx.model.PolygonData;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import util.EntityBeanCopy;
import util.HiberUtil;
import util.JsfUti;
import util.Utils;

/**
 * Contiene los metodos para el procesamiento y validacion del proceso de
 * fracionamiento.
 *
 * @author Fernando
 */
@ApplicationScoped
@Singleton
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class GeoProcesosService extends GeoServiceMaster {

    private static final Logger LOG = Logger.getLogger(GeoProcesosService.class.getName());

    @Inject
    protected GeoConnectionFactory geocf;
    @Inject
    protected PredioTableMetadata predioTM;
    @Inject
    protected GeoProcesoDivisionFacade divfac;
    @Inject
    protected GeoValidations valid;
    @Inject
    protected ServiceDataBaseIb dataBaseIb;
    @Inject
    protected ServiceLists catalogos;
    @Inject
    protected UserSession userSession;
    @Inject
    private ListenerEvent listenerEvt;

    /**
     * Optiene el registro del predio que esta en procesos de division
     *
     * @param codigoPredial Clave catastral del predio a dividir
     * @param tipoPredio
     * @return
     * @throws java.lang.Exception
     */
    public GeoProcesoDivision getProcesoDivisionData(String codigoPredial, String tipoPredio) throws Exception {
        HiberUtil.requireTransaction();
        Query q1 = this.sess()
                .createQuery("SELECT pd FROM GeoProcesoDivision pd WHERE pd.codigo = :codigo AND activo = true");
        q1.setString("codigo", codigoPredial);
        GeoProcesoDivision procDiv = (GeoProcesoDivision) q1.uniqueResult();
        if (procDiv == null) {
            procDiv = this.createProcesoDivision(codigoPredial, tipoPredio);
        } else {
            this.eliminarDivPredios(procDiv);
            procDiv = this.createProcesoDivision(codigoPredial, tipoPredio);
        }

        return procDiv;
    }

    /**
     * Realiza la consulta de todos los poligonos dibujados
     *
     * @param codigoPredial
     * @param tipoPredio
     * @return
     * @throws java.lang.Exception
     */
    protected GeoProcesoDivision createProcesoDivision(String codigoPredial, String tipoPredio) throws Exception {
        List<PolygonData> polygons = this.findDivisionPolygons(codigoPredial);
        if (polygons.size() < 2) {
            JsfUti.messageError(null, "Error", "No se encontraron polígonos divididos.");
            throw new GeoProcesosException("No se encontraron polígonos divididos.");
        }
        if (tipoPredio.equalsIgnoreCase("R")) {
            if (activarCalidadSueloRural) {
                List< CtCalidadSueloRuralPredio> tempClasificacion = findDivisionPolygonsCalidad(codigoPredial);
                if (tempClasificacion.size() <= 0) {
                    JsfUti.messageError(null, "Error", "No existe calidad de suelo para el predio matrix.");
                    throw new GeoProcesosException("No existe calidad de suelo para el predio matrix.");
                }
            }
        }
        Map<Integer, Short> validaNumeracion = new HashMap<>();

        if (polygons.size() > 1) {
            for (PolygonData cp1 : polygons) {
                if (cp1.getArea() == null) {
                    JsfUti.messageError(null, "Error", "Se encuentran poliginos sin areas. Gid " + cp1.getGid() + " sin area.");
                    throw new GeoProcesosException("Se encuentran poliginos sin areas. Gid " + cp1.getGid() + " sin area.");
                }

                if (cp1.getArea().doubleValue() <= 1) {
                    JsfUti.messageError(null, "Error", "Se encuentran poliginos con areas menores a 3 metros cuadrados. Gid: " + cp1.getGid() + ".");
                    throw new GeoProcesosException("Se encuentran poliginos con areas menores a 3 metros cuadrados.");
                }

                if (cp1.getNumeracion() == null) {
                    JsfUti.messageError(null, "Error", "Divididos sin el atributo numeracion.");
                    throw new GeoProcesosException("Divididos sin el atributo numeracion.");
                }
                this.valid.checkPredioPolygonConflictos(cp1.getGid());
                Short get = validaNumeracion.get(cp1.getManzana());
                if (get == null) {
                    validaNumeracion.put(cp1.getManzana(), cp1.getNumeracion());
                } else {
                    if (cp1.getNumeracion().compareTo(get) == 0) {
                        JsfUti.messageError(null, "Error", "Exite dos predios con la misma numeración.");
                        throw new GeoProcesosException("Exite dos predios con la misma numeración.");
                    }
                }
                // Verificamos que existe manzana creada
                String mz = null;
                if ((cp1.getManzana() != null) && (cp1.getManzana() > 0)) {
                    mz = codigoPredial.substring(0, 10) + Utils.completarCadenaConCeros(cp1.getManzana().toString(), 3);
                } else {
                    mz = codigoPredial.substring(0, 13);
                }

                if (tipoPredio.equalsIgnoreCase("U")) {
                    if ((cp1.getManzana() != null) && (cp1.getManzana() > 0)) {
                        if (this.existeManzana(mz) == null) {
                            JsfUti.messageError(null, "Error", "No existe manzana " + cp1.getManzana() + " en la capa " + this.predioTM.getCtManzanas());
                            throw new GeoProcesosException("No existe manzana " + cp1.getManzana() + " en la capa " + this.predioTM.getCtManzanas());
                        }
                    }
                    // VERIFICAMOS QUE EXISTA LA MANZANA NUEVA CREADA 
                    // DENTRO DEL METODOS VERIFICA EL EJE DE VALOS Y LA AFECTACION DEL PREDIO SI TIENEN
                    this.verificarValoraManzana(mz, codigoPredial.subSequence(0, 13).toString());
                } else {
                    if (activarCalidadSueloRural) {
                        this.valid.checkPredioPolygonConflictosCalidad(cp1.getGid());
                        List<CtCalidadSueloRuralPredio> findDivisionPolygonsCalidad = findDivisionPolygonsCalidad(cp1.getGid());
                        // VERIFICAMOS QUE EXISTA CLASE DE SUELO CREADAS PARA EL PREDIO NUEVO
                        if (Utils.isEmpty(findDivisionPolygonsCalidad)) {
                            JsfUti.messageError(null, "Error", "No se encontro calidad de suelo para uno de los predios nuevos con gid " + cp1.getGid());
                            throw new GeoProcesosException("No se encontro calidad de suelo para uno de los predios nuevos con gid " + cp1.getGid());
                        }
                        BigDecimal areaCalidadSuelo = BigDecimal.ZERO;
                        // Sumamos las areas de las clases de suelo 
                        for (CtCalidadSueloRuralPredio ctcspr : findDivisionPolygonsCalidad) {
                            areaCalidadSuelo = areaCalidadSuelo.add(ctcspr.getArea());
                        }
                        // Verificamos que el area del predio sea igual a la suma de las areas de las clase de suelo.
                        if (!(cp1.getArea().setScale(2, RoundingMode.HALF_UP).doubleValue() == areaCalidadSuelo.setScale(2, RoundingMode.HALF_UP).doubleValue())) {
                            if (!(cp1.getArea().setScale(1, RoundingMode.HALF_UP).doubleValue() == areaCalidadSuelo.setScale(1, RoundingMode.HALF_UP).doubleValue())) {
                                JsfUti.messageError(null, "Error", "El area del predio con la suma de las areas de la calidad del suelo no son iguales. Area de predio "
                                        + cp1.getArea().setScale(1, RoundingMode.HALF_UP) + " area de Calidades " + areaCalidadSuelo.setScale(1, RoundingMode.HALF_UP));
                                System.out.println(cp1.getGid() + " Area del predio tx " + cp1.getArea().setScale(2, RoundingMode.HALF_UP) + ", area del clase de suelo " + areaCalidadSuelo.setScale(2, RoundingMode.HALF_UP));
                                throw new GeoProcesosException("El area del predio con la suma de las areas de la calidad del suelo no son iguales ");
                            }
                        }
                    }
                }
            }
        }
        // crear proceso division
        GeoProcesoDivision procDiv = new GeoProcesoDivision();
        procDiv.setCodigo(codigoPredial);
        procDiv.setActivo(Boolean.TRUE);
        this.sess().persist(procDiv);
        this.sess().flush();
        // crear los predios a dividir
        for (PolygonData cp1 : polygons) {
            GeoPrediosDivididos predDiv = new GeoPrediosDivididos();
            predDiv.setGid(cp1.getGid());
            predDiv.setProcesoDiv(procDiv);
            predDiv.setNumeracion(cp1.getNumeracion());
            predDiv.setArea(cp1.getArea());
            // predDiv.setCodigoNuevo(cp1.getManzana() == null ? null :
            // cp1.getManzana().toString());
            if ((cp1.getManzana() != null) && (cp1.getManzana() > 0)) {
                // valid.checkPredioPolygonConflictosInterseccion(codigoPredial,
                // cp1.getNumeracion(), cp1.getManzana());
                predDiv.setCodigoNuevo(cp1.getManzana().toString());
            } else { // SE CREARA O ACREGAR A MANZANA.
                // valid.checkPredioPolygonConflictosInterseccion(codigoPredial,
                // cp1.getNumeracion());
                predDiv.setCodigoNuevo(null);
            }
            this.sess().persist(predDiv);
        }
        this.sess().flush();
        this.sess().refresh(procDiv);
        return procDiv;
    }

    /**
     * Realiza la validacion si existen los clases de suelo, ademas validad el
     * area del predio sea igual al area de la calidad de suelo
     *
     * @param pol Poligono a validar la calidad de suelo
     * @throws GeoProcesosException Excepcion si ocurre algun error
     */
    public void validarClasesSuelo(PolygonData pol) throws GeoProcesosException {
        this.valid.checkPredioPolygonConflictosCalidad(pol.getCodigo());
        List<CtCalidadSueloRuralPredio> findDivisionPolygonsCalidad = findDivisionPolygonsCalidad(pol.getGid());
        // VERIFICAMOS QUE EXISTA CLASE DE SUELO CREADAS PARA EL PREDIO NUEVO
        if (Utils.isEmpty(findDivisionPolygonsCalidad)) {
            JsfUti.messageError(null, "Error", "No se encontro calidad de suelo para uno de los predios nuevos.");
            throw new GeoProcesosException("No se encontro calidad de suelo para uno de los predios nuevos ");
        }
        BigDecimal areaCalidadSuelo = BigDecimal.ZERO;
        // Sumamos las areas de las clases de suelo 
        for (CtCalidadSueloRuralPredio ctcspr : findDivisionPolygonsCalidad) {
            areaCalidadSuelo = areaCalidadSuelo.add(ctcspr.getArea());
            this.valid.checkPredioPolygonConflictosCalidad(ctcspr.getCodigo());
        }
        // Verificamos que el area del predio sea igual a la suma de las areas de las clase de suelo.
        if (!(pol.getArea().setScale(2, RoundingMode.HALF_UP).doubleValue() == areaCalidadSuelo.setScale(2, RoundingMode.HALF_UP).doubleValue())) {
            if (!(pol.getArea().setScale(1, RoundingMode.HALF_UP).doubleValue() == areaCalidadSuelo.setScale(1, RoundingMode.HALF_UP).doubleValue())) {
                JsfUti.messageError(null, "Error", "El area del predio con la suma de las areas de la calidad del suelo no son iguales. Area de predio "
                        + pol.getArea().setScale(1, RoundingMode.HALF_UP) + " area de Calidades " + areaCalidadSuelo.setScale(1, RoundingMode.HALF_UP));
                System.out.println(pol.getGid() + " Area del predio tx " + pol.getArea().setScale(2, RoundingMode.HALF_UP) + ", area del clase de suelo " + areaCalidadSuelo.setScale(2, RoundingMode.HALF_UP));
                throw new GeoProcesosException("El area del predio con la suma de las areas de la calidad del suelo no son iguales ");
            }
        }
    }

    /**
     * Elimina GeoProcesoDivision y el detalle.
     *
     * @param procDiv GeoProcesoDivision a elimnar.
     */
    protected void eliminarDivPredios(GeoProcesoDivision procDiv) {
        this.dataBaseIb.getManager().deleteList(procDiv.getPredios());
        this.dataBaseIb.getManager().delete(procDiv);
    }

    protected List<PolygonData> findDivisionPolygons(String codigoPredial) {
        SQLQuery sql1 = this.sessGis().createSQLQuery(
                "SELECT gid, codigo, numeracion, round(CAST(ST_Area(geom) AS numeric), 4) AS area, manzana" + " FROM "
                + this.predioTM.getTransTableName() + " WHERE codigo = :codigo ORDER BY manzana, numeracion");
        sql1.setString("codigo", codigoPredial);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));
        List<PolygonData> polygons = sql1.list();
        for (PolygonData ip1 : polygons) {
            ip1.setBloques(this.tempFac.localizarBloquesDentro(ip1.getGid()));
        }
        return polygons;
    }

    public PolygonData findNewPolygon(String codigoPredial) {
        try {
            this.valid.checkPredioPolygonConflictos(codigoPredial);
        } catch (Exception e) {
            JsfUti.messageError(null, "Error", "Se encontró más de un polígono con ese código catastral, en tabla pre-transaccional.");
            throw new GeoProcesosException(
                    "Se encontró más de un polígono con ese código catastral, en tabla pre-transaccional.");
        }
        SQLQuery sql1 = this.sessGis().createSQLQuery(
                "SELECT gid, codigo, round(CAST(ST_Area(geom) AS numeric), 4) AS area, habilitado, manzana, tipo"
                + " FROM " + this.predioTM.getTransTableName()
                + " WHERE habilitado = true AND codigo = :codigo ORDER BY gid DESC");
        sql1.setString("codigo", codigoPredial);
        sql1.setMaxResults(1);
        sql1.setResultTransformer(Transformers.aliasToBean(PolygonData.class));

        return (PolygonData) sql1.uniqueResult();
    }

    protected void createProcessTables() {
        HiberUtil.requireTransaction();

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ").append(this.predioTM.getTransTableName()).append(" (");
        sb.append(this.predioTM.getTransTableIdColumn()).append(" bigserial ");
        sb.append(", ").append(this.predioTM.getTransTableProcessColumn()).append(" bigint");
        sb.append(", ").append(this.predioTM.getCodigoColumn()).append(" character varying(50)");
        sb.append(", ").append(this.predioTM.getEnableColumn()).append(" boolean");
        sb.append(", ").append(this.predioTM.getGeomColumn()).append(" geometry(").append(this.predioTM.getGeomType())
                .append(", ").append(this.predioTM.getSrid()).append(")");
        sb.append(" );");

        SQLQuery sql1 = this.sessGis().createSQLQuery(sb.toString());
        sql1.executeUpdate();
    }

    @Deprecated
    public void fusionarPredios(FusionPrediosPost fpevent) {

        this.requireTxGis();
        // Example:
        // INSERT INTO geodata.geo_solar (codigo, geom) SELECT '12345672000',
        // ST_Union(ST_SnapToGrid(the_geom,0.0001)) FROM geodata.predio_2 WHERE
        // cod_predia IN (2045, 2046, 2047);
        StringBuilder sqlb = new StringBuilder("INSERT INTO ").append(this.predioTM.getTablename())
                .append(" (codigo, geom) SELECT ").append("'").append(fpevent.getCodPredioFinal())
                .append("', ST_Multi( ST_Union(ST_SnapToGrid(").append(this.predioTM.getGeomColumn())
                .append(", 0.01)) ) FROM ").append(this.predioTM.getTablename()).append(" WHERE ")
                .append(this.predioTM.getCodigoColumn()).append(" IN (");
        int i1 = 1;
        for (String c1 : fpevent.getCodPrediosFusion()) {
            if (i1 > 1) {
                sqlb.append(", ");
            }
            sqlb.append("'").append(c1).append("'");
            i1++;
        }
        sqlb.append(");");

        SQLQuery q1 = this.sessGis().createSQLQuery(sqlb.toString());
        q1.executeUpdate();

        this.deshabilitarPredios(fpevent.getCodPrediosFusion());
    }

    public void deshabilitarPredio(String codigo) {
        this.requireTxGis();
        StringBuilder sqlb = new StringBuilder("UPDATE ").append(this.predioTM.getTablename()).append(" SET ")
                .append(this.predioTM.getEnableColumn()).append(" = FALSE WHERE codigo = '").append(codigo)
                .append("';");
        this.sessGis().createSQLQuery(sqlb.toString()).executeUpdate();
    }

    public void rehabilitarPredio(String codigo) {
        this.requireTxGis();
        StringBuilder sqlb = new StringBuilder("UPDATE ").append(this.predioTM.getTablename()).append(" SET ")
                .append(this.predioTM.getEnableColumn()).append(" = true WHERE codigo = '").append(codigo).append("';");
        this.sessGis().createSQLQuery(sqlb.toString()).executeUpdate();
    }

    public void deshabilitarPredios(List<String> codigos) {
        this.requireTxGis();
        codigos.stream().forEach((cc) -> {
            this.deshabilitarPredio(cc);
        });
    }

    public void dividirPredio(@Observes DivisionPrediosPost dppevent) {
        if ((dppevent.getCodPredioDividir() == null) || dppevent.getCodPredioDividir().isEmpty()) {
            throw new GeoProcesosException("Codigo de predio vacío o nulo en evento DividionPredioPost.");
        }
        HiberUtil.requireTransaction();
        // Obtenemos el predio que fue dividido junto con las claves resultantes
        GeoProcesoDivision procDiv = this.divfac.getByCodigo(dppevent.getCodPredioDividir());
        // if(procDiv==null) throw new GeoProcesoException("No existe proceso
        // GeoProcesoDivision de division activo.");
        short i = 1;
        Map<Integer, Short> count = new HashMap<>();
        for (Map.Entry<String, BigInteger> entry : dppevent.getCodigos().entrySet()) {
            String c1 = entry.getKey();
            GeoPrediosDivididos findClaveCatPredio = procDiv.findClaveCatPredio(c1);
            if (findClaveCatPredio != null) {
                Integer mz = Integer.valueOf(c1.substring(10, 13));
                i = count.get(mz) == null ? 1 : count.get(mz);
                if (c1.substring(0, 13).equals(dppevent.getCodPredioDividir().substring(0, 13))) {
                    this.asentarPoligonoTx(this.tempFac
                            .getPolygon(dppevent.getCodPredioDividir(), findClaveCatPredio.getNumeracion()).getGid(),
                            c1, entry.getValue(), dppevent.getTipo());
                } else {
                    PolygonData polygonMz = this.tempFac.getPolygonMz(dppevent.getCodPredioDividir(),
                            findClaveCatPredio.getNumeracion(), mz);
                    if (polygonMz != null) {
                        this.asentarPoligonoTx(polygonMz.getGid(), c1, entry.getValue(), dppevent.getTipo());
                    } else {
                        System.out.println("No se encontro clave " + dppevent.getCodPredioDividir() + " Numeracion " + i
                                + " en Mz " + mz);
                    }
                }
                i++;
                count.put(mz, i);
                this.tempFac.asentarBloques_porCodigo(c1);
            } else {
                System.out.println(" KEY " + c1 + " Value " + entry.getValue());
                /**
                 * String sqlNative = "WITH UP AS ( " + " WITH pdX AS ( " + "
                 * WITH XD AS ( " + " SELECT pd.*, p.id predio, ph.clave_cat,
                 * ph.mz, ph.solar, ph.estado " + " FROM
                 * sgm_app.geo_proceso_division pd " + " INNER JOIN
                 * sgm_app.cat_predio p ON pd.codigo = p.clave_cat " + " INNER
                 * JOIN sgm_app.cat_predio ph ON ph.predio_raiz = p.id " + "
                 * WHERE activo = true " + " ORDER BY p.id, ph.mz, ph.solar " +
                 * " ) SELECT *, ROW_NUMBER () OVER (PARTITION BY codigo ORDER
                 * BY solar) " + " FROM XD " + " WHERE clave_cat ~ LEFT(codigo,
                 * 13) AND estado = 'P' " + " ORDER BY predio,
                 * clave_cat::numeric " + " ) SELECT pdx.*, pd.id id_proc_div_p,
                 * pd.codigo_nuevo cod_nuv " + " FROM pdx INNER JOIN
                 * sgm_app.geo_predios_divididos pd " + " ON pd.procesodiv =
                 * pdx.id AND pd.numeracion = pdx.ROW_NUMBER " + " AND
                 * pd.procesodiv = (SELECT id FROM sgm_app.geo_proceso_division
                 * " + " WHERE codigo = :codigoMatriz) " + ") UPDATE
                 * sgm_app.geo_predios_divididos df SET codigo_nuevo =
                 * up.clave_cat " + " FROM UP WHERE up.id_proc_div_p = df.id";
                 * SQLQuery sq1 = this.sessGis().createSQLQuery(sqlNative);
                 * sq1.setString("codigoMatriz",
                 * dppevent.getCodPredioDividir()); sq1.executeUpdate();
                 * this.dividirPredio(dppevent); throw new
                 * GeoProcesosException(sqlNative + " No se encontro codigo en
                 * la tabla GeoPrediosDivididos.");
                 *
                 */
            }
        }
        this.deshabilitarPredio(dppevent.getCodPredioDividir());
        procDiv.setActivo(false);
    }

    public void asentarPoligonoTx(Integer tempGid, String codigo) {
        // ELIMINAR EL PREDIO
        this.requireTxGis();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(this.predioTM.getTablename())
                .append(" (codigo, geom, ficha_alfanumerica) SELECT '").append(codigo)
                .append("', geom, :urlFicha FROM ").append(this.predioTM.getTransTableName()).append(" WHERE gid = ")
                .append(tempGid);
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setString("urlFicha", this.getFichaUrlLocal(codigo));
        sq1.executeUpdate();
        this.syncPoligonoTx(tempGid, codigo);
    }

    public String getFichaUrlLocal(String codigo) {
        // http://172.16.8.125/sgmEE/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml?dswid=-2195
        return this.getUrlLocal() + "/faces/vistaprocesos/catastro/fichaPredial/fichaPredial.xhtml?claveCat=" + codigo;
    }

    protected String getUrlLocal() {
        return "http://172.16.8.125/sgmEE";
    }

    public void syncPoligonoTx(Integer gid, String codigo) {
        StringBuilder sb = new StringBuilder("UPDATE ").append(this.predioTM.getTransTableName())
                .append(" SET codigo = E'").append(codigo).append("', ficha_alfanumerica = :urlFicha ")
                .append(" WHERE gid = ").append(gid);
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setString("urlFicha", this.getFichaUrlLocal(codigo));
        sq1.executeUpdate();
    }

    public PolygonData getNuevoPredioData(String codigoPredial) {
        return this.findNewPolygon(codigoPredial);
    }

    @Deprecated
    public void agregarPredio(CreacionPredioPost creEvt) {
        HiberUtil.requireTransaction();
        // el nuevo predio viene agregado en predio_tx; con el codigo, geom nuevos;
        // habilitado=true
        PolygonData npdata = this.findNewPolygon(creEvt.getCodPredio());
        this.asentarPoligonoTx(npdata.getGid(), creEvt.getCodPredio());
    }

    public void eliminarPredio(EliminacionPredioPost elimEvt) {
        HiberUtil.requireTransaction();
        String cod = elimEvt.getCodPredio();
        this.deshabilitarPredio(cod);
    }

    public void actualizarDataDivision(GeoProcesoDivision division) {
        try {
            HiberUtil.requireTransaction();
            Session sess = HiberUtil.getSession();
            sess.update(division);
            for (GeoPrediosDivididos predio : division.getPredios()) {
                sess.update(predio);
            }
            sess.flush();
        } catch (HibernateException e) {
            Logger.getLogger(GeoProcesosService.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public GeoProcesoDivision getProcesoDivisionData(Long processId) {
        // HiberUtil.requireTransaction();
        Query q1 = this.sess()
                .createQuery("SELECT pd FROM GeoProcesoDivision pd WHERE pd.processId = :processId AND activo = true");
        q1.setBigInteger("processId", BigInteger.valueOf(processId));
        GeoProcesoDivision procDiv = (GeoProcesoDivision) q1.uniqueResult();
        return procDiv;
    }

    public void asentarPoligonoTx(Integer tempGid, String codigo, BigInteger numPredio, String tipo) {
        this.requireTxGis();
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(this.predioTM.getTablename())
                .append(" (codigo, geom, ficha_alfanumerica, nro_predio, tipo) SELECT '").append(codigo)
                .append("', geom, :urlFicha, :nro_predio, :tipo FROM ").append(this.predioTM.getTransTableName())
                .append(" WHERE gid = ").append(tempGid);
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setString("urlFicha", this.getFichaUrlLocal(codigo));
        sq1.setBigInteger("nro_predio", numPredio);
        sq1.setString("tipo", tipo);
        sq1.executeUpdate();
        this.syncPoligonoTx(tempGid, codigo, numPredio, tipo);
    }

    public void syncPoligonoTx(Integer gid, String codigo, BigInteger numPredio, String tipo) {
        StringBuilder sb = new StringBuilder("UPDATE ").append(this.predioTM.getTransTableName())
                .append(" SET codigo = E'").append(codigo).append("', ficha_alfanumerica = :urlFicha ")
                .append(", nro_predio = ").append(numPredio).append(", tipo = '")
                .append(tipo.equalsIgnoreCase("U") ? "URBANO" : "RURAL").append("'").append(" WHERE gid = ")
                .append(gid);
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setString("urlFicha", this.getFichaUrlLocal(codigo));
        sq1.executeUpdate();
    }

    /**
     * Verifica si existe la manzana creada en Ct_manzanas clave de manzana de
     * 13 digitos
     *
     * @param mznCod Codigo de Manzana de 13 digitos
     * @return true si existe caso contrario false
     */
    public CtManzanas existeManzana(String mznCod) throws Exception {
        StringBuilder sb = new StringBuilder(
                "SELECT gid, mzn_cod \"mznCod\", mzn_fte \"mznFte\", txt, mzn_cod_ant \"mznCodAnt\", mzn_area \"mznArea\" ")
                .append(" FROM ").append(this.predioTM.getCtManzanas()).append(" WHERE mzn_cod = :mznCod ");
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setString("mznCod", mznCod);
        sq1.setResultTransformer(Transformers.aliasToBean(CtManzanas.class));
        return (CtManzanas) sq1.uniqueResult();
    }

    /**
     * Busca el codigo de la manzana a que sector pertenece
     *
     * @param mznCod Codigo de Manzana de 13 digitos
     * @return Modelo de datos CtSectoresValor con los datos del sector de valor
     * si existe caso contrario null.
     * @throws java.lang.Exception
     */
    public CtSectoresValor obtenerSectorValor(String mznCod) throws Exception {
        StringBuilder sb = new StringBuilder("SELECT sv.gid, n_zona \"nZona\", "
                + "area_zona \"areaZona\", costo_de_z \"costoDeZ\", total_zona \"totalZona\" ").append(" FROM ")
                .append(this.predioTM.getCtManzanas()).append(" mz INNER JOIN ")
                .append(this.predioTM.getCtSectoresValor())
                .append(" sv ON ST_Intersects(mz.geom, ST_Buffer(sv.geom, -5)) ")
                .append(" WHERE mz.mzn_cod = :mznCod AND ST_Intersects(mz.geom, sv.geom)");
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setString("mznCod", mznCod);
        sq1.setResultTransformer(Transformers.aliasToBean(CtSectoresValor.class));
        return (CtSectoresValor) sq1.uniqueResult();
    }

    /**
     * Busca el codigo de la manzana a que sector pertenece
     *
     * @param mznCod Codigo de Manzana de 13 digitos
     * @return Modelo de datos CtSectoresValor con los datos del sector de valor
     * si existe caso contrario null.
     * @throws java.lang.Exception
     */
    public CtSectoresValor obtenerSectorValor(Integer gidPredio) throws Exception {
        StringBuilder sb = new StringBuilder("SELECT pt.gid, n_zona \"nZona\", "
                + "area_zona \"areaZona\", costo_de_z \"costoDeZ\", total_zona \"totalZona\" ").append(" FROM ")
                .append(this.predioTM.getTransTableName()).append(" pt INNER JOIN ")
                .append(this.predioTM.getCtSectoresValor())
                .append(" cev ON ST_intersects(cev.geom, ST_Centroid(pt.geom)) ")
                .append(" WHERE pt.gid = :gidPredio");
        SQLQuery sq1 = this.sessGis().createSQLQuery(sb.toString());
        sq1.setInteger("gidPredio", gidPredio);
        sq1.setResultTransformer(Transformers.aliasToBean(CtSectoresValor.class));
        return (CtSectoresValor) sq1.uniqueResult();
    }

    /**
     *
     * @param mz Clave de la manzana del predio (3 digitos).
     * @param claveMzMatris Clave de la manzana del predio que se desmanbra (3
     * digitos). Solo para cuando se desmenbra de un predio caso contrario este
     * parametro es nulo.
     * @throws GeoProcesosException
     */
    public void verificarValoraManzana(String mz, String claveMzMatris) throws GeoProcesosException {
        String name = mz.substring(0, 10) + mz.substring(11, 13);
        ValoraMnz valoraMz = this.dataBaseIb.getValoraMzn(name);
        if (valoraMz == null) {
            try {
                CtSectoresValor sectorValor = this.obtenerSectorValor(mz);
                if (sectorValor == null) {
                    throw new GeoProcesosException("No se encontro sector de valor.");
                } else {
                    ValoraMnz valoraMatrix = null;
                    if (!mz.equals(name)) {
                        if (claveMzMatris != null) {
                            valoraMatrix = this.dataBaseIb
                                    .getValoraMzn(claveMzMatris.substring(0, 10) + claveMzMatris.substring(11, 13));
                        }
                    }
                    valoraMz = new ValoraMnz(mz.substring(0, 10) + mz.substring(11, 13), mz.substring(4, 6),
                            mz.substring(6, 8), mz.substring(8, 10), mz.substring(11, 13), BigDecimal.ZERO,
                            Integer.valueOf(sectorValor.getNZona()), sectorValor.getCostoDeZ(), BigDecimal.ZERO);
                    if (valoraMatrix != null) {
                        // ASIGNAMOS EL TOTAL DE MANZANA DE LA CUAL SE DESMENBRO
                        valoraMz.setTotal(valoraMatrix.getTotal());
                        // Calculamos el costo de la zona
                        if (valoraMatrix.getTotal().doubleValue() > sectorValor.getTotalZona().doubleValue()) {
                            valoraMz.setCosto(sectorValor.getCostoDeZ());
                        } else {
                            valoraMz.setCosto(
                                    (valoraMatrix.getTotal().multiply(sectorValor.getCostoDeZ(), MathContext.DECIMAL32))
                                            .divide(sectorValor.getTotalZona(), 2, RoundingMode.HALF_UP));
                        }
                    }
                    // Ponderacion de la manzana
                    this.dataBaseIb.getManager().persist(valoraMz);
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, mz, e);
                JsfUti.messageError(null, "Error.", "No se pudo crear Registro en la tabla valora_mnz " + mz);
                throw new GeoProcesosException("No se pudo crear Registro en la tabla valora_mnz " + mz);
            }
        }
    }

    /**
     * Consulta en la tabla {@link EjeComercialPredio} en cualquier estado.
     * 1>>ACTIVO, 0>> INACTIVO
     *
     * @param predio predio verificar si esta es la {@link EjeComercialPredio}
     * @return Listado de ejes que se encuantra el predio.
     */
    public List<EjeComercialPredio> getEjeComercialPredio(CatPredio predio) {
        Map<String, Object> map = new HashMap<>();
        map.put("codCatastralPredio", predio.getPredialant());
        // map.put("estado", 1);
        return this.dataBaseIb.getManager().findObjectByParameterList(EjeComercialPredio.class, map);
    }

    /**
     * Verifica si la {@code claveMatrixAnt} esta en la tabla
     * {@link EjeComercialPredio} Clona los datos del eje y registra el predio
     * nuevo en eje, para el caso que {@code claveMatrixAnt} entonces solo se
     * hara la consulta espacial, ademas permite verificar si el predio esta en
     * la tabla de {@link ValoraAfectacion}.
     *
     * @param predioMatriz Clave Catastral nueva del predio matriz, para el caso
     * que se nulo se asumira como la creacion de predio nuevo
     * @param predioNuevo Clave Catastral nueva del predio nuevo.
     * @return {@link EjeComercialPredio} para nuevo predio.
     */
    public CtEjesValor verificarEjeValor(CatPredio predioMatriz, CatPredio predioNuevo) {
        if (java.util.Objects.nonNull(predioNuevo)) {
            try {
                List<EjeComercialPredio> ejesPredio = null;
                // Verificamos que exista el registro del predio que se desmembro en la tabla
                // EjeComercialPredio
                // Si la clave es nula se asumira que es la creacion de un predio nuevo.
                if (java.util.Objects.nonNull(predioMatriz)) {
                    ejesPredio = this.getEjeComercialPredio(predioMatriz);
                    this.verificarValoraAfectacion(predioMatriz, predioNuevo, null);
                }
                String sql = "SELECT CAST(cev.gid AS integer), cev.descripcion, cev.valor, cev.codigo, cev.estado, cev.buffer "
                        + "FROM " + this.ptm.getTransTableName() + " pt "
                        + "INNER JOIN geodata.ct_ejes_valor cev ON ST_intersects(St_Buffer(cev.geom, cev.buffer, 'endcap=flat join=round'), pt.geom) "
                        + "WHERE pt.codigo = ? ORDER BY cev.valor DESC LIMIT 1";
                List<CtEjesValor> ejesValor = this.dataBaseIb.getManager().nativeQuery(sql,
                        new Object[]{predioNuevo.getClaveCat()}, CtEjesValor.class);
                // SI EL PREDIO NO ESTA EN LA TABLA EjeComercialPredio VERIFICAMOS REALIZANDO LA
                // CONSULTA ESPACIAL.
                // QUE ESTE FUERA DEL RANDO CASO CONTRARIO LO AGREGAMOS A LA TABLA
                System.out.println("Verificando predio en eje: " + predioNuevo.getClaveCat());
                CtEjesValor cevPredioNuevo = null;
                if (Utils.isEmpty(ejesPredio)) {
                    if (Utils.isNotEmpty(ejesValor)) {
                        System.out.println("##Predio Nuevo en eje: " + ejesValor);
                        for (CtEjesValor cev : ejesValor) {
                            EjeComercialPredio ejeP = new EjeComercialPredio();
                            ejeP.setAfectacionPredio(BigDecimal.ZERO);
                            ejeP.setCodCatastralPredio(predioNuevo.getPredialant());
                            ejeP.setCodigoEjeComercial(cev.getCodigo());
                            ejeP.setEstado(Integer.valueOf(cev.getEstado()));
                            this.dataBaseIb.getManager().persist(ejeP);
                            cevPredioNuevo = cev;
                        }
                        return cevPredioNuevo;
                    }
                } else {
                    if (Utils.isNotEmpty(ejesValor)) {
                        System.out.println("##Copiando eje a predio nuevo... " + predioNuevo.getClaveCat());
                        for (EjeComercialPredio ecp : ejesPredio) {
                            List<EjeComercialPredio> ejeComercialPredio = this.getEjeComercialPredio(predioNuevo);
                            EjeComercialPredio ejePredioNuevo = new EjeComercialPredio();
                            if (Utils.isEmpty((Collection) ejeComercialPredio)) {
                                BeanUtils.copyProperties(ecp, ejePredioNuevo, new String[]{"id"});
                                ejePredioNuevo.setCodCatastralPredio(predioNuevo.getPredialant());
                                this.dataBaseIb.getManager().persist(ejePredioNuevo);
                                cevPredioNuevo = ejesValor.get(0);
                            }
                        }
                        return cevPredioNuevo;
                    }
                }
            } catch (NumberFormatException | BeansException e) {
                LOG.log(Level.SEVERE, "Procesando eje de clave matriz: " + predioMatriz.getClaveCat(), e);
            }
        } else {
            throw new GeoProcesosException(
                    "No se pudo crear Registro en la tabla EjeComercialPredio " + predioMatriz.getClaveCat());
        }
        return null;
    }

    /**
     * Verifica si la {@code claveMatrixAnt} esta en la tabla
     * {@link EjeComercialPredio} Clona los datos del eje y registra el predio
     * nuevo en eje, para el caso que {@code claveMatrixAnt} entonces solo se
     * hara la consulta espacial, ademas permite verificar si el predio esta en
     * la {@link ValoraAfectacion}.
     *
     * @param predioMatriz Clave Catastral nueva del predio matriz, para el caso
     * que se nulo se asumira como la creacion de predio nuevo
     * @param predioNuevo Clave Catastral nueva del predio nuevo.
     * @param gidPredioTx
     * @return {@link EjeComercialPredio} para nuevo predio.
     */
    public CtEjesValor verificarEjeValorGidPredioTx(CatPredio predioMatriz, CatPredio predioNuevo,
            Integer gidPredioTx) {
        if (java.util.Objects.nonNull(gidPredioTx)) {
            try {
                List<EjeComercialPredio> ejesPredio = null;
                // Verificamos que exista el registro del predio que se desmembro en la tabla
                // EjeComercialPredio
                // Si la clave es nula se asumira que es la creacion de un predio nuevo.
                if (java.util.Objects.nonNull(predioMatriz)) {
                    ejesPredio = this.getEjeComercialPredio(predioMatriz);
                }
                this.verificarValoraAfectacion(predioMatriz, predioNuevo, gidPredioTx);
                // VERIFICA SI EL PREDIO ESTA EN EJE 
                String sql = "SELECT CAST(cev.gid AS integer), cev.descripcion, cev.valor, cev.codigo, cev.estado, cev.buffer "
                        + "FROM " + this.ptm.getTransTableName() + " pt "
                        + "INNER JOIN geodata.ct_ejes_valor cev ON ST_intersects(St_Buffer(cev.geom, cev.buffer, 'endcap=flat join=round'), pt.geom) "
                        + "WHERE pt.gid = ? ORDER BY cev.valor DESC LIMIT 1";

                List<CtEjesValor> ejesValor = this.dataBaseIb.getManager().nativeQuery(sql,
                        new Object[]{gidPredioTx}, CtEjesValor.class);
                // SI EL PREDIO NO ESTA EN LA TABLA EjeComercialPredio VERIFICAMOS REALIZANDO LA
                // CONSULTA ESPACIAL.
                // QUE ESTE FUERA DEL RANDO CASO CONTRARIO LO AGREGAMOS A LA TABLA
                System.out.println("Verificando predio en eje: " + predioNuevo.getClaveCat());
                if (Utils.isEmpty(ejesPredio)) {
                    if (Utils.isNotEmpty(ejesValor)) {
                        System.out.println("Predio Nuevo en eje: " + ejesValor);
                        for (CtEjesValor cev : ejesValor) {
                            EjeComercialPredio ejeP = new EjeComercialPredio();
                            ejeP.setAfectacionPredio(BigDecimal.ZERO);
                            ejeP.setCodCatastralPredio(predioNuevo.getPredialant());
                            ejeP.setCodigoEjeComercial(cev.getCodigo());
                            ejeP.setEstado(Integer.valueOf(cev.getEstado()));
                            this.dataBaseIb.getManager().persist(ejeP);
                        }
                    }
                } else {
                    // ESTA DUPLICANDO EL EJE HACER QUE ACTUALIZE EL PREDIO
                    if (Utils.isNotEmpty(ejesValor)) {
                        System.out.println("Copiando eje a predio nuevo... " + predioNuevo.getClaveCat());
                        for (EjeComercialPredio ecp : ejesPredio) {
                            List<EjeComercialPredio> ejeComercialPredio = this.getEjeComercialPredio(predioNuevo);
                            EjeComercialPredio ejePredioNuevo = new EjeComercialPredio();
                            if (Utils.isEmpty((Collection) ejeComercialPredio)) {
                                BeanUtils.copyProperties(ecp, ejePredioNuevo, new String[]{"id"});
                                ejePredioNuevo.setCodCatastralPredio(predioNuevo.getPredialant());
                                this.dataBaseIb.getManager().persist(ejePredioNuevo);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Procesando eje de clave matriz: " + predioMatriz.getClaveCat(), e);
            }
        } else {
            throw new GeoProcesosException(
                    "No se pudo crear Registro en la tabla EjeComercialPredio " + predioMatriz.getClaveCat());
        }
        return null;
    }

    /**
     * Verifica si el {@code predioMatriz} se encuentra en la tabla
     * {@link ValoraAfectacion} si encuentra un registro copia y crea un nuevo
     * registro con los datos del {@code predioNuevo}, caso contrario no realiza
     * ninguna accion.
     *
     * @param predioMatriz {@link CatPredio} de donde esta saliendo el predio
     * nuevo.
     * @param predioNuevo {@link CatPredio} predio nue
     * @param gidPredioTx
     * @param gidPredioTxvo.
     */
    public void verificarValoraAfectacion(CatPredio predioMatriz, CatPredio predioNuevo, Integer gidPredioTx) {
        try {
            System.out.println("Verificando ValoraAfectacion del predio matriz " + predioNuevo.getClaveCat());
            List<ValoraAfectacion> afectacionesPredio = null;
            if (Objects.nonNull(predioMatriz)) {
                Map<String, Object> map = new HashMap<>();
                map.put("codCatastralPredio", predioMatriz.getPredialant());
                // map.put("estado", 1);
                afectacionesPredio = this.dataBaseIb.getManager()
                        .findObjectByParameterList(ValoraAfectacion.class, map);
            }
            // si devuelve registro se realiza la creacion de un nuevo con los datos del
            // predio nuevo
            if (Utils.isNotEmpty(afectacionesPredio)) {
                for (ValoraAfectacion valoraAfectacion : afectacionesPredio) {
                    ValoraAfectacion afectacionPredioNuevo = new ValoraAfectacion();
                    BeanUtils.copyProperties(valoraAfectacion, afectacionPredioNuevo, new String[]{"id"});
                    afectacionPredioNuevo.setId(null);
                    afectacionPredioNuevo.setCodCatastralPredio(predioNuevo.getPredialant());
                    afectacionPredioNuevo.setObservaciones("Registro generado automaticamante por la tarea");
                    this.dataBaseIb.getManager().persist(afectacionPredioNuevo);
                }
            } else {
                if (gidPredioTx != null) {
                    // OBTENEMOS EL SECTOR DE VALOR DONDE ESTA UBICADO EL PREDIO
                    CtSectoresValor sectorValorPredio = obtenerSectorValor(gidPredioTx);
                    // VERIFICAR EN CAPA SECTORES DE VALOR
                    String name = predioNuevo.getClaveCat().substring(0, 10) + predioNuevo.getClaveCat().substring(11, 13);
                    ValoraMnz valoraMz = this.dataBaseIb.getValoraMzn(name);
                    if (sectorValorPredio != null) {
                        // SI EL VALOR DE COSTO DE ZONA ES DIFERENTE AL VALOR DE COSTO DE ZONA DEL REGISTRO DE MANZANA 
                        // CREAMOS EL REGISTRO UN NUEVO REGISTRO EL LA TABLA AFECTACION
                        if (valoraMz.getCostoZona().doubleValue() != sectorValorPredio.getCostoDeZ().doubleValue()) {
                            ValoraAfectacion afectacionPredioNuevo = new ValoraAfectacion();
                            afectacionPredioNuevo.setCodCatastralPredio(predioNuevo.getPredialant());
                            afectacionPredioNuevo.setManzana(name);
                            afectacionPredioNuevo.setObservaciones("Registro generado automaticamante por la tarea");
                            afectacionPredioNuevo.setParroquia(Utils.completarCadenaConCeros(predioNuevo.getParroquia() + "", 2));
                            afectacionPredioNuevo.setZona(Utils.completarCadenaConCeros(predioNuevo.getZona() + "", 2));
                            afectacionPredioNuevo.setSector(Utils.completarCadenaConCeros(predioNuevo.getSector() + "", 2));
                            afectacionPredioNuevo.setManzana(Utils.completarCadenaConCeros(predioNuevo.getMz() + "", 2));
                            afectacionPredioNuevo.setValorCalculado((valoraMz.getTotal().multiply(sectorValorPredio.getCostoDeZ(), MathContext.DECIMAL32))
                                    .divide(sectorValorPredio.getTotalZona(), 2, RoundingMode.HALF_UP));
                            afectacionPredioNuevo.setCosto(valoraMz.getCosto());
                            afectacionPredioNuevo.setAfectacion(afectacionPredioNuevo.getValorCalculado().divide(valoraMz.getCosto(), 6, RoundingMode.HALF_UP));
                            this.dataBaseIb.getManager().persist(afectacionPredioNuevo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Procesando ValoraAfectacion de clave matriz: " + predioNuevo.getClaveCat(), e);
        }
    }

    public void verificarClasificacionsuelo(CatPredio predioNuevo, Integer gid, List<CatPredioClasificRural> catPredioClasificRuralCollection) throws GeoProcesosException {
        CtlgItem defaultUsoSuelo = null;
        if (Utils.isNotEmpty(catPredioClasificRuralCollection)) {
            defaultUsoSuelo = catPredioClasificRuralCollection.get(0).getUsoPredio();
        }
        try {
            List<CtCalidadSueloRuralPredio> calidadesPredio = findDivisionPolygonsCalidad(gid); // 
            if (Utils.isEmpty(calidadesPredio)) {
                throw new GeoProcesosException("No se encontro calidad de suelo para uno de los predi");
            }
            List<CatPredioClasificRural> predioClasificRurals = new ArrayList<>();
            BigDecimal areaCalidades = BigDecimal.ZERO;
            for (CtCalidadSueloRuralPredio calidadPredio : calidadesPredio) {
                if (calidadPredio.getArea() == null) {
                    JsfUti.messageError(null, "Error", "Error al obtner area del poligono con gid: " + calidadPredio.getGid());
                    throw new GeoProcesosException("Error al obtner area del poligono con gid: " + calidadPredio.getGid());
                }
                if (calidadPredio.getArea().doubleValue() == 0) {
                    JsfUti.messageError(null, "Error", "El poligono con gid: " + calidadPredio.getGid() + " retorno area = 0 ");
                    throw new GeoProcesosException("El poligono con gid: " + calidadPredio.getGid() + " retorno area = 0 ");
                }
//                for (CatPredioClasificRural cpr : catPredioClasificRuralCollection) {
//                    if (cpr.getCalidadSuelo().getOrden() == calidadPredio.getCalidadSuelo() && cpr.getSectorHomogeneo().getId() == calidadPredio.getSectorHomogeneo().longValue()) {
//                        System.out.println(" cpr.getCalidadSuelo().getOrden() " + cpr.getCalidadSuelo().getOrden());
//                        System.out.println(" cpr.getSectorHomogeneo().getId() " + cpr.getSectorHomogeneo().getId());
//                        return;
//                    }
//                }
                areaCalidades = areaCalidades.add(calidadPredio.getArea());
                CtlgItem usoSueloTemp = null;
                CtlgItem itemSectorHomogeneo = new CtlgItem(calidadPredio.getSectorHomogeneo().longValue());
                CtlgItem calidadSuelo = (CtlgItem) EntityBeanCopy.clone(this.catalogos.getItemByPadre(calidadPredio.getSectorHomogeneo(), calidadPredio.getCalidadSuelo()));
                if (Utils.isNotEmpty(catPredioClasificRuralCollection)) {
                    for (CatPredioClasificRural pmatriz : catPredioClasificRuralCollection) {
                        if (pmatriz.getSectorHomogeneo().equals(itemSectorHomogeneo)) {
                            if (pmatriz.getCalidadSuelo().equals(calidadSuelo)) {
                                usoSueloTemp = pmatriz.getUsoPredio();
                            }
                        }
                    }
                }
                CatPredioClasificRural clasificRural = new CatPredioClasificRural();
                if (calidadPredio.getCodigoUsoSuelo() == null) {
                    if (usoSueloTemp == null) {
                        clasificRural.setUsoPredio(defaultUsoSuelo);
                    } else {
                        clasificRural.setUsoPredio(usoSueloTemp);
                    }
                } else {
                    clasificRural.setUsoPredio(this.catalogos.getItemByCatalagoOrder("predio.uso_solar", calidadPredio.getCodigoUsoSuelo()));
                }
                clasificRural.setPredio(predioNuevo);
                clasificRural.setUsuario(this.userSession.getName_user());
                clasificRural.setEstado(EstadosPredio.ACTIVO);
                clasificRural.setFecha(new Date());
                clasificRural.setSectorHomogeneo(itemSectorHomogeneo);
                clasificRural.setCalidadSuelo(calidadSuelo);
                clasificRural.setSuperficie(calidadPredio.getArea().divide(BigDecimal.valueOf(10000.00)));

                // AGREGAMOS UNA NUEVA CALIDAD PARA LA PARTE ALFANUMERICA
                BigDecimal valor = listenerEvt.valorTerrenoRural(clasificRural);
                if (valor != null || valor.doubleValue() > 0) {
                    clasificRural.setValorUnitarioHectareaTerreno(valor);
                    clasificRural.setValorTerreno(valor.multiply(clasificRural.getSuperficie()).setScale(4, RoundingMode.HALF_UP));
                }

                clasificRural = (CatPredioClasificRural) this.dataBaseIb.getManager().persist(clasificRural);
                predioClasificRurals.add(clasificRural);
                // ASIGNAMOS LA CLAVE DEL NUEVO PREDIO AL LA CALIDAD DEL SUELO NUEVA
                calidadPredio.setCodigo(predioNuevo.getClaveCat());
                calidadPredio.setNumPredio(predioNuevo.getNumPredio());
                System.out.println("Generando clasificacion de suelo para predio nuevo " + predioNuevo.getNumPredio());
            }
            // Borramos las calidades enteriores 
            if (Utils.isNotEmpty(catPredioClasificRuralCollection)) {
                this.dataBaseIb.getManager().deleteList(catPredioClasificRuralCollection);
            }
            // GUARDAMOS LAS CALIDADES NUEVAS ENCONTRADAS 
            predioNuevo.setCatPredioClasificRuralCollection(predioClasificRurals);
        } catch (GeoProcesosException e) {
            LOG.log(Level.SEVERE, "ct_calidad_suelo_rural_predio " + gid, e);
            throw new GeoProcesosException("No se encontro calidad del suelo para el gid: " + gid);
        }

    }

    public void verificarClasificacionsuelo1(CatPredio predioNuevo, Integer gid, CtlgItem defaultUsoSuelo) throws GeoProcesosException {
        try {
            List<CtCalidadSueloRuralPredio> calidadesPredio = findDivisionPolygonsCalidad(gid); // 
            if (Utils.isEmpty(calidadesPredio)) {
                throw new GeoProcesosException("No se encontro calidad de suelo para uno de los predios nuevos ");
            }
            List<CatPredioClasificRural> predioClasificRurals = new ArrayList<>();
            BigDecimal areaCalidades = BigDecimal.ZERO;
            for (CtCalidadSueloRuralPredio calidadPredio : calidadesPredio) {
                if (calidadPredio.getArea() == null) {
                    JsfUti.messageError(null, "Error", "Error al obtner area del poligono con gid: " + calidadPredio.getGid());
                    throw new GeoProcesosException("Error al obtner area del poligono con gid: " + calidadPredio.getGid());
                }
                if (calidadPredio.getArea().doubleValue() == 0) {
                    JsfUti.messageError(null, "Error", "El poligono con gid: " + calidadPredio.getGid() + " retorno area = 0 ");
                    throw new GeoProcesosException("El poligono con gid: " + calidadPredio.getGid() + " retorno area = 0 ");
                }
                areaCalidades = areaCalidades.add(calidadPredio.getArea());
                CtlgItem itemSectorHomogeneo = new CtlgItem(calidadPredio.getSectorHomogeneo().longValue());
                CtlgItem calidadSuelo = (CtlgItem) EntityBeanCopy.clone(this.catalogos.getItemByPadre(calidadPredio.getSectorHomogeneo(), calidadPredio.getCalidadSuelo()));
                CatPredioClasificRural clasificRural = new CatPredioClasificRural();
                if (calidadPredio.getCodigoUsoSuelo() == null) {
                    clasificRural.setUsoPredio(defaultUsoSuelo);
                } else {
                    clasificRural.setUsoPredio(this.catalogos.getItemByCatalagoOrder("predio.uso_solar", calidadPredio.getCodigoUsoSuelo()));
                }
                clasificRural.setPredio(predioNuevo);
                clasificRural.setUsuario(this.userSession.getName_user());
                clasificRural.setEstado(EstadosPredio.ACTIVO);
                clasificRural.setFecha(new Date());
                clasificRural.setSectorHomogeneo(itemSectorHomogeneo);
                clasificRural.setCalidadSuelo(calidadSuelo);
                clasificRural.setSuperficie(calidadPredio.getArea().divide(BigDecimal.valueOf(10000.00)));

                // AGREGAMOS UNA NUEVA CALIDAD PARA LA PARTE ALFANUMERICA
                BigDecimal valor = listenerEvt.valorTerrenoRural(clasificRural);
                if (valor != null || valor.doubleValue() > 0) {
                    clasificRural.setValorUnitarioHectareaTerreno(valor);
                    clasificRural.setValorTerreno(valor.multiply(clasificRural.getSuperficie()).setScale(4, RoundingMode.HALF_UP));
                }

                clasificRural = (CatPredioClasificRural) this.dataBaseIb.getManager().persist(clasificRural);
                predioClasificRurals.add(clasificRural);
                // ASIGNAMOS LA CLAVE DEL NUEVO PREDIO AL LA CALIDAD DEL SUELO NUEVA
                calidadPredio.setCodigo(predioNuevo.getClaveCat());
                calidadPredio.setNumPredio(predioNuevo.getNumPredio());
                System.out.println("Generando clasificacion de suelo para predio nuevo " + predioNuevo.getNumPredio());
            }
            // GUARDAMOS LAS CALIDADES NUEVAS ENCONTRADAS 
            predioNuevo.setCatPredioClasificRuralCollection(predioClasificRurals);
        } catch (GeoProcesosException e) {
            LOG.log(Level.SEVERE, "ct_calidad_suelo_rural_predio " + gid, e);
            throw new GeoProcesosException("No se encontro calidad del suelo para el gid: " + gid);
        }

    }

    public List<CtCalidadSueloRuralPredio> findDivisionPolygonsCalidad(String claveCat) throws GeoProcesosException {
        this.valid.checkPredioPolygonConflictosCalidad(claveCat);
        List<CtCalidadSueloRuralPredio> calidadesPredio = dataBaseIb.getManager().nativeQuery(QueryGeo.consultarCalidadSueloByClave, new Object[]{claveCat}, CtCalidadSueloRuralPredio.class);
        if (Utils.isEmpty(calidadesPredio)) {
            JsfUti.messageError(null, "Error", "No se encontro calidad de suelo para uno de los predios nuevos.");
            throw new GeoProcesosException("No se encontro calidad de suelo para uno de los predios nuevos ");
        }
        return calidadesPredio;
    }

    public List<CtCalidadSueloRuralPredio> findDivisionPolygonsCalidad(Integer gid) throws GeoProcesosException {
        this.valid.checkPredioPolygonConflictosCalidad(gid);
//        List<CtCalidadSueloRuralPredio> calidadesPredio = dataBaseIb.getManager().nativeQuery(QueryGeo.consultarCalidadSuelo, new Object[]{gid}, CtCalidadSueloRuralPredio.class);
        List<CtCalidadSueloRuralPredio> calidadesPredio = dataBaseIb.getManager().nativeQuery(QueryGeo.intersectionCalidadSuelo, new Object[]{gid}, CtCalidadSueloRuralPredio.class);
        if (Utils.isEmpty(calidadesPredio)) {
            JsfUti.messageError(null, "Error", "No se encontro calidad de suelo para uno de los predios nuevos.");
            throw new GeoProcesosException("No se encontro calidad de suelo para uno de los predios nuevos ");
        }
        return calidadesPredio;
    }

}
