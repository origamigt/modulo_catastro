/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops;

import migrarprops.models.PredioClave;
import migrarprops.services.OmegaConfigs;
import migrarprops.subirFotos.MigrarFotos;

/**
 *
 * @author Angel Navarro
 */
public class MigrarProps {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		MigrarFotos mg = new MigrarFotos();

		OmegaConfigs config = new OmegaConfigs();
		// BASE FOTOS
		config.setDbpass("sis98");
		config.setDbuser("sisapp");
		config.setDburl("jdbc:postgresql://172.16.8.109:5432/fotos");
		// config.setDburl("jdbc:postgresql://192.168.1.95:5432/fotos");
		config.setTableName("archivos.doc_file");
		// base SGM POSTGRES - ORACLE
		// config.setDbType("ORACLE");
		config.setDbType("POSTGRES");
		config.setDbpassTemp("sis98");
		config.setDbuserTemp("sisapp");
		// config.setDburlTemp("jdbc:oracle:thin:@127.0.0.1:1522:DESAORCL");
		config.setDburlTemp("jdbc:postgresql://172.16.8.109:5432/prueba");
		config.setTableNameTemp("sgm_app.foto_predio");
		mg.setConfig(config);
		mg.upload("/home/test");
		if (mg.getWithoutpredio() != null || !mg.getWithoutpredio().isEmpty()) {
			for (PredioClave fp : mg.getWithoutpredio()) {
				System.out.println(fp);
			} 
		}
	}

}
