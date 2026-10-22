package edu.jecrc.portal.modules.auth.dto;

import edu.jecrc.portal.modules.user.dto.UserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private long expiresInMs;
    private UserProfileResponse user;
}
