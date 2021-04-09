package reactor.pulsar.client.internals;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.ClientOptions;
import reactor.pulsar.client.ReactorPulsarClient;

public class DefaultReactorPulsarClient implements ReactorPulsarClient {

    static final Logger log = LoggerFactory.getLogger(DefaultReactorPulsarClient.class.getName());

    private final Mono<PulsarClient> clientMono;

    public DefaultReactorPulsarClient(ClientOptions options) {
        this.clientMono = Mono.fromCallable(() -> createClient(options))
                .doOnSuccess(__ -> log.info("PulsarClient initialized"))
                .doOnError(error -> log.trace("Failed to create PulsarClient", error))
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
                .doOnError(error -> log.trace("Failed to close PulsarClient", error))
                .block();
    }

    private PulsarClient createClient(ClientOptions options) throws PulsarClientException {
        return PulsarClient.builder().loadConf(options.properties()).build();
    }
}
