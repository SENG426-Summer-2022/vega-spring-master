package com.uvic.venus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import com.uvic.venus.auth.JwtUtil;
import com.uvic.venus.controller.LoginController;
import com.uvic.venus.model.AuthenticationRequest;
import com.uvic.venus.model.AuthenticationResponse;
import com.uvic.venus.model.RegisterUserInfo;
import com.uvic.venus.repository.UserInfoDAO;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class LoginTests {
    @InjectMocks
    LoginController loginController;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    DataSource dataSource;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserInfoDAO userInfoDAO;

    @Test
    void register_valid() throws Exception {
        loginController = spy(new LoginController());
        MockitoAnnotations.openMocks(this);

        doNothing().when(loginController).createUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        doReturn("password").when(passwordEncoder).encode(anyString());

        RegisterUserInfo user = new RegisterUserInfo("test", "test", "user", "password");
        ResponseEntity<?> responseEntity = loginController.registerUser(user);
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void register_invalid_noUsername() throws Exception {
        loginController = spy(new LoginController());
        MockitoAnnotations.openMocks(this);

        doNothing().when(loginController).createUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        doReturn("password").when(passwordEncoder).encode(anyString());

        RegisterUserInfo user = new RegisterUserInfo(null, "test", "user", "password");
        Exception thrown = assertThrows(Exception.class, () -> {
            loginController.registerUser(user);
        });
 
        assertThat(thrown.getMessage()).isEqualTo("username cannot be null");
    }

    @Test
    void register_invalid_noPassword() throws Exception {
        loginController = spy(new LoginController());
        MockitoAnnotations.openMocks(this);

        doNothing().when(loginController).createUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        doReturn(null).when(passwordEncoder).encode(anyString());

        RegisterUserInfo user = new RegisterUserInfo("test", "test", "user", null);
        Exception thrown = assertThrows(Exception.class, () -> {
            loginController.registerUser(user);
        });
 
        assertThat(thrown.getMessage()).isEqualTo("password cannot be null");
    }

    @Test
    void login_valid() throws Exception {
        loginController = spy(new LoginController());
        MockitoAnnotations.openMocks(this);

        doReturn(null).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);
        String token = "thisisatoken";

        doReturn(userDetails).when(loginController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doReturn(token).when(jwtUtil).generateToken(any(UserDetails.class));

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "password");
        ResponseEntity<?> responseEntity = loginController.createAuthenticationToken(authenticationRequest);
        AuthenticationResponse body = (AuthenticationResponse) responseEntity.getBody();

        assertThat(body.getJwt()).isEqualTo(token);
        assertThat(authorities.get(0).equals(body.getAuthorities()[0])).isTrue();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void login_invalid_badPassword() throws Exception {
        loginController = spy(new LoginController());
        MockitoAnnotations.openMocks(this);

        doThrow(new BadCredentialsException("Bad Credentials")).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("username", "badpassword");
        ResponseEntity<?> responseEntity = loginController.createAuthenticationToken(authenticationRequest);

        assertThat(responseEntity.getBody()).isEqualTo("User Not Found");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
