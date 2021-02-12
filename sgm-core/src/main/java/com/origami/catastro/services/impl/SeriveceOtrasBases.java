/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastro.services.impl;

import com.origami.catastroextras.models.Radicado;
import com.origami.config.SisVars;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sql.SqlTransaction;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.sql.DataSource;
import util.DataBaseParameters;

/**
 *
 * @author Angel Navarro
 */
// @Alternative
// @Priority(1000)
@Singleton
@ApplicationScoped
@Interceptors(value = {HibernateEjbInterceptor.class})
public class SeriveceOtrasBases {

    private static final Logger LOG = Logger.getLogger(SeriveceOtrasBases.class.getName());

    @Inject
    private DataBaseConectionIB baseConectionQuipux;
    @javax.inject.Inject
    private SqlTransaction transaction;

    public Radicado getDataQuipux(String numTramite) {
        DataBaseParameters dbp = null;
        try {
            dbp = new DataBaseParameters(SisVars.driverClassSgmDocs,
                    SisVars.urlSgmDocs, SisVars.userNameSgmDocs, SisVars.passwordSgmDocs);
        } catch (Exception e) {
            //         DataBaseParameters 
            System.out.println("No se puede conectar a base quipux");
            return new Radicado();
        }
        try {
            DataSource dataSource = this.baseConectionQuipux.getDataSource(dbp);
            List<Object> paramt = new ArrayList<>();
            paramt.add("%".concat(numTramite).concat("%"));
            Radicado find = this.transaction.find(dataSource.getConnection(),
                    "SELECT r.radi_nume_text, r.radi_asunto, radi_fech_ofic FROM radicado r "
                    + "WHERE r.radi_nume_text LIKE ? ORDER BY radi_nume_radi DESC LIMIT 1;",
                    paramt, new Radicado());
            return find;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, numTramite, e);
            return null;
        }
    }

}
