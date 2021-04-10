package reactor.pulsar.receiver;

import org.apache.pulsar.client.api.Schema;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.function.Supplier;

public class ImmutableReceiverOptions<M> implements ReceiverOptions<M> {

  private final Map<String, Object> properties;
  private final Schema<M> schema;
  private List<String> topics;
  private final Supplier<Scheduler> schedulerSupplier;
  private String subscriptionName;

  public ImmutableReceiverOptions(Map<String, Object> properties, Schema<M> schema) {
    this.properties = properties;
    this.schema = schema;
    this.schedulerSupplier = Schedulers::immediate;
  }

  private ImmutableReceiverOptions(
      Map<String, Object> properties,
      Schema<M> schema,
      Collection<String> topics,
      String subscriptionName) {
    this.properties = properties;
    this.schema = schema;
    this.topics = new ArrayList<>(topics);
    this.schedulerSupplier = Schedulers::immediate;
    this.subscriptionName = subscriptionName;
  }

  @Override
  public Map<String, Object> properties() {
    return properties;
  }

  @Override
  public Schema<M> schema() {
    return schema;
  }

  @Override
  public ReceiverOptions<M> withSubscription(Collection<String> topics) {
    return new ImmutableReceiverOptions<>(properties, schema, topics, subscriptionName);
  }

  @Override
  public ReceiverOptions<M> withSubscriptionName(String subscriptionName) {
    return new ImmutableReceiverOptions<>(properties, schema, topics, subscriptionName);
  }

  @Override
  public List<String> subscriptions() {
    return topics;
  }

  @Override
  public String subscriptionName() {
    return subscriptionName;
  }

  @Override
  public Supplier<Scheduler> schedulerSupplier() {
    return schedulerSupplier;
  }
}
