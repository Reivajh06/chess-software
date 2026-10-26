package reivajh06.api.ports;

@FunctionalInterface
public interface OutputSerializer<O> {

	String serialize(O data);
}
