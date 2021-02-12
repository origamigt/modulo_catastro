/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans.component;

import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.context.RequestContext;

/**
 *
 * @author Angel Navarro
 * @Date 06/06/2016
 */
@Named
@ViewScoped
public class PredioRusticoDLG implements Serializable {

    private CatPredioLazy predios;
    private List<CatCiudadela> ciudadelas;
    private boolean filtro;
    private List<CatPredio> predioSelect;

    @javax.inject.Inject
    private Entitymanager serv;

    @PostConstruct
    protected void initView() {
        predios = new CatPredioLazy();
        ciudadelas = serv.findAllOrdered(CatCiudadela.class, new String[]{"nombre"}, new Boolean[]{true});
        filtro = false;
    }

    public SelectItem[] getLisUrbanizaciones() {
        int cantRegis = ciudadelas.size();
        SelectItem[] options = new SelectItem[cantRegis + 1];
        options[0] = new SelectItem("", "Seleccione");
        for (int i = 0; i < cantRegis; i++) {
            options[i + 1] = new SelectItem(ciudadelas.get(i).getNombre(), ciudadelas.get(i).getNombre());
        }
        return options;
    }

    public void seleccionarPredios() {
        RequestContext.getCurrentInstance().closeDialog(predioSelect);
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public List<CatCiudadela> getCiudadelas() {
        return ciudadelas;
    }

    public void setCiudadelas(List<CatCiudadela> ciudadelas) {
        this.ciudadelas = ciudadelas;
    }

    public boolean isFiltro() {
        return filtro;
    }

    public void setFiltro(boolean filtro) {
        this.filtro = filtro;
    }

    public List<CatPredio> getPredioSelect() {
        return predioSelect;
    }

    public void setPredioSelect(List<CatPredio> predioSelect) {
        this.predioSelect = predioSelect;
    }

    public PredioRusticoDLG() {
    }

}
