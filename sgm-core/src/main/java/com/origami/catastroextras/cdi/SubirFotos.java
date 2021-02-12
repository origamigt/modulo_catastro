/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template files, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastroextras.cdi;

import com.origami.config.SisVars;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import migrarprops.models.PredioClave;
import migrarprops.services.OmegaConfigs;
import migrarprops.subirFotos.MigrarFotos;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import util.FilesUtil;
import util.JsfUti;

/**
 * Controlador que permite subir las fotos por lotes
 *
 * @author Angel Navarro
 */
@Named
@ViewScoped
public class SubirFotos implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private static final Logger LOG = Logger.getLogger(SubirFotos.class.getName());
    private List<PredioClave> prediosError;
    private Date nombreDirectorio;
    private String directorio;
    private String directorioAbsolute;
    private List<UploadedFile> files;
    private Integer count = 0;
    private Integer countUploads = 0;
    private Boolean subidos = false;

    @Inject
    private com.origami.sgm.services.ejbs.censocat.OmegaConfigs configsFotos;

    @PostConstruct
    public void initView() {
        if (!JsfUti.isAjaxRequest()) {
            this.prediosError = new ArrayList<>();
            this.directorio = "" + new Date().getTime();
        }
    }

    public void nuevasFotos() {
        this.prediosError = new ArrayList<>();
        this.directorio = "" + new Date().getTime();
        this.count = 0;
        this.countUploads = 0;
        this.subidos = false;
        this.directorioAbsolute = null;
    }

    public void handleFileUpload(FileUploadEvent event) {
        this.files = new ArrayList<>();
        this.files.add(event.getFile());
        this.uploadFotos();
        this.count++;
    }

    public void uploadFotos() {
        try {
            // long currentTimeMillis1 = System.currentTimeMillis();
            File copyFileServer = FilesUtil.copyFileServer1(this.files, this.directorio);
            // System.out.println("Ciclo de subida >> " + (System.currentTimeMillis() -
            // currentTimeMillis1));
            if (this.directorioAbsolute == null) {
                this.directorioAbsolute = copyFileServer.getAbsolutePath();
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "subiendo Fotos.", e);
        }
    }

    public void uploadDataBase() {
        try {
            MigrarFotos mg = new MigrarFotos();
            OmegaConfigs config = new OmegaConfigs();
            // Configuraciones para base de fotos
            config.setDburl(this.configsFotos.getDburl());
            config.setDbuser(this.configsFotos.getDbuser());
            config.setDbpass(this.configsFotos.getDbpass());
            config.setTableName(this.configsFotos.getTableName());

            // Base predios
            config.setDbType("POSTGRES");
            config.setDburlTemp(SisVars.url);
            config.setDbuserTemp(SisVars.userName);
            config.setDbpassTemp(SisVars.password);
            config.setTableNameTemp("sgm_app.foto_predio");
            mg.setConfig(config);
            // SUBIMOS TODAS LAS FOTOS QUE ESTEN EN EL DIRECTORIO CREADO directorioAbsolute
            mg.upload(this.directorioAbsolute);
            // LISTADO DE FOTOS QUE NO FUERON SUBIDAS
            this.prediosError = mg.getWithoutpredio();
            // Obtenemos el numero que fueron subidas
            this.countUploads = mg.getCount();
            this.subidos = true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "subiendo Fotos.", e);
        }
    }

    public List<PredioClave> getPrediosError() {
        return this.prediosError;
    }

    public void setPrediosError(List<PredioClave> prediosError) {
        this.prediosError = prediosError;
    }

    public Date getNombreDirectorio() {
        return this.nombreDirectorio;
    }

    public void setNombreDirectorio(Date nombreDirectorio) {
        this.nombreDirectorio = nombreDirectorio;
    }

    public List<UploadedFile> getFiles() {
        return this.files;
    }

    public void setFiles(List<UploadedFile> files) {
        this.files = files;
    }

    public String getDirectorioAbsolute() {
        return this.directorioAbsolute;
    }

    public void setDirectorioAbsolute(String directorioAbsolute) {
        this.directorioAbsolute = directorioAbsolute;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getCountUploads() {
        return this.countUploads;
    }

    public void setCountUploads(Integer countUploads) {
        this.countUploads = countUploads;
    }

    public Boolean getSubidos() {
        return this.subidos;
    }

    public void setSubidos(Boolean subidos) {
        this.subidos = subidos;
    }

}
