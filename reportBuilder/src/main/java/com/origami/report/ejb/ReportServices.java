/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.origami.report.ejb;

import com.origami.annotations.Report;
import com.origami.annotations.ReportField;
import com.origami.report.cdi.Utils;
import com.origami.report.enums.NivelTree;
import com.origami.report.model.ModelJoins;
import com.origami.report.model.ModelMetadata;
import com.origami.report.model.ModelWhere;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.enterprise.context.Dependent;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.metadata.ClassMetadata;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 * Procesamiento de entities mapeados en el hibernate.cfg.xml,
 *
 * Ejb para obtener arblor con las tablas y campos de una tabla
 *
 * @author ANGEL NAVARRO
 */
@Stateless
//@Interceptors(value = {HibernateEjbInterceptor.class})
@Dependent
public class ReportServices {

    private static final Logger LOG = Logger.getLogger(ReportServices.class.getName());

    private final Class<Report> classRepor = Report.class;
    private final Class<ReportField> fieldRepor = ReportField.class;

    /**
     * Procesa el listado de entities que estan disponibles dentro del
     * hibernate.
     *
     * @return Node con el listado de tablas y columnas
     */
    @SuppressWarnings("unchecked")
    public TreeNode listarTablas() {
        Map<String, ClassMetadata> allClassMetadata = Utils.getSession("util.HiberUtil").getSessionFactory().getAllClassMetadata();

        List<ModelMetadata> models = new ArrayList<>();
        TreeNode node = new DefaultTreeNode("Root", null);
        for (Map.Entry<String, ClassMetadata> entry : allClassMetadata.entrySet()) {
            ClassMetadata value = entry.getValue();
            Class entity = value.getMappedClass();
            // Verificamos que tenga la anotacion Report para mostrar como reporte
            if (entity.isAnnotationPresent(classRepor)) {
                Report entityReport = (Report) entity.getAnnotation(classRepor);
                if (entityReport.showTree()) {
                    Table annotationTable = (Table) entity.getAnnotation(Table.class);
                    ModelMetadata tableMetadata = new ModelMetadata(entityReport.description(),
                            (annotationTable.schema() == null ? "" : annotationTable.schema().concat(".")) + annotationTable.name(),
                            annotationTable.name(), null, null);
                    // Tomanos la descripcion de la anotacion
                    tableMetadata.setNameClass(value.getEntityName());
                    tableMetadata.setIdentifier(value.getIdentifierPropertyName());
                    tableMetadata.setNivel(NivelTree.FIRST);
                    TreeNode table = new DefaultTreeNode(tableMetadata, node);
                    // Obtenemos todos los fields que tiene la entiti
                    loadFieldsObject(entity, table, null);
                }
            }
        }
        return node;
    }

    /**
     * Recorremos los campos de cada uno de las entities y verificamos que tenga
     * la anotacion {@link ReportField}
     *
     * @param entity Entiti a procesar los fields
     * @param table node padre
     * @param entityParent Entiti padre de donde se esta llamando la entiti
     */
    private void loadFieldsObject(Class entity, TreeNode table, Class entityParent) {
        ModelMetadata tableParent = (ModelMetadata) table.getData();
        int count_referen = 0;
        // Obtenemos todos los fields que tiene la entiti
        List<Field> fields = Utils.getFields(entity);
        // Recorremos cada uno de los campos
        for (Field field : fields) {
            // Verificamos que temga la anotacion ReportField
            if (field.isAnnotationPresent(fieldRepor)) {
                try {
                    ReportField anntFieldRep = (ReportField) field.getAnnotation(fieldRepor);
                    ModelMetadata model = new ModelMetadata();
                    // Tomamos el nombre del campo asignado
                    model.setParent(tableParent);
                    if (model.getParent().getIsCatalog()) {
                        model.setDescription(anntFieldRep.description() + " " + model.getParent().getDescription());
                    } else {
                        model.setDescription(anntFieldRep.description());
                    }
                    if (anntFieldRep.filter().length() > 0) {
                        model.setWhere(new ModelWhere(anntFieldRep.filter()));
                    }
                    TreeNode nodeField = null;
                    // Verificamos el tipo de campo
                    switch (anntFieldRep.type()) {
                        // Para los campos que solo sean de tipos de datos normales
                        case NORMAL:
                            Column col = field.getAnnotation(Column.class);
                            if (col != null) {
                                model.setSqlName(col.name());
                            } else {
                                model.setSqlName(field.getName());
                            }
                            model.setNameClass(field.getType().getName());
                            nodeField = new DefaultTreeNode(model, table);
                            break;
                        // Para las asosiaciones de objetos
                        case COLLECTION_ONE_TO_ONE:
                            // Verifica si la entidad se encuentra como padre la excluye para evitar entrar en ciclo infinito
                            if (this.verificarParent(field.getType().getName(), tableParent)) {
                                break;
                            }
                            // Verificamos los datos principales para agregar el modelo
                            this.checkAnnotation(field.getType(), model, count_referen);
                            // Verificamos si existe la Anotacion para obtner la columna referenciada
                            if (field.isAnnotationPresent(JoinColumn.class)) {
                                JoinColumn join = (JoinColumn) field.getAnnotation(JoinColumn.class);
//                                Obtenemos la columna que hace referencia
                                model.setReferencesColumn(join.referencedColumnName());
                                // Obtenemos el nombre de la columna que esta haciendo join
                                model.setSqlName(join.name());
                            }
                            // Verificamos si existe la Anotacion para obtner la columna referenciada
                            if (field.isAnnotationPresent(OneToOne.class)) {
                                OneToOne join = (OneToOne) field.getAnnotation(OneToOne.class);
                                //Obtenemos la columna que hace referencia
                                model.setReferencesColumn(join.mappedBy());
                                // Obtenemos el nombre de la columna que esta haciendo join
                                model.setSqlName(model.getParent().getIdentifier());
                            }
                            // Incrementamos el numero de referencia
                            count_referen++;
                            // Creamos el nodo
                            nodeField = new DefaultTreeNode(model, table);
                            // Obtenemos los campos para el caso que sea una referencia
                            loadFieldsObject(field.getType(), nodeField, entityParent);
                            break;
                        // Para las asosiaciones de lista
                        case COLLECTION_ONE_TO_MANY:
                            // Obtenemos los campos para el caso que sea una referencia
                            // Obtenemos el tipo de clase
//                            System.out.println("field.getGenericType().toString() " + field.getGenericType().toString());
                            String className = field.getGenericType().toString().substring(field.getGenericType().toString().lastIndexOf("<") + 1, field.getGenericType().toString().lastIndexOf(">"));
                            if (!entity.equals(entityParent)) {

                                Class clazz = Class.forName(className);
                                // Verifica si la entidad se encuentra como padre la excluye para evitar entrar en ciclo infinito
                                if (this.verificarParent(clazz.getName(), (ModelMetadata) table.getData())) {
                                    break;
                                }
                                // Verificamos si existe la Anotacion para obtner la columna referenciada
                                if (field.isAnnotationPresent(OneToMany.class)) {
                                    OneToMany joinC = (OneToMany) field.getAnnotation(OneToMany.class);
                                    //Obtenemos la columna que hace referencia
                                    model.setReferencesColumn(joinC.mappedBy());
                                }
                                // Verificamos los datos principales para agregar el modelo
                                this.checkAnnotation(clazz, model, count_referen);
                                // Incrementamos el numero de referencia
                                count_referen++;
                                // Obtenemos el nombre de la columna que esta haciendo join
                                model.setSqlName(model.getParent().getIdentifier());
                                // Creamos el nodo
                                nodeField = new DefaultTreeNode(model, table);
                                // Obtenemos los campos para el caso que sea una referencia
                                loadFieldsObject(clazz, nodeField, entityParent);
                            }
                            break;
                        default:
                            throw new AssertionError(anntFieldRep.type().name());
                    }
                } catch (ClassNotFoundException ex) {
                    LOG.log(Level.SEVERE, entity.getName(), ex);
                }
            } else if (field.isAnnotationPresent(Embedded.class)) {
                this.loadFieldsObject(field.getType(), table, entityParent);
            }
        }
    }

    private void checkAnnotation(Class entiti, ModelMetadata model, int count_referen) {
        ClassMetadata getOne = Utils.getSession("util.HiberUtil").getSessionFactory().getAllClassMetadata().get(entiti.getName());
        // Obtenemos el identificador de la tabla
        model.setIdentifier(getOne.getIdentifierPropertyName());
        //Agregamos el nivel que va tener en el arbol
        model.setNivel(model.getParent().getNivel().byCode(model.getParent().getNivel().getCode() + 1));
        // Obtenemos la anotacion Table
        Table annotationTable = (Table) entiti.getAnnotation(Table.class);
        // agregamos el nombre de la tabla junto con el schema
        model.setReferencesTable((annotationTable.schema() == null ? "" : annotationTable.schema().concat(".")) + annotationTable.name());
        // Agregamos el nombre de la tabla
        model.setAlias(annotationTable.name());
        // Creamos el alias para la tabla
        model.setKey(annotationTable.name() + (model.getNivel().getCode()) + count_referen);
        // Agregamos el nombre de entiti
        model.setNameClass(entiti.getName());
        // Verificamos si existe agregado la Anotacion Report
        if (entiti.isAnnotationPresent(Report.class)) {
            Report annotationReport = (Report) entiti.getAnnotation(Report.class);
            // Agregamos el valor si es catalogo
            model.setIsCatalog(annotationReport.isCatatalog());
        }
    }

    private Boolean verificarParent(String entity, ModelMetadata table) {
        if (table == null) {
            return false;
        }
        if (table.getNameClass().equals(entity)) {
            return true;
        }
        if (table.getParent() != null) {
            return this.verificarParent(entity, table.getParent());
        }
        return false;
    }

    public void processNodesUser(Map<TreeNode, ModelMetadata> nodesSeleccionados, StringBuilder selectUser, Boolean nativeQuery) {
        this.processNodes(nodesSeleccionados, selectUser, nativeQuery);
    }

    /**
     * Procesa todos los nodos y va agregando en las varaibles respectivas para
     * formar lasentencia sql
     *
     * @param selectionNodes Mapa con los nodos a procesar
     * @param SELECT variable donde se va agregar cada columna
     * @param FROM Mapa donde se va almacenar las tablas que se van agregar al
     * from y el tipp de join que tiene
     */
    private void processNodes(Map<TreeNode, ModelMetadata> selectionNodes, StringBuilder SELECT, Boolean nativeQuery) {
//        Contenedor de los FROM de las tablas seleccionadas
        Map<String, ModelJoins> FROM = new HashMap<>();
//        Contenedor para almacenar las codiciones WHERE
        StringBuilder whereConditions = new StringBuilder();
//        Contenedor para almacenar los ORDER BY
        StringBuilder orderesBY = new StringBuilder();
        try {
            // Recorremos cada uno de los campos seleccionados
            for (Map.Entry<TreeNode, ModelMetadata> itemTemp : selectionNodes.entrySet()) {
                // Obtenemos el valor
                ModelMetadata model = itemTemp.getValue();
                // La primera vez que se haga el recorrido se asigan valor a la variable parent
                // Verificamos si es un principal
                // Si esta dentro de un padre verificamos si esta en el FROM
                if (model.getNivel().getCode() >= 1) {
                    // creamos el model con los datos de la referencia
                    this.createJoin(model, FROM);
                }
                // Chepueamos que sea una columna normal que no sea una referencia ni nodo padre
                if (!model.getNivel().equals(NivelTree.FIRST) && !model.getIsReferences()) {
                    // Para los casos que la referencia se extiende a mas de dos verificamos que los padres esten registrados en el FROM
                    this.createJoin(model.getParent(), FROM);
                    // Obtenemos el nombre de la columna con el con el alias de la tabla
                    // agregamos el nombre de columna al select junto al alias que va tener
                    if (nativeQuery) {
                        SELECT.append("\n").append(model.getNameColumn()).append(" \"").append(model.getDescription()).append("\", ");
                    } else {
                        SELECT.append("<br/>").append(" \"").append(model.getDescription()).append("\", ");
                    }
                    // Verificamos si la columna esta en una clausula WHERE
                    if (model.getIsWhere()) {
                        // Si no hay clausula WHERE agregamos
                        if (whereConditions.length() == 0) {
                            whereConditions.append(model.getWhere().getCondition(model.getNameColumn()));
                        } else {
                            // Si ya existe agregados ponemos el AND O OR
                            if (nativeQuery) {
                                whereConditions.append("\n");
                            } else {
                                whereConditions.append("<br/>");
                            }
                            whereConditions.append(model.getWhere().getWhereType()).append(" ").append(model.getWhere().getCondition(model.getNameColumn()));
                        }
                    }
                    // Verificamos si esta incluido en una sentencia Order by
                    if (model.getOrderBy() != null) {
                        // Si no hay clausula WHERE agregamos
                        if (orderesBY.length() == 0) {
                            orderesBY.append(model.getOrder());
                        } else {
                            // Si ya existe agregados ponemos el AND O OR
                            orderesBY.append(", ").append(model.getOrder());
                        }
                    }
                }
            }
            this.createSql(SELECT, FROM, whereConditions, orderesBY, nativeQuery);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error procesando nodos seleccionados", e);
        }
    }

    /**
     * Agrega una nueva clausula join
     *
     * @param model nodo padre a agregar y verificar
     * @param FROM map donde estan todas los tablas con los alias
     */
    private void createJoin(ModelMetadata model, Map<String, ModelJoins> FROM) {
        // CHEQUEAMOS SI LA REFERENCIA MAS NIVELES para agregarlos primeros
        if (model.getParent() != null) {
            if (model.getParent().getNivel().getCode() >= 1) {
                this.createJoin(model.getParent(), FROM);
            }
        }
        // CREAMOS EL KEY QUE ES NOMBRE DE LA TABLA MAS EL ALIAS QUE VA TENER
        String reftable = (model.getReferencesTable() == null ? model.getSqlName() : model.getReferencesTable());
        // VERIFICAMOS SI NO EXISTE PARA AGREGARLO
        if (!FROM.containsKey(reftable.concat(" ").concat(model.getKey()))) {
            // ARMAMOS EL MODELO PARA LA CLAUSULA ON DEL JOIN
            ModelJoins join = null;
            String nameTable = null;
            if (model.getNivel().equals(NivelTree.FIRST)) {
                join = new ModelJoins(model.getNivel().getCode()); // CREAMOS UN JOIN SOLO CON EL NIVEL EN EL ARBOL
                nameTable = model.getSqlName().concat(" ").concat(model.getKey()); // AGREGAMOS EL NOMBRE DE LA TABLA CON ELALIAS QUE VA TENER
            } else {
//                System.out.println("Property join " + model + "\n");
                join = new ModelJoins(
                        model.getParent().getKey().concat(".").concat(model.getSqlName()), // REFERENCIA DE LA TABLA SUPERIOR
                        model.getKey().concat(".").concat(model.getReferencesColumn()), // CONDICION DE LA TABLA ACTUAL
                        model.getNivel().getCode()); // NIVEL EN EL ARBLOR
                if (model.getJoin() != null) {
                    join.setJoinType(model.getJoin());
                }
                nameTable = reftable.concat(" ").concat(model.getKey());
            }
            // AGREGAMOS A MAPA DE LOS FROM
            FROM.putIfAbsent(nameTable, join);
        }

    }

    /**
     * Forma lasentencia SQL.
     */
    private void createSql(StringBuilder SELECT, Map<String, ModelJoins> FROM, StringBuilder whereConditions, StringBuilder orderesBY, Boolean nativeQuery) {
        // OBtenemos la ultima coma
        int indexUltimaComma = SELECT.lastIndexOf(",");
        // Creamos una variabletemporal para formar el FROM
        StringBuilder tempfrom = new StringBuilder("");
        //verificamos si existe una utlima coma y la removemos
        if (indexUltimaComma > -1) {
            SELECT.replace(indexUltimaComma, indexUltimaComma + 1, "");
        }
        // Recoremos el listado de FROM
//        System.out.println("FROM " + FROM);
        FROM = FROM.entrySet().stream().sorted(Map.Entry.comparingByValue((ModelJoins e1, ModelJoins e2) -> e1.getNivel().compareTo(e2.getNivel()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        for (Map.Entry<String, ModelJoins> entryFrom : FROM.entrySet()) {
            String key = entryFrom.getKey();
            ModelJoins value = entryFrom.getValue();
            // Verificamos si tiene condicion join
            if (!value.isJoin()) {
                // verificamos si esta vacio la variable temporal para agregar la coma
                if (tempfrom.length() == 0) {
                    tempfrom.append(key);
                } else {
                    tempfrom.insert(0, key.concat(", "));
                }
            } else {
                // Armamos el JOIN
                if (nativeQuery) {
                    tempfrom.append("\n");
                } else {
                    tempfrom.append("<br/>");
                }
                tempfrom.append(value.getJoinType().getNameJoin()) // TIPO DE JOIN INNER, LEFT, ETC
                        .append(" ").append(key) // TABLA QUE ESTA EN CLAUSULA JOIN
                        .append(" ON (")
                        .append(value.getNameColumn())
                        .append(" = ")
                        .append(value.getNameReferencesColumn())
                        .append(")");
            }
        }

        if (tempfrom.length() == 0) {
            SELECT.delete(0, SELECT.length());
        } else {
            // Concatenamos el SELECT +FROM
            if (nativeQuery) {
                SELECT.append("\nFROM ").append(tempfrom);
            } else {
                SELECT.append("<br/>FROM ").append(tempfrom);
            }
        }
        // chequeamos si hay WHERE agregados
        if (whereConditions != null) {
            if (whereConditions.length() > 0) {
                if (nativeQuery) {
                    SELECT.append("\nWHERE ").append(whereConditions);
                } else {
                    SELECT.append("<br/>WHERE ").append(whereConditions);
                }
            }
        }
        // chequeamos si hay ORDER BY y agregados
        if (orderesBY != null) {
            if (orderesBY.length() > 0) {
                if (nativeQuery) {
                    SELECT.append("\nORDER BY ").append(orderesBY);
                } else {
                    SELECT.append("<br/>ORDER BY ").append(orderesBY);
                }
            }
        }
    }

}
