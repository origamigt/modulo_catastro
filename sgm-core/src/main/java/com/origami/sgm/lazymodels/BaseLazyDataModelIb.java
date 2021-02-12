/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.lazymodels;

import com.origami.sgm.bpm.util.ReflexionEntity;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author CarlosLoorVargas
 * @param <T> Entity Class
 */
public class BaseLazyDataModelIb<T extends Object> extends LazyDataModel<T> {

    private Entitymanager manager;
    private Class<T> entity;
    private int rowCount = 0;
    private String defaultSorted = "id";
    private String defaultSortOrder = "ASC";
    private Criteria orderCrit;
    private String orderField;

    public BaseLazyDataModelIb() {
        manager = BeanProvider.getContextualReference(Entitymanager.class);
    }

    public BaseLazyDataModelIb(Class<T> entity) {
        this.entity = entity;
        this.defaultSorted = ReflexionEntity.getNameIdEntity(entity);
        manager = BeanProvider.getContextualReference(Entitymanager.class);
    }

    public BaseLazyDataModelIb(Class<T> entity, String defaultSorted) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        manager = BeanProvider.getContextualReference(Entitymanager.class);
    }

    public BaseLazyDataModelIb(Class<T> entity, String defaultSorted, String defaultSortOrder) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.defaultSortOrder = defaultSortOrder;
        manager = BeanProvider.getContextualReference(Entitymanager.class);
    }

    public BaseLazyDataModelIb(Class<T> entity, String defaultSorted, String defaultSortOrder,
            String defaultSorted2, String defaultSortOrder2, String defaultSorted3,
            String defaultSortOrder3, String defaultSorted4, String defaultSortOrder4) {
        this.entity = entity;
        this.defaultSorted = defaultSorted;
        this.defaultSortOrder = defaultSortOrder;
        manager = BeanProvider.getContextualReference(Entitymanager.class);
    }

    public void criteriaFilterSetup(Criteria crit, Map<String, Object> filters) throws Exception {
        try {
            if (filters == null || filters.isEmpty()) {
                return;
            }
            filters.entrySet().forEach((Map.Entry<String, Object> entry) -> {
                Criteria c = null;
                String key = entry.getKey();
                Object value = entry.getValue();
                Class type = (Class) ReflexionEntity.getTypeObject(this.entity, key);
                if (type != null) {
                    if (key.contains(".")) {
                        String[] split = key.split("\\.");
                        int index = 0;
                        for (String sp : split) {
                            if (index == 0) {
                                c = crit.createCriteria(sp);
                            } else if (index < (split.length - 1)) {
                                c = c.createCriteria(sp);
                            } else {
                                key = sp;
                            }
                            index++;
                        }
                    } else {
                        c = crit;
                    }
                    if (type.equals(String.class)) {
                        if (value != null) {
                            c.add(Restrictions.ilike(key, "%" + value.toString().trim() + "%"));
                        }
                    } else if (type.equals(Character.class)) {
                        if (value != null) {
                            c.add(Restrictions.ilike(key, "%" + value.toString().trim() + "%"));
                        }
                    } else {
                        if (NumberUtils.isNumber(value.toString())) {
                            c.add(Restrictions.eq(key, ReflexionEntity.instanceConsString((Class) type, value.toString().trim())));
                        }
                    }
                }
            });
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        List result = null;
        Criteria cq, dcq;
        try {
            Session session = manager.getSession();
            cq = session.createCriteria(this.getEntity(), "entity");
            this.criteriaFilterSetup(cq, filters);
            cq.setProjection(Projections.projectionList().add(Projections.rowCount()));
            rowCount = ((Long) cq.uniqueResult()).intValue();
            this.setRowCount(rowCount);

            dcq = session.createCriteria(this.getEntity(), "entity1");
            this.criteriaFilterSetup(dcq, filters);
            if (orderCrit != null) {
                this.criteriaSortSetup(orderCrit, orderField, sortOrder);
            } else {
                this.criteriaSortSetup(dcq, sortField, sortOrder);
            }
            this.criteriaPageSetup(dcq, first, pageSize);
            result = dcq.list();
            Hibernate.initialize(result);
//            HiberUtil.closeSession();
        } catch (Exception ex) {
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }

    public T getRowData(Object key) {
        T ob = null;
        try {
            ob = manager.find(entity, key);
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, null, e);
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
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, null, e);
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
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void criteriaPageSetup(Criteria crit, int first, int pageSize) {
        try {
            crit.setFirstResult(first);
            crit.setMaxResults(pageSize);
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void criteriaSortSetup(Criteria crit, String field, SortOrder order) {
        try {
            if (field == null) {
                crit.addOrder((defaultSortOrder.equalsIgnoreCase("ASC")) ? Order.asc(defaultSorted) : Order.desc(defaultSorted));
            } else {
                if (order.equals(SortOrder.ASCENDING)) {
                    crit.addOrder(Order.asc(field));
                } else {
                    crit.addOrder(Order.desc(field));
                }
            }
        } catch (Exception e) {
            Logger.getLogger(BaseLazyDataModelIb.class.getName()).log(Level.SEVERE, null, e);
        }
    }

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

}
