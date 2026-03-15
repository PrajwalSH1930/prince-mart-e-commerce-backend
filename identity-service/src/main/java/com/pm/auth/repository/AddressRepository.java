package com.pm.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pm.auth.entity.Addresses;

public interface AddressRepository extends JpaRepository<Addresses, Long> {
	List<Addresses> findByUser_UserId(Long userId);
}
