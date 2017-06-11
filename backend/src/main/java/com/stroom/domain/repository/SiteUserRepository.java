package com.stroom.domain.repository;

import com.stroom.domain.model.SiteUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Stroom on 11/06/2017.
 */
@Repository
public interface SiteUserRepository extends JpaRepository<SiteUser, String> {
}
