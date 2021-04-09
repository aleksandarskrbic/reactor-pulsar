package reactor.pulsar.sender;

import org.apache.pulsar.client.api.Schema;
import reactor.util.annotation.NonNull;

import java.util.Map;

public interface SenderOptions<M> {

  @NonNull
  static <M> SenderOptions<M> create(
      @NonNull Map<String, Object> properties, @NonNull Schema<M> schema) {
    return new ImmutableSenderOptions<>(properties, schema);
  }

  Map<String, Object> properties();

  Schema<M> schema();
}
