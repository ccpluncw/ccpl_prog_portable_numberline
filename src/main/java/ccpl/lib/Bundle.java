/*
 * This file is part of the Cohen Ray Number Line.
 *
 * Latesco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Latesco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Latesco.  If not, see <http://www.gnu.org/licenses/>.
 */

package ccpl.lib;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Bundle {
  private Map<String, Object> values = new HashMap<>();

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
