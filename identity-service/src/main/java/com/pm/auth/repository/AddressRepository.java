package com.pm.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.pm.auth.entity.Addresses;

import feign.Param;

public interface AddressRepository extends JpaRepository<Addresses, Long> {
	List<Addresses> findByUser_UserId(Long userId);
	
	@Modifying
	@Query("UPDATE Addresses a SET a.isDefault = false WHERE a.user.id = :userId AND a.addressId <> :currentAddressId")
	void unsetOtherDefaults(@Param("userId") Long userId, @Param("currentAddressId") Long currentAddressId);
}
