package reactor.pulsar.receiver.internals;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.MessageId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.pulsar.client.ReactorPulsarClient;
import reactor.pulsar.receiver.PulsarReceiver;
import reactor.pulsar.receiver.ReceiverMessage;
import reactor.pulsar.receiver.ReceiverOptions;

import java.util.function.BiFunction;

public class DefaultPulsarReceiver<M> implements PulsarReceiver<M> {

  static final Logger log = LoggerFactory.getLogger(DefaultPulsarReceiver.class.getName());

  private final ReactorPulsarClient reactorPulsarClient;
  private final ReceiverOptions<M> receiverOptions;
  private final Mono<Consumer<M>> consumerMono;
  private ConsumerHandler<M> consumerHandler;

  public DefaultPulsarReceiver(
      ReactorPulsarClient reactorPulsarClient, ReceiverOptions<M> receiverOptions) {
    this.reactorPulsarClient = reactorPulsarClient;
    this.receiverOptions = receiverOptions;
    this.consumerMono =
        reactorPulsarClient
            .client()
            .flatMap(
                pulsarClient -> {
                  log.info("Creating consumer");
                  return Mono.fromCompletionStage(
                      () -> {
                        return pulsarClient
                            .newConsumer(receiverOptions.schema())
                            .topics(receiverOptions.subscriptions())
                            .subscriptionName(receiverOptions.subscriptionName())
                            .subscribeAsync();
                      });
                });
  }

  public Consumer<M> get() {
    return consumerMono.block();
  }

  @Override
  public Flux<ReceiverMessage<M>> receive() {
    return withHandler((scheduler, handler) -> handler.receive().publishOn(scheduler));
  }

  @Override
  public void ack(MessageId messageId) {
    consumerMono
        .flatMap(consumer -> Mono.fromCompletionStage(() -> consumer.acknowledgeAsync(messageId)))
        .then()
        .subscribe();
  }

  @Override
  public void nack(MessageId messageId) {
    consumerMono
        .flatMap(consumer -> Mono.fromRunnable(() -> consumer.negativeAcknowledge(messageId)))
        .subscribe();
  }

  private Flux<ReceiverMessage<M>> withHandler(
      BiFunction<Scheduler, ConsumerHandler<M>, Flux<ReceiverMessage<M>>> fn) {
    return Flux.usingWhen(
        consumerMono.flatMap(
            consumer -> Mono.fromCallable(() -> consumerHandler = new ConsumerHandler<>(consumer))),
        handler ->
            Flux.using(
                () -> Schedulers.single(receiverOptions.schedulerSupplier().get()),
                scheduler -> fn.apply(scheduler, handler),
                Scheduler::dispose),
        handler -> handler.stop().doFinally(__ -> consumerHandler = null));
  }
}
