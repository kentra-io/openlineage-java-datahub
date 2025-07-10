package io.kentra.openlineage.config;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
public class ObjectMapperConfig {

  public ObjectMapper objectMapper() {
    var mapper = new ObjectMapper();
    mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
    return mapper;
  }
}
