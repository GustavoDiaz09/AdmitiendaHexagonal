package com.admitienda.dominio.puerto.salida;

import com.admitienda.dominio.modelo.Credenciales;

/**
 * Puerto de salida: cifrado de contraseñas. El dominio dicta QUÉ necesita
 * (cifrar y verificar) y el adaptador decide CÓMO (PBKDF2), de modo que
 * el algoritmo puede cambiarse sin tocar el núcleo.
 */
public interface CifradorDeContrasenas {

    /** Deriva sal aleatoria y hash de la contraseña en texto claro. */
    Credenciales cifrar(String contrasena);

    /** Verifica una contraseña contra las credenciales almacenadas. */
    boolean verificar(String contrasena, Credenciales almacenadas);
}
