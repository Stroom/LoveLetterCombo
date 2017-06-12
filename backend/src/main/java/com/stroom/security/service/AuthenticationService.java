package com.stroom.security.service;

import com.stroom.rest.dto.RegistrationDTO;
import com.stroom.domain.model.Authority;
import com.stroom.domain.model.SiteUser;
import com.stroom.domain.repository.AuthorityRepository;
import com.stroom.domain.repository.SiteUserRepository;
import com.stroom.service.IdentifierFactory;
import com.stroom.service.RegistrationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Stroom on 12/06/2017.
 */
@Service
public class AuthenticationService {
	
	@Autowired
	SiteUserRepository userRepository;
	@Autowired
	AuthorityRepository authorityRepository;
	
	@Autowired
	IdentifierFactory identifierFactory;
	public List<String> authenticate() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<String> roles = new ArrayList<String>();
		if(principal instanceof UserDetails) {
			UserDetails details = (UserDetails) principal;
			for(GrantedAuthority authority : details.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
		}
		return roles;
	}
	
	
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
	
}
