package com.origami.sgmee.catastro.geotx.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import util.Utils;

/**
 * Modelo de datos para la la tabla tem_bloque
 *
 * @author Angel Navarro
 */
public class BloqueGeoData implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigInteger gid;
    private String codigo;
    private String codigoPh;
    private Short num;
    private Short piso;
    private BigDecimal area;
    private Boolean habilitado;
    private TipoCUD tipoTx;
    private Short numeracion;

    private List<BloqueGeoData> niveles;
    private Map<Short, Short> clavePorPiso;

    public BloqueGeoData() {

    }

    public BloqueGeoData(BigInteger gid, String codigo, Short num, Short piso, BigDecimal area, Boolean habilitado,
            TipoCUD tipoTx) {
        super();
        this.gid = gid;
        this.codigo = codigo;
        this.num = num;
        this.piso = piso;
        this.area = area;
        this.habilitado = habilitado;
        this.tipoTx = tipoTx;
    }

    public BloqueGeoData(Short num) {
        this.num = num;
    }

    public BigInteger getGid() {
        return gid;
    }

    public void setGid(BigInteger gid) {
        this.gid = gid;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Short getNum() {
        return num;
    }

    public void setNum(Short num) {
        this.num = num;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public Boolean getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(Boolean habilitado) {
        this.habilitado = habilitado;
    }

    public TipoCUD getTipoTx() {
        return tipoTx;
    }

    public void setTipoTx(TipoCUD tipoTx) {
        this.tipoTx = tipoTx;
    }

    public Short getPiso() {
        return piso;
    }

    public void setPiso(Short piso) {
        this.piso = piso;
    }

    public List<BloqueGeoData> getNiveles() {
        if (Utils.isNotEmpty(niveles)) {
            Collections.sort(niveles, (BloqueGeoData b1, BloqueGeoData b2) -> b1.piso.compareTo(b2.piso));
        }
        return niveles;
    }

    public void setNiveles(List<BloqueGeoData> niveles) {
        this.niveles = niveles;
    }

    public String getCodigoPh() {
        return codigoPh;
    }

    public void setCodigoPh(String codigoPh) {
        this.codigoPh = codigoPh;
    }

    public Short getNumeracion() {
        return numeracion;
    }

    public void setNumeracion(Short numeracion) {
        this.numeracion = numeracion;
    }

    public BigDecimal getAreaBloque() {
        BigDecimal areaTemp = BigDecimal.ZERO;
        if (Utils.isNotEmpty(niveles)) {
            for (BloqueGeoData nivel : niveles) {
                if (nivel.getArea() == null) {
                    nivel.setArea(BigDecimal.ZERO);
                }
                areaTemp = areaTemp.add(nivel.getArea());
            }
        }
        return areaTemp;
    }

    /**
     * Retorne el numero de pisos que se Hallan agregado el grafico
     *
     * @return
     */
    public Map<Short, BigDecimal> getNumPisos() {
        Map<Short, BigDecimal> count = new HashMap<Short, BigDecimal>();
        if (Utils.isNotEmpty(niveles)) {
            for (BloqueGeoData nivel : niveles) {
                BigDecimal get = count.get(nivel.getPiso());
                if (nivel.getArea() == null) {
                    nivel.setArea(BigDecimal.ZERO);
                }
                if (get == null) {
                    get = BigDecimal.ZERO;
                }
                count.put(nivel.getPiso(), get.add(nivel.getArea()));
            }
        }
        return count;
    }

    /**
     * Realiza la unificacion de las areas de cada una de las phs, el key es la
     * la numeracion de la ph hija en que se van asignar las claves
     *
     * @return Mapa con el orden de la numeracion de cada ph y el area.
     */
    public List<ModelPhs> getPhsPorPiso() {
        // El key es el orden de la ph hijas
        List<ModelPhs> mdls = new ArrayList<>();
        for (Map.Entry<Short, BigDecimal> entry : this.getNumPisos().entrySet()) {
            ModelPhs md = new ModelPhs();
            md.setPiso(entry.getKey());
            Set<Map.Entry<Short, Short>> temp = clavePorPiso.entrySet();
            md.setPhsHijas(temp.stream().filter(map -> entry.getKey().equals(map.getValue()))
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue())));
            md.setNumPhsHijas(md.getPhsHijas().size());
            mdls.add(md);
        }
        return mdls;
    }

    /**
     * Realiza la verificacion de todas las phs hijas que se encuentran an cada
     * uno de los pisos, si la ph ya existe la remueve de la lista y que la que
     * este en la primer bloque agregado, agrega todos las hijas en la variable
     * <code>clavePorPiso</code> donde el hey es el orden de asignacion de
     * claves de cada hija, el value es piso donde se encuantra la hija
     *
     * @param result Lista de todos los bloques encontrados, para realizar la
     * verificacion de las phs
     */
    public void verificarPhs(List<BloqueGeoData> result) {
        if (clavePorPiso == null) {
            clavePorPiso = new HashMap<Short, Short>();
        }
        this.verificarNumeracionNull(result);
        if (Utils.isNotEmpty(niveles)) {
            for (BloqueGeoData nivel : niveles) {
                Short get = clavePorPiso.get(nivel.getNumeracion());
                if (get == null) {
                    if (nivel.getNumeracion() != null) {
                        clavePorPiso.put(nivel.getNumeracion(), nivel.piso);
                    }
                }
            }
            for (BloqueGeoData geo : result) {
                if (geo.getNum() < this.num) {
                    if (geo.getClavePorPiso() != null) {
                        for (Map.Entry<Short, Short> hija : geo.getClavePorPiso().entrySet()) {
                            if (clavePorPiso.containsKey(hija.getKey())) {
                                clavePorPiso.remove(hija.getKey());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * El key es el orden de la ph hijas, el value es el piso donde se encuantra
     * la ph hija
     *
     * @return
     */
    public Map<Short, Short> getClavePorPiso() {
        return clavePorPiso;
    }

    public void setClavePorPiso(Map<Short, Short> clavePorPiso) {
        this.clavePorPiso = clavePorPiso;
    }

    @Override
    public boolean equals(Object obj) {
        BloqueGeoData ob = (BloqueGeoData) obj;
        if (Objects.isNull(ob)) {
            return false;
        }

        return Objects.equals(this.num, ob.num);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.num);
        return hash;
    }

    @Override
    public String toString() {
        return "Bloque : " + num + " pisos: " + (niveles == null ? 0 : niveles.size()); //To change body of generated methods, choose Tools | Templates.
    }

    private void verificarNumeracionNull(List<BloqueGeoData> result) {
        try {
            for (BloqueGeoData d : result) {
                if (Utils.isNotEmpty(d.getNiveles())) {
                    for (BloqueGeoData nivel : d.getNiveles()) {
                        if (nivel.getCodigoPh() != null && !nivel.getCodigoPh().isEmpty()) {
                            if (nivel.getCodigoPh().length() == 24) {
                                if (nivel.getCodigoPh().endsWith("00000")) {
                                    nivel.setNumeracion(Short.valueOf(nivel.getCodigoPh().substring(16, 19)));
                                } else {
                                    System.out.println("Clave ph " + nivel.getCodigoPh() + " tiene piso y unidad.");
                                }
                            } else {
                                System.out.println("Clave es diferente de 24 digitos.");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public BloqueGeoData getDatosNivel(Short piso) {
        try {
            if (Utils.isNotEmpty(niveles)) {
                for (BloqueGeoData nv : niveles) {
                    if (Objects.equals(nv.getNumeracion(), piso)) {
                        return nv;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
