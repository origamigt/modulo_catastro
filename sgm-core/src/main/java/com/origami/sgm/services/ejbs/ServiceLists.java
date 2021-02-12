/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.ejbs;

import com.origami.catastroextras.entities.ibarra.EjeComercial;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatCanton;
import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatProvincia;
import com.origami.sgm.entities.CatTipoConjunto;
import com.origami.sgm.entities.CtlgCatalogo;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import util.EntityBeanCopy;

/**
 * Ejb para realizar consulta solo a los diferentes catalogos o lista que sean
 * de uso frecuente.
 *
 * @author Angel Navarro
 */
@Named
@Singleton
@Interceptors(value = {HibernateEjbInterceptor.class})
@ApplicationScoped
public class ServiceLists {

    private static final Logger LOG = Logger.getLogger(ServiceLists.class.getName());

    @javax.inject.Inject
    private Entitymanager manager;
    @javax.inject.Inject
    private CatastroServices catas;

    //**********************************//
    //********Listas de Catastro********//
    //**********************************//
    public List<CatCiudadela> getCiudadelas() {
        return (List<CatCiudadela>) EntityBeanCopy.clone(manager.findAllObjectOrder(CatCiudadela.class, new String[]{"nombre"}, Boolean.TRUE));
    }

    public List<CatParroquia> getParroquiasRurales() {
        return (List<CatParroquia>) manager.findAllEntCopy(Querys.parroquiasRurales);
    }

    // Exoneraciones, Descuentos y Recargos
    /**
     *
     * @return Retorna todos los FnExoneracionClase sin proxis
     */
    /**
     * Si el prefijo es nulo devualve todos los RenTipoValor
     *
     * @param prefijo
     * @return
     */
    public List<CtlgItem> getListado(String argumento) {
        if (argumento == null) {
            return null;
        }
        return (List<CtlgItem>) manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{argumento});
    }

    /**
     * Si el prefijo es nulo devualve todos los RenTipoValor
     *
     * @param prefijo
     * @return
     */
    public List<CtlgItem> getCtlgItem(String prefijo) {
        if (prefijo == null) {
            return null;
        }
        return (List<CtlgItem>) manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{prefijo});
    }

    public List<CtlgItem> getListado(CtlgItem padre) {
        if (padre == null) {
            return null;
        }
        return (List<CtlgItem>) manager.findAllEntCopy(Querys.getCtlgItemByParent,
                new String[]{"padre"}, new Object[]{BigInteger.valueOf(padre.getId())});
    }

    public CtlgItem listadoItemsCultivos(CtlgItem tipo) {
        if (tipo.getHijo() != null) {
            return (CtlgItem) EntityBeanCopy.clone(manager.find(Querys.getCtlgItemaByCultivosHijos, new String[]{"hijo"}, new Object[]{tipo.getHijo().longValue()}));
        }
        return null;
    }

    public List<CatCanton> getCantones() {
        return manager.findAllEntCopy(CatCanton.class);
    }

    public List<CatProvincia> getProvincias() {
        return manager.findAllEntCopy(CatProvincia.class);
    }

    public CatProvincia obtenerCatProvinciaxCodNac(Integer codnac) {
        if (codnac == null) {
            return null;
        }
        CatProvincia provincia = (CatProvincia) EntityBeanCopy.clone(manager.find(Querys.getCatProvinciaxCodNac, new String[]{"codNac"}, new Object[]{codnac.shortValue()}));
        return provincia;
    }

    public List<CatParroquia> getParroquiasxCanton(CatCanton canton) {
        if (canton == null) {
            return manager.findAllEntCopy(CatParroquia.class);
        } else {
            Map<String, Object> mp = new HashMap<>();
            mp.put("idCanton", canton);
            return (List<CatParroquia>) EntityBeanCopy.clone(manager.findObjectByParameterList(CatParroquia.class, mp));
        }
    }

    public CatCanton obtenerCatCatonxCodNac(String codnac) {
        CatCanton canton = (CatCanton) EntityBeanCopy.clone(manager.find(Querys.getCatCatonxCodNac, new String[]{"codNac"}, new Object[]{Short.valueOf(codnac)}));
        return canton;
    }

    public List<CatCanton> getCantones(CatProvincia prov) {
        if (prov == null) {
            return getCantones();
        } else {
            Map<String, Object> mp = new HashMap<>();
            mp.put("idProvincia", prov);
            return (List<CatCanton>) EntityBeanCopy.clone(manager.findObjectByParameterList(CatCanton.class, mp));
        }

    }

    public List<CatTipoConjunto> getTiposConjunto() {
        return manager.findAllEntCopy(CatTipoConjunto.class);
    }

    public List<CatCiudadela> getCiudadelas(CatTipoConjunto tipoConjunto) {
        try {
            if (tipoConjunto != null) {
                return catas.getCiudadelasByTipoConjunto(tipoConjunto);
            } else {
                return catas.getCiudadelasByTipoConjunto(null);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "getCiudadelas", e);
        }
        return null;
    }

    public List<EjeComercial> getCatalogoEjes() {
        return this.manager.findObjectByParameterOrderList(EjeComercial.class, null, new String[]{"codigo"}, Boolean.TRUE);
    }

    public CtlgItem getItemByPadre(BigInteger padre, Integer orden) {
        Map<String, Object> map = new HashMap<>();
        map.put("padre", padre);
        map.put("orden", orden);
        return manager.findObjectByParameter(CtlgItem.class, map);
    }

    public CtlgItem getItemByCatalagoOrder(String prediobloquerevestpiso, Integer orden) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("orden", orden);
            paramt.put("catalogo", this.getCatalogoNombre(prediobloquerevestpiso));
            return this.manager.findObjectByParameter(CtlgItem.class, paramt);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    /**
     * Trae el listadao de item que tenga el catalogo.
     *
     * @param argumento Nombre del catalogo.
     * @return
     */
    public CtlgCatalogo getCatalogoNombre(String argumento) {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("nombre", argumento);
        return this.manager.findObjectByParameter(CtlgCatalogo.class, paramt);
    }

    /**
     * Obtiene todos los Departamentos que contenga la tabla GeDepartamento.
     *
     * @return Lista de GeDepartamento.
     */
    public List<GeDepartamento> getDepartamentos() {
        return manager.findAllObjectOrder(GeDepartamento.class, new String[]{"nombre"}, Boolean.TRUE);
    }

    public CatEnte getCatEnteByParemt(Map paramt) {
        return (CatEnte) manager.findObjectByParameter(CatEnte.class, paramt);
    }

}
