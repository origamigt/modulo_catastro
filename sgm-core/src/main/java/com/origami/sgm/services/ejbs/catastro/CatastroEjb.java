/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.ejbs.catastro;

import com.google.gson.annotations.Expose;
import com.origami.app.AppConfig;
import com.origami.censocat.restful.JsonUtils;
import com.origami.config.SisVars;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.models.ListCollectionsReff;
import com.origami.sgm.bpm.models.ModelDepuracionEntes;
import com.origami.sgm.bpm.models.ModelUsuarios;
import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.sgm.database.DatabaseEngine;
import com.origami.sgm.database.Querys;
import com.origami.sgm.database.SchemasConfig;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatBloqueObraEspecial;
import com.origami.sgm.entities.CatCanton;
import com.origami.sgm.entities.CatCertificadoAvaluo;
import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEdfCategProp;
import com.origami.sgm.entities.CatEdfProp;
import com.origami.sgm.entities.CatEdificacionPisosDet;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatNacionalidad;
import com.origami.sgm.entities.CatPais;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioClasificRural;
import com.origami.sgm.entities.CatPredioCultivo;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioEdificacionProp;
import com.origami.sgm.entities.CatPredioLinderos;
import com.origami.sgm.entities.CatPredioObraInterna;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatPredioS12;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatPredioS6;
import com.origami.sgm.entities.CatPropiedadItem;
import com.origami.sgm.entities.CatProvincia;
import com.origami.sgm.entities.CatTipoConjunto;
import com.origami.sgm.entities.CtlgCatalogo;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.EnteCorreo;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.GeDepartamento;
import com.origami.sgm.entities.GeDocumentos;
import com.origami.sgm.entities.HistoricoTramiteDet;
import com.origami.sgm.entities.HistoricoTramites;
import com.origami.sgm.entities.avaluos.SectorValorizacion;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgm.services.ejbs.ServiceLists;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.ejbs.censocat.UploadFotoBean;
import com.origami.sgm.services.interfaces.SeqGenMan;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.Embedded;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionImpl;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import util.EntityBeanCopy;
import util.HiberUtil;
import util.Utils;
import util.managedbeans.AppSimpleList;

/**
 * Implementacion de {@link CatastroServices}, Services con los metodos
 * transaccionales de la ficha
 *
 * @author CarlosLoorVargas
 */
@Stateless(name = "catastro")
@Interceptors(value = {HibernateEjbInterceptor.class})
@Dependent
public class CatastroEjb implements CatastroServices {

    private static final Logger LOG = Logger.getLogger(CatastroEjb.class.getName());

    @Inject
    private Entitymanager manager;

    @Inject
    private SeqGenMan secuencias;

    @Inject
    private ServiceLists catalogoList;
    @Inject
    private AppConfig appconfig;
    @Inject
    protected OmegaUploader fserv;
    @Inject
    protected UploadFotoBean fotoBean;
    @Inject
    private AppSimpleList catalogos;
    @Inject
    private UserSession session;

    @Override
    public CatPredio getPredioId(Long id) {
        CatPredio p = null;
        try {
            p = manager.find(CatPredio.class, id);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return p;
    }

    @Override
    public CatPredio getPredioNumPredio(Long numPredio) {
        CatPredio p = null;
        try {
            p = this.getPredio(Querys.getPredioByNumPredio, new String[]{"numPredio"}, new Object[]{numPredio});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return p;
    }

    @Override
    public CatPredio getPredio(String query, String[] params, Object[] vals) {
        CatPredio p = null;
        try {
            p = (CatPredio) manager.find(query, params, vals);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return p;
    }

    @Override
    public List<CatPredio> getPredios() {
        List<CatPredio> p = null;
        try {
            p = manager.findAll(Querys.getPrediosActivos);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return p;
    }

    @Override
    public List<CatPredio> getPredios(String query, String[] params, Object[] vals) {
        List<CatPredio> p = null;
        try {
            p = manager.findAll(query, params, vals);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return p;
    }

    @Override
    public CatEscritura getEscritura(String query, String[] params, Object[] vals) {
        CatEscritura ct = null;
        try {
            ct = (CatEscritura) manager.find(query, params, vals);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return ct;
    }

    @Override
    public CatPredio generarNumPredio(CatPredio predio) {
        try {
            return secuencias.generarNumPredioAndGuardarCatPredio(predio);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public Boolean editarEscritura(CatEscritura escritura) {
        Boolean flag = false;
        try {
            flag = manager.persist(escritura) != null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return flag;
    }

    @Override
    public Boolean editarPrediosEdificaciones(CatPredio predio, List<CatPredioEdificacion> edificacionEliminar, List<CatPredioEdificacionProp> propiedadesEliminar) {
        Boolean ok = null;
        List<CatPredioEdificacionProp> catPredioEdificacionProps;
        try {
            for (CatPredioEdificacion el : predio.getCatPredioEdificacionCollection()) {
                catPredioEdificacionProps = el.getCatPredioEdificacionPropCollection();
                el.setCatPredioEdificacionPropCollection(null);
                if (el.getId() != null) {
                    manager.update(el);
                } else {
                    el.setEstado("A");
                    el.setPredio(predio);
                    el = (CatPredioEdificacion) manager.persist(el);
                }
                if (!catPredioEdificacionProps.isEmpty()) {
                    ok = guadarOEditarPredioEdifProp(el, catPredioEdificacionProps);
                }
            }
            //Eliminar edificación y sus propiedades. Cambiar el estado de a false
            for (CatPredioEdificacion el : edificacionEliminar) {
                manager.update(el);
                el.setEstado("I");
                for (CatPredioEdificacionProp e : el.getCatPredioEdificacionPropCollection()) {
                    e.setEstado(false);
                    ok = manager.update(e);
                }
            }
            // Eliminar Propiedades individuales. Cambiar estado a false.
            if (!propiedadesEliminar.isEmpty()) {
                ok = guadarOEditarPredioEdifProp(null, propiedadesEliminar);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            ok = false;
        }
        return ok;
    }

    @Override
    public CatPredio getAvaluoPredio(CatPredio predio, Integer anio) {
        CatCertificadoAvaluo ca;
        Connection con = null;
        PreparedStatement ps;
        ResultSet rs;
        CatPredio pred;
        SessionImpl sessionImpl;

        try {
            sessionImpl = (SessionImpl) manager.getSession().getSessionFactory().getCurrentSession();
            con = sessionImpl.connection();
            CallableStatement pstmt = con.prepareCall("{call  " + SchemasConfig.APP1 + ".valoracion_predio(?, ?)}");
            //manager.getNativeQuery("select  "+SchemasConfig.APP1+".valoracion_predio(?, ?)");
            pstmt.setLong(1, predio.getId());
            pstmt.setInt(2, anio);
            pstmt.execute();

            pred = manager.find(CatPredio.class, predio.getId());
        } catch (SQLException | HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
            return null;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
        return pred;
    }

    @Override
    public Boolean guadarOEditarPredioEdifProp(CatPredioEdificacion temp, List<CatPredioEdificacionProp> catPredioEdificacionProps) {
        Boolean ok = null;
        try {
            for (CatPredioEdificacionProp col : catPredioEdificacionProps) {
                if (col.getId() == null) {
                    col.setEstado(true);
                    col.setEdificacion(temp);
                    ok = manager.persist(col) != null;
                } else {
                    ok = manager.update(col);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            ok = false;
        }
        return ok;
    }

    @Override
    public Boolean editPredioS12(CatPredioS12 catPredioS12) {
        try {
            return manager.persist(catPredioS12) != null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredioS4 guardarPredioS4(CatPredioS4 s4) {
        CatPredioS4 ss4 = null;
        CatPredioS4 sss4 = null;
        try {
            ss4 = (CatPredioS4) manager.find(Querys.getPredioS4ByPredio, new String[]{"idPredio"}, new Object[]{s4.getPredio().getId()});
            if (ss4 != null) {
                s4.setId(ss4.getId());
            }
            if (s4.getId() != null) {
                manager.persist(s4);
                sss4 = s4;
            } else {
                sss4 = (CatPredioS4) manager.persist(s4);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return EntityBeanCopy.initializeAndUnproxy(sss4);
    }

    @Override
    public CatPredio guardarPredio(CatPredio s1) {
        if (s1.getProvincia() > 0 && s1.getCanton() > 0 && s1.getParroquia() > 0
                && s1.getZona() != null && s1.getSector() != null && s1.getMz() != null
                && s1.getSolar() != null && s1.getZona() != null && s1.getSector() != null
                && s1.getZona() > 0 && s1.getSector() >= 0 && s1.getMz() >= 0 && s1.getSolar() > 0) {
            Collection<CatPredioEdificacion> edfs = s1.getCatPredioEdificacionCollection();
            s1.setCatPredioEdificacionCollection(null);
            if (s1.getInstCreacion() == null) {
                s1.setInstCreacion(new Date());
            }
            if (!existePredio(s1)) {
                List<CatPredioPropietario> p = (List<CatPredioPropietario>) s1.getCatPredioPropietarioCollection();
                s1 = (CatPredio) secuencias.generarNumPredioAndGuardarCatPredio(s1);
                if (s1 != null) {
                    if (Utils.isNotEmpty(p)) {
                        for (CatPredioPropietario p1 : p) {
                            p1.setPredio(s1);
                        }
                    }
                }
                return s1;
            } else {
                manager.persist(s1);
                if (s1.getCatPredioS6() != null) {
                    s1.setCatPredioS6(this.guardarPredioS6(s1.getCatPredioS6()));
                }
                return EntityBeanCopy.initializeAndUnproxy(s1);
            }
        }
        return null;
    }

    @Override
    public Boolean existePredio(CatPredio s1) {
        try {
            CatPredio predio = (CatPredio) manager.find(Querys.getPredioByCodCat,
                    new String[]{"zonap", "sectorp", "mzp", "solarp", "bloquep", "pisop", "unidadp", "provincia", "canton", "parroquia"},
                    new Object[]{s1.getZona(), s1.getSector(), s1.getMz(), s1.getSolar(), s1.getBloque(), s1.getPiso(), s1.getUnidad(),
                        s1.getProvincia(), s1.getCanton(), s1.getParroquia()});
            return predio != null;
//                if (predio.getEstado() == null) {
//                    predio.setEstado("A");
//                } else {
//                    if (!predio.getEstado().equalsIgnoreCase("A") && !predio.getEstado().equalsIgnoreCase("I")) {
//                        predio.setEstado("A");
//                        manager.persist(predio);
//                        return true;
//                    }
//                }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "existePredio", e);
            return null;
        }
    }

    @Override
    public Boolean validarFrente(CatPredioS4 s4) {
        if (s4.getFrente1() != null && s4.getFrente2() != null && s4.getFrente3() != null && s4.getFrente4() != null) {
            BigDecimal total = s4.getFrente1().add(s4.getFrente2().add(s4.getFrente3().add(s4.getFrente4())));
            return s4.getFrenteTotal().compareTo(total) == 0;
        }
        return false;
    }

    @Override
    public BigDecimal areaCalculada(CatPredioS4 s4) {
        BigDecimal sFrene1 = s4.getFrente1().add(s4.getFrente2());
        BigDecimal sFondo = s4.getFondo1().add(s4.getFondo2());
        return sFrene1.divide(new BigDecimal(2)).multiply(sFondo.divide(new BigDecimal(2)));
    }

    @Override
    public CatPredioS12 guardarPredioS12(CatPredioS12 catPredioS12, List<CtlgItem> usos) {
        CatPredioS12 s12 = null;
        try {
            if (catPredioS12 != null) {
                catPredioS12.setUsosList(null);
                s12 = (CatPredioS12) manager.persist(catPredioS12);
                s12.setUsosList(usos);
                manager.update(s12);
            } else {
                s12 = (CatPredioS12) manager.persist(catPredioS12);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return s12;
    }

    @Override
    public CatPredioS6 guardarPredioS6(CatPredioS6 s6, List<CtlgItem> vias, List<CtlgItem> instalacionesEspeciales) {
        HiberUtil.newTransaction();
        CatPredioS6 ss6 = null;
        try {
            if (s6 == null) {
                return null;
            }
            if (vias != null && !vias.isEmpty()) {
                s6.setCtlgItemCollection(vias);
            }
            if (instalacionesEspeciales != null && !instalacionesEspeciales.isEmpty()) {
                s6.setCtlgItemCollectionInstalacionEspecial(instalacionesEspeciales);
            }
            ss6 = (CatPredioS6) manager.find(Querys.getPredioS6ByPredio, new String[]{"idPredio"}, new Object[]{s6.getPredio().getId()});
            if (ss6 != null && ss6.getId() != null) {
                s6.setId(ss6.getId());
            }
            if (s6.getId() != null) {
                manager.persist(s6);
                Hibernate.initialize(s6);
            } else {
                s6 = (CatPredioS6) manager.persist(s6);
            }
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, "guardarPredioS6", e);
        }
        return s6;

    }

    @Override
    public CatPredioS4 getCatPredioS4(Long idPredio) {
        return manager.find(CatPredioS4.class, idPredio);
    }

    @Override
    public CatPredioS6 getCatPredioS6(Long idPredio) {
        return manager.find(CatPredioS6.class, idPredio);
    }

    @Override
    public CatPredioS12 getCatPredioS12(Long idPredio) {
        return manager.find(CatPredioS12.class, idPredio);
    }

    private ModelDepuracionEntes AgregarModelDepuracionEntes(CatEnte e, List list1) {
        if (Utils.isNotEmpty(list1)) {
            if (list1.size() > 0) {
                return new ModelDepuracionEntes(e, list1);
            }
        }
        return null;
    }

    @Override
    public Boolean actualizarEnteAndCollection(List<CatEnte> sel, List<ModelDepuracionEntes> modificarLista, String user) {
        try {
            for (ModelDepuracionEntes list : modificarLista) {
                for (ListCollectionsReff elemento : list.getElementosAsociados()) {
                    for (Object object : elemento.getElementos()) {
//                        System.out.print("Elemento actualizado: " + object);
                        manager.persist(object);
                    }

                }
//                System.out.println(" >>> Ente inhabilitado " + list.getEnteElemento());
            }

            for (CatEnte e : sel) {
                e.setUserMod(user);
                e.setFechaMod(new Date());
                e.setEstado("I");
                e.setEstadoCorrecion("C");
                manager.persist(e);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            return false;
        }
        return true;
    }

    protected Boolean existe(Boolean x) {
        if (x != null) {
            return x;
        } else {
            return false;
        }
    }

    @Override
    public List<CatEnte> getCatEnteByNombresApellidos(String entry, Boolean esPersona) {
        List<CatEnte> list = new ArrayList<>();
        Object[] parametros = new Object[]{("%".concat(entry).concat("%")), esPersona};
        try {
            List<CatEnte> listAp = manager.findAll(Querys.CatEnteByApellidoslList,
                    new String[]{"apellidos", "esPersona"}, parametros);
            List<CatEnte> listNo = manager.findAll(Querys.CatEnteByNombresList,
                    new String[]{"nombres", "esPersona"}, parametros);

            if (Utils.isNotEmpty(listAp)) {
                list.addAll(listAp);
            }
            if (Utils.isNotEmpty(listNo)) {
                List<CatEnte> entes = new ArrayList<>();
                if (Utils.isNotEmpty(list)) {
                    for (CatEnte ente : listNo) {
                        for (CatEnte l : list) {
                            if (l.getId().compareTo(ente.getId()) == 1) {
                                entes.add(ente);
                            }
                        }
                    }
                } else {
                    entes.addAll(listNo);
                }
                list.addAll(entes);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return list;
    }

    @Override
    public List<CatEnte> getEntesByRazonSocial(String razonSocial) {
        try {
            return manager.findAll(Querys.CatEnteByRazonSocialListAct, new String[]{"razonSocial"}, new Object[]{"%" + razonSocial + "%"});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public Boolean actualizarEntesDepurado(List<ModelDepuracionEntes> modificarLista, CatEnte enteValido) {
        try {
            for (ModelDepuracionEntes elementos : modificarLista) {
                for (ListCollectionsReff reff : elementos.getElementosAsociados()) {
                    for (Object asoc : reff.getElementos()) {
                        Boolean cambiado = ReflexionEntity.setCampo(asoc, reff.getCampoAsociado(), enteValido);
                        if (cambiado) {
//                            System.out.println("Tabla reff " + asoc
//                                    + " Campo " + reff.getCampoAsociado()
//                                    + " ente anterior: " + elementos.getEnteElemento().getId()
//                                    + " ente nuevo: " + enteValido.getId());
                            elementos.getEnteElemento().setEstado("I");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            return false;
        }
        return true;
    }

    @Override
    public Boolean actualizarEnte(CatEnte valido) {
        return manager.update(valido);
    }

    @Override
    public Boolean guardarHistoricoPredio(com.origami.sgm.entities.historic.Predio p) {
        try {
            if (p != null) {
                manager.persist(p);
                return true;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public Long existeEnteByCiRuc(String[] param, Object[] values) {
        return (Long) manager.findNoProxy(Querys.getIdEnte, param, values);
    }

    @Override
    public CatCertificadoAvaluo guardarCertificado(CatCertificadoAvaluo c) {
        try {
            if (c.getId() == null) {
                CatCertificadoAvaluo cert = (CatCertificadoAvaluo) manager.persist(c);
                if (SchemasConfig.DB_ENGINE.equals(DatabaseEngine.POSTGRESQL)) {
                    cert.setCodigo(Utils.encriptSHAHex(cert.getId().toString()));
                }
                return (CatCertificadoAvaluo) manager.persist(cert);
            } else {
                c.setFecAct(new Date());
                return (CatCertificadoAvaluo) manager.persist(c);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public List getPropietarios(String ident, String nombres, String apellidos, String rsocial) {
        try {
            int x = 0;
            String sql = "select e.id from CatPredioPropietario e where ";
            String rest = "";
            if (ident != null) {
                if (x == 0) {
                    rest += "e.ente.ciRuc ilike '%".concat(ident).concat("%'");
                    x++;
                } else {
                    rest += " and e.ente.ciRuc ilike '%".concat(ident).concat("%'");
                }
            }
            if (nombres != null) {
                if (x == 0) {
                    rest += "e.ente.nombres ilike '%".concat(nombres).concat("%'");
                    x++;
                } else {
                    rest += " and e.ente.nombres ilike '%".concat(nombres).concat("%'");
                }
            }
            if (apellidos != null) {
                if (x == 0) {
                    rest += "e.ente.apellidos ilike '%".concat(apellidos).concat("%'");
                    x++;
                } else {
                    rest += " and e.ente.apellidos ilike '%".concat(apellidos).concat("%'");
                }
            }
            if (rsocial != null) {
                if (x == 0) {
                    rest += "e.ente.razonSocial ilike '%".concat(rsocial).concat("%'");
                    x++;
                } else {
                    rest += " and e.ente.razonSocial ilike '%".concat(rsocial).concat("%'");
                }
            }
            return manager.findAll(sql.concat(rest));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public Boolean guardarHistoricoPredio(Long numPredio, String jsPredioAnt, String jsPredioAct, String usurario, String obs, String fedifAnt, String fedifAct, String fModelAnt, String fModelAct, GeDocumentos documento) {
        try {
            com.origami.sgm.entities.historic.Predio p = new com.origami.sgm.entities.historic.Predio();
            p.setPredio(numPredio);
            p.setFecAct(new Date());
            p.setFichaAnt(jsPredioAnt);
            p.setFichaAct(jsPredioAct);
            p.setFichaEdificacionAnt(fedifAnt);
            p.setFichaEdificacionAct(fedifAct);
            p.setFichaModelAnt(fModelAnt);
            p.setFichaModelAct(fModelAct);
            p.setUsuario(usurario);
            p.setObservacion(obs.toUpperCase());
            p.setMigrado(Boolean.FALSE);
            p.setGeDocumento(documento);
            JsonUtils js = new JsonUtils();
            String cambios = null;
            try {
                cambios = obtenerCambios(js.jsonToObject(jsPredioAnt, CatPredio.class), js.jsonToObject(jsPredioAct, CatPredio.class));
            } catch (Exception exception) {
                LOG.log(Level.SEVERE, null, exception);
            }
            p.setCambios(cambios);
            return manager.persist(p) != null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return false;
    }

//    @Override
    public String obtenerCambios(Object anterior, Object actual) throws Exception {
        String cambios = "";
        if (anterior != null && actual != null) {
            if (anterior.getClass() != null && actual.getClass() != null) {
                List<Field> camposAnt = Utils.getList(Arrays.asList(anterior.getClass().getDeclaredFields()));
                List<Field> camposActual = Utils.getList(Arrays.asList(actual.getClass().getDeclaredFields()));
                List<Field> tempList = null;
                if (camposAnt.size() < 0 && camposActual.size() > 0) {
                    tempList = camposActual;
                } else {
                    tempList = camposAnt;
                }
                for (Field campoAnt : tempList) {
                    try {
                        campoAnt.setAccessible(true);
                        Field campoAct;
                        String nombre = campoAnt.getName();
                        if (campoAnt.isAnnotationPresent(Expose.class)) {
                            int indexOf = camposActual.indexOf(campoAnt);
                            campoAct = camposActual.get(indexOf);
                            campoAct.setAccessible(true);
                            Object valorAnterior = campoAnt.get(anterior);
                            Object valorActual = campoAct.get(actual);
                            nombre = appconfig.retornarNombreCampo(nombre);
                            if (indexOf > -1) {
                                if (campoAnt.isAnnotationPresent(Embedded.class)) {
                                    if (valorAnterior == null) {
                                        valorAnterior = campoAnt.getType().newInstance();
                                    }
                                    if (valorActual == null) {
                                        valorActual = campoAct.getType().newInstance();
                                    }
                                    cambios += obtenerCambios(valorAnterior, valorActual);
                                } else if (valorAnterior instanceof CtlgItem) {
                                    CtlgItem item = (CtlgItem) valorAnterior;
                                    CtlgItem itemAct = (CtlgItem) valorActual;
                                    if (!item.equals(itemAct)) {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio((item == null) ? null : item.getValor()) + " a "
                                                + Utils.retornarVacio((itemAct == null) ? null : itemAct.getValor()) + "\n";
                                    }
                                } else if (valorAnterior instanceof CatProvincia) {
                                    CatProvincia item = (CatProvincia) valorAnterior;
                                    CatProvincia itemAct = (CatProvincia) valorActual;
                                    if (!item.equals(itemAct)) {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio((item == null) ? null : item.getDescripcion()) + " a "
                                                + Utils.retornarVacio((itemAct == null) ? null : itemAct.getDescripcion()) + "\n";
                                    }
                                } else if (valorAnterior instanceof CatTipoConjunto) {
                                    CatTipoConjunto item = (CatTipoConjunto) valorAnterior;
                                    CatTipoConjunto itemAct = (CatTipoConjunto) valorActual;
                                    if (!item.equals(itemAct)) {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio((item == null) ? null : item.getNombre()) + " a "
                                                + Utils.retornarVacio((itemAct == null) ? null : itemAct.getNombre()) + "\n";
                                    }
                                } else if (valorAnterior instanceof CatCiudadela) {
                                    CatCiudadela item = (CatCiudadela) valorAnterior;
                                    CatCiudadela itemAct = (CatCiudadela) valorActual;
                                    if (!item.equals(itemAct)) {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio((item == null) ? null : item.getNombre()) + " a "
                                                + Utils.retornarVacio((itemAct == null) ? null : itemAct.getNombre()) + "\n";
                                    }
                                } else if (valorAnterior instanceof SectorValorizacion) {
                                    SectorValorizacion item = (SectorValorizacion) valorAnterior;
                                    SectorValorizacion itemAct = (SectorValorizacion) valorActual;
                                    if (!item.equals(itemAct)) {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio((item == null) ? null : item.getDetalle()) + " a "
                                                + Utils.retornarVacio((itemAct == null) ? null : itemAct.getDetalle()) + "\n";
                                    }
                                } else if (valorAnterior instanceof CatPropiedadItem) {
                                    CatPropiedadItem item = (CatPropiedadItem) valorAnterior;
                                    CatPropiedadItem itemAct = (CatPropiedadItem) valorActual;
                                    if (!item.equals(itemAct)) {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio((item == null) ? null : item.getNombre()) + " a "
                                                + Utils.retornarVacio((itemAct == null) ? null : itemAct.getNombre()) + "\n";
                                    }
                                } else if (valorAnterior instanceof Collection) {
                                    List collAnt = (List) valorAnterior;
                                    List collActual = (List) valorActual;

                                    if (collAnt != null && collActual != null) {
                                        if (collAnt.size() < collActual.size()) {
                                            cambios += "Se agrego " + (collActual.size() - collAnt.size()) + " nuevo item a " + nombre + "\n";
                                        } else if (collAnt.size() > collActual.size()) {
                                            cambios += "Se elimino " + (collAnt.size() - collActual.size()) + " item de " + nombre + "\n";
                                        } else {
                                            String temp = "";
                                            for (Object object1 : collAnt) {
                                                int indexActual = collActual.indexOf(object1);
                                                if (indexActual > -1) {
                                                    Object dato = collActual.get(indexActual);
                                                    temp += obtenerCambios(object1, dato);
                                                } else {
                                                    Object faltante = new Object();
                                                    for (Object object : collActual) {
                                                        int indx = collAnt.indexOf(object);
                                                        if (indx <= -1) {
                                                            faltante = object;
                                                            break;
                                                        }
                                                    }
                                                    cambios += "Se reemplazo en " + nombre + " al item " + object1 + " por " + faltante + "\n";
                                                }
                                            }
                                            if (!temp.equals("")) {
                                                cambios += temp + " en " + appconfig.retornarNombreCampo(campoAnt.getName());
                                            }
                                        }
                                    }
                                } else if (campoAnt.isAnnotationPresent(javax.persistence.ManyToOne.class)
                                        || campoAnt.isAnnotationPresent(javax.persistence.OneToOne.class)) {
                                    if (valorAnterior == null) {
                                        valorAnterior = campoAnt.getType().newInstance();
                                    }
                                    if (valorActual == null) {
                                        valorActual = campoAct.getType().newInstance();
                                    }
                                    cambios += obtenerCambios(valorAnterior, valorActual);
                                } else if (!Objects.equals(valorAnterior, valorActual)) {
                                    if (valorAnterior instanceof Date) {
                                        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
                                        cambios += "Se cambió " + nombre + " de " + valorAnterior + " a " + valorActual + "\n";
                                    } else if (valorAnterior instanceof Number) {
                                        if (valorAnterior == null && valorActual != null) {
                                            cambios += "Se cambió " + nombre + " de vacio a " + valorActual + "\n";
                                        } else if (valorAnterior != null && valorActual == null) {
                                            cambios += "Se cambió " + nombre + " de " + valorAnterior + " a vacio\n";
                                        } else if ((new BigDecimal(valorAnterior.toString()).compareTo(new BigDecimal(valorActual.toString())) != 0)) {
                                            cambios += "Se cambió " + nombre + " de " + valorAnterior + " a " + valorActual + "\n";
                                        }
                                    } else {
                                        cambios += "Se cambió " + nombre + " de " + Utils.retornarVacio(valorAnterior) + " a " + Utils.retornarVacio(valorActual) + "\n";
                                    }
                                }
                            } else {
                                cambios += "Se elimino en " + "\n";
                            }
                        }
                    } catch (IllegalArgumentException | IllegalAccessException | SecurityException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }

                }
            }
        }
        return cambios;
    }

    @Override
    public Boolean guardarHistoricoPredio(Long numPredio, String pant, String pact, String sac, String usurario, String obs) {
        try {
            com.origami.sgm.entities.historic.Predio p = new com.origami.sgm.entities.historic.Predio();
            p.setPredio(numPredio);
            p.setFecAct(new Date());
            p.setFichaAnt(pant);
            p.setFichaAct(pact);
            p.setUsuario(usurario);
            p.setObservacion(obs.toUpperCase());
            //p.setSac(sac);
            p.setMigrado(Boolean.TRUE);
            return manager.persist(p) != null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public CatPredioPropietario getPredioPropietarioById(Long valueOf) {
        return manager.find(CatPredioPropietario.class, valueOf);
    }

    @Override
    public boolean existePropietarioPredio(CatPredio predio, Long idEnte) {

        String[] param = {"idPredio", "idEnte", "estado"};
        Object[] values = {predio.getId(), idEnte, "A"};
        List<CatPredioPropietario> result = manager.findAllEntCopy(Querys.existePropietario, param, values);

        if (result != null) {
            if (!result.isEmpty()) {
                System.out.println("Id del Catprediopropietario: " + result.get(0).getId());
                return true;
            }
        }
        return false;
//        Map paramt = new HashMap<>();
//        paramt.put("predio", predio);
//        paramt.put("estado", "A");
//        List<CatPredioPropietario> l = manager.findObjectByParameterList(CatPredioPropietario.class, paramt);
//        for (CatPredioPropietario p : l) {
//            if (p.getEnte().getId().compareTo(idEnte) == 0) {
//                return true;
//            }
//        }
//        return false;
    }

    @Override
    public CatPredioEdificacion guardarEdificacion(CatPredioEdificacion edif, String usuario) {
        try {
            if (edif != null) {

                CatPredioEdificacion edf = null;
                if (edif.getId() == null) {
                    edf = (CatPredioEdificacion) manager.persist(edif);
                } else {
                    manager.persist(edif);
                    edf = edif;
                }
                if (edf != null) {
                    Object dep = null;
                    switch (SchemasConfig.DB_ENGINE) {
                        case ORACLE:
                            if (edf.getVidautil() != null && edf.getAnioCons() != null && edf.getEstadoConservacion().getId() != null) {
                                dep = manager.getNativeQuery(Querys.getDepreciacion(edf.getVidautil(), edf.getAnioCons(), edf.getEstadoConservacion().getId()));
                            }
                            break;
                        case POSTGRESQL:
                            if (edf.getVidautil() != null) {
                                dep = manager.getNativeQuery("select  " + SchemasConfig.APP1 + ".depreciar(?,?,?)", new Object[]{edf.getVidautil(), edf.getAnioCons(), edf.getEstadoConservacion().getId()});
                            }
                            break;
                        default:
                            break;
                    }
                    if (dep == null) {
                        if (edif.getEstadoConservacion() != null) {
                            System.out.println("factor depreciacion es null, estado conservacion: " + edif.getEstadoConservacion().getId());
                        } else {
                            System.out.println("factor depreciacion es null");
                        }

                    } else {
                        edf.setFactorDepreciacion(new BigDecimal(dep.toString()));
                    }
//                    sumarAreaEdificacion(edf.getPredio());
                    return edf;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredioPropietario guardarPropietario(CatPredioPropietario p, String usuario) {
        try {
            if (p != null) {
                if (p.getId() != null) {
                    if (p.getEnte() != null) {
                        manager.persist(p.getEnte());
                    }
                    p.setModificado(usuario);
                    p = (CatPredioPropietario) manager.persist(p);
                    return p;
                } else {
                    if (p.getEnte() != null) {
                        CatEnte e = p.getEnte();
                        if (e.getId() == null) {
                            e.setFechaCre(new Date());
                            e.setUserCre(usuario);
                            p.setEnte((CatEnte) manager.persist(e));
                        } else {
                            manager.persist(p.getEnte());
                        }
                    }
                    CatPredioPropietario titular = this.existePropietarioPredioTitular(p.getPredio());
                    if (titular != null && p.getTipo().getOrden() != 1) {
                        BigDecimal subtract = BigDecimal.ZERO;
                        if (p.getEstado().equalsIgnoreCase("A")) {
                            subtract = titular.getPorcentajePosecion().subtract(p.getPorcentajePosecion());
                        } else {
                            subtract = titular.getPorcentajePosecion().add(p.getPorcentajePosecion());
                        }
                        titular.setPorcentajePosecion(subtract);
                        titular = (CatPredioPropietario) manager.persist(titular);
                    }
                    p = (CatPredioPropietario) manager.saveAll(p);
                    return p;
                }
            }
        } catch (HibernateException e) {
            LOG.log(Level.SEVERE, null, e);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public Boolean quitarEdificacionGeneral(CatPredioEdificacion ed) {
        try {

            ed = (CatPredioEdificacion) manager.persist(ed);
            if (ed.getCatPredioEdificacionPropCollection() != null) {
                for (CatPredioEdificacionProp cx : ed.getCatPredioEdificacionPropCollection()) {
                    cx = (CatPredioEdificacionProp) manager.persist(cx);
                    manager.delete(cx);
                }
            }
            manager.delete(ed);
            sumarAreaEdificacion(ed.getPredio());
            return true;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public Collection<CatPredioEdificacion> getEdificaciones(CatPredio predio) {
        return manager.findAll(Querys.getCatEdificacionesByPredio, new String[]{"predioId"}, new Object[]{predio.getId()});
    }

    @Override
    public Boolean quitarFoto(FotoPredio f) {
        try {
            if (f != null) {
                f = (FotoPredio) manager.persist(f);
                return manager.delete(f);

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public Boolean guardarDetallePisos(List<CatEdificacionPisosDet> pisos) {
        try {
            for (CatEdificacionPisosDet p : pisos) {
                manager.persist(p);
            }
            return Boolean.TRUE;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public List<CatNacionalidad> getNacionalidades() {
        return manager.findAllEntCopy(CatNacionalidad.class
        );
    }

    @Override
    public List<CatPais> getPaises() {
        return manager.findAllEntCopy(CatPais.class
        );
    }

    @Override
    public List<CtlgItem> getItemsByCatalogo(String catalogo) {
        return manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{catalogo});
    }

    @Override
    public List<CatPredio> registrarPHs(List<CatPredio> v, List<CatPredio> h) {
        List<CatPredio> lpreds = new ArrayList<>();
        try {
            for (CatPredio pv : v) {
                if (!pv.getCrear()) {
                    Collection<CatPredioEdificacion> edifi = pv.getCatPredioEdificacionCollection();
                    pv.setCatPredioEdificacionCollection(null);
                    CatPredio pxv = this.guardarPredio(pv);
                    pv.setCatPredioEdificacionCollection(null);
                    if (pxv != null) {
                        if (pv.getCatPredioS6() != null) {
                            pv.getCatPredioS6().setPredio(pxv);
                            pv.getCatPredioS6().setId(null);
                            pxv.setCatPredioS6(this.guardarPredioS6(pv.getCatPredioS6()));
                            this.guardarPredio(pxv);
                        }
                        if (pv.getCatPredioS4() != null) {
                            pv.getCatPredioS4().setPredio(pxv);
                            pv.getCatPredioS4().setId(null);
                            pxv.setCatPredioS4(this.guardarPredioS4(pv.getCatPredioS4()));
                            this.guardarPredio(pxv);
                        }
                        if (pv.getCatPredioPropietarioCollection() != null) {
                            for (CatPredioPropietario pp : pv.getCatPredioPropietarioCollection()) {
                                if (pp.getEstado().equalsIgnoreCase("A")) {
                                    CatPredioPropietario px = new CatPredioPropietario();
                                    px.setPredio(pxv);
                                    px.setTipo(pp.getTipo());
                                    px.setFecha(new Date());
                                    px.setEnte(pp.getEnte());
                                    px.setUsuario(pxv.getUsuarioCreador().getUsuario());
                                    px.setEstado("A");
                                    px.setEsResidente(pp.getEsResidente());
                                    manager.persist(px);
                                }
                            }
                        }
                        if (Utils.isNotEmpty((List<?>) pv.getCatEscrituraCollection())) {
                            for (CatEscritura ce : pv.getCatEscrituraCollection()) {
                                if (ce.getEstado().equalsIgnoreCase("A")) {
                                    ce.setPredio(pxv);
                                    ce.setIdEscritura(null);
                                    manager.persist(ce);
                                }
                            }
                        }
                        if (Utils.isNotEmpty((List<?>) edifi)) {
                            for (CatPredioEdificacion ed : edifi) {
                                ed.setPredio(pxv);
                                ed.setId(null);
                                List<CatPredioEdificacionProp> edp = ed.getCatPredioEdificacionPropCollection();
                                ed.setCatPredioEdificacionPropCollection(null);
                                ed = (CatPredioEdificacion) manager.persist(ed);
                                if (ed != null) {
                                    if (Utils.isNotEmpty((List<?>) edp)) {
                                        for (CatPredioEdificacionProp p : edp) {
                                            p.setId(null);
                                            p.setEdificacion(ed);
                                            manager.persist(p);
                                        }
                                    }
                                }
                            }
                        }
//                        try {
//                            ValoracionPredial vp = avaluos.getEmisionPredial(pxv.getUsuarioCreador().getUsuario(), Integer.parseInt(new SimpleDateFormat("YYYY").format(new Date())), pxv.getNumPredio(), Boolean.TRUE).get();
//                            if (vp != null) {
//                                try {
//                                    pxv.setAreaSolar(vp.getAreaSolar());
//                                    pxv.setAvaluoConstruccion(vp.getAvaluoEdificacion());
//                                    pxv.setAvaluoSolar(vp.getAvaluoSolar());
//                                    pxv.setAvaluoMunicipal(vp.getAvaluoMunicipal());
//                                } catch (Exception e) {
//                                    System.out.println("error " + e.getMessage());
//                                }
//                                ValoracionPredial matriz = (ValoracionPredial) manager.find(QuerysAvaluos.getPredioByNumPredio_Version, new String[]{"numPredio", "numVersion"}, new Object[]{vp.getNumeroMatriz(), vp.getNumVersion()});
//                                if (matriz != null) {
//                                    if (vp.getAreaConstruccion() != null) {
//                                        pxv.setAreaConstPh(matriz.getAreaConstruccion().multiply(pxv.getAlicuotaUtil().divide(new BigDecimal("100"))));
//                                    }
//                                }
//                                this.guardarPredio(pxv);
//                            }
//                        } catch (Exception e) {
//                            System.out.println("error " + e.getMessage());
//                        }
                        lpreds.add(pxv);

                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return lpreds;
    }

    @Override
    public CatPredioS6 guardarPredioS6(CatPredioS6 catPredioS6) {
        try {
            if (catPredioS6.getId() == null) {
                return (CatPredioS6) manager.persist(catPredioS6);
            } else {
                manager.persist(catPredioS6);
                return EntityBeanCopy.initializeAndUnproxy(catPredioS6);

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }

    @Override
    public CatPredio saveUpdatePredio(CatPredio predio) {
        try {
            return (CatPredio) manager.persist(predio);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
            return null;
        }

    }

    @Override
    public CatPredio sumarAreaEdificacion(CatPredio predio) {
        try {
            CatPredio px = null;
            if (predio != null) {
                BigDecimal areaConstruccion = (BigDecimal) manager.find(Querys.getCatEdificacionesByPredioSumAreaConst, new String[]{"predioId"}, new Object[]{predio.getId()});
                if (areaConstruccion == null) {
                    areaConstruccion = BigDecimal.ZERO;
                }
                predio.setAreaDeclaradaConst(areaConstruccion);
                px = (CatPredio) manager.persist(predio);
                Hibernate.initialize(predio);
            }
            return px;

        } catch (HibernateException e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public Integer getSolarMaxPredio(CatPredio s1) {
        try {
            HiberUtil.newTransaction();
            Integer numMaxSolar = Integer.valueOf((String.valueOf(manager.find(Querys.getMaxSolar,
                    new String[]{"zonap", "sectorp", "mzp", "parroquia", "provincia", "canton"},
                    new Object[]{s1.getZona(), s1.getSector(), s1.getMz(), s1.getParroquia(), s1.getProvincia(), s1.getCanton()}))));
            if (numMaxSolar != null) {
                return numMaxSolar;
            } else {
                return 0;
            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, "getSolarMaxPredio", e);
            return null;
        }
    }

    @Override
    public Integer getUnidadMaxPredio(CatPredio s1) {
        try {
            Integer numMaxUnidad = Integer.valueOf((String.valueOf(manager.find(Querys.getMaxUnidad,
                    new String[]{"zonap", "sectorp", "mzp", "parroquia", "provincia", "canton", "bloquep", "pisop"},
                    new Object[]{s1.getZona(), s1.getSector(), s1.getMz(), s1.getParroquia(), s1.getProvincia(), s1.getCanton(), s1.getBloque(), s1.getPiso()}))));

            if (numMaxUnidad != null) {
                return numMaxUnidad;
            } else {
                return null;

            }
        } catch (NumberFormatException e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, "getUnidadMaxPredio", e);
            return null;
        }
    }

    @Override
    public List<CatPredio> findAllByManzana(Short codParroquia, Short codZona, Short codSector, Short codManzana) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CatPredioS4 getPredioS4ByPredio(CatPredio predio) {
        CatPredioS4 s4 = null;
        String[] param = {"idPredio"};
        Object[] values = {predio.getId()};
        try {
            List<CatPredioS4> result = manager.findAll(Querys.getPredioS4ByPredio, param, values);
            if (result != null) {
                if (result.size() > 0) {
                    s4 = result.get(0);
                }
            }
            return s4;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, "existePredio", e);
            return null;
        }
    }

    @Override
    public CatPredioS6 getPredioS6ByPredio(CatPredio predio) {
        CatPredioS6 s6 = null;
        String[] param = {"idPredio"};
        Object[] values = {predio.getId()};
        try {
            List<CatPredioS6> result = manager.findAll(Querys.getPredioS6ByPredio, param, values);
            if (result != null) {
                if (result.size() > 0) {
                    s6 = result.get(0);
                }
            }
            return s6;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, "existePredio", e);
            return null;
        }
    }

    @Override
    public List<CatEdfCategProp> getCategoriasConst() {
        List<CatEdfCategProp> p = manager.findAllObjectOrder(CatEdfCategProp.class,
                new String[]{"guiOrden"}, true);
        List<CatEdfCategProp> result = new ArrayList<>();
        p.stream().map((catEdfCategProp) -> {
            catEdfCategProp.getCatEdfPropCollection().size();
            return catEdfCategProp;
        }).map((catEdfCategProp) -> {
            List<CatEdfProp> propTemp = (List<CatEdfProp>) EntityBeanCopy.clone(catEdfCategProp.getCatEdfPropCollection());
            CatEdfCategProp temp = (CatEdfCategProp) EntityBeanCopy.clone(catEdfCategProp);
            temp.setCatEdfPropCollection(propTemp);
            return temp;
        }).forEachOrdered((temp) -> {
            result.add(temp);
        });
        return result;
    }

    @Override
    public List<CatEdfCategProp> getCategoriasConstByEstado() {
        List<CatEdfCategProp> p = manager.findAll(Querys.getCatEdfCategPropByEstado, new String[]{"estado"}, new Object[]{'A'});
        List<CatEdfCategProp> result = new ArrayList<>();
        p.stream().map((catEdfCategProp) -> {
            catEdfCategProp.getCatEdfPropCollection().size();
            return catEdfCategProp;
        }).map((catEdfCategProp) -> {
            List<CatEdfProp> propTemp = (List<CatEdfProp>) EntityBeanCopy.clone(manager.findAll(Querys.getCatEdfPropByEstadoAndCategoria, new String[]{"estado", "idCategoria"}, new Object[]{'A', catEdfCategProp.getId()}));
            CatEdfCategProp temp = (CatEdfCategProp) EntityBeanCopy.clone(catEdfCategProp);
            temp.setCatEdfPropCollection(propTemp);
            return temp;
        }).forEachOrdered((temp) -> {
            result.add(temp);
        });
        return result;
    }

    @Override
    public List<CatPredioLinderos> guardarLinderos(List<CatPredioLinderos> linderos) {
        List<CatPredioLinderos> ld = new ArrayList<>();
        try {
            for (CatPredioLinderos lindero : linderos) {
                CatPredioLinderos persist;
                if (lindero.getId() == null) {
                    persist = (CatPredioLinderos) manager.persist(lindero);
                } else {
                    manager.persist(lindero);
                    persist = lindero;
                }
                if (persist != null) {
                    ld.add(persist);
                }
            }
            return ld;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public TreeNode getTreeNode(List<CatEdfCategProp> catProp) {
        TreeNode nodo = new DefaultTreeNode();
        catProp.forEach((catEdfCategProp) -> {
            TreeNode caracteristicas = new DefaultTreeNode(catEdfCategProp, nodo);
            if (Utils.isNotEmpty((List<?>) catEdfCategProp.getCatEdfPropCollection())) {
                catEdfCategProp.getCatEdfPropCollection().forEach((prop) -> {
                    TreeNode propiedades = new DefaultTreeNode(prop, caracteristicas);
                });
            }
        });
        return nodo;
    }

    @Override
    public CatPredioClasificRural
            getPredioClasificRuralById(Long valueOf) {
        return manager.find(CatPredioClasificRural.class,
                valueOf);
    }

    @Override
    public CatPredioClasificRural guardarClasificacionSueloRural(CatPredioClasificRural p, String usuario) {
        try {
            if (p.getId() != null) {
                return (CatPredioClasificRural) manager.persist(p);
            } else {
                manager.persist(p);
                return p;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredioEdificacionProp
            getBloqueCaracteristicaById(Long valueOf) {
        return manager.find(CatPredioEdificacionProp.class,
                valueOf);
    }

    @Override
    public CatPredioEdificacion
            getPredioBloqueById(Long valueOf) {
        return manager.find(CatPredioEdificacion.class,
                valueOf);
    }

    @Override
    public CatPredioEdificacion guardarBloque(CatPredioEdificacion b, String usuario) {
        try {
            CatPredioEdificacion cpe;
            if (b.getId() != null) {
                cpe = (CatPredioEdificacion) manager.persist(b);
                return cpe;
            } else {
                b.setModificado(usuario);
                cpe = (CatPredioEdificacion) manager.persist(b);
                return cpe;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CtlgItem
            getCtlgitemById(Long valueOf) {
        return manager.find(CtlgItem.class,
                valueOf);
    }

    @Override
    public CatPredioEdificacionProp guardarCaracteristica(CatPredioEdificacionProp c) {
        try {
            if (c.getId() == null) {
                return (CatPredioEdificacionProp) manager.persist(c);
            } else {
                manager.persist(c);
                return c;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredioCultivo guardarCultivo(CatPredioCultivo p, String usuario) {
        try {
            if (p.getId() == null) {
                return (CatPredioCultivo) manager.persist(p);
            } else {
                manager.persist(p);
                return p;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredioCultivo
            getPredioCultivoById(Long valueOf) {
        return manager.find(CatPredioCultivo.class,
                valueOf);
    }

    @Override
    public CatBloqueObraEspecial
            getBloqueObraEspecialById(Long valueOf) {
        return manager.find(CatBloqueObraEspecial.class,
                valueOf);
    }

    @Override
    public CatBloqueObraEspecial guardarObraEspecial(CatBloqueObraEspecial p, String usuario) {
        try {
            if (p.getId() == null) {
                return (CatBloqueObraEspecial) manager.persist(p);
            } else {
                manager.persist(p);
                return p;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public void guardarEdificacion(CatPredioEdificacion edificacion) {
        try {
            manager.persist(edificacion);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

//    @Override
//    public List<CatEdfCategProp> getCategoriasConst() {
//        return manager.findAllObjectOrder(CatEdfCategProp.class,
//                new String[]{"guiOrden"}, true);
//    }
    @Override
    public List<CatEdfProp> getPropiedadesConst(CatEdfCategProp categoriaConst) {

        Map<String, Object> paramts = new HashMap<>();
        paramts.put("categoria", categoriaConst);
        switch (SchemasConfig.DB_ENGINE) {
            case ORACLE:
                paramts.put("extras.estado", "A");
                break;
            default:
                break;

        }

        return manager.findObjectByParameterOrderList(CatEdfProp.class,
                paramts, new String[]{"orden"}, true);
    }

    @Override
    public List<CatEdfProp> getPropiedadesConstByEstado(CatEdfCategProp categoriaConst) {

        List<CatEdfProp> lista = manager.findAll(Querys.getCatEdfPropByEstadoAndCategoria, new String[]{"estado", "idCategoria"}, new Object[]{'A', categoriaConst.getId()});
        return lista;
    }

    @Override
    public void guardarCaracteristica(CatEdificacionPisosDet caract) {
        try {
            manager.persist(caract);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public Boolean actualizarFotos(FotoPredio fotoPredio) {
        try {
            return manager.update(fotoPredio);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public List<CatProvincia> provincias() {
        try {
            return manager.findAllObjectOrder(CatProvincia.class,
                    new String[]{"descripcion"}, true);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public Boolean actualizarLindero(CatPredioLinderos c) {
        try {
            return manager.update(c);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public Boolean actualizarPredio(CatPredio px) {
        try {
            return manager.update(px);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    @Override
    public CatPredioPropietario guardarPropietarioCiudadano(CatPredioPropietario p, String usuario) {
        try {
            if (p != null) {
                CatPredioPropietario titular = this.existePropietarioPredioTitular(p.getPredio());
                if (titular != null && p.getTipo().getOrden() != 1) {
                    BigDecimal subtract = BigDecimal.ZERO;
                    if (p.getEstado().equalsIgnoreCase("A")) {
                        subtract = titular.getPorcentajePosecion().subtract(p.getPorcentajePosecion());
                    } else {
                        subtract = titular.getPorcentajePosecion().add(p.getPorcentajePosecion());
                    }
                    titular.setPorcentajePosecion(subtract);
                    manager.persist(titular);
                }
                if (p.getEnte() != null) {
                    CatEnte persist = (CatEnte) manager.persist(p.getEnte());
                    p.setEnte(persist);
                }
                if (p.getId() != null) {
                    manager.persist(p);
                    Hibernate.initialize(p);
                } else {
                    p = (CatPredioPropietario) manager.persist(p);
                }
                return p;
            }
        } catch (HibernateException e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CtlgItem getItemByCatalagoOrder(String prediobloquerevestpiso, BigInteger orden) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("orden", orden.intValue());
            paramt.put("catalogo", getCatalogoNombre(prediobloquerevestpiso));

            return manager.findObjectByParameter(CtlgItem.class,
                    paramt);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CtlgItem getDefaultItem(String catalogo) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            CtlgCatalogo cc = getCatalogoNombre(catalogo);
            if (cc == null) {
                return null;
            }
            paramt.put("catalogo", cc);
            paramt.put("isDefault", Boolean.TRUE);

            return (CtlgItem) EntityBeanCopy.clone(manager.findObjectByParameter(CtlgItem.class,
                    paramt));

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CtlgCatalogo getCatalogoNombre(String argumento) {
        HiberUtil.newTransaction();
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("nombre", argumento);

        return manager.findObjectByParameter(CtlgCatalogo.class,
                paramt);
    }

    @Override
    public List<AclUser> getUser(GeDepartamento geDepartamento) {
        List<AclUser> users = null;
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("departamento", geDepartamento);
            paramt.put("estado", SchemasConfig.SEARCH_ESTADO);
            List<AclRol> findObjectByParameterList = manager.findObjectByParameterList(AclRol.class, paramt);
            if (Utils.isNotEmpty(findObjectByParameterList)) {
                users = new ArrayList<>();
                for (AclRol ar : findObjectByParameterList) {
                    if (Utils.isNotEmpty((List<?>) ar.getAclUserCollection())) {
                        for (AclUser au : ar.getAclUserCollection()) {
                            if (!users.contains(au)) {
                                users.add(au);
                            }
                        }
                    }
                }
                for (AclUser aclUser : users) {
                    if (aclUser.getEnte() != null) {
                        Map<String, Object> pm = new HashMap<>();
                        pm.put("ente", aclUser.getEnte());
                        aclUser.getEnte().setEnteCorreoCollection(manager.findObjectByParameterList(EnteCorreo.class, pm));
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return users;
    }

    @Override
    public CatPredioObraInterna
            getPredioObraInternaById(Long valueOf) {
        return manager.find(CatPredioObraInterna.class,
                valueOf);
    }

    @Override
    public CatPredioObraInterna guardarObraInterna(CatPredioObraInterna p, String usuario) {
        try {
            if (p.getId() == null) {
                return (CatPredioObraInterna) manager.persist(p);
            } else {
                manager.persist(p);
                return p;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public boolean existePropietarioPredioCiudadano(CatPredio predio, String ciuCedRuc) {

        String[] param = {"idPredio", "estado"};
        Object[] values = {predio.getId(), "A"};
        List<CatPredioPropietario> result = manager.findAllEntCopy(Querys.existenPropietarios, param, values);

        if (result != null) {
            if (!result.isEmpty()) {
                if (result.stream().filter((p) -> (p.getCiuCedRuc() != null)).anyMatch((p) -> (p.getCiuCedRuc().equals(ciuCedRuc)))) {
                    return true;
                }
            }
        }
        return false;

//        Map paramt = new HashMap<>();
//        paramt.put("predio", predio);
//        paramt.put("estado", "A");
//        List<CatPredioPropietario> l = manager.findObjectByParameterList(CatPredioPropietario.class,
//                paramt);
//        for (CatPredioPropietario p : l) {
//            if (p.getCiuCedRuc().compareTo(ciuCedRuc) == 0) {
//                return true;
//            }
//        }
//        return false;
    }

    @Override
    public Short obtenerNumEdificacion(CatPredio predio) {
        Short num_temp = null;
        try {
            Short num_edif_temp = Short.valueOf((String.valueOf(manager.find(Querys.getNumeroEdificacionesByPredio, new String[]{"predioId"}, new Object[]{predio.getId()}))));
            if (num_edif_temp != null) {
                num_temp = (short) (num_edif_temp + 1);
            } else {
                num_temp = new Short("1");

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return num_temp;
    }

    @Override
    public List<CatPredioEdificacionProp> getCaracteristicasEdificacion(CatPredioEdificacion edf) {
        try {
            Map paramt = new HashMap<>();
            paramt.put("edificacion", edf);
            paramt.put("estado", SchemasConfig.SEARCH_ESTADO);

            return manager.findObjectByParameterList(CatPredioEdificacionProp.class,
                    paramt);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public List<CatPredioEdificacionProp> getCaracteristicasEdificacionByEstado(CatPredioEdificacion edf) {
        try {

            return manager.findAll(Querys.getEdificacionesPropsByEdificacionAndEstado, new String[]{"idEdificacion", "estado"}, new Object[]{edf.getId(), Boolean.TRUE});

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public List<CatParroquia> getProrroquias(Long idCanton) {
        if (idCanton == null) {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("idCanton", getCantonDefault());
            paramt.put("estado", Boolean.TRUE);

            return (List<CatParroquia>) EntityBeanCopy.clone(manager.findObjectByParameterOrderList(CatParroquia.class,
                    paramt, new String[]{"idCanton"}, true));
        } else {
//            return (List<CatParroquia>) EntityBeanCopy.clone(propHoriz.getFichaServices().getCatPerroquiasListByCanton(idCanton));
            return null;
        }
    }

    @Override
    public CatCanton getCantonDefault() {
        System.out.println("CANTON " + SisVars.CANTON + " >> PROVINCIA " + SisVars.PROVINCIA);
        return (CatCanton) manager.find(Querys.getParroquiasByCanton, new String[]{"codigoNacional", "codNac"}, new Object[]{SisVars.CANTON, SisVars.PROVINCIA});
    }

    @Override
    public List<CatCiudadela> getCiudadelasByTipoConjunto(CatTipoConjunto tipoConjunto) {
        try {
            if (tipoConjunto != null) {
                Map<String, Object> paramt = new HashMap<>();
                paramt.put("codTipoConjunto", tipoConjunto);

                return (List<CatCiudadela>) EntityBeanCopy.clone(manager.findObjectByParameterOrderList(CatCiudadela.class,
                        paramt, new String[]{"nombre"}, true));

            } else {
                return (List<CatCiudadela>) manager.findAllEntCopy(CatCiudadela.class
                );

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredio savePredio(CatPredio p) {
        try {
            HiberUtil.newTransaction();
            if (p != null) {
                if (p.getId() == null) {
                    return (CatPredio) manager.persist(p);
                } else {
                    manager.persist(p);
                    manager.getSession().refresh(p);
                    return p;

                }
            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredio getPredioByClaveCat(String claveCat) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("claveCat", claveCat);
            return manager.findObjectByParameter(CatPredio.class, paramt);
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public CatPredio getPredioByClaveCat(String claveCat, String estado) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("claveCat", claveCat);
            paramt.put("estado", estado);
            return manager.findObjectByParameter(CatPredio.class, paramt);
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatPredio getPredioByClaveCatAnt(String claveCatAnt) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("predialant", claveCatAnt);

            return manager.findObjectByParameter(CatPredio.class,
                    paramt);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public CatPredio getPredioByClaveCatAnt(String claveCatAnt, String estado) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("predialant", claveCatAnt);
            paramt.put("estado", estado);

            return manager.findObjectByParameter(CatPredio.class,
                    paramt);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public List<CtlgItem> getListadoCultivos(Integer padreItem) {
        if (padreItem != null && padreItem > 0) {
            return manager.findAllEntCopy(Querys.getCtlgItemaByCultivos, new String[]{"padre"}, new Object[]{padreItem});
        }
        return null;
    }

    @Override
    public List<CtlgItem> getListadoItemsCultivos(Integer hijoItem) {
        if (hijoItem != null && hijoItem > 0) {
            return manager.findAllEntCopy(Querys.getCtlgItemaByCultivosHijos, new String[]{"hijo"}, new Object[]{hijoItem});
        }
        return null;
    }

    @Override
    public List<FotoPredio> getFotosBloque(CatPredioEdificacion bloq) {
        try {
            return manager.findAll(Querys.getFotosIdPredioBloque, new String[]{"bloque"}, new Object[]{bloq.getId()});

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public List<CatPredioPropietario> getPropietarios(String cuiCiRuc) {
        try {
            Map<String, Object> param = new HashMap<>();
            param.put("ciuCedRuc", cuiCiRuc);
            param.put("estado", "A");

            return manager.findObjectByParameterList(CatPredioPropietario.class,
                    param);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public CatParroquia getCatParroquia(Short parroquia) {
        try {
            Map<String, Object> pm = new HashMap<>();
            pm.put("idCanton", this.getCantonDefault());
            pm.put("codNac", parroquia);

            return manager.findObjectByParameter(CatParroquia.class,
                    pm);

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public GeDepartamento getDptoByName(String nombre) {

        List<GeDepartamento> result = manager.findAll(Querys.getDptoByName, new String[]{"nombre"}, new Object[]{nombre});

        if (!result.isEmpty()) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public List<CatPredio> getPrediosByPropietarios(String cuiCiRuc) {
        try {
            List<CatPredio> temp = new ArrayList<>();
            List<CatPredioPropietario> propietarios = this.getPropietarios(cuiCiRuc);
            if (Utils.isNotEmpty(propietarios)) {
                for (CatPredioPropietario propietario : propietarios) {
                    CatPredio predio = propietario.getPredio();
                    if (!temp.contains(predio)) {
                        temp.add(predio);
                    }
                }
                return temp;
            } else {
                return null;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public List<CatPredio> getPrediosSinMatrices(String cuiCiRuc) {
        try {
            List<CatPredio> temp = new ArrayList<>();
            List<CatPredioPropietario> propietarios = this.getPropietarios(cuiCiRuc);
            if (Utils.isNotEmpty(propietarios)) {
                for (CatPredioPropietario propietario : propietarios) {
                    CatPredio predio = propietario.getPredio();
                    if (!predio.getFichaMadre()) {
                        if (!temp.contains(predio)) {
                            if (predio.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
                                temp.add(predio);
                            }
                        }
                    }
                }
                return temp;
            } else {
                return null;

            }
        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public Boolean subirFotoPredio(ByteArrayInputStream byteArrayInputStream, String nombreFoto, String contentType, CatPredio predio) {
        try {
            Long fileId = fserv.uploadFile(byteArrayInputStream, nombreFoto, contentType);
            fotoBean.setNombre(nombreFoto);
            fotoBean.setPredioId(predio.getNumPredio().longValue());
            fotoBean.setIdPredio(predio.getId());
            fotoBean.setContentType(contentType);
            fotoBean.setFileId(fileId);
            fotoBean.saveFoto();
            return true;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    @Override
    public Boolean subirFotoBloque(ByteArrayInputStream byteArrayInputStream, String nombreFoto, String contentType, CatPredioEdificacion bloque) {
        try {
            Long fileId = fserv.uploadFile(byteArrayInputStream, nombreFoto, contentType);
            fotoBean.setNombre(nombreFoto);
            fotoBean.setPredioId(bloque.getPredio().getNumPredio().longValue());
            fotoBean.setBloque(bloque.getId());
            fotoBean.setContentType(contentType);
            fotoBean.setFileId(fileId);
            fotoBean.saveFotoBloque();
            return true;

        } catch (Exception e) {
            Logger.getLogger(CatastroEjb.class
                    .getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    @Override
    public List<FormatoReporte> getFormatosInformes(Boolean estado) {
        try {
            Map<String, Object> pm = new HashMap<>();
            pm.put("estado", estado);
            return manager.findObjectByParameterList(FormatoReporte.class, pm);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            return null;
        }
    }

    @Override
    public List<AclUser> getUserActivos() {
        try {
            Map<String, Object> pm = new HashMap<>();
            pm.put("sisEnabled", SchemasConfig.SEARCH_ESTADO);
            return manager.findObjectByParameterList(AclUser.class, pm);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
            return null;
        }
    }

    @Override
    public boolean existePropietarioPredioCiudadano(CatPredio predio, String ciuCedRuc, CtlgItem tipoPropietario) {
        Map<String, Object> pm = new HashMap<>();
        pm.put("predio", predio);
        pm.put("estado", "A");
        pm.put("tipo", tipoPropietario);
        pm.put("ciuCedRuc", ciuCedRuc);
        CatPredioPropietario result = manager.findObjectByParameter(CatPredioPropietario.class, pm);

        return result != null;
    }

    @Override
    public CatPredioPropietario existePropietarioPredioTitular(CatPredio predio) {
        List<CtlgItem> tipoPropietarios = catalogos.findCatalogoItem("predio.propietario.tipo");
//        List<CtlgItem> tipoPropietarios = propHoriz.getCtlgItem("predio.propietario.tipo");
        if (Utils.isEmpty(tipoPropietarios)) {
            LOG.log(Level.WARNING,
                    "No ha ingresado el catalogo 'predio.propietario.tipo' debe poner en el sigiente orden:"
                    + " 1 >> Titular y 2 >> Accionista");
        }
        CtlgItem tipoPropietario = null;
        for (CtlgItem tp : tipoPropietarios) {
            if (tp.getOrden() == 1) { // Order 1 es para el titular.
                tipoPropietario = tp;
                break;
            }
        }
        Map<String, Object> pm = new HashMap<>();
        pm.put("predio", predio);
        pm.put("estado", "A");
        pm.put("tipo", tipoPropietario);
//        CatPredioPropietario result = manager.findObjectByParameter(CatPredioPropietario.class, pm);
        List<CatPredioPropietario> result = manager.findObjectByParameterOrderList(CatPredioPropietario.class,
                pm, new String[]{"id"}, true);
//        return (CatPredioPropietario) EntityBeanCopy.clone(result);
        if (Utils.isNotEmpty(result)) {
            return result.get(0);
        } else {
            return null;
        }
    }

    @Override
    public CatPredio valorarPredio(CatPredio predio, Integer tipoProcedimiento) {
        String claveCat = predio.getPredialant();
        try {
            List<Object> list = new ArrayList<>();
            list.add("'" + claveCat + "'");
            Object valor = null;
            if (tipoProcedimiento == 1 || tipoProcedimiento == 3) {
                try {
                    if (predio.getTipoPredio().startsWith("U")) {
                        valor = this.manager.executeFunction(SchemasConfig.APP1 + ".sp_valor_terreno_urbano", list, Boolean.FALSE);
                    } else {
                        valor = this.manager.executeFunction(SchemasConfig.APP1 + ".sp_coef_terreno_rural", list, Boolean.FALSE);
                        BigDecimal aval = BigDecimal.ZERO;
                        for (CatPredioClasificRural cpcr : predio.getCatPredioClasificRuralCollection()) {
                            if (cpcr.getEstado().equalsIgnoreCase("A")) {
                                aval = aval.add(cpcr.getValorTerreno() == null ? BigDecimal.ZERO : cpcr.getValorTerreno());
                            }
                        }
                        if (valor != null) {
                            valor = new BigDecimal(valor.toString()).multiply(aval).setScale(2, RoundingMode.HALF_UP);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error al ejecutar terreno con clave " + claveCat, e);
                }
                if (valor != null) {
                    predio.setAvaluoSolar(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));
                }
            } else {
                try {
                    valor = this.manager.executeFunction(SchemasConfig.APP1 + ".sp_valor_edificacion", list, Boolean.FALSE);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error al ejecutar >> sp_valor_edificacion con clave " + claveCat, e);
                }
                if (valor != null) {
                    predio.setAvaluoConstruccion(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));
                }
            }
            if (tipoProcedimiento == 3) {
                try {
                    valor = this.manager.executeFunction(SchemasConfig.APP1 + "sp_valor_edificacion", list, Boolean.FALSE);
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error al ejecutar >> sp_valor_edificacion con clave " + claveCat, e);
                }
                if (valor != null) {
                    predio.setAvaluoConstruccion(new BigDecimal(valor.toString()).setScale(2, RoundingMode.HALF_UP));
                }
            }
            System.out.println(predio.getTipoPredio() + " Valorando "
                    + (tipoProcedimiento == 3 ? "Todo" : (tipoProcedimiento == 1 ? "Terreno" : "Edificacion"))
                    + " Clave Catastral: " + claveCat + ", Valor: " + valor + " Aval Solar " + predio.getAvaluoSolar());
            if (valor != null) {
                predio.setAvaluoMunicipal(BigDecimal.ZERO);
                predio.setAvaluoMunicipal((predio.getAvaluoSolar() == null ? BigDecimal.ZERO : predio.getAvaluoSolar())
                        .add(predio.getAvaluoObraComplement() == null ? BigDecimal.ZERO : predio.getAvaluoObraComplement())
                        .add(predio.getAvaluoConstruccion() == null ? BigDecimal.ZERO : predio.getAvaluoConstruccion()));
                predio.setBaseImponible(predio.getAvaluoMunicipal());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al Valoracion clave: " + claveCat, e);
        }
        return predio;
    }

    @Override
    public BigInteger getNumCertificado() {
        try {
            Object obj = manager.getNativeQuery("SELECT nextval('" + SchemasConfig.APP1 + ".num_certificado')");
            if (obj != null) {
                return new BigInteger(obj.toString());
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al generar numero de certificado", e);
        }
        return null;
    }

    @Override
    public List<ModelUsuarios> getCertificadosPorUsuario(String tempFechaInicio, String time) {
        try {
            List<ModelUsuarios> sd = this.manager.nativeQuery(Querys.getCertificadosVendidoUsuario, new Object[]{tempFechaInicio, time}, ModelUsuarios.class);
            if (Utils.isNotEmpty(sd)) {
                return sd;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al generar numero de certificado", e);
        }
        return null;
    }

    @Override
    public List<ModelUsuarios> getCertificadosPorUsuario(String dateFormatPattern, String dateFormatPattern0, String usuario) {
        try {
            List<ModelUsuarios> sd = this.manager.nativeQuery(Querys.getCertificadosVendidoUsuarioPorUsuario,
                    new Object[]{usuario, dateFormatPattern, dateFormatPattern0}, ModelUsuarios.class);
            if (Utils.isNotEmpty(sd)) {
                return sd;
            } else {
                return null;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al generar numero de certificado.", e);
        }
        return null;
    }

    @Override
    public List<HistoricoTramites> getTramitesPredio(CatPredio predio) {
        if (Objects.nonNull(predio)) {
            java.util.Map<String, Object> pm = new HashMap<>();
            pm.put("numPredio", predio.getNumPredio());
            pm.put("estado", "Pendiente");
            List<HistoricoTramites> hts = manager.findObjectByParameterList(HistoricoTramites.class, pm);
            pm = new HashMap<>();
            pm.put("predio", predio);
            List<HistoricoTramiteDet> dets = manager.findObjectByParameterList(HistoricoTramiteDet.class, pm);
            if (Utils.isEmpty(hts)) {
                hts = new ArrayList<>();
            }
            if (Utils.isNotEmpty(dets)) {
                for (HistoricoTramiteDet det : dets) {
                    if (!hts.contains(det.getTramite())) {
                        hts.add(det.getTramite());
                    }
                }
            }
            return hts;
        } else {
            return null;
        }
    }

    @Override
    public boolean existePropietariosPredio(CatPredio predio) {
        String[] param = {"idPredio", "estado"};
        Object[] values = {predio.getId(), "A"};
        List<CatPredioPropietario> result = manager.findAllEntCopy(Querys.existenPropietarios, param, values);
        return Utils.isNotEmpty(result);
    }

    @Override
    public List<ModelDepuracionEntes> getObjectosAsociados(List<CatEnte> listEntesSel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServiceLists propiedadHorizontal() {
        return catalogoList;
    }

    @Override
    public CatEnte buscarCliente(String identificacion) {
        if (identificacion == null) {
            return null;
        }
        if (identificacion.length() < 13) {
            return catalogoList.buscarClienteDinardap(identificacion, Boolean.TRUE);
        } else {
            return catalogoList.buscarClienteDinardap(identificacion, Boolean.FALSE);
        }
    }

    public List<FotoPredio> getFotosPredio(CatPredio predio) {
        try {
            List<FotoPredio> fotos = null;
            if (predio.getPredioRaiz() == null) {
                fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getId()});
            } else {
                fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getPredioRaiz().longValue()});
            }
            return fotos;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

}
