package com.origami.sgm.events;

import com.origami.sgm.entities.models.Tipo;
import java.math.BigInteger;
import java.util.Map;

public class AfectacionPost {

    private String codigo;
    private Map<String, Tipo> codigos;
    private String tipo;
    private BigInteger numPredio;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigInteger getNumPredio() {
        return numPredio;
    }

    public void setNumPredio(BigInteger numPredio) {
        this.numPredio = numPredio;
    }

    /**
     * El key es la clave catastral y el value es numero de predio(codigo unico)
     *
     * @return Mapa con los codigos y numero de predios generados
     */
    public Map<String, Tipo> getCodigos() {
        return codigos;
    }

    /**
     * El key es la clave catastral y el value es numero de predio(codigo unico)
     *
     * @param codigos Mapa con las clave y el numero de predio en el value
     */
    public void setCodigos(Map<String, Tipo> codigos) {
        this.codigos = codigos;
    }

}
