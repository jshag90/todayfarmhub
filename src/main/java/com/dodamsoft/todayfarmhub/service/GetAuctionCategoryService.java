package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;

public interface GetAuctionCategoryService {

    public <T> T getCategory(AuctionPriceVO auctionPriceVO);

}
