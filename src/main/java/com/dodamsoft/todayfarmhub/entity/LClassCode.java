package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대분류 코드 테이블
 */

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String lclasscode;
    String lclassname;
}


