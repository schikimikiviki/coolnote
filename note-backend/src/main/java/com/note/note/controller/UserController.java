
package com.note.note.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.note.note.data.CustomUserDetailsService;
import com.note.note.data.Note;
import com.note.note.data.User;
import com.note.note.data.UserDto;
import com.note.note.service.UserService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("")
public class UserController {

 @Autowired
 private UserDetailsService userDetailsService;
 
 private final PasswordEncoder passwordEncoder; 

 private final UserService userService;
 
 private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);


 public UserController(UserService userService, PasswordEncoder passwordEncoder) {
  this.passwordEncoder = passwordEncoder;
  this.userService = userService;
 }

 @GetMapping("/home")
 public UserDetails getHome(Principal principal) {
  // Ensure principal is not null and return user details
  return userDetailsService.loadUserByUsername(principal.getName());
 }
 
 @GetMapping("/test")
 public ResponseEntity<String> testLogging() {
	 System.out.println("testtt");
     logger.info("Test endpoint called");
     return ResponseEntity.ok("Test successful");
 }


 @GetMapping("/users")
 public List<User> getAllUsers() {
     // Return list of users from the database
     return userService.findAllUsers();
 }
 
 @GetMapping("/users/{userName}")
 public User getUserData(@PathVariable String userName) {
     // Return details of a single user
     return userService.findByUsername(userName);
 }
 
 @GetMapping("/users/id/{Id}")
 public Optional<User> getUserDataById(@PathVariable Long Id) {
     // Return details of a single user
     return userService.findUserById(Id);
 }
 
 @PostMapping("/login")
 public ResponseEntity<?> login(@RequestParam Map<String, String> loginData) {
     String username = loginData.get("username");
     String password = loginData.get("password");

     // Load user details by username
     UserDetails userDetails = userDetailsService.loadUserByUsername(username);

     if (userDetails != null && passwordEncoder.matches(password, userDetails.getPassword())) {
         // Login successful
         return ResponseEntity.ok(Map.of("success", true, "message", "Login successful"));
     } else {
         // Invalid credentials
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "Invalid credentials"));
     }
 }


 
 @PostMapping("/register")
 public ResponseEntity<?> register(@RequestBody UserDto userDto) {
     User existingUser = userService.findByUsername(userDto.getUsername());
     if (existingUser != null) {
         return ResponseEntity
                 .status(HttpStatus.CONFLICT)
                 .body(Map.of("success", false, "message", "User already exists"));
     }
     User newUser = userService.save(userDto);
     return ResponseEntity
             .status(HttpStatus.CREATED)
             .body(Map.of("success", true, "user", newUser));
 }
 
 
 @PatchMapping("/users/{userId}")
 public User editUser(@PathVariable Long userId, @RequestBody User user) {

     // Find the note to edit
     Optional<User> foundUserOptional = userService.findUserById(userId);
     
     if (foundUserOptional.isPresent()) {
         User foundUser = foundUserOptional.get();

         // Only update fields that are not null
         if (user.getUsername() != null) {
             foundUser.setUsername(user.getUsername());
         }
         if (user.getFullname() != null) {
        	 foundUser.setFullname(user.getFullname());
         }
         
         if (user.getPassword() != null) {
        	 System.out.println(user.getPassword());
        	 foundUser.setPassword(passwordEncoder.encode(user.getPassword()));
        	 System.out.println(foundUser.getPassword());    
        	}
         
         if (user.getEmail() != null) {
        	 foundUser.setEmail(user.getEmail());
         }

         if (user.getIsAuthActive() != null) {
        	 foundUser.setIsAuthActive(user.getIsAuthActive());
         }
        
         if (user.getLoginList() != null && !user.getLoginList().isEmpty()) {
        	    // Append new values to the existing login list
        	    if (foundUser.getLoginList() == null) {
        	        foundUser.setLoginList(new ArrayList<>(user.getLoginList())); // Initialize if null
        	    } else {
        	        foundUser.getLoginList().addAll(user.getLoginList()); // Append new entries
        	        
        	     // Enforce a maximum of 10 entries by removing the oldest ones
        	        while (foundUser.getLoginList().size() > 10) {
        	            foundUser.getLoginList().remove(0); // Remove the first (oldest) entry
        	        }
        	    }
        	}
         
         if (user.getTheme() != null) {
        	 foundUser.setTheme(user.getTheme());
         }
         
         if (user.getFontSize() != null) {
        	 foundUser.setFontSize(user.getFontSize());
         }
         
         if (user.getColorPalette() != null) {
        	 foundUser.setColorPalette(user.getColorPalette());
         }


         UserDto userDto = new UserDto(
                 foundUser.getId(),
                 foundUser.getUsername(),
                 foundUser.getPassword(), 
                 foundUser.getFullname(),
                 foundUser.getNotes(),
                 foundUser.getRoles(),
                 foundUser.getEmail(),
                 foundUser.getIsAuthActive(), 
                 foundUser.getLoginList(), 
                 foundUser.getTheme(), 
                 foundUser.getFontSize(), 
                 foundUser.getColorPalette()
             );

       
             return userService.save(userDto);
     }

     // If the user isn't found, return null or throw an exception
     return null;
 }
 
 @DeleteMapping("/users/{userId}")
 public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
     try {
         userService.deleteUserById(userId);
         return ResponseEntity.ok(Map.of("success", true, "message", "User deleted successfully"));
     } catch (Exception e) {
         return ResponseEntity
                 .status(HttpStatus.NOT_FOUND)
                 .body(Map.of("success", false, "message", "User not found"));
     }
 }



}
