package ni.edu.uam.agenda.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Ejemplo académico: contraseñas en PBKDF2+sal, nunca en texto plano en PostgreSQL. */
public final class HashClaves {
    private static final int ITERACIONES = 210_000;
    private static final int TAMANO_BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private HashClaves() { }

    public static String crear(String clave) {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        byte[] hash = calcular(clave, sal, ITERACIONES);
        return "pbkdf2$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(sal) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean coincide(String clave, String cadenaAlmacenada) {
        try {
            String[] partes = cadenaAlmacenada.split("\\$");
            if (partes.length != 4 || !"pbkdf2".equals(partes[0])) return false;
            int rondas = Integer.parseInt(partes[1]);
            if (rondas < 1 || rondas > 1_000_000) return false;
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            return MessageDigest.isEqual(esperado, calcular(clave, sal, rondas));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] calcular(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec datos = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, TAMANO_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(datos).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo procesar la contraseña", e);
        } finally {
            datos.clearPassword();
        }
    }
}
