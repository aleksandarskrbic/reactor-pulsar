package reactor.pulsar.receiver;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.receiver.internals.DefaultPulsarReceiver;
import reactor.pulsar.receiver.internals.ReceiverRecord;

public interface PulsarReceiver<M> {

  static <M> PulsarReceiver<M> create(
      ReactorPulsarClient reactorPulsarClient, ReceiverOptions<M> receiverOptions) {
    return new DefaultPulsarReceiver<>(reactorPulsarClient, receiverOptions);
  }

  Flux<ReceiverRecord<M>> receive();
}
