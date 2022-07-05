package com.uvic.venus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import com.uvic.venus.model.UserInfo;
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
import com.uvic.venus.model.UserInfoWithRole;
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
    void fetchAllUsersSingleUser() throws Exception {
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        UserInfoWithRole userInfo = new UserInfoWithRole("username",
                                                         "fname",
                                                         "lname",
                                                         "ROLE_USER",
                                                         true);
        ArrayList<UserInfoWithRole> expectedinfo = new ArrayList<UserInfoWithRole>();
        expectedinfo.add(userInfo);

        // Mock the entry in the database
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(userInfo.getRole());
        UserDetails userDetails = new User(userInfo.getUsername(), "password", userInfo.getEnabled(),
                                           true, true, true, authorities);
        doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());


        // Mock for loading the user for lookup
        List<UserInfo> userlist = new ArrayList<UserInfo>();
        userlist.add(new UserInfo(userInfo.getUsername(), userInfo.getFirstName(), userInfo.getLastName()));
        doReturn(userlist).when(userInfoDAO).findAll();

        ResponseEntity<?> responseEntity = adminController.fetchAllUsers();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<UserInfoWithRole> users = (List<UserInfoWithRole>) responseEntity.getBody();
        assertThat(users.get(0).getUsername()).isEqualTo(userInfo.getUsername());
        assertThat(users.get(0).getFirstName()).isEqualTo(userInfo.getFirstName());
        assertThat(users.get(0).getRole()).isEqualTo(userInfo.getRole());
        assertThat(users.get(0).getEnabled()).isEqualTo(userInfo.getEnabled());
    }

    @Test
    void fetchAllUsersMultiUser() throws Exception {
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        ArrayList<UserInfoWithRole> expectedinfo = new ArrayList<UserInfoWithRole>();
        expectedinfo.add(new UserInfoWithRole("username0", "fname0", "lname0", "ROLE_USER", true));
        expectedinfo.add(new UserInfoWithRole("username1", "fname1", "lname1", "ROLE_STAFF", true));

        List<UserInfo> userlist = new ArrayList<UserInfo>();

        for (UserInfoWithRole user : expectedinfo) {
            List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
            authorities.add(user.getRole());
            UserDetails userDetails = new User(user.getUsername(), "password", user.getEnabled(), true, true, true,
                    authorities);
            // Mock the entry in the database
            doReturn(userDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class),
                                                                           eq(user.getUsername()));

            // Mock for loading the user for lookup
            userlist.add(new UserInfo(user.getUsername(), user.getFirstName(), user.getLastName()));
        }

        doReturn(userlist).when(userInfoDAO).findAll();
        ResponseEntity<?> responseEntity = adminController.fetchAllUsers();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<UserInfoWithRole> users = (List<UserInfoWithRole>) responseEntity.getBody();
        for(int i = 0; i < users.size(); i++){
            assertThat(users.get(i).getUsername()).isEqualTo(expectedinfo.get(i).getUsername());
            assertThat(users.get(i).getFirstName()).isEqualTo(expectedinfo.get(i).getFirstName());
            assertThat(users.get(i).getRole()).isEqualTo(expectedinfo.get(i).getRole());
            assertThat(users.get(i).getEnabled()).isEqualTo(expectedinfo.get(i).getEnabled());
        }
    }

    @Test
    void fetchAllUsersNoUser() throws Exception {
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        doReturn(new ArrayList<UserInfo>()).when(userInfoDAO).findAll();

        ResponseEntity<?> responseEntity = adminController.fetchAllUsers();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<UserInfoWithRole>) responseEntity.getBody()).isEmpty();
    }

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

    @Test
    void deleteUser() throws Exception {
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        UserInfo testUser = new UserInfo("username","firstname","lastname");
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserDetails userDetails = new User( "username", "pass",true,true,true,true, authorities);

        doReturn(testUser).when(adminController).getUserFromUserInfo(anyString());
        doReturn(true).when(adminController).userExistsCheck(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).deleteFromUserInfoDB(any(UserInfo.class));
        doNothing().when(adminController).deleteFromUsersDB(any(JdbcUserDetailsManager.class), anyString());

        ResponseEntity<?> responseEntity = adminController.deleteUser("username");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((String) responseEntity.getBody()).isEqualTo("username deleted successfully!");

    }

    @Test
    void changeEmailTest() throws Exception{
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);


        UserInfo testUser = new UserInfo("oldusername", "firstname", "lastname");

       // UserInfoDAO mockedUserInfoDao = mock( UserInfoDAO.class );
        doReturn(testUser).when(adminController).getUserFromUserInfo(anyString());
        doNothing().when(adminController).savetoUserInfoDB(any(UserInfo.class));

        ResponseEntity<?> responseEntity = adminController.changeEmail("oldusername", "newusername");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void changeUserFirstAndLastNameTest() throws Exception{
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);


        UserInfo testUser = new UserInfo("username", "oldfirstname", "oldlastname");

        // UserInfoDAO mockedUserInfoDao = mock( UserInfoDAO.class );
        doReturn(testUser).when(adminController).getUserFromUserInfo(anyString());
        doNothing().when(adminController).savetoUserInfoDB(any(UserInfo.class));

        ResponseEntity<?> responseEntity = adminController.changeUserName("username", "newfirstname", "newlastname");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    void updateUserTest() throws Exception{
        adminController = spy(new AdminController());
        MockitoAnnotations.openMocks(this);

        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        UserInfo testUser = new UserInfo("username", "oldfirstname", "oldlastname");
        UserDetails testUserDetails = new User( "username", "password", true, true, true, true, authorities);;

        doReturn(testUser).when(adminController).getUserFromUserInfo(anyString());
        doNothing().when(adminController).savetoUserInfoDB(any(UserInfo.class));
        doReturn(testUserDetails).when(adminController).loadUserByUsername(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).updateUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));
        doNothing().when(adminController).deleteFromUserInfoDB(any(UserInfo.class));
        doNothing().when(adminController).deleteFromUsersDB(any(JdbcUserDetailsManager.class), anyString());
        doNothing().when(adminController).createUser(any(JdbcUserDetailsManager.class), any(User.UserBuilder.class));

        ResponseEntity<?> responseEntity = adminController.updateUser("username","newUsername", "newfirstname", "newlastname");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    }
}
