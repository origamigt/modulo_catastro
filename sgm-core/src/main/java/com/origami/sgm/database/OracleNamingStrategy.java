/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.database;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.AssertionFailure;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.util.StringHelper;

/**
 *
 * @author Fernando
 */
public class OracleNamingStrategy extends ImprovedNamingStrategy {

    public static final NamingStrategy INSTANCE = getINSTANCE();
    private static final Logger LOG = Logger.getLogger(OracleNamingStrategy.class.getName());

    public static NamingStrategy getINSTANCE() {
        return new OracleNamingStrategy();
    }

    protected String getDefaultSchema() {
        return SchemasConfigOracle.APP1;
    }

    public OracleNamingStrategy() {
    }

    protected String getPostPunto(String cad) {
        int dotPos = cad.lastIndexOf(".");
        if (dotPos == -1) {
            return cad;
        }
        return cad.substring(dotPos + 1, cad.length() - 1);
    }

    protected String getSchemaName(String fullname) {
        int dotPos = fullname.lastIndexOf(".");
        if (dotPos == -1) {
            return null;
        }
        return fullname.substring(0, dotPos - 1);
    }

    protected String replacePostPunto(String fullname, String cad) {
        int dotPos = fullname.lastIndexOf(".");
        if (dotPos == -1) {
            return cad;
        }
        // ppi = prePuntoIncluido
        String ppi = fullname.substring(0, dotPos);
        return ppi + cad;
    }

    protected String cleanUnderscores(String cad) {
        String res = cad.replace("_", "");
        return res;
    }

//    @Override
//    public String columnName(String columnName) {
//        String postPunto = getPostPunto(columnName);
//        if (postPunto.length() > 30 && SchemasConfig.DB_ENGINE == DatabaseEngine.ORACLE) {
//            // usar primeros 25 y ultimos 5
//            String sub1 = postPunto.substring(0, 24);
//            String sub2 = postPunto.substring(postPunto.length() - 6, postPunto.length() - 1);
//            // New column name
//            String ncn = sub1 + sub2;
//            return replacePostPunto(columnName, ncn);
//        }
//        return super.columnName(columnName); //To change body of generated methods, choose Tools | Templates.
//    }
//    @Override
//    public String tableName(String tableName) {
//        String postPunto = getPostPunto(tableName);
//        //postPunto = getPrefijo(tableName) + cleanUnderscores(postPunto);
//        if (postPunto.length() > 30 && SchemasConfig.DB_ENGINE == DatabaseEngine.ORACLE) {
//            LOG.log(Level.INFO, " ### Tabla: {0}  > Exede 30 caracteres.", postPunto);
//            postPunto = cleanUnderscores(postPunto);
//            if (postPunto.length() <= 30) {
//                return postPunto;
//            } else {
//                // usar primeros 25 y ultimos 5
//                String sub1 = postPunto.substring(0, 24);
//                String sub2 = postPunto.substring(postPunto.length() - 6, postPunto.length() - 1);
//                // New table name
//                String ntn = sub1 + sub2;
//                return ntn;
//            }
//        }
//        return postPunto;
//    }
    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        String header = propertyName != null ? StringHelper.unqualify(propertyName) : propertyTableName;
        if (header == null) {
            throw new AssertionFailure("NamingStrategy not properly filled");
        }
        LOG.log(Level.INFO, "\\\\\\ {0}", header);
        LOG.log(Level.INFO, "//// propertyName {0} propertyEntityName {1} propertyTableName {2} referencedColumnName {3}", new Object[]{propertyName, propertyEntityName, propertyTableName, referencedColumnName});
//        return columnName(header); //+ "_" + referencedColumnName not used for backward compatibility
        return header + "_fkey"; //+ "_" + referencedColumnName not used for backward compatibility
    }

}
