package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String email;
    private String rol;
    private String nombre;
    private String apellido;
}
