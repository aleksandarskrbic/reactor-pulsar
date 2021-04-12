package reactor.pulsar.sender;

public class SenderMessage<V> {

  private String key;
  private V value;

  public static <V> SenderMessage<V> create(String key, V value) {
    return new SenderMessage<>(key, value);
  }

  private SenderMessage(String key, V value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }
}
