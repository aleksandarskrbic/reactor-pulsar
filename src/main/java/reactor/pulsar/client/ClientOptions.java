package reactor.pulsar.client;

import reactor.util.annotation.NonNull;

import java.util.Map;

public interface ClientOptions {

  @NonNull
  static ClientOptions create(@NonNull Map<String, Object> properties) {
    return new ImmutableClientOptions(properties);
  }

  Map<String, Object> properties();
}
