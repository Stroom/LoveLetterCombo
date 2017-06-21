package com.stroom.website.domain.model;

import com.stroom.website.rest.dto.SiteUserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Stroom on 11/06/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SiteUser {
	
	@Id
	private String id;
	
	@Column(nullable = false, unique = true)
	private String username;
	@Column(nullable = false)
	private String password;
	
	@ManyToMany(fetch =  FetchType.EAGER)
	private List<Authority> authorities;
	
	@NotNull
	private Boolean enabled;
	
	@Temporal(TemporalType.TIMESTAMP)
	@NotNull
	private Date lastPasswordResetDate;
	
	public SiteUserDTO toDTO() {
		List<String> auths = new ArrayList<String>();
		if(this.authorities != null && !this.authorities.isEmpty()) {
			this.authorities.forEach(a -> auths.add(a.getName()));
		}
		return SiteUserDTO.of(
				this.id,
				this.username,
				auths
		);
	}
}
