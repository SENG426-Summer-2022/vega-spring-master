package com.uvic.venus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.uvic.venus.controller.LoginController;
import com.uvic.venus.controller.PasswordController;
import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

@SpringBootTest
public class PasswordTests {
    @InjectMocks
    PasswordController passController;

    @InjectMocks
    LoginController loginController;

    @Mock
    UserInfoDAO userInfoDAO;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    PasswordEncoder passwordEncoder;


    @Mock
    DataSource dataSource;

    @Mock
    StorageService storageService;

    @Test
    void updatePasswordUnauthorized() throws Exception {
        passController = spy(new PasswordController());
        MockitoAnnotations.openMocks(this);

        assertThat(passController.updatePassword("hello").getBody())
            .isEqualTo("User Not Found");
    }

    @Test
    void updatePasswordAuthorized() throws Exception {
        passController = spy(new PasswordController());
        MockitoAnnotations.openMocks(this);

        doReturn(null).when(authenticationManager)
            .authenticate(any(UsernamePasswordAuthenticationToken.class));
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                authorities);

        doReturn(userDetails).when(passController)
            .loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(passController)
            .updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        doReturn("password").when(passwordEncoder).encode(anyString());

        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        doReturn("username").when(auth).getName();
        assertThat(passController.updatePassword("password").getBody()).isEqualTo("username");
    }
}
