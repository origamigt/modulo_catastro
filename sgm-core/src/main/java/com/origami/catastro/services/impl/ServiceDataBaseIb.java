/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.catastro.services.impl;

import com.origami.catastroextras.entities.ibarra.CiuCiudadano;
import com.origami.catastroextras.entities.ibarra.CiuDireccion;
import com.origami.catastroextras.entities.ibarra.CiuReferenciasPersonal;
import com.origami.catastroextras.entities.ibarra.CiuTelefono;
import com.origami.catastroextras.entities.ibarra.FinanPrestamo;
import com.origami.catastroextras.entities.ibarra.PredioAnio;
import com.origami.catastroextras.entities.ibarra.Restricciones;
import com.origami.catastroextras.entities.ibarra.RestricionPredio;
import com.origami.catastroextras.entities.ibarra.RestricionPredioPK;
import com.origami.catastroextras.entities.ibarra.ValoraMnz;
import com.origami.session.UserSession;
import com.origami.sgm.entities.models.TasaAmortizacion;
import com.origami.sgm.services.ejb.QuerysDB;
import com.origami.sgm.services.ejbs.HibernateEjbInterceptor;
import com.origami.sgmee.catastro.geotx.TempBloqueFacade;
import com.origami.sgmee.catastro.geotx.model.BloqueGeoData;
import com.origami.sgmee.catastro.geotx.model.GeoVias;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import util.EntityBeanCopy;
import util.Utils;

/**
 * EJB con metodos de consulta a las tablas especificas de ibarra.
 *
 * @author Angel Navarro
 */
@Stateless
@Dependent
@Interceptors(value = {HibernateEjbInterceptor.class})
public class ServiceDataBaseIb {

    private static final Logger LOG = Logger.getLogger(ServiceDataBaseIb.class.getName());

    @Inject
    private Entitymanager manager;
    @Inject
    private UserSession session;

    @Inject
    private TempBloqueFacade areasLinderos;

    public Entitymanager getManager() {
        return this.manager;
    }

    /**
     * Busca
     *
     * @return
     */
    public Integer getCodigoRestriccion() {
        try {
            Integer max = (Integer) this.manager.find(QuerysDB.getMaxCodigoRestriccion);
            if (max == null) {
                max = 1;
            } else {
                max += 1;
            }
            return max;
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Buscar RestricionPredio", e);
        }
        return null;
    }

    /**
     * Busca
     *
     * @return
     */
    public Integer getCodigoDireccion() {
        try {
            Integer max = (Integer) this.manager.find(QuerysDB.getMaxCodigoDireccion);
            if (max == null) {
                max = 1;
            } else {
                max += 1;
            }
            return max;
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Buscar CodigoCiudadno", e);
        }
        return null;
    }

    /**
     * Busca
     *
     * @return
     */
    public Integer getCodigoCiudadano() {
        try {
            Integer max = (Integer) this.manager.find(QuerysDB.getMaxCodigoCiudadano);
            if (max == null) {
                max = 1;
            } else {
                max += 1;
            }
            return max;
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Buscar CodigoDireccion", e);
        }
        return null;
    }

    /**
     * Busca
     *
     * @return
     */
    public Integer getCodigoTelefono() {
        try {
            Integer max = (Integer) this.manager.find(QuerysDB.getMaxCodigoTelefono);
            if (max == null) {
                max = 1;
            } else {
                max += 1;
            }
            return max;
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Buscar CodigoTelefono", e);
        }
        return null;
    }

    /**
     * Busca la restriccion y la retorna si la encuentra caso contrario retorna
     * null
     *
     * @param idRestriccion
     * @return
     */
    public Restricciones findRestricciones(String idRestriccion) {
        try {
            return this.manager.find(Restricciones.class, Integer.valueOf(idRestriccion));
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Restriccion", e);
        }
        return null;
    }

    /**
     * Busca la restriccion y la retorna si la encuentra caso contrario retorna
     * null
     *
     * @param idRestriccion
     * @return
     */
    public Restricciones findRestricciones(Integer idRestriccion) {
        try {
            return this.manager.find(Restricciones.class, idRestriccion);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Restriccion", e);
        }
        return null;
    }

    public List<Restricciones> listRestricciones() {
        try {
            return this.manager.findObjectByParameterOrderList(Restricciones.class, null,
                    new String[]{"descripcionRestriccion"}, true);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Restriccion", e);
        }
        return null;
    }

    /**
     * Actualiza o persiste la Restriccion y retorn la restriccion persistida
     *
     * @param rest
     * @return
     */
    public Restricciones saveUpdateRestriccion(Restricciones rest) {
        try {
            if (rest != null) {
                if (rest.getCodigoRestriccion() == null) { // Actualizar
                    rest.setCodigoRestriccion(this.getCodigoRestriccion());
                    rest.setEstadoRestricion("A");
                    return (Restricciones) this.manager.persist(rest);
                } else { // Nuevo
                    this.manager.persist(rest);
                    return EntityBeanCopy.initializeAndUnproxy(rest);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Restriccion", e);
        }
        return null;
    }

    /**
     * Actualiza o persiste el Ciudadano y retorna el ciudadano persistido
     *
     * @param ciudadano
     * @return
     */
    public CiuCiudadano saveUpdateCiudadano(CiuCiudadano ciudadano) {
        try {
            if (ciudadano != null) {
                if (ciudadano.getCiuCodigo() == null) { // Actualizar
                    ciudadano.setCiuCodigo(this.getCodigoCiudadano());
                    ciudadano.setCiuEstado('A');
                    return (CiuCiudadano) this.manager.persist(ciudadano);
                } else { // Nuevo
                    this.manager.persist(ciudadano);
                    return EntityBeanCopy.initializeAndUnproxy(ciudadano);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Restriccion", e);
        }
        return null;
    }

    /**
     * Actualiza o persiste Direccion
     *
     * @param direccion
     * @return
     */
    public CiuDireccion saveUpdateCiudireccion(CiuDireccion direccion) {
        try {
            if (direccion != null) {
                if (direccion.getDiSecuencia() == null) { // Actualizar
                    direccion.setDiSecuencia(this.getCodigoDireccion());
                    return (CiuDireccion) this.manager.persist(direccion);
                } else { // Nuevo
                    this.manager.persist(direccion);
                    return EntityBeanCopy.initializeAndUnproxy(direccion);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Direccion", e);
        }
        return null;
    }

    /**
     * Actualiza o persiste Telefono
     *
     * @param telefono
     * @return
     */
    public CiuTelefono saveUpdateTelefono(CiuTelefono telefono) {
        try {
            if (telefono != null) {
                if (telefono.getTeSecuencial() == null) {
                    telefono.setTeEstado('V');
                    telefono.setTeSecuencial(this.getCodigoTelefono());
                    return (CiuTelefono) this.manager.persist(telefono);
                } else {

                    this.manager.persist(telefono);
                    return EntityBeanCopy.initializeAndUnproxy(telefono);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Telefono", e);
        }
        return null;
    }

    /**
     * Actualiza Referencias
     *
     * @param referencia
     * @return
     */
    public void updateReferencia(CiuReferenciasPersonal referencia) {
        try {
            if (referencia != null) {
                this.manager.persist(referencia);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Actualizar Referencia", e);
        }

    }

    /**
     * Remueve Direccion
     *
     * @param direccion
     * @return
     */
    public void removeDireccion(CiuDireccion direccion) {
        try {
            if (direccion != null) {
                this.manager.delete(direccion);
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Actualizar Referencia", e);
        }

    }

    /**
     * Busca la restriccio por la clave primaria
     *
     * @param rest RestricionPredio
     * @return retorna la entiti persistida
     */
    public RestricionPredio saveUpdateRestriccionPredio(RestricionPredio rest) {
        try {
            if (rest != null) {
                if (this.findRestricciones(rest.getRestricionPredioPK()) != null) {
                    throw new RuntimeException("Ya existe restriccion agregada del mismo tipo.");
                }
                if (rest.getId() == null) { // Nuevo
                    rest.setFechaIngreso(new Date());
                    rest.setUsuarioIngreso(this.session.getName_user());
                    return (RestricionPredio) this.manager.persist(rest);
                } else { // Actualizar
                    this.manager.persist(rest);
                    return EntityBeanCopy.initializeAndUnproxy(rest);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar RestricionPredio", e);
        }
        return null;
    }

    /**
     * Busca la restriccion y la retorna si la encuentra caso contrario retorna
     * null
     *
     * @param idRestriccion
     * @return
     */
    public RestricionPredio findRestricionPredio(String idRestriccion) {
        try {
            if (idRestriccion == null) {
                return null;
            }
            return this.manager.find(RestricionPredio.class, Long.valueOf(idRestriccion));
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Guardar o Actualizar Restriccion", e);
        }
        return null;
    }

    /**
     * Busca la RestricionPredio por el la clave primaria
     *
     * @param idRestricionPredio
     * @return retorna null si la restriccion no se encuentra en la base de
     * datos
     */
    public RestricionPredio findRestricciones(RestricionPredioPK idRestricionPredio) {
        try {
            if (idRestricionPredio == null) {
                return null;
            }
            Map<String, Object> paramt = new HashMap<>();
            paramt.put("restricionPredioPK.codCatastralPredio", idRestricionPredio.getCodCatastralPredio());
            paramt.put("restricionPredioPK.codigoRestriccion", idRestricionPredio.getCodigoRestriccion());
            return this.manager.findObjectByParameter(RestricionPredio.class, paramt);
            // return manager.find(RestricionPredio.class, idRestricionPredio);
        } catch (NumberFormatException e) {
            LOG.log(Level.SEVERE, "Buscar RestricionPredio", e);
        }
        return null;
    }

    public CiuCiudadano getCiudadano(String CiRuc) {
        try {
            if (CiRuc == null) {
                return null;
            }
            Map<String, Object> mp = new HashMap<>();
            mp.put("ciuCedRuc", CiRuc);
            return this.manager.findObjectByParameter(CiuCiudadano.class, mp);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar Propietario", e);
        }
        return null;
    }

    /**
     * Envia a buscar
     *
     * @param id
     * @return
     */
    public List<RestricionPredio> findRestriccionPredio(Long id) {
        try {
            if (id == null) {
                return null;
            }
            Map<String, Object> mp = new HashMap<>();
            mp.put("predio", BigInteger.valueOf(id));
            return this.manager.findObjectByParameterList(RestricionPredio.class, mp);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar RestricionPredio", e);
        }
        return null;
    }

    public List<RestricionPredio> findRestriccionPredio(String predialant) {
        try {
            if (predialant == null) {
                return null;
            }
            Map<String, Object> mp = new HashMap<>();
            //mp.put("restricionPredioPK.codCatastralPredio", predialant);
            mp.put("restricionPredioPK.codCatastralPredio", predialant);
            return this.manager.findObjectByParameterList(RestricionPredio.class, mp);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar RestricionPredio", e);
        }
        return null;
    }

    public PredioAnio findPredioAnio(String predialant, String anio) {
        try {
            if (predialant == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("codCatastralPredio", predialant);
            map.put("anio", anio);
            return this.manager.findObjectByParameter(PredioAnio.class, map);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar Predio Anio", e);
        }
        return null;
    }

    public List<String> findAniosPredio(String clave) {
        try {
            if (clave == null) {
                List<String> anios = new ArrayList<>();
                for (int i = 2005; i < Utils.getAnio(new Date()); i++) {
                    anios.add(i + "");
                }
                return anios;
            }
            return this.manager.findAll(QuerysDB.getAniosPredio, new String[]{"codCatastralPredio"},
                    new Object[]{clave});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Buscar Predio Anio", e);
        }
        return null;
    }

    /**
     * En lista bloque nuevos
     *
     * @param codigo
     * @return
     */
    public List<BloqueGeoData> getBloquesGis(String codigo) {
        try {
            return this.areasLinderos.detectNuevos(codigo);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    /**
     * En lista bloque Modficados
     *
     * @param codigo
     * @return
     */
    public List<BloqueGeoData> getBloquesGisEditados(String codigo) {
        try {
            return this.areasLinderos.detectEnModificacion(codigo, this.session.getName_user().toLowerCase());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, codigo, e);
        }
        return null;
    }

    /**
     * Lista todos los niveles de un bloqe
     *
     * @param codigo
     * @param numBloque
     * @return
     */
    public List<BloqueGeoData> getBloqueGis(String codigo, Short numBloque) {
        try {
            return this.areasLinderos.listByCodigoNiveles(codigo, numBloque);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    /**
     * Eliminar las niveles de la capa transaccional e inhabilita los bloque de
     * la capa definitiva
     *
     * @param bloqueGis
     */
    public void eliminarBloqueGis(List<BloqueGeoData> bloqueGis) {
        try {
            this.areasLinderos.deleteTx(bloqueGis);
            this.areasLinderos.enableBloque(bloqueGis);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Inserta los diferentes niveles en la capa definitic=va
     *
     * @param bloqueGis
     */
    public void crearBloqueGis(List<BloqueGeoData> bloqueGis) {
        try {
            if (bloqueGis != null) {
                bloqueGis.forEach((bloqueGi) -> {
                    this.areasLinderos.getTempFac().asentarBloques_porCodigo(bloqueGi.getCodigo());
                });
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Trae todos los bloque que existen ingresados en el predio.
     *
     * @param codigo Clave catastral del predio
     * @return
     */
    public List<BloqueGeoData> getBloques(String codigo) {
        try {
            return this.areasLinderos.bloquesPredio(codigo);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public void crearBloqueGisPhs(List<BloqueGeoData> bloqueGis) {
        try {
            if (Utils.isEmpty(bloqueGis)) {
                return;
            }

            bloqueGis.forEach((bloqueGi) -> {
                this.areasLinderos.updateClaveYNumPh(bloqueGi.getCodigo(), bloqueGi.getCodigoPh(),
                        bloqueGi.getNumeracion());
            });
            this.areasLinderos.getTempFac().asentarBloques_porCodigo(bloqueGis.get(0).getCodigo());
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param codigo Clave catastral anterior hasta la manzana 12 digitos
     * @return ValoraMnz si existe
     */
    public ValoraMnz getValoraMzn(String codigo) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("claveTotal", codigo);
            return this.manager.findObjectByParameter(ValoraMnz.class, map);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public ValoraMnz crearValoraManzana(ValoraMnz valoraMz) {
        try {
            return null;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public List<BloqueGeoData> getBloquesGisPH(String claveCatastralCompleta) {
        try {
            return this.areasLinderos.detectNuevosPH(claveCatastralCompleta);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

    public List<BloqueGeoData> getBloquesGisEditadosPH(String claveCatastralCompleta) {
        try {
            return this.areasLinderos.detectEnModificacion(claveCatastralCompleta,
                    this.session.getName_user().toLowerCase(), true);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, claveCatastralCompleta, e);
        }
        return null;
    }

    public List<GeoVias> getViasPredio(String claveCatastral) {
        return this.manager.nativeQuery(
                "SELECT CAST(mz.gid AS bigint) AS gid,mz.nombre,mz.clave_cata \"claveCata\"  FROM geodata.predios_tx pt "
                + "INNER JOIN geodata.geo_vias mz ON ST_intersects(ST_BUFFER(mz.geom, 30), pt.geom) "
                + "WHERE pt.codigo =? LIMIT 5",
                new Object[]{claveCatastral}, GeoVias.class);
    }

    public List<GeoVias> getViasPredio(Integer gidPrediostx) {
        return this.manager.nativeQuery(
                "SELECT CAST(mz.gid AS bigint) AS gid,mz.nombre,mz.clave_cata \"claveCata\"  FROM geodata.predios_tx pt "
                + "INNER JOIN geodata.geo_vias mz ON ST_intersects(ST_BUFFER(mz.geom, 30), pt.geom) "
                + "WHERE pt.gid =? LIMIT 5",
                new Object[]{gidPrediostx}, GeoVias.class);
    }

    public void guardarAmortizacion(Integer codigoPrestamo) {
        List<Object> param = new ArrayList<>();
        param.add(codigoPrestamo);
        System.out.println("Imprimiendo lista: " + param);
        Object valor = null;
        //          this.manager.executeFunction("catastro.sp_guardar_finan_saldo_anios", param, Boolean.TRUE);
        valor = this.manager.executeNativeQuery("SELECT catastro.sp_guardar_finan_saldo_anios(?)", new Object[]{codigoPrestamo});

    }

//    public FinanPrestamoPredio getHipotecaPredioPorCodigoPrestamo(Long codigoPrestamo) {
//        Map<String, Object> paramt = new HashMap<String, Object>();
//        paramt.put("codigoPrestamo", codigoPrestamo);
//        return this.manager.findObjectByParameter(FinanPrestamoPredio.class, paramt);
//    }
    public List<FinanPrestamo> getFinanPrestamos() {
        return this.manager.findAll(FinanPrestamo.class);
    }

//    public FinanPrestamoPredio saveUpdateHipotecaPredio(FinanPrestamoPredio rest) {
//
//        try {
//
//            if (rest != null) {
//                rest.setCodCatastralPredio(rest.getPredio().getPredialant());
//
//                // if (this.findRestricciones(rest.getRestricionPredioPK()) != null) {
//                // throw new RuntimeException("Ya existe restriccion agregada del mismo tipo.");
////                }
//                if (rest.getCodigoPrestamo() == null) { // Nuevo
//                    rest.setFechaIngreso(new Date());
//                    return (FinanPrestamoPredio) this.manager.persist(rest);
//                } else { // Actualizar
//                    this.manager.persist(rest);
//                    return EntityBeanCopy.initializeAndUnproxy(rest);
//                }
//            }
//        } catch (Exception e) {
//            LOG.log(Level.SEVERE, "Guardar o Actualizar RestricionPredio", e);
//        }
//        return null;
//    }
    public List<TasaAmortizacion> getAmortizacion(Long codPredios) {
        if (codPredios == null) {
            return null;
        }
        SQLQuery sql = manager.getSession().createSQLQuery("SELECT * FROM catastro.sp_tasa_amortizacion(:codPredios) WHERE num IS NOT NULL;");
        sql.setInteger("codPredios", codPredios.intValue());
        sql.setResultTransformer(Transformers.aliasToBean(TasaAmortizacion.class));
        List<TasaAmortizacion> result = sql.list();
        return result;
    }

    public List<BloqueGeoData> getBloquesGis(Integer gid) {
        try {
            return this.areasLinderos.detectNuevos(gid);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
        return null;
    }

}
