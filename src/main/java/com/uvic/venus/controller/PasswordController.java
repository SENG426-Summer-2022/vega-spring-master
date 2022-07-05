package com.uvic.venus.controller;

import javax.sql.DataSource;

import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PasswordController {

    @Autowired
    UserInfoDAO userInfoDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DataSource dataSource;

    @Autowired
    StorageService storageService;

    @PostMapping(value = "/updatepassword")
    public ResponseEntity<?> updatePassword(String password){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        if(auth == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User Not Found");

	UserDetails userDetails = loadUserByUsername(manager, auth.getName());

	User.UserBuilder builder = User.builder();
        builder.username(userDetails.getUsername());
        builder.passwordEncoder(passwordEncoder::encode);
        builder.password(password);
        builder.authorities(userDetails.getAuthorities());
        builder.disabled(!userDetails.isEnabled());

        updateUser(manager, builder);
	return ResponseEntity.ok(auth.getName());
    }

    public void updateUser(JdbcUserDetailsManager manager, User.UserBuilder builder) {
        manager.updateUser(builder.build());
    }

    public UserDetails loadUserByUsername(JdbcUserDetailsManager manager, String username) {
        return manager.loadUserByUsername(username);
    }
}
