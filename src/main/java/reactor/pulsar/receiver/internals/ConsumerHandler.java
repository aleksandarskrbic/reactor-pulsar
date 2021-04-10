package reactor.pulsar.receiver.internals;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumerHandler<M> {

  private final Consumer<M> consumer;
  private final Sinks.Many<ReceiverRecord<M>> sink = Sinks.many().unicast().onBackpressureBuffer();
  private final AtomicBoolean shouldRun = new AtomicBoolean(true);
  private final Future<?> eventLoop;
  // private Function<> decider ??? message acknowledger???

  public ConsumerHandler(Consumer<M> consumer) {
    this.consumer = consumer;
    // Use Scheduler.schedule() instead of ExecutorService ???
    this.eventLoop = Executors.newSingleThreadExecutor().submit(this::consumerLoop);
  }

  public Flux<ReceiverRecord<M>> receive() {
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
        // receive vs batchReceive? introduce enum consumer_mode, micro-batch vs. streaming
        CompletableFuture<Message<M>> future = consumer.receiveAsync();
        future.whenComplete(
            (message, error) -> sink.tryEmitNext(ReceiverRecord.create(consumer, message)));
      }
    }
  }
}
