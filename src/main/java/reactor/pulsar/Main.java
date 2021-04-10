package reactor.pulsar;

import org.apache.pulsar.client.api.*;
import reactor.core.publisher.Flux;
import reactor.pulsar.client.ClientOptions;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.receiver.PulsarReceiver;
import reactor.pulsar.receiver.ReceiverOptions;
import reactor.pulsar.sender.PulsarRecord;
import reactor.pulsar.sender.PulsarSender;
import reactor.pulsar.sender.SenderOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) throws PulsarClientException, InterruptedException {
    Map<String, Object> options = Map.of("serviceUrl", "pulsar://localhost:6650");
    ClientOptions clientOptions = ClientOptions.create(options);
    ReactorPulsarClient client = ReactorPulsarClient.create(clientOptions);
    SenderOptions<String> senderOptions =
        SenderOptions.create(Map.of("topicName", "topic11"), Schema.STRING);
    PulsarSender<String> sender = PulsarSender.create(client, senderOptions);

    Flux<PulsarRecord<String>> recordFlux =
        Flux.just(
            PulsarRecord.create("Key1", "Value1"),
            PulsarRecord.create("Key2", "Value2"),
            PulsarRecord.create("Key3", "Value3"));

    Flux<MessageId> sent = sender.send(recordFlux);
    sent.subscribe(messageId -> System.out.println("Sent: " + messageId));

    System.out.println(5000);

    ReceiverOptions<String> opts =
        ReceiverOptions.create(Map.of("serviceUrl", "pulsar://localhost:6650"), Schema.STRING)
            .withSubscription(Collections.singleton("topic11"))
            .withSubscriptionName("consumer-11");

    PulsarReceiver<String> pulsarReceiver = PulsarReceiver.create(client, opts);
    pulsarReceiver
        .receive()
        .map(record -> record.negativeAcknowledge())
        .subscribe(a -> System.out.println("Received: " + a.message().getValue()));

    Thread.sleep(60000);
  }
}
