/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops.models;

/**
 *
 * @author Angel Navarro
 */
public class CodPredial {

	private Short provincia;
	private Short canton;
	private Short parroquia;
	private Short zona;
	private Short sector;
	private Short manzana;
	private Short solar;
	private Short bloque = Short.valueOf("0");
	private Short piso = Short.valueOf("0");
	private Short unidad = Short.valueOf("0");

	private Short numDigitosClave = Short.valueOf("24");
	private String charAppend = "0";
	private String clavecat;

	public CodPredial() {
	}

	/**
	 * Permite agregar caracteres en la posicion indicada
	 *
	 * @param clavecat
	 *            Cadena a agregar el caracter
	 * @param postAddChar
	 *            Posicion donde se agregara el caracter
	 * @param completarCadena
	 *            Parametro indica si se agregar un caracter o no
	 */
	public CodPredial(String clavecat, Integer postAddChar, Boolean completarCadena) {
		if (clavecat.length() < 24) {
			clavecat = this.claveAddChar(clavecat, charAppend, postAddChar, completarCadena);
		}
		this.clavecat = clavecat;
	}

	public String getClavecat() {
		return clavecat;
	}

	public void setClavecat(String clavecat) {
		this.clavecat = clavecat;
	}

	public Short getProvincia() {
		return provincia;
	}

	public void setProvincia(Short provincia) {
		this.provincia = provincia;
	}

	public Short getCanton() {
		return canton;
	}

	public void setCanton(Short canton) {
		this.canton = canton;
	}

	public Short getParroquia() {
		return parroquia;
	}

	public void setParroquia(Short parroquia) {
		this.parroquia = parroquia;
	}

	public Short getZona() {
		return zona;
	}

	public void setZona(Short zona) {
		this.zona = zona;
	}

	public Short getSector() {
		return sector;
	}

	public void setSector(Short sector) {
		this.sector = sector;
	}

	public Short getManzana() {
		return manzana;
	}

	public void setManzana(Short manzana) {
		this.manzana = manzana;
	}

	public Short getSolar() {
		return solar;
	}

	public void setSolar(Short solar) {
		this.solar = solar;
	}

	public Short getBloque() {
		return bloque;
	}

	public void setBloque(Short bloque) {
		this.bloque = bloque;
	}

	public Short getPiso() {
		return piso;
	}

	public void setPiso(Short piso) {
		this.piso = piso;
	}

	public Short getUnidad() {
		return unidad;
	}

	public void setUnidad(Short unidad) {
		this.unidad = unidad;
	}

	public Short getNumDigitosClave() {
		return numDigitosClave;
	}

	public void setNumDigitosClave(Short numDigitosClave) {
		this.numDigitosClave = numDigitosClave;
	}

	public String getCharAppend() {
		return charAppend;
	}

	public void setCharAppend(String charAppend) {
		this.charAppend = charAppend;
	}

	private String claveAddChar(String clave, String addChar, Integer pos, Boolean completarClave) {
		StringBuilder builder = new StringBuilder(clave);
		if (addChar != null && pos != null) {
			builder.insert(pos, addChar);
		}
		if (completarClave) {
			for (int i = 0; i < (numDigitosClave - clave.length() - 1); i++) {
				builder.append(charAppend);
			}
		}
		return builder.toString();
	}

}
