package reactor.pulsar.sender;

import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.sender.internals.DefaultPulsarSender;

public interface PulsarSender<M> {

    static <M> PulsarSender<M> create(ReactorPulsarClient reactorPulsarClient, SenderOptions<M> senderOptions) {
        return new DefaultPulsarSender<>(reactorPulsarClient, senderOptions);
    }

    // <T> Flux<MessageId> send(Publisher<?> records);

    void close();
}
