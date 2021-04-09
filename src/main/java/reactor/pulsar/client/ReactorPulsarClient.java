package reactor.pulsar.client;

import org.apache.pulsar.client.api.PulsarClient;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.internals.DefaultReactorPulsarClient;
import reactor.util.annotation.NonNull;

public interface ReactorPulsarClient {

  @NonNull
  static ReactorPulsarClient create(@NonNull ClientOptions clientOptions) {
    return new DefaultReactorPulsarClient(clientOptions);
  }

  Mono<PulsarClient> client();

  void close();
}
