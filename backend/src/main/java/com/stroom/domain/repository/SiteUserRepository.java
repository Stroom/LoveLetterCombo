package com.stroom.domain.repository;

import com.stroom.domain.model.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by Stroom on 11/06/2017.
 */
@Repository
public interface SiteUserRepository extends JpaRepository<SiteUser, String> {
	
	@Query("select u from SiteUser u where u.username = ?1")
	SiteUser findByUsername(String username);
	
	@Query("select u from SiteUser u where u.id = ?1")
	SiteUser findById(String id);
}
