/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sql;

import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;

/**
 * Metodos para la insercion o consultas usando jdbc
 *
 * @author Angel Navarro
 */
@Stateless
@Interceptors(value = {HibernateEjbInterceptor.class})
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class SqlTransaction {

    private static final Logger LOG = Logger.getLogger(SqlTransaction.class.getName());

    /**
     * Returna el primary key de la tabla
     *
     * @param c Connection sql
     * @param sql Sentemcia de insercion
     * @param paramt Parametros
     * @return id de la tabla.
     * @throws java.sql.SQLException
     */
    public Long insertInto(Connection c, String sql, List<Object> paramt) throws SQLException {
        Long x = null;
        try {

            if (c != null) {
                c.setAutoCommit(false);
                PreparedStatement ps = c.prepareStatement(sql);
                int countParamt = 1;
                for (Object object : paramt) {
                    ps.setObject(countParamt, object);
                    countParamt++;
                }
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    x = rs.getLong(1);
                }
//                ps.getConnection().commit();
                c.commit();
                ps.close();
                c.close();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        } finally {
            c.close();
        }
        return x;
    }

    /**
     * Realiza la consulta a la base de datos con la conecion dada y ejecuta la
     * sentencia sql
     *
     * @param c Coneccion
     * @param sql Sentencia de consulta
     * @param paramt Lista de parametros para consulta
     * @return Retorna un objecto con los datos de consulta.
     * @throws SQLException
     */
    public Object find(Connection c, String sql, List<Object> paramt) throws SQLException {
        Object ob = null;
        try {
            if (c != null) {
                PreparedStatement ps = c.prepareCall(sql);
                int countParamt = 1;
                for (Object object : paramt) {
                    ps.setObject(countParamt, object);
                    countParamt++;
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ob = rs.getObject(1);
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        } finally {
            c.close();
        }
        return ob;
    }

    /**
     * REaliza la consulta a con la conecion dada y transforma el resultado a un
     * modelo de datos
     *
     * @param <T>
     * @param c Conecion
     * @param sql Sentencia de consulta sql
     * @param paramt parametros de la consulta
     * @param objectTranform Objeto a transformar el resultado.
     * @return Objecto con el resultado de la consulta
     * @throws SQLException
     */
    public <T> T find(Connection c, String sql, List<Object> paramt, T objectTranform) throws SQLException {
        T ob = objectTranform;
        try {
            if (c != null) {
                PreparedStatement ps = c.prepareCall(sql);
                int countParamt = 1;
                for (Object object : paramt) {
                    ps.setObject(countParamt, object);
                    countParamt++;
                }
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        ReflexionEntity.setCampo(objectTranform,
                                transformUpperCase(metaData.getColumnName(i).toLowerCase()),
                                rs.getObject(i));
                    }
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        } finally {
            c.close();
        }
        return ob;
    }

    /**
     * Realiza la insercion en batch
     *
     * @param c Conexion
     * @param sql Sentencia sgl de insecion
     * @param paramt parametros a procesar en batch
     * @return true si se ejecuto correctamente caso contrario false.
     */
    public Boolean insertIntoBacth(Connection c, String sql, List<List<Object>> paramt) {
        Boolean x = null;
        try {
            if (c != null) {
                c.setAutoCommit(false);
                PreparedStatement ps = c.prepareStatement(sql);
                for (List<Object> lo : paramt) {
                    int countParamt = 1;
                    for (Object object : lo) {
                        ps.setObject(countParamt, object);
                        countParamt++;
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
                ps.close();
            }
        } catch (SQLException e) {
            try {
                LOG.log(Level.SEVERE, null, e);
                c.rollback();
            } catch (SQLException ex) {
                Logger.getLogger(SqlTransaction.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                c.close();
            } catch (SQLException ex) {
                Logger.getLogger(SqlTransaction.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return x;
    }

    /**
     * TRANSFORMA LA PRIMERA LETRA EN MAYUSCULA
     *
     * @param text Texto a procesar.
     * @return Texto con la primera letra en mayucula y despues de cada _ la
     * pasa a mayucula
     */
    private static String transformUpperCase(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        String[] aux = text.split("_");
        String result = "";
        int count = 0;
        for (String string : aux) {
            if (count > 0) {
                result += string.substring(0, 1).toUpperCase().concat(string.substring(1));
            } else {
                result = string;
            }
            count++;
        }
        return result;
    }

    public List findList(Connection c, String sql, List<Object> paramt, Object objectTranform) throws SQLException {
        List list = null;
        try {
            if (c != null) {
                PreparedStatement ps = c.prepareCall(sql);
                int countParamt = 1;
                for (Object object : paramt) {
                    ps.setObject(countParamt, object);
                    countParamt++;
                }
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Object ob = objectTranform.getClass().newInstance();
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    ResultSetMetaData metaData = rs.getMetaData();
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        ReflexionEntity.setCampo(ob,
                                transformUpperCase(metaData.getColumnName(i).toLowerCase()),
                                rs.getObject(i));
                    }
                    list.add(ob);
                }
                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, null, e);
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(SqlTransaction.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            c.close();
        }
        return list;
    }

}
