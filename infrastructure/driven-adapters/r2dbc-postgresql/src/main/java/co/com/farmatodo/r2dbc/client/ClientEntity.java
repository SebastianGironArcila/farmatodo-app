package co.com.farmatodo.r2dbc.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("client")
public class ClientEntity {

    @Id
    @Column("id")
    private Integer id;

    @Column("name")
    private String name;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    @Column("address")
    private String address;
}