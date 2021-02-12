/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgmee.catastro.geotx.model;

import com.google.gson.annotations.Expose;
import com.origami.sgm.annotations.GreaterThan;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import util.Utils;

/**
 *
 * @author Dairon Freddy
 */
public class ManzanaModel implements Serializable {

    @GreaterThan
    @NotNull
    @Expose
    private short codProvincia;

    @GreaterThan
    @NotNull
    @Expose
    private short codCanton;

    @NotNull
    @GreaterThan
    @Expose
    private short codParroquia;

    @NotNull
    @GreaterThan
    @Expose
    private short codZona;

    @NotNull
    @GreaterThan
    @Expose
    private short codSector;

    @NotNull
    @GreaterThan
    @Expose
    private short codManzana;

    @GreaterThan
    @Expose
    private short codSolarNuevo;

    public ManzanaModel() {
    }

    public ManzanaModel(short codProvincia, short codCanton, short codParroquia, short codZona, short codSector, short codManzana, short codSolarNuevo) {
        this.codProvincia = codProvincia;
        this.codCanton = codCanton;
        this.codParroquia = codParroquia;
        this.codZona = codZona;
        this.codSector = codSector;
        this.codManzana = codManzana;
        this.codSolarNuevo = codSolarNuevo;
    }

    public short getCodProvincia() {
        return codProvincia;
    }

    public void setCodProvincia(short codProvincia) {
        this.codProvincia = codProvincia;
    }

    public short getCodCanton() {
        return codCanton;
    }

    public void setCodCanton(short codCanton) {
        this.codCanton = codCanton;
    }

    public short getCodParroquia() {
        return codParroquia;
    }

    public void setCodParroquia(short codParroquia) {
        this.codParroquia = codParroquia;
    }

    public short getCodZona() {
        return codZona;
    }

    public void setCodZona(short codZona) {
        this.codZona = codZona;
    }

    public short getCodSector() {
        return codSector;
    }

    public void setCodSector(short codSector) {
        this.codSector = codSector;
    }

    public short getCodManzana() {
        return codManzana;
    }

    public void setCodManzana(short codManzana) {
        this.codManzana = codManzana;
    }

    public short getCodSolarNuevo() {
        return codSolarNuevo;
    }

    public void setCodSolarNuevo(short codSolarNuevo) {
        this.codSolarNuevo = codSolarNuevo;
    }

    public String getClaveCatastralMz() {
        return Utils.completarCadenaConCeros(this.codProvincia + "", 2)
                + Utils.completarCadenaConCeros(this.codCanton + "", 2)
                + Utils.completarCadenaConCeros(this.codParroquia + "", 2)
                + Utils.completarCadenaConCeros(this.codZona + "", 2)
                + Utils.completarCadenaConCeros(this.codSector + "", 2)
                + Utils.completarCadenaConCeros(this.codManzana + "", 3);
    }
}
