package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.entity.LClassCode;
import com.dodamsoft.todayfarmhub.entity.MClassCode;
import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;

public interface GetAuctionCategoryService {

    public <T> T getCategory(AuctionPriceVO auctionPriceVO) throws InterruptedException;

    public <T> void saveInfoByResponseDataUsingAPI(T t, LClassCode lClassCode, MClassCode mClassCode) throws InterruptedException;

}
