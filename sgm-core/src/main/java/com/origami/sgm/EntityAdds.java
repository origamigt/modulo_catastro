/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm;

import com.origami.app.cdi.jpa.hibernate.HibernateAddClassesEvent;
import com.origami.app.cdi.jpa.hibernate.HibernateFactory;
import com.origami.app.cdi.jpa.hibernate.UnitQualifier;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.catastroextras.entities.ibarra.CiuDireccion;
import com.origami.catastroextras.entities.ibarra.CiuReferenciasPersonal;
import com.origami.catastroextras.entities.ibarra.CiuRelaciones;
import com.origami.catastroextras.entities.ibarra.CiuTelefono;
import com.origami.catastroextras.entities.ibarra.EjeComercial;
import com.origami.catastroextras.entities.ibarra.EjeComercialPredio;
import com.origami.catastroextras.entities.ibarra.FinanPrestamo;
import com.origami.catastroextras.entities.ibarra.FinanSaldoPrestamo;
import com.origami.catastroextras.entities.ibarra.PredioAnio;
import com.origami.catastroextras.entities.ibarra.Restricciones;
import com.origami.catastroextras.entities.ibarra.RestricionPredio;
import com.origami.catastroextras.entities.ibarra.ValoraAfectacion;
import com.origami.catastroextras.entities.ibarra.ValoraMnz;
import com.origami.sgm.entities.CatEscrituraPropietario;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Cid permite agregar las entities al hibernate.
 *
 * @author Angel Navarro
 */
@Dependent
public class EntityAdds {

    private static final Logger LOG = Logger.getLogger(EntityAdds.class.getName());

    @Inject
    @UnitQualifier("sgm")
    protected HibernateFactory hf;

    public void addEnts(@Observes @UnitQualifier("sgm") HibernateAddClassesEvent evnt) {
        LOG.info(" ### Registrando Entities de Modulo Ibarra...");
        List<Class> classList = new LinkedList<>();
        // Agregar cada clase anotada:
        classList.add(CatEscrituraPropietario.class);
        classList.add(EjeComercialPredio.class);
        classList.add(ValoraAfectacion.class);
        classList.add(ValoraMnz.class);
        classList.add(EjeComercial.class);
        // descomentar en caso de integraciòn
        classList.add(CiuCiudadano.class);
        classList.add(CiuDireccion.class);
        classList.add(CiuReferenciasPersonal.class);
        classList.add(CiuRelaciones.class);
        classList.add(CiuTelefono.class);
        classList.add(Restricciones.class);
        classList.add(RestricionPredio.class);
        classList.add(PredioAnio.class);
        classList.add(FinanPrestamo.class);
        classList.add(FinanSaldoPrestamo.class);
        // pasar la lista
        hf.addEntityClasses(classList);
    }

}
