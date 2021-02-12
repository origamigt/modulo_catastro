/**
 *
 */
package migrarprops.models;

import java.io.Serializable;

/**
 * @author SUPERGOLD
 *
 */
public class PredioClave implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 537733554289544533L;
	private Long id;
	private String claveCat;
	private Long codPredio;
	private String nombreFoto;
	private String obs;

	/**
	 * @param id
	 * @param claveCat
	 * @param codPredio
	 */
	public PredioClave(String claveCat, Long id, Long codPredio) {
		super();
		this.id = id;
		this.claveCat = claveCat;
		this.codPredio = codPredio;
	}

	/**
	 * @param id
	 * @param claveCat
	 * @param codPredio
	 * @param nombreFoto
	 */
	public PredioClave(Long id, String claveCat, Long codPredio, String nombreFoto) {
		super();
		this.id = id;
		this.claveCat = claveCat;
		this.codPredio = codPredio;
		this.nombreFoto = nombreFoto;
	}

	/**
	 * @param id
	 * @param claveCat
	 * @param codPredio
	 * @param nombreFoto
	 * @param obs
	 */
	public PredioClave(Long id, String claveCat, Long codPredio, String nombreFoto, String obs) {
		super();
		this.id = id;
		this.claveCat = claveCat;
		this.codPredio = codPredio;
		this.nombreFoto = nombreFoto;
		this.obs = obs;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the claveCat
	 */
	public String getClaveCat() {
		return this.claveCat;
	}

	/**
	 * @param claveCat
	 *            the claveCat to set
	 */
	public void setClaveCat(String claveCat) {
		this.claveCat = claveCat;
	}

	/**
	 * @return the codPredio
	 */
	public Long getCodPredio() {
		return this.codPredio;
	}

	/**
	 * @param codPredio
	 *            the codPredio to set
	 */
	public void setCodPredio(Long codPredio) {
		this.codPredio = codPredio;
	}

	/**
	 * @return the nombreFoto
	 */
	public String getNombreFoto() {
		return this.nombreFoto;
	}

	/**
	 * @param nombreFoto
	 *            the nombreFoto to set
	 */
	public void setNombreFoto(String nombreFoto) {
		this.nombreFoto = nombreFoto;
	}

	/**
	 * @return the obs
	 */
	public String getObs() {
		return this.obs;
	}

	/**
	 * @param obs
	 *            the obs to set
	 */
	public void setObs(String obs) {
		this.obs = obs;
	}

	@Override
	public String toString() {
		return "ClavePredio( id [" + this.id + "], numPredio [" + this.codPredio + "], ClaveCatastral [" + this.claveCat
				+ "], [" + this.nombreFoto + "] )";
	}

}
