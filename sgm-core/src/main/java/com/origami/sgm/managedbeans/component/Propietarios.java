/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans.component;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.config.MainConfig;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.managedbeans.catastro.FichaPredial;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatNacionalidad;
import com.origami.sgm.entities.CatPais;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.EnteCorreo;
import com.origami.sgm.entities.EnteTelefono;
import com.origami.sgm.entities.GeDocumentos;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.lazymodels.CatEnteLazy;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.ejbs.censocat.UploadDocumento;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import util.Faces;
import util.JsfUti;
import util.Messages;
import util.PhoneUtils;
import util.Utils;

/**
 *
 * @author Angel Navarro, CarlosLoorVargas
 * @date 03/11/2016
 */
@Named
@ViewScoped
public class Propietarios implements Serializable {

    protected static final Logger LOG = Logger.getLogger(Propietarios.class.getName());

    protected String correo;
    protected String telefono;
    protected Boolean nuevo;
    protected Boolean esAnterior = Boolean.FALSE;
    protected Boolean edicion;
    protected Boolean tipoSelect;
    protected Boolean closeDialog = false;
    protected Boolean closeDialog1 = false;
    private Boolean datosTemp = false;
    private Boolean esTitular = false;
    private Boolean existeTitular = false;
    private Boolean persistir = true;

    protected GeDocumentos saveDocumento;
    protected Integer tipo;
    protected CatPredioPropietario pro;
    protected List<EnteTelefono> eliminarTelefono;
    protected List<EnteCorreo> eliminarCorreo;
    // Parametros
    protected String idCatPredioPro;
    protected String idPredio;
    protected String esNuevo;
    protected String editar;
    protected String anterior;
    protected List<CatPredioPropietario> propietariosPredio;
    protected CatEnte representanteLegal;
    protected CatEnte conyuge;
    protected CatEnteLazy entes;
    protected String mensajeDoc;
    protected MainConfig config;
    private String ciuCedRuc;
    private BigDecimal porcenjPart = BigDecimal.ZERO;
    private BigDecimal porcenjEdicionInicial;

    @javax.inject.Inject
    protected CatastroServices ejb;
    @Inject
    protected UserSession us;
    @javax.inject.Inject
    protected Entitymanager manager;

    @Inject
    protected UploadDocumento documentoBean;
    @Inject
    protected OmegaUploader fserv;
    @Inject
    protected ServletSession ss;
    @Inject
    protected ServiceDataBaseIb dataBaseIb;

//    @PostConstruct
    public void initView() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                nuevo = Boolean.valueOf(esNuevo);
                edicion = Boolean.valueOf(editar);
                if (this.ss.tieneParametro("propietarios")) {
                    propietariosPredio = (List<CatPredioPropietario>) this.ss.getParametro("propietarios");
                }
                conyuge = new CatEnte();
                if (nuevo) {
                    if (idPredio == null) {
                        return;
                    }
                    pro = new CatPredioPropietario();
                    pro.setPredio(this.ejb.getPredioId(Long.valueOf(idPredio)));
                    pro.setEnte(new CatEnte());
                    pro.setEsResidente(false);
                    pro.setCiudadano(new CiuCiudadano());
                    pro.setUsuario(us.getName_user());
                    pro.setPorcentajePosecion(BigDecimal.ZERO);
                    pro.setEstado("A");
                    conyuge = new CatEnte();
                    if (Utils.isNotEmpty(this.propietariosPredio)) {
                        this.pro.setCopropietario(true);
                        JsfUti.update("formProp:porcePosession");
                    }
                    representanteLegal = new CatEnte();
                } else {
                    if (idCatPredioPro == null) {
                        return;
                    }
                    pro = ejb.getPredioPropietarioById(Long.valueOf(idCatPredioPro));
                    esTitular = pro.getTipo().getOrden() == 1;
//                    pro.setPredio((CatPredio) EntityBeanCopy.clone(pro.getPredio()));
                    CiuCiudadano ciudadanoEnte = (CiuCiudadano) dataBaseIb.getCiudadano(pro.getCiuCedRuc());
                    this.ejb.buscarCliente(pro.getCiuCedRuc());
                    if (ciudadanoEnte != null) {
                        pro.setCiudadano(ciudadanoEnte);
                        ciuCedRuc = ciudadanoEnte.getCiuCedRuc();
                    }
                    pro.setPorcentajeAvalDivision(pro.getPorcentajePosecion());
                    porcenjPart = BigDecimal.valueOf(100l);
                    this.porcenjEdicionInicial = this.pro.getPorcentajePosecion();
                    if (pro.getEnte() == null) {
                        pro.setEnte(new CatEnte());
                    }
                    buscarConyuge(pro.getEnte());
                }
                config = new MainConfig();
                if (nuevo) {
                    pro.setPorcentajePosecion(BigDecimal.valueOf(100.00));
                }
                this.existeTitular();
                if (ss.getParametros() != null && ss.getParametros().get("DatosTemp") != null) {
                    datosTemp = Boolean.valueOf(ss.getParametros().get("DatosTemp").toString());
                }
            }
        } catch (NumberFormatException ne) {
            LOG.log(Level.SEVERE, null, ne);
        }

    }

    /**
     *
     * @param tipo 1 conyuge no valida propietario,
     */
    public void buscarEnte(int tipo) {
        String temp = null;
        switch (tipo) {
            case 1:
                temp = conyuge.getCiRuc();
            default:
                temp = ciuCedRuc;
        }
        System.out.println("Tipo " + tipo + " cd " + temp);
        if (temp != null && !temp.isEmpty()) {
            try {
                Map paramt = new HashMap<>();
                paramt.put("ciRuc", temp);
                CatEnte newEnt = ejb.propiedadHorizontal().getCatEnteByParemt(paramt);
                if (newEnt.getId() != null) {
                    if (tipo != 1) {
                        if (ejb.existePropietarioPredioCiudadano(pro.getPredio(), temp, pro.getTipo())) {
                            JsfUti.messageInfo(null, "Cliente ya fue agregado al predio", "");
                            return;
                        }
                        if (!persistir) {
                            if (Utils.isNotEmpty(this.propietariosPredio)) {
                                for (CatPredioPropietario p : this.propietariosPredio) {
                                    if (p.getCiuCedRuc() != null) {
                                        if (p.getCiuCedRuc().compareTo(pro.getCiuCedRuc()) == 0) {
                                            JsfUti.messageInfo(null, "Cliente ya fue agregado al predio", "");
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    JsfUti.messageInfo(null, "Info", "Cliente Encontrado");
                    switch (tipo) {
                        case 1:
                            conyuge = newEnt;
                        default:
                            if (newEnt.getTipoDocumento() == null) {
                                newEnt.setTipoDocumento(pro.getEnte().getTipoDocumento());
                            }
                            pro.setEnte(newEnt);
                            pro.setCiuCedRuc(newEnt.getCiRuc());
                            buscarConyuge(newEnt);
                            pro.setCiuCedRuc(temp);
                    }
                } else {
                    JsfUti.messageInfo(null, Messages.cedulaCIinvalida, "");
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, null, e);
            }
        } else {
            JsfUti.messageError(null, "Debe ingresar el número de identificación", "");
        }
    }

    private void buscarConyuge(CatEnte e) {
        if (e.getConyuge() != null) {
            Map paramt = new HashMap<>();
            paramt.put("id", e.getConyuge().longValue());
            conyuge = ejb.propiedadHorizontal().getCatEnteByParemt(paramt);
        }
    }

    public void agregarPropietario() {
        try {
            if (pro == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a guardar");
                return;
            }
            if (pro.getCiuCedRuc() == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a guardar");
                return;
            }
            if (!verificarPorcentajeParticipacion()) {
                return;
            }
            String nombres = pro.getEnte().getNombreCompleto()
                    + (pro.getOtros() == null ? "" : " " + pro.getOtros());
            pro.setNombresCompletos(nombres);
            pro.setFecha(new Date());
            pro.setCiuCedRuc(pro.getEnte().getCiRuc());
            if (conyuge != null && conyuge.getId() != null) {
                if (pro.getEnte() != null) {
                    pro.getEnte().setConyuge(BigInteger.valueOf(conyuge.getId()));
                }
            }
            if (datosTemp) {
                pro.setEstado(EstadosPredio.TEMPORAL);
                if (pro.getId() != null) {
                    pro = ejb.guardarPropietarioCiudadano(pro, us.getName_user());
                }
            } else {
                pro.setEstado(EstadosPredio.ACTIVO);
                // Mantenemos los datos en memoria
//                pro = ejb.guardarPropietarioCiudadano(pro, us.getName_user());
            }

            RequestContext.getCurrentInstance().closeDialog(pro);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public void agregarPropietarioUnSave() {
        try {
            if (pro == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a guardar");
                return;
            }
            if (pro.getCiuCedRuc() == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a guardar");
                return;
            }
            if (pro.getCopropietario()) {
                if (!verificarPorcentajeParticipacion()) {
                    return;
                }
            }
            String nombres = pro.getEnte().getNombreCompleto()
                    + (pro.getOtros() == null ? "" : " " + pro.getOtros());
            pro.setNombresCompletos(nombres);
            pro.setFecha(new Date());
            pro.setCiuCedRuc(pro.getEnte().getCiRuc());
            if (datosTemp) {
                pro.setEstado(EstadosPredio.TEMPORAL);
            } else {
                pro.setEstado(EstadosPredio.ACTIVO);
            }
            RequestContext.getCurrentInstance().closeDialog(pro);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    private Boolean existeDocumento() {
        if (tipo == null) {
            return true;
        }
        switch (tipo) {
            case 1:
                if (saveDocumento == null) {
                    JsfUti.executeJS("PF('dlgSubirDocumento').show()");
                    closeDialog1 = true;
                    return false;
                }
                break;
            case 2:
                if (!pro.getEnte().getDiscapacidad().getValor().equalsIgnoreCase("Ninguna")) {
                    if (saveDocumento == null) {
                        JsfUti.executeJS("PF('dlgSubirDocumento').show()");
                        closeDialog1 = true;
                        return false;
                    }
                }
                break;
            case 3:
                if (saveDocumento == null) {
                    JsfUti.executeJS("PF('dlgSubirDocumento').show()");
                    closeDialog1 = true;
                    return false;
                }
                break;
        }
        return true;
    }

    public void cerrar() {
        RequestContext.getCurrentInstance().closeDialog(null);
    }

    public void modificarPropietario() {
        try {
            if (pro == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a actualizar");
                return;
            }
            if (pro.getCopropietario()) {
                if (!verificarPorcentajeParticipacion()) {
                    return;
                }
            }
            if (pro.getEnte() != null) {
                if (pro.getEnte().getEsTerceraEdad()) {
                    if (config.getFichaPredial().getRenderDialogUploadDocument()) {
                        if (saveDocumento == null) {
                            JsfUti.executeJS("PF('dlgSubirDocumento').show()");
                            closeDialog = true;
                            return;
                        }
                    }
                }
                if (pro.getEnte().getDiscapacidad() != null) {
                    if (config.getFichaPredial().getRenderDialogUploadDocument()) {
                        if (!pro.getEnte().getDiscapacidad().getValor().equalsIgnoreCase("Ninguna")) {
                            if (saveDocumento == null) {
                                JsfUti.executeJS("PF('dlgSubirDocumento').show()");
                                closeDialog = true;
                                return;
                            }
                        }
                    }
                }
            }
            String nombres = pro.getEnte().getNombreCompleto()
                    + (pro.getOtros() == null ? "" : " " + pro.getOtros());
            pro.setNombresCompletos(nombres);
            pro.setModificado(us.getName_user());
            if (conyuge != null) {
                pro.getEnte().setConyuge(BigInteger.valueOf(conyuge.getId()));
            }
            if (datosTemp) {
                pro.setEstado(EstadosPredio.TEMPORAL);
                if (pro.getId() != null) {
                    pro = ejb.guardarPropietario(pro, us.getName_user());
                }
            } else {
                pro.setEstado(EstadosPredio.ACTIVO);
                //Mantenemos los datos en memoria
//                pro = ejb.guardarPropietario(pro, us.getName_user());
            }

            RequestContext.getCurrentInstance().closeDialog(pro);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public void modificarPropietarioUnsave() {
        try {
            if (pro == null) {
                JsfUti.messageError(null, "Error", "No se encontro registro a actualizar");
                return;
            }
            if (pro.getCopropietario()) {
                if (!verificarPorcentajeParticipacion()) {
                    return;
                }
            }
            String nombres = pro.getCiudadano().getCiuNombreCompleto()
                    + (pro.getOtros() == null ? "" : " " + pro.getOtros());
            pro.setNombresCompletos(nombres);
            pro.setModificado(us.getName_user());
            if (datosTemp) {
                pro.setEstado(EstadosPredio.TEMPORAL);
            } else {
                pro.setEstado(EstadosPredio.ACTIVO);
            }
            RequestContext.getCurrentInstance().closeDialog(pro);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    public Boolean verificarPorcentajeParticipacion() {
        if (pro.getCopropietario()) {
            if (pro.getPorcentajePosecion().doubleValue() == 0) {
                JsfUti.messageInfo(null, "Nota!", "El porcentaje de posesión no puede ser igual a Cero");
                return false;
            }
        }
        if (pro.getTipo() == null) {
            JsfUti.messageInfo(null, "Nota!", "Debe Ingresar el tipo de propietario...");
            return false;
        }

        if (existeTitular() && pro.getTipo().getOrden() == 1) {
            JsfUti.messageError(null, "Error", "Ya existe un Titular agregado.");
            return false;
        }
        if (pro.getTipo().getOrden() != 1 && !existeTitular()) {
            JsfUti.messageError(null, "Error", "Debe haber por lo menos un titular.");
            return false;
        }
        if (porcenjPart.doubleValue() > 0 && !esTitular) {
            if (pro.getPorcentajePosecion().doubleValue() > porcenjPart.doubleValue()) {
                JsfUti.messageError(null, "Error", "Porcentaje Ingresado en mayor al del Titular.");
                this.pro.setPorcentajePosecion(porcenjPart);
                return false;
            }
        }
        BigDecimal porcentajeTemp = this.sumaProcentajeParticipacion();
        if (porcentajeTemp.doubleValue() > 100) {
            JsfUti.messageInfo(null, "Nota!", "La suma del porcentaje de parcipacion de los coopropietarios no debe exceder el 100%, la suma es: " + porcentajeTemp);
            pro.setPorcentajePosecion(BigDecimal.ZERO);
            return false;
        }
        if (!datosTemp && pro.getId() == null) {
            if (existeTitular() && pro.getTipo().getOrden() == 1) {
                JsfUti.messageError(null, "Error", "Ya existe un Titular agregado.");
                return false;
            }
        }
        if (pro.getTipo().getOrden() == 1 && pro.getId() == null
                && pro.getPorcentajePosecion() != null && pro.getPorcentajePosecion().doubleValue() <= 0) {
            pro.setPorcentajePosecion(BigDecimal.valueOf(100L));
        }

        return true;
    }

    public Boolean existeTitular() {
        if (Utils.isNotEmpty(this.propietariosPredio)) {
            for (CatPredioPropietario prop : this.propietariosPredio) {
                if (prop.getEstado().contains(EstadosPredio.ACTIVO)) {
                    if (nuevo) {
                        if (prop.getTipo().getOrden() == 1) {
                            porcenjPart = prop.getPorcentajePosecion();
                            if (!nuevo) {
                                porcenjPart = porcenjPart.add(porcenjEdicionInicial);
                            }
                            return true;
                        }
                    } else {
                        if (prop.getCiuCedRuc() != null) {
                            if (!prop.getCiuCedRuc().equalsIgnoreCase(pro.getCiuCedRuc())) {
                                if (prop.getTipo().getOrden() == 1) {
                                    porcenjPart = prop.getPorcentajePosecion();
                                    if (!nuevo) {
                                        porcenjPart = porcenjPart.add(porcenjEdicionInicial);
                                    }
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            porcenjPart = pro.getPorcentajePosecion();
            return false;
        }
        return false;
    }

    public BigDecimal sumaProcentajeParticipacion() {
        BigDecimal suma = BigDecimal.ZERO;
        if (Utils.isNotEmpty(this.propietariosPredio)) {
            for (CatPredioPropietario prop : this.propietariosPredio) {
                if (prop.getEstado().contains(EstadosPredio.ACTIVO)) {
                    if (prop.getCiuCedRuc() != pro.getCiuCedRuc()) {
                        suma = suma.add(prop.getPorcentajePosecion());
                    } else {
                        suma = suma.add(pro.getPorcentajePosecion());
                    }
                }
            }
        } else {
            suma = pro.getPorcentajePosecion();
        }
        return suma;
    }

    public String getCiuCedRuc() {
        return ciuCedRuc;
    }

    public void setCiuCedRuc(String ciuCedRuc) {
        this.ciuCedRuc = ciuCedRuc;
    }

    public void selecPropietarios() {
        if (propietariosPredio != null) {
            RequestContext.getCurrentInstance().closeDialog(propietariosPredio);
        }
    }

    public void agregarTelefono() {
        try {
            if (telefono == null) {
                JsfUti.messageInfo(null, "Debe Ingresar Número de Telefonico.", "");
                return;
            }
            if (!Utils.validateNumberPattern(telefono)) {
                JsfUti.messageInfo(null, "solo debe Ingresar Números", "");
                return;
            }

            if (!PhoneUtils.getValidNumber(telefono, "EC")) {
                JsfUti.messageInfo(null, "Número Telefonico invalido", "");
                return;
            }
            if (pro.getEnte().getEnteTelefonoCollection() == null) {
                pro.getEnte().setEnteTelefonoCollection(new ArrayList<EnteTelefono>());
            }
            EnteTelefono c = new EnteTelefono();
            c.setTelefono(telefono);
            c.setEnte(pro.getEnte());
            pro.getEnte().getEnteTelefonoCollection().add(c);
            telefono = null;

        } catch (Exception e) {
            Logger.getLogger(Propietarios.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    public void agregarCorreo() {
        try {
            if (correo == null) {
                JsfUti.messageInfo(null, "Debe Ingresar Correo.", "");
                return;
            }
            if (!Utils.validarEmailConExpresion(correo)) {
                JsfUti.messageInfo(null, "Correo Ingresado es invalido.", "");
                return;
            }
            if (pro.getEnte().getEnteCorreoCollection() == null) {
                pro.getEnte().setEnteCorreoCollection(new ArrayList<EnteCorreo>());
            }
            EnteCorreo c = new EnteCorreo();
            c.setEmail(correo);
            c.setEnte(pro.getEnte());
            pro.getEnte().getEnteCorreoCollection().add(c);
            correo = null;

        } catch (Exception e) {
            Logger.getLogger(Propietarios.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    public void eliminarTelefono(EnteTelefono tel) {
        try {
            if (eliminarTelefono == null) {
                eliminarTelefono = new ArrayList<>();
            }
            if (tel.getId() != null) {
                eliminarTelefono.add(tel);
            }
            pro.getEnte().getEnteTelefonoCollection().remove(tel);

        } catch (Exception e) {
            Logger.getLogger(Propietarios.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    public void eliminarCorreo(EnteCorreo corr) {
        try {
            if (eliminarCorreo == null) {
                eliminarCorreo = new ArrayList<>();
            }
            if (corr.getId() != null) {
                eliminarCorreo.add(corr);
            }
            pro.getEnte().getEnteCorreoCollection().remove(corr);

        } catch (Exception e) {
            Logger.getLogger(Propietarios.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    public List<CtlgItem> getTipoPro() {
        return ejb.propiedadHorizontal().getCtlgItem("predio.propietario.tipo");
    }

    /**
     * Obtiene la lista del catalogo ctlgItem
     *
     * @param argumento
     * @return
     */
    public List<CtlgItem> getListado(String argumento) {
        return manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{argumento});
    }

    public void buscarRes(Boolean tipoSelect) {
        this.tipoSelect = tipoSelect;
        entes = new CatEnteLazy(true);
        JsfUti.executeJS("PF('dlgEntes').show()");
    }

    public void seleccionarComprador(CatEnte ente) {
        if (tipoSelect) {
            pro.getEnte().setConyuge(BigInteger.valueOf(ente.getId()));
            conyuge = ente;
        } else {
            pro.getEnte().setRepresentanteLegal(BigInteger.valueOf(ente.getId()));
            representanteLegal = ente;
        }
    }

    public List<CatNacionalidad> getNacionalidades() {
        return ejb.getNacionalidades();
    }

    public List<CatPais> getPaises() {
        return ejb.getPaises();
    }

    public List<CtlgItem> getDiscapacidades() {
        return ejb.getItemsByCatalogo("ente.discapacidad");
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public CatPredioPropietario getPro() {
        return pro;
    }

    public void setPro(CatPredioPropietario pro) {
        this.pro = pro;
    }

    public String getIdCatPredioPro() {
        return idCatPredioPro;
    }

    public void setIdCatPredioPro(String idCatPredioPro) {
        this.idCatPredioPro = idCatPredioPro;
    }

    public String getIdPredio() {
        return idPredio;
    }

    public void setIdPredio(String idPredio) {
        this.idPredio = idPredio;
    }

    public Boolean getNuevo() {
        return nuevo;
    }

    public void setNuevo(Boolean nuevo) {
        this.nuevo = nuevo;
    }

    public String getEsNuevo() {
        return esNuevo;
    }

    public void setEsNuevo(String esNuevo) {
        this.esNuevo = esNuevo;
    }

    public List<CatPredioPropietario> getPropietariosPredio() {
        return propietariosPredio;
    }

    public void setPropietariosPredio(List<CatPredioPropietario> propietariosPredio) {
        this.propietariosPredio = propietariosPredio;
    }

    public Boolean getEdicion() {
        return edicion;
    }

    public void setEdicion(Boolean edicion) {
        this.edicion = edicion;
    }

    public String getEditar() {
        return editar;
    }

    public void setEditar(String editar) {
        this.editar = editar;
    }

    public CatEnte getRepresentanteLegal() {
        return representanteLegal;
    }

    public void setRepresentanteLegal(CatEnte representanteLegal) {
        this.representanteLegal = representanteLegal;
    }

    public CatEnte getConyuge() {
        return conyuge;
    }

    public void setConyuge(CatEnte conyuge) {
        this.conyuge = conyuge;
    }

    public CatEnteLazy getEntes() {
        return entes;
    }

    public void setEntes(CatEnteLazy entes) {
        this.entes = entes;
    }

    public void handleFileDocumentBySave(FileUploadEvent event) {
        try {
            Date d = new Date();
            File file = new File(SisVars.rutaRepotiorioArchivo + d.getTime() + event.getFile().getFileName());

            InputStream is;
            is = event.getFile().getInputstream();
            OutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            Long documentoId = fserv.uploadFile(event.getFile().getInputstream(), event.getFile().getFileName(), event.getFile().getContentType());
            documentoBean.setFechaCreacion(new Date());
            documentoBean.setNombre(event.getFile().getFileName());
            try {
                if (this.pro.getEnte().getId() == null) {
                    documentoBean.setRaiz(this.pro.getEnte().getId());
                } else {
                    documentoBean.setRaiz(0L);
                }
            } catch (Exception e) {
            }
            documentoBean.setContentType(event.getFile().getContentType());
            documentoBean.setDocumentoId(documentoId);
            switch (tipo) {
                case 1:
                    documentoBean.setIdentificacion("Tercera Edad");
                    break;
                case 2:
                    documentoBean.setIdentificacion("Discapacidad");
                    break;
                case 3:
                    documentoBean.setIdentificacion("Coopropietarios");
                    break;
            }
            saveDocumento = documentoBean.saveDocumento();
            if (closeDialog1) {
                agregarPropietario();
            }
            if (closeDialog) {
                modificarPropietario();
            }
            is.close();
            out.close();
            Faces.messageInfo(null, "Nota1", "Archivo cargado Satisfactoriamente");
        } catch (IOException e) {
            Logger.getLogger(FichaPredial.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
        switch (tipo) {
            case 1:
                mensajeDoc = "Ingrese pdf del 'DOCUMENTO DE IDENTIDAD'";
                break;
            case 2:
                mensajeDoc = "Ingrese pdf del 'CARNET DE DISCAPACIDAD'";
                break;
            case 3:
                mensajeDoc = "Ingrese pdf de la 'DECLARATORIA DE PROPIEDAD HORINZONTAL'";
                break;
        }
    }

    public String getMensajeDoc() {
        return mensajeDoc;
    }

    public void setMensajeDoc(String mensajeDoc) {
        this.mensajeDoc = mensajeDoc;
    }

    public MainConfig getConfig() {
        return config;
    }

    public void setConfig(MainConfig config) {
        this.config = config;
    }

    public Boolean getEsAnterior() {
        return esAnterior;
    }

    public void setEsAnterior(Boolean esAnterior) {
        this.esAnterior = esAnterior;
    }

    public String getAnterior() {
        return anterior;
    }

    public void setAnterior(String anterior) {
        this.anterior = anterior;
    }

    public Boolean getPersistir() {
        return persistir;
    }

    public void setPersistir(Boolean persistir) {
        this.persistir = persistir;
    }

    public Boolean getEsTitular() {
        return esTitular;
    }

    public BigDecimal getPorcenjPart() {
        return porcenjPart;
    }

    public boolean existenAccionistas() {
        if (Utils.isNotEmpty(this.propietariosPredio)) {
            return this.propietariosPredio.size() > 1;
        } else {
            return (nuevo && pro.getCopropietario());
        }
    }
}
