package com.dodamsoft.todayfarmhub.dto;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.entity.MarketCode;
import com.dodamsoft.todayfarmhub.entity.Prices;
import com.dodamsoft.todayfarmhub.entity.SClassCode;
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
     * 珥??곗씠??嫄댁닔 (response.body.totalCount?먯꽌 媛?몄샂)
     */
    public int getTotCnt() {
        if (response != null && response.body != null) {
            return response.body.totalCount;
        }
        return 0;
    }

    /**
     * 寃곌낵 由ъ뒪??(response.body.items.item?먯꽌 媛?몄샂)
     */
    public List<ResultList> getResultList() {
        if (response != null && response.body != null &&
                response.body.items != null && response.body.items.item != null) {
            return response.body.items.item;
        }
        return new ArrayList<>();
    }

    /**
     * 珥??섏씠吏 ??怨꾩궛 (pageSize ?⑥쐞)
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
     * API 寃곌낵 由ъ뒪???대? ?대옒??
     */
    @Data
    public static class ResultList {

        // 湲곗〈 ?꾨뱶紐??좎? (DB ??μ슜)
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

        // API ?묐떟 ?꾨뱶 留ㅽ븨
        @SerializedName("scsbd_dt")
        private String scsbd_dt;  // ?숈같?쇱떆 ??bidtime 蹂???꾩슂

        @SerializedName("whsl_mrkt_cd")
        private String whsl_mrkt_cd;  // ?꾨ℓ?쒖옣肄붾뱶 ??marketcode

        @SerializedName("corp_cd")
        private String corp_cd;  // 踰뺤씤肄붾뱶 ??cocode

        @SerializedName("corp_nm")
        private String corp_nm;  // 踰뺤씤紐???coname

        @SerializedName("trd_clcln_ymd")
        private String trd_clcln_ymd;  // 嫄곕옒?뺤궛?쇱옄 ??dates

        @SerializedName("scsbd_prc")
        private String scsbd_prc;  // ?숈같媛寃???price

        @SerializedName("plor_cd")
        private String plor_cd;  // ?곗?肄붾뱶 ??sanco

        @SerializedName("plor_nm")
        private String plor_nm;  // ?곗?紐???sanji

        @SerializedName("unit_nm")
        private String unit_nm;  // ?⑥쐞紐???unitname

        @SerializedName("unit_tot_qty")
        private String unit_tot_qty;  // ?⑥쐞珥앹닔????tradeamt

        @SerializedName("gds_lclsf_cd")
        private String gds_lclsf_cd;  // ?곹뭹?遺꾨쪟肄붾뱶 ??lclasscode

        @SerializedName("gds_mclsf_cd")
        private String gds_mclsf_cd;  // ?곹뭹以묐텇瑜섏퐫????mclasscode

        @SerializedName("gds_sclsf_cd")
        private String gds_sclsf_cd;  // ?곹뭹?뚮텇瑜섏퐫????sclasscode

        // 異붽? ?꾨뱶??
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
         * API ?묐떟 ?꾨뱶瑜?湲곗〈 ?꾨뱶紐낆쑝濡?蹂??
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
            this.price = parseNumber(this.scsbd_prc);
            this.tradeamt = parseNumber(resolveTradeAmount());

            if (this.bidtime == null && this.trd_clcln_ymd != null) {
                this.bidtime = this.trd_clcln_ymd + " 23:00:00";
            }
        }

        private String resolveTradeAmount() {
            if (hasText(this.unit_tot_qty)) {
                return this.unit_tot_qty;
            }
            if (hasText(this.qty)) {
                return this.qty;
            }
            return this.unit_qty;
        }

        private Integer parseNumber(String value) {
            if (!hasText(value)) {
                return null;
            }
            return Double.valueOf(value).intValue();
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
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
                    .tradeamt(this.getTradeamt() != null ? String.valueOf(this.getTradeamt()) : null)
                    .lClassCode(oneBylclasscode)
                    .mClassCode(oneBylClassCodeAndMclasscode)
                    .sClassCode(oneBysclasscode)
                    .marketCode(oneByMarketCode)
                    .build();

        }

    }
}
