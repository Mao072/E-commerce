package com.esunbank.ecommerce.repository;

import com.esunbank.ecommerce.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Member> memberRowMapper = (ResultSet rs, int rowNum) -> {
        Member member = new Member();
        member.setMemberId(rs.getString("member_id"));
        member.setUsername(rs.getString("username"));
        member.setPassword(rs.getString("password"));
        member.setRole(rs.getString("role"));
        return member;
    };

    public Optional<Member> findByUsername(String username) {
        try {
            Member member = jdbcTemplate.queryForObject(
                    "CALL sp_get_member_by_username(?)",
                    memberRowMapper,
                    username);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Map<String, Object> register(String memberId, String username, String encodedPassword, String role) {
        Map<String, Object> result = new HashMap<>();

        jdbcTemplate.update(
                "CALL sp_register_member(?, ?, ?, ?, @p_result, @p_message)",
                memberId,
                username,
                encodedPassword,
                role != null ? role : "USER");

        Map<String, Object> outParams = jdbcTemplate.queryForMap(
                "SELECT @p_result as result, @p_message as message");

        result.put("result", ((Number) outParams.get("result")).intValue());
        result.put("message", outParams.get("message"));

        return result;
    }
}
