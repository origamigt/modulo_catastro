/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.censocat.restful;

import com.origami.censocat.entity.ordentrabajo.OrdenDet;
import com.origami.censocat.entity.ordentrabajo.OrdenTrabajo;
import com.origami.sgm.database.Querys;
import com.origami.sgm.entities.AclUser;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import util.Utils;

/**
 * Realiza el envio de ordenes para la mobil
 *
 * @author Angel Navarro
 */
@Named
@RequestScoped
@Path("ordenes")
@Produces({"application/Json", "text/xml"})
public class OrdenesMovil implements Serializable {

    private static final long serialVersionUID = 1l;
    private static final Logger LOG = Logger.getLogger(OrdenesMovil.class.getName());

    @javax.inject.Inject
    private Entitymanager manager;
    private JsonUtils jsonUtils;

    @GET
    @Path(value = "/auth/userName/{userName}/password/{password}")
    public Response getAuth(@PathParam("userName") String userName, @PathParam("password") String password) {
        AclUser u = (AclUser) manager.find(Querys.getUsuariobyUserPass, new String[]{"user", "pass"}, new Object[]{userName, Utils.encriptSHAHex(password)});
        try {
//            if (u != null) {
            jsonUtils = new JsonUtils();
            String js = jsonUtils.generarJson(u);
            return Response.status(Response.Status.OK).entity(js)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                    .header("Content-Type", "application/json;charset=UTF-8").build();
            //}
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Copiar Ordenes.", e);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path(value = "/copiar/userName/{userName}/password/{password}")
    public Response getOrdenesFecha(@PathParam("userName") String userName, @PathParam("password") String password) {
        AclUser u = (AclUser) manager.find(Querys.getUsuariobyUserPass, new String[]{"user", "pass"}, new Object[]{userName, Utils.encriptSHAHex(password)});
        try {
            List<OrdenTrabajo> ot = null;
            if (u != null) {
                ot = manager.findAll(com.origami.censocat.querys.Querys.getOrdenes, new String[]{"responsable", "estadoOt", "estadoDet"}, new Object[]{u, EstadoMovil.PENDIENTE, EstadoMovil.PENDIENTE});
            }
            //if (ot != null) {
            jsonUtils = new JsonUtils();
            String js = jsonUtils.generarJson(ot);
            return Response.status(Response.Status.OK).entity(js)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                    .header("Content-Type", "application/json;charset=UTF-8").build();
            // }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Copiar Ordenes.", e);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path(value = "/copiar/userName/{userName}/password/{password}/fechaCreacion/{fechaCreacion}")
    public Response getOrdenesFecha(@PathParam("userName") String userName, @PathParam("password") String password, @PathParam("fechaCreacion") Long fecha) {
        AclUser u = (AclUser) manager.find(Querys.getUsuariobyUserPass, new String[]{"user", "pass"}, new Object[]{userName, Utils.encriptSHAHex(password)});
        try {
            List<OrdenTrabajo> ot = null;
            List<OrdenTrabajo> result = null;
            if (u != null) {
                Date d = new Date(fecha);
                System.out.println(userName + " Fecha de ultima verificacion " + d);
                ot = manager.findAll(com.origami.censocat.querys.Querys.getOrdenesMayorFecha, new String[]{"responsable", "estadoOt", "fecCre"}, new Object[]{u, EstadoMovil.PENDIENTE, d});
            }
            //if (ot != null) {
            jsonUtils = new JsonUtils();
            List<OrdenDet> dets = null;
            if (ot != null) {
                result = new ArrayList<>();
                for (OrdenTrabajo o : ot) {
                    o.setObservaciones(null);
                    for (OrdenDet od : o.getOrdenDetCollection()) {
                        if (od.getEstadoDet().equalsIgnoreCase("P")) {
                            if (dets == null) {
                                dets = new ArrayList<>(o.getOrdenDetCollection().size());
                            }
                            od.setObservaciones(null);
                            dets.add(od);
                        }
                    }
                    o.setOrdenDetCollection(dets);
                    if (dets != null) {
                        result.add(o);
                    }
                    dets = null;
                }
            }
            String js = jsonUtils.generarJson(result);
            return Response.status(Response.Status.OK).entity(js)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                    .header("Content-Type", "application/json;charset=UTF-8").build();
            //  }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Copiar Ordenes.", e);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path(value = "/sincronizar/userName/{userName}/password/{password}/numOrden/{numOrden}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescargarOrdenes(@PathParam("userName") String userName, @PathParam("password") String password, @PathParam("numOrden") Long numOrden, String data) throws Exception {
        jsonUtils = new JsonUtils();
        Boolean ok = false;
        OrdenTrabajo ordenMovil = null;
        try {
            createFileImagesPredios(numOrden, data, false);
//            System.out.println(data);
            ordenMovil = (OrdenTrabajo) jsonUtils.jsonToObject(data, OrdenTrabajo.class);
            if (ordenMovil != null) {
                LOG.log(Level.INFO, "Iniciando sincronizacion de orden # {0}", ordenMovil.getNumOrden());
                AclUser u = (AclUser) manager.find(Querys.getUsuariobyUserPass, new String[]{"user", "pass"}, new Object[]{userName, Utils.encriptSHAHex(password)});
                OrdenTrabajo ot = getOrdenUser(u, numOrden);
                if (ot != null) {
                    ot.setFecAct(new Date());
                    try {
                        String fotos = jsonUtils.getElementFromJson(data, "fotos");
                        if (fotos != null) {
                            if (fotos.length() > 0) {
                                ot.setObservaciones(jsonUtils.getElementFromJson(data, "fotos"));
                            }
                        }
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Obtener Fotos.", e);
                    }
                    ot.setEstadoOt(EstadoMovil.CENSADA); // Cargada
                    if (Utils.isNotEmpty(ordenMovil.getOrdenDetCollection())) {
                        for (OrdenDet dtMovil : ordenMovil.getOrdenDetCollection()) {
                            if (Utils.isNotEmpty(ot.getOrdenDetCollection())) {
                                OrdenDet odt = ((List<OrdenDet>) ot.getOrdenDetCollection())
                                        .get(((List<OrdenDet>) ot.getOrdenDetCollection()).indexOf(dtMovil));
                                if (odt != null) {
                                    odt.setDatoAct(dtMovil.getDatoRef());
                                    odt.setEstadoDet(EstadoMovil.CENSADA);
                                    if (dtMovil.getImgs() != null) {
                                        String im = "";
                                        System.out.println("Imagenes " + dtMovil.getImgs().length);
                                        for (String img : dtMovil.getImgs()) {
                                            if (im.length() > 0) {
                                                im = im + ";";
                                            }
                                            im = im + img;
                                        }
                                        if (im.trim().length() > 0) {
                                            odt.setObservaciones(im);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    manager.saveList((List) ot.getOrdenDetCollection());
                    manager.persist(ot);
                    ok = true;
                }
            }
            if (ok) {
                LOG.log(Level.INFO, "Finalizando sincronizacion de orden # {0}", ordenMovil.getNumOrden());
                String js = jsonUtils.generarJson("{}");
                return Response.status(Response.Status.OK).entity(js)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            } else {
                createFileImagesPredios(numOrden, data, false);
                LOG.info("Error al sincronizar orden...");
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            }
        } catch (Exception e) {
            createFileImagesPredios(numOrden, data, false);
            LOG.log(Level.SEVERE, "Descarga de Ordenes.", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .allow("OPTIONS")
                    .build();
        }
    }

    @POST
    @Path(value = "/sincronizar/fotos/userName/{userName}/password/{password}/idDet/{idDet}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDescargarFotoOrdenDet(@PathParam("userName") String userName, @PathParam("password") String password, @PathParam("idDet") Long idDet, String data) throws Exception {
        jsonUtils = new JsonUtils();
        Boolean ok = false;
        OrdenDet d = null;
        try {
            createFileImagesPredios(idDet, data, true);
            d = manager.find(OrdenDet.class, idDet);
//            System.out.println(idDet + " idDet " + Arrays.toString(data));
            if (data == null) {
                LOG.log(Level.INFO, "Finalizando sincronizacion de orden_det sin imagenes # {0}", d.getId());
                String js = jsonUtils.generarJson("{}");
                return Response.status(Response.Status.OK).entity(js)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            }
            data = data.replace("{, id, ", "{\"id\"=\"");
            data = data.replace(", image, ", "\", \"image\"=\"");
            data = data.replace(", }", "\"}");
            createFileImagesPredios(idDet, data, true);
            if (d != null) {
                LOG.log(Level.INFO, "Copiando Fotos de detalle de orden # {0} predio {1}", new Object[]{idDet, d.getPredio().getId()});
                if (d.getImgs() != null) {
                    System.out.println("Opcion 1");
                    String im = "";
                    System.out.println("Imagenes " + d.getImgs().length);
                    for (String img : d.getImgs()) {
                        if (im.length() > 0) {
                            im = im + ";";
                        }
                        im = im + img;
                    }
                    if (im.trim().length() > 0) {
                        d.setObservaciones(im);
                    }
                    manager.update(d);
                } else {
                    System.out.println("Opcion 2");
                    if (data.length() > 3) {
                        d.setObservaciones(data);
                    }
                    manager.update(d);
                }
                ok = true;
            }
            if (ok) {
                LOG.log(Level.INFO, "Finalizando sincronizacion de orden # {0}", d.getId());
                String js = jsonUtils.generarJson("{}");
                return Response.status(Response.Status.OK).entity(js)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            } else {
                createFileImagesPredios(idDet, data, true);
                LOG.info("Error al sincronizar  detalle de orden...");
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            }
        } catch (Exception e) {
            createFileImagesPredios(idDet, data, true);
            LOG.log(Level.SEVERE, "Descarga de Ordenes.", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .allow("OPTIONS")
                    .build();
        }
    }

    @POST
    @Path(value = "/sincronizar/error/userName/{userName}/password/{password}/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerLog(@PathParam("userName") String userName, @PathParam("password") String password, String data) throws Exception {
        Boolean ok = false;
        try {
            if (data == null) {
                LOG.log(Level.INFO, "Finalizando sincronizacion de orden_det sin imagenes # {0}", "...");
                String js = jsonUtils.generarJson("{}");
                return Response.status(Response.Status.OK).entity(js)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            }
            LOG.log(Level.INFO, "Finalizando sincronizacion de orden # {0}", data);
            createFileImagesPredios(0l, data, true);
            if (ok) {
                String js = jsonUtils.generarJson("{}");
                return Response.status(Response.Status.OK).entity(js)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            } else {
                createFileImagesPredios(0l, data, true);
                LOG.info("Error al sincronizar  detalle de orden...");
                return Response.status(Response.Status.NOT_FOUND)
                        .header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                        .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                        .header("Content-Type", "application/json;charset=UTF-8")
                        .allow("OPTIONS")
                        .build();
            }
        } catch (Exception e) {
            createFileImagesPredios(0l, data, true);
            LOG.log(Level.SEVERE, "Descarga de Ordenes.", e);
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE")
                    .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .allow("OPTIONS")
                    .build();
        }
    }

    private OrdenTrabajo getOrdenUser(AclUser u, Long numOrden) {
        try {
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("numOrden", numOrden);
            paramt.put("responsable", u);
            return manager.findObjectByParameter(OrdenTrabajo.class, paramt);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Obtener Ordenes.", e);
        }
        return null;
    }

    private void createFileImagesPredios(Long id, String[] data) {
        FileWriter file = null;
        try {
            file = new FileWriter("/servers_files/order" + id + ".txt");
            file.write(Arrays.toString(data));
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Crear archivo.", ioe);
        } finally {
            try {
                if (file != null) {
                    file.flush();
                    file.close();
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Crear archivo.", ex);
            }
        }
    }

    private void createFileImagesPredios(Long id, String data, Boolean esDet) {
        FileWriter file = null;
        try {
            String nombre = (esDet ? "OrdenDet" : "NumOrden") + id + "_" + (Utils.dateFormatPattern("yyyy-MM-dd", new Date()));
            file = new FileWriter("/servers_files/" + nombre + "_" + new Date().getTime() + ".txt");
            file.write(data);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Crear archivo.", ioe);
        } finally {
            try {
                if (file != null) {
                    file.flush();
                    file.close();
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Crear archivo.", ex);
            }
        }
    }

}
