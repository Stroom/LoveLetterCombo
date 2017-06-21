package com.stroom.website.rest.controller;

import com.stroom.website.rest.dto.SiteUserDTO;
import com.stroom.security.JwtTokenUtil;
import com.stroom.security.JwtUser;
import com.stroom.website.service.SiteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Stroom on 12/06/2017.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class SiteUserRestController {
	
	@Autowired
	SiteUserService siteUserService;
	
	@Value("${jwt.header}")
	private String tokenHeader;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@GetMapping
	public ResponseEntity<List<SiteUserDTO>> findAllUsers() {
		HttpHeaders headers = new HttpHeaders();
		
		return new ResponseEntity<List<SiteUserDTO>>(siteUserService.findAllUsers(), headers, HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<SiteUserDTO> findUserById(@PathVariable("id") String id) {
		HttpHeaders headers = new HttpHeaders();
		
		SiteUserDTO user = siteUserService.findUserById(id);
		if(user == null) {
			return new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<SiteUserDTO>(user, headers, HttpStatus.OK);
	}
	
	@GetMapping("user")
	public JwtUser getAuthenticatedUser(HttpServletRequest request) {
		String token = request.getHeader(tokenHeader);
		String username = jwtTokenUtil.getUsernameFromToken(token);
		JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);
		return user;
	}
}
