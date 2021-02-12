/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro.certAvaluos;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.managedbeans.catastro.PredioUtil;
import com.origami.sgm.database.Querys;
import com.origami.sgm.database.QuerysFinanciero;
import com.origami.sgm.entities.AclRol;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatCertificadoAvaluo;
import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.MsgFormatoNotificacion;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.lazymodels.CatEnteLazy;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.interfaces.SeqGenMan;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import util.Faces;
import util.GroovyUtil;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author CarlosLoorVargas
 */
@Named
@ViewScoped
public class CertAvaluos extends PredioUtil implements Serializable {

    private static final long serialVersionUID = 1L;
    @javax.inject.Inject
    protected Entitymanager manager;
    @javax.inject.Inject
    protected SeqGenMan seq;
    @javax.inject.Inject
    protected CatastroServices catastro;
    protected CatPredioLazy predios;
    @Inject
    protected OmegaUploader omegaUploader;
    protected BaseLazyDataModel<CatCertificadoAvaluo> certificados;
    protected CatCertificadoAvaluo cert;
    protected List<CatCiudadela> cdlas;
    protected CatEnteLazy solicitantes;
    @Inject
    protected UserSession sess;
    @Inject
    protected ServletSession ss;
    protected boolean contenido = false;
    protected FormatoReporte frep = null;
    protected GroovyUtil gUtil;
    protected CatPredioPropietario cpp;
    protected String path, emailDir, emailSolicitante;
    protected MsgFormatoNotificacion msg;
    protected StringBuilder sb;
    protected HashMap<String, Object> params;
    protected Boolean tipoEntidad = false;
    protected Boolean visualizarCert = false;

    @PostConstruct
    protected void load() {
        try {
            ss.instanciarParametros();
            params = new HashMap<>();
            msg = manager.find(MsgFormatoNotificacion.class, 5L);
            path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
            if (sess != null) {

                predios = new CatPredioLazy("A");
                certificados = new BaseLazyDataModel<CatCertificadoAvaluo>(CatCertificadoAvaluo.class, new String[]{"estado"}, new Object[]{true});
                cdlas = manager.findAllEntCopy(CatCiudadela.class);
                solicitantes = new CatEnteLazy();
                frep = (FormatoReporte) manager.find(Querys.getFormatoxCodigo, new String[]{"codigo"}, new Object[]{"CPU-16"});
                if (frep != null) {
                    gUtil = new GroovyUtil(frep.getFormato());
                }
            }
        } catch (Exception e) {
            Logger.getLogger(CertAvaluos.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void nuevo() {
        cert = new CatCertificadoAvaluo();
        cert.setFecha(new Date());
        cert.setEstado(Boolean.TRUE);
        Long comp = null;
        comp = (Long) manager.find(QuerysFinanciero.getRenSecuenciaMaxNumComprobante, new String[]{"anio"}, new Object[]{Utils.getAnio(new Date())});
        if (comp != null) {
            comp = comp + 1L;
            cert.setCodComprobante(BigInteger.valueOf(comp));
        } else {
            comp = 1L;
            cert.setCodComprobante(BigInteger.valueOf(comp));
        }
        sb = new StringBuilder();
    }

    public void selectSolicitante(CatEnte sol) {
        cert.setSolicitante(sol);
    }

    public void selectPredio(CatPredio pred) throws Exception {
        if (pred != null) {
            if (cert.getSolicitante() != null) {
                CatCertificadoAvaluo ca = new CatCertificadoAvaluo();
                ca.setAlicuota(pred.getAlicuotaUtil() == null ? BigDecimal.ZERO + "" : pred.getAlicuotaUtil() + "");
                ca.setAreaSolar(pred.getAreaSolar() == null ? BigDecimal.ZERO : pred.getAreaSolar());
                ca.setAvalConstruccion(pred.getAvaluoConstruccion() == null ? BigDecimal.ZERO : pred.getAvaluoConstruccion());
                ca.setAvalSolar(pred.getAvaluoSolar() == null ? BigDecimal.ZERO : pred.getAvaluoSolar());
                ca.setAvalPropiedad(pred.getAvaluoMunicipal() == null ? BigDecimal.ZERO : pred.getAvaluoMunicipal());
                ca.setCodigoActual(pred.getCodigoPredialCompleto());
                ca.setCodigoAnterior(pred.getPredialant());
                ca.setPredio(pred);

                cert.setPredio(pred);
                cert.setAlicuota(ca.getAlicuota());
                cert.setAreaSolar(ca.getAreaSolar());
                cert.setAvalConstruccion(ca.getAvalConstruccion());
                cert.setAvalSolar(ca.getAvalSolar());
                cert.setAvalPropiedad(ca.getAvalPropiedad());
                cert.setCodigoActual(ca.getCodigoActual());
                cert.setCodigoAnterior(ca.getCodigoAnterior());
                contenido = true;
                gUtil.setProperty("cert", ca);
                if (cert.getSolicitante().getEsPersona()) {
                    gUtil.setProperty("solicitante", cert.getSolicitante().getApellidos() + " " + cert.getSolicitante().getNombres());
                } else {
                    gUtil.setProperty("solicitante", cert.getSolicitante().getRazonSocial());
                }
                gUtil.setProperty("nombrecanton", SisVars.NOMBRECANTON);
            } else {
                Faces.messageWarning(null, "Advertencia", "Debe seleccionar el solicitante y el propietario respectivo");
            }
        }
    }

    public void ver() {
        if (cpp != null) {
            if (cpp.getEnte().getEsPersona()) {
                gUtil.setProperty("propietario", cpp.getEnte().getApellidos() + " " + cpp.getEnte().getNombres());
            } else {
                gUtil.setProperty("propietario", cpp.getEnte().getRazonSocial());
            }
            cert.setDetalle("<div style=\"text-align:justify\">" + gUtil.getExpression("getDetalle", null).toString() + "</div>");
            JsfUti.executeJS("PF('dlgCertificado').show()");
        } else {
            Faces.messageWarning(null, "Advertencia", "Debe seleccionar el propietario respectivo");
        }
    }

    public void guardar() {
        try {
            if (cert != null && cert.getId() == null) {
//                RenSecuenciaNumComprobante comprobante = new RenSecuenciaNumComprobante();
                cert.setUsuario(sess.getName_user());
                AclRol catRol = (AclRol) manager.find(Querys.getAclRolByNombre, new String[]{"nombre"}, new Object[]{"director_catastro"});
                for (AclUser dir : catRol.getAclUserCollection()) {
                    cert.setDirCat(dir.getEnte().getNombres() + " " + dir.getEnte().getApellidos());
                    cert.setFirmaDir(path + dir.getRutaImagen());
                    //ss.agregarParametro("firma", cert.getFirmaDir());
                    if (dir.getEnte().getEnteCorreoCollection() != null && !dir.getEnte().getEnteCorreoCollection().isEmpty()) {
                        emailDir = dir.getEnte().getEnteCorreoCollection().get(0).getEmail();
                    }
                }
                if (cert.getDirCat() == null) {
                    Faces.messageWarning(null, "Advertencia", "El director del departamento no tiene registrados sus datos personales");
                    return;
                }
                if (cert.getSolicitante().getEnteCorreoCollection() != null && !cert.getSolicitante().getEnteCorreoCollection().isEmpty()) {
                    emailSolicitante = cert.getSolicitante().getEnteCorreoCollection().get(0).getEmail();
                }
                cert = catastro.guardarCertificado(cert);
//                comprobante.setNumComprobante(cert.getCodComprobante().longValue());
//                comprobante.setAnio(Utils.getAnio(new Date()).longValue());
//                comprobante = catastro.saveRenSecuenciaNumComprobante(comprobante);

                if (cert.getId() != null) {
                    String solicitante;
                    if (cert.getSolicitante().getEsPersona()) {
                        solicitante = cert.getSolicitante().getApellidos() + " " + cert.getSolicitante().getNombres();
                    } else {
                        solicitante = cert.getSolicitante().getRazonSocial();
                    }
                    sb.append(msg.getHeader());
                    sb.append("<table style=\"height: 60px;\" width=\"314\">\n <tbody>\n");
                    if (cert.getCodComprobante() != null) {
                        sb.append("<tr>\n <td><strong>No COMPROBANTE</strong></td>\n <td>").append(cert.getCodComprobante().toString()).append("</td>\n </tr>");
                    } else {
                        sb.append("<tr>\n <td><strong>No COMPROBANTE</strong></td>\n <td>").append(cert.getIdentificacion().toString()).append("</td>\n </tr>");
                    }
                    sb.append("<tr>\n <td><strong>FECHA</strong></td>\n <td>").append(cert.getFecha().toString()).append("</td>\n </tr>");
                    sb.append("<tr>\n <td><strong>MATRICULA INMOBILIARIA</strong></td>\n <td>").append(cert.getPredio().getNumPredio()).append("</td>\n </tr>");
                    sb.append("<tr> <td><strong>SOLICITANTE</strong></td> <td>").append(solicitante.toUpperCase()).append("</td></tr>");
                    sb.append("<tr> <td><strong>REALIZADO POR</strong></td> <td>").append(cert.getUsuario().toUpperCase()).append("</td></tr>");
                    sb.append("</tbody>\n </table>");
                    sb.append("<div style=\"width: 550px; height: 10px; background-color: #ffffff;\" align=\"center\">&nbsp;</div>\n"
                            + "<div style=\"width: 550px; height: 10px; background-color: #ffffff;\" align=\"center\">&nbsp;</div>");
                    sb.append("<h2 style=\"width: 550px; height: 10px; background-color: #ffffff;\" align=\"center\"><span style=\"color: #003366;\"><strong><a style=\"color: #003366;\"title=\"Certificado\" href=\"").append(SisVars.urlPublica).append("/certificadoAvaluo?cert=").append(cert.getCodigo()).append("\" target=\"_blank\">Descargar certificado</a></strong></span></h2>");
                    sb.append(msg.getFooter());
//                    if (emailSolicitante != null) {
//                        try {
//                            new EmailUtil().sendEmail(emailSolicitante, "Certificado de avaluo No. " + cert.getId(), sb.toString(), null, emailDir);
//                        } catch (Exception e) {
//                        }
//                    } else {
//                        try {
//                            new EmailUtil().sendEmail(emailDir, "Certificado de avaluo No. " + cert.getId(), sb.toString(), null, emailDir);
//                        } catch (Exception e) {
//                        }
//                    }
                    System.out.println("ID " + cert.getId());
                    Faces.executeJS("PF('dlgNuevo').hide()");
                    Faces.executeJS("PF('dlgCertificado').hide()");
                    Faces.messageInfo(null, "Nota", "Certificado No." + cert.getId() + " generado satisfactoriamente.");
                    ss.instanciarParametros();
                    ss.setTieneDatasource(Boolean.TRUE);
                    ss.agregarParametro("id", cert.getId());
                    ss.agregarParametro("logo", path + SisVars.logoReportes);
                    ss.agregarParametro("NOMBRECANTON", path + SisVars.NOMBRECANTON);
                    ss.setNombreSubCarpeta("catastro");
                    // CARGAR FOTOS E IMAGEN DEL PREDIO
                    int numFotos = 1;
                    List<FotoPredio> fotos = null;
                    if (cert.getPredio().getPredioRaiz() == null) {
                        fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{cert.getPredio().getId()});
                    } else {
                        fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{cert.getPredio().getPredioRaiz().longValue()});
                    }
                    if (Utils.isNotEmpty(fotos)) {
                        for (FotoPredio foto : fotos) {
                            switch (numFotos) {
                                case 1:
                                    ss.agregarParametro("FachadaFrontal", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                                case 2:
                                    ss.agregarParametro("FachadaIzquierda", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                                case 3:
                                    ss.agregarParametro("FachadaDerecha", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                                case 4:
                                    ss.agregarParametro("FachadaPosterior", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                            }
                            numFotos++;
                        }
                    }
                    if (dataBaseConnect() == Boolean.TRUE) {
                        ss.setNombreReporte("CertificadoAvaluoPropiedad");
                        if (cert.getPredio().getPredioRaiz() != null) {
                            CatPredio predioRaiz = (CatPredio) manager.find(Querys.getPrediosById, new String[]{"predioID"}, new Object[]{cert.getPredio().getPredioRaiz()});
                            ss.agregarParametro("IMAGEN_PREDIO", SisVars.URLPLANOIMAGENPREDIO + predioRaiz.getNumPredio());
                        } else {
                            ss.agregarParametro("IMAGEN_PREDIO", SisVars.URLPLANOIMAGENPREDIO + cert.getPredio().getNumPredio());
                        }
                    }
                    // FIN DE CARGA DE FOTOS
                    Faces.redirectNewTab(com.origami.config.SisVars.urlbase + "Documento");
                    contenido = false;
                }
            } else {
                manager.persist(cert);
                contenido = false;
            }
        } catch (Exception e) {
            Logger.getLogger(CertAvaluos.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void editar(CatCertificadoAvaluo cert) {
        if (cert != null) {

        }
    }

    public void imprimir(CatCertificadoAvaluo cert) {
        if (cert != null) {
            int numFotos = 1;
            ss.instanciarParametros();
            ss.setTieneDatasource(Boolean.TRUE);
            List<FotoPredio> fotos = null;
            if (cert.getPredio().getPredioRaiz() == null) {
                fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{cert.getPredio().getId()});
            } else {
                fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{cert.getPredio().getPredioRaiz().longValue()});
            }
            if (Utils.isNotEmpty(fotos)) {
                for (FotoPredio foto : fotos) {
                    switch (numFotos) {
                        case 1:
                            ss.agregarParametro("FachadaFrontal", omegaUploader.streamFile(foto.getFileOid()));
                            break;
                        case 2:
                            ss.agregarParametro("FachadaIzquierda", omegaUploader.streamFile(foto.getFileOid()));
                            break;
                        case 3:
                            ss.agregarParametro("FachadaDerecha", omegaUploader.streamFile(foto.getFileOid()));
                            break;
                        case 4:
                            ss.agregarParametro("FachadaPosterior", omegaUploader.streamFile(foto.getFileOid()));
                            break;
                    }
                    numFotos++;
                }
            }

            // ss.agregarParametro("LOGO", SisVars.logoReportes);
            //ss.agregarParametro("LOGO2", SisVars.sisLogo1);
            ss.agregarParametro("id", cert.getId());

            //ss.agregarParametro("firma", cert.getFirmaDir());
            ss.agregarParametro("logo", path + SisVars.logoReportes);
            ss.setNombreSubCarpeta("catastro");
            if (dataBaseConnect() == Boolean.TRUE) {
                ss.setNombreReporte("CertificadoAvaluoPropiedad");
                if (cert.getPredio().getPredioRaiz() != null) {
                    CatPredio predioRaiz = (CatPredio) manager.find(Querys.getPrediosById, new String[]{"predioID"}, new Object[]{cert.getPredio().getPredioRaiz()});
                    ss.agregarParametro("IMAGEN_PREDIO", SisVars.URLPLANOIMAGENPREDIO + predioRaiz.getNumPredio());
                } else {
                    ss.agregarParametro("IMAGEN_PREDIO", SisVars.URLPLANOIMAGENPREDIO + cert.getPredio().getNumPredio());
                }
            } else {
                ss.setNombreReporte("CertificadoAvaluoPropiedadOracle");
                if (cert.getPredio().getPredioRaiz() != null) {
                    CatPredio predioRaiz = (CatPredio) manager.find(Querys.getPrediosById, new String[]{"predioID"}, new Object[]{cert.getPredio().getPredioRaiz()});
                    ss.agregarParametro("IMAGEN_PREDIO", SisVars.URLPLANOIMAGENPREDIO + this.claveCroquis(predioRaiz));
                } else {
                    ss.agregarParametro("IMAGEN_PREDIO", SisVars.URLPLANOIMAGENPREDIO + this.claveCroquis(cert.getPredio()));
                }
            }
            JsfUti.redirectNewTab("/sgmEE/Documento");
        } else {
            Faces.messageWarning(null, "Advertencia", "No se pudo imprimir el certificado, verificar que los datos esten correctos");
        }
    }

    public void validarComprobante() {
        try {
            if (!tipoEntidad) {
                contenido = true;
                visualizarCert = Boolean.TRUE;
            } else {
                contenido = cert.getIdentificacion() != null;
                visualizarCert = Boolean.TRUE;
            }
        } catch (Exception e) {
            Logger.getLogger(CertAvaluos.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public BaseLazyDataModel<CatCertificadoAvaluo> getCertificados() {
        return certificados;
    }

    public void setCertificados(BaseLazyDataModel<CatCertificadoAvaluo> certificados) {
        this.certificados = certificados;
    }

    public CatCertificadoAvaluo getCert() {
        return cert;
    }

    public void setCert(CatCertificadoAvaluo cert) {
        visualizarCert = Boolean.FALSE;
        this.cert = cert;
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
    }

    public List<CatCiudadela> getCdlas() {
        return cdlas;
    }

    public void setCdlas(List<CatCiudadela> cdlas) {
        this.cdlas = cdlas;
    }

    public CatEnteLazy getSolicitantes() {
        return solicitantes;
    }

    public void setSolicitantes(CatEnteLazy solicitantes) {
        this.solicitantes = solicitantes;
    }

    public boolean getContenido() {
        return contenido;
    }

    public void setContenido(boolean contenido) {
        this.contenido = contenido;
    }

    public CatPredioPropietario getCpp() {
        return cpp;
    }

    public void setCpp(CatPredioPropietario cpp) {
        this.cpp = cpp;
    }

    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

    public String getEmailSolicitante() {
        return emailSolicitante;
    }

    public void setEmailSolicitante(String emailSolicitante) {
        this.emailSolicitante = emailSolicitante;
    }

    public Boolean getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(Boolean tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public Boolean getVisualizarCert() {
        return visualizarCert;
    }

    public void setVisualizarCert(Boolean visualizarCert) {
        this.visualizarCert = visualizarCert;
    }

}
