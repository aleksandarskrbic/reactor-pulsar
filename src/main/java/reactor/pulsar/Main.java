package reactor.pulsar;

import org.apache.pulsar.client.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.pulsar.client.ClientOptions;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.sender.PulsarRecord;
import reactor.pulsar.sender.PulsarSender;
import reactor.pulsar.sender.SenderOptions;
import reactor.tools.agent.ReactorDebugAgent;

import java.util.HashMap;
import java.util.Map;

public class Main {

  public static void main(String[] args) throws PulsarClientException, InterruptedException {
    Map<String, Object> options = Map.of("serviceUrl", "pulsar://localhost:6650");
    ClientOptions clientOptions = ClientOptions.create(options);
    ReactorPulsarClient client = ReactorPulsarClient.create(clientOptions);
    SenderOptions<String> senderOptions =
        SenderOptions.create(Map.of("topicName", "topic5"), Schema.STRING);
    PulsarSender<String> sender = PulsarSender.create(client, senderOptions);

    Flux<PulsarRecord<String>> recordFlux =
        Flux.just(
            PulsarRecord.create("Key1", "Value1"),
            PulsarRecord.create("Key2", "Value2"),
            PulsarRecord.create("Key3", "Value3"));

    Flux<MessageId> sent = sender.send(recordFlux);
    sent.subscribe(messageId -> System.out.println("Poruka " + messageId + " je poslata"));

    System.out.println(5000);

    final HashMap<String, Object> map = new HashMap<>();
    map.put("serviceUrl", "pulsar://localhost:6650");
    final PulsarClient pc = PulsarClient.builder().loadConf(map).build();
    Consumer<String> consumer =
        pc.newConsumer(Schema.STRING)
            .topic("topic5")
            .consumerName("consumer-5")
            .subscriptionName("consumer-5")
            .subscribe();

    int consumed = 0;
    while (consumed < 2) {
      System.out.println("Fetch iteration " + consumed);
      Message<String> receive = consumer.receive();
      consumed++;
      System.out.println(consumed + " primljena " + receive.getValue());
    }
  }
}
