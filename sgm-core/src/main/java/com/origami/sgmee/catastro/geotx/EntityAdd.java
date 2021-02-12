/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx;

import com.origami.app.cdi.jpa.hibernate.HibernateAddClassesEvent;
import com.origami.app.cdi.jpa.hibernate.HibernateFactory;
import com.origami.app.cdi.jpa.hibernate.UnitQualifier;
import com.origami.sgmee.catastro.geotx.entity.GeoPrediosDivididos;
import com.origami.sgmee.catastro.geotx.entity.GeoProcesoDivision;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Permite agregar las etities de manera dimanica al hibernate.
 *
 * @author Fernando
 */
@Dependent
public class EntityAdd {

    private static Logger LOG = Logger.getLogger(EntityAdd.class.getName());

    @Inject
    @UnitQualifier("sgm")
    protected HibernateFactory hf;

    public void addEnts(@Observes @UnitQualifier("sgm") HibernateAddClassesEvent evnt) {
        LOG.info(" #### updated  ");
        List<Class> classList = new LinkedList<>();
        // Agregar cada clase anotada:
        classList.add(GeoProcesoDivision.class);
        classList.add(GeoPrediosDivididos.class);

        // pasar la lista
        hf.addEntityClasses(classList);
    }

}
