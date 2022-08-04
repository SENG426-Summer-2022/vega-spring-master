package com.uvic.venus.controller;

import com.uvic.venus.model.UserInfo;
import com.uvic.venus.model.UserInfoWithRole;
import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserInfoDAO userInfoDAO;

    @Autowired
    DataSource dataSource;

    @Autowired
    StorageService storageService;

    @GetMapping(value = "/fetchusers")
    public ResponseEntity<List<UserInfoWithRole>> fetchAllUsers(){
        List<UserInfo> userInfoList = userInfoDAO.findAll();
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        List<UserInfoWithRole> userWithRoleList = userInfoList.stream()
            .map(u -> new UserInfoWithRole(u.getUsername(),
                                           u.getFirstName(),
                                           u.getLastName(),

                                           // It may be better to make this an array if we add more roles
                                           loadUserByUsername(manager, u.getUsername()).getAuthorities()
                                           .contains(new SimpleGrantedAuthority("ROLE_STAFF"))
                                           ? new SimpleGrantedAuthority("ROLE_STAFF")
                                           : new SimpleGrantedAuthority("ROLE_USER"),
                                           loadUserByUsername(manager, u.getUsername()).isEnabled()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userWithRoleList);
    }

    public void updateUser(JdbcUserDetailsManager manager, User.UserBuilder builder) {
        manager.updateUser(builder.build());
    }

    public void createUser(JdbcUserDetailsManager manager, User.UserBuilder builder) {
        manager.createUser(builder.build());
    }

    public UserDetails loadUserByUsername(JdbcUserDetailsManager manager, String username) {
        return manager.loadUserByUsername(username);
    }

    public UserInfo getUserFromUserInfo(String username){
        return userInfoDAO.getById(username);
    }

    public void savetoUserInfoDB(UserInfo userInfo) {
        userInfoDAO.save(userInfo);
    }

    public Boolean userExistsCheck(JdbcUserDetailsManager manager, String username){
        return manager.userExists(username);
    }

    public void deleteFromUserInfoDB(UserInfo userToDelete){
        userInfoDAO.delete(userToDelete);
    }

    public void deleteFromUsersDB(JdbcUserDetailsManager manager, String username){
        manager.deleteUser(username);
    }
    
    @GetMapping(value ="/enableuser")
    public ResponseEntity<String> enableUserAccount(@RequestParam String username, @RequestParam boolean enable){

        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        UserDetails userDetails = loadUserByUsername(manager, username);

        User.UserBuilder builder = User.builder();
        builder.username(userDetails.getUsername());
        builder.password(userDetails.getPassword());
        builder.authorities(userDetails.getAuthorities());
        builder.disabled(!enable);

        updateUser(manager, builder);
        return ResponseEntity.ok("User Updated Successfully");
    }

    @GetMapping(value ="/changerole")
    public ResponseEntity<String> changeRole(@RequestParam String username, @RequestParam String role){
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));

        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        UserDetails userDetails = loadUserByUsername(manager, username);

        User.UserBuilder builder = User.builder();
        builder.username(userDetails.getUsername());
        builder.password(userDetails.getPassword());
        builder.authorities(authorities);
        builder.disabled(userDetails.isEnabled());

        updateUser(manager, builder);

        Object[] userAuthorities = userDetails.getAuthorities().toArray();
        GrantedAuthority oldAuthority =  (GrantedAuthority) userAuthorities[0];
        String oldRole = oldAuthority.getAuthority();
        return ResponseEntity.ok(String.format("User Role Updated from %s to %s", oldRole, role));
    }

    // A GET request access-point to delete a user
    @GetMapping(value="/deleteuser")
    public ResponseEntity<?> deleteUser(@RequestParam String username){

        // Open a JDBC manager to connect to Users DB.
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        // A sanity check before deletion
        if(userExistsCheck(manager,username)){
            // Get the user to delete
            UserInfo userToDelete = getUserFromUserInfo(username);

            // Deleting user from UserInfo DB
            deleteFromUserInfoDB(userToDelete);

            // Deleting user from Users DB
            deleteFromUsersDB(manager, username);

            return ResponseEntity.ok(username + " deleted successfully!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Found");
    }

    // A GET request access point to change email(username) of a user
    @GetMapping(value="/changeemail")
    public ResponseEntity<?> changeEmail(@RequestParam String username, @RequestParam String newEmail){

        UserInfo userToUpdate = getUserFromUserInfo(username);
        userToUpdate.setUsername(username);
        savetoUserInfoDB(userToUpdate);

        return ResponseEntity.ok("");
    }

    // A GET request access point to change User's First and/or Last names.
    @GetMapping(value="/changeusername")
    public ResponseEntity<?> changeUserName(@RequestParam String username, @RequestParam String newuserFirstname, @RequestParam String newuserLastname){

        // Get User by username key
        UserInfo userToUpdate = getUserFromUserInfo(username);

        //Update to new First and Last names
        userToUpdate.setFirstName(newuserFirstname);
        userToUpdate.setLastName(newuserLastname);

        // Save the changes to DB
        savetoUserInfoDB(userToUpdate);

        return ResponseEntity.ok("");
    }

    // A request access point to update email(username), First name and last name of a user
    @GetMapping(value="/updateuser")
    public ResponseEntity<?> updateUser(@RequestParam String username, @RequestParam String newusername, @RequestParam String newFirstname, @RequestParam String newLastname ) {

        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        // Get User from userInfo DB by username key
        UserInfo userToDelete = getUserFromUserInfo(username);

        // Get User from Users DB by old username key
        UserDetails userDetails = loadUserByUsername(manager, username);

        // Get old user's password and authorities before deleting
        User.UserBuilder builder = User.builder();
        builder.username(newusername);
        builder.password(userDetails.getPassword());
        builder.authorities(userDetails.getAuthorities());

        //Create a replacement User to update to
        UserInfo replacementUser = new UserInfo(newusername, newFirstname, newLastname);

        // Delete old user from UserInfoDB  (delete user from userinfo first)
        deleteFromUserInfoDB(userToDelete);

        //Delete old user from UsersDB
        deleteFromUsersDB(manager, username);

        //Save changes to Users DB
        createUser(manager, builder);

        // Save the changes to UserInfo DB
        savetoUserInfoDB(replacementUser);

        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/handlefileupload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file){
        storageService.store(file);
        return ResponseEntity.ok("File uploaded Successfully");
    }

}
