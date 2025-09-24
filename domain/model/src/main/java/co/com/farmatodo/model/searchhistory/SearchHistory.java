package co.com.farmatodo.model.searchhistory;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class SearchHistory {
    private String id;
    private String searchTerm;
    private LocalDateTime searchTimestamp;
}
