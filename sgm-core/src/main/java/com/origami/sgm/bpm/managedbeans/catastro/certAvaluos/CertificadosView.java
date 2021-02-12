/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro.certAvaluos;

import com.origami.app.AppConfig;
import com.origami.catastro.services.impl.SeriveceOtrasBases;
import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.catastroextras.models.Radicado;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.bpm.models.DatoSeguro;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatCertificadoAvaluo;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.FormatoReporte;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.entities.models.EstadosPredio;
import com.origami.sgm.geo.GeodataService;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.event.SelectEvent;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 * Controlador del facelet de certificados.
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class CertificadosView implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CertificadosView.class.getName());

    @javax.inject.Inject
    protected Entitymanager manager;
    @javax.inject.Inject
    protected CatastroServices catastro;
    @Inject
    protected OmegaUploader omegaUploader;
    protected CatCertificadoAvaluo cert;
    @Inject
    protected UserSession sess;
    @Inject
    protected ServletSession ss;
    @Inject
    protected AppConfig appConfig;
//    @Inject
//    private DatoSeguroServices datoservice;
    protected FormatoReporte frep = null;
    protected Integer opcionesReporte = 1;
    protected Integer tipoConsulta = 1;
    protected CatPredio seleccion;
    protected List<CatPredio> seleccionados;
    protected List<CatPredio> predios;
    protected List<FormatoReporte> formatosReportes;
    protected List<CatPredioPropietario> predioPropietarios;
    protected CatPredioPropietario cpp;
    protected CatPredioEdificacion bloqueSeleccionado;
    protected String propietarios;
    protected String ciuRuc;
    protected String detalle;
    protected String clavesPredios;
    protected String petNombres;
    protected String otroNombres;
    protected String otroCedRUc;
    protected String otroObservaciones;
    protected String otroTipo;
    protected String petIdentificacion;
    protected String codigo;
    protected Boolean otroValidar;
    protected Boolean showCerts;
    protected Boolean certificadoAvaluoIndividual = Boolean.TRUE;
    protected Boolean btnDisabled = Boolean.FALSE;
    protected BigDecimal porcentajePosecion = BigDecimal.ZERO;
    protected BigDecimal avaluoPlussolar;
    protected BigDecimal avaluoPlusconstruccion;
    protected BigDecimal AvaluoPluObraComplement;
    protected BigDecimal avaluoPlusmunicipal;
    protected String footer;
    protected Integer anioValorizacion = Utils.getAnio(new Date());
    protected Integer bienio1;
    protected Integer bienio2;
    protected Integer tipo = 1;
    protected Integer count = 80;

    @javax.inject.Inject
    protected GeodataService geodataService;
    protected Boolean generado = Boolean.FALSE;

    protected FormatoReporte faux;
    protected Radicado radicado;
    protected Integer idFact = null;
    protected Integer countFactPag = 0;
    protected Boolean isFacturado = false;

    @javax.inject.Inject
    protected SeriveceOtrasBases quipux;
    @javax.inject.Inject
    protected ServiceDataBaseIb ib;
    private String textCed = " con el número de CED/RUC/PASP: ";

    @PostConstruct
    protected void load() {
        try {
            ss.instanciarParametros();
            if (sess != null) {
                Map<String, Object> pm = new HashMap<>();
                pm.put("estado", Boolean.TRUE);
//                pm.put("ventanilla", Boolean.TRUE);
                formatosReportes = manager.findObjectByParameterList(FormatoReporte.class, pm);
                List<FormatoReporte> frTemp = new ArrayList<>();
                int countTemp = 0;
                for (FormatoReporte fp : formatosReportes) {
                    if (fp.getId() == 5L) {
                        faux = fp;
                        //break;
                    } else if (!sess.getRoles().contains(215L)) {
                        if (!fp.getVentanilla()) {
                            frTemp.add(fp);
                        }
                    } else {
                        if (fp.getVentanilla()) {
                            if (countTemp == 0) {
                                isFacturado = true;
                            }
                            frTemp.add(fp);
                        }
                    }
                }
                formatosReportes = frTemp;
                formatosReportes.remove(faux);

                showCerts = false;
                footer = SisVars.sisLogo1.substring(0, SisVars.sisLogo1.length() - 6) + "-footer.png";
            }
        } catch (Exception e) {
            Logger.getLogger(CertificadosView.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Realiza la validacion para cada uno de los diferentes certificados, y
     * llama al metodod donde se envian los parametros para el reporte y con el
     * nombre del reporte
     */
    public void imprimirCertificado() {
        if (frep == null) {
            return;
        }
        if (Utils.isEmpty(predios) && frep.getId().intValue() != 2) {
            return;
        }
        if (Utils.isEmpty(predios)) {
            predios = null;
        }
        switch (frep.getId().intValue()) {
            case 1: // Avaluo es de un predio
                if (certificadoAvaluoIndividual) {
                    if (predios.size() > 1 && seleccion == null) {
                        JsfUti.messageError(null, "Error", "Debe seleccionar un predio.");
                        return;
                    }
                } else {
                    if (Utils.isEmpty(seleccionados)) {
                        JsfUti.messageError(null, "Error", "Debe seleccionar los predios que desea generar el certificado.");
                        return;
                    }
                    seleccion = seleccionados.get(0);
                    seleccionados.remove(seleccion);
                }
                if (seleccion != null) {
                    predios = Arrays.asList(seleccion);
                }
                this.obtenerPorcentajePosession(seleccion);
                this.generarCertificado("CERTIFICACIÓN DE AVALÚO");
                break;
            case 2: // Bienes todos los predios
                this.generarCertificado("CERTIFICACIÓN DE BIENES");
                break;
            case 3: // Ficha catastral solo un predio
                if (predios.size() > 1 && seleccion == null) {
                    JsfUti.messageError(null, "Error", "Debe seleccionar un predio.");
                    return;
                }
                if (seleccion != null) {
                    predios = Arrays.asList(seleccion);
                }
                this.generarCertificado("FICHA CATASTRAL");
                break;
            case 4: // Certificado de plusvalia solo un predio
                if (predios.size() > 1 && seleccion == null) {
                    JsfUti.messageError(null, "Error", "Debe seleccionar un predio.");
                    return;
                }

                if (seleccion != null) {

                    if (seleccion.getAvaluoPlussolar() != null) {
                        avaluoPlussolar = seleccion.getAvaluoPlussolar();
                    } else {
                        avaluoPlussolar = BigDecimal.ZERO;
                    }

                    if (seleccion.getAvaluoPlusconstruccion() != null) {
                        avaluoPlusconstruccion = seleccion.getAvaluoPlusconstruccion();
                    } else {
                        avaluoPlusconstruccion = BigDecimal.ZERO;
                    }

                    if (seleccion.getAvaluoPluObraComplement() != null) {
                        AvaluoPluObraComplement = seleccion.getAvaluoPluObraComplement();
                    } else {
                        AvaluoPluObraComplement = BigDecimal.ZERO;
                    }

                    if (seleccion.getAvaluoPlusmunicipal() != null) {
                        avaluoPlusmunicipal = seleccion.getAvaluoPlusmunicipal();
                    } else {
                        avaluoPlusmunicipal = BigDecimal.ZERO;
                    }

                    JsfUti.executeJS("PF('dlgPlusvalia').show()");
                    JsfUti.update("frmplusvalia");
                }
                break;

            default:
                if (predios.size() > 1 && seleccion == null) {
                    JsfUti.messageError(null, "Error", "Debe seleccionar un predio.");
                    return;
                }
                if (seleccion != null) {
                    predios = Arrays.asList(seleccion);
                }
                this.generarCertificado(this.frep.getCodigo());
                break;
        }
    }

    /**
     *
     * @param seleccion
     */
    protected void obtenerPorcentajePosession(CatPredio seleccion) {
        if (cpp != null && cpp.getPredio() != null) {
            if (seleccion != null) {
                for (CatPredioPropietario cppTem : seleccion.getCatPredioPropietarioCollection()) {
                    if (Objects.equals(cppTem.getCiuCedRuc(), cpp.getCiuCedRuc()) && cppTem.getEstado().contains(EstadosPredio.ACTIVO)) {
                        porcentajePosecion = cppTem.getPorcentajePosecion();
                        break;
                    } else {
                        porcentajePosecion = BigDecimal.ZERO;
                    }
                }
            }
        }
    }

    /**
     * Genera el certificado
     */
    private void generarCertificado(String nombreCertificado) {

        try {

            clavesPredios = "";
            if (!Utils.isEmpty(predios) && predios != null) {
                clavesPredios = " /Claves catastrales/";
                predios.forEach((predio) -> {
                    clavesPredios += predio.getClaveCat() + "/";
                });
                clavesPredios = detalle + clavesPredios;
            }
            try {
                cert = new CatCertificadoAvaluo();
                cert.setFormato(frep);
                cert.setIdentificacion(petIdentificacion + "/" + petNombres);
                cert.setDetalle(clavesPredios);
                cert.setUsuario(sess.getName_user());
                cert.setFecha(new Date());
                cert.setEstado(true);
                if (frep.getGeneraCobro()) {
//                    cert.setNumCert(java.math.BigInteger.valueOf(count));
                    cert.setNumCert(catastro.getNumCertificado());
                    if (predios.get(0) != null) {
                        this.cambiarEstadoFacturaRentas(predios.get(0).getClaveCat(), cert.getNumCert());
                    }
                    this.getIdFact();
                }
                count++;
                cert = (CatCertificadoAvaluo) manager.persist(cert);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Guardado de certificado", e);
            }

            ss.instanciarParametros();
            ss.setNombreReporte(frep.getReporte());
            if (nombreCertificado.equals("CERTIFICADO DE AVALUO ACTUALIZADO")) {
                manager.persist(predios.get(0));
            }
            ss.setNombreSubCarpeta("/catastro/certificados/");
            ss.agregarParametro("LOGO", SisVars.logoReportes);
            ss.agregarParametro("LOGO_1", SisVars.sisLogo1);
            ss.agregarParametro("LOGO_FOOTER", footer);
            ss.agregarParametro("TITULO", SisVars.NOMBREMUNICIPIO);
            ss.agregarParametro("NOMBRE_CERTIFICADO", nombreCertificado);
            ss.agregarParametro("DETALLE", detalle);
            ss.agregarParametro("USUARIO", cert.getUsuario());
            ss.agregarParametro("FECHA", cert.getFecha());
            ss.agregarParametro("NUM_CERTIFICADO", cert.getNumCert());

            ss.agregarParametro("PETIDOR", petNombres);
            ss.agregarParametro("PERTIDOR_IDNT", petIdentificacion);
//            ss.agregarParametro("CODIGO", count.longValue());
            ss.agregarParametro("CODIGO", cert.getId());
            ss.agregarParametro("ANIO", anioValorizacion);
            ss.agregarParametro("SUBREPORT_DIR", JsfUti.getRealPath(SisVars.REPORTES + "/"));
            ss.agregarParametro("PORCENTAJE_POSESSION", porcentajePosecion);

            ss.agregarParametro("FUNCIONARIO", sess.getNombrePersonaLogeada());
            ss.agregarParametro("VALIDO", radicado != null);
            ss.setTieneDatasource(Boolean.FALSE);
            ss.setDataSource(predios);
            if (bloqueSeleccionado != null) {
                if (frep.getCodigo().equalsIgnoreCase("valorEdificacion")) {
                    seleccion.setAreaDeclaradaConst(bloqueSeleccionado.getAreaBloque());
                    predios = Arrays.asList(seleccion);
                }
            }
            // SI NO ES CERTIFICADO DE AVALUO INDIVIDUAL AGREGAMOS LOS OTROS CERTIFICADOS
            if (nombreCertificado.equals("CERTIFICACIÓN DE AVALÚO") && !certificadoAvaluoIndividual) {
                Map<String, Object> temp = new HashMap<>();
                temp.putAll(ss.getParametros());
                // removemos los el predio con que se tomo como principal para
                // que no se repita.
                System.out.println("Imprimiendo seleccionados: " + seleccionados);
                for (CatPredio select : seleccionados) {
                    System.out.println("Seleccionado: " + select.getId());
                    System.out.println("Pos(0): " + predios.get(0).getId());
                    if (select.getId() != predios.get(0).getId()) {
                        Map<String, Object> reporteadd = new HashMap<>();
                        reporteadd.putAll(temp);
                        reporteadd.put("nombreSubCarpeta", "/catastro/certificados/");
                        reporteadd.put("nombreReporte", frep.getReporte());
                        String clavesPredios1 = " /Claves catastrales/";
                        clavesPredios1 += select.getClaveCat() + "/";
                        clavesPredios1 = detalle + clavesPredios1;
                        this.obtenerPorcentajePosession(select);
                        try {
                            CatCertificadoAvaluo cert1 = new CatCertificadoAvaluo();
                            cert1.setFormato(frep);
                            cert1.setIdentificacion(petIdentificacion + "/" + petNombres);
                            cert1.setDetalle(clavesPredios1);
                            cert1.setUsuario(sess.getName_user());
                            cert1.setFecha(new Date());
                            if (frep.getGeneraCobro()) {
//                                cert1.setNumCert(java.math.BigInteger.valueOf(count));
                                cert1.setNumCert(catastro.getNumCertificado());
                                System.out.println("Generando certificados " + select.getClaveCat() + "-" + cert1.getNumCert());
                                this.cambiarEstadoFacturaRentas(select.getClaveCat(), cert1.getNumCert());
                                this.getIdFact();
                            }
                            count++;
                            cert1.setEstado(true);
                            // GUARDAMOS EL CERTIFICADO
                            cert1 = (CatCertificadoAvaluo) manager.persist(cert1);
                            reporteadd.put("FECHA", cert1.getFecha());
                            reporteadd.put("NUM_CERTIFICADO", cert1.getNumCert());

//                            reporteadd.put("CODIGO", count.longValue());
                            reporteadd.put("CODIGO", cert1.getId());
                            reporteadd.put("PORCENTAJE_POSESSION", porcentajePosecion);
                            reporteadd.put("DATASOURCE", Arrays.asList(select));
                            // AGREGAMOS CADA UNO DE LOS REPORTES
                            ss.addParametrosReportes(reporteadd);
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, "Agregando Ceriticados de Avaluos", e);
                        }
                    }
                }

                // INDICAMOS QUE VAMOS VAMOS A AGREGAR VARIOS REPORTES AL MISMO REPORTE
                if (Utils.isNotEmpty(seleccionados) && Utils.isNotEmpty(ss.getReportes())) {
                    ss.getReportes().size();
                    ss.setAgregarReporte(true);
                }
            }
            // AGREGAMOS LOS DATOS DE LA FICHA CATASTRAL AL CERTIFICADO
            if (nombreCertificado.equals("FICHA CATASTRAL")) {
                Map<String, Object> reporteadd = new HashMap<>();
                reporteadd.put("nombreSubCarpeta", "/catastro/Ibarra");
                reporteadd.put("nombreReporte", "fichaMiduvi");

                CatPredio predio = predios.get(0);
                if (predio != null) {
                    String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                    int numFotos = 1;
                    List<FotoPredio> fotos = null;
                    if (predio.getPredioRaiz() == null) {
                        fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getId()});
                    } else {
                        fotos = manager.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predio.getPredioRaiz().longValue()});
                    }

                    if (Utils.isNotEmpty(fotos)) {
                        fotos = fotos.subList(0, (fotos.size() < 4 ? fotos.size() : 4));
                        for (FotoPredio foto : fotos) {
                            switch (numFotos) {
                                case 1:
                                    reporteadd.put("FachadaFrontal", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                                case 2:
                                    reporteadd.put("FachadaIzquierda", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                                case 3:
                                    reporteadd.put("FachadaDerecha", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                                case 4:
                                    reporteadd.put("FachadaPosterior", omegaUploader.streamFile(foto.getFileOid()));
                                    break;
                            }
                            numFotos++;
                        }
                    }
                    reporteadd.put("predio", predio.getId());

                    reporteadd.put("IMAGEN_PREDIO", appConfig.formatUrlPredio(predio.getClaveCat(), predio.getPropiedadHorizontal()));
                    reporteadd.put("LOGO", path + SisVars.sisLogo);
                    reporteadd.put("LOGO2", path + SisVars.sisLogo1);
                    reporteadd.put("SUBREPORT_DIR_TITLE", path + "reportes/");
                    reporteadd.put("SUBREPORT_DIR", path + "reportes/catastro/Ibarra/");
                    ss.addParametrosReportes(reporteadd);

                    if (predio.getCatPredioEdificacionCollection() != null && !Utils.isEmpty(predio.getCatPredioEdificacionCollection())) {
                        int count = 0;
                        System.out.println("Agregando construcciones");
                        String edificaciones = "";
                        for (CatPredioEdificacion edif : predio.getCatPredioEdificacionCollection()) {
                            count++;
                            edificaciones += edif.getId().toString() + ",";
                            if ((count % 4) == 0 || (predio.getCatPredioEdificacionCollection().size() - count) == 0) {
                                edificaciones = edificaciones.substring(0, edificaciones.length() - 1);
                                reporteadd = new HashMap<>();
                                reporteadd.put("nombreSubCarpeta", "/catastro/Ibarra");
                                reporteadd.put("nombreReporte", "fichaMiduviBloques");
                                reporteadd.put("predio", predio.getId());
                                reporteadd.put("edificaciones", edificaciones);
                                ss.addParametrosReportes(reporteadd);
                                edificaciones = "";
                            }

                        }
                    }

                    ss.setAgregarReporte(true);
                    ss.getReportes().size();
                }
            }
            showCerts = false;
            codigo = "";
            seleccion = null;
            JsfUti.update("mainForm");
            JsfUti.redirectNewTab(SisVars.urlbase + "Documento");
            generado = Boolean.TRUE;
        } catch (Exception e) {
            Logger.getLogger(CertificadosView.class
                    .getName()).log(Level.SEVERE, null, e);
        }
    }

    public void borrarDatos() {
        seleccion = null;
        seleccionados = null;
    }

    public void verificarCodigo() {
        try {
            showCerts = false;

//            if (sess.getName_user().equals("VDavila")) {
            if (sess.getRoles().contains(220L)
                    || sess.getIsAdmin()) {

                isFacturado = false;
                showCerts = true;
            } else {

                if (this.tipo == 2) {
                    try {
                        radicado = quipux.getDataQuipux(this.codigo);
                    } catch (Exception e) {

                    }
                    showCerts = true;
                } else {
                    // si se factura verificamos que exista en la tabla rentas.ren_bandeja_especies
                    if (isFacturado) {
                        String sql = "SELECT count(r.be_id) FROM rentas.ren_bandeja_especies r "
                                + "WHERE (r.be_contribuyente_facrtua='%s' OR r.be_factura='%s') AND "
                                + "(r.be_estado='F' OR r.be_estado='X') AND r.be_especie=6";
                        Object find = manager.getNativeQuery(String.format(sql, codigo, codigo));
                        if (find != null) {
                            countFactPag = Integer.valueOf(find.toString());
                            if (countFactPag > 0) {
                                if (getIdFact()) {
                                    showCerts = true;
                                } else {
                                    System.out.println("No se puedo obtener el id de la especie usuario " + this.sess.getName_user());
                                }
                            } else {
                                JsfUti.messageWarning(null, "!Advertencia", "El numero de factura o de cedula ya no se encuentra disponible.");
                            }
                        } else {
                            JsfUti.messageWarning(null, "!Advertencia", "No se encontro el numero de cedula o factura.");
                            showCerts = false;
                        }
                    } else {
                        showCerts = true;
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Mostrar predios", e);
        }
    }

    private boolean getIdFact() {
//        String sqlGetId = "SELECT be_id as id FROM rentas.ren_bandeja_especies "
//                + "WHERE (be_contribuyente_facrtua= '%s' OR be_factura= '%s') AND "
//                + "(be_estado='F' OR be_estado='X') AND be_especie=6 LIMIT 1";
//        Integer id = (Integer) manager.getNativeQuery(String.format(sqlGetId, codigo, codigo));
        Integer id = null;
        if (id != null) {
            idFact = id;
            return true;
        } else {
            System.out.println("No se puedo obtener el id de la especie usuario " + this.sess.getName_user());
        }
        return false;
    }

    public void onRowSelectPredios(SelectEvent event) {
        if (isFacturado) {
            if (countFactPag != null && Utils.isNotEmpty(seleccionados)) {
                if (seleccionados.size() == countFactPag) {
                    this.setBtnDisabled(false);
                    JsfUti.messageInfo(null, "!Informacion", "El numero de certificados pagados es igual a los predios seleccionados.");
                } else if (seleccionados.size() > countFactPag) {
                    JsfUti.messageWarning(null, "!Error", "Los predios seleccionados es mayor a los certificados pagados.");
                    this.setBtnDisabled(true);
                    try {
                        System.out.println("Index of " + seleccionados.indexOf(event.getObject()));
                        boolean remove = seleccionados.remove((CatPredio) event.getObject());
                        System.out.println("Removido " + remove);
                    } catch (Exception e) {
                        System.out.println("Error el remover objecto en certificado onRowSelectPredios: \n " + e.getMessage());
                    }
                }
            }
        }
    }

    private Boolean cambiarEstadoFacturaRentas(String claveCat, BigInteger numCert) {
        System.out.println("Cambiar estado Factura" + idFact);
        if (idFact != null) {
            System.out.println("Actualizando fac" + idFact);
            String update = "UPDATE rentas.ren_bandeja_especies SET be_estado = 'I', "
                    + "be_contribuyente = ?, be_clave_catastral = ?, "
                    + "be_usuario_imprime = ?, be_numero_copia = ?,be_fecha_imprime=? "
                    + "WHERE be_id = " + idFact;
            try {
                AclUser user = manager.find(AclUser.class, sess.getUserId());
                return manager.executeNativeQuery(update, new Object[]{petIdentificacion, claveCat,
                    (user.getUsuarioSigm() == null ? sess.getName_user() : user.getUsuarioSigm()), numCert,
                    new Date()});
            } catch (Exception e) {
                System.out.println("Error el actualizar factura cambiarEstadoFacturaRentas: " + e.getMessage());
            }
            //idFact = null;
            this.getIdFact();
            System.out.println("Siguiente Fact: " + idFact);
        } else {
            System.out.println("No se puedo cambiar el estado de la factura del certificado... cambiarEstadoFacturaRentas");
        }
        return false;
    }

    private List<CatPredio> limpiarRepetidos(List<CatPredio> predios) {
        if (Utils.isEmpty(predios)) {
            return null;
        }
        Map<Long, CatPredio> pt = new HashMap<>();
        for (CatPredio predio : predios) {
            pt.put(predio.getId(), predio);
        }
        List<CatPredio> prediosT = new ArrayList<>();
        for (Map.Entry<Long, CatPredio> catPredio : pt.entrySet()) {
            prediosT.add(catPredio.getValue());
        }
        return prediosT;
    }

    private List<CatPredioPropietario> limpiarRepetidosPP(List<CatPredioPropietario> predioPropietarios) {
        if (Utils.isEmpty(predioPropietarios)) {
            return null;
        }

        Map<String, CatPredioPropietario> pt = new HashMap<>();
        for (CatPredioPropietario predio : predioPropietarios) {
            pt.put(predio.getCiuCedRuc(), predio);
        }
        List<CatPredioPropietario> prediosT = new ArrayList<>();
        for (Map.Entry<String, CatPredioPropietario> catPredio : pt.entrySet()) {
            prediosT.add(catPredio.getValue());
        }
        return prediosT;
    }

    public void visualizarPredios(String page) {
        try {
            Utils.openDialog(page, null, "550", "80");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Mostrar predios", e);
        }
    }

    public boolean mostrarAgregar() {
        if (frep == null) {
            return false;
        } else if (frep.getCodigo().equals("CERTIFICADO DE BIENES")) {
            return true;
        }
        return false;
    }

    public void procesarPredios(SelectEvent event) {
        try {
            if (predios == null) {
                predios = new ArrayList<>();
            }
            if (event.getObject() instanceof CiuCiudadano) {
                CiuCiudadano en = (CiuCiudadano) event.getObject();
                ciuRuc = en.getCiuCedRuc();
                petNombres = en.getCiuNombreCompleto();
                petIdentificacion = en.getCiuCedRuc();
                predioPropietarios = this.catastro.getPropietarios(en.getCiuCedRuc());
                if (!Utils.isNotEmpty(predioPropietarios)) {
                    CatPredioPropietario aux = new CatPredioPropietario();
                    aux.setCiuCedRuc(en.getCiuCedRuc());
                    aux.setNombresCompletos(en.getCiuNombreCompleto());
                    aux.setOtros("");
                    aux.setId(Double.valueOf(Math.random() * 100).longValue());
                    predioPropietarios.add(aux);
                }
                this.llenarPredio(en.getCiuCedRuc(), en);
            } else {
                this.llenarPredios((List<CatPredio>) event.getObject());
                if (Utils.isNotEmpty(this.predioPropietarios)) {
                    for (CatPredioPropietario cpp : predioPropietarios) {
                        llenarPredio(cpp.getCiuCedRuc(), null);
                    }
                }
            }
            predioPropietarios = this.limpiarRepetidosPP(predioPropietarios);
            this.llenarNombres(predioPropietarios);
            this.llenarFormato();

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Procesar predios", e);
        }
    }

    public void llenarPredio(String ciRuc, CiuCiudadano en) {
        List<CatPredio> prediostemp = new ArrayList<>();
        prediostemp = this.catastro.getPrediosSinMatrices(ciRuc);
        if (predios == null) {
            predios = new ArrayList<>();
        }

        if (Utils.isNotEmpty(prediostemp)) {
            for (CatPredio catPredio : prediostemp) {
                if (!catPredio.getFichaMadre()) {
                    if (!predios.contains(catPredio)) {
                        if (catPredio.getEstado().equalsIgnoreCase(EstadosPredio.ACTIVO)) {
                            catPredio.setCatParroquia(catastro.getCatParroquia(catPredio.getParroquia()));
                            predios.add(catPredio);
                        }
                    }
                }
            }
        }
        predios = limpiarRepetidos(predios);
    }

    public void procesarPetidor(SelectEvent event) {
        try {
            CiuCiudadano en = (CiuCiudadano) event.getObject();
            if (en != null) {
                petNombres = en.getCiuNombreCompleto();
                petIdentificacion = en.getCiuCedRuc();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Procesar Petidor Alterno", e);
        }
    }

    public void procesarOtros(SelectEvent event) {
        try {
            CiuCiudadano en = (CiuCiudadano) event.getObject();
            otroNombres = en.getCiuNombreCompleto();
            otroCedRUc = en.getCiuCedRuc();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Procesar Petidor Alterno", e);
        }
    }

    public void abrirOtros() {
        otroNombres = "";
        otroCedRUc = "";
        otroObservaciones = "";
        otroTipo = "";
        otroValidar = true;

        JsfUti.executeJS("PF('dlgprop').show()");
        JsfUti.update("frmprop");
    }

    public void actulizarPredio() {

        if (Utils.isNotEmpty(predioPropietarios)) {
            for (CatPredioPropietario predioPropietario : predioPropietarios) {
                if (predioPropietario.getPredio() != null) {
                    if (!predios.contains(predioPropietario.getPredio())) {
                        predios.add(predioPropietario.getPredio());
                    }
                }
            }
            this.llenarFormato();
        }

    }

    public void llenarPredios(List<CatPredio> p) {
        if (Utils.isNotEmpty(p)) {
            predioPropietarios = new ArrayList<>();
            List<CatPredio> temp = new ArrayList<>();
            for (CatPredio catPredio : p) {
                catPredio.setCatParroquia(catastro.getCatParroquia(catPredio.getParroquia()));
                if (Utils.isNotEmpty(catPredio.getCatPredioPropietarioCollection())) {
                    for (CatPredioPropietario pp : catPredio.getCatPredioPropietarioCollection()) {
                        boolean equals = false;
                        if (Utils.isNotEmpty(predioPropietarios)) {
                            for (CatPredioPropietario pps : predioPropietarios) {
                                equals = pps.getCiuCedRuc().equals(pp.getCiuCedRuc());
                            }
                        }
                        if (!equals) {
                            predioPropietarios.add(pp);
                        }
                    }
                } else {
                    System.out.println(">>>>>> Collection Propietarios vacia");
                }
                if (!predios.contains(catPredio)) {
                    if (catPredio.getFichaMadre() != null) {
                        if (!catPredio.getFichaMadre()) {
                            temp.add(catPredio);
                        }
                    }
                }
            }
//        predios.addAll(temp);
            predios = Utils.verificarRepetidos(new ArrayList<>(), predios, temp, 0);
            predioPropietarios = this.limpiarRepetidosPP(predioPropietarios);
        }
    }

    public void onRowSelect(SelectEvent event) {
        try {
            if (cpp != null) {
                ciuRuc = cpp.getCiuCedRuc();
                petNombres = cpp.getNombresCompletos();
                petIdentificacion = cpp.getCiuCedRuc();
                predios = new ArrayList<>();
                if (this.catastro.getPrediosSinMatrices(ciuRuc) != null) {
                    predios.addAll(this.catastro.getPrediosSinMatrices(ciuRuc));
                    predios.forEach((predio) -> {
                        predio.setCatParroquia(catastro.getCatParroquia(predio.getParroquia()));
                    });
                }

                llenarNombres(Arrays.asList(cpp));
                this.llenarFormato();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Procesar predios", e);
        }
    }

    public void sumarPlusvalias() {
        avaluoPlusmunicipal = (avaluoPlussolar.add(avaluoPlusconstruccion).add(AvaluoPluObraComplement));
        JsfUti.update("frmplusvalia:tot_plusvalia");
    }

    public void llenarNombres(Collection<CatPredioPropietario> props) {
        propietarios = "";
        if (Utils.isNotEmpty(props)) {
            if (ciuRuc == null) {
                ciuRuc = Utils.get(props, 0).getCiuCedRuc();
            }
            petNombres = Utils.get(props, 0).getNombresCompletos();
            petIdentificacion = Utils.get(props, 0).getCiuCedRuc();
            for (Iterator<CatPredioPropietario> iterator = props.iterator(); iterator.hasNext();) {
                CatPredioPropietario next = iterator.next();
                next.setCiudadano(this.ib.getCiudadano(next.getCiuCedRuc()));
                if (next.getCiudadano() != null) {
                    propietarios = propietarios + next.getCiudadano().nombresCompletos() + textCed + next.getCiudadano().getCiuCedRuc();
                } else {
                    propietarios = propietarios + Utils.isEmpty(next.getNombresCompletos()) + Utils.isEmpty(textCed) + Utils.isEmpty(next.getCiuCedRuc());
                }
                if (next.getOtros() != null && !next.getOtros().trim().isEmpty()) {
                    propietarios = propietarios + " " + Utils.isEmpty(next.getOtros());
                }
                if (iterator.hasNext()) {
                    propietarios = propietarios + " Y ";
                }
            }
        }
    }

    public void llenarFormato() {
        if (frep == null) {
            return;
        }
        try {
            if ((predios == null || Utils.isEmpty(predios)) && frep.getId().intValue() == 2) {
                frep = faux;
                detalle = String.format(faux.getFormato(), SisVars.NOMBREMUNICIPIO, propietarios);
            } else {
//                detalle = String.format(frep.getFormato(), SisVars.NOMBREMUNICIPIO, propietarios);
                detalle = frep.getFormato();
                if (Utils.isEmpty(predios)) {
                    JsfUti.messageInfo(null, "Informacion", "No registra Predios");
                    return;
                }
                CatPredio predio = this.predios.get(0);
                String ubicacion = predio.getDireccionPredio();
                if (detalle.contains(":nombreMunicipio")) {
                    detalle = detalle.replace(":nombreMunicipio", SisVars.NOMBREMUNICIPIO);
                }
                if (detalle.contains(":fecha")) {
                    detalle = detalle.replace(":fecha", Utils.converterDateFormat(new Date()));
                }
                if (detalle.contains(":numTramite")) {
                    detalle = detalle.replace(":numTramite", this.codigo);
                }
                if (detalle.contains(":claveCat")) {
                    detalle = detalle.replace(":claveCat", predio.getClaveCat());
                }
                if (detalle.contains(":ubicacion")) {
                    detalle = detalle.replace(":ubicacion", ubicacion);
                }
                if (detalle.contains(":propietario")) {
                    if ((this.propietarios == null && this.propietarios.isEmpty())
                            || (this.textCed == null && this.textCed.isEmpty())) {
                        JsfUti.messageInfo(null, "Error.", "La lista de propietarios esta vacia o cedula esta vacia.");
                    } else {
                        detalle = detalle.replace(":propietario", this.propietarios);
                    }
                }
                if (detalle.contains(":cedula")) {
                    detalle = detalle.replace(":cedula", this.getPetIdentificacion());
                }
                if (detalle.contains(":anioValorizacion")) {
                    detalle = detalle.replace(":anioValorizacion", this.anioValorizacion == null ? "" : this.anioValorizacion.toString());
                }
                if (detalle.contains(":solicitante")) {
                    detalle = detalle.replace(":solicitante", this.getPetNombres());
                }
                if (detalle.contains(":cedula")) {
                    detalle = detalle.replace(":cedula", this.getPetIdentificacion());
                }
                if (detalle.contains(":bienio1")) {
                    this.bienio1 = this.anioValorizacion;
                    this.bienio2 = this.anioValorizacion;
                    detalle = detalle.replace(":bienio1", this.bienio1 == null ? "" : this.bienio1.toString());
                }
                if (detalle.contains(":bienio2")) {
                    this.bienio2++;
                    detalle = detalle.replace(":bienio2", this.bienio2 == null ? "" : this.bienio2.toString());
                }
                detalle.replace("\n", "<br/>");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Llenar Formato Certificado", e);
        }
    }

    public void eliminar(CatPredio cp) {
        if (cp != null) {
            predios.remove(cp);
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
            ss.agregarParametro("id", cert.getId());
            ss.agregarParametro("logo", SisVars.logoReportes);
            ss.agregarParametro("valido", radicado != null);
            ss.setNombreSubCarpeta("catastro");
            JsfUti.redirectNewTab("/sgmEE/Documento");
        } else {
            Faces.messageWarning(null, "Advertencia", "No se pudo imprimir el certificado, verificar que los datos esten correctos");
        }
    }

    public void guardarPlusvalia() {
        if (seleccion != null) {
            seleccion.setAvaluoPlussolar(avaluoPlussolar);
            seleccion.setAvaluoPluObraComplement(AvaluoPluObraComplement);
            seleccion.setAvaluoPlusconstruccion(avaluoPlusconstruccion);
            seleccion.setAvaluoPlusmunicipal(avaluoPlusmunicipal);
            predios = Arrays.asList(seleccion);
            this.generarCertificado("CERTIFICADO DE AVALUO ACTUALIZADO");
            JsfUti.executeJS("PF('dlgPlusvalia').hide()");

        }
    }

    public void agregarOtros() {
        if (otroObservaciones == null || otroObservaciones.trim().isEmpty()) {
            Faces.messageWarning(null, "Advertencia", "Debe seleccionar la observación");
            return;
        }

        CatPredioPropietario prop = new CatPredioPropietario();
        prop.setCiuCedRuc(otroCedRUc);
        prop.setId(Double.valueOf(Math.random() * 100).longValue());

        prop.setNombresCompletos(otroNombres);
        prop.setOtros(otroObservaciones);

        if (predioPropietarios == null) {
            predioPropietarios = new ArrayList<>();
        } else {
            for (CatPredioPropietario predioPropietario : predioPropietarios) {
                if (predioPropietario.getOtros() != null && !predioPropietario.getOtros().trim().isEmpty()) {
                    if (!predioPropietario.getOtros().equals(otroObservaciones)) {
                        Faces.messageWarning(null, "Advertencia", "Exiten propietarios con observaciones diferentes a " + otroObservaciones);
                        return;
                    }
                }
            }
        }
        predioPropietarios.add(prop);
        this.llenarNombres(predioPropietarios);
        llenarFormato();
        JsfUti.executeJS("PF('dlgprop').hide()");
        JsfUti.update("mainForm:frmCertificados");

    }

    public void verificarCiudadano() {
        DatoSeguro data = null;
        Boolean empresa = false;
        if (otroTipo == null) {
            JsfUti.messageInfo(null, "Advertencia", "El tipo de identificación no puede estar vacio");
            return;
        }
        if (otroCedRUc == null) {
            JsfUti.messageInfo(null, "Advertencia", "El número de identificación no puede estar vacio");
            return;
        }
        if (otroCedRUc != null) {

            if (otroTipo.equals("C") || otroTipo.equals("R")) {
                if (otroTipo.equals("R")) {
                    empresa = true;
                }

//                data = datoservice.getDatos(otroCedRUc, empresa, 0);
                if (data != null) {
                    llenarCiudadano(data);
                    JsfUti.update("frmprop:nombre_input");

                } else {
                    if (!Utils.validateCCRuc(otroCedRUc)) {
                        JsfUti.messageInfo(null, "Advertencia", "El número de identificación es incorrecto");
                        return;
                    }
                }

            }
        }

    }

    public void removerPropietario(CatPredioPropietario propietario) {
        if (predioPropietarios == null || !Utils.isNotEmpty(predioPropietarios)) {
            return;
        }
//        if (propietario.getOtros() == null) {
//            JsfUti.messageInfo(null, "Advertencia", "No se puede remover el propietario");
//            return;
//        }
        int index = predioPropietarios.indexOf(propietario);
        predioPropietarios.remove(index);
        this.llenarNombres(predioPropietarios);
        llenarFormato();
//          System.out.println("propietarios: " + propietarios);
        JsfUti.update("mainForm:frmCertificados");
    }

    public void llenarCiudadano(DatoSeguro data) {
        String fields[];
        Integer num;
        String nombre = "";
        String aux = "";

        try {
            if (data != null) {
                data.setDescripcion(verificarContenido(data.getDescripcion()));
                fields = data.getDescripcion().split(" ");
                num = fields.length;

                switch (num) {
                    case 3:

                        nombre = fields[0] + " " + fields[1] + " " + fields[2];

                        break;
                    case 4:
                        nombre = fields[0] + " " + fields[1] + " " + fields[2] + " " + fields[3];
                        break;
                    case 5:
                        nombre = fields[0] + " " + fields[1] + " " + fields[2] + " " + fields[3] + " " + fields[4];
                        break;
                    default:
                        nombre = fields[0] + " " + fields[1];

                        for (int i = 2; i < num; i++) {
                            aux = aux + fields[i];
                            if (i != num - 1) {
                                aux = aux + " ";
                            }
                        }
                        nombre = fields[0] + " " + fields[1] + " " + aux;
                        break;
                }
                otroNombres = nombre;
                otroCedRUc = data.getIdentificacion();

            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "llenar Ciudadano", e);
        }

    }

    private String verificarContenido(String descripcion) {
        Charset utf8 = Charset.forName("UTF-8");
        String Buffer = new String(descripcion.getBytes(), utf8);
        return Buffer;
    }

    public void nuevo() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest servletRequest = (HttpServletRequest) ctx.getExternalContext().getRequest();
        String fullURI = servletRequest.getRequestURI();
        JsfUti.redirect(fullURI);
    }

    //<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public Boolean getCertificadoAvaluoIndividual() {
        return certificadoAvaluoIndividual;
    }

    public void setCertificadoAvaluoIndividual(Boolean certificadoAvaluoIndividual) {
        this.certificadoAvaluoIndividual = certificadoAvaluoIndividual;
    }

    public CatCertificadoAvaluo getCert() {
        return cert;
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
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

    public Integer getTipoConsulta() {
        return tipoConsulta;
    }

    public void setTipoConsulta(Integer tipoConsulta) {
        this.tipoConsulta = tipoConsulta;
    }

    public Integer getOpcionesReporte() {
        return opcionesReporte;
    }

    public void setOpcionesReporte(Integer opcionesReporte) {
        this.opcionesReporte = opcionesReporte;
    }

    public List<CatPredio> getPredios() {
        if (Utils.isNotEmpty(predios)) {
            Collections.sort(predios, (CatPredio p1, CatPredio p2) -> p1.getClaveCat().compareTo(p2.getClaveCat()));
        }
        return predios;
    }

    public void setPredios(List<CatPredio> predios) {
        this.predios = predios;
    }

    public List<CatPredioPropietario> getPredioPropietarios() {
        return predioPropietarios;
    }

    public void setPredioPropietarios(List<CatPredioPropietario> predioPropietarios) {
        this.predioPropietarios = predioPropietarios;
    }

    public String getPropietarios() {
        return propietarios;
    }

    public void setPropietarios(String propietarios) {
        this.propietarios = propietarios;
    }

    public String getCiuRuc() {
        return ciuRuc;
    }

    public void setCiuRuc(String ciuRuc) {
        this.ciuRuc = ciuRuc;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public CatPredio getSeleccion() {
        return seleccion;
    }

    public void setSeleccion(CatPredio seleccion) {
        this.seleccion = seleccion;
    }

    public String getOtroObservaciones() {
        return otroObservaciones;
    }

    public void setOtroObservaciones(String otroObservaciones) {
        this.otroObservaciones = otroObservaciones;
    }

    public List<FormatoReporte> getFormatosReportes() {
        return formatosReportes;
    }

    public void setFormatosReportes(List<FormatoReporte> formatosReportes) {
        this.formatosReportes = formatosReportes;
    }

    public FormatoReporte getFrep() {
        return frep;
    }

    public void setFrep(FormatoReporte frep) {
        this.frep = frep;
    }

    public String getPetNombres() {
        return petNombres;
    }

    public void setPetNombres(String petNombres) {
        this.petNombres = petNombres;
    }

    public String getPetIdentificacion() {
        return petIdentificacion;
    }

    public String getOtroNombres() {
        return otroNombres;
    }

    public void setOtroNombres(String otroNombres) {
        this.otroNombres = otroNombres;
    }

    public String getOtroCedRUc() {
        return otroCedRUc;
    }

    public void setOtroCedRUc(String otroCedRUc) {
        this.otroCedRUc = otroCedRUc;
    }

    public void setPetIdentificacion(String petIdentificacion) {
        this.petIdentificacion = petIdentificacion;
    }

    public BigDecimal getAvaluoPlussolar() {
        return avaluoPlussolar;
    }

    public void setAvaluoPlussolar(BigDecimal avaluoPlussolar) {
        this.avaluoPlussolar = avaluoPlussolar;
    }

    public BigDecimal getAvaluoPlusconstruccion() {
        return avaluoPlusconstruccion;
    }

    public void setAvaluoPlusconstruccion(BigDecimal avaluoPlusconstruccion) {
        this.avaluoPlusconstruccion = avaluoPlusconstruccion;
    }

    public BigDecimal getAvaluoPluObraComplement() {
        return AvaluoPluObraComplement;
    }

    public void setAvaluoPluObraComplement(BigDecimal AvaluoPluObraComplement) {
        this.AvaluoPluObraComplement = AvaluoPluObraComplement;
    }

    public BigDecimal getAvaluoPlusmunicipal() {
        return avaluoPlusmunicipal;
    }

    public void setAvaluoPlusmunicipal(BigDecimal avaluoPlusmunicipal) {
        this.avaluoPlusmunicipal = avaluoPlusmunicipal;
    }

    public String getOtroTipo() {
        return otroTipo;
    }

    public void setOtroTipo(String otroTipo) {
        this.otroTipo = otroTipo;
    }

    public Boolean getOtroValidar() {
        return otroValidar;
    }

    public void setOtroValidar(Boolean otroValidar) {
        this.otroValidar = otroValidar;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getShowCerts() {
        return showCerts;
    }

    public void setShowCerts(Boolean showCerts) {
        this.showCerts = showCerts;
    }

    public Integer getAnioValorizacion() {
        return anioValorizacion;
    }

    public void setAnioValorizacion(Integer anioValorizacion) {
        this.anioValorizacion = anioValorizacion;
    }

    public Integer getBienio1() {
        return bienio1;
    }

    public void setBienio1(Integer bienio1) {
        this.bienio1 = bienio1;
    }

    public Integer getBienio2() {
        return bienio2;
    }

    public void setBienio2(Integer bienio2) {
        this.bienio2 = bienio2;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public CatPredioEdificacion getBloqueSeleccionado() {
        return bloqueSeleccionado;
    }

    public void setBloqueSeleccionado(CatPredioEdificacion bloqueSeleccionado) {
        this.bloqueSeleccionado = bloqueSeleccionado;
    }

    public Boolean getGenerado() {
        return generado;
    }

    public void setGenerado(Boolean generado) {
        this.generado = generado;
    }
//</editor-fold>

    public List<CatPredio> getSeleccionados() {
        return seleccionados;
    }

    public void setSeleccionados(List<CatPredio> seleccionados) {
        this.seleccionados = seleccionados;
    }

    public Boolean getBtnDisabled() {
        return btnDisabled;
    }

    public void setBtnDisabled(Boolean btnDisabled) {
        this.btnDisabled = btnDisabled;
    }

}
