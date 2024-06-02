package com.dodamsoft.todayfarmhub.service;

import com.dodamsoft.todayfarmhub.vo.AuctionPriceVO;
import org.springframework.stereotype.Service;

public interface GetAuctionCategoryService {

    public <T> T getCategory(AuctionPriceVO auctionPriceVO);

}
