/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.cdi;

import com.origami.report.ejb.ReportServices;
import com.origami.report.enums.JoinType;
import com.origami.report.enums.NivelTree;
import com.origami.report.enums.OrderType;
import com.origami.report.enums.WhereComparator;
import com.origami.report.enums.WhereType;
import com.origami.report.model.ModelMetadata;
import com.origami.report.model.ModelWhere;
import com.origami.report.model.Variables;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.MenuActionEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.NodeUnselectEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;

/**
 * Permite crear reporte y exportarlos a excel
 *
 * @author ANGEL NAVARRO
 */
@Named
@ViewScoped
public class ReportBuilder implements Serializable {

    private static final Logger LOG = Logger.getLogger(ReportBuilder.class.getName());

    private TreeNode node;
    private TreeNode nodeSelect;
    private TreeNode selectionNodes[];
    private String selectEdit;
    private StringBuilder SELECT;
    private StringBuilder selectUser;

    private ModelMetadata selectItem;
    /**
     * Variable para agregar una nueva condicion WHERE
     */
    private ModelWhere condition;
    /**
     * Mapa que almacena todas las condiciones where agregadas
     */
    private Map<TreeNode, ModelMetadata> nodesSeleccionados;

    @Inject
    private ServletSessionReport ss;
    /**
     * Ejb para obtener arblor con las tablas y campos de una tabla
     */
    @Inject
    private ReportServices services;

    /**
     * Inicializa los datos para el facelet.
     */
    @PostConstruct
    public void InitView() {
        System.out.println("Iniciado arbol de tablas");
        node = services.listarTablas();
        SELECT = new StringBuilder();
        System.out.println("Arbol de tablas listas");
    }

    /**
     * Llama al servlet y envia a ejecutar el sql y realiza la exportacion a
     * xlsx
     */
    public void exportar() {
        try {
            System.out.println("Exportando datos...");
            ss.agregarParametro(Variables.SENTENCIA, selectEdit);
            ss.setNombreDocumento("InformePredios(" + new Date().getTime() + ")");
            ss.setNombreReporte("Informacion Alfanumerica");
            Utils.redirectNewTab(Utils.getContextPath() + "exporter/xlsx");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error el exportar datos", e);
        }
    }

    /**
     * Limpia todos los datos del formulario
     */
    public void clearData() {
        Utils.redirectFaces("/faces/report/reportBuilder.xhtml");
    }

    /**
     * Evento lanzado al selecionar un nodo nuevo
     *
     * @param selectEvent
     */
    public void selectNode(NodeSelectEvent selectEvent) {
        try {
            if (selectEvent.isContextMenu()) {
                nodeSelect = selectEvent.getTreeNode();
                selectItem = (ModelMetadata) selectEvent.getTreeNode().getData();
                if (selectItem.getIsWhere()) {
                    condition = selectItem.getWhere();
                } else {
                    condition = new ModelWhere();
                }
            } else {
                // Inicializamos las variables
                SELECT = new StringBuilder("SELECT ");
                // Enviamos a procesar los nodos seleccionados
                nodesSeleccionados = new HashMap<>();
                for (TreeNode node1 : selectionNodes) {
                    nodesSeleccionados.putIfAbsent(node1, (ModelMetadata) node1.getData());
                }
                System.out.println("nodesSeleccionados " + nodesSeleccionados.size() + " selectionNodes " + selectionNodes.length);
                services.processNodesUser(nodesSeleccionados, SELECT, true);
                // Creamos la sentencia para el usuario final
                selectUser = new StringBuilder("SELECCIONAR ");
                services.processNodesUser(nodesSeleccionados, selectUser, false);
                selectEdit = SELECT.toString();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al seleccionar nodo", e);
        }
    }

    /**
     * Evento lanzado al deselecionar un nodo
     *
     * @param selectEvent
     */
    public void unSelectNode(NodeUnselectEvent selectEvent) {
        try {
// Inicializamos las variables
            SELECT = new StringBuilder("SELECT ");
            // Removemos el nodo que que fue quitado de la lista los nodos
            if (nodesSeleccionados != null) {
                nodesSeleccionados.remove(selectEvent.getTreeNode());
                deleteChildrenNode(selectEvent.getTreeNode());
            }

            // Enviamos a procesar los nodos seleccionados
            if (nodesSeleccionados != null) {
                services.processNodesUser(nodesSeleccionados, SELECT, true);
                // Creamos la sentencia para el usuario final
                selectUser = new StringBuilder("SELECCIONAR ");
                services.processNodesUser(nodesSeleccionados, selectUser, false);
                selectEdit = SELECT.toString();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al quitar nodo", e);
        }
    }

    private void deleteChildrenNode(TreeNode node) {
        if (Utils.isNotEmpty(node.getChildren())) {
            for (TreeNode children : node.getChildren()) {
                nodesSeleccionados.remove(children);
                deleteChildrenNode(children);
            }
        }
    }

    public void addWhereClause(ActionEvent event) {
        Utils.executeJS("PF('dlgCnditions').show();");
    }

    public void deleteWhereClause(ActionEvent event) {
        try {
// Indicamos que el campo esta en la clausula where
            // Asignamos el modelo para la clausula where
            selectItem.setWhere(null);
            // Modificamos el valor del nodo de donde se obtienen los datos para realizar la sentencia
            nodesSeleccionados.put(nodeSelect, selectItem);
            // Modificamos el nodo root
            findNode(node, nodeSelect);
            condition = null;
            selectItem = null;
            // LIMPIAMOS LAS VARIABLES
            SELECT = new StringBuilder("SELECT ");
            // Enviamos a procesar los nodos seleccionados
            services.processNodesUser(nodesSeleccionados, SELECT, true);
            // Creamos la sentencia para el usuario final
            selectUser = new StringBuilder("SELECCIONAR ");
            services.processNodesUser(nodesSeleccionados, selectUser, false);
            selectEdit = SELECT.toString();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al eliminar clausula", e);
        }
    }

    /**
     * Agrega la nueva restriccion a la consulta
     */
    public void saveWhereConditions() {
        try {
// Indicamos que el campo esta en la clausula where
            // Asignamos el modelo para la clausula where
            selectItem.setWhere(condition);
            // Modificamos el valor del nodo de donde se obtienen los datos para realizar la sentencia
            nodesSeleccionados.put(nodeSelect, selectItem);
            // Modificamos el nodo root
            findNode(node, nodeSelect);
            condition = null;
            selectItem = null;
            // LIMPIAMOS LAS VARIABLES
            SELECT = new StringBuilder("SELECT ");
            // Enviamos a procesar los nodos seleccionados
            services.processNodesUser(nodesSeleccionados, SELECT, true);
            // Creamos la sentencia para el usuario final
            selectUser = new StringBuilder("SELECCIONAR ");
            services.processNodesUser(nodesSeleccionados, selectUser, false);
            // Creamos la sentencia sql
            selectEdit = SELECT.toString();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al guardar datos", e);
        }
    }

    public void editJoin(ActionEvent event) {
        try {
            MenuItem menuItem = ((MenuActionEvent) event).getMenuItem();
            selectItem.setJoin(JoinType.valueOf(menuItem.getParams().get("joinType").get(0)));
            // Asignamos el modelo para la clausula where
            // Modificamos el valor del nodo de donde se obtienen los datos para realizar la sentencia
            nodesSeleccionados.put(nodeSelect, selectItem);
            // Modificamos el nodo root
            findNode(node, nodeSelect);
            condition = null;
            selectItem = null;
            // LIMPIAMOS LAS VARIABLES
            SELECT = new StringBuilder("SELECT ");
            // Enviamos a procesar los nodos seleccionados
            services.processNodesUser(nodesSeleccionados, SELECT, true);
            // Creamos la sentencia para el usuario final
            selectUser = new StringBuilder("SELECCIONAR ");
            services.processNodesUser(nodesSeleccionados, selectUser, false);
            selectEdit = SELECT.toString();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error editar join", e);
        }
    }

    public void agregarOrderBy(ActionEvent event) {
        try {
            MenuItem menuItem = ((MenuActionEvent) event).getMenuItem();
            // Asignamos la propiedad orderBy
            selectItem.setOrderBy(OrderType.valueOf(menuItem.getParams().get("orderByCode").get(0)));
            // Modificamos el valor del nodo de donde se obtienen los datos para realizar la sentencia
            nodesSeleccionados.put(nodeSelect, selectItem);
            // Modificamos el nodo root
            findNode(node, nodeSelect);
            condition = null;
            selectItem = null;
            // LIMPIAMOS LAS VARIABLES
            SELECT = new StringBuilder("SELECT ");
            // Enviamos a procesar los nodos seleccionados
            services.processNodesUser(nodesSeleccionados, SELECT, true);
            selectEdit = SELECT.toString();
            System.out.println("Agregar Order by " + selectEdit);
            selectEdit = SELECT.toString();
            Utils.update("frmMainReport:tabContainer:sentencia");
            // Creamos la sentencia para el usuario final
            selectUser = new StringBuilder("SELECCIONAR ");
            services.processNodesUser(nodesSeleccionados, selectUser, false);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error agregar order by", e);
        }
    }

    public void deleteOrderByClause(ActionEvent event) {
        try {
// Indicamos que el campo esta en la clausula where
            // Asignamos el modelo para la clausula where
            selectItem.setOrderBy(null);
            // Modificamos el valor del nodo de donde se obtienen los datos para realizar la sentencia
            nodesSeleccionados.put(nodeSelect, selectItem);
            // Modificamos el nodo root
            findNode(node, nodeSelect);
            condition = null;
            selectItem = null;
            // LIMPIAMOS LAS VARIABLES
            SELECT = new StringBuilder("SELECT ");
            // Enviamos a procesar los nodos seleccionados
            services.processNodesUser(nodesSeleccionados, SELECT, true);
            // Creamos la sentencia para el usuario final
            selectUser = new StringBuilder("SELECCIONAR ");
            services.processNodesUser(nodesSeleccionados, selectUser, false);
            selectEdit = SELECT.toString();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al eliminar order by", e);
        }
    }

    /**
     * Busca el nodo seleccionado dento del nodo root
     *
     * @param node nodo root
     * @param replace nodo a buscar y reemplazar
     */
    private void findNode(TreeNode node, TreeNode replace) {
        List<TreeNode> subChild = node.getChildren();
        if (Utils.isNotEmpty(subChild)) {
            for (TreeNode treeNode : subChild) {
                if (treeNode == replace) {
                    int indexOf = subChild.indexOf(replace);
                    if (indexOf > -1) {
                        node.getChildren().add(indexOf, replace);
                        break;
                    }
                }
                findNode(treeNode, replace);
            }
        }
    }

    public TreeNode getNode() {
        return node;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }

    public TreeNode[] getSelectionNodes() {
        return selectionNodes;
    }

    public void setSelectionNodes(TreeNode[] selectionNodes) {
        this.selectionNodes = selectionNodes;
    }

    public StringBuilder getSELECT() {
        return SELECT;
    }

    public void setSELECT(StringBuilder SELECT) {
        this.SELECT = SELECT;
    }

    public ModelMetadata getSelectItem() {
        return selectItem;
    }

    public void setSelectItem(ModelMetadata selectItem) {
        this.selectItem = selectItem;
    }

    public boolean addWhere() {
        if (selectItem == null) {
            return false;
        }

        return !selectItem.getIsWhere();
    }

    public boolean removeWhere() {
        if (selectItem == null) {
            return false;
        }

        return selectItem.getIsWhere();
    }

    public ModelWhere getCondition() {
        return condition;
    }

    public void setCondition(ModelWhere condition) {
        this.condition = condition;
    }

    public List<WhereType> getWhereTypes() {
        return Arrays.asList(WhereType.values());
    }

    public List<WhereComparator> getWhereComparators() {
        return Arrays.asList(WhereComparator.values());
    }

    public List<JoinType> getJoinTypes() {
        return Arrays.asList(JoinType.values());
    }

    public StringBuilder getSelectUser() {
        return selectUser;
    }

    public void setSelectUser(StringBuilder selectUser) {
        this.selectUser = selectUser;
    }

    public String getSelectEdit() {
        return selectEdit;
    }

    public void setSelectEdit(String SelectEdit) {
        this.selectEdit = SelectEdit;
    }

    /**
     * Genera el contextMenu a partir de las caracteristicas del nodo
     * seleccionado
     *
     * @return MenuModel para item
     */
    public MenuModel getMenuModel() {
        MenuModel model = new DefaultMenuModel();
        DefaultMenuItem item = null;
        if (selectItem != null) {
            if (selectItem.getNivel().equals(NivelTree.FIRST)) {
                item = new DefaultMenuItem("Limite");
                item.setId("1");
                item.setTitle("Limit de registros a exportar");
                item.setImmediate(false);
                model.addElement(item);
            } else {
                if (!selectItem.getIsReferences()) {
                    // Verificacion de condicion WHERE
                    if (selectItem.getIsWhere()) {
                        item = new DefaultMenuItem("Editar Clausula Where");
                        item.setId("2");
                        item.setTitle("Edita los datos de la condicion");
                        item.setCommand("#{reportBuilder.addWhereClause}");
                        item.setUpdate("frmCond, frmCond:pngCod");
                        item.setImmediate(true);
                        model.addElement(item);

                        item = new DefaultMenuItem("Quitar de Clausula Where");
                        item.setId("3");
                        item.setTitle("Elimina el campo de la condicion");
                        item.setCommand("#{reportBuilder.deleteWhereClause}");
                        item.setUpdate(":frmMainReport:tabContainer:sentencia, :frmMainReport:tabContainer:sentenciaUser");
                        item.setImmediate(true);
                        model.addElement(item);
                    } else {
                        item = new DefaultMenuItem("Agregar a Clausula Where");
                        item.setId("1");
                        item.setTitle("Agrega el campo a la condicion");
                        item.setCommand("#{reportBuilder.addWhereClause}");
                        item.setUpdate("dlgCnditions, frmCond");
                        item.setImmediate(true);
                        model.addElement(item);
                    }
                    // Agregamos las opciones de ORDER BY
                    for (OrderType value : OrderType.values()) {
                        item = new DefaultMenuItem("ORDER BY " + value.getValue());
                        item.setId((model.getElements().size() + 2) + "");
                        item.setTitle(value.getDescripcion());
                        item.setCommand("#{reportBuilder.agregarOrderBy}");
                        item.setUpdate(":frmMainReport:tabContainer:sentenciaUser, frmMainReport:tabContainer:sentencia");
                        item.setImmediate(true);
                        item.setParam("orderByCode", value);
                        model.addElement(item);
                    }
                    // Verificacion de Order by
                    if (selectItem.getOrderBy() != null) {
                        item = new DefaultMenuItem("Eliminar de Order By");
                        item.setId((model.getElements().size() + 3) + "");
                        item.setTitle("Elimina la columna de la clausula Order By");
                        item.setCommand("#{reportBuilder.deleteOrderByClause}");
                        item.setUpdate(":frmMainReport:tabContainer:sentencia, :frmMainReport:tabContainer:sentenciaUser");
                        item.setImmediate(true);
                        model.addElement(item);
                    }
                } else {
                    for (JoinType value : JoinType.values()) {
                        item = new DefaultMenuItem(value.getNameJoin());
                        item.setId(value.getCodigo() + "");
                        item.setTitle(value.getDescripcion());
                        item.setCommand("#{reportBuilder.editJoin}");
                        item.setUpdate(":frmMainReport:tabContainer:sentencia, :frmMainReport:tabContainer:sentenciaUser");
                        item.setImmediate(true);
                        item.setParam("joinType", value);
                        model.addElement(item);
                    }
                }
            }
        }
        model.generateUniqueIds();
        return model;

    }
}
