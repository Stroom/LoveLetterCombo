package com.stroom.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Stroom on 12/06/2017.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Authority {
	
	@Id
	private String id;
	
	@Column(nullable = false, unique = true)
	private String name;
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final Authority other = (Authority) obj;
		if(id == null) {
			if(other.id != null) {
				return false;
			}
		}
		else if(!id.equals(other.id)) {
			return false;
		}
		if(name == null) {
			if(other.name != null) {
				return false;
			}
		}
		else if(!name.equals(other.name)) {
			return false;
		}
		return true;
	}
	
}
