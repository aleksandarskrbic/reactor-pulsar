package reactor.pulsar.sender;

import org.apache.pulsar.client.api.Schema;

import java.util.Map;

public class ImmutableSenderOptions<M> implements SenderOptions<M> {

    private final Map<String, Object> properties;
    private final Schema<M> schema;

    public ImmutableSenderOptions(Map<String, Object> properties, Schema<M> schema) {
        this.properties = properties;
        this.schema = schema;
    }

    @Override
    public Map<String, Object> properties() {
        return properties;
    }

    @Override
    public Schema<M> schema() {
        return schema;
    }
}
