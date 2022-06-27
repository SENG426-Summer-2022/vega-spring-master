package com.uvic.venus.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserInfoWithRole extends UserInfo {
    private GrantedAuthority role;
    private Boolean enabled;

    public UserInfoWithRole(String username, String firstName, String lastName, GrantedAuthority role, Boolean enabled) {
        super(username, firstName, lastName);
        this.role = role;
        this.enabled = enabled;
    }

    public UserInfoWithRole(String username, String firstName, String lastName, String role, Boolean enabled) {
        super(username, firstName, lastName);
        this.role = new SimpleGrantedAuthority(role);
        this.enabled = enabled;
    }

    public GrantedAuthority getRole() {
        return role;
    }

    public void setRole(GrantedAuthority role) {
        this.role = role;
    }
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }


    @Override
    public String toString() {
        return "UserInfoWithRole{" +
            "username='" + this.getUsername() + '\'' +
            ", firstname='" + this.getFirstName() + '\'' +
            ", lastname='" + this.getLastName() + '\'' +
            ", role=" + role +
            "}";
    }

}
