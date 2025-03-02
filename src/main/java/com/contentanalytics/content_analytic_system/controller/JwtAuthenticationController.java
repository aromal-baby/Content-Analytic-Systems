package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.config.UserDetailsImpl;
import com.contentanalytics.content_analytic_system.payload.request.LoginRequest;
import com.contentanalytics.content_analytic_system.payload.response.JwtResponse;
import com.contentanalytics.content_analytic_system.security.Jwtutils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final Jwtutils jwtutils;

    public JwtAuthenticationController(AuthenticationManager authenticationManager,
                                       Jwtutils jwtutils) {
        this.authenticationManager = authenticationManager;
        this.jwtutils = jwtutils;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtutils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());


        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getEmail(), roles));
    }
}
