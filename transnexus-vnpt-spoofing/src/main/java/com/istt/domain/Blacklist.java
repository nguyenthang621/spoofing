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

/** A Blacklist. */
@Entity
@Table(name = "blacklist")
@Data
public class Blacklist {

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

  public Blacklist state(int state) {
    this.state = state;
    return this;
  }

  public Blacklist prefix(long prefix) {
    this.prefix = prefix;
    return this;
  }

  public Blacklist length(int length) {
    this.length = length;
    return this;
  }

  public Blacklist description(String description) {
    this.description = description;
    return this;
  }
  
}
