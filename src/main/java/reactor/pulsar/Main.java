package reactor.pulsar;

import org.apache.pulsar.client.api.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws PulsarClientException {
        final Mono<String> objectMono = Mono.fromCallable(() -> null);
        final HashMap<String, Object> map = new HashMap<>();
        map.put("serviceUrl", "pulsar://localhost:6650");
        final PulsarClient client = PulsarClient.builder().loadConf(map).build();
        final Producer<String> producer =
                client.newProducer(Schema.STRING).topic("asd").create();

        MessageId send1 =
                producer.newMessage(Schema.STRING).key("asd").value("asd").send();
        producer.flush();
        final MessageId send = producer.send("ASD");

        System.out.println("as");
    }
}
