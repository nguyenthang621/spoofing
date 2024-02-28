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

/** A Whitelist. */
@Entity
@Table(name = "whitelist")
@Data
public class Whitelist   {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
  @SequenceGenerator(name = "sequenceGenerator")
  private Long id;

  @NotNull
  @Column(name = "state", nullable = false)
  private Integer state;

  @NotNull
  @Column(name = "prefix", nullable = false, unique = true)
  private Long prefix;

  @Column(name = "length")
  private int length = 0;

  @Column(name = "description")
  private String description;
  
}
