package practice.javal1.dto.response.Auth;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token; // Access Token (15 phút)
    String refreshToken; // Refresh Token (7 ngày)
    boolean authenticated;
}