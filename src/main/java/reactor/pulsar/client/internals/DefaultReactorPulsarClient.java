package reactor.pulsar.client.internals;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.ClientOptions;
import reactor.pulsar.client.ReactorPulsarClient;

public class DefaultReactorPulsarClient implements ReactorPulsarClient {

    private final Mono<PulsarClient> clientMono;

    public DefaultReactorPulsarClient(ClientOptions options) {
        this.clientMono = Mono.fromCallable(() -> {
                    try {
                        return org.apache.pulsar.client.api.PulsarClient.builder()
                                .loadConf(options.properties())
                                .build();
                    } catch (PulsarClientException e) {
                        return null;
                    }
                })
                .cache();
    }

    @Override
    public Mono<PulsarClient> client() {
        return clientMono;
    }

    @Override
    public void close() {
        clientMono
                .flatMap(client -> Mono.fromCompletionStage(client::closeAsync))
                .block();
    }
}
