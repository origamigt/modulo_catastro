package com.origami.sgm.events;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Fernando
 */
public class FusionPrediosPost {

    private List<String> codPrediosFusion = new LinkedList<>();
    private String codPredioFinal;
    private List<BloqueFusionData> bloques;
    private String tipo;
    private BigInteger numPredio;

    public FusionPrediosPost() {
    }

    public List<String> getCodPrediosFusion() {
        return codPrediosFusion;
    }

    public void setCodPrediosFusion(List<String> codPrediosFusion) {
        this.codPrediosFusion = codPrediosFusion;
    }

    public String getCodPredioFinal() {
        return codPredioFinal;
    }

    public void setCodPredioFinal(String codPredioFinal) {
        this.codPredioFinal = codPredioFinal;
    }

    public List<BloqueFusionData> getBloques() {
        return bloques;
    }

    public void setBloques(List<BloqueFusionData> bloques) {
        this.bloques = bloques;
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

}
