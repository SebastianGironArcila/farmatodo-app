package co.com.farmatodo.model.card;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Card {
    private String cardNumber;
    private String cvv;
    private String expirationDate;
    private String email;
}
