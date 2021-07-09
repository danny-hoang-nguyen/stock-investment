package danny.stock.calculate.model.vndirect;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ListedStockResult {
private List<StockResult> data;
private Integer currentPage;
private Integer size;
private Integer totalElements;
private Integer totalPages;
/*
   "currentPage": 1,
    "size": 3000,
    "totalElements": 1659,
    "totalPages": 1
 */
 }
