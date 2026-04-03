package com.mahmoudyoussef.url_shortener.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mahmoudyoussef.url_shortener.entity.UrlMapping;

@Repository
public interface UrlRepository extends JpaRepository<UrlMapping, String> {
	
}
