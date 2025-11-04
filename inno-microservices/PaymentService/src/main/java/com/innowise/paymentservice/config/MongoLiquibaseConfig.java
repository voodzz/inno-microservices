package com.innowise.paymentservice.config;

import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for initializing Liquibase integration with MongoDB. It provides the
 * required beans for connecting to the database and running migrations.
 */
@Configuration
public class MongoLiquibaseConfig {
  @Value("${mongo.url}")
  private String url;

  /**
   * Creates the runner bean responsible for executing the Liquibase changesets upon application
   * startup.
   *
   * @param database The configured MongoLiquibaseDatabase connection.
   * @return A {@link MongoLiquibaseRunner} instance.
   */
  @Bean
  public MongoLiquibaseRunner liquibaseRunner(final MongoLiquibaseDatabase database) {
    return new MongoLiquibaseRunner(database);
  }

  /**
   * Initializes and returns the Liquibase-specific database connection for MongoDB. It uses the
   * configured MongoDB URL to establish the connection.
   *
   * @return Database with connection
   * @throws DatabaseException when cannot connect
   */
  @Bean
  public MongoLiquibaseDatabase database() throws DatabaseException {
    return (MongoLiquibaseDatabase)
        DatabaseFactory.getInstance().openDatabase(url, null, null, null, null);
  }
}
