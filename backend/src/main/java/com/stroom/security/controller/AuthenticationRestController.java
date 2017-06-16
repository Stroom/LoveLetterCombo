package com.stroom.security.controller;

import com.stroom.rest.dto.RegistrationDTO;
import com.stroom.domain.model.SiteUser;
import com.stroom.security.JwtTokenUtil;
import com.stroom.security.JwtUser;
import com.stroom.security.dto.JwtAuthenticationRequest;
import com.stroom.security.dto.JwtAuthenticationResponse;
import com.stroom.security.service.AuthenticationService;
import com.stroom.service.RegistrationFailedException;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
	
	@Value("${jwt.header}")
	private String tokenHeader;
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	UserDetailsService userDetailsService;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@PostMapping("/login")
	public ResponseEntity<JwtAuthenticationResponse> logIn(@RequestBody JwtAuthenticationRequest authenticationRequest, Device device) {
		HttpHeaders headers = new HttpHeaders();
		try {
			final Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							authenticationRequest.getUsername(),
							authenticationRequest.getPassword()
					)
			);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
			final String token = jwtTokenUtil.generateToken(userDetails, device);
			List<String> authorities = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
			
			return new ResponseEntity<JwtAuthenticationResponse>(new JwtAuthenticationResponse(token, authorities), headers, HttpStatus.OK);
		}
		catch (AuthenticationException e) {
			return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logOut(@RequestBody String body, HttpServletRequest request, HttpServletResponse response, Device device) {
		//TODO might not be correct. Maybe use token and log out based on that?
		HttpHeaders headers = new HttpHeaders();
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String name = authentication.getName();
		
		CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
		SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
		cookieClearingLogoutHandler.logout(request, response, null);
		securityContextLogoutHandler.logout(request, response, null);
		
		log.info("Logged out " + name);
		return new ResponseEntity<Void>(headers, HttpStatus.OK);
	}
	
	//TODO use this in angular2
	@GetMapping("/refresh")
	public ResponseEntity<JwtAuthenticationResponse> refreshAndGetAuthenticationToken(HttpServletRequest request) {
		HttpHeaders headers = new HttpHeaders();
		String token = request.getHeader(tokenHeader);
		String username = jwtTokenUtil.getUsernameFromToken(token);
		JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
		
		if(jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
			String refreshedToken = jwtTokenUtil.refreshToken(token);
			List<String> authorities = user.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
			return new ResponseEntity<JwtAuthenticationResponse>(new JwtAuthenticationResponse(refreshedToken, authorities), headers, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<JwtAuthenticationResponse>(headers, HttpStatus.BAD_REQUEST);
		}
	}
	
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
	
}
