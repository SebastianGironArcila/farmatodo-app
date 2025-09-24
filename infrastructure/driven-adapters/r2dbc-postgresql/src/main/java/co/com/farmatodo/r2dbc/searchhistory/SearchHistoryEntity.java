package co.com.farmatodo.r2dbc.searchhistory;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("search_history")
public class SearchHistoryEntity implements Persistable<String> {
    @Id
    @Column("id")
    private String id;

    @Column("search_term")
    private String searchTerm;

    @Column("search_timestamp")
    private LocalDateTime searchTimestamp;


    @Transient
    private boolean isNew = true;


    @Override
    public boolean isNew() {
        return this.isNew;
    }
}


