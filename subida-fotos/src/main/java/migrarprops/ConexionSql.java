/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Permite realizar la coneccion a la base que se le pasa los parametros.
 *
 * @author Anyelo
 */
public class ConexionSql {

    public static Connection getConnection(String orgpostgresqlDriver, String url, String user, String pass) {
        Connection conexion = null;
        try {
            Class.forName(orgpostgresqlDriver);
            conexion = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException | SQLException e) {
            Logger.getLogger(ConexionSql.class.getName()).log(Level.SEVERE, null, e);
        }
        return conexion;
    }

}
