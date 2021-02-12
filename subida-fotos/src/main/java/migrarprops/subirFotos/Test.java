/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migrarprops.subirFotos;

/**
 *
 * @author Angel Navarro
 */
public class Test {

	public static void main(String[] args) {
		String clave = "080201010101001";
		StringBuilder builder = new StringBuilder("080201010101001");
		System.out.println("Clave inicial: " + builder);
		builder.insert(10, "0");
		System.out.println(24 - (clave.length()) + "Clave inicial: " + builder);
		for (int i = 0; i < (24 - clave.length() - 1); i++) {
			builder.append("0");
		}
		System.out.println("Clave...: " + builder);
	}
}
