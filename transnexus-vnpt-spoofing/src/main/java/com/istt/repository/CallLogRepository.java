package com.istt.repository;

import com.istt.domain.CallLog;
import com.istt.domain.QCallLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.bitbucket.gt_tech.spring.data.querydsl.value.operators.ExpressionProviderFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Spring Data repository for the CallLog entity. */
@SuppressWarnings("unused")
@Repository
public interface CallLogRepository
    extends JpaRepository<CallLog, Long>,
        QuerydslPredicateExecutor<CallLog>,
        QuerydslBinderCustomizer<QCallLog> {

  @Override
  default void customize(QuerydslBindings bindings, QCallLog root) {
    bindings
        .bind(root.id)
        .all((path, values) -> ExpressionProviderFactory.getPredicate(path, values));
  }

  @Query(
      value =
          "SELECT count(*) as cnt, state, error_code, error_desc, date(created_at at time zone"
              + " 'utc' at time zone 'ict') as date FROM call_log WHERE created_at >= :startDate"
              + " and created_at < :endDate GROUP BY state, error_code, error_desc, date ORDER BY"
              + " date DESC, error_code",
      nativeQuery = true)
  public List<Map<String, Object>> dailyStatsByStateIn(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query("SELECT DISTINCT state FROM CallLog")
  public List<Integer> findDistinctState();

  @Query("SELECT DISTINCT errorCode FROM CallLog")
  public List<Integer> findDistinctErrorCode();
}
