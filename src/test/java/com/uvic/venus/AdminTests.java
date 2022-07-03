package com.uvic.venus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import com.uvic.venus.controller.AdminController;
import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class AdminTests {
    @InjectMocks
    AdminController adminController;

    @Mock
    UserInfoDAO userInfoDAO;

    @Mock
    DataSource dataSource;

    @Mock
    StorageService storageService;

    @Test
    void enableUser() throws Exception {
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            false,
                                            true,
                                            true,
                                            true,
                                            authorities);
        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.enableUserAccount("username", true);
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo("User Updated Successfully");
    }

    @Test
    void disableUser() throws Exception {
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);
        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.enableUserAccount("username", false);
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo("User Updated Successfully");
    }

    @Test
    void changeRoleFromStaffToUser() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);

        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.changeRole("username", "ROLE_USER");
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("User Role Updated from ROLE_STAFF to ROLE_USER");
    }

    @Test
    void changeRoleFromStaffToAdmin() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);

        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.changeRole("username", "ROLE_ADMIN");
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("User Role Updated from ROLE_STAFF to ROLE_ADMIN");
    }

    @Test
    void changeRoleFromUserToStaff() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);

        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.changeRole("username", "ROLE_STAFF");
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("User Role Updated from ROLE_USER to ROLE_STAFF");
    }

    @Test
    void changeRoleFromUserToAdmin() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);

        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.changeRole("username", "ROLE_ADMIN");
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("User Role Updated from ROLE_USER to ROLE_ADMIN");
    }

    @Test
    void changeRoleFromAdminToUser() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);

        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.changeRole("username", "ROLE_USER");
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("User Role Updated from ROLE_ADMIN to ROLE_USER");
    }

    @Test
    void changeRoleFromAdminToStaff() throws Exception {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        UserDetails userDetails = new User( "username",
                                            "password",
                                            true,
                                            true,
                                            true,
                                            true,
                                            authorities);

        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        ResponseEntity<?> responseEntity = adminController.changeRole("username", "ROLE_STAFF");
        
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("User Role Updated from ROLE_ADMIN to ROLE_STAFF");
    }
}
