package com.origami.sgmee.catastro.geotx;

import java.util.LinkedList;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;

import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import util.HiberUtil;
import util.Utils;

/**
 * Clase abstacta que contiene los metodos de procesamiento y consulta sobre la
 * tabla de bloques.
 *
 * @author Angel Navarro
 */
public abstract class GeoMasterBloqueFacade extends GeoServiceMaster {

    public String getTableName() {
        return "geodata.ct_bloque_constructivo";
    }

    public void delete(List<BloqueGeoData> bloques) throws Exception {
        requireTxGis();
        for (BloqueGeoData cb : bloques) {
            SQLQuery sql = sessGis().createSQLQuery(" DELETE FROM " + ptm.getGeoBloque() + " WHERE blo_cod = :codigo AND num_bloq=:num"
                    + " AND piso = 4 ; ");
            sql.setString("codigo", cb.getCodigo());
            sql.setShort("num", cb.getNum());
            sql.executeUpdate();
            HiberUtil.flushAndCommit();
        }
    }

    public void enableBloque(List<BloqueGeoData> bloques) throws Exception {
        requireTxGis();
        for (BloqueGeoData cb : bloques) {
            SQLQuery sql = sessGis().createSQLQuery("UPDATE " + ptm.getGeoBloque() + " SET habilitado = false "
                    + " WHERE blo_cod = :codigo AND num_bloq = :num ; ");
            sql.setString("codigo", cb.getCodigo());
            sql.setShort("num", cb.getNum());
            sql.executeUpdate();
        }
        HiberUtil.flushAndCommit();
    }

    public void deleteTx(List<BloqueGeoData> bloques) throws Exception {
        requireTxGis();
        for (BloqueGeoData cb : bloques) {
            SQLQuery sql = sessGis().createSQLQuery(" DELETE FROM " + ptm.getTransBloque() + " WHERE gid = :gid ; ");
            sql.setBigInteger("gid", cb.getGid());
            sql.executeUpdate();
        }
        HiberUtil.flushAndCommit();
    }

    public void create(List<BloqueGeoData> bloques) {
        requireTxGis();
        if (Utils.isNotEmpty(bloques)) {
            for (BloqueGeoData cb : bloques) {
//            SQLQuery sql = sessGis().createSQLQuery(" INSERT INTO " + ptm.getGeoBloque() + " ( gid, blo_cod, num_bloq, habilitado, piso) "
//                    + " VALUES ( :gid, :codigo , :num , :hab, :piso );");
                SQLQuery sql = sessGis().createSQLQuery("INSERT INTO geodata.ct_bloque_constructivo (gid, blo_cod, num_bloq, geom, habilitado, piso, blo_piso) "
                        + "SELECT gid, blo_cod, num_bloq, geom, true, piso, descripcion_piso "
                        + "FROM geodata.temp_bloque WHERE gid=:gid");
                sql.setParameter("gid", cb.getGid());
//            sql.setParameter("codigo", cb.getCodigo());
//            sql.setParameter("num", cb.getNum());
//            sql.setParameter("piso", cb.getPiso());
//            sql.setParameter("hab", Boolean.TRUE);
                sql.executeUpdate();
            }
        }
    }

    public String getSelectTemplate(String alias) {
        return " " + alias + ".gid AS gid, " + alias + ".blo_cod AS codigo, " + alias + ".habilitado AS habilitado, "
                + alias + ".num_bloq AS num, " + alias + ".piso AS piso, " + "round(CAST(ST_Area(" + alias + ".geom) AS numeric), 2) AS area ";
    }

    public void updateClaveYNum(String codigo, Short num, String codigoNew, Short numNew) {
        requireTxGis();
        StringBuilder sb = new StringBuilder();
        sb.append(" UPDATE " + ptm.getGeoBloque() + " SET blo_cod = :codNew, num_bloq = :numNew WHERE blo_cod = :cod AND num_bloq = :num ;");
        SQLQuery q1 = sessGis().createSQLQuery(sb.toString());
        q1.setString("cod", codigo);
        q1.setShort("num", num);
        q1.setString("codNew", codigoNew);
        q1.setShort("numNew", numNew);
        q1.executeUpdate();
    }

    public void updateClaveYNumPh(String codigo, String codigoNewPh, Short num) {
        requireTxGis();
        StringBuilder sb = new StringBuilder();
        sb.append(" UPDATE " + ptm.getTransBloque() + " SET codigo_ph = :codNew WHERE blo_cod = :cod AND numeracion = :num ;");
        SQLQuery q1 = sessGis().createSQLQuery(sb.toString());
        q1.setString("cod", codigo);
        q1.setShort("num", num);
        q1.setString("codNew", codigoNewPh);
        q1.executeUpdate();
    }

    public List<BloqueGeoData> listBySolar(String codigo) {
        List<BloqueGeoData> resList = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT " + getSelectTemplate("bl1") + " FROM " + getTableName() + " bl1 WHERE bl1.blo_cod = :cod AND bl1.piso = 4 ");
        SQLQuery q1 = sessGis().createSQLQuery(sb.toString());
        q1.setString("cod", codigo);
        q1.setResultTransformer(Transformers.aliasToBean(BloqueGeoData.class));
        resList = q1.list();
        return resList;
    }

}
