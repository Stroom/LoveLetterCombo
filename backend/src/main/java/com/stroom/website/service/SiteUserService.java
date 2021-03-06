package com.stroom.website.service;

import com.stroom.website.domain.model.SiteUser;
import com.stroom.website.domain.repository.SiteUserRepository;
import com.stroom.website.rest.dto.SiteUserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Stroom on 12/06/2017.
 */
@Service
public class SiteUserService {
	
	@Autowired
	SiteUserRepository userRepository;
	
	public List<SiteUserDTO> findAllUsers() {
		List<SiteUserDTO> response = new ArrayList<SiteUserDTO>();
		List<SiteUser> list = userRepository.findAll();
		if(list != null && !list.isEmpty()) {
			list.stream().forEach(user -> response.add(user.toDTO()));
		}
		return response;
	}
	
	public SiteUserDTO findUserById(String id) {
		SiteUser user = userRepository.findById(id);
		if(user != null) {
			return user.toDTO();
		}
		return null;
	}
	
}
