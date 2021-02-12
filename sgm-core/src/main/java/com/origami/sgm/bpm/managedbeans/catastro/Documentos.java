/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.config.SisVars;
import com.origami.sgm.entities.GeDocumentos;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.jboss.logging.Logger;
import util.JsfUti;

/**
 *
 * @author Mariuly
 */
@Named
@ViewScoped
public class Documentos implements Serializable {

    public static final long serialVersionUID = 1l;
    @javax.inject.Inject
    private Entitymanager manager;

    @javax.inject.Inject
    private OmegaUploader servicesDoc;
    protected BaseLazyDataModel<GeDocumentos> documentos;
    protected GeDocumentos documento;

    @PostConstruct
    public void init() {
        try {
            documentos = new BaseLazyDataModel<GeDocumentos>(GeDocumentos.class, "fechaCreacion", "DESC");
        } catch (Exception e) {
            Logger.getLogger(Documentos.class.getName()).log(Logger.Level.FATAL, null, e);
        }
    }

    public void editar(GeDocumentos editar) {
        try {
            this.documento = editar;
        } catch (Exception e) {
            Logger.getLogger(Documentos.class.getName()).log(Logger.Level.FATAL, null, e);
        }
    }

    public void nuevo() {
        documento = new GeDocumentos();
    }

    public void guardar() {
        try {
            if (documento.getId() == null) {
                documento.setFechaCreacion(new Date());
            } else {
                documento.setFechaCreacion(new Date());
            }
            this.manager.persist(documento);
        } catch (Exception e) {
        }

    }

    public void eliminar(GeDocumentos delete) {
        try {
            this.manager.delete(delete);
            this.servicesDoc.deleteFile(delete.getTipoLegal().longValue());
        } catch (Exception e) {
            Logger.getLogger(Documentos.class.getName()).log(Logger.Level.FATAL, null, e);
        }
    }

    public BaseLazyDataModel<GeDocumentos> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(BaseLazyDataModel<GeDocumentos> documentos) {
        this.documentos = documentos;
    }

    public GeDocumentos getDocumento() {
        return documento;
    }

    public void setDocumento(GeDocumentos documento) {
        this.documento = documento;
    }

    public void descargarDocumento(String url, String type) {
        if (url != null && url.trim().length() > 0) {
            JsfUti.redirectNewTab(SisVars.urlbase + "DescargarDocsRepositorio?idDoc=" + url + "&type=" + type);
        }
    }

    public void showDocument1(String url, String type) {
        if (url != null && url.trim().length() > 0) {
            JsfUti.redirectNewTab(SisVars.urlbase + "showDocuments?idDoc=" + url + "&type=" + type);
        }
    }

}
