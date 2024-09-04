package ru.smartup.timetracker.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.smartup.timetracker.entity.Notice;
import ru.smartup.timetracker.repository.NoticeBatchRepository;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class NoticeBatchRepositoryImpl implements NoticeBatchRepository {
    private static final int BATCH_SIZE = 100;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void createNotices(List<Notice> notices) {
        String query = "INSERT INTO notice (type, user_id, text, data, created_by) VALUES (?, ?, ?, to_jsonb(?::jsonb), ?)";
        jdbcTemplate.batchUpdate(query, notices, BATCH_SIZE,
                (PreparedStatement ps, Notice notice) -> {
                    ps.setString(1, notice.getType().name());
                    ps.setInt(2, notice.getUserId());
                    ps.setString(3, notice.getText());
                    try {
                        ps.setString(4, objectMapper.writeValueAsString(notice.getData()));
                    } catch (JsonProcessingException e) {
                        log.error("Failed to write notices data", e);
                    }
                    ps.setInt(5, notice.getCreatedBy());
                });
    }
}