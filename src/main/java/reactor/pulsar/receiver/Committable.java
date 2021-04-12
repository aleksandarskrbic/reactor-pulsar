package reactor.pulsar.receiver;

public interface Committable {

  void acknowledge();

  void negativeAcknowledge();
}
