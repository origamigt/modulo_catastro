/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops.services;

/**
 *
 * @author Fernando
 */
public class OmegaConfigs {

	private String dburl, dburlTemp;
	private String dbuser, dbuserTemp;
	private String dbpass, dbpassTemp;
	private String tableName, tableNameTemp;

	private String dbType;

	public Boolean validarDatosConn() {
		return !(dburl == null && dbuser == null && dbpass == null && tableName == null);
	}

	public Boolean validarDatosConnTemp() {
		return !(dburlTemp == null && dbuserTemp == null && dbpassTemp == null && tableNameTemp == null);
	}

	public String getDburl() {
		return dburl;
	}

	public String getDbuser() {
		return dbuser;
	}

	public String getDbpass() {
		return dbpass;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getDburlTemp() {
		return dburlTemp;
	}

	public void setDburlTemp(String dburlTemp) {
		this.dburlTemp = dburlTemp;
	}

	public String getDbuserTemp() {
		return dbuserTemp;
	}

	public void setDbuserTemp(String dbuserTemp) {
		this.dbuserTemp = dbuserTemp;
	}

	public String getDbpassTemp() {
		return dbpassTemp;
	}

	public void setDbpassTemp(String dbpassTemp) {
		this.dbpassTemp = dbpassTemp;
	}

	public String getTableNameTemp() {
		return tableNameTemp;
	}

	public void setTableNameTemp(String tableNameTemp) {
		this.tableNameTemp = tableNameTemp;
	}

	public void setDburl(String dburl) {
		this.dburl = dburl;
	}

	public void setDbuser(String dbuser) {
		this.dbuser = dbuser;
	}

	public void setDbpass(String dbpass) {
		this.dbpass = dbpass;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

}
