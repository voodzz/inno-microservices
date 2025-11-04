package com.innowise.paymentservice.config;

import liquibase.Liquibase;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.integration.spring.SpringResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

@RequiredArgsConstructor
public class MongoLiquibaseRunner implements CommandLineRunner, ResourceLoaderAware {
  private final MongoLiquibaseDatabase database;

  @Setter protected ResourceLoader resourceLoader;

  public void run(final String... args) throws Exception {
    Liquibase liquibase =
        new Liquibase(
            "db/changelog/initial-changelog.yaml",
            new SpringResourceAccessor(resourceLoader),
            database);
    liquibase.update();
  }
}
