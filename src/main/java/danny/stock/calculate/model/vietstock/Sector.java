package danny.stock.calculate.model.vietstock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Sector {

  @JsonProperty("_sc_")
  private String ticker;
  @JsonProperty("_in_")
  private String group;
  @JsonProperty("_vhtt_")
  private Double capital;

  @Override
  public String toString() {
    return group+"|"+ticker+"|"+capital;
  }
}
