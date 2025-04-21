package test.crudboard.repository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BackUpRepository {
    private final JdbcTemplate template;

    @Transactional
    public void jdbcBatchUpdate(Map<Long, Long> postViews) {
        if (postViews.isEmpty()) return;

        StringBuilder sql = new StringBuilder("UPDATE post SET view = CASE post_id ");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : postViews.entrySet()) {
            sql.append(" WHEN ? THEN ? ");
            params.add(entry.getKey());
            params.add(entry.getValue());
        }

        sql.append(" ELSE view END WHERE post_id IN (");
        postViews.keySet().forEach(id -> sql.append("?,"));
        sql.deleteCharAt(sql.length() - 1);  // 마지막 쉼표 제거
        sql.append(")");

        params.addAll(postViews.keySet());
        template.update(sql.toString(), params.toArray());
    }
}
