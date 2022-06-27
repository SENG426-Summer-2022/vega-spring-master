package com.uvic.venus.controller;

import com.uvic.venus.model.UserInfo;
import com.uvic.venus.model.UserInfoWithRole;
import com.uvic.venus.repository.UserInfoDAO;
import com.uvic.venus.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
                                           manager.loadUserByUsername(u.getUsername()).getAuthorities()
                                           .contains(new SimpleGrantedAuthority("ROLE_STAFF"))
                                           ? new SimpleGrantedAuthority("ROLE_STAFF")
                                           : new SimpleGrantedAuthority("ROLE_USER"),
                                           manager.loadUserByUsername(u.getUsername()).isEnabled()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userWithRoleList);
    }

    @GetMapping(value ="/enableuser")
    public ResponseEntity<String> enableUserAccount(@RequestParam String username, @RequestParam boolean enable){
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        UserDetails userDetails = manager.loadUserByUsername(username);

        User.UserBuilder builder = User.builder();
        builder.username(userDetails.getUsername());
        builder.password(userDetails.getPassword());
        builder.authorities(userDetails.getAuthorities());
        builder.disabled(!enable);

        manager.updateUser(builder.build());
        return ResponseEntity.ok("User Updated Successfully");
    }

    @GetMapping(value ="/changerole")
    public ResponseEntity<String> changeRole(@RequestParam String username, @RequestParam String role){
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(role));

        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);
        UserDetails userDetails = manager.loadUserByUsername(username);

        User.UserBuilder builder = User.builder();
        builder.username(userDetails.getUsername());
        builder.password(userDetails.getPassword());
        builder.authorities(authorities);
        builder.disabled(userDetails.isEnabled());

        manager.updateUser(builder.build());
        return ResponseEntity.ok("User Updated Successfully");
    }

    @PostMapping(value = "/handlefileupload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file){
        storageService.store(file);
        return ResponseEntity.ok("File uploaded Successfully");
    }

}
