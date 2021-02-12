/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops.subirFotos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import migrarprops.models.PredioClave;
import migrarprops.services.OmegaConfigs;
import migrarprops.services.OmegaUploader;

/**
 *
 * @author Angel Navarro
 */
public class MigrarFotos {

	private OmegaConfigs config;
	private List<File> tempFiles;
	private List<PredioClave> withoutpredio;
	private int count = 0;

	public void upload(String directorio) {
		System.out.println("Directorio: " + directorio);
		this.tempFiles = new ArrayList<>();
		this.withoutpredio = new ArrayList<>();
		List<File> fotos = this.getDirectorioFotos(directorio);
		List<List<Object>> paramt = new ArrayList<>();
		if ((this.config == null) || (!this.config.validarDatosConn() && !this.config.validarDatosConnTemp())) {
			return;
		}
		OmegaUploader omega = new OmegaUploader(this.config);
		for (File foto : fotos) {
			try {
				InputStream stream = new FileInputStream(foto);
				String nombreFoto = foto.getName();
				String claveCatPredio = null;
				// Verificacamos si la fotos ya fue procesada Anteriormente
				if (nombreFoto.startsWith("P-")) {

					claveCatPredio = nombreFoto.substring(2, 26);
					nombreFoto = nombreFoto.substring(2, nombreFoto.length());
				} else {
					claveCatPredio = nombreFoto.substring(0, nombreFoto.length() - 4);
					if (this.config.getDbType().equals("ORACLE")) {
						String claveCatTemp = "240250";
						String claveSeparate[] = claveCatPredio.split("-");
						for (int i = 0; i < claveSeparate.length; i++) {
							claveCatTemp = claveCatTemp + claveSeparate[i];
						}
						claveCatPredio = claveCatTemp;
					}
				}
				PredioClave datos = null;
				if (claveCatPredio != null) {
					if (claveCatPredio.length() > 24) {
						String[] split = null;
						if (claveCatPredio.contains("-")) {
							split = claveCatPredio.split("-");
							claveCatPredio = split[0];
						} else if (claveCatPredio.contains("_")) {
							split = claveCatPredio.split("_");
							claveCatPredio = split[0];
						} else if (claveCatPredio.contains("(")) {
							split = claveCatPredio.split("\\(");
							claveCatPredio = split[0];
						}
					}
					switch (claveCatPredio.length()) {
					case 24: // Busca en la tabla cat_predio
						datos = omega.findIdPredio(claveCatPredio);
						break;
					case 18:// en caso que falte una digito en la clave y se tenga que completar la clave Y
							// sea postgres
						datos = omega.findIdPredioCompletar(claveCatPredio, (claveCatPredio.length() - 1),
								Boolean.TRUE);
						break;
					default: //
						if (this.config.getDbType().equals("ORACLE")) {
							datos = omega.findIdPredio(claveCatPredio);
						} else {
							datos = omega.findIdPredioCompletar(claveCatPredio, null, Boolean.FALSE);
						}
						break;
					}
				}
				if (!omega.existeFoto(nombreFoto, null)) {
					if (datos != null) {
						Long idfoto = null;
						datos.setNombreFoto(nombreFoto);
						System.out.println("datos: " + datos);
						if (datos.getId() == null) {
							datos.setObs("Fotos posiblemente mal codificada.");
							this.withoutpredio.add(datos);
						} else {
							idfoto = omega.uploadFile(stream, nombreFoto, "image/jpeg");
						}

						if (idfoto != null) {
							// descomentar si necesita subir fotos e ir registrando al mismo tiempo que se
							// sube a la base de fotos
							// este hace que se demore mas tiempo la subida
							// omega.agregarFotoPredio(idfoto, "image/jpeg", nombreFoto, datos.getId(),
							// claveCatPredio, Boolean.TRUE, datos.getCodPredio());
							// Optimiza la subida DE LAS FOTO COMENTAR SE LA LINEA DE ARRIBA ESTA
							// DESCOMENTADA.

							if ((idfoto != null) && (datos.getId() != null)) {

								paramt.add(this.addDatosFotos(idfoto, "image/jpeg", nombreFoto, datos.getId(),
										claveCatPredio, Boolean.TRUE, datos.getCodPredio()));
								this.count++;
								try {
									if (!nombreFoto.startsWith("P-")) {
										File f = new File(foto.getAbsolutePath());
										boolean renameTo = f.renameTo(new File(foto.getParent() + "/P-" + nombreFoto));
									}
								} catch (Exception ex) {
									Logger.getLogger(MigrarFotos.class.getName()).log(Level.SEVERE, null, ex);
								}
							}
						}
						stream.close();
					}
				} else {
					System.out.println("Existe foto: " + nombreFoto + " Predio: " + datos.getId());
					datos.setObs("Fotos ya fue subida con anterioridad.");
					datos.setNombreFoto(nombreFoto);
					this.withoutpredio.add(datos);
				}
			} catch (FileNotFoundException ex) {
				Logger.getLogger(MigrarFotos.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(MigrarFotos.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		if (paramt.size() > 0) {
			omega.insertIntoBacth(paramt);
		}
		System.out.println("Total Archivos (" + fotos.size() + "), Upload (" + this.count + ")");

	}

	private List<File> getDirectorioFotos(String ruta) {
		File directorio = new File(ruta);

		File[] ficheros = directorio.listFiles();
		for (File fichero : ficheros) {
			if (fichero.isDirectory()) {
				this.getDirectorioFotos(fichero.getPath());
			} else {
				if (fichero.getName().toUpperCase().endsWith(".jpg".toUpperCase())
						|| fichero.getName().toUpperCase().endsWith(".png".toUpperCase())) {

					this.tempFiles.add(fichero);
				}
			}
		}

		return this.tempFiles;
	}

	public OmegaConfigs getConfig() {
		return this.config;
	}

	public void setConfig(OmegaConfigs config) {
		this.config = config;
	}

	private List<Object> addDatosFotos(Long oid, String contentType, String nombre, Long predio, String descripcion,
			Boolean estado, Long codPredio) {
		try {
			List<Object> list = new ArrayList<>();
			list.add(contentType);
			list.add(oid);
			list.add(nombre);
			list.add((predio == null ? -1L : predio));
			list.add(descripcion);
			list.add(estado);
			list.add((codPredio == null ? -1L : codPredio));
			return list;
		} catch (Exception e) {
			Logger.getLogger(MigrarFotos.class.getName()).log(Level.SEVERE, null, e);
		}
		return null;
	}

	public List<PredioClave> getWithoutpredio() {
		return this.withoutpredio;
	}

	public void setWithoutpredio(List<PredioClave> withoutpredio) {
		this.withoutpredio = withoutpredio;
	}

	public List<File> getTempFiles() {
		return this.tempFiles;
	}

	public void setTempFiles(List<File> tempFiles) {
		this.tempFiles = tempFiles;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
