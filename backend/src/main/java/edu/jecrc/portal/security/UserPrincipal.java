package edu.jecrc.portal.security;

import edu.jecrc.portal.modules.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final UUID securityStamp;
    private final boolean active;
    private final boolean suspended;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Set<String> roles;
    private final Set<String> permissions;

    public static UserPrincipal create(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> roleNames = new HashSet<>();
        Set<String> permissionNames = new HashSet<>();

        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> {
                roleNames.add(role.getRoleName());
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(perm -> {
                        permissionNames.add(perm.getPermissionName());
                        authorities.add(new SimpleGrantedAuthority(perm.getPermissionName()));
                    });
                }
            });
        }

        return UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPasswordHash())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .securityStamp(user.getSecurityStamp())
                .active(Boolean.TRUE.equals(user.getIsActive()))
                .suspended(Boolean.TRUE.equals(user.getIsSuspended()))
                .authorities(authorities)
                .roles(roleNames)
                .permissions(permissionNames)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !suspended;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active && !suspended;
    }
}
