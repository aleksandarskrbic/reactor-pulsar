package reactor.pulsar.receiver.internals;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Messages;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerHandler<M> {

  private Consumer<M> consumer;
  private final Sinks.Many<Messages<M>> sink = Sinks.many().unicast().onBackpressureBuffer();
  private final AtomicBoolean shouldRun = new AtomicBoolean(true);
  private final Future<?> eventLoop;
  // private Function<> decider ??? message acknowledger???

  public ConsumerHandler(Consumer<M> consumer) {
    this.consumer = consumer;
    this.eventLoop = Executors.newSingleThreadExecutor().submit(this::consumerLoop);
  }

  public Flux<Messages<M>> receive() {
    return sink.asFlux();
  }

  public Mono<Void> stop() {
    return Mono.defer(
        () -> {
          shouldRun.set(false);
          eventLoop.cancel(true);
          return Mono.empty();
        });
  }

  private void consumerLoop() {
    while (true) {
      if (shouldRun.get()) {
        // receive vs batchReceive?
        CompletableFuture<Messages<M>> future = consumer.batchReceiveAsync();
        future.whenComplete((messages, error) -> sink.tryEmitNext(messages));
      }
    }
  }
}
