package com.ecommerce.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query(value = """
            SELECT * FROM product
            WHERE
                title ~* CONCAT('\\y', :query, '\\y') OR
                description ~* CONCAT('\\y', :query, '\\y') OR
                brand ~* CONCAT('\\y', :query, '\\y') OR
                color ~* CONCAT('\\y', :query, '\\y') OR
                EXISTS (
                    SELECT 1 FROM categories
                    WHERE categories.id = product.category_id
                    AND categories.name ~* CONCAT('\\y', :query, '\\y')
                )
            """, nativeQuery = true)
    List<Product> searchProduct(@Param("query") String query);

    @Query(value = """
            SELECT p
            FROM Product p
            JOIN p.category c
            WHERE LOWER(c.name) = LOWER(:categoryName)
            """)
    List<Product> findProductsByCategoryName(@Param("categoryName") String categoryName);

//    Old filter
//    @Query("SELECT p FROM Product p " +
//            "WHERE (p.category.name = :category OR :category = '') " +
//            "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR (p.discountedPrice BETWEEN :minPrice AND :maxPrice)) " +
//            "AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount) " +
//            "ORDER BY " +
//            "CASE WHEN :sort = 'price_low' THEN p.discountedPrice END ASC, " +
//            "CASE WHEN :sort = 'price_high' THEN p.discountedPrice END DESC")
//    List<Product> filterProducts(
//            @Param("category") String category,
//            @Param("minPrice") Integer minPrice,
//            @Param("maxPrice") Integer maxPrice,
//            @Param("minDiscount") Integer minDiscount,
//            @Param("sort") String sort
//    );

    //    New filter
    @Query("SELECT p FROM Product p " +
            "WHERE (:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(p.color) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR (p.discountedPrice BETWEEN :minPrice AND :maxPrice)) " +
            "AND (:minDiscount IS NULL OR p.discountPercent >= :minDiscount) " +
            "ORDER BY " +
            "CASE WHEN :sort = 'price_low' THEN p.discountedPrice END ASC, " +
            "CASE WHEN :sort = 'price_high' THEN p.discountedPrice END DESC")
    List<Product> filterProducts(
            @Param("query") String query,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            @Param("minDiscount") Integer minDiscount,
            @Param("sort") String sort
    );

    // Getting all featured products
    List<Product> findByIsFeaturedTrue();

    // Getting the most recent products added to the catalog
    @Query("SELECT p FROM Product p WHERE p.createdAt >= :fromDate ORDER BY p.createdAt DESC")
    List<Product> findNewArrivals(@Param("fromDate") LocalDateTime fromDate);

    // Getting all discounted products
    @Query("SELECT p FROM Product p WHERE p.discountPercent > 0 ORDER BY p.discountPercent DESC")
    List<Product> findDiscountedProducts();

    // Getting all top-rated products
    @Query("SELECT p FROM Product p WHERE p.totalRatings > 0 ORDER BY p.averageRating DESC")
    List<Product> findTopRatedProducts();

//    @Query(value = """
//    WITH RECURSIVE category_hierarchy AS (
//        -- Start with the category that matches the query
//        SELECT id, name, parent_category_id
//        FROM categories
//        WHERE name ~* CONCAT('\\y', :query, '\\y')
//        UNION ALL
//        -- Recursively find child categories
//        SELECT c.id, c.name, c.parent_category_id
//        FROM categories c
//        INNER JOIN category_hierarchy ch ON c.parent_category_id = ch.id
//    )
//    SELECT DISTINCT p.*
//    FROM product p
//    INNER JOIN category_hierarchy ch ON p.category_id = ch.id
//    WHERE
//        p.title ~* CONCAT('\\y', :query, '\\y') OR
//        p.description ~* CONCAT('\\y', :query, '\\y') OR
//        p.brand ~* CONCAT('\\y', :query, '\\y') OR
//        p.color ~* CONCAT('\\y', :query, '\\y') OR
//        ch.name ~* CONCAT('\\y', :query, '\\y')
//    """, nativeQuery = true)
//    List<Product> searchProduct(@Param("query") String query);

}
