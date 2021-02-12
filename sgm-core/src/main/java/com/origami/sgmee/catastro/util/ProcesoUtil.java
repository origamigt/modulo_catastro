/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.util;

import com.origami.config.SisVars;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.Observaciones;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Clase que contiene los metodos necesarios para todos las tareas.
 *
 * @author Dairon Freddy
 */
public class ProcesoUtil {

    public static String[] getAllFilesRelationsShip(Object source) {

        Set<String> emptyRelationsShip = new HashSet<>();
        emptyRelationsShip.add("catParroquia");
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(OneToMany.class)
                    || field.isAnnotationPresent(Transient.class)) {
                emptyRelationsShip.add(field.getName());
            }
        }
        String[] result = new String[emptyRelationsShip.size()];
        return emptyRelationsShip.toArray(result);
    }

    public static List<Observaciones> getObservacionesDetalle(Collection<Observaciones> observaciones, Long idPredio) {

        List<Observaciones> result = new ArrayList<>();

        for (Observaciones ob : observaciones) {
            if (idPredio != null) {
                if (ob.getIdProceso() != null) {
                    if (ob.getIdProceso().equals(BigInteger.valueOf(idPredio))) {
                        result.add(ob);
                    }
                }
            } else {
                if (ob.getIdProceso() == null) {
                    result.add(ob);
                }
            }
        }
        return result;
    }

    public static List<Observaciones> getObservacionesTask(Collection<Observaciones> observaciones, String taskDefinitionKey) {

        List<Observaciones> result = new ArrayList<>();
        if (taskDefinitionKey != null) {
            for (Observaciones ob : observaciones) {
                if (ob.getTarea() != null) {
                    if (ob.getIdProceso() != null) {

                    } else {
                        if (ob.getTarea().equals(taskDefinitionKey)) {
                            result.add(ob);
                        }
                    }
                }
            }
        }
        return result;
    }

    public static String claveCatastralCompleta(CatPredio predio) {

        String clave = predio.getClaveCat();
        String prov = clave.substring(0, 2);
        if (prov.equals("99")) {
            prov = String.format("%02d", SisVars.PROVINCIA);
        }
        String a = clave.substring(2, clave.length());
        a = prov + a;
        return a;
    }
}
