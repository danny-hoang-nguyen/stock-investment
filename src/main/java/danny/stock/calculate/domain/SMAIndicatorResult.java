package danny.stock.calculate.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private boolean sma9Matched;
  private boolean sma18Matched;
  private boolean sma40Matched;

  private Map<String, Double> latestClosePrice;
  private Double buyPrice;
  private String code;
}
