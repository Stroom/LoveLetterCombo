package com.stroom.security.service;

import com.stroom.security.JwtTokenUtil;
import com.stroom.security.JwtUser;
import com.stroom.security.dto.JwtAuthRequestDTO;
import com.stroom.security.dto.JwtAuthResponseDTO;
import com.stroom.security.dto.JwtAuthRolesDTO;
import com.stroom.security.dto.RegistrationDTO;
import com.stroom.domain.model.Authority;
import com.stroom.domain.model.SiteUser;
import com.stroom.domain.repository.AuthorityRepository;
import com.stroom.domain.repository.SiteUserRepository;
import com.stroom.service.IdentifierFactory;
import com.stroom.service.exception.RegistrationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stroom on 12/06/2017.
 */
@Service
public class AuthenticationService {
	
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	UserDetailsService userDetailsService;
	@Autowired
	JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	SiteUserRepository userRepository;
	@Autowired
	AuthorityRepository authorityRepository;
	
	@Autowired
	IdentifierFactory identifierFactory;
	
	@Value("${jwt.header}")
	private String tokenHeader;
	
	public SiteUser register(RegistrationDTO dto) throws RegistrationFailedException {
		//Check if the user exists.
		if(dto == null || dto.getUsername() == null || dto.getUsername().length() < 1 ||
				dto.getPassword() == null || dto.getPassword().length() < 1) {
			throw new RegistrationFailedException("Bad input");
		}
		SiteUser existing = userRepository.findByUsername(dto.getUsername());
		if(existing != null) {
			throw new RegistrationFailedException("User already exists");
		}
		//If not, create user.
		
		List<Authority> authorities = new ArrayList<Authority>();
		Authority userAuthority = authorityRepository.findByName("ROLE_USER");
		if(userAuthority == null) {
			userAuthority = Authority.of(identifierFactory.nextID(), "ROLE_USER");
			authorityRepository.save(userAuthority);
		}
		authorities.add(userAuthority);
		SiteUser user = SiteUser.of(
				identifierFactory.nextID(),
				dto.getUsername(),
				new BCryptPasswordEncoder().encode(dto.getPassword()),
				authorities,
				true,
				new Date());
		userRepository.save(user);
		return user;
	}
	
	public JwtAuthResponseDTO logIn(JwtAuthRequestDTO authenticationRequest, Device device) {
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
		
		return new JwtAuthResponseDTO(token, authorities);
	}
	
	public JwtAuthRolesDTO getRoles(HttpServletRequest request) {
		String token = request.getHeader(tokenHeader);
		String username = jwtTokenUtil.getUsernameFromToken(token);
		JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
		
		//Check if token is valid?
		if(jwtTokenUtil.validateToken(token, user)) {
			List<String> authorities = user.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
			return new JwtAuthRolesDTO(authorities);
		}
		else {
			return null;
		}
	}
	
	public void logOut(HttpServletRequest request, HttpServletResponse response) {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String name = authentication.getName();
		
		CookieClearingLogoutHandler cookieClearingLogoutHandler = new CookieClearingLogoutHandler(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY);
		SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
		cookieClearingLogoutHandler.logout(request, response, null);
		securityContextLogoutHandler.logout(request, response, null);
	}
	
	public JwtAuthResponseDTO refreshAndGetAuthenticationToken(HttpServletRequest request) {
		String token = request.getHeader(tokenHeader);
		String username = jwtTokenUtil.getUsernameFromToken(token);
		JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
		
		if(jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
			String refreshedToken = jwtTokenUtil.refreshToken(token);
			List<String> authorities = user.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
			return new JwtAuthResponseDTO(refreshedToken, authorities);
		}
		else {
			return null;
		}
	}
	
}
