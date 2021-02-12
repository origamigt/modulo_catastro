/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.sgm.services.ejbs;

import com.origami.sgm.services.interfaces.TitulosReporteCacheLocal;
import com.origami.transactionalcore.entitymanager.Entitymanager;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import util.EntityBeanCopy;

/**
 *
 * @author Joao Sanga
 */
@Singleton(name = "titulosReporteCache")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)

public class TitulosReporteCache implements TitulosReporteCacheLocal {

    @javax.inject.Inject
    private Entitymanager manager;

    protected ConcurrentMap<String, TreeNode> treeMap;
    protected ConcurrentMap<String, String> lockerMap = new ConcurrentHashMap<>();

    @Override
    public void clearCache() {
        treeMap.remove("arbol_recaudaciones");
    }

    @Override
    public TreeNode getTree() {
        TreeNode root = treeMap.get("arbol_recaudaciones");
        if (root == null) {
            generarArbol();
            return getTree();
        }

        return (TreeNode) EntityBeanCopy.clone(root);
    }

    private TreeNode createTree() {
        TreeNode root = new DefaultTreeNode("Titulos", null);
        this.llenarArbol(root);
        return root;
    }

    private void llenarArbol(TreeNode root) {
        try {
//            List<RenTipoLiquidacion> raices = manager.findAll(QuerysFinanciero.getRenTransaccionesPadres, new String[]{"idPadre"}, new Object[]{0L});
//            for (RenTipoLiquidacion temp : raices) {
//                if (!temp.getTomado()) {
//                    temp.setTomado(true);
//                    TreeNode node = new DefaultTreeNode(temp, root);
//                    llenarHijosArbol(temp, node);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void generarArbol() {

        synchronized (getLockerObject()) {

            // comprobar si no se entró en espera y ya existe el "arbol_recaudaciones" mapeado:
            TreeNode tree = treeMap.get("arbol_recaudaciones");
            if (tree == null) {
                this.loadTree();
            }

        }
    }

    private void loadTree() {
        //TreeNode tree = this.getTree();

        TreeNode tree = createTree();
        // si se encontro menubar, realizar la clonacion
        if (tree != null) {
            treeMap.putIfAbsent("arbol_recaudaciones", tree);
        }
    }

    protected String getLockerObject() {
        lockerMap.putIfAbsent("arbol_recaudaciones", "arbol_recaudaciones");

        return lockerMap.get("arbol_recaudaciones");
    }

    /**
     * Inicializa el map de menubars en vacio
     */
    protected void initMenubarsMap() {
        this.treeMap = new ConcurrentHashMap<>();
    }

    /**
     * Inicializa el singleton ejb
     */
    @PostConstruct
    protected void init() {
        this.initMenubarsMap();
    }

}
