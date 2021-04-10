package reactor.pulsar.receiver.internals;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

public final class ReceiverRecord<M> {

  public enum State {
    RECEIVED,
    ACKNOWLEDGE,
    NEGATIVE_ACKNOWLEDGE
  }

  private final Consumer<M> consumer;
  private final Message<M> message;
  private final State state;

  static <M> ReceiverRecord<M> create(Consumer<M> consumer, Message<M> message) {
    return new ReceiverRecord<>(consumer, message, State.RECEIVED);
  }

  private ReceiverRecord(Consumer<M> consumer, Message<M> message, State state) {
    this.consumer = consumer;
    this.message = message;
    this.state = state;
  }

  // Probably bad to include consumer in this class for ack/nack, must be better way
  public ReceiverRecord<M> acknowledge() {
    consumer.acknowledgeAsync(message);
    return new ReceiverRecord<>(null, message, State.ACKNOWLEDGE);
  }

  public ReceiverRecord<M> negativeAcknowledge() {
    consumer.negativeAcknowledge(message);
    return new ReceiverRecord<>(null, message, State.NEGATIVE_ACKNOWLEDGE);
  }

  public Message<M> message() {
    return message;
  }

  public State state() {
    return state;
  }
}
