package reactor.pulsar.sender.internals;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.sender.PulsarRecord;
import reactor.pulsar.sender.PulsarSender;
import reactor.pulsar.sender.SenderOptions;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultPulsarSender<M> implements PulsarSender<M>, EmitFailureHandler {

    private final Scheduler scheduler;
    private final AtomicBoolean hasProducer;
    private final Mono<Producer<M>> producerMono;

    public DefaultPulsarSender(ReactorPulsarClient reactorPulsarClient, SenderOptions<M> senderOptions) {
        this.scheduler = Schedulers.newSingle(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setName("reactor-pulsar-sender-" + System.identityHashCode(this));
                return thread;
            }
        });
        this.hasProducer = new AtomicBoolean();
        this.producerMono = reactorPulsarClient.client().flatMap(pulsarClient -> {
            return Mono.fromCompletionStage(() -> {
                        return pulsarClient
                                .newProducer(senderOptions.schema())
                                .loadConf(senderOptions.properties())
                                .createAsync();
                    })
                    .doOnSubscribe(__ -> hasProducer.set(true))
                    .cache();
        });
    }

    public Flux<MessageId> send(Publisher<? extends PulsarRecord<M>> records) {
        return producerMono.flatMapMany(producer -> {
            return Flux.from(records).publishOn(scheduler).flatMap(record -> {
                return Mono.fromCompletionStage(() -> {
                    return producer.newMessage()
                            .key(record.getKey())
                            .value(record.getValue())
                            .sendAsync();
                });
            });
        });
    }

    @Override
    public void close() {
        producerMono
                .flatMap(producer -> {
                    return Mono.fromCompletionStage(producer::closeAsync).flatMap(__ -> {
                        hasProducer.set(false);
                        return Mono.empty();
                    });
                })
                .block();
    }

    @Override
    public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
        return hasProducer.get();
    }
}
