package com.istt.repository;

import com.istt.domain.ConnectorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data repository for the ConnectorLog entity. */
@Repository
public interface ConnectorLogRepository extends JpaRepository<ConnectorLog, Long> {}
