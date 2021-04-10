package reactor.pulsar.receiver;

import org.apache.pulsar.client.api.Schema;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface ReceiverOptions<M> {

  @NonNull
  static <M> ReceiverOptions<M> create(
      @NonNull Map<String, Object> properties, @NonNull Schema<M> schema) {
    return new ImmutableReceiverOptions<>(properties, schema);
  }

  Map<String, Object> properties();

  Schema<M> schema();

  ReceiverOptions<M> withSubscription(Collection<String> topics);

  List<String> subscriptions();

  ReceiverOptions<M> withSubscriptionName(String subscriptionName);

  String subscriptionName();

  Supplier<Scheduler> schedulerSupplier();
}
