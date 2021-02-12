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
public class ClavePredio {

	/**
	 *
	 */
	// private static final long serialVersionUID = 1L;
	private String claveCat;
	private Long id;
	private Long codPredio;
	private String nombreFoto;
	private String obs;

	public ClavePredio(String claveCat, Long id, Long codPredio) {
		this.claveCat = claveCat;
		this.id = id;
		this.codPredio = codPredio;
	}

	public ClavePredio(String claveCat, Long id, Long codPredio, String nombreFoto) {
		this.claveCat = claveCat;
		this.id = id;
		this.codPredio = codPredio;
		this.nombreFoto = nombreFoto;
	}

	public String getClaveCat() {
		return this.claveCat;
	}

	public void setClaveCat(String claveCat) {
		this.claveCat = claveCat;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCodPredio() {
		return this.codPredio;
	}

	public void setCodPredio(Long codPredio) {
		this.codPredio = codPredio;
	}

	public String getNombreFoto() {
		return this.nombreFoto;
	}

	public void setNombreFoto(String nombreFoto) {
		this.nombreFoto = nombreFoto;
	}

	public String getObs() {
		return this.obs;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}

	@Override
	public String toString() {
		return "ClavePredio( id [" + this.id + "], numPredio [" + this.codPredio + "], ClaveCatastral [" + this.claveCat
				+ "], [" + this.nombreFoto + "] )";
	}

}
