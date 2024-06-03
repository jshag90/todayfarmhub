package com.dodamsoft.todayfarmhub.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 소분류 모음 테이블
 */
@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    String sclasscode;
    String sclassname;

    @ManyToOne(targetEntity = MClassCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "m_class_code_id")
    MClassCode mClassCode;

    @ManyToOne(targetEntity = LClassCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "l_class_code_id")
    LClassCode lClassCode;


}
