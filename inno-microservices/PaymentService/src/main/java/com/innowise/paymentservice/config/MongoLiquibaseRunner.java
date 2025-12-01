package com.innowise.paymentservice.config;

import liquibase.Liquibase;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.integration.spring.SpringResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

/**
 * Executes the Liquibase migration logic for MongoDB upon Spring application startup. Implements
 * {@link CommandLineRunner} to run the migration as soon as the application context is loaded.
 */
@RequiredArgsConstructor
public class MongoLiquibaseRunner implements CommandLineRunner, ResourceLoaderAware {
  private final MongoLiquibaseDatabase database;

  @Setter protected ResourceLoader resourceLoader;

  /**
   * The main execution method that runs the database migration. It finds the changelog file,
   * initializes Liquibase, and calls the update method.
   *
   * @param args Command-line arguments (unused).
   * @throws Exception if Liquibase fails to run the migration.
   */
  public void run(final String... args) throws Exception {
    Liquibase liquibase =
        new Liquibase(
            "db/changelog/initial-changelog.yaml",
            new SpringResourceAccessor(resourceLoader),
            database);
    liquibase.update();
  }
}
