package danny.stock.calculate.model.tcb;

import java.math.BigInteger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TickerDetail {

  private Double open;
  private Double high;
  private Double low;
  private Double close;
  private BigInteger volume;
  private String tradingDate;
}
