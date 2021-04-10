package reactor.pulsar.receiver;

import org.apache.pulsar.client.api.Message;
import reactor.core.publisher.Flux;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.receiver.internals.DefaultPulsarReceiver;
import reactor.pulsar.sender.PulsarSender;
import reactor.pulsar.sender.SenderOptions;
import reactor.pulsar.sender.internals.DefaultPulsarSender;

public interface PulsarReceiver<M> {

  static <M> PulsarReceiver<M> create(
      ReactorPulsarClient reactorPulsarClient, ReceiverOptions<M> receiverOptions) {
    return new DefaultPulsarReceiver<>(reactorPulsarClient, receiverOptions);
  }

  Flux<Message<M>> receive();
}
