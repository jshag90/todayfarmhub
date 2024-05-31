package com.dodamsoft.todayfarmhub.dto;

import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@ToString
public class LClassAPIDto {

    List<ResultList> resultList;

    @Builder
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultList {
        String lclasscode;
        String lclassname;
    }
}
