package com.dodamsoft.todayfarmhub.dto;

import com.dodamsoft.todayfarmhub.entity.*;
import com.dodamsoft.todayfarmhub.repository.LClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MClassCodeRepository;
import com.dodamsoft.todayfarmhub.repository.MarketCodeRepository;
import com.dodamsoft.todayfarmhub.repository.SClassCodeRepository;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuctionAPIDto {

    @SerializedName("response")
    private Response response;

    /**
     * 총 데이터 건수 (response.body.totalCount에서 가져옴)
     */
    public int getTotCnt() {
        if (response != null && response.body != null) {
            return response.body.totalCount;
        }
        return 0;
    }

    /**
     * 결과 리스트 (response.body.items.item에서 가져옴)
     */
    public List<ResultList> getResultList() {
        if (response != null && response.body != null &&
                response.body.items != null && response.body.items.item != null) {
            return response.body.items.item;
        }
        return new ArrayList<>();
    }

    /**
     * 총 페이지 수 계산 (pageSize 단위)
     */
    public int getTotalPage(int pageSize) {
        if (pageSize <= 0) return 1;
        int totalCount = getTotCnt();
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    @Data
    public static class Response {
        @SerializedName("body")
        private Body body;

        @SerializedName("header")
        private Header header;
    }

    @Data
    public static class Header {
        @SerializedName("resultCode")
        private String resultCode;

        @SerializedName("resultMsg")
        private String resultMsg;
    }

    @Data
    public static class Body {
        @SerializedName("dataType")
        private String dataType;

        @SerializedName("items")
        private Items items;

        @SerializedName("numOfRows")
        private int numOfRows;

        @SerializedName("pageNo")
        private int pageNo;

        @SerializedName("totalCount")
        private int totalCount;
    }

    @Data
    public static class Items {
        @SerializedName("item")
        private List<ResultList> item;
    }

    /**
     * API 결과 리스트 내부 클래스
     */
    @Data
    public static class ResultList {

        // 기존 필드명 유지 (DB 저장용)
        private String bidtime;
        private String coco;
        private String cocode;
        private String coname;
        private String dates;
        private Integer price;
        private String sanco;
        private String sanji;
        private String unitname;
        private Integer tradeamt;
        private String lclasscode;
        private String mclasscode;
        private String sclasscode;
        private String marketcode;

        // API 응답 필드 매핑
        @SerializedName("scsbd_dt")
        private String scsbd_dt;  // 낙찰일시 → bidtime 변환 필요

        @SerializedName("whsl_mrkt_cd")
        private String whsl_mrkt_cd;  // 도매시장코드 → marketcode

        @SerializedName("corp_cd")
        private String corp_cd;  // 법인코드 → cocode

        @SerializedName("corp_nm")
        private String corp_nm;  // 법인명 → coname

        @SerializedName("trd_clcln_ymd")
        private String trd_clcln_ymd;  // 거래정산일자 → dates

        @SerializedName("scsbd_prc")
        private String scsbd_prc;  // 낙찰가격 → price

        @SerializedName("plor_cd")
        private String plor_cd;  // 산지코드 → sanco

        @SerializedName("plor_nm")
        private String plor_nm;  // 산지명 → sanji

        @SerializedName("unit_nm")
        private String unit_nm;  // 단위명 → unitname

        @SerializedName("unit_tot_qty")
        private String unit_tot_qty;  // 단위총수량 → tradeamt

        @SerializedName("gds_lclsf_cd")
        private String gds_lclsf_cd;  // 상품대분류코드 → lclasscode

        @SerializedName("gds_mclsf_cd")
        private String gds_mclsf_cd;  // 상품중분류코드 → mclasscode

        @SerializedName("gds_sclsf_cd")
        private String gds_sclsf_cd;  // 상품소분류코드 → sclasscode

        // 추가 필드들
        @SerializedName("auctn_seq")
        private String auctn_seq;

        @SerializedName("auctn_seq2")
        private String auctn_seq2;

        @SerializedName("corp_gds_cd")
        private String corp_gds_cd;

        @SerializedName("corp_gds_item_nm")
        private String corp_gds_item_nm;

        @SerializedName("corp_gds_vrty_nm")
        private String corp_gds_vrty_nm;

        @SerializedName("gds_lclsf_nm")
        private String gds_lclsf_nm;

        @SerializedName("gds_mclsf_nm")
        private String gds_mclsf_nm;

        @SerializedName("gds_sclsf_nm")
        private String gds_sclsf_nm;

        @SerializedName("grd_cd")
        private String grd_cd;

        @SerializedName("grd_nm")
        private String grd_nm;

        @SerializedName("pkg_cd")
        private String pkg_cd;

        @SerializedName("pkg_nm")
        private String pkg_nm;

        @SerializedName("qty")
        private String qty;

        @SerializedName("spm_no")
        private String spm_no;

        @SerializedName("spmt_se")
        private String spmt_se;

        @SerializedName("sz_cd")
        private String sz_cd;

        @SerializedName("sz_nm")
        private String sz_nm;

        @SerializedName("totprc")
        private String totprc;

        @SerializedName("trd_se")
        private String trd_se;

        @SerializedName("unit_cd")
        private String unit_cd;

        @SerializedName("unit_qty")
        private String unit_qty;

        @SerializedName("whsl_mrkt_nm")
        private String whsl_mrkt_nm;

        /**
         * API 응답 필드를 기존 필드명으로 변환
         */
        public void mapToLegacyFields() {
            this.bidtime = this.scsbd_dt;
            this.marketcode = this.whsl_mrkt_cd;
            this.cocode = this.corp_cd;
            this.coname = this.corp_nm;
            this.dates = this.trd_clcln_ymd;
            this.sanco = this.plor_cd;
            this.sanji = this.plor_nm;
            this.unitname = this.unit_nm;
            this.lclasscode = this.gds_lclsf_cd;
            this.mclasscode = this.gds_mclsf_cd;
            this.sclasscode = this.gds_sclsf_cd;
            this.coco = this.corp_cd;

            // 숫자 변환
            if (this.scsbd_prc != null) {
                this.price = Double.valueOf(this.scsbd_prc).intValue();
            }
            if (this.unit_tot_qty != null) {
                this.tradeamt = Double.valueOf(this.unit_tot_qty).intValue();
            }
            // 예외 처리: scsbd_dt가 null이면 trd_clcln_ymd + " 23:00:00"
            if (this.bidtime == null && this.trd_clcln_ymd != null) {
                this.bidtime = this.trd_clcln_ymd + " 23:00:00";
            }
        }

        public Prices toEntity(LClassCodeRepository lClassCodeRepository,
                               MClassCodeRepository mClassCodeRepository,
                               SClassCodeRepository sClassCodeRepository,
                               MarketCodeRepository marketCodeRepository) {

            LClassCode oneBylclasscode = lClassCodeRepository.findOneBylclasscode(this.getLclasscode());
            MClassCode oneBylClassCodeAndMclasscode = mClassCodeRepository.findOneBylClassCodeAndMclasscode(oneBylclasscode, this.getMclasscode());
            SClassCode oneBysclasscode = sClassCodeRepository.findOneBysclasscode(oneBylclasscode.getId(), oneBylClassCodeAndMclasscode.getId(), this.getSclasscode());
            MarketCode oneByMarketCode = marketCodeRepository.findOneByMarketCode(this.getMarketcode());
            return Prices.builder()
                    .bidtime(this.getBidtime())
                    .coco(this.getCoco())
                    .cocode(this.getCocode())
                    .coname(this.getConame())
                    .dates(this.getDates())
                    .price(this.getPrice())
                    .sanco(this.getSanco())
                    .sanji(this.getSanji())
                    .unitname(this.getUnitname())
                    .tradeamt(String.valueOf(this.getTradeamt()))
                    .lClassCode(oneBylclasscode)
                    .mClassCode(oneBylClassCodeAndMclasscode)
                    .sClassCode(oneBysclasscode)
                    .marketCode(oneByMarketCode)
                    .build();

        }

    }
}