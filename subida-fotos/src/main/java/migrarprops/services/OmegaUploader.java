/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import migrarprops.models.CodPredial;
import migrarprops.models.PredioClave;

/**
 *
 * @author Fernando
 */
public class OmegaUploader {

	private OmegaConfigs configs;

	public OmegaUploader(OmegaConfigs configs) {
		this.configs = configs;
		if (this.configs == null) {
			System.out.println("Debe Ingresar la configuraciones de la base de datos...");
			return;
		}
	}

	public Boolean streamFile(Long oid, OutputStream out) {
		Boolean flag = false;
		LargeObjectManager lobj = null;
		LargeObject obj = null;
		Connection conn = this.getDocumentalConnection();
		try {
			// All LargeObject API calls must be within a transaction block
			if (conn == null) {
				return false;
			}
			conn.setAutoCommit(false);
			// Get the Large Object Manager to perform operations with
			lobj = ((org.postgresql.PGConnection) conn).getLargeObjectAPI();
			try {
				obj = lobj.open(oid, LargeObjectManager.READ);
			} catch (SQLException e) {
				flag = false;
			}
			if (obj != null) {
				flag = true;
			} else {
				return false;
			}
			byte buf[] = new byte[2048];
			int s, tl = 0;
			while ((s = obj.read(buf, 0, 2048)) > 0) {
				out.write(buf, 0, s);
				tl += s;
				out.flush();
			}
			obj.close();
			conn.commit();

		} catch (SQLException | IOException ex) {
			flag = false;
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				conn.close();
			} catch (SQLException ex) {
				flag = false;
				Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return flag;
	}

	public Long uploadFile(InputStream stream, String nombre, String contentType) {
		Connection conn = this.getDocumentalConnection();
		Long oid = null;

		try {
			// All LargeObject API calls must be within a transaction block
			conn.setAutoCommit(false);
			// Get the Large Object Manager to perform operations with
			LargeObjectManager lobj = ((org.postgresql.PGConnection) conn).getLargeObjectAPI();
			// Create a new large object
			oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
			// Open the large object for writing
			LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
			// Copy the data from the file to the large object
			byte buf[] = new byte[2048];
			int s, tl = 0;
			while ((s = stream.read(buf, 0, 2048)) > 0) {
				obj.write(buf, 0, s);
				tl += s;
			}
			// Close the large object
			obj.close();
			// Now insert the row into table
			PreparedStatement ps = conn
					.prepareStatement("INSERT INTO " + this.configs.getTableName() + " VALUES (?, ?, ?, ?, ?)");
			ps.setLong(1, oid);
			ps.setString(2, nombre);
			ps.setBoolean(3, false);
			ps.setTimestamp(4, new Timestamp((new Date()).getTime()));
			ps.setString(5, contentType);
			ps.executeUpdate();
			ps.close();
			stream.close();
			// Finally, commit the transaction.
			conn.commit();
		} catch (SQLException | IOException ex) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				conn.close();
			} catch (SQLException ex) {
				Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return oid;
	}

	public Connection getDocumentalConnection() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		}
		Properties props = new Properties();
		props.setProperty("user", this.configs.getDbuser());
		props.setProperty("password", this.configs.getDbpass());
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(this.configs.getDburl(), props);
		} catch (SQLException ex) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return conn;
	}

	public Connection getDocumentalConnectionTemp() {
		try {
			if (this.configs.getDbType().equals("POSTGRES")) {
				Class.forName("org.postgresql.Driver");
			} else {
				Class.forName("oracle.jdbc.driver.OracleDriver");
			}
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		}
		Properties props = new Properties();
		props.setProperty("user", this.configs.getDbuserTemp());
		props.setProperty("password", this.configs.getDbpassTemp());
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(this.configs.getDburlTemp(), props);
		} catch (SQLException ex) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return conn;
	}

	public void agregarFotoPredio(Long oid, String contentType, String nombre, Long predio, String descripcion,
			Boolean enable, Long codPredio) {
		try {
			Connection conn = this.getDocumentalConnectionTemp();
			conn.setAutoCommit(false);
			// Now insert the row into table
			try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + this.configs.getTableNameTemp()
					+ "(content_type, file_oid, nombre, id_predio, descripcion, sis_enabled, cod_predio) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

				ps.setString(1, contentType);
				ps.setLong(2, oid);
				ps.setString(3, nombre);
				ps.setLong(4, (predio == null ? -1L : predio));
				ps.setString(5, descripcion);
				ps.setBoolean(6, true);
				ps.setLong(7, (codPredio == null ? -1L : codPredio));
				ps.executeUpdate();
				ResultSet key = ps.getGeneratedKeys();
				key.next();
				Long id = key.getLong(1);
				System.out.println(id);
				key.close();
			}
			// Finally, commit the transaction.
			conn.commit();
			conn.close();
		} catch (SQLException ex) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public PredioClave findIdPredio(String claveCatPredio) {
		Connection conn = this.getDocumentalConnectionTemp();
		PredioClave datos = null;
		try {
			PreparedStatement ps = conn.prepareCall(
					"SELECT id, num_predio, clave_cat, predialant FROM  sgm_app.cat_predio WHERE TRIM(clave_cat)=?");

			ps.setString(1, claveCatPredio.trim());
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				datos = new PredioClave(claveCatPredio, resultSet.getLong("id"), resultSet.getLong("num_predio"));
			}
			if (datos == null) {
				datos = new PredioClave(claveCatPredio, null, null);
			}
			ps.closeOnCompletion();
			conn.close();
		} catch (SQLException e) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, e);
		}
		return datos;
	}

	public PredioClave findIdPredioCompletar(String claveCatPredio, Integer postAddChar, Boolean completarCadena) {
		Connection conn = this.getDocumentalConnectionTemp();
		PredioClave datos = null;
		try {
			CodPredial cod = new CodPredial(claveCatPredio, postAddChar, completarCadena);
			claveCatPredio = cod.getClavecat();
			PreparedStatement ps = conn.prepareCall(
					"SELECT id, num_predio, clave_cat, predialant FROM  sgm_app.cat_predio WHERE TRIM(clave_cat)=?");
			ps.setString(1, claveCatPredio.trim());
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				datos = new PredioClave(claveCatPredio, resultSet.getLong("id"), resultSet.getLong("num_predio"));
			}
			if (datos == null) {
				datos = new PredioClave(claveCatPredio, null, null);
			}
			ps.closeOnCompletion();
			conn.close();
		} catch (SQLException e) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, e);
		}
		return datos;
	}

	public Boolean existeFoto(String nombrefoto, Long idPredio) {
		Connection conn = this.getDocumentalConnectionTemp();
		Long dato = null;
		try {
			PreparedStatement ps = conn.prepareCall("SELECT id FROM  sgm_app.foto_predio WHERE nombre = ?");
			ps.setString(1, nombrefoto);
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				dato = resultSet.getLong("id");
			}
			ps.closeOnCompletion();
			conn.close();
		} catch (SQLException e) {
			Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, e);
		}
		return dato != null;
	}

	public Boolean insertIntoBacth(List<List<Object>> paramt) {
		Boolean x = null;
		Connection c = this.getDocumentalConnectionTemp();
		String sql = "INSERT INTO " + this.configs.getTableNameTemp()
				+ "(content_type, file_oid, nombre, id_predio, descripcion, sis_enabled, cod_predio) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
		try {
			if (c != null) {
				c.setAutoCommit(false);
				try (PreparedStatement ps = c.prepareStatement(sql)) {
					for (List<Object> lo : paramt) {
						int countParamt = 1;
						for (Object object : lo) {
							ps.setObject(countParamt, object);
							countParamt++;
						}
						ps.addBatch();
					}
					ps.executeBatch();
					c.commit();
				}
			}
		} catch (SQLException e) {
			try {
				Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, e);
				c.rollback();
			} catch (SQLException ex) {
				Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
			}
		} finally {
			try {
				c.close();
			} catch (SQLException ex) {
				Logger.getLogger(OmegaUploader.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		return x;
	}

}
