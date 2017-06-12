package com.stroom.security;

import com.stroom.domain.model.Authority;
import com.stroom.domain.model.SiteUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Stroom on 12/06/2017.
 */
public final class JwtUserFactory {
	
	private JwtUserFactory() {}
	
	public static JwtUser create(SiteUser user) {
		return new JwtUser(
				user.getId(),
				user.getUsername(),
				user.getPassword(),
				mapToGrantedAuthorities(user.getAuthorities()),
				user.getEnabled(),
				user.getLastPasswordResetDate()
		);
	}
	
	private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
		return authorities.stream().map(a -> new SimpleGrantedAuthority(a.getName())).collect(Collectors.toList());
	}
}
