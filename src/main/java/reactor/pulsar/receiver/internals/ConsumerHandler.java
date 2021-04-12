package reactor.pulsar.receiver.internals;

import org.apache.pulsar.client.api.*;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.pulsar.receiver.Committable;
import reactor.pulsar.receiver.ReceiverMessage;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConsumerHandler<M> {

  private final Consumer<M> consumer;
  private final Sinks.Many<ReceiverMessage<M>> sink = Sinks.many().unicast().onBackpressureBuffer();
  private final AtomicBoolean shouldRun = new AtomicBoolean(true);
  private final EventLoop<M> eventLoop;
  // private Future<?> eventLoop;
  // private Function<> decider ??? message acknowledger???

  public ConsumerHandler(Consumer<M> consumer) {
    this.consumer = consumer;
    this.eventLoop = new EventLoop<>(consumer, sink);
    Executors.newSingleThreadExecutor().submit(eventLoop);

    // Use Scheduler.schedule() instead of ExecutorService ???
    // this.eventLoop = Executors.newSingleThreadExecutor().submit(this::consumerLoop);
    // Disposable schedule = Schedulers.immediate().schedule(this::consumerLoop);
  }

  public Flux<ReceiverMessage<M>> receive() {
    return sink.asFlux();
  }

  public Mono<Void> stop() {
    return Mono.defer(
        () -> {
          shouldRun.set(false);
          // eventLoop.cancel(true);
          return Mono.empty();
        });
  }

  private void consumerLoop() {
    while (true) {
      if (shouldRun.get()) {
        try {
          Messages<M> messages = consumer.batchReceive();
          if (messages.size() != 0) {
            System.out.println("Batch size: " + messages.size());
          }
          messages.forEach(
              message -> {
                CommittableMessage<M> committable =
                    new CommittableMessage<>(message.getMessageId(), consumer);

                ReceiverMessage<M> receiverMessage = ReceiverMessage.create(message, committable);

                sink.tryEmitNext(receiverMessage);
              });
        } catch (PulsarClientException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static class EventLoop<M> implements Runnable {

    private final Consumer<M> consumer;
    private final Sinks.Many<ReceiverMessage<M>> sink;

    public EventLoop(Consumer<M> consumer, Sinks.Many<ReceiverMessage<M>> sink) {
      this.consumer = consumer;
      this.sink = sink;
    }

    @Override
    public void run() {
      while (true) {
        try {
          Messages<M> messages = consumer.batchReceive();
          if (messages.size() != 0) {
            System.out.println("Batch size: " + messages.size());
          }
          messages.forEach(
              message -> {
                CommittableMessage<M> committable =
                    new CommittableMessage<>(message.getMessageId(), consumer);

                ReceiverMessage<M> receiverMessage = ReceiverMessage.create(message, committable);

                sink.tryEmitNext(receiverMessage);
              });
        } catch (PulsarClientException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static class CommittableMessage<M> implements Committable {

    private final MessageId messageId;
    private final Consumer<M> consumer;

    public CommittableMessage(MessageId messageId, Consumer<M> consumer) {
      this.messageId = messageId;
      this.consumer = consumer;
    }

    @Override
    public void acknowledge() {
      consumer.acknowledgeAsync(messageId);
    }

    @Override
    public void negativeAcknowledge() {
      consumer.negativeAcknowledge(messageId);
    }
  }
}
