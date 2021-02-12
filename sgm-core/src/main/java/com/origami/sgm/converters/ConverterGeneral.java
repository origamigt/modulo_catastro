/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.converters;

import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.sgm.util.EjbsCaller;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Angel Navarro
 */
@FacesConverter("entityConverter")
public class ConverterGeneral implements Converter, Serializable {

    // gob.origami.ent.AclUser:20
    // split :
    @Override
    public Object getAsObject(FacesContext context, UIComponent c, String value) {
        if (value == null || value.isEmpty() == true || value.equals("")) {
            return null;
        }
        try {
//            System.out.println(value);
            String[] p = value.split(":");
            if (p == null || p.length < 3) {
                return null;
            }
            Object find = EjbsCaller.getTransactionManager().find(Class.forName(p[0]), ReflexionEntity.instanceConsString(p[2], p[1]));
//            System.out.println(find);
            return find;
        } catch (NullPointerException | ELException | NumberFormatException e) {
            Logger.getLogger(ConverterGeneral.class.getName()).log(Level.SEVERE, value, e);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConverterGeneral.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent c, Object value) {
        if (value == null) {
            return null;
        }
        // gob.origami.ent.AclUser+ : + id
        // gob.origami.ent.AclUser:20:java.lang.Long
        try {
            return ReflexionEntity.getIdEntity(value);
        } catch (Exception ex) {
            Logger.getLogger(ConverterGeneral.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
