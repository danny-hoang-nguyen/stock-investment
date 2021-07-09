package danny.stock.calculate.model.tcb;

import java.util.ArrayList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BarsLongTerm {

  private String ticker;
  private ArrayList<TickerDetail> data;
}
