/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.ejbs;

import com.origami.catastroextras.entities.ibarra.EjeComercial;
import com.origami.config.SisVars;
import com.origami.session.UserSession;
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
import com.origami.sgm.entities.models.PubPersona;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.faces.bean.ApplicationScoped;
import javax.inject.Named;
import javax.interceptor.Interceptors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import util.EntityBeanCopy;
import util.Utils;

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
    @javax.inject.Inject
    private UserSession session;

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

    public CtlgItem getItemByCatalagoOrder(String catalogo, Integer orden) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("orden", orden);
            paramt.put("catalogo", this.getCatalogoNombre(catalogo));
            return this.manager.findObjectByParameter(CtlgItem.class, paramt);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public CtlgItem getItemByCatalagoOrder(String catalogo, String valor) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("valor", valor);
            paramt.put("catalogo", this.getCatalogoNombre(catalogo));
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
        CatEnte ente = (CatEnte) manager.findObjectByParameter(CatEnte.class, paramt);
        if (ente != null) {
            return ente;
        } else {
            String ident = (String) paramt.get("ciRuc");
            if (ident.length() < 13) {
                return buscarClienteDinardap(ident, Boolean.TRUE);
            } else {
                return buscarClienteDinardap(ident, Boolean.FALSE);
            }
        }
    }

    public CatEnte buscarClienteDinardap(String identificacion, Boolean esPersona) {
        CatEnte resultado = new CatEnte();
        if (identificacion != null) {
            try {
                if (!identificacion.isEmpty()) {
                    RestTemplate restTemplate = new RestTemplate(Utils.getClientHttpRequestFactory(SisVars.SERVICE_USER, SisVars.SERVICE_PASS));
                    URI uri = new URI(String.format(SisVars.SERVICE_URL, identificacion));
                    try {
                        ResponseEntity<PubPersona> contribuyente = restTemplate.getForEntity(uri, PubPersona.class);
                        if (contribuyente != null && contribuyente.getBody() != null) {
                            PubPersona p = contribuyente.getBody();
                            if (p.getIdentificacion() != null && !p.getIdentificacion().isEmpty()) {
                                resultado = new CatEnte();
                                resultado.setCiRuc(identificacion);
                                resultado.setApellidos(p.getApellidos().toUpperCase());
                                resultado.setNombres(p.getNombres().toUpperCase());
                                if (p.getTipoDocumento() != null) {
                                    resultado.setTipoDocumento(this.getItemByCatalagoOrder("cliente.identificacion", p.getTipoDocumento()));
                                }
                                if (p.getEstadoCivil() != null) {
                                    resultado.setEstadoCivil(this.getItemByCatalagoOrder("cliente.estado_civil", p.getEstadoCivil()));
                                }
                                resultado.setDireccion(p.getDireccion());
                                resultado.setCorreo(p.getCorreo());
                                resultado.setFechaCre(new Date());
                                resultado.setUserCre(session.getName_user());
                                resultado.setEsPersona(esPersona);
                                if (p.getFechaNacimiento() != null) {
                                    try {
                                        SimpleDateFormat dateInstance = new SimpleDateFormat("yyyy-MM-dd");
                                        resultado.setFechaNacimiento(dateInstance.parse(p.getFechaNacimiento()));
                                    } catch (ParseException parseException) {
                                        System.out.println("Error al parsear fecha Nacimiento");
                                    }
                                }
                                resultado = (CatEnte) this.manager.persist(resultado);
                            }
                        }
                    } catch (RestClientException restClientException) {
                        return resultado;
                    }
                }
                return resultado;
            } catch (URISyntaxException | RestClientException e) {
                LOG.log(Level.SEVERE, "Busqueda de cliente", e);
            }
            return resultado;
        } else {
            return null;
        }
    }

}
