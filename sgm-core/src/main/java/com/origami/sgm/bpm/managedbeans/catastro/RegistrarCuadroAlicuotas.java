/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.bpm.managedbeans.catastro;

import com.origami.catastro.services.impl.ServiceDataBaseIb;
import com.origami.config.SisVars;
import com.origami.session.ServletSession;
import com.origami.session.UserSession;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.CatCanton;
import com.origami.sgm.entities.CatCiudadela;
import com.origami.sgm.entities.CatEnte;
import com.origami.sgm.entities.CatEscritura;
import com.origami.sgm.entities.CatPredio;
import com.origami.sgm.entities.CatPredioAlicuotaComponente;
import com.origami.sgm.entities.CatPredioEdificacion;
import com.origami.sgm.entities.CatPredioPropietario;
import com.origami.sgm.entities.CatPredioS4;
import com.origami.sgm.entities.CatTiposDominio;
import com.origami.sgm.entities.CtlgItem;
import com.origami.sgm.entities.GeDocumentos;
import com.origami.sgm.events.ValorarPredioPost;
import com.origami.sgm.lazymodels.CatEnteLazy;
import com.origami.sgm.lazymodels.CatPredioLazy;
import com.origami.sgm.predio.models.NivelModel;
import com.origami.sgm.services.ejbs.censocat.OmegaUploader;
import com.origami.sgm.services.ejbs.censocat.UploadDocumento;
import com.origami.sgm.services.interfaces.catastro.CatastroServices;
import com.origami.sgm.services.interfaces.catastro.FusionDivisionServices;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.ModelPhs;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.FlowEvent;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import util.Faces;
import util.HiberUtil;
import util.JsfUti;
import util.Utils;

/**
 *
 * @author dfcalderio
 */
@Named //(value = "cuadroAlicuotasView")
@ViewScoped
public class RegistrarCuadroAlicuotas implements Serializable {

    private static final Logger LOG = Logger.getLogger(RegistrarCuadroAlicuotas.class.getName());
    @javax.inject.Inject
    protected Entitymanager manager;
    @javax.inject.Inject
    protected FusionDivisionServices fusionDivisionEjb;
    @Inject
    protected UserSession sess;
    @Inject
    protected ServletSession ss;
    @javax.inject.Inject
    protected CatastroServices catastroService;
    @Inject
    protected UploadDocumento documentoBean;
    @Inject
    protected OmegaUploader fserv;
    private Long idDoc;

    protected CatPredioLazy predios;
    protected CatPredio predioSeleccionado;
    protected CatPredio predioMatriz;
    protected List<CatCiudadela> ciudadelas;
    protected Collection<CatPredioEdificacion> edificaciones;
    protected CatPredioEdificacion edificacionSeleccionada;
    protected CatEscritura escritura;
    protected int cantAlicuotasBloque;
    protected List<NivelModel> niveles;
    protected List<CatPredio> prediosGenerados;
    protected List<CatPredio> prediosGeneradosFiltrados;
    protected Boolean fichaMatriz;
    protected BigDecimal totalAlicuotasComunal;
    protected CatPredioPropietario propietarioSeleccionado;
    protected int index;

    protected CatEnteLazy entes;
    protected CatEnte enteSeleccionado;
    protected CatPredioPropietario prop;

    protected CatPredioAlicuotaComponente componenteSeleccionado;
    protected List<CatPredioAlicuotaComponente> componenteEliminar;
    protected List<CatCanton> cantones;
    protected boolean tieneEscritura;
    protected boolean skipGenerarPh = true;

    private List<BloqueGeoData> bloqueGis;
    private List<BloqueGeoData> bloqueGisMod;
    private BloqueGeoData geoBloqueSeleccionado;
    private List<BloqueGeoData> bloqueGisFinal;
    private List<BloqueGeoData> phsGeneradas;

    private final String keyGeoBloque = "geoBloque";

    @Inject
    protected ServiceDataBaseIb dataBaseIb;
    // Events
    @Inject
    protected Event<ValorarPredioPost> eventValoracion;
    protected Map<Short, ModelPhs> count;
    private Boolean validarBloqueGrafica = true;

    @PostConstruct
    public void init() {
        predios = new CatPredioLazy("A");
        predioSeleccionado = new CatPredio();
        ciudadelas = manager.findAllOrdered(CatCiudadela.class, new String[]{"nombre"}, new Boolean[]{true});
        escritura = new CatEscritura();
        escritura.setTipoPh(1);
        escritura.setCantAlicuotas(0);
        escritura.setCantBloques(0);
        edificaciones = new ArrayList<>();
        edificacionSeleccionada = new CatPredioEdificacion();
        edificacionSeleccionada.setNiveles(new ArrayList<>());
        niveles = new ArrayList<>();
        prediosGenerados = new ArrayList<>();
        fichaMatriz = Boolean.FALSE;
        totalAlicuotasComunal = BigDecimal.ZERO;
        enteSeleccionado = new CatEnte();
        entes = new CatEnteLazy(true);
        componenteSeleccionado = new CatPredioAlicuotaComponente();
        componenteEliminar = new ArrayList<>();
        cantones = manager.findAll(CatCanton.class);
    }

    /**
     * Registra cada una de las alicuotas que fueron generadas en el proceso,
     * ademas envia a actualizar la clumna codigo_ph de la tabla de bloque
     */
    public void grabarCuadroAlicuotas() {
        BigDecimal alicuotas = BigDecimal.ZERO;
        for (CatPredio pg : prediosGenerados) {
            if (pg.getAlicuotaUtil() == null) {
                JsfUti.messageError(null, "Error", "Hay un phs hijas no tiene alicuota.");
                return;
            }
            if (pg.getAlicuotaUtil().doubleValue() == 0) {
                JsfUti.messageError(null, "Error", "Hay un phs hijas que tiene alicuota 0.");
                return;
            }
            alicuotas = alicuotas.add(pg.getAlicuotaUtil());
        }
        if (alicuotas.doubleValue() < 90) {
            JsfUti.messageError(null, "Error", "La suma de alicutas es inferior al 90%.");
            return;
        }
        if (alicuotas.doubleValue() > 101) {
            JsfUti.messageError(null, "Error", "La suma de alicutas es mayor al 100%.");
            return;
        }
        try {
            if (validarSumaAlicuotas()) {
                componenteEliminar.forEach((cpc) -> {
                    manager.delete(cpc);
                });
                if (!fichaMatriz) {
                    if (tieneEscritura) {
                        String[] param = {"idPredio"};
                        Object[] val = {predioMatriz.getId()};
                        List<CatEscritura> escs = manager.findAll(Querys.getEscriturasByPredioDesc, param, val);
                        if (!escs.isEmpty()) {
                            escs.stream().map((e) -> {
                                e.setEstado("I");
                                return e;
                            }).forEachOrdered((e) -> {
                                manager.persist(e);
                            });
                        }
                        escritura.setAreaSolar(predioMatriz.getAreaSolar());
                        escritura.setPredio(predioMatriz);
                        escritura.setEstado("A");
                        escritura.setFecCre(new Date());
                        escritura = (CatEscritura) manager.persist(escritura);
                    }

                    predioMatriz.setFichaMadre(Boolean.TRUE);
                    predioMatriz.setPropiedadHorizontal(Boolean.FALSE);
                    predioMatriz.setCantAlicuotas(escritura.getCantAlicuotas());
                    predioMatriz = (CatPredio) manager.persist(predioMatriz);

                }
                try {
                    if (prediosGenerados != null) {
                        prediosGenerados.forEach((p) -> {
                            if (p.getEscritura() != null) {
                                if (p.getEscritura().getIdEscritura() != null) {
                                    p.setTieneEscritura(true);
                                }
                            }
                        });
                        fusionDivisionEjb.registrarPredios(prediosGenerados, predioMatriz);
                        try {
                            this.dataBaseIb.crearBloqueGisPhs(phsGeneradas);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Actulizar Escrituras", e);
                }
                JsfUti.messageInfo(null, "Gestion", " de alicuotas realizado satisfactoriamente.");
                JsfUti.update("growl");
                JsfUti.redirectFaces("/faces/vistaprocesos/catastro/gestionarPH/gestionarPH.xhtml");
            } else {
                JsfUti.messageWarning(null, "Suma de alicuotas es mayor a 100, rectificar la asignación.", "");
                JsfUti.update("growl");
            }
        } catch (Exception e) {
            JsfUti.messageError(null, "Error", " Hubo un error al procesar la inforamción. " + e.getMessage());
            JsfUti.update("growl");
            JsfUti.redirectFaces("/faces/vistaprocesos/catastro/gestionarPH/gestionarPH.xhtml");
        }
    }

    /**
     * Procesa el numero de alicuota que se especifican.
     */
    public void procesarCuadroAlicuotas() {
        int cantidad = 0;
        cantidad = edificaciones.stream().map((e) -> e.getCantAlicuotas()).reduce(cantidad, Integer::sum);
        if (cantidad != count.size()) {
            JsfUti.messageWarning(null, "Advertencia", "No coincide cantidad de alicutas ingresadas con phs hijas de la base grafica");
            JsfUti.update("growl");
        } else {
            niveles = new ArrayList<>();
            edificaciones.stream().sorted((CatPredioEdificacion ed1, CatPredioEdificacion ed2) -> ed1.getNoEdificacion().compareTo(ed2.getNoEdificacion()))
                    .forEach((e) -> {
                        List<NivelModel> generarNiveles = generarNiveles(e);
                        if (generarNiveles == null) {
                            return;
                        }
                        niveles.addAll(generarNiveles);
                    });
        }
    }

    /**
     * Inicializa el modelo de datos de los niveles.
     */
    public void procesarCuadroAlicuotasCancelar() {
        niveles = new ArrayList<>();
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

    public List<CatTiposDominio> getDominios() {
        return manager.findAllObjectOrder(CatTiposDominio.class, new String[]{"nombre"}, true);
    }

    public List<CtlgItem> getCatalogoItems(String argumento) {
        HiberUtil.newTransaction();
        List<CtlgItem> ctlgItem = (List<CtlgItem>) manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{argumento});
        return ctlgItem;
    }

    public Integer cantidadEdificaciones(CatPredio p) {

        Collection<CatPredioEdificacion> eds = manager.findAll(Querys.edificacionesByPredio, new String[]{"idPredio"}, new Object[]{p.getId()});
        return eds != null ? eds.size() : 0;
    }

    public String onFlowProcess(FlowEvent event) {
//        if (event.getNewStep().equals("escritura_ficha_madre")) {
        if (event.getNewStep().equals("registro_bloques")) {
            if (predioMatriz == null) {
                JsfUti.messageWarning(null, "Debe seleccionar una predio para continuar", "");
                JsfUti.update("growl");
                return event.getOldStep();
            } else {
//                this.countPhsHijas();
                if (!prediosGenerados.isEmpty()) {
                    if (!this.buscarPhsHijas(event)) {
                        JsfUti.messageWarning(null, "No se encontraron Phs hijas en la capa bloques", "");
                        return event.getOldStep();
                    }
                    sumaAlicuotasComunal();
                    if (Utils.isNotEmpty(bloqueGis)) {
                        return "registro_bloques";
                    }
                    if (Utils.isNotEmpty(bloqueGisMod)) {
                        return "registro_bloques";
                    }
                    return "predios_generados";
                }
                if (cantidadEdificaciones(predioMatriz) == 0) {
                    JsfUti.messageWarning(null, "Debe ingresar Phs hijas en la capa bloques", "");
                    if (!this.buscarPhsHijas(event)) {
                        JsfUti.messageWarning(null, "No se encontraron Phs hijas en la capa bloques", "");
                        return event.getOldStep();
                    }
                    JsfUti.update("growl");
                    return "registro_bloques";
                } else {
                    this.buscarPhsHijas(event);
                }
            }
        }
        if (event.getNewStep().equals("escritura_ficha_madre")) {
            if (Utils.isNotEmpty(bloqueGis)) {
                JsfUti.messageWarning(null, "Advertencia", "Debe ingresar todos los bloques.");
                return event.getOldStep();
            } else {
                if (count == null) {
                    count = new HashMap<Short, ModelPhs>();
                }
                this.bloqueGisFinal.forEach((dg) -> {
                    dg.getNiveles().forEach((nvb) -> {
                        ModelPhs get = count.get(nvb.getNumeracion());
                        if (get == null) {
                            count.put(nvb.getNumeracion(), new ModelPhs(nvb.getPiso(), nvb.getNumeracion(), nvb.getNum()));
                        }
                    });
                });
//                System.out.println(count);
                escritura.setCantAlicuotas(count.size());
                predioMatriz = this.catastroService.getPredioId(predioMatriz.getId());
            }
        }
        if (event.getNewStep().equals("predios_generados")) {
            if (!prediosGenerados.isEmpty()) {
                edificaciones.stream().sorted((CatPredioEdificacion o1, CatPredioEdificacion o2) -> o1.getNoEdificacion().compareTo(o2.getNoEdificacion()))
                        .forEach((CatPredioEdificacion edf) -> {
                            int index = bloqueGisFinal.indexOf(new BloqueGeoData(edf.getNoEdificacion()));
                            if (index > -1) {
                                BloqueGeoData get = bloqueGisFinal.get(index);
                                edf.setCantAlicuotas(get.getClavePorPiso().size());
                            }
                        });
                if (escritura.getCantAlicuotas() != count.size()) {
                    JsfUti.messageWarning(null, "Advertencia", "No coincide cantidad de alicutas ingresadas con phs hijas de la base grafica");
                    return event.getOldStep();
                }
                this.generarAlicuotas();
                return "predios_generados";
            }
            if (niveles.isEmpty()) {
                JsfUti.messageWarning(null, "Debe procesar las edificaciones para continuar", "");
                JsfUti.update("growl");
                return event.getOldStep();
            } else {
                if (!validarAlicuotasPorNivel()) {
                    return event.getOldStep();
                } else {
                    generarAlicuotas();
                }
            }
        }
        if (event.getOldStep().equals("predios_generados")) {
            niveles.clear();
            prediosGenerados.clear();
            predioSeleccionado = new CatPredio();
            predioMatriz = null;
            return "ficha_madre";
        }
        if (event.getNewStep().equals("generar_phs")) {
            skipGenerarPh = escritura.getTipoPh() == 1;
            edificaciones.stream().sorted((CatPredioEdificacion o1, CatPredioEdificacion o2) -> o1.getNoEdificacion().compareTo(o2.getNoEdificacion()))
                    .forEach((CatPredioEdificacion edf) -> {
                        int index = bloqueGisFinal.indexOf(new BloqueGeoData(edf.getNoEdificacion()));
                        if (index > -1) {
                            BloqueGeoData get = bloqueGisFinal.get(index);
                            edf.setCantAlicuotas(get.getClavePorPiso().size());
                        }
                    });
            if (escritura.getCantAlicuotas() != count.size()) {
                JsfUti.messageWarning(null, "Advertencia", "No coincide cantidad de alicutas ingresadas con phs hijas de la base grafica");
                return event.getOldStep();
            }
            if (skipGenerarPh) {
                this.generarAlicuotas();
                return "predios_generados";
            }
        }
        if (event.getOldStep().equals("generar_phs")) {
            if (skipGenerarPh) {
                return "escritura_ficha_madre";
            }
        }
        return event.getNewStep();
    }

    private void countPhsHijas() {
        if (count == null) {
            count = new HashMap<Short, ModelPhs>();
        }
        this.bloqueGisFinal.forEach((dg) -> {
            dg.getNiveles().forEach((nvb) -> {
                ModelPhs get = count.get(nvb.getNumeracion());
                if (get == null) {
                    count.put(nvb.getNumeracion(), new ModelPhs(nvb.getPiso(), nvb.getNumeracion(), nvb.getNum()));
                }
            });
        });
    }

    /**
     * Verifica cada uno de los bloques de la base grafica y la alfanumerica y
     * chequea las areas y los niveles de cada bloque si existe diferencia de
     * areas o niveles seran marcadas como editadas
     *
     * @param event
     * @return
     */
    public Boolean buscarPhsHijas(FlowEvent event) {
        bloqueGisFinal = null;
        bloqueGis = dataBaseIb.getBloques(this.predioMatriz.getClaveCat());
        if (Utils.isNotEmpty(bloqueGis)) {
            edificaciones.forEach((edf) -> {
                int indexOf = bloqueGis.indexOf(new BloqueGeoData(edf.getNoEdificacion()));
                // Si index es mayor a cero ya existe ingresado en los alfanumerico 
                if (indexOf > -1) {
                    if (bloqueGisMod == null) {
                        bloqueGisMod = new ArrayList<>();
                    }
                    if (bloqueGisFinal == null) {
                        bloqueGisFinal = new ArrayList<>();
                    }
                    BloqueGeoData get = bloqueGis.get(indexOf);
                    // Si area de bloque es diferente y el numero de pisos es diferente fue editado
                    if (get.getAreaBloque().doubleValue() != edf.getAreaBloque().doubleValue()
                            || get.getNumPisos().size() != edf.getNumPisos()) {
                        edf.setEdicionGrafica(true);
                        bloqueGisMod.add(get);
                        bloqueGisFinal.add(get);
                        bloqueGis.remove(indexOf);
                    } else { // Bloque no editado 
                        bloqueGis.remove(indexOf);
                        bloqueGisFinal.add(get);
                    }
                }
            });
            if (Utils.isNotEmpty(bloqueGis)) {
                JsfUti.messageInfo(null, "Info.", "Se encontraron " + bloqueGis.size() + " Bloque.");
            }
            if (Utils.isNotEmpty(bloqueGisMod)) {
                JsfUti.messageInfo(null, "Info.", "Se encontraron " + bloqueGisMod.size() + " Bloque Editado.");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Realiza la verificacion del predio
     *
     * @param event
     */
    public void onRowSelect(SelectEvent event) {
        predioSeleccionado = (CatPredio) event.getObject();
        escritura.setCantBloques(cantidadEdificaciones(predioSeleccionado));
        edificaciones = manager.findAll(Querys.edificacionesByPredio, new String[]{"idPredio"}, new Object[]{predioSeleccionado.getId()});
        predioMatriz = manager.find(CatPredio.class, predioSeleccionado.getId());
        predioSeleccionado = new CatPredio();
        String[] params = {"predioRaiz", "estado"};
        Object[] vals = {predioMatriz.getId(), "A"};
        prediosGenerados = manager.findAll(Querys.getPHsByMatriz, params, vals);
        if (!prediosGenerados.isEmpty()) {
            String[] param = {"idPredio"};
            Object[] val = new Object[1];

            for (int i = 0; i < prediosGenerados.size(); i++) {
                val[0] = prediosGenerados.get(i).getId();
                prediosGenerados.get(i).setEscritura(new CatEscritura());
                if (prediosGenerados.get(i).getTieneEscritura() != null) {
                    if (Objects.equals(prediosGenerados.get(i).getTieneEscritura(), Boolean.TRUE)) {
                        CatEscritura e = (CatEscritura) manager.find(Querys.getEscriturasByPredioDesc, param, val);
                        if (e != null) {
                            prediosGenerados.get(i).setEscritura(e);
                        }
                    }
                }
                Collection<CatPredioPropietario> propietarios = (Collection<CatPredioPropietario>) manager.findAll(Querys.getCatPropietariosByPredio, new String[]{"id"}, new Object[]{prediosGenerados.get(i).getId()});
                if (propietarios == null) {
                    propietarios = new ArrayList<>();
                }
                prediosGenerados.get(i).setCatPredioPropietarioCollection(propietarios);
            }
            fichaMatriz = Boolean.TRUE;
            sumaAlicuotasComunal();
        }

    }

    public void onCellEditNiveles(CellEditEvent event) {

        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
    }

    public void onCellEditPredios(CellEditEvent event) {

    }

    protected boolean validarAlicuotasPorNivel() {
        for (CatPredioEdificacion ed : edificaciones) {
            int totalAlicuotasPorNivel = 0;
            totalAlicuotasPorNivel = niveles.stream().filter((n)
                    -> (Objects.equals(n.getNoEdificacion(), ed.getNoEdificacion()))).map((n)
                    -> (n.getCantDpto() + n.getCantBodegas() + n.getCantParqueos())).reduce(totalAlicuotasPorNivel, Integer::sum);
            if (totalAlicuotasPorNivel != ed.getCantAlicuotas()) {
                JsfUti.messageWarning(null, "El Bloque " + ed.getNoEdificacion() + " tiene " + ed.getCantAlicuotas() + " hijas y la suma de alicuotas de los niveles da " + totalAlicuotasPorNivel, ", no coinciden.");
                JsfUti.update("growl");
                return false;
            }
        }
        return true;
    }

    /**
     * Genera Los niveles de cada edificacion con
     *
     * @param edf
     * @return
     */
    protected List<NivelModel> generarNiveles(CatPredioEdificacion edf) {
        String edif = "Nro. ";
        int cont = 0;
        List<NivelModel> lista = new ArrayList<>();
        // OBtenemos el bloque dibujado si existe 
        int indexOf = bloqueGisFinal.indexOf(new BloqueGeoData(edf.getNoEdificacion()));
        BloqueGeoData geoBloque = null;
        if (indexOf > -1) {
            geoBloque = bloqueGisFinal.get(indexOf);
        } else {
            return null;
        }
        // Verificamos que se las alicuotas sean iguales a los bloques ingresados en la base graficacacaca
        if (edf.getCantAlicuotas() == geoBloque.getClavePorPiso().size()) {
            if (edf.getCantAlicuotas() > -1) {
                // Generamos el cuadro de alicuotas por cada nivel del bloque 
                for (ModelPhs phsPiso : geoBloque.getPhsPorPiso()) {
                    if (phsPiso.getNumPhsHijas() > 0) {
                        NivelModel nivel = new NivelModel(phsPiso.getPiso(), edf.getNoEdificacion());
                        nivel.setEdificacion(edf.getId());
                        /* Checkeamos que no este lleno el campo cogigoPh en la tabla tempBloque
                        si esta lleno quere decir que ya esta ingresado la ph caso contrario llena el modelo de datos*/
                        List<Short> lsd = new ArrayList<>();
                        for (Map.Entry<Short, Short> entry : phsPiso.getPhsHijas().entrySet()) {
                            BloqueGeoData dn = geoBloque.getDatosNivel(entry.getKey());
                            if (dn != null) {
                                if (dn.getCodigoPh() == null) {
                                    lsd.add(entry.getKey());
                                }
                            }
                        }
                        nivel.setCantDpto(lsd.size());
                        nivel.setHijas(lsd);
                        lista.add(nivel);
                    }
                }
            } else {
                if (cont == 0) {
                    edif = edf.getNoEdificacion() + "";
                } else {
                    edif += ", " + edf.getNoEdificacion();
                }
                cont++;
            }
        }
        if (cont == 1) {
            JsfUti.messageWarning(null, "La Edificacion " + edif + " no tiene Nro nivel registrado, no se pueden generar las alicuotas. ", "");
//            JsfUti.update("growl");
        }
        if (cont > 1) {
            JsfUti.messageWarning(null, "Las edificaciones " + edif + " no tienen Nro nivel registrado, no se pueden generar las alicuotas. ", "");
//            JsfUti.update("growl");
        }

        return lista;
    }

    /**
     * Genera el cuadro de alicuotas ingresadas.
     *
     */
    public void generarAlicuotas() {
//        prediosGenerados = new ArrayList<>();
        BigInteger numP = fusionDivisionEjb.generarNumPredio();
        // skipGenerarPh = false cuando se ejecuta de forma normal
        if (!skipGenerarPh) {
            for (NivelModel n : niveles) {
//                short unidad = 1;
                short unidad = this.buscarUltimaUnidadBloquePiso(n.getNoEdificacion(), (short) n.getNroNivel());
                for (int i = 0; i < n.getCantDpto(); i++) {
                    unidad++;
                    CatPredio copyPredio = copyPredio(0, n.getNoEdificacion(), (short) n.getNroNivel(), unidad, numP, n.getHijas().get(i));
                    if (copyPredio != null) {
                        prediosGenerados.add(copyPredio);
                        numP = numP.add(new BigInteger("1"));
                    }
                }
                for (int i = 0; i < n.getCantBodegas(); i++) {
                    prediosGenerados.add(copyPredio(1, n.getNoEdificacion(), (short) n.getNroNivel(), unidad++, numP));
                    numP = numP.add(new BigInteger("1"));
                }
                for (int i = 0; i < n.getCantParqueos(); i++) {
                    prediosGenerados.add(copyPredio(2, n.getNoEdificacion(), (short) n.getNroNivel(), unidad++, numP));
                    numP = numP.add(new BigInteger("1"));
                }
            }
        } else { //  Cuando skipGenerarPh =  true 
            switch (this.escritura.getTipoPh()) {
                case 1: // PHH
                    for (int i = 0; i < escritura.getCantAlicuotas(); i++) {
                        CatPredio copyPredio = copyPredio(1, Short.valueOf("" + (i + 1)), Short.valueOf("0"), Short.valueOf("0"), numP, null);
                        if (copyPredio != null) {
                            prediosGenerados.add(copyPredio);
                            numP = numP.add(new BigInteger("1"));
                        }
                    }
                    break;
                default: //PHV O (PHV Y PHH)
                    for (NivelModel n : niveles) {
//                        short unidad = 1;
                        short unidad = this.buscarUltimaUnidadBloquePiso(n.getNoEdificacion(), (short) n.getNroNivel());
                        for (int i = 0; i < n.getCantDpto(); i++) {
                            unidad++;
                            CatPredio copyPredio = copyPredio(0, n.getNoEdificacion(), (short) n.getNroNivel(), unidad, numP, n.getHijas().get(i));
                            if (copyPredio != null) {
                                prediosGenerados.add(copyPredio);
                                numP = numP.add(new BigInteger("1"));
                            }
                        }
                        for (int i = 0; i < n.getCantBodegas(); i++) {
                            unidad++;
                            prediosGenerados.add(copyPredio(1, n.getNoEdificacion(), (short) n.getNroNivel(), unidad, numP));
                            numP = numP.add(new BigInteger("1"));
                        }
                        for (int i = 0; i < n.getCantParqueos(); i++) {
                            unidad++;
                            prediosGenerados.add(copyPredio(2, n.getNoEdificacion(), (short) n.getNroNivel(), unidad, numP));
                            numP = numP.add(new BigInteger("1"));
                        }
                    }
                    break;
            }
        }
    }

    protected Short buscarUltimaUnidadBloquePiso(Short noEdificacion, Short piso) {
        Short ultimaUnidad = 0;
        for (CatPredio pg : prediosGenerados) {
            if (pg.getBloque() == noEdificacion && pg.getPiso() == piso) {
                if (ultimaUnidad < pg.getUnidad()) {
                    ultimaUnidad = pg.getUnidad();
                }
            }
        }
        return ultimaUnidad;
    }

    protected CatPredio copyPredio(int tipo, Short bloque, Short piso, Short unidad, BigInteger numP) {
        try {
            CatPredio ph = new CatPredio();
            BeanUtils.copyProperties(predioMatriz, ph);
            ph.setId(null);
            ph.setInstCreacion(new Date());
            ph.setBloque(bloque);
            ph.setPiso(piso);
            ph.setUnidad(unidad);
            ph.setPredioRaiz(new BigInteger(predioMatriz.getId().toString()));
            ph.setPropiedadHorizontal(Boolean.TRUE);
            ph.setEscritura(new CatEscritura());
            ph.setNumDepartamento(tipo == 0 ? (unidad + 100) + "" : null);
            ph.setAreaDeclaradaConst(null);
            ph.setCatPredioPropietarioCollection(new ArrayList<>());
            ph.setAlicuotaComponentes(new ArrayList<>());
            ph.setNumPredio(numP);
            ph.setNumeroFicha(numP);
            ph.setObservaciones(null);
            ph = fusionDivisionEjb.registrarPredio(ph);
            return ph;
        } catch (BeansException ex) {
            Logger.getLogger(RegistrarCuadroAlicuotas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected CatPredio copyPredio(int tipo, Short bloque, Short piso, Short unidad, BigInteger numP, Short numeracion) {
        try {
            CatPredio ph = new CatPredio();
            BeanUtils.copyProperties(predioMatriz, ph);
            ph.setId(null);
            ph.setInstCreacion(new Date());
            ph.setBloque(bloque);
            ph.setPiso(piso);
            ph.setUnidad(unidad);
            ph.setPredioRaiz(new BigInteger(predioMatriz.getId().toString()));
            ph.setPropiedadHorizontal(Boolean.TRUE);
            ph.setEscritura(new CatEscritura());
            ph.setNumDepartamento(tipo == 0 ? (unidad + 100) + "" : null);
            ph.setAreaDeclaradaConst(null);
            ph.setAlicuotaComponentes(new ArrayList<>());
            ph.setNumPredio(numP);
            ph.setNumeroFicha(numP);
            ph.setObservaciones(null);
            ph.setCatPredioS6(null);
            ph.setCatPredioS4(null);
            ph.setFichaMadre(false);
            ph.setCatPredioPropietarioCollection(null);
            // generamos la clave anterior
            if (numeracion != null) {
                ph.setPredialant(
                        Utils.completarCadenaConCeros(ph.getProvincia().toString(), 2) //2
                        + Utils.completarCadenaConCeros(ph.getCanton().toString(), 2) //4
                        + Utils.completarCadenaConCeros(ph.getParroquia().toString(), 2) //6
                        + Utils.completarCadenaConCeros(ph.getZona().toString(), 2) // 8
                        + Utils.completarCadenaConCeros(ph.getSector().toString(), 2) // 10 
                        + Utils.completarCadenaConCeros(ph.getMz().toString(), 2) // 12
                        + Utils.completarCadenaConCeros(ph.getSolar().toString(), 3) // 15
                        + Utils.completarCadenaConCeros(numeracion.toString(), 3)); // 18
            } else {
                ph.setPredialant(
                        Utils.completarCadenaConCeros(ph.getProvincia().toString(), 2) //2
                        + Utils.completarCadenaConCeros(ph.getCanton().toString(), 2) //4
                        + Utils.completarCadenaConCeros(ph.getParroquia().toString(), 2) //6
                        + Utils.completarCadenaConCeros(ph.getZona().toString(), 2) // 8
                        + Utils.completarCadenaConCeros(ph.getSector().toString(), 2) // 10 
                        + Utils.completarCadenaConCeros(ph.getMz().toString(), 2) // 12
                        + Utils.completarCadenaConCeros(ph.getSolar().toString(), 3) // 15
                        + Utils.completarCadenaConCeros(ph.getBloque().toString(), 3)); // 18
            }
            // Creamos la lista de las phs hijas generadas
            BloqueGeoData bloqueGeoData = new BloqueGeoData();
            if (phsGeneradas == null) {
                phsGeneradas = new ArrayList<>();
            }

            bloqueGeoData.setCodigo(predioMatriz.getClaveCat());
            bloqueGeoData.setCodigoPh(ph.getCodigoPredial());
            if (numeracion == null) {
                bloqueGeoData.setNumeracion(bloque);
            } else {
                bloqueGeoData.setNumeracion(numeracion);
            }
            // verificamos que no exista la clave ingresada para el caso de modificatoria 
            boolean existe = false;
            for (CatPredio pg : prediosGenerados) {
                if (pg.getClaveCat().contains(ph.getCodigoPredial())) {
                    existe = true;
                    break;
                }
            }
            System.out.println("Existe Ph " + ph.getCodigoPredial() + " " + existe);
            // si existe ya no lo agregamos a la lista 
            if (existe) {
                return null;
            } else {
                phsGeneradas.add(bloqueGeoData);
                ph = fusionDivisionEjb.registrarPredio(ph);
                HiberUtil.flushAndCommit();
                return ph;
            }
        } catch (BeansException beansException) {
            LOG.log(Level.SEVERE, "", beansException);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    protected boolean validarSumaAlicuotas() {
        return totalAlicuotasComunal.compareTo(new BigDecimal("100")) < 1;
    }

    public void sumaAlicuotasComunal() {
        totalAlicuotasComunal = BigDecimal.ZERO;
        prediosGenerados.stream().filter((p) -> (p.getAlicuotaConst() != null)).forEachOrdered((p) -> {
            totalAlicuotasComunal = totalAlicuotasComunal.add(p.getAlicuotaConst());
        });

        if (totalAlicuotasComunal.compareTo(new BigDecimal("100")) > 0) {
            Faces.messageWarning(null, "La suma total de alicuotas supera el 100%", "");
            Faces.update("growl");
        }
    }

    public void addComponenteAlicuotas(int pos) {
        index = pos;

        componenteSeleccionado.setPredio(prediosGenerados.get(index));
        componenteSeleccionado = (CatPredioAlicuotaComponente) manager.persist(componenteSeleccionado);
        prediosGenerados.get(index).getAlicuotaComponentes().add(componenteSeleccionado);

        List<CatPredioAlicuotaComponente> comps = prediosGenerados.get(index).getAlicuotaComponentes();
        BigDecimal areaConstruccion = BigDecimal.ZERO;
        BigDecimal areaDeclarada = BigDecimal.ZERO;
        BigDecimal alicuotaUtil = BigDecimal.ZERO;
        BigDecimal alicuotaComunal = BigDecimal.ZERO;

        for (CatPredioAlicuotaComponente c : comps) {
            if (c.getAlicuotaComunal() != null) {
                alicuotaComunal = alicuotaComunal.add(c.getAlicuotaComunal());
            }
            if (c.getAlicuotaUtil() != null) {
                alicuotaUtil = alicuotaUtil.add(c.getAlicuotaUtil());
            }
            if (c.getAreaConstruccion() != null) {
                areaConstruccion = areaConstruccion.add(c.getAreaConstruccion());
            }
            if (c.getAreaDeclarada() != null) {
                areaDeclarada = areaDeclarada.add(c.getAreaDeclarada());
            }
        }
        prediosGenerados.get(index).setAlicuotaConst(alicuotaComunal.equals(BigDecimal.ZERO) ? null : alicuotaComunal);
        prediosGenerados.get(index).setAlicuotaUtil(alicuotaUtil.equals(BigDecimal.ZERO) ? null : alicuotaUtil);
        prediosGenerados.get(index).setAreaConstPh(areaConstruccion.equals(BigDecimal.ZERO) ? null : areaConstruccion);
        prediosGenerados.get(index).setAreaDeclaradaConst(areaDeclarada.equals(BigDecimal.ZERO) ? null : areaDeclarada);

        componenteSeleccionado = new CatPredioAlicuotaComponente();

        sumaAlicuotasComunal();
    }

    public void updatePH(int pos) {
        index = pos;

        List<CatPredioAlicuotaComponente> comps = prediosGenerados.get(index).getAlicuotaComponentes();
        BigDecimal areaConstruccion = BigDecimal.ZERO;
        BigDecimal areaDeclarada = BigDecimal.ZERO;
        BigDecimal alicuotaUtil = BigDecimal.ZERO;
        BigDecimal alicuotaComunal = BigDecimal.ZERO;

        for (CatPredioAlicuotaComponente c : comps) {
            if (c.getAlicuotaComunal() != null) {
                alicuotaComunal = alicuotaComunal.add(c.getAlicuotaComunal());
            }
            if (c.getAlicuotaUtil() != null) {
                alicuotaUtil = alicuotaUtil.add(c.getAlicuotaUtil());
            }
            if (c.getAreaConstruccion() != null) {
                areaConstruccion = areaConstruccion.add(c.getAreaConstruccion());
            }
            if (c.getAreaDeclarada() != null) {
                areaDeclarada = areaDeclarada.add(c.getAreaDeclarada());
            }
        }
        prediosGenerados.get(index).setAlicuotaConst(alicuotaComunal.equals(BigDecimal.ZERO) ? null : alicuotaComunal);
        prediosGenerados.get(index).setAlicuotaUtil(alicuotaUtil.equals(BigDecimal.ZERO) ? null : alicuotaUtil);
        prediosGenerados.get(index).setAreaConstPh(areaConstruccion.equals(BigDecimal.ZERO) ? null : areaConstruccion);
        prediosGenerados.get(index).setAreaDeclaradaConst(areaDeclarada.equals(BigDecimal.ZERO) ? null : areaDeclarada);

        sumaAlicuotasComunal();
    }

    public void deleteComponenteAlicuotas(int posPredio, int postComponente) {
        index = posPredio;

        CatPredioAlicuotaComponente comp = prediosGenerados.get(index).getAlicuotaComponentes().get(postComponente);
        if (!componenteEliminar.contains(comp)) {
            componenteEliminar.add(comp);
        }
        prediosGenerados.get(index).getAlicuotaComponentes().remove(postComponente);
    }

    public void updateEscritura(int pos, CatEscritura e) {
        index = pos;
        prediosGenerados.get(index).setEscritura(e);

    }

    public void grabarEscritura(int pos) {
        index = pos;
        CatEscritura e = prediosGenerados.get(index).getEscritura();
        if (e.getIdEscritura() == null) {
            e.setFecCre(new Date());
        }
        prediosGenerados.get(index).setTieneEscritura(Boolean.TRUE);
        CatPredio p = (CatPredio) manager.persist(prediosGenerados.get(index));
        prediosGenerados.remove(index);
        prediosGenerados.add(index, p);
        e.setPredio(prediosGenerados.get(index));
        e.setEstado("A");
        e.setCantAlicuotas(null);
        try {

            e = (CatEscritura) manager.persist(e);
            prediosGenerados.get(index).setEscritura(e);
            Faces.messageInfo(null, "Escritura", " actualizada con exito.");
            Faces.update("growl");
        } catch (Exception ex) {
            Faces.messageError(null, "Error: Escritura no grabada.", "");
            Faces.update("growl");
        }

    }

    public void propietario(CatPredioPropietario propietario, int pos) {
        index = pos;
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(prediosGenerados.get(index).getId().toString());
        params.put("idPredio", p);
        p = new ArrayList<>();
        if (propietario != null && propietario.getId() != null) {
            p.add(propietario.getId().toString());
        }
        params.put("idCatPredioPro", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        params.put("nuevo", p);
        p = new ArrayList<>();
        if (propietario == null) {
            p.add("true");
        } else {
            p.add("false");
        }
        p = new ArrayList<>();
        p.add("true");
        params.put("editar", p);

        Map<String, Object> options = new HashMap<>();
        options.put("resizable", false);
        options.put("draggable", false);
        options.put("modal", true);
        options.put("width", "85%");
        options.put("height", "450");
        options.put("closable", true);
        options.put("contentWidth", "100%");
        RequestContext.getCurrentInstance().openDialog("/resources/dialog/propietarios_1", options, params);
    }

    public void procesarPropietario(SelectEvent event) {

        propietarioSeleccionado = (CatPredioPropietario) event.getObject();
        if (propietarioSeleccionado != null) {

            if (!prediosGenerados.get(index).getCatPredioPropietarioCollection().contains(propietarioSeleccionado)) {
                prediosGenerados.get(index).getCatPredioPropietarioCollection().add(propietarioSeleccionado);
            } else {
            }

            Faces.messageInfo(null, "Nota!", "Propietarios actualizadas satisfactoriamente");
            Faces.update("phs:" + index + ":dtPropietarios");
        }
    }

    public void eliminarPropietario(CatPredioPropietario propietario, int post) {
        index = post;
        propietario.setEstado("I");
        propietario.setModificado(sess.getName_user());
        propietario = catastroService.guardarPropietario(propietario, sess.getName_user());
        prediosGenerados.get(index).getCatPredioPropietarioCollection().remove(propietario);
        JsfUti.messageInfo(null, "Propietario", "Propietario eliminado.");
        Faces.update("phs:" + index + ":dtPropietarios");
    }

    /**
     * Se realiza la verificacion que se halla seleccionado el bloque, y se
     * envia a mostrar el dialogo para ingreso de bloque
     *
     * @param ed
     */
    public void bloque(CatPredioEdificacion ed) {
        if (ed != null) {
            int index1 = this.bloqueGisMod.indexOf(new BloqueGeoData(ed.getNoEdificacion()));
            if (index1 > -1) {
                geoBloqueSeleccionado = this.bloqueGisMod.get(index1);
            }
        }
        if (geoBloqueSeleccionado == null) {
            JsfUti.messageWarning(null, "Advertencia.", "Debe Seleccionar el bloque a ingresar en la tabla 'Bloque Nuevos'.");
            return;
        }
        Map<String, List<String>> params = new HashMap<>();
        List<String> p = new ArrayList<>();
        p.add(predioMatriz.getId().toString());
        params.put("idPredio", p);
        ss.agregarParametro(keyGeoBloque, this.geoBloqueSeleccionado);

        p = new ArrayList<>();
        p.add((ed == null) + "");
        params.put("nuevo", p);

        if (ed != null) {
            p = new ArrayList<>();
            p.add(ed.getId().toString());
            params.put("idCatPredioBloq", p);
        }
        p = new ArrayList<>();
        p.add("false");
        params.put("ver", p);

        Utils.openDialog("/resources/dialog/edificacionesPredio", params, "520");
    }

    /**
     * Procesa el bloque despues que se cierra el dialogo de ingreso de bloque
     *
     * @param event
     */
    public void procesarBloque(SelectEvent event) {
        CatPredioEdificacion bloque = (CatPredioEdificacion) event.getObject();
        if (bloque != null) {
            if (Utils.isEmpty(predioMatriz.getCatPredioEdificacionCollection())) {
                predioMatriz.setCatPredioEdificacionCollection(new ArrayList<>());
            }
            if (!predioMatriz.getCatPredioEdificacionCollection().contains(bloque)) {
                predioMatriz.getCatPredioEdificacionCollection().add(bloque);
            } else {
                ((List) predioMatriz.getCatPredioEdificacionCollection()).set(((List) predioMatriz.getCatPredioEdificacionCollection()).indexOf(bloque), bloque);
            }
            if (ss.tieneParametro(keyGeoBloque)) {
                ss.getParametros().remove(keyGeoBloque);
            }
            // Verificamos que haya seleccionado un bloque gis
            if (geoBloqueSeleccionado != null) {
                if (bloqueGisFinal == null) {
                    bloqueGisFinal = new ArrayList<>();
                }
                bloqueGisFinal.add(geoBloqueSeleccionado);
                bloqueGis.remove(geoBloqueSeleccionado);
                geoBloqueSeleccionado = null;
            }
            if (predioMatriz.getCatPredioS4() == null) {
                CatPredioS4 catPredioS4 = new CatPredioS4();
                catPredioS4.setPredio(predioMatriz);
                predioMatriz.setCatPredioS4((CatPredioS4) this.manager.persist(catPredioS4));

            }
            if (predioMatriz.getCatPredioS4().getEstadoSolar() == null
                    || predioMatriz.getCatPredioS4().getEstadoSolar().equals(this.catastroService.getDefaultItem("predio.estado_solar"))) {
                predioMatriz.getCatPredioS4().setEstadoSolar(this.catastroService.getItemByCatalagoOrder("predio.estado_solar", BigInteger.ONE));
                this.manager.persist(predioMatriz.getCatPredioS4());
            }
            edificaciones = predioMatriz.getCatPredioEdificacionCollection();
            this.eventValoracion.fire(new ValorarPredioPost(this.predioMatriz.getClaveCat(), this.predioMatriz.getPredialant(), 2, this.predioMatriz.getTipoPredio()));
        }
    }

    public List<CatPredioPropietario> propietariosPredio(CatPredio p) {

        List<CatPredioPropietario> list = new ArrayList<>();
        list.addAll(p.getCatPredioPropietarioCollection());

        return list;
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
            documentoBean.setRaiz(predioMatriz.getId());
            documentoBean.setContentType(event.getFile().getContentType());
            documentoBean.setDocumentoId(documentoId);
            documentoBean.setIdentificacion("Propiedad Horizontal");
            GeDocumentos saveDocumento = documentoBean.saveDocumento();
            if (saveDocumento != null) {
                this.idDoc = saveDocumento.getId();
            }
            is.close();
            out.close();
            Faces.messageInfo(null, "Información", "Archivo cargado satisfactoriamente.");
        } catch (IOException e) {
            Logger.getLogger(RegistrarCuadroAlicuotas.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void calcularAlicuota(CatPredio pt) {
        if (pt == null) {
            return;
        }
        if (pt.getAlicuotaUtil() == null) {
            Faces.messageInfo(null, "Información", "Debe ingresar la alicuota.");
            return;
        }
        if (predioMatriz.getAreaSolar() == null) {
            predioMatriz.setAreaSolar(BigDecimal.ZERO);
        }
//        pt.setAreaTerrenoAlicuota(predioMatriz.getAreaSolar().multiply(pt.getAlicuotaUtil())
//                .divide(BigDecimal.valueOf(100.00), 2, RoundingMode.HALF_UP));
        pt.setAreaSolar(predioMatriz.getAreaSolar().multiply(pt.getAlicuotaUtil())
                .divide(BigDecimal.valueOf(100.00), 2, RoundingMode.HALF_UP));
        pt.setAreaDeclaradaConst(predioMatriz.getAreaDeclaradaConst().multiply(pt.getAlicuotaUtil())
                .divide(BigDecimal.valueOf(100.00), 2, RoundingMode.HALF_UP));
        pt.setAlicuotaConst(pt.getAlicuotaUtil());
        pt.setAlicuotaTerreno(pt.getAlicuotaUtil());
    }

    public void checkTieneEscritura() {
        predioMatriz.setCantAlicuotas(escritura.getCantAlicuotas());
    }
//<editor-fold defaultstate="collapsed" desc="GETTER AND SETTER">

    public Boolean getValidarBloqueGrafica() {
        return validarBloqueGrafica;
    }

    public void setValidarBloqueGrafica(Boolean validarBloqueGrafica) {
        this.validarBloqueGrafica = validarBloqueGrafica;
    }

    public CatPredioLazy getPredios() {
        return predios;
    }

    public void setPredios(CatPredioLazy predios) {
        this.predios = predios;
    }

    public CatPredio getPredioSeleccionado() {
        return predioSeleccionado;
    }

    public void setPredioSeleccionado(CatPredio predioSeleccionado) {
        this.predioSeleccionado = predioSeleccionado;
    }

    public FusionDivisionServices getFusionDivisionEjb() {
        return fusionDivisionEjb;
    }

    public void setFusionDivisionEjb(FusionDivisionServices fusionDivisionEjb) {
        this.fusionDivisionEjb = fusionDivisionEjb;
    }

    public UserSession getSess() {
        return sess;
    }

    public void setSess(UserSession sess) {
        this.sess = sess;
    }

    public CatastroServices getCatastroService() {
        return catastroService;
    }

    public void setCatastroService(CatastroServices catastroService) {
        this.catastroService = catastroService;
    }

    public Entitymanager getManager() {
        return manager;
    }

    public void setManager(Entitymanager manager) {
        this.manager = manager;
    }

    public ServletSession getSs() {
        return ss;
    }

    public void setSs(ServletSession ss) {
        this.ss = ss;
    }

    public List<CatCiudadela> getCiudadelas() {
        return ciudadelas;
    }

    public void setCiudadelas(List<CatCiudadela> ciudadelas) {
        this.ciudadelas = ciudadelas;
    }

    public CatEscritura getEscritura() {
        return escritura;
    }

    public void setEscritura(CatEscritura escritura) {
        this.escritura = escritura;
    }

    public Collection<CatPredioEdificacion> getEdificaciones() {
        return edificaciones;
    }

    public void setEdificaciones(Collection<CatPredioEdificacion> edificaciones) {
        this.edificaciones = edificaciones;
    }

    public CatPredioEdificacion getEdificacionSeleccionada() {
        return edificacionSeleccionada;
    }

    public void setEdificacionSeleccionada(CatPredioEdificacion edificacionSeleccionada) {
        this.edificacionSeleccionada = edificacionSeleccionada;
    }

    public int getCantAlicuotasBloque() {
        return cantAlicuotasBloque;
    }

    public void setCantAlicuotasBloque(int cantAlicuotasBloque) {
        this.cantAlicuotasBloque = cantAlicuotasBloque;
    }

    public List<NivelModel> getNiveles() {
        return niveles;
    }

    public void setNiveles(List<NivelModel> niveles) {
        this.niveles = niveles;
    }

    public List<CatPredio> getPrediosGenerados() {
        return prediosGenerados;
    }

    public void setPrediosGenerados(List<CatPredio> prediosGenerados) {
        this.prediosGenerados = prediosGenerados;
    }

    public List<CatPredio> getPrediosGeneradosFiltrados() {
        return prediosGeneradosFiltrados;
    }

    public void setPrediosGeneradosFiltrados(List<CatPredio> prediosGeneradosFiltrados) {
        this.prediosGeneradosFiltrados = prediosGeneradosFiltrados;
    }

    public CatPredio getPredioMatriz() {
        return predioMatriz;
    }

    public void setPredioMatriz(CatPredio predioMatriz) {
        this.predioMatriz = predioMatriz;
    }

    public Boolean getFichaMatriz() {
        return fichaMatriz;
    }

    public void setFichaMatriz(Boolean fichaMatriz) {
        this.fichaMatriz = fichaMatriz;
    }

    public BigDecimal getTotalAlicuotasComunal() {
        return totalAlicuotasComunal;
    }

    public void setTotalAlicuotasComunal(BigDecimal totalAlicuotasComunal) {
        this.totalAlicuotasComunal = totalAlicuotasComunal;
    }

    public CatPredioPropietario getPropietarioSeleccionado() {
        return propietarioSeleccionado;
    }

    public void setPropietarioSeleccionado(CatPredioPropietario propietarioSeleccionado) {
        this.propietarioSeleccionado = propietarioSeleccionado;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public CatEnteLazy getEntes() {
        return entes;
    }

    public void setEntes(CatEnteLazy entes) {
        this.entes = entes;
    }

    public CatEnte getEnteSeleccionado() {
        return enteSeleccionado;
    }

    public void setEnteSeleccionado(CatEnte enteSeleccionado) {
        this.enteSeleccionado = enteSeleccionado;
    }

    public CatPredioPropietario getProp() {
        return prop;
    }

    public void setProp(CatPredioPropietario prop) {
        this.prop = prop;
    }

    public CatPredioAlicuotaComponente getComponenteSeleccionado() {
        return componenteSeleccionado;
    }

    public void setComponenteSeleccionado(CatPredioAlicuotaComponente componenteSeleccionado) {
        this.componenteSeleccionado = componenteSeleccionado;
    }

    public List<CatPredioAlicuotaComponente> getComponenteEliminar() {
        return componenteEliminar;
    }

    public void setComponenteEliminar(List<CatPredioAlicuotaComponente> componenteEliminar) {
        this.componenteEliminar = componenteEliminar;
    }

    public UploadDocumento getDocumentoBean() {
        return documentoBean;
    }

    public void setDocumentoBean(UploadDocumento documentoBean) {
        this.documentoBean = documentoBean;
    }

    public OmegaUploader getFserv() {
        return fserv;
    }

    public void setFserv(OmegaUploader fserv) {
        this.fserv = fserv;
    }

    public Long getIdDoc() {
        return idDoc;
    }

    public void setIdDoc(Long idDoc) {
        this.idDoc = idDoc;
    }

    public List<CatCanton> getCantones() {
        return cantones;
    }

    public void setCantones(List<CatCanton> cantones) {
        this.cantones = cantones;
    }

    public boolean isTieneEscritura() {
        return tieneEscritura;
    }

    public void setTieneEscritura(boolean tieneEscritura) {
        this.tieneEscritura = tieneEscritura;
    }

    public boolean isSkipGenerarPh() {
        return skipGenerarPh;
    }

    public void setSkipGenerarPh(boolean skipGenerarPh) {
        this.skipGenerarPh = skipGenerarPh;
    }

    public List<CtlgItem> getListado(String argumento) {
        HiberUtil.newTransaction();
        List<CtlgItem> ctlgItem = (List<CtlgItem>) manager.findAllEntCopy(Querys.getCtlgItemaASC, new String[]{"catalogo"}, new Object[]{argumento});
        return ctlgItem;
    }

    public List<BloqueGeoData> getBloqueGis() {
        return bloqueGis;
    }

    public void setBloqueGis(List<BloqueGeoData> bloqueGis) {
        this.bloqueGis = bloqueGis;
    }

    public BloqueGeoData getGeoBloqueSeleccionado() {
        return geoBloqueSeleccionado;
    }

    public void setGeoBloqueSeleccionado(BloqueGeoData geoBloqueSeleccionado) {
        this.geoBloqueSeleccionado = geoBloqueSeleccionado;
    }

//</editor-fold>
}
