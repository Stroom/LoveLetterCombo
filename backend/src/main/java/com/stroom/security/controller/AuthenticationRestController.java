package com.stroom.security.controller;

import com.stroom.security.dto.RegistrationDTO;
import com.stroom.domain.model.SiteUser;
import com.stroom.security.JwtTokenUtil;
import com.stroom.security.JwtUser;
import com.stroom.security.dto.JwtAuthRequestDTO;
import com.stroom.security.dto.JwtAuthResponseDTO;
import com.stroom.security.dto.JwtAuthRolesDTO;
import com.stroom.security.service.AuthenticationService;
import com.stroom.service.exception.RegistrationFailedException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stroom on 12/06/2017.
 */
@RestController
@CrossOrigin
@RequestMapping("/auth")
public class AuthenticationRestController {
	
	private final Logger log = Logger.getLogger(AuthenticationRestController.class);
	
	@Autowired
	AuthenticationService authenticationService;
	
	@PostMapping("/register")
	public ResponseEntity<String> register(@RequestBody RegistrationDTO dto) {
		HttpHeaders headers = new HttpHeaders();
		try {
			SiteUser user = authenticationService.register(dto);
			log.info("New user created: " + user.getUsername());
			return new ResponseEntity<String>("Success", headers, HttpStatus.OK);
		}
		catch (RegistrationFailedException e) {
			log.warn("User creation failed: " + e.getMessage());
			return new ResponseEntity<String>(e.getMessage(), headers, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/login")
	public ResponseEntity<JwtAuthResponseDTO> logIn(@RequestBody JwtAuthRequestDTO authenticationRequest, Device device) {
		HttpHeaders headers = new HttpHeaders();
		try {
			
			JwtAuthResponseDTO response = authenticationService.logIn(authenticationRequest, device);
			
			return new ResponseEntity<JwtAuthResponseDTO>(response, headers, HttpStatus.OK);
		}
		catch (AuthenticationException e) {
			return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
		}
	}
	
	@GetMapping("/roles")
	public ResponseEntity<JwtAuthRolesDTO> getRoles(HttpServletRequest request, HttpServletResponse response, Device device) {
		HttpHeaders headers = new HttpHeaders();
		
		JwtAuthRolesDTO roles = authenticationService.getRoles(request);
		
		if(roles != null) {
			return new ResponseEntity<JwtAuthRolesDTO>(roles, headers, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<JwtAuthRolesDTO>(headers, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logOut(@RequestBody String body, HttpServletRequest request, HttpServletResponse response, Device device) {
		//TODO might not be correct. Maybe use token and log out based on that?
		HttpHeaders headers = new HttpHeaders();
		
		authenticationService.logOut(request, response);
		
		return new ResponseEntity<Void>(headers, HttpStatus.OK);
	}
	
	//TODO use this in angular2?
	@GetMapping("/refresh")
	public ResponseEntity<JwtAuthResponseDTO> refreshAndGetAuthenticationToken(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		
		JwtAuthResponseDTO response = authenticationService.refreshAndGetAuthenticationToken(request);
		
		if(response != null) {
			return new ResponseEntity<JwtAuthResponseDTO>(response, headers, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<JwtAuthResponseDTO>(headers, HttpStatus.BAD_REQUEST);
		}
	}
	
}
