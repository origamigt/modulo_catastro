/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.managedbeans.mejoras;

import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.sgm.entities.CatParroquia;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatUbicacion;
import com.origami.sgm.entities.MejObra;
import com.origami.sgm.entities.MejObraUbicacion;
import com.origami.sgm.entities.MejTipoObra;
import com.origami.sgm.entities.models.PrediosManzanaDTO;
import com.origami.sgm.lazymodels.BaseLazyDataModel;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.services.interfaces.mejoras.MejorasServices;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.FlowEvent;
import util.Faces;
import util.JsfUti;

/**
 *
 * @author HenryPilco, XndySxnchez
 */
@Named(value = "obrasR")
@ViewScoped
public class ObrasRegister implements Serializable {

    private static final long serialVersionUID = 1L;
    @javax.inject.Inject
    private Entitymanager manager;
    private Map<String, Object> parametros = new HashMap<>();

    @Inject
    protected UserSession sess;

    @Inject
    private ServletSession servletSession;

    @Inject
    private MejorasServices mejorasServices;

    protected BaseLazyDataModel<MejObra> obras;
    protected MejObra obra;
    protected List<MejTipoObra> tiposObra;
    protected List<CatUbicacion> ubicaciones;
    protected CatUbicacion ubicacion;
    protected Long anioReporte;

    private Boolean nextPage = Boolean.FALSE;
    private String tipoDefinicion;

    private List<PrediosManzanaDTO> prediosXManzana, zonasPredios, sectoresPredios;

    private List<PrediosManzanaDTO> prediosDTOSeleccionados;
    protected CatPredioLazy predios;
    private List<CatPredio> catPredios;

    private AclUser user;
    protected Map<String, Object> paramt;
    protected CatParroquia parroquia;

    @PostConstruct
    public void initView() {
        try {
            paramt = new HashMap<>();
            paramt.put("usuario", sess.getName_user());
            user = (AclUser) manager.findObjectByParameter(AclUser.class, paramt);
            obras = new BaseLazyDataModel<MejObra>(MejObra.class, "id");
            obra = new MejObra();
            parametros.put("estado", Boolean.TRUE);
            tiposObra = manager.findObjectByParameterList(MejTipoObra.class, parametros);
            parametros = new HashMap<>();
            parametros.put("estado", Boolean.TRUE);
            loadRegistersObras();
        } catch (Exception e) {
            Logger.getLogger(Obras.class.getName()).log(Level.SEVERE, null, e);
        }

    }

    public void loadRegistersObras() {
        this.catPredios = new ArrayList<>();
        this.prediosXManzana = new ArrayList<>();
        this.prediosDTOSeleccionados = new ArrayList<>();
        this.predios = new CatPredioLazy("A");
        this.tipoDefinicion = "Parroquia";
        this.prediosXManzana = manager.getSqlQueryParametros(PrediosManzanaDTO.class, Querys.getPrediosXManzana, new String[]{"estado"}, new Object[]{"A"});
        this.zonasPredios = manager.getSqlQueryParametros(PrediosManzanaDTO.class, Querys.getZonaPredios, new String[]{"estado"}, new Object[]{"A"});
        this.sectoresPredios = manager.getSqlQueryParametros(PrediosManzanaDTO.class, Querys.getSectoresPredios, new String[]{"estado"}, new Object[]{"A"});
//        this.rubrosList = manager.findAll(QuerysFinanciero.getRubrosByTipoLiquidacionCodRubroASC, new String[]{"tipoLiq"}, new Object[]{13L});
        updatePrediosDTO();
    }

    public void updatePrediosDTO() {
        int count = 0;
        for (PrediosManzanaDTO prediosManzanaDTO : this.prediosXManzana) {
            prediosManzanaDTO.setId(count);
            this.prediosXManzana.set(count, prediosManzanaDTO);
            count++;
        }
        count = 0;
        for (PrediosManzanaDTO prediosManzanaDTO : this.zonasPredios) {
            prediosManzanaDTO.setId(count);
            this.zonasPredios.set(count, prediosManzanaDTO);
            count++;
        }
        count = 0;
        for (PrediosManzanaDTO prediosManzanaDTO : this.sectoresPredios) {
            prediosManzanaDTO.setId(count);
            this.sectoresPredios.set(count, prediosManzanaDTO);
            count++;
        }
    }

    public SelectItem[] getListTipoObras() {
        int cantRegis = tiposObra.size();
        SelectItem[] options = new SelectItem[cantRegis + 1];
        options[0] = new SelectItem("", "Seleccione");
        for (int i = 0; i < cantRegis; i++) {
            options[i + 1] = new SelectItem(tiposObra.get(i).getDescripcion(), tiposObra.get(i).getDescripcion());
        }
        return options;
    }

    public SelectItem[] getListUbicaciones() {
        int cantRegis = ubicaciones.size();
        SelectItem[] options = new SelectItem[cantRegis + 1];
        options[0] = new SelectItem("", "Seleccione");
        for (int i = 0; i < cantRegis; i++) {
            options[i + 1] = new SelectItem(ubicaciones.get(i).getNombre(), ubicaciones.get(i).getNombre());
        }
        return options;
    }

    public void registerObraPage() {
        Faces.redirectFaces("/faces/vistaprocesos/mejoras/_registroObras.xhtml");
        nextPage = Boolean.TRUE;
    }

//    public void redirectToCreateNewRubros(RenTipoLiquidacion tipoLiquidacion) {
//        servletSession.instanciarParametros();
//        servletSession.agregarParametro("idTipoLiquidacion", 13);
//        JsfUti.redirectFacesNewTab("/rentas/mantenimiento/asignacionrubros.xhtml");
//
//    }
    public String onFlowProcess(FlowEvent event) {
        if (event.getNewStep().equals("ubicacion")) {
            if (obra != null) {
                if (obra.getTipoObra() == null || obra.getAnio() == null || obra.getCostoTotal() == null
                        || obra.getSubsidio() == null || obra.getPlazo() == null) {
                    Faces.messageWarning(null, "Debe registrar todos los  campos Obligatorios", "");
                    Faces.update("growl");
                    return event.getOldStep();
                }
            }

        }
        if (event.getNewStep().equals("rubrosSelection")) {
            setValuesToCatUbicacion();
            if (this.tipoDefinicion.equals("Parroquia")) {

            } else {
                if (this.tipoDefinicion.equals("Zona") || this.tipoDefinicion.equals("Mz")
                        || this.tipoDefinicion.equals("Sector")) {
                    if (prediosDTOSeleccionados != null) {
                        if (prediosDTOSeleccionados.isEmpty()) {
                            Faces.messageWarning(null, "Debe Seleccionar la(s) Ubicaciones Correspondientes", "");
                            Faces.update("growl");
                            return event.getOldStep();
                        }
                    } else {
                        Faces.messageWarning(null, "Debe Seleccionar la(s) Ubicaciones Correspondientes", "");
                        Faces.update("growl");
                        return event.getOldStep();
                    }

                }
                if (this.tipoDefinicion.equals("Lote")) {
                    if (catPredios != null) {
                        if (catPredios.isEmpty()) {
                            Faces.messageWarning(null, "Debe Seleccionar la(s) Ubicaciones Correspondientes", "");
                            Faces.update("growl");
                            return event.getOldStep();
                        }
                    } else {
                        Faces.messageWarning(null, "Debe Seleccionar la(s) Ubicaciones Correspondientes", "");
                        Faces.update("growl");
                        return event.getOldStep();
                    }

                }
            }

        }
        if (event.getNewStep().equals("confirmar")) {
            for (PrediosManzanaDTO dto : prediosDTOSeleccionados) {
                System.out.println("dto" + dto);
            }
//            if (rubroSelected == null) {
//                Faces.messageWarning(null, "Debe Asignar Rubros al tipo de Obra que se Esta Registrando", "");
//                Faces.update("growl");
//                return event.getOldStep();
//            }
        }
        return event.getNewStep();
    }

    public void setValuesToCatUbicacion() {
        ubicaciones = new ArrayList<>();
        String nombreUbicacion = "";
        switch (tipoDefinicion) {
            case "Parroquia":
                this.ubicacion = new CatUbicacion();
                this.ubicacion.setEstado(Boolean.TRUE);
                this.ubicacion.setFechaIngreso(new Date());
                this.ubicacion.setUsuarioIngreso(this.user);
                this.ubicacion.setSolar((short) -1);
                if (parroquia != null) {
                    this.ubicacion.setNombre(tipoDefinicion + ": " + parroquia.getDescripcion());
                    this.ubicacion.setParroquia(parroquia.getCodigoParroquia().shortValue());
                    this.ubicacion.setZona((short) -1);
                    this.ubicacion.setSector((short) -1);
                    this.ubicacion.setMz((short) -1);

                } else {
                    this.ubicacion.setNombre("Todas las " + tipoDefinicion + "s");
                    this.ubicacion.setParroquia((short) -1);
                    this.ubicacion.setZona((short) -1);
                    this.ubicacion.setSector((short) -1);
                    this.ubicacion.setMz((short) -1);
                }
                this.ubicaciones.add(this.ubicacion);
                break;
            case "Zona":
            case "Sector":
            case "Mz":
                for (PrediosManzanaDTO dto : prediosDTOSeleccionados) {
                    this.ubicacion = new CatUbicacion();
                    this.ubicacion.setEstado(Boolean.TRUE);
                    this.ubicacion.setFechaIngreso(new Date());
                    this.ubicacion.setUsuarioIngreso(this.user);
                    nombreUbicacion = "Parroquia: " + dto.getParroquia().toString();
                    this.ubicacion.setParroquia(dto.getParroquia());
                    this.ubicacion.setSolar((short) -1);
                    //SI ES ZONA
                    if (dto.getZona() > 0 && dto.getSector() == 0 && dto.getMz() == 0 && dto.getSolar() == 0) {
                        nombreUbicacion = nombreUbicacion + " Zona:" + dto.getZona().toString();
                        this.ubicacion.setNombre(nombreUbicacion);
                        this.ubicacion.setZona(dto.getZona());
                        this.ubicacion.setSector((short) -1);
                        this.ubicacion.setMz((short) -1);
                    }
                    //SI ES SECTOR
                    if (tipoDefinicion.equals("Sector")) {
                        nombreUbicacion = nombreUbicacion + " Zona:" + dto.getZona().toString() + " Sector: " + dto.getSector();
                        this.ubicacion.setNombre(nombreUbicacion);
                        this.ubicacion.setZona(dto.getZona());
                        this.ubicacion.setSector(dto.getSector());
                        this.ubicacion.setMz((short) -1);
                    }
                    //SI ES MZ
                    if (tipoDefinicion.equals("Mz")) {
                        nombreUbicacion = nombreUbicacion + " Zona:" + dto.getZona().toString() + " Sector: " + dto.getSector().toString() + " Mz: " + dto.getMz().toString();
                        this.ubicacion.setNombre(nombreUbicacion);
                        this.ubicacion.setZona(dto.getZona());
                        this.ubicacion.setSector(dto.getSector());
                        this.ubicacion.setMz(dto.getMz());
                    }
                    this.ubicaciones.add(this.ubicacion);
                }
                break;
            case "Lote":
                for (CatPredio p : catPredios) {
                    this.ubicacion = new CatUbicacion();
                    nombreUbicacion = "Parroquia: " + p.getParroquia().toString() + " Zona:" + p.getZona().toString() + " Sector: " + p.getSector().toString() + " Mz: " + p.getMz().toString();
                    this.ubicacion.setNombre(nombreUbicacion);
                    this.ubicacion.setEstado(Boolean.TRUE);
                    this.ubicacion.setUsuarioIngreso(this.user);
                    this.ubicacion.setFechaIngreso(new Date());
                    this.ubicacion.setParroquia(p.getParroquia());
                    this.ubicacion.setZona(p.getZona());
                    this.ubicacion.setSector(p.getSector());
                    this.ubicacion.setMz(p.getMz());
                    this.ubicacion.setSolar(p.getSolar());
                    this.ubicaciones.add(this.ubicacion);
                }
                break;
            default:
                break;
        }
    }

    public void cleanVarPrediosDTO() {
        this.catPredios = new ArrayList<>();
        this.prediosDTOSeleccionados = new ArrayList<>();
    }

    public void seleccionarObra(MejObra obra) {
        if (obra == null) {
            this.obra = new MejObra();
        } else {
            this.obra = obra;
        }
    }

    public void calcularValores() {
        if (obra != null && obra.getCostoTotal() != null && obra.getSubsidio() != null) {
            obra.setValorSubcidiado(obra.getCostoTotal().multiply(obra.getSubsidio()).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP));
            obra.setValorRecuperar(obra.getCostoTotal().subtract(obra.getValorSubcidiado()));
        }
        if (obra != null && obra.getPlazo() != null && obra.getValorRecuperar() != null) {
            obra.setValorEmisionAnual(obra.getValorRecuperar().divide(new BigDecimal(obra.getPlazo()), 2, BigDecimal.ROUND_HALF_UP));
        }
    }

    public void saveUbicaciones() {
        try {
            this.ubicaciones = mejorasServices.saveUbicaciones(this.ubicaciones);
        } catch (Exception e) {
            Logger.getLogger(Obras.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void guardarObra() {
        try {
//            obra.setRubro(rubroSelected);
            this.saveUbicaciones();
            obra = mejorasServices.saveObra(obra, this.ubicaciones);
            if (obra != null) {
                JsfUti.messageInfo(null, "Mensaje.", "Guardado Exitoso.");
            } else {
                JsfUti.messageError(null, "Mensaje.", "Ocurrio un Problema mientras se  Persistian los Datos.");
            }
        } catch (Exception e) {
            Logger.getLogger(Obras.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void setUbicacionesByObras() {
        List<MejObraUbicacion> mejObraUbicacions = new ArrayList<>();
    }

    public void generarReporteObras() {
        try {
            String path = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/");

            servletSession.instanciarParametros();
            servletSession.setTieneDatasource(true);
            servletSession.setNombreReporte("obras");
            servletSession.setNombreSubCarpeta("mejoras");
            servletSession.agregarParametro("LOGO", path + SisVars.sisLogo);
            servletSession.agregarParametro("ANIO", this.anioReporte);
            JsfUti.redirectNewTab(SisVars.urlServidorPublica + "/Documento");

        } catch (Exception e) {
            Logger.getLogger(Obras.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public List<CatParroquia> getParroquias() {
        Map<String, Object> paramt = new HashMap<>();
        paramt.put("idCanton", manager.find(Querys.getParroquiasByCanton, new String[]{"codigoNacional", "codNac"}, new Object[]{SisVars.CANTON, SisVars.PROVINCIA}));
        return manager.findObjectByParameterOrderList(CatParroquia.class, paramt, new String[]{"idCanton"}, true);
    }

    public MejObra getObra() {
        return obra;
    }

    public void setObra(MejObra obra) {
        this.obra = obra;
    }

    public BaseLazyDataModel<MejObra> getObras() {
        return obras;
    }

    public void setObras(BaseLazyDataModel<MejObra> obras) {
        this.obras = obras;
    }

    public List<MejTipoObra> getTiposObra() {
        return tiposObra;
    }

    public void setTiposObra(List<MejTipoObra> tiposObra) {
        this.tiposObra = tiposObra;
    }

    public List<CatUbicacion> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<CatUbicacion> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }

    public ServletSession getServletSession() {
        return servletSession;
    }

    public void setServletSession(ServletSession servletSession) {
        this.servletSession = servletSession;
    }

    public Long getAnioReporte() {
        return anioReporte;
    }

    public void setAnioReporte(Long anioReporte) {
        this.anioReporte = anioReporte;
    }

    public List<PrediosManzanaDTO> getPrediosXManzana() {
        return prediosXManzana;
    }

    public void setPrediosXManzana(List<PrediosManzanaDTO> prediosXManzana) {
        this.prediosXManzana = prediosXManzana;
    }

    public String getTipoDefinicion() {
        return tipoDefinicion;
    }

    public void setTipoDefinicion(String tipoDefinicion) {
        this.tipoDefinicion = tipoDefinicion;
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public List<PrediosManzanaDTO> getZonasPredios() {
        return zonasPredios;
    }

    public void setZonasPredios(List<PrediosManzanaDTO> zonasPredios) {
        this.zonasPredios = zonasPredios;
    }

    public CatParroquia getParroquia() {
        return parroquia;
    }

    public void setParroquia(CatParroquia parroquia) {
        this.parroquia = parroquia;
    }

    public List<PrediosManzanaDTO> getSectoresPredios() {
        return sectoresPredios;
    }

    public void setSectoresPredios(List<PrediosManzanaDTO> sectoresPredios) {
        this.sectoresPredios = sectoresPredios;
    }

    public List<PrediosManzanaDTO> getPrediosDTOSeleccionados() {
        return prediosDTOSeleccionados;
    }

    public void setPrediosDTOSeleccionados(List<PrediosManzanaDTO> prediosDTOSeleccionados) {
        this.prediosDTOSeleccionados = prediosDTOSeleccionados;
    }

    public List<CatPredio> getCatPredios() {
        return catPredios;
    }

    public void setCatPredios(List<CatPredio> catPredios) {
        this.catPredios = catPredios;
    }

    public CatUbicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(CatUbicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

}
