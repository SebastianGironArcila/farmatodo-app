package co.com.farmatodo.model.client;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Client {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
}
