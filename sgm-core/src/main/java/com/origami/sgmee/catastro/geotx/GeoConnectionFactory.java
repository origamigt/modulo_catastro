/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.app.cdi.jpa.hibernate.HibernateFactory;
import com.origami.app.cdi.jpa.hibernate.UnitQualifier;
import java.sql.Connection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.hibernate.internal.SessionImpl;

/**
 * Permite Obtener la coneccion hacia la base grafica.
 *
 * @author Fernando
 */
@ApplicationScoped
public class GeoConnectionFactory {

    @Inject
    @UnitQualifier("sgm")
    protected HibernateFactory hf;

    public Connection getConnection() {
        // https://stackoverflow.com/questions/3526556/session-connection-deprecated-on-hibernate
        SessionImpl si = (SessionImpl) hf.getFactory().getCurrentSession();
        return si.connection();
    }

}
