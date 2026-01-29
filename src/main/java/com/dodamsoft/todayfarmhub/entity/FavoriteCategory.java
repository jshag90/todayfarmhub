package com.dodamsoft.todayfarmhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "favorite_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lclass_code")
    private String lclassCode;

    @Column(name = "mclass_code")
    private String mclassCode;

    @Column(name = "sclass_code")
    private String sclassCode;

    @Column(name = "market_code")
    private String marketCode;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0L;
        }
        this.viewCount++;
    }
}
