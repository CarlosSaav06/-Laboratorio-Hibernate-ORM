package ni.edu.uam.agenda.service;

import ni.edu.uam.agenda.dto.DatosUsuario;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidacionUsuarioTest {
    @Test
    void rechazaClaveCortaAlCrear() {
        var datos = new DatosUsuario("Ana", "Lopez", "ana.lopez", "ana@mail.com", "123", "");
        assertThrows(IllegalArgumentException.class, () -> ValidacionUsuario.verificar(datos, true));
    }
    @Test
    void permiteNoCambiarClaveAlEditar() {
        var datos = new DatosUsuario("Ana", "Lopez", "ana.lopez", "ana@mail.com", "", "");
        assertDoesNotThrow(() -> ValidacionUsuario.verificar(datos, false));
    }
}
