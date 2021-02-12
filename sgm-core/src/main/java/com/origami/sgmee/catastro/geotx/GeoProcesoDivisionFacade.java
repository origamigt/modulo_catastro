/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import org.hibernate.HibernateException;
import org.hibernate.Query;

/**
 * Permite realizar la verificacion en la tabla {@link GeoProcesoDivision} que
 * es para el proceso de fraccionamiento, en la cual de almacenan el
 * identificador de cada poligono nuevo.
 *
 * @author Fernando
 */
@Singleton
@ApplicationScoped
@Lock(LockType.READ)
@Interceptors(HibernateEjbInterceptor.class)
public class GeoProcesoDivisionFacade extends GeoServiceMaster {

    private static final Logger LOG = Logger.getLogger(GeoProcesoDivisionFacade.class.getName());

    public GeoProcesoDivision getByCodigo(String codigoPredial) {
        try {
            Query q1 = sess().createQuery("SELECT pd1 FROM GeoProcesoDivision pd1 WHERE pd1.codigo = :codigo AND pd1.activo = true");
            q1.setString("codigo", codigoPredial);
            return (GeoProcesoDivision) q1.uniqueResult();
        } catch (HibernateException hibernateException) {
            LOG.log(Level.SEVERE, codigoPredial, hibernateException);
        }
        return null;
    }

}
