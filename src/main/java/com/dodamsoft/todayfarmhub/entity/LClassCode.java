package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * 대분류 코드 테이블
 */

@Entity
public class LClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    String lclasscode;
    String lclassname;
}


