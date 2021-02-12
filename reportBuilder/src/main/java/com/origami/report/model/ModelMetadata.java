/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.model;

import com.origami.report.enums.JoinType;
import com.origami.report.enums.NivelTree;
import com.origami.report.enums.OrderType;
import com.origami.report.enums.WhereComparator;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 *
 * @author ANGEL NAVARRO
 */
public class ModelMetadata implements Serializable {

    /**
     * identificador de la tabla que es el nombre la tabla concatenado con el
     * nivel en el arbol y el numero de referecnia
     */
    private String key;
    /**
     * Nombre de la clase
     */
    private String nameClass;
    /**
     * Nombre que se muestra en el arbol
     */
    private String description;
    /**
     * Nombre del campo en la db
     */
    private String sqlName;
    /**
     * Nombre de la tabla
     */
    private String alias;
    /**
     * Nombre de la columna que hace referencia para los join
     */
    private String referencesColumn;
    /**
     * Nombre de la tabla referenciada
     */
    private String referencesTable;
    /**
     * Nivel en el arbol, por defecto en none que representa a una columna
     */
    private NivelTree nivel = NivelTree.NONE;
    /**
     * Identificador o clave primaria para el caso de las referencias
     */
    private String identifier;
    /**
     * Modelo de datos para la clausula where
     */
    private ModelWhere where;
    /**
     * Padre al que pertenece el campo
     */
    private ModelMetadata parent;
    /**
     * Incida si la tabla es un catalogo
     */
    private Boolean isCatalog = Boolean.FALSE;
    /**
     * Modelo de datos para los join
     */
    private JoinType join;
    /**
     * Define el ordenamiento que va tener la columna
     */
    private OrderType orderBy;

    public ModelMetadata() {
    }

    public ModelMetadata(String description, String sqlName, String alias, String referencesColumn, String referencesTable) {
        this.description = description;
        this.sqlName = sqlName;
        this.alias = alias;
        this.referencesColumn = referencesColumn;
        this.referencesTable = referencesTable;
    }

    public String getKey() {
        if (key == null) {
            key = alias;
        }
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNameClass() {
        return nameClass;
    }

    public void setNameClass(String nameClass) {
        this.nameClass = nameClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getReferencesColumn() {
        return referencesColumn;
    }

    public void setReferencesColumn(String referencesColumn) {
        this.referencesColumn = referencesColumn;
    }

    public String getReferencesTable() {
        return referencesTable;
    }

    public void setReferencesTable(String referencesTable) {
        this.referencesTable = referencesTable;
    }

    /**
     * Verificar si la propiedad {@code referencesTable} no es nulled
     *
     * @return true si es una referencia hacio otra tabla, caso conytrario false
     */
    public Boolean getIsReferences() {
        return (this.referencesTable != null);
    }

    public NivelTree getNivel() {
        return nivel;
    }

    public void setNivel(NivelTree nivel) {
        this.nivel = nivel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Verificar si la propiedad {@code where} no es nulled
     *
     * @return true si esta en la clausula WHERE, caso conytrario false
     */
    public Boolean getIsWhere() {
        return (where != null);
    }

    public ModelWhere getWhere() {
        return where;
    }

    public void setWhere(ModelWhere where) {
        this.where = where;
    }

    public ModelMetadata getParent() {
        return parent;
    }

    public void setParent(ModelMetadata parent) {
        this.parent = parent;
    }

    public Boolean getIsCatalog() {
        return isCatalog;
    }

    public void setIsCatalog(Boolean isCatalog) {
        this.isCatalog = isCatalog;
    }

    public JoinType getJoin() {
        return join;
    }

    public void setJoin(JoinType join) {
        this.join = join;
    }

    public OrderType getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderType orderBy) {
        this.orderBy = orderBy;
    }

    @Override
    public String toString() {
        return "ModelMetadata{" + "key=" + key + ", nameClass=" + nameClass + ", description=" + description + ", sqlName=" + sqlName + ", alias=" + alias + ", referencesColumn="
                + referencesColumn + ", referencesTable=" + referencesTable + ", nivel=" + nivel + ", identifier=" + identifier + ", where=" + where + ", parent=" + parent + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        ModelMetadata model = (ModelMetadata) obj;

        return (model.description == null ? this.description == null : model.description == this.description) && (model.getKey() == null ? this.getKey() == null : model.getKey() == this.getKey());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.description);
        return hash;
    }

    /**
     * Verifica si el tipo de dato es numerico
     *
     * @return True si es numerico, caso contrario false
     */
    public Boolean typeNumber() {
        if (nameClass == null) {
            return false;
        }
        if (this.where != null) {
            if (this.where.getComparator().En.equals(WhereComparator.En)) {
                return false;
            }
        }
        if (nameClass.equals(Long.class.getName())) {
            return true;
        } else if (nameClass.equals(Integer.class.getName())) {
            return true;
        } else if (nameClass.equals(Short.class.getName())) {
            return true;
        } else if (nameClass.equals(BigInteger.class.getName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifica si el tipo de dato en decimal
     *
     * @return True si es numerico, caso contrario false
     */
    public Boolean typeDecimal() {
        if (nameClass == null) {
            return false;
        }
        if (this.where != null) {
            if (this.where.getComparator().En.equals(WhereComparator.En)) {
                return false;
            }
        }
        if (nameClass.equals(Float.class.getName())) {
            return true;
        } else if (nameClass.equals(Double.class.getName())) {
            return true;
        } else if (nameClass.equals(BigDecimal.class.getName())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Verifica si el tipo de dato en Boolean
     *
     * @return True si es Booleano, caso contrario false
     */
    public Boolean typeBoolean() {
        if (nameClass == null) {
            return false;
        }
        if (this.where != null) {
            if (this.where.getComparator().En.equals(WhereComparator.En)) {
                return false;
            }
        }
        return nameClass.equals(Boolean.class.getName());
    }

    /**
     * Verifica si el tipo de dato texto
     *
     * @return True si es texto, caso contrario false
     */
    public Boolean typeText() {
        if (nameClass == null) {
            return false;
        }
        if (this.where != null) {
            if (this.where.getComparator().En.equals(WhereComparator.En)) {
                return true;
            }
        }
        return nameClass.equals(String.class.getName());
    }

    /**
     * Retorn un string con la sintaxis del orden
     *
     * @return column ASC
     */
    public String getOrder() {
        String columna = parent.getKey().concat(".").concat(sqlName);
        return columna + " " + orderBy.getValue();
    }

    /**
     * Devuelve elnombre de la columna concatenado con el nombre del alias del
     * padre
     *
     * @return parent.getKey().concat(".").concat(sqlName)
     */
    public String getNameColumn() {
        if (parent != null) {
            return parent.getKey().concat(".").concat(sqlName);
        } else {
            return sqlName;
        }
    }
}
