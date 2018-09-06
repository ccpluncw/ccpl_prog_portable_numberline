package ccpl.lib;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Bundle {
  private final Map<String, Object> values = new HashMap<>();

  public void add(String key, Object value) {
    values.put(key, value);
  }

  private Object get(String key) {
    return values.get(key);
  }

  public String getAsString(String key) {
    return get(key).toString();
  }

  public Integer getAsInt(String key) {
    return parseInt(getAsString(key));
  }

  public Boolean getAsBoolean(String key) {
    return parseBoolean(getAsString(key));
  }

  /**
   * Merges two Bundle together, destructively.
   *
   * <p>This function favors values found in the first Bundle.
   *
   * @param secondBundle Second bundle
   * @return New merged Bundle.
   */
  public Bundle merge(Bundle secondBundle) {
    Bundle newBundle = new Bundle();

    secondBundle.values.forEach(newBundle::add);
    values.forEach(newBundle::add);

    return newBundle;
  }

  public boolean contains(String key) {
    return values.containsKey(key);
  }

  public int getSize() {
    return values.size();
  }

  @Override
  public String toString() {
    List<String> vals =
        values
            .entrySet()
            .stream()
            .map(it -> it.getKey() + ": " + it.getValue())
            .collect(Collectors.toCollection(ArrayList::new));

    return String.join("\n", vals);
  }
}
