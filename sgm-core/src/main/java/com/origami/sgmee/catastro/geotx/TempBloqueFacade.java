package com.origami.sgmee.catastro.geotx;

import com.origami.session.UserSession;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.TipoCUD;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

/**
 * Contiene los metodos transacionales para el procesamiento de bloques. table:
 * temp_bloque cols: ( gid, codigo, num, geom )
 *
 * @author Fernando
 */
@Singleton
@Lock(LockType.READ)
@ApplicationScoped
@Interceptors(HibernateEjbInterceptor.class)
public class TempBloqueFacade extends GeoMasterBloqueFacade {

    @Inject
    protected UserSession user;
    @Inject
    protected GeoBloqueFacade bloqFac;

    /**
     * Lista todos los bloques para el codigo catastral dado
     *
     * @param codigo
     * @return bloques data
     */
    public List<BloqueGeoData> listByCodigo(String codigo) {
        SQLQuery sql = sessGis().createSQLQuery(" SELECT gid, blo_cod codigo, num_bloq num, piso, round(CAST(ST_Area(geom) AS numeric), 2) AS area "
                + " FROM " + getTableName() + " WHERE blo_cod = :cod1 AND piso = 1 ORDER BY num_bloq, piso ASC ");
        sql.setString("cod1", codigo);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> bl = sql.list();
        // consultamos los niveles.
        for (BloqueGeoData bloqueGeoData : bl) {
            bloqueGeoData.setNiveles(this.listByCodigoNiveles(codigo, bloqueGeoData.getNum()));
        }
        return bl;
    }

    /**
     * Lista todos los niveles de un bloque
     *
     * @param codigo
     * @return bloques data
     */
    public List<BloqueGeoData> listByCodigoNiveles(String codigo, Short num_bloque) {
        if (codigo == null && num_bloque == null) {
            return null;
        }
        SQLQuery sql = sessGis().createSQLQuery(" SELECT gid, blo_cod codigo, num_bloq num, piso, round(CAST(ST_Area(geom) AS numeric), 2) AS area "
                + " , codigo_ph \"codigoPh\", numeracion"
                + " FROM " + getTableName() + " WHERE blo_cod = :cod1 AND num_bloq = :num ORDER BY num_bloq, piso ASC");
        sql.setString("cod1", codigo);
        sql.setShort("num", num_bloque);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        return sql.list();
    }

    /**
     * Lista todos los niveles de un bloque
     *
     * @param codigo
     * @return bloques data
     */
    public List<BloqueGeoData> listByCodigoNivelesPH(String codigo, Short num_bloque) {
        if (codigo == null && num_bloque == null) {
            return null;
        }
        SQLQuery sql = sessGis().createSQLQuery(" SELECT gid, blo_cod codigo, num_bloq num, piso, round(CAST(ST_Area(geom) AS numeric), 2) AS area "
                + " , codigo_ph \"codigoPh\", numeracion"
                + " FROM " + getTableName() + " WHERE codigo_ph = :cod1 AND num_bloq = :num ORDER BY num_bloq, piso ASC");
        sql.setString("cod1", codigo);
        sql.setShort("num", num_bloque);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        return sql.list();
    }

    public List<BloqueGeoData> detectEliminados(String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT " + getSelectTemplate("bl1") + " ");
        sb.append(" FROM ").append(bloqFac.getTableName()).append(" bl1 ");
        sb.append(" WHERE NOT EXISTS( ")
                .append(" SELECT tmp1.gid FROM ").append(getTableName())
                .append(" tmp1 WHERE tmp1.blo_cod=bl1.blo_cod AND tmp1.num = bl1.num AND tmp1.piso = bl1.piso"
                        + " AND tmp1.piso=4 ");
        sb.append(" ) AND bl1.piso=4 AND bl1.blo_cod = :cod1 AND bl1.habilitado = true ORDER BY bl1.num_bloq, bl1.piso ASC ; ");
        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        sql.setString("cod1", codigo);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        for (BloqueGeoData cb : result) {
            cb.setTipoTx(TipoCUD.DELETE);
        }
        return result;
    }

    /**
     * Detecta los bloques nuevos los que no se encuentran en la tablas
     * geodata.geo_solar son los considerados como nuevos
     *
     * @param codigo Clave catastral
     * @return Lista de {@link BloqueGeoData} con los bloque nuevos.
     */
    public List<BloqueGeoData> detectNuevos(String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT ").append(getSelectTemplate("tmp1")).append(" ");
        sb.append(" FROM ").append(getTableName()).append(" tmp1 ");
        sb.append(" WHERE tmp1.blo_cod = :cod1 AND piso = 4 ");
        sb.append(" AND NOT EXISTS( SELECT bl1.gid FROM ").append(bloqFac.getTableName())
                .append(" bl1 WHERE bl1.blo_cod = tmp1.blo_cod AND bl1.num_bloq = tmp1.num_bloq AND bl1.piso = tmp1.piso AND bl1.habilitado=true) ; ");
        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        sql.setString("cod1", codigo);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        List<BloqueGeoData> resultTemp = new ArrayList<>();
        for (BloqueGeoData cb : result) {
            if (cb.getPiso() == Short.valueOf("4")) {
                cb.setTipoTx(TipoCUD.CREATE);
                cb.setNiveles(this.listByCodigoNiveles(cb.getCodigo(), cb.getNum()));
                resultTemp.add(cb);
            }
        }
        return resultTemp;
    }

    /**
     * Detecta los bloques nuevos en las phs los que no se encuentran en la
     * tablas geodata.geo_solar son los considerados como nuevos
     *
     * @param codigo Clave catastral
     * @return Lista de {@link BloqueGeoData} con los bloque nuevos.
     */
    public List<BloqueGeoData> detectNuevosPH(String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT ").append(getSelectTemplate("tmp1")).append(" ");
        sb.append(" FROM ").append(getTableName()).append(" tmp1 ");
        sb.append(" WHERE tmp1.codigo_ph = :cod1 AND piso = 4 ");
        sb.append(" AND NOT EXISTS( SELECT bl1.gid FROM ").append(bloqFac.getTableName());
        sb.append(" bl1 WHERE bl1.codigo_ph = tmp1.codigo_ph AND bl1.num_bloq = tmp1.num_bloq AND bl1.piso = tmp1.piso AND bl1.habilitado=true) ; ");
        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        sql.setString("cod1", codigo);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        List<BloqueGeoData> resultTemp = new ArrayList<>();
        for (BloqueGeoData cb : result) {
            if (cb.getPiso() == Short.valueOf("4")) {
                cb.setTipoTx(TipoCUD.CREATE);
                cb.setCodigoPh(codigo);
                cb.setNiveles(this.listByCodigoNivelesPH(codigo, cb.getNum()));
                resultTemp.add(cb);
            }
        }
        return resultTemp;
    }

    /**
     * Obtiene todos los bloques dibujados
     *
     * @param codigo
     * @return
     */
    public List<BloqueGeoData> bloquesPredio(String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT " + getSelectTemplate("tmp1") + ", tmp1.codigo_ph \"codigoPh\", numeracion ");
        sb.append(" FROM ").append(getTableName()).append(" tmp1 ");
        sb.append(" WHERE tmp1.blo_cod = :cod1 ORDER BY piso, numeracion");
        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        sql.setString("cod1", codigo);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        List<BloqueGeoData> resultTemp = new ArrayList<>();
        for (BloqueGeoData cb : result) {
            if (cb.getPiso() == Short.valueOf("4") && !resultTemp.contains(cb)) {
                cb.setTipoTx(TipoCUD.CREATE);
                cb.setNiveles(this.listByCodigoNiveles(cb.getCodigo(), cb.getNum()));
                cb.verificarPhs(result);
                resultTemp.add(cb);
            }
        }
        return resultTemp;
    }

    @Override
    public String getTableName() {
        return "geodata.temp_bloque";
    }

    /**
     * Detecta los bloque editados no usados
     *
     * @param codigo Clave catastral
     * @return Lista de {@link BloqueGeoData} con los bloque en editados.
     */
    public List<BloqueGeoData> detectEnModificacion(String codigo) {
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT " + getSelectTemplate("tmp1") + " ");
        sb.append(" FROM " + getTableName() + " tmp1 ");
        sb.append(" WHERE tmp1.blo_cod = :cod1 AND bl1.habilitado = true AND UPPER(external_edit) = UPPER('up')  ; ");
        sb.append(" AND usuario = :usuario");
        SQLQuery sql = sessGis().createSQLQuery(sb.toString());
        sql.setString("cod1", codigo);
        sql.setString("usuario", user.getName_user());
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        for (BloqueGeoData cb : result) {
            cb.setTipoTx(TipoCUD.UPDATE);
            cb.setNiveles(this.listByCodigoNiveles(cb.getCodigo(), cb.getNum()));
        }
        return result;
    }

    /**
     * Lista de pisos en proceso de creacion, actualizacion y eliminacion, en
     * ese orden
     *
     * @param codigo
     * @return Lista de pisos
     */
    public List<BloqueGeoData> detectTxList(String codigo) {
        List<BloqueGeoData> resList = new LinkedList<>();
        resList.addAll(detectNuevos(codigo));
        resList.addAll(detectEnModificacion(codigo));
        resList.addAll(detectEliminados(codigo));
        return resList;
    }

    /**
     * Nombre de la tabla de los de bloques.
     *
     * @return
     */
    public String getTempBloqueLogTable() {
        return "geodata.temp_bloque_log";
    }

    /**
     * Detecta todo los bloques que se hayan modificado por el usuario que esta
     * realizando la tarea.
     *
     * @param codigo
     * @param pgUser
     * @return
     */
    public List<BloqueGeoData> detectEnModificacion(String codigo, String pgUser) {
        SQLQuery sql = sessGis().createSQLQuery("SELECT * FROM geodata.\"getBloquesUpdated\"(:codigo, :usuario)"
                + " c(gid bigint, codigo varchar, num smallint, piso smallint, habilitado boolean,  \"codigoPh\" varchar)"
                + " WHERE gid IS NOT NULL;");
        sql.setString("codigo", codigo);
        sql.setString("usuario", pgUser);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        for (BloqueGeoData cb : result) {
            cb.setTipoTx(TipoCUD.UPDATE);
            cb.setNiveles(this.listByCodigoNiveles(cb.getCodigo(), cb.getNum()));
        }
        return result;
    }

    public List<BloqueGeoData> detectEnModificacion(String codigo, String pgUser, Boolean esPh) {
        SQLQuery sql = sessGis().createSQLQuery("SELECT * FROM geodata.\"getBloquesUpdated\"(:codigo, :usuario)"
                + " c(gid bigint, codigo varchar, num smallint, piso smallint, habilitado boolean,  \"codigoPh\" varchar)"
                + " WHERE gid IS NOT NULL;");
        sql.setString("codigo", codigo);
        sql.setString("usuario", pgUser);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        for (BloqueGeoData cb : result) {
            cb.setTipoTx(TipoCUD.UPDATE);
            if (esPh) {
                cb.setNiveles(this.listByCodigoNivelesPH(codigo, cb.getNum()));
            } else {
                cb.setNiveles(this.listByCodigoNiveles(cb.getCodigo(), cb.getNum()));
            }
        }
        return result;
    }

    public List<BloqueGeoData> detectNuevos(Integer gid) {
        SQLQuery sql = sessGis().createSQLQuery("SELECT  " + getSelectTemplate("pb")
                + "FROM geodata.predios_tx pt "
                + "INNER JOIN geodata.temp_bloque pb ON ST_INTERSECTS(pt.geom, ST_Buffer(pb.geom, -1)) "
                + "WHERE pt.gid = :gid");
        sql.setInteger("gid", gid);
        sql.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        List<BloqueGeoData> result = sql.list();
        for (BloqueGeoData cb : result) {
            cb.setTipoTx(TipoCUD.UPDATE);
            cb.setNiveles(this.listByCodigoNiveles(cb.getCodigo(), cb.getNum()));
        }
        return result;
    }

}
