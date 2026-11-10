package com.admitienda.infraestructura.seguridad;

import com.admitienda.dominio.modelo.Credenciales;
import com.admitienda.dominio.puerto.salida.CifradorDeContrasenas;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

/**
 * Adaptador de salida: cifrado de contraseñas con PBKDF2WithHmacSHA256
 * (210.000 iteraciones OWASP, sal de 16 bytes y hash de 32 bytes). El
 * dominio solo conoce el contrato; el algoritmo vive aquí y puede
 * reemplazarse sin tocar la lógica de negocio.
 */
public final class AdaptadorCifradorPbkdf2 implements CifradorDeContrasenas {

    private static final int ITERACIONES = 210_000;
    private static final int BYTES_DE_SAL = 16;
    private static final int BYTES_DE_HASH = 32;
    private static final String FORMATO = "pbkdf2";

    private final SecureRandom azar = new SecureRandom();

    @Override
    public Credenciales cifrar(String contrasena) {
        byte[] sal = new byte[BYTES_DE_SAL];
        azar.nextBytes(sal);
        byte[] hash = derivar(contrasena, sal, ITERACIONES);
        return new Credenciales(HexFormat.of().formatHex(sal), formatoDe(hash));
    }

    @Override
    public boolean verificar(String contrasena, Credenciales almacenadas) {
        try {
            String[] partes = almacenadas.hash().split("\\$");
            if (partes.length != 4 || !FORMATO.equals(partes[0])) {
                return false;
            }
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = HexFormat.of().parseHex(partes[2]);
            byte[] esperado = HexFormat.of().parseHex(partes[3]);
            byte[] calculado = derivar(contrasena, sal, iteraciones);
            return MessageDigest.isEqual(esperado, calculado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private byte[] derivar(String contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec especificacion = new PBEKeySpec(contrasena.toCharArray(), sal,
                iteraciones, BYTES_DE_HASH * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("No se pudo derivar la contraseña.", e);
        } finally {
            especificacion.clearPassword();
        }
    }

    private String formatoDe(byte[] hash) {
        return FORMATO + "$" + ITERACIONES + "$" + HexFormat.of().formatHex(hash);
    }
}
