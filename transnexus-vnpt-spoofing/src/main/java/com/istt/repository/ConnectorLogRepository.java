package com.istt.repository;

import org.bitbucket.gt_tech.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import com.istt.domain.ConnectorLog;
import com.istt.domain.QConnectorLog;

/** Spring Data repository for the ConnectorLog entity. */
@Repository
public interface ConnectorLogRepository
    extends JpaRepository<ConnectorLog, Long>,
        QuerydslPredicateExecutor<ConnectorLog>,
        QuerydslBinderCustomizer<QConnectorLog> {

  @Override
  default void customize(QuerydslBindings bindings, QConnectorLog root) {
    bindings
        .bind(root.id)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
  }
}
