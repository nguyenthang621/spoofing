package com.juno.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.juno.domain.PersistentAuditEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Spring Data MongoDB repository for the {@link PersistentAuditEvent} entity.
 */
public interface PersistenceAuditEventRepository extends ReactiveMongoRepository<PersistentAuditEvent, String> {

    Flux<PersistentAuditEvent> findByPrincipal(String principal);

    Flux<PersistentAuditEvent> findAllByAuditEventDateBetween(Instant fromDate, Instant toDate, Pageable pageable);

    Flux<PersistentAuditEvent> findByAuditEventDateBefore(Instant before);

    Flux<PersistentAuditEvent> findAllBy(Pageable pageable);

    Mono<Long> countByAuditEventDateBetween(Instant fromDate, Instant toDate);
}
