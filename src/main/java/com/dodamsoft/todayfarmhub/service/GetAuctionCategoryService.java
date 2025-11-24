package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.util.CategoryType;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;

public interface GetAuctionCategoryService {

    boolean isType(CategoryType categoryType);

    <T> T getCategory(AuctionPriceVO auctionPriceVO) throws InterruptedException;

    <T> void saveInfoByResponseDataUsingAPI(LClassCode lClassCode, MClassCode mClassCode) throws InterruptedException;

}
