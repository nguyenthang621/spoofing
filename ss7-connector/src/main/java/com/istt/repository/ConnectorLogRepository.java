package com.istt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.istt.domain.ConnectorLog;

/** Spring Data repository for the ConnectorLog entity. */
@Repository
public interface ConnectorLogRepository extends JpaRepository<ConnectorLog, Long> {

}
