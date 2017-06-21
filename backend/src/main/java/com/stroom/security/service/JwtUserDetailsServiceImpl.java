package com.stroom.security.service;

import com.stroom.website.domain.model.SiteUser;
import com.stroom.website.domain.repository.SiteUserRepository;
import com.stroom.security.JwtUserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Created by Stroom on 12/06/2017.
 */
@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private SiteUserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		SiteUser user = userRepository.findByUsername(username);
		
		if(user == null) {
			throw new UsernameNotFoundException("Username not found: " + username);
		}
		return JwtUserFactory.create(user);
	}
	
}
