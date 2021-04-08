package reactor.pulsar.sender;

public class PulsarRecord<V> {

    private String key;
    private V value;

    public static <V> PulsarRecord<V> create(String key, V value) {
        return new PulsarRecord<>(key, value);
    }

    private PulsarRecord(String key, V value) {
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
