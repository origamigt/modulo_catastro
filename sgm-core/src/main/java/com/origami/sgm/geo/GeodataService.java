/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.geo;

import com.origami.config.SisVars;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.interceptor.Interceptors;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import util.HiberUtil;

/**
 *
 * @author Fernando
 */
@Singleton
@Lock(LockType.READ)
@Interceptors(value = {HibernateEjbInterceptor.class})
public class GeodataService {

    private static final Logger LOG = Logger.getLogger(GeodataService.class.getName());
    protected static final Integer ENVELOPE_EXTENDED = 30;
    protected static final Integer ENVELOPE_COLINDANTES = 50;

    protected Session getSession() {
        return HiberUtil.getSession();
    }

    @Resource
    private String geoserverUrl;

    @javax.inject.Inject
    protected GeodataIdentifiers geoIdent;
    @javax.inject.Inject
    protected GeoserverIdentifiers geoserverIdnt;

    protected Boolean existSchema() {
        Session sess = getSession();
        SQLQuery schemaExist = sess.createSQLQuery(
                "SELECT count(*) FROM pg_namespace pn INNER JOIN pg_tables pt ON pn.nspname = pt.schemaname WHERE nspname = :schema AND pt.tablename=:tabla");
        schemaExist.setString("schema", geoIdent.getGeodataSchema());
        schemaExist.setString("tabla", geoIdent.getTblGeoPredio());
        Object result = schemaExist.uniqueResult();
        if (result != null) {
            Boolean name = Integer.valueOf(result.toString()) != 0;
            return name;
        } else {
            return false;
        }
    }

    public Bbox getBboxByClaveCatastral(String clave) {
        Bbox bbox1 = null;
        try {
            Session sess = getSession();
            if (existSchema()) {
                SQLQuery sq1 = sess.createSQLQuery("SELECT ST_XMax(ST_Expand(ST_Envelope(gp1.geom),20)) AS xmax, ST_XMin(ST_Expand(ST_Envelope(gp1.geom),20)) AS xmin,"
                        + "ST_YMax(ST_Expand(ST_Envelope(gp1.geom),20)) AS ymax, ST_YMin(ST_Expand(ST_Envelope(gp1.geom),20)) AS ymin "
                        + "FROM " + geoIdent.getGeoPredio() + " gp1 "
                        + "WHERE gp1.codigo = :clave AND gp1.habilitado=true");
                sq1.setResultTransformer(Transformers.aliasToBean(Bbox.class));
                sq1.setString("clave", clave);
                sq1.setMaxResults(1);
                bbox1 = (Bbox) sq1.uniqueResult();
            } else {
                bbox1 = null;
            }

        } catch (HibernateException hibernateException) {
            LOG.log(Level.SEVERE, "Clave: " + clave, hibernateException);
        }
        return bbox1;
    }

    public Bbox getBboxColindantesByClaveCatastral(String clave) {
        Bbox bbox1 = null;
        try {
            Session sess = getSession();
            if (existSchema()) {
                SQLQuery sq1 = sess.createSQLQuery("SELECT ST_XMax(ST_Expand(ST_Envelope(xtbbox.geom)," + GeodataService.ENVELOPE_COLINDANTES + ")) AS xmax, "
                        + "ST_XMin(ST_Expand(ST_Envelope(xtbbox.geom)," + GeodataService.ENVELOPE_COLINDANTES + ")) AS xmin,"
                        + "ST_YMax(ST_Expand(ST_Envelope(xtbbox.geom)," + GeodataService.ENVELOPE_COLINDANTES + ")) AS ymax, "
                        + "ST_YMin(ST_Expand(ST_Envelope(xtbbox.geom)," + GeodataService.ENVELOPE_COLINDANTES + ")) AS ymin "
                        + "FROM " + geoIdent.getGeoPredio() + " xtbbox WHERE xtbbox.codigo = :clave"
                        + " AND xtbbox.habilitado = true LIMIT 1; ");
                sq1.setResultTransformer(Transformers.aliasToBean(Bbox.class));
                sq1.setString("clave", clave);
                bbox1 = (Bbox) sq1.uniqueResult();
            } else {
                bbox1 = null;
            }
        } catch (HibernateException hibernateException) {
            LOG.log(Level.SEVERE, hibernateException.getMessage(), hibernateException);
        }
        return bbox1;
    }

    public Bbox getBboxExtendedByClaveCatastral(String clave) {
        Bbox bbox1 = null;
        try {
            Session sess = getSession();
            if (existSchema()) {
                SQLQuery sq1 = sess.createSQLQuery("SELECT ST_XMax(ST_Expand(ST_Envelope(gp1.geom)," + GeodataService.ENVELOPE_EXTENDED + ")) AS xmax, "
                        + "ST_XMin(ST_Expand(ST_Envelope(gp1.geom)," + GeodataService.ENVELOPE_EXTENDED + ")) AS xmin,"
                        + "ST_YMax(ST_Expand(ST_Envelope(gp1.geom)," + GeodataService.ENVELOPE_EXTENDED + ")) AS ymax, "
                        + "ST_YMin(ST_Expand(ST_Envelope(gp1.geom)," + GeodataService.ENVELOPE_EXTENDED + ")) AS ymin "
                        + "FROM " + geoIdent.getGeoPredio() + " gp1 "
                        + "WHERE gp1.codigo = :clave AND gp1.habilitado=true");
                sq1.setResultTransformer(Transformers.aliasToBean(Bbox.class));
                sq1.setString("clave", clave);
                sq1.setMaxResults(1);
                bbox1 = (Bbox) sq1.uniqueResult();
            } else {
                bbox1 = null;
            }
        } catch (HibernateException hibernateException) {
            LOG.log(Level.SEVERE, hibernateException.getMessage(), hibernateException);
        }
        return bbox1;
    }

    public String getUrlColindantesImage(String clave, Integer ancho, Integer alto) {
        if (clave == null) {
            return "/css/homeIconsImages/reselladoPlanos.png";
        }
        Bbox bbox1 = null;
        try {
            bbox1 = getBboxColindantesByClaveCatastral(clave);
        } catch (Exception ex) {
            //Logger.getLogger(GeodataService.class.getName()).log(Level.SEVERE, null, ex);
            return "/css/homeIconsImages/reselladoPlanos.png";
        }
        if (bbox1 == null) {
            return "/css/homeIconsImages/reselladoPlanos.png";
        }
        StringBuilder urlBuilder = new StringBuilder(getGeoserverUrl())
                .append("catastro/wms?service=WMS&version=1.1.0&request=GetMap&layers=")
                .append(geoserverIdnt.concatNs(geoserverIdnt.getColindanteLayer()))
                .append("&styles=&bbox=")
                .append(bbox1.getXmin()).append(",")
                .append(bbox1.getYmin()).append(",").append(bbox1.getXmax()).append(",")
                .append(bbox1.getYmax())
                .append("&width=650")
                .append("&height=550")
                .append("&srs=EPSG:32717&format=image%2Fpng&env=clave:")
                .append(clave)
                .append(";clave_colindantes:")
                .append(getClavesColindantes(clave));
        String result = urlBuilder.toString();
        return result;
    }

    /**
     * Genera la url de la imagen del predio
     *
     * @param clave
     * @param ancho
     * @param alto
     * @return
     */
    public String getUrlPredioImage(String clave, Integer ancho, Integer alto) {
        if (clave == null) {
            return "/css/homeIconsImages/reselladoPlanos.png";
        }

        Bbox bbox1 = null;
        try {
            bbox1 = getBboxByClaveCatastral(clave);
        } catch (Exception ex) {
            Logger.getLogger(GeodataService.class.getName()).log(Level.SEVERE, null, ex);
            return "/css/homeIconsImages/reselladoPlanos.png";
        }
        if (bbox1 == null) {
            return "/css/homeIconsImages/reselladoPlanos.png";
        }

        StringBuilder urlBuilder = new StringBuilder(getGeoserverUrl())
                .append("catastro/wms?service=WMS&version=1.1.0&request=GetMap&layers=")
                .append(geoserverIdnt.concatNs(geoserverIdnt.getPredioSeleccionadoLayer()))
                .append("&styles=&bbox=")
                .append(bbox1.getXmin()).append(",")
                .append(bbox1.getYmin()).append(",").append(bbox1.getXmax()).append(",")
                .append(bbox1.getYmax())
                .append("&width=750")
                .append("&height=650")
                .append("&srs=EPSG:32717&format=image%2Fpng&env=clave:")
                .append(clave);
        String result = urlBuilder.toString();
        return result;
    }

    /**
     * Genera la url de la imagen del predio ph hija
     *
     * @param clavePredioRaiz
     * @param clavePhHija
     * @return
     */
    public String getUrlPredioImagePH(String clavePredioRaiz, String clavePhHija) {
        if (clavePredioRaiz == null) {
            return "/css/homeIconsImages/reselladoPlanos.png";
        }

        Bbox bbox1 = null;
        try {
            bbox1 = getBboxByClaveCatastral(clavePredioRaiz);
        } catch (Exception ex) {
            Logger.getLogger(GeodataService.class.getName()).log(Level.SEVERE, null, ex);
            return "/css/homeIconsImages/reselladoPlanos.png";
        }
        if (bbox1 == null) {
            return "/css/homeIconsImages/reselladoPlanos.png";
        }

        StringBuilder urlBuilder = new StringBuilder(getGeoserverUrl())
                .append("catastro/wms?service=WMS&version=1.1.0&request=GetMap&layers=")
                .append(geoserverIdnt.concatNs(geoserverIdnt.getPredioSeleccionadoLayerPh()))
                .append("&styles=&bbox=")
                .append(bbox1.getXmin()).append(",")
                .append(bbox1.getYmin()).append(",").append(bbox1.getXmax()).append(",")
                .append(bbox1.getYmax())
                .append("&width=750")
                .append("&height=650")
                .append("&srs=EPSG:32717&format=image%2Fpng&env=clave:")
                .append(clavePredioRaiz)
                .append(";clavePh:")
                .append(clavePhHija);
        String result = urlBuilder.toString();
        return result;
    }

    public String getGeoserverUrl() {
        return geoserverUrl;
    }

    public String getClavesColindantes(String clave) {
        String claves = null;
        try {
            Session sess = getSession();
            if (existSchema()) {
                SQLQuery sq1 = sess.createSQLQuery("SELECT string_agg(gp1.codigo, ',') AS claves " + "FROM "
                        + geoIdent.getGeoPredio() + " gp1, " + geoIdent.getGeoPredio() + " gp2 "
                        + "WHERE ST_INTERSECTS(gp1.geom, gp2.geom) AND gp1.codigo = :clave "
                        + "AND gp1.habilitado=true AND gp2.habilitado=true AND gp1.codigo <> gp2.codigo");
                sq1.setString("clave", clave);
                claves = (String) sq1.uniqueResult();
            } else {
                claves = null;
            }
        } catch (HibernateException hibernateException) {
            LOG.log(Level.SEVERE, "Clave: " + clave, hibernateException);
            return "";
        }
        return claves;
    }

}
