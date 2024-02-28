package com.istt.repository;

import com.istt.domain.QRoutingRule;
import com.istt.domain.RoutingRule;
import java.util.List;
import java.util.Set;
import org.bitbucket.gt_tech.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

/** Spring Data repository for the RoutingRule entity. */
@Repository
public interface RoutingRuleRepository
    extends JpaRepository<RoutingRule, Long>,
        QuerydslPredicateExecutor<RoutingRule>,
        QuerydslBinderCustomizer<QRoutingRule> {

  @Override
  default void customize(QuerydslBindings bindings, QRoutingRule root) {
    bindings
        .bind(root.id)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.name)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.aprefix)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.alength)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
  }

  List<RoutingRule> findAllByStateAndAprefixInAndAlengthIn(
      int i, Set<Long> callingPrefixes, List<Integer> asList);
}
