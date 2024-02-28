package com.istt.repository;

import com.istt.domain.Blacklist;
import com.istt.domain.QBlacklist;
import java.util.Set;
import org.bitbucket.gt_tech.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

/** Spring Data repository for the Blacklist entity. */
@SuppressWarnings("unused")
@Repository
public interface BlacklistRepository
    extends JpaRepository<Blacklist, Long>,
        QuerydslPredicateExecutor<Blacklist>,
        QuerydslBinderCustomizer<QBlacklist> {

  @Override
  default void customize(QuerydslBindings bindings, QBlacklist root) {
    bindings
        .bind(root.id)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.prefix)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.state)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.length)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
    bindings
        .bind(root.description)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
  }

  long countByPrefixInAndStateAndLengthIn(
      Set<Long> callingPrefixes, int i, Set<Integer> j);
}
