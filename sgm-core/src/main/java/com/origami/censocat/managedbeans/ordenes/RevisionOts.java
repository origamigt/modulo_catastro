package com.origami.censocat.managedbeans.ordenes;

import com.origami.app.AppConfig;
import com.origami.censocat.entity.ordentrabajo.OrdenDet;
import com.origami.censocat.models.FotosModel;
import com.origami.censocat.restful.EstadoMovil;
import com.origami.censocat.restful.JsonUtils;
import com.origami.censocat.service.catastro.PredioEjb;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.FotoPredio;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import java.io.ByteArrayInputStream;
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
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import util.Faces;
import util.JsfUti;
import util.Utils;
import util.managedbeans.OtsUtil;

/**
 *
 * @author CarlosLoorVargas
 */
@Named
@ViewScoped
public class RevisionOts extends OtsUtil implements Serializable {

    private static final Logger LOG = Logger.getLogger(RevisionOts.class.getName());

    private static final long serialVersionUID = 1L;
    private BaseLazyDataModel<OrdenDet> ordenes;
    @Inject
    private PredioEjb preds;
    @Inject
    private CatastroServices catastroServices;
    @Inject
    protected OmegaUploader omegaUploader;
    @Inject
    protected AppConfig appconfig;
    @Inject
    private UserSession sess;
    private Map<String, Object> params;
    @Inject
    private ServletSession ss;

    @PostConstruct
    protected void load() {
        if (sess != null) {
            if (sess.getVarTemp() != null) {
                ordenes = new BaseLazyDataModel<>(OrdenDet.class, "id", "DESC");
                ordenes.setFilterss(new String[]{"numOrden"});
                ordenes.setFiltersValue(new Object[]{Long.parseLong(sess.getVarTemp())});
            } else {
                ordenes = new BaseLazyDataModel<>(OrdenDet.class, "id", "DESC");
            }
        }
    }

    public void editar(OrdenDet dt) {
        if (dt.getPredio() != null) {
            params = new HashMap();
            params.put("numPredio", dt.getPredio().getNumPredio());
            params.put("idPredio", dt.getPredio().getId());
            params.put("edit", true);
            ss.setParametros(params);
            Faces.redirectFacesNewTab("/faces/vistaprocesos/catastro/fichaPredial.xhtml");
        } else {
            Faces.messageWarning(null, "Advertencia", "El detalle de la orden debe ser procesada, para poder revisarla");
        }
    }

    public void revisar(OrdenDet dt) {
        try {
            if (!dt.getEstadoDet().equals(EstadoMovil.CENSADA)) {
                Faces.messageWarning(null, "Advertencia", "La orden no ha sido realizada por el investigador.");
                return;
            }
            if (!dt.getEstado()) {
                Faces.messageWarning(null, "Advertencia", "El detalle de la orden esta inactiva.");
                return;
            }
            params = new HashMap<>();
            params.put("idDetOrden", dt.getId());
            ss.setParametros(params);
            Faces.redirectFacesNewTab("/faces/vistaprocesos/catastro/ordenes/revisionPredio.xhtml");
        } catch (Exception e) {
            Faces.messageWarning(null, "Advertencia", "El detalle de la orden debe ser procesada, para poder revisarla");
        }
    }

    public void copiarImaganes(OrdenDet dt) {
        try {
            JsonUtils js = new JsonUtils();
            CatPredio pc = js.jsonToObject(dt.getDatoAct(), CatPredio.class);
            if (pc == null) {
                return;
            }
            if (Utils.isEmpty(pc.getFotos())) {
                System.out.println("No hay ids de fotos...");
                return;
            }
            List<FotosModel> fotosModel = null;
            if (dt.getOrden().getObservaciones() != null) {
                fotosModel = Arrays.asList(js.jsonToObject(dt.getOrden().getObservaciones(), FotosModel[].class));
            }
            List<FotosModel> model = new ArrayList<>();
            if (fotosModel != null) {
                for (String foto : pc.getFotos()) {
//            System.out.println("Foto: " + foto);
                    int indexOf = fotosModel.indexOf(new FotosModel(foto));
                    if (indexOf > -1) {
                        FotosModel get = fotosModel.get(fotosModel.indexOf(new FotosModel(foto)));
                        if (get != null) {
                            model.add(get);
                        }
                    } else {
                        String image = getImage(dt, foto);
                        if (image != null) {
                            FotosModel m = new FotosModel(foto);
                            m.setImage(image);
                            model.add(m);
                        }
                    }
                }
            }
            if (Utils.isNotEmpty(model)) {
                for (FotosModel ftm : model) {
                    Boolean subida = this.catastroServices.subirFotoPredio(new ByteArrayInputStream(ftm.getImageFromBase64()),
                            ftm.getId().concat(".jpg"), "image/jpg", pc);
                    System.out.println("Subida foto predio " + subida);
                }
            } else {
                Faces.messageInfo(null, "Info", "No hay fotos");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al copiar imagenes", e);
            Faces.messageWarning(null, "Advertencia", "Ocurrio un error al copiar las imagenes.");
        }
    }

    public void imprimirFicha(OrdenDet dt) {
        try {
            if (!dt.getEstadoDet().equals(EstadoMovil.FINALIZADO)) {
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

    private String getImage(OrdenDet ordenDet, String foto) {
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

    public void reprocesarPropietarios() {
        preds.reprocesarPropietarios();
    }

    public BaseLazyDataModel<OrdenDet> getOrdenes() {
        return ordenes;
    }

    public void setOrdenes(BaseLazyDataModel<OrdenDet> ordenes) {
        this.ordenes = ordenes;
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
    }

    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

}
