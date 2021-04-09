package reactor.pulsar.client;

import java.util.Map;

public class ImmutableClientOptions implements ClientOptions {

  private final Map<String, Object> properties;

  public ImmutableClientOptions(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public Map<String, Object> properties() {
    return properties;
  }
}
