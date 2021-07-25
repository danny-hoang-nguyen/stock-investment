package danny.stock.calculate.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.AbstractMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class SMAIndicatorResult {

  @JsonIgnore
  private Map<String, Double> sma9;
  @JsonIgnore
  private Map<String, Double> sma18;
  @JsonIgnore
  private Map<String, Double> sma40;

  @JsonIgnore
  private boolean sma9Matched;
  @JsonIgnore
  private boolean sma18Matched;
  @JsonIgnore
  private boolean sma40Matched;

  private Map<String, Double> latestClosePrice;
  private AbstractMap.SimpleEntry<String, Double> buyPrice;
  private Double sellPrice;
  private String code;

  public void determineEnterPrice() {
    if (!(latestClosePrice.values().stream().findFirst().get() > buyPrice.getValue() - 7000)) {
      System.out.println(code + "| ENTER ::: " + latestClosePrice.values().stream().findFirst().get());
    }
  }
}
