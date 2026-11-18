package ni.edu.uam.agenda.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashClavesTest {
    @Test
    void nuncaGuardaLaClavePlano() {
        String almacenado = HashClaves.crear("MiClaveSegura123");
        assertFalse(almacenado.contains("MiClaveSegura123"));
        assertTrue(HashClaves.coincide("MiClaveSegura123", almacenado));
        assertFalse(HashClaves.coincide("claveIncorrecta", almacenado));
    }
    @Test
    void salAleatoriaProduceHashesDistintos() {
        assertNotEquals(HashClaves.crear("mismoValor123"), HashClaves.crear("mismoValor123"));
    }
}
