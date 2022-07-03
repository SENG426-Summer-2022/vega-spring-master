package com.uvic.venus.controller;

import com.uvic.venus.model.UserInfo;
import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping(value = "/handlefileupload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file){
        storageService.store(file);
        return ResponseEntity.ok("File uploaded Successfully");
    }

}
