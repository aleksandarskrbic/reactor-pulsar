package reactor.pulsar.sender;

import org.apache.pulsar.client.api.MessageId;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.sender.internals.DefaultPulsarSender;

public interface PulsarSender<M> {

    static <M> PulsarSender<M> create(ReactorPulsarClient reactorPulsarClient, SenderOptions<M> senderOptions) {
        return new DefaultPulsarSender<>(reactorPulsarClient, senderOptions);
    }

    Flux<MessageId> send(Publisher<? extends PulsarRecord<M>> records);

    void close();
}
