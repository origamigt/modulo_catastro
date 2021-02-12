/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.cdi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.hibernate.Session;
import org.primefaces.context.RequestContext;

/**
 *
 * @author ANGEL NAVARRO
 */
public class Utils {

    public static boolean isEmpty(Collection l) {
        return l == null || l.isEmpty();
    }

    public static boolean isNotEmpty(Collection l) {
        return !Utils.isEmpty(l);
    }

    public static void messageInfo(String id, String main, String desc) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(id, new FacesMessage(FacesMessage.SEVERITY_INFO, main, desc));
    }

    public static void messageWarning(String id, String main, String desc) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(id, new FacesMessage(FacesMessage.SEVERITY_WARN, main, desc));
    }

    public static void messageError(String id, String main, String desc) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(id, new FacesMessage(FacesMessage.SEVERITY_ERROR, main, desc));
    }

    public static void messageFatal(String id, String main, String desc) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(id, new FacesMessage(FacesMessage.SEVERITY_FATAL, main, desc));
    }

    public static void executeJS(String js) {
        RequestContext rc = RequestContext.getCurrentInstance();
        rc.execute(js);
    }

    public static void update(String target) {
        RequestContext rc = RequestContext.getCurrentInstance();
        rc.update(target);
    }

    /**
     * *
     * Usar asi: FerFaces.redirectFaces("/faces/admin/acl/usuarioEdit.xhtml?id="
     * + usuario.getId() ); dependiendo del uso, reemplazar el
     * requestcontextpath con el sisVar del contexto
     *
     * @param url
     */
    public static void redirectFaces(String url) {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            ec.redirect(ec.getRequestContextPath() + url);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String getContextPath() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        try {
            return ec.getRequestContextPath();
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void redirectNewTab(String url) {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("window.open('" + url + "', '_blank', 'replace=false');");
        FacesContext.getCurrentInstance().renderResponse();
    }

    public static Session getSession(String clazzName) {
        try {
            Class clazz = Class.forName(clazzName);
            if (clazz != null) {
                Method m = clazz.getDeclaredMethod("getSession", (Class) null);
                if (m != null) {
                    return (Session) m.invoke(clazz.newInstance(), (Object[]) null);
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public static List<Field> getFields(Class aClass) {
        try {
            return Arrays.asList(aClass.getDeclaredFields());
        } catch (Exception e) {
            return null;
        }
    }

}
