package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.*;

/**
 * 중분류 코드 모음
 */
@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String mclasscode;
    String mclassname;
    @ManyToOne(targetEntity = LClassCode.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "l_class_code_id")
    LClassCode lClassCode;
}
