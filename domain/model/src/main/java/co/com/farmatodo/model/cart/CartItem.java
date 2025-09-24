package co.com.farmatodo.model.cart;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CartItem {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}
