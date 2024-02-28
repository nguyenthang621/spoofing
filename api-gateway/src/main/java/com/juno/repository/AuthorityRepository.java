package com.juno.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.juno.domain.Authority;

/**
 * Spring Data MongoDB repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends ReactiveMongoRepository<Authority, String> {
}
