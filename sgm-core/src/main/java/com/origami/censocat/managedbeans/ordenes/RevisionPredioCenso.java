/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.managedbeans.ordenes;

import com.origami.app.AppConfig;
import com.origami.censocat.entity.ordentrabajo.OrdenDet;
import com.origami.censocat.models.FotosModel;
import com.origami.censocat.restful.EstadoMovil;
import com.origami.censocat.restful.JsonUtils;
import com.origami.censocat.service.catastro.PredioEjb;
import com.origami.censocat.service.ordenes.OrdenService;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioObraInterna;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.component.accordionpanel.AccordionPanel;
import org.primefaces.event.TabChangeEvent;
import org.springframework.beans.BeansException;
import util.Faces;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class RevisionPredioCenso implements Serializable {

    private static final Logger LOG = Logger.getLogger(RevisionPredioCenso.class.getName());

    @Inject
    protected ServletSession ss;
    @Inject
    protected UserSession session;
    @Inject
    protected PredioEjb preds;
    @javax.inject.Inject
    protected OrdenService ots;
    @javax.inject.Inject
    protected CatastroServices catastroServices;
    @Inject
    protected Entitymanager em;
    @Inject
    protected OmegaUploader omegaUploader;
    @Inject
    protected AppConfig appconfig;

    protected CatPredio predioCenso;
    protected CatPredio predioAct;
    protected CatEscritura escritura;
    protected List<FotosModel> fotosModel;
    protected List<FotosModel> model;
    protected List<FotoPredio> fotos;

    protected OrdenDet ordenDet;
    protected JsonUtils jsUtils;
    protected Integer index = 0;
    protected FotosModel fotoModel;

    protected CatPredioEdificacion bloq;
    protected CatPredioObraInterna obra;

    @PostConstruct
    public void initView() {
        try {
            if (!JsfUti.isAjaxRequest()) {
                if (ss.getParametros() == null) {
                    Faces.redirectFaces2("/faces/vistaprocesos/catastro/ordenes/revision.xhtml");
                    return;
                }
                if (ss.getParametros().get("idDetOrden") != null) {
                    ordenDet = ots.getOrdenDet(Long.valueOf(ss.getParametros().get("idDetOrden").toString()));
                    System.out.println("Id OrdenDet " + ordenDet.getId());
                    if (ordenDet != null) {
                        fotosModel = new ArrayList<>();
                        if (ordenDet.getPredio() != null) {
                            Long idPredio = ordenDet.getPredio().getId();
                            ordenDet.setPredio(null);
                            predioAct = catastroServices.getPredioId(idPredio);
                        } else {
                            predioAct = catastroServices.getPredioNumPredio(ordenDet.getNumPredio());
                        }
                        escritura = Utils.get(predioAct.getCatEscrituraCollection(), 0);
                        jsUtils = new JsonUtils();
                        predioCenso = jsUtils.jsonToObject(ordenDet.getDatoAct(), CatPredio.class);
                        predioCenso.setId(predioAct.getId());

                        if (ordenDet.getOrden().getObservaciones() != null) {
                            fotosModel = Arrays.asList(jsUtils.jsonToObject(ordenDet.getOrden().getObservaciones(), FotosModel[].class));
                        } else {
                            if (ordenDet.getObservaciones() != null) {
                                if (ordenDet.getObservaciones().contains("\"id\"")) {
                                    fotosModel = Arrays.asList(jsUtils.jsonToObject(ordenDet.getObservaciones(), FotosModel[].class));
                                }
                            }
                        }
                        predioCenso.setSolar(predioAct.getSolar());
                        predioCenso.setLote(predioAct.getSolar());
                        cargarFotos();
                    } else {
                        Faces.redirectFaces2("/faces/vistaprocesos/catastro/ordenes/revision.xhtml");
                    }
                } else {
                    Faces.redirectFaces2("/faces/vistaprocesos/catastro/ordenes/revision.xhtml");
                }
            }
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Init View", e);
        }
    }

    public void cargarFotos() {
        if (predioAct == null) {
            return;
        }
        fotos = em.findAll(Querys.getFotosIdPredio, new String[]{"predio"}, new Object[]{predioAct.getId()});
        if (fotos != null) {
            fotos.size();
        }

    }

    public void tabChange(TabChangeEvent event) {
        AccordionPanel tv = (AccordionPanel) event.getComponent();
        this.setIndex(Integer.valueOf(tv.getActiveIndex()));
    }

    public void procesarDatos() {
        try {
            System.out.println("Procesando...");
//            String[] ignoreProperties = new String[]{"numPredio", "id", "clave_cat", "instCreacion",
//                "predialant", "catPredios6.predio", "catPredios4.predio"};
//            BeanUtils.copyProperties(predioCenso, predioAct, ignoreProperties);
            Boolean status = ots.procesarPredio(predioCenso, predioAct, fotosModel);
            if (status) {
                ordenDet.setPredio(predioAct);
                ordenDet.setFecAct(new Date());
                ordenDet.setUsrAct(session.getName_user());
                ordenDet.setEstadoDet(EstadoMovil.FINALIZADO);
                ots.guardarDetOrden(ordenDet);
                Faces.messageInfo(null, "Información", "Detalle de la orden fue procesada correctamente.");
                Faces.redirectFaces("/faces/vistaprocesos/catastro/ordenes/revision.xhtml");
            } else {
                Faces.messageError(null, "Error", "Ocurrio un error al procesar el detalle de la orden.");
            }
        } catch (BeansException e) {
            LOG.log(Level.SEVERE, "procesarDatos", e);
        }
    }

    public void rechazar() {
        try {
            ordenDet.setEstadoDet(EstadoMovil.PENDIENTE);
            ordenDet.setPredio(predioAct);
            ordenDet.setDatoRef(ordenDet.getDatoAct());
            ots.guardarDetOrden(ordenDet);
            ordenDet.getOrden().setEstadoOt(EstadoMovil.PENDIENTE);
            ordenDet.getOrden().setFecCre(new Date());
            ots.guardarOrden(ordenDet.getOrden());
            Faces.messageInfo(null, "Información", "Detalle de la orden # " + ordenDet.getOrden().getNumOrden() + " rechazada correctamente.");
            Faces.redirectFaces("/faces/vistaprocesos/catastro/ordenes/revision.xhtml");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "rechazar", e);
            Faces.messageError(null, "Error", "Ocurrio un error al rechazar el detalle de la orden.");
        }
    }

    public List<FotosModel> fotos(List<String> fts) {
        if (Utils.isEmpty(fts)) {
            System.out.println("OrdenDet " + ordenDet.getId() + " No hay ids de fotos...");
            return null;
        }
        if (model == null) {
            model = new ArrayList<>();
        }
//        System.out.println("FotosModel " + fotosModel);
        if (model.isEmpty()) {
            for (String foto : fts) {
//            System.out.println("Foto: " + foto);
                int indexOf = fotosModel.indexOf(new FotosModel(foto));
                if (indexOf > -1) {
                    FotosModel get = fotosModel.get(fotosModel.indexOf(new FotosModel(foto)));
                    if (get != null) {
                        model.add(get);
                    }
                } else {
                    String image = getImage(foto);
                    if (image != null) {
                        FotosModel m = new FotosModel(foto);
                        m.setImage(image);
                        model.add(m);
                    }
                }
            }
            System.out.println("OrdenDet " + ordenDet.getId() + " Imagenes " + model);
        }
        return model;
    }

    private String getImage(String foto) {
        if (ordenDet.getObservaciones() != null) {
            String[] split = ordenDet.getObservaciones().split(";");
            if (split != null) {
                String id = foto.concat("||");
                for (String im : split) {
                    if (im.startsWith(id)) {
                        return im.replace(id, "");
                    }
                }
            }
        }
        return null;
    }

    public void imprimirFicha(OrdenDet dt) {
        try {
            if (!dt.getEstado().equals(EstadoMovil.FINALIZADO)) {
                JsfUti.messageWarning(null, "Advertencia", "Debe estar finalizado para realizar la impresion de la ficha");
                return;
            }
            JsonUtils js = new JsonUtils();
            CatPredio predio = js.jsonToObject(dt.getDatoAct(), CatPredio.class);
            if (predio != null) {
                ss.instanciarParametros();
                String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");
                int numFotos = 1;
                List<FotoPredio> fotos = catastroServices.getFotosPredio(predio);
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

                ss.agregarParametro("IMAGEN_PREDIO", appconfig.formatUrlPredio(predio.getClaveCat(), predio.getPropiedadHorizontal()));
                ss.agregarParametro("COLINDANTES", appconfig.formatUrlPredioColindates(predio.getClaveCat()));
                ss.agregarParametro("MUNICIPIO", SisVars.NOMBREMUNICIPIO);
                ss.agregarParametro("predio", predio.getId());
                ss.agregarParametro("LOGO", SisVars.sisLogo);
                ss.agregarParametro("LOGO2", SisVars.sisLogo1);
                ss.setNombreReporte("Ficha predial " + predio.getNumPredio());
                ss.agregarParametro("SUBREPORT_DIR", path + "reportes/catastro/Ibarra/");
                ss.agregarParametro("SUBREPORT_DIR_TITLE", path + "reportes/");
                // Parametros para agregar un reporte al final del primero
                Map<String, Object> reporteadd = new HashMap<>();
                ss.setAgregarReporte(Boolean.FALSE);
                if (predio.getCatPredioEdificacionCollection() != null && !Utils.isEmpty(predio.getCatPredioEdificacionCollection())) {
                    int count = 0;
                    String edificaciones = "";
                    ss.setAgregarReporte(Boolean.TRUE);
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
                ss.setNombreSubCarpeta("catastro/Ibarra");
                ss.setNombreReporte("fichaMiduvi");
                ss.setNombreDocumento("fichaPredial-" + predio.getId() + "-(" + new Date() + ")");
                ss.setTieneDatasource(Boolean.TRUE);

                JsfUti.redirectNewTab(SisVars.urlbase + "Documento");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

//<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public CatPredio getPredioCenso() {
        return predioCenso;
    }

    public void setPredioCenso(CatPredio predioCenso) {
        this.predioCenso = predioCenso;
    }

    public CatPredio getPredioAct() {
        return predioAct;
    }

    public void setPredioAct(CatPredio predioAct) {
        this.predioAct = predioAct;
    }

    public OrdenDet getOrdenDet() {
        return ordenDet;
    }

    public void setOrdenDet(OrdenDet ordenDet) {
        this.ordenDet = ordenDet;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public CatEscritura getEscritura() {
        return escritura;
    }

    public void setEscritura(CatEscritura escritura) {
        this.escritura = escritura;
    }

    public List<FotosModel> getFotosModel() {
        return fotosModel;
    }

    public void setFotosModel(List<FotosModel> fotosModel) {
        this.fotosModel = fotosModel;
    }

    public FotosModel getFotoModel() {
        return fotoModel;
    }

    public void setFotoModel(FotosModel fotoModel) {
        this.fotoModel = fotoModel;
    }

    public CatPredioEdificacion getBloq() {
        return bloq;
    }

    public void setBloq(CatPredioEdificacion bloq) {
        this.bloq = bloq;
    }

    public CatPredioObraInterna getObra() {
        return obra;
    }

    public void setObra(CatPredioObraInterna obra) {
        this.obra = obra;
    }
//</editor-fold>

    public List<FotoPredio> getFotos() {
        return fotos;
    }

    public void setFotos(List<FotoPredio> fotos) {
        this.fotos = fotos;
    }

}
