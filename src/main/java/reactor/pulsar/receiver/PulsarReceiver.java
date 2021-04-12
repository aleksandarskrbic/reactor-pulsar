package reactor.pulsar.receiver;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.receiver.internals.DefaultPulsarReceiver;

public interface PulsarReceiver<M> {
  public Consumer<M> get();

  static <M> PulsarReceiver<M> create(
      ReactorPulsarClient reactorPulsarClient, ReceiverOptions<M> receiverOptions) {
    return new DefaultPulsarReceiver<>(reactorPulsarClient, receiverOptions);
  }

  Flux<ReceiverMessage<M>> receive();

  void ack(MessageId messageId);

  void nack(MessageId messageId);
}
