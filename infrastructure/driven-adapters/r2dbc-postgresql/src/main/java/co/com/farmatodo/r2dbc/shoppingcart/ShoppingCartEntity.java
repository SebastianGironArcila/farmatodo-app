package co.com.farmatodo.r2dbc.shoppingcart;

import co.com.farmatodo.model.cart.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("shopping_cart")
public class ShoppingCartEntity implements Persistable<String> {

    @Id
    @Column("client_id")
    private String clientId;

    @Column("items")
    private Map<String,CartItem> items;

    @Transient
    private boolean isNew;

    @Override
    public String getId() {
        return this.clientId;
    }

    @Override
    @Transient
    public boolean isNew() {
        return this.isNew;
    }

}


