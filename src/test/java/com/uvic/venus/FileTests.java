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
import org.junit.runner.RunWith;
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
import com.uvic.venus.controller.FileController;
import com.uvic.venus.model.AuthenticationRequest;
import com.uvic.venus.model.AuthenticationResponse;
import com.uvic.venus.model.RegisterUserInfo;
import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class FileTests {

    @InjectMocks
    FileController fileController;

    @Mock
    StorageService storageService;

    @Test
    public void list_valid() throws Exception {
    }
    
}
