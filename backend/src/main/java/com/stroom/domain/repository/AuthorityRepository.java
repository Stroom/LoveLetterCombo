package com.stroom.domain.repository;

import com.stroom.domain.model.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Created by Stroom on 12/06/2017.
 */
@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
	
	@Query("select a from Authority a where a.name = ?1")
	Authority findByName(String name);
	
}
