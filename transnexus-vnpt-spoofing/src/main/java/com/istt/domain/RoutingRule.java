package com.istt.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

/** A RoutingRule. */
@Entity
@Table(name = "routing_rule")
@Data
public class RoutingRule  {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
  @SequenceGenerator(name = "sequenceGenerator")
  private Long id;

  @NotNull
  @Column(name = "name", nullable = false)
  private String name;

  @NotNull
  @Column(name = "state", nullable = false)
  private Integer state = 200;

  @Column(name = "description")
  private String description;

  @NotNull
  @Column(name = "aprefix", nullable = false)
  private Long aprefix;

  @Column(name = "alength")
  private Integer alength;
  
}
