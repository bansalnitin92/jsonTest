package com.jsonTest.jsonTest;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Component
public class DbQueries {

  private EntityManager entityManager;

  public DbQueries(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Object executeSelectQuery(String query) {
    Query emQuery = entityManager.createQuery(query);
    return emQuery.getSingleResult();
  }

}
