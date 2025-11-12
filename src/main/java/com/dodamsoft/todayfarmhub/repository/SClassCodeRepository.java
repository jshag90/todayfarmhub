package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SClassCodeRepository extends JpaRepository<SClassCode, Long> {

    // 1. 존재 여부 (Integer 반환)
    @Query(value = "SELECT COUNT(*) FROM sclass_code " +
            "WHERE l_class_code_id = :lClassId " +
            "AND m_class_code_id = :mClassId",
            nativeQuery = true)
    Integer countByLClassCodeAndMClassCode(
            @Param("lClassId") Long lClassId,
            @Param("mClassId") Long mClassId
    );

    // 2. 전체 조회 + 정렬
    @Query(value = "SELECT * FROM sclass_code " +
            "WHERE l_class_code_id = :lClassId " +
            "AND m_class_code_id = :mClassId " +
            "ORDER BY sclassname ASC",
            nativeQuery = true)
    List<SClassCode> findAllByLClassCodeAndMClassCode(
            @Param("lClassId") Long lClassId,
            @Param("mClassId") Long mClassId
    );

    // 3. 단건 조회
    @Query(value = "SELECT * FROM sclass_code WHERE sclasscode = ?1 LIMIT 1",
            nativeQuery = true)
    Optional<SClassCode> findBySclasscode(String sclasscode);

    // 4. 호환성 메서드
    default SClassCode findOneBysclasscode(Long lClassCodeId, Long mClassCodeId, String sclasscode) {
        return findByLClassCodeIdAndMClassCodeIdAndSclasscode(lClassCodeId, mClassCodeId, sclasscode);
    }

    // 5. 유일성 체크 (Integer 반환)
    @Query(value = "SELECT COUNT(*) FROM sclass_code " +
            "WHERE l_class_code_id = :lClassId " +
            "AND m_class_code_id = :mClassId " +
            "AND sclasscode = :sclasscode",
            nativeQuery = true)
    Integer countByLClassCodeAndMClassCodeAndSclasscode(
            @Param("lClassId") Long lClassId,
            @Param("mClassId") Long mClassId,
            @Param("sclasscode") String sclasscode
    );

    @Query("SELECT s FROM SClassCode s " +
            "WHERE s.lClassCode.id = :lId " +
            "  AND s.mClassCode.id = :mId " +
            "  AND s.sclasscode = :sCode")
    SClassCode findByLClassCodeIdAndMClassCodeIdAndSclasscode(
            @Param("lId") Long lClassCodeId,
            @Param("mId") Long mClassCodeId,
            @Param("sCode") String sClassCode
    );

}