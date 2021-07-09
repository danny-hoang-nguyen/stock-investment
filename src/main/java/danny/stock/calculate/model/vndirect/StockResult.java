package danny.stock.calculate.model.vndirect;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockResult {
  private String code;
  private String type;
  private String floor;
  private String isin;
  private String status;
  private String companyName;
  private String companyNameEng;
  private String shortName;
  private String listedDate;
  private String companyId;
}
