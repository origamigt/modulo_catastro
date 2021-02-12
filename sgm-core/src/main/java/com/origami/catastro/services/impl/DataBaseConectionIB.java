/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastro.services.impl;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.faces.bean.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.sql.DataSource;
import org.apache.commons.dbcp.BasicDataSource;
import util.DataBaseParameters;

/**
 * Configuracion de coneccion hacia la base Quipux.
 *
 * @author Angel Navarro
 */
@Singleton
@ApplicationScoped
@Interceptors(value = {HibernateEjbInterceptor.class})
public class DataBaseConectionIB {

    public DataSource getDataSource(DataBaseParameters dbp) {
        BasicDataSource ds = null;
        try {
            if (dbp != null) {
                ds = new BasicDataSource();
                ds.setUrl(dbp.getUrl());
                ds.setDriverClassName(dbp.getDriverClass());
                ds.setUsername(dbp.getUserName());
                ds.setPassword(dbp.getPassword());
                ds.setMaxIdle(dbp.getMaxIdleTime());
                ds.setMaxActive(dbp.getMaxPoolSize());
                ds.setMinIdle(dbp.getMinPoolSize());
                ds.setDefaultAutoCommit(false);
            }
        } catch (Exception e) {
            Logger.getLogger(DataBaseConectionIB.class.getName()).log(Level.SEVERE, null, e);
        }
        return ds;
    }
}
