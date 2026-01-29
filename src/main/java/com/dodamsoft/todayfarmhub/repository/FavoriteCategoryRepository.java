package com.dodamsoft.todayfarmhub.repository;

import com.dodamsoft.todayfarmhub.entity.FavoriteCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteCategoryRepository extends JpaRepository<FavoriteCategory, Long> {
    Optional<FavoriteCategory> findByLclassCodeAndMclassCodeAndSclassCodeAndMarketCode(
            String lclassCode, String mclassCode, String sclassCode, String marketCode);

    List<FavoriteCategory> findTop10ByOrderByViewCountDesc();
}
