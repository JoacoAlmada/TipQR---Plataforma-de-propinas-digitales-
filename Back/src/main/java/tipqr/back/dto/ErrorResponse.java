package tipqr.back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private LocalDateTime timestamp;

    public static ErrorResponse of(int status, String mensaje) {
        return ErrorResponse.builder()
                .status(status)
                .error(mensaje)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
