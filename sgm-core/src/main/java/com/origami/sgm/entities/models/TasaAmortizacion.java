/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.entities.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author ANGEL NAVARRO
 */
public class TasaAmortizacion implements Serializable {

    private Integer num;
    private Date fecha;
    private BigDecimal amort_capital_variable;
    private BigDecimal interes_saldo;
    private BigDecimal capital_pagado_variable;

    public TasaAmortizacion() {
    }

    public TasaAmortizacion(Integer num, Date fecha, BigDecimal amortCapitalVariable, BigDecimal interesSaldo, BigDecimal capitalPagadoVariable) {
        this.num = num;
        this.fecha = fecha;
        this.amort_capital_variable = amortCapitalVariable;
        this.interes_saldo = interesSaldo;
        this.capital_pagado_variable = capitalPagadoVariable;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getAmort_capital_variable() {
        return amort_capital_variable;
    }

    public void setAmort_capital_variable(BigDecimal amort_capital_variable) {
        this.amort_capital_variable = amort_capital_variable;
    }

    public BigDecimal getInteres_saldo() {
        return interes_saldo;
    }

    public void setInteres_saldo(BigDecimal interes_saldo) {
        this.interes_saldo = interes_saldo;
    }

    public BigDecimal getCapital_pagado_variable() {
        return capital_pagado_variable;
    }

    public void setCapital_pagado_variable(BigDecimal capital_pagado_variable) {
        this.capital_pagado_variable = capital_pagado_variable;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.num);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TasaAmortizacion other = (TasaAmortizacion) obj;
        if (!Objects.equals(this.num, other.num)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TasaAmortizacion{" + "num=" + num + ", fecha=" + fecha + '}';
    }

}
