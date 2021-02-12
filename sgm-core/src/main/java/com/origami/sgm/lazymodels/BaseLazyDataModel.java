/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.lazymodels;

import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.sgm.util.EjbsCaller;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * Lazy por defecto que realiza la busqueda por demanda
 *
 * @author CarlosLoorVargas
 * @param <T> Entity Class
 */
public class BaseLazyDataModel<T extends Object> extends LazyDataModel<T> {

    private Entitymanager manager;
    private Class<T> entity;
    private int rowCount = 0;
    private String defaultSorted = "id";
    private String defaultSortOrder = "ASC";
    private Criteria orderCrit;
    private String orderField;
    private String colunmEstado = "estado";
    private String propertyIsNotNull;
    private Object valueEstado;
    private List valueEstados;
    private Boolean equalsEstado = Boolean.TRUE;
    private Boolean defaulfNull = Boolean.TRUE;
    private String tempKey = null;

    private String[] filterss;
    private Object[] filtersValue;

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     */
    public BaseLazyDataModel() {
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     *
     * @param entity Entity a la que se hacer lazy
     */
    public BaseLazyDataModel(Class<T> entity) {
        this.entity = entity;
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     *
     * @param entity Entity a la que se hacer lazy
     * @param defaultSorted Propiedad por defecto para realizar el filtro
     */
    public BaseLazyDataModel(Class<T> entity, String defaultSorted) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     *
     * @param entity Entity a la que se hacer lazy
     * @param defaultSorted Propiedad por defecto para realizar el filtro
     * @param propertyIsNotNull Propiedad a validar si es nula.
     * @param defaulfNull true si la propiedad <code>propertyIsNotNull</code>
     * puede ser nula caso contrario false.
     */
    public BaseLazyDataModel(Class<T> entity, String defaultSorted, String propertyIsNotNull, Boolean defaulfNull) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.propertyIsNotNull = propertyIsNotNull;
        this.defaulfNull = defaulfNull;
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     *
     * @param entity Entity a la que se hacer lazy
     * @param filterss Nombre de las propiedades a filtrar en la entity
     * @param filtersValue Valor para cada propiedad ingresada en los filterss
     */
    public BaseLazyDataModel(Class<T> entity, String[] filterss, Object[] filtersValue) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.filterss = filterss;
        this.filtersValue = filtersValue;
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     *
     * @param entity Entity a la que se hacer lazy
     * @param filterss
     * @param filtersValue Valor para cada propiedad ingresada en los filterss
     * @param defaultSorted Propiedad por defecto para realizar el filtro
     */
    public BaseLazyDataModel(Class<T> entity, String[] filterss, Object[] filtersValue, String defaultSorted) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.filterss = filterss;
        this.filtersValue = filtersValue;
        this.defaultSorted = defaultSorted;
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Lazy por defecto que realiza la busqueda por demanda
     *
     * @param entity Entity a la que se hacer lazy
     * @param filterss Nombre de las propiedades a filtrar en la entity
     * @param filtersValue Valor para cada propiedad ingresada en los filterss
     * @param defaultSorted Propiedad por defecto para realizar el filtro
     * @param defaultSortOrder 'ASC' si en el data tabla se mostran en forma
     * ascendente, caso contrario 'DESC'
     */
    public BaseLazyDataModel(Class<T> entity, String[] filterss, Object[] filtersValue, String defaultSorted, String defaultSortOrder) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.filterss = filterss;
        this.filtersValue = filtersValue;
        this.defaultSorted = defaultSorted;
        this.defaultSortOrder = defaultSortOrder;
        manager = EjbsCaller.getTransactionManager();
    }

    public BaseLazyDataModel(Class<T> entity, String defaultSorted, String defaultSortOrder) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.defaultSortOrder = defaultSortOrder;
        manager = EjbsCaller.getTransactionManager();
    }

    public BaseLazyDataModel(Class<T> entity, String defaultSorted, String defaultSortOrder,
            String defaultSorted2, String defaultSortOrder2, String defaultSorted3,
            String defaultSortOrder3, String defaultSorted4, String defaultSortOrder4) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.defaultSortOrder = defaultSortOrder;
        manager = EjbsCaller.getTransactionManager();
    }

    /**
     * Filtro que se realizara cada vez que se realize la busqueda
     *
     * @param crit {@link Criteria} de hibernate para realizar las consulta a la
     * base de datos
     * @param filters key nombre de la propiedad que se realizara la busqueda,
     * value valor del filtro
     * @throws Exception se lanza error para el caso que lo halla
     */
    public void criteriaFilterSetup(Criteria crit, Map<String, Object> filters) throws Exception {
        try {
            if (this.filterss != null) {
                for (int i = 0; i < this.filterss.length; i++) {
                    Object aClass = this.filtersValue[i];
                    if (aClass instanceof Date) {
                        Calendar ffin = Calendar.getInstance();
                        ffin.setTime((Date) this.filtersValue[i]);
                        ffin.add(Calendar.DAY_OF_MONTH, 1);
                        System.out.println(ffin.getTime());
                        crit.add(Restrictions.between(this.filterss[i], this.filtersValue[i], ffin.getTime()));
                    } else {
                        crit.add(Restrictions.eq(this.filterss[i], this.filtersValue[i]));
                    }
                }
            }
            if (colunmEstado != null && valueEstado != null) {
                if (equalsEstado) {
                    crit.add(Restrictions.eq(colunmEstado, valueEstado));
                } else {
                    crit.add(Restrictions.ne(colunmEstado, valueEstado));
                }
            }
            if (defaulfNull) {
                if (Objects.nonNull(propertyIsNotNull)) {
                    crit.add(Restrictions.isNotNull(propertyIsNotNull));
                }
            } else {
                if (Objects.nonNull(propertyIsNotNull)) {
                    crit.add(Restrictions.isNull(propertyIsNotNull));
                }
            }
            if (filters == null || filters.isEmpty()) {
                return;
            }
            // LLAMAMOS EL ME  PARA REALIZAR LOS FILTER
            this.findPropertyFilter(crit, filters);
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Implementacion del load del lazy
     *
     * @param first primer registro
     * @param pageSize Tamanio de registro de cada pagina
     * @param sortField Campos por los que se realizar el ordenamiento
     * @param sortOrder tipo de Ordenamiento en que se realizaran el
     * ordenamiento
     * @param filters key nombre de la propiedad que se realizara la busqueda,
     * value valor del filtro
     * @return lista de registro con el tamanio de pagina
     */
    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List<T> result = null;
        Criteria cq, dcq;
        try {
            cq = manager.getSession().createCriteria(this.getEntity(), "entity");
            this.criteriaFilterSetup(cq, filters);
            cq.setProjection(Projections.projectionList().add(Projections.rowCount()));
            dcq = manager.getSession().createCriteria(this.getEntity(), "entity1");

            this.criteriaFilterSetup(dcq, filters);
            if (orderCrit != null) {
                this.criteriaSortSetup(orderCrit, orderField, sortOrder);
            } else {
                this.criteriaSortSetup(dcq, sortField, sortOrder);
            }
            this.criteriaPageSetup(dcq, first, pageSize);
            rowCount = 0;
            rowCount = ((Long) cq.uniqueResult()).intValue();
            this.setRowCount(rowCount);
            result = dcq.list();
            Hibernate.initialize(result);
        } catch (Exception ex) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, this.getEntity().getName(), ex);
        }
        return result;
    }

    public T getRowData(Object key) {
        T ob = null;
        try {
            ob = manager.find(entity, key);
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, e);
        }
        return ob;
    }

    @Override
    public T getRowData(String rowKey) {
        T ob = null;
        Object x = rowKey;
        try {
            if (NumberUtils.isNumber(rowKey)) {
                ob = manager.find(entity, Long.parseLong(rowKey));
            } else {
                ob = manager.find(entity, rowKey);
            }
        } catch (NumberFormatException e) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, e);
        }
        return ob;
    }

    @Override
    public void setRowIndex(int rowIndex) {
        try {
            if (rowIndex == -1 || getPageSize() == 0) {
                super.setRowIndex(-1);
            } else {
                super.setRowIndex(rowIndex % getPageSize());
            }
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void criteriaPageSetup(Criteria crit, int first, int pageSize) {
        try {
            crit.setFirstResult(first);
            crit.setMaxResults(pageSize);
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void criteriaSortSetup(Criteria crit, String field, SortOrder order) {
        try {
            if (field == null) {
                crit.addOrder((defaultSortOrder.equalsIgnoreCase("ASC")) ? Order.asc(defaultSorted) : Order.desc(defaultSorted));
            } else {
                Criteria c = this.findProperty(crit, field.contains("extras") ? field.replace("extras.", "") : field);
                field = (field.contains(".")) ? (field.contains("extras") ? field.substring(field.lastIndexOf("extras"))
                        : field.substring(field.lastIndexOf(".") + 1)) : field;
                if (order.equals(SortOrder.ASCENDING)) {
                    c.addOrder(Order.asc(field));
                } else {
                    c.addOrder(Order.desc(field));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Realiza la busqueda de la propiedad que esta en el filter si hay una
     * propieda que esta dentro de otra entity entonces se obtiene la entity y
     * se agrega ese criteria al criteria principal
     *
     * @param crit {@link Criteria} para realizar la cunsulta a la db
     * @param filters {@link Map} con los filter ingresados en el dataTable
     */
    protected void findPropertyFilter(Criteria crit, Map<String, Object> filters) {
        filters.entrySet().forEach((Map.Entry<String, Object> entry) -> {
            Criteria c = null;
            String key = entry.getKey();
            Object value = entry.getValue();
            Class type = (Class) ReflexionEntity.getTypeObject(BaseLazyDataModel.this.entity, key);
            if (type != null) {
                // Si es una clave Conpuesta solo se realiza envia busca esa propiedad en la misma entity
                // No soporta relaciones de entity con clave compuestas.
                c = this.findProperty(crit, key);
                key = tempKey;
                if (type.equals(String.class)) {
                    if (value != null) {
                        c.add(Restrictions.ilike(key, "%" + value.toString().trim() + "%"));
                    }
                } else if (type.equals(Character.class)) {
                    if (value != null) {
                        c.add(Restrictions.ilike(key, "%" + value.toString().trim() + "%"));
                    }
                } else if (type.equals(Boolean.class)) {
                    if (value != null) {
                        c.add(Restrictions.eq(key, Boolean.valueOf(value.toString().trim())));
                    }
                } else {
                    Class clazz = (Class) type;
                    Object obj = null;
                    try {
                        if (clazz.isPrimitive()) {
                            if (type.equals(long.class)) {
                                type = Long.class;
                                obj = Long.valueOf("0");
                            } else if (type.equals(short.class)) {
                                type = Short.class;
                                obj = Short.valueOf("0");
                            } else if (type.equals(int.class)) {
                                type = Integer.class;
                                obj = Integer.valueOf("0");
                            } else if (type.equals(boolean.class)) {
                                type = Boolean.class;
                                obj = Boolean.valueOf("false");
                            } else if (type.equals(double.class)) {
                                type = Double.class;
                                obj = Double.valueOf("0");
                            }
                        } else {
                            if (value != null) {
//                                    obj = clazz.newInstance();
                                obj = type;
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(BaseLazyDataModel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Boolean isNumber = Boolean.FALSE;
                    if (obj != null) {
                        isNumber = (obj instanceof Number);
                    }
                    if (isNumber) {
                        if (NumberUtils.isNumber(value.toString())) {
                            if (value != null) {
                                c.add(Restrictions.eq(key, ReflexionEntity.instanceConsString((Class) type, value.toString().trim())));
                            }
                        }
                    } else {
                        if (value != null) {
                            c.add(Restrictions.eq(key, ReflexionEntity.instanceConsString((Class) type, value.toString().trim())));
                        }
                    }
                }
            }
        });
    }

    /**
     * Realiza el recorrido realizando un split sobre el nombre del campo.
     *
     * @param crit Criteria
     * @param key Campo en para realizar el ordenamiento
     * @return Criteria para el campo solicitado.
     */
    private Criteria findProperty(Criteria crit, String key) {
        Criteria c = null;
        // Si es una clave Conpuesta solo se realiza envia busca esa propiedad en la misma entity
        // No soporta relaciones de entity con clave compuestas.
        if (key.contains(".") && !key.contains("PK")) {
            String[] split = key.split("\\.");
            int index = 0;
            for (String sp : split) {
                if (index == 0) {
                    c = crit.createCriteria(sp);
                } else if (index < (split.length - 1)) {
                    c = c.createCriteria(sp);
                } else {
                    key = sp;
                    tempKey = sp;
                }
                index++;
            }
        } else {
            c = crit;
            tempKey = key;
        }
        return c;
    }

//<editor-fold defaultstate="collapsed" desc="Getter and Setter">
    public Class<T> getEntity() {
        return entity;
    }

    public void setEntity(Class<T> entity) {
        this.entity = entity;
    }

    public String getDefaultSorted() {
        return defaultSorted;
    }

    public void setDefaultSorted(String defaultSorted) {
        this.defaultSorted = defaultSorted;
    }

    public String getDefaultSortOrder() {
        return defaultSortOrder;
    }

    public void setDefaultSortOrder(String defaultSortOrder) {
        this.defaultSortOrder = defaultSortOrder;
    }

    public Entitymanager getManager() {
        return manager;
    }

    public void setManager(Entitymanager manager) {
        this.manager = manager;
    }

    public Criteria getOrderCrit() {
        return orderCrit;
    }

    public void setOrderCrit(Criteria orderCrit) {
        this.orderCrit = orderCrit;
    }

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public String getColunmEstado() {
        return colunmEstado;
    }

    /**
     * Al asiganar el estado se debe agregar tambien {@code valueEstados}
     * @param colunmEstado 
     */
    public void setColunmEstado(String colunmEstado) {
        this.colunmEstado = colunmEstado;
    }

    public Object getValueEstado() {
        return valueEstado;
    }

    public void setValueEstado(Object valueEstado) {
        this.valueEstado = valueEstado;
    }

    public String[] getFilterss() {
        return filterss;
    }

    /**
     * Nombre de los campos a filtrar
     *
     * @param filterss nombre de los campos a filtrar
     * <p>
     * Ejemplo: "numPredio", "claveCat". solo los campos de la entity no soporta
     * filtros de campos relacionados.</p>
     */
    public void setFilterss(String[] filterss) {
        this.filterss = filterss;
    }

    public Object[] getFiltersValue() {
        return filtersValue;
    }

    /**
     * Valor de los filtros ingresados <code>filterss</code>
     *
     * @param filtersValue Valores para <code>filterss</code> en el mismo orden
     * que fueron ingresados.
     */
    public void setFiltersValue(Object[] filtersValue) {
        this.filtersValue = filtersValue;
    }

    public Boolean getEqualsEstado() {
        return equalsEstado;
    }

    /**
     * Si es true solo filtrara los que tienen ese estado, caso contrario
     * excluira ese estado
     *
     * @param equalsEstado true para filtrar por la propiedad estado, caso
     * contrario false.
     */
    public void setEqualsEstado(Boolean equalsEstado) {
        this.equalsEstado = equalsEstado;
    }

    public void setValueEstados(List valueEstados) {
        this.valueEstados = valueEstados;
    }

    public List getValueEstados() {
        return valueEstados;
    }

//</editor-fold>
}
