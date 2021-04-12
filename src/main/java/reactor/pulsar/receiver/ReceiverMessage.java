package reactor.pulsar.receiver;

import org.apache.pulsar.client.api.Message;

public final class ReceiverMessage<M> {

  private final Message<M> message;
  private final Committable committable;

  private ReceiverMessage(Message<M> message, Committable committable) {
    this.message = message;
    this.committable = committable;
  }

  public static <M> ReceiverMessage<M> create(Message<M> message, Committable id) {
    return new ReceiverMessage<>(message, id);
  }

  public Message<M> message() {
    return message;
  }

  public Committable committable() {
    return committable;
  }
}
