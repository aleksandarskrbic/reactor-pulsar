package reactor.pulsar.client;

import org.apache.pulsar.client.api.PulsarClient;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.internals.DefaultReactorPulsarClient;

public interface ReactorPulsarClient {

    static ReactorPulsarClient create(ClientOptions clientOptions) {
        return new DefaultReactorPulsarClient(clientOptions);
    }

    Mono<PulsarClient> client();

    void close();
}
