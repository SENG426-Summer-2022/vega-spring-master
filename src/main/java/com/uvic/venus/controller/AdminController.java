package com.uvic.venus.controller;

import com.uvic.venus.model.UserInfo;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserInfoDAO userInfoDAO;

    @Autowired
    DataSource dataSource;

    @Autowired
    StorageService storageService;

    @RequestMapping(value = "/fetchusers", method = RequestMethod.GET)
    public ResponseEntity<?> fetchAllUsers(){
        List<UserInfo> userInfoList = userInfoDAO.findAll();
        return ResponseEntity.ok(userInfoList);
    }

    public void updateUser(JdbcUserDetailsManager manager, User.UserBuilder builder) {
        manager.updateUser(builder.build());
    }

    public UserDetails loadUserByUsername(JdbcUserDetailsManager manager, String username) {
        return manager.loadUserByUsername(username);
    }

    @RequestMapping(value ="/enableuser", method = RequestMethod.GET)
    public ResponseEntity<?> enableUserAccount(@RequestParam String username, @RequestParam boolean enable){
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

    @RequestMapping(value ="/changerole", method = RequestMethod.GET)
    public ResponseEntity<?> changeRole(@RequestParam String username, @RequestParam String role){
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
    @RequestMapping(value="/deleteuser", method = RequestMethod.GET)
    public ResponseEntity<?> deleteUser(@RequestParam String username){

        // Open a JDBC manager to connect to Users DB.
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        // A sanity check before deletion
        if(manager.userExists(username)){
            // Get the user to delete
            Optional<UserInfo> userToDelete = userInfoDAO.findById(username);

            // Deleting user from UserInfo DB
            userInfoDAO.delete(userToDelete.get());

            // Deleting user from Users DB
            manager.deleteUser(username);

            //System.out.println("User Deleted");

            return ResponseEntity.ok(username + " deleted successfully!");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Found");
    }

    // A GET request access point to change email(username) of a user
    @RequestMapping(value="/changeemail", method = RequestMethod.GET)
    public ResponseEntity<?> changeEmail(@RequestParam String username, @RequestParam String newEmail){
        //TODO: Test this to see if username gets updated in users DB too.
        UserInfo userToUpdate = userInfoDAO.getById(username);
        userToUpdate.setUsername(username);
        userInfoDAO.save(userToUpdate);

        return ResponseEntity.ok("");
    }

    // A GET request access point to change User's First and/or Last names.
    @RequestMapping(value="/changeusername", method = RequestMethod.GET)
    public ResponseEntity<?> changeUserName(@RequestParam String username, @RequestParam String newuserFirstname, @RequestParam String newuserLastname){

        // Get User by username key
        UserInfo userToUpdate = userInfoDAO.getById(username);

        //Update to new First and Last names
        userToUpdate.setFirstName(newuserFirstname);
        userToUpdate.setLastName(newuserLastname);

        //Terminal Display the update
        System.out.println(userToUpdate);

        // Save the changes to DB
        userInfoDAO.save(userToUpdate);

        return ResponseEntity.ok("");
    }

    // A POST request access point to update email(username), First name and last name of a user
    @RequestMapping(value="/updateuser", method = RequestMethod.POST)
    public ResponseEntity<?> updateUser(@RequestParam String username, @RequestParam String newusername, @RequestParam String newFirstname, @RequestParam String newLastname ) {

        //For debug purposes
        System.out.println(username);
        System.out.println(newusername);
        System.out.println(newFirstname);
        System.out.println(newLastname);

        // Get User from Users DB by old username key
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        UserDetails userDetails = loadUserByUsername(manager, username);

        // Update user's username(email)
        User.UserBuilder builder = User.builder();
        builder.username(newusername);
        builder.password(userDetails.getPassword());
        builder.authorities(userDetails.getAuthorities());

        // Save to Users DB
        updateUser(manager, builder);

        // Get User from userInfo DB by username key
        UserInfo userToUpdate = userInfoDAO.getById(username);

        // Update to new First and Last names
        userToUpdate.setFirstName(newFirstname);
        userToUpdate.setLastName(newLastname);

        // Terminal Display the update
        System.out.println(userToUpdate);

        // Save the changes to DB
        userInfoDAO.save(userToUpdate);

        return ResponseEntity.ok("");
    }

    @PostMapping(value = "/handlefileupload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file){
        storageService.store(file);
        return ResponseEntity.ok("File uploaded Successfully");
    }

}
