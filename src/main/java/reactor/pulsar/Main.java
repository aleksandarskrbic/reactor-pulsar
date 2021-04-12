package reactor.pulsar;

import org.apache.pulsar.client.api.*;
import reactor.core.publisher.Flux;
import reactor.pulsar.client.ClientOptions;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.receiver.PulsarReceiver;
import reactor.pulsar.receiver.ReceiverOptions;
import reactor.pulsar.sender.SenderMessage;
import reactor.pulsar.sender.PulsarSender;
import reactor.pulsar.sender.SenderOptions;

import java.util.*;

public class Main {

  public static void main(String[] args) throws InterruptedException {

    String topic = "tt6";
    Map<String, Object> options = Map.of("serviceUrl", "pulsar://localhost:6650");
    ClientOptions clientOptions = ClientOptions.create(options);
    ReactorPulsarClient client = ReactorPulsarClient.create(clientOptions);
    SenderOptions<String> senderOptions =
        SenderOptions.create(Map.of("topicName", topic), Schema.STRING);
    PulsarSender<String> sender = PulsarSender.create(client, senderOptions);

    Flux<SenderMessage<String>> recordFlux =
        Flux.just(
            SenderMessage.create("Key1", "Value1"),
            SenderMessage.create("Key2", "Value2"),
            SenderMessage.create("Key3", "Value3"));

    Flux<MessageId> sent = sender.send(recordFlux);
    sent.subscribe(messageId -> System.out.println("Sent: " + messageId));

    Map<String, Object> serviceUrl =
        Map.of("serviceUrl", "pulsar://localhost:6650", "negativeAckRedeliveryDelayMicros", 2);

    ReceiverOptions<String> opts =
        ReceiverOptions.create(
                Map.of(
                    "serviceUrl", "pulsar://localhost:6650", "negativeAckRedeliveryDelayMicros", 2),
                Schema.STRING)
            .withSubscription(Collections.singleton(topic))
            .withSubscriptionName(topic + "-consumer");

    PulsarReceiver<String> pulsarReceiver = PulsarReceiver.create(client, opts);
    List<String> r = new ArrayList<>();

    pulsarReceiver
        .receive()
        .map(
            record -> {
              if (r.contains(record.message().getValue())) {
                System.out.println("Message redelivered: " + record.message().getValue());
              }
              r.add(record.message().getValue());
              record.committable().negativeAcknowledge();
              return record;
            })
        .subscribe(a -> System.out.println("Received: " + a.message().getValue()));
  }
}
