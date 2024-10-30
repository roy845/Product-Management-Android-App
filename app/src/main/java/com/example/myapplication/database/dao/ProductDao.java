package com.example.myapplication.database.dao;


import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.ProductCountPerMonth;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(Product product);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllProducts(List<Product> products);

    @Update
    void updateProduct(Product product);

    @Delete
    int deleteProduct(Product product);

    @Query("DELETE FROM products")
    int deleteAllProducts();

    // Fetch products with pagination that are not archived
    @Query("SELECT * FROM products WHERE LOWER(title) LIKE LOWER(:query) AND isArchived = 0 LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> searchProductsWithPagination(String query, int offset,int limit);

    // Fetch product count per month for products that are not archived
    @Query("SELECT strftime('%Y-%m', datetime(createdAt / 1000, 'unixepoch')) AS month, " +
            "COUNT(*) AS productCount " +
            "FROM products " +
            "WHERE isArchived = 0 AND createdAt IS NOT NULL AND createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY month " +
            "ORDER BY month ASC")
    LiveData<List<ProductCountPerMonth>> getProductCountPerMonth(long startDate, long endDate);

    // Get total count of products that are not archived
    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 0")
    LiveData<Integer> getTotalProductCount();

    // Fetch products sorted by ascending order, and not archived
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY " +
            "CASE WHEN :sortField = 'name' THEN LOWER(title) END ASC, " +
            "CASE WHEN :sortField = 'id' THEN id END ASC, " +
            "CASE WHEN :sortField = 'createdAt' THEN createdAt END ASC, " +
            "CASE WHEN :sortField = 'price' THEN price END ASC " +
            "LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> loadProductsSortedByAsc(String sortField, int offset, int limit);

    // Fetch products sorted by descending order, and not archived
    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY " +
            "CASE WHEN :sortField = 'name' THEN LOWER(title) END DESC, " +
            "CASE WHEN :sortField = 'id' THEN id END DESC, " +
            "CASE WHEN :sortField = 'createdAt' THEN createdAt END DESC, " +
            "CASE WHEN :sortField = 'price' THEN price END DESC " +
            "LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> loadProductsSortedByDesc(String sortField, int offset, int limit);

    // Fetch products by page that are not archived
    @Query("SELECT * FROM products WHERE isArchived = 0 LIMIT :pageSize OFFSET :offset")
    PagingSource<Integer, Product> loadProductsByPage(int offset, int pageSize);

    // Delete selected products that are not archived
    @Query("DELETE FROM products WHERE isSelected = 1 AND isArchived = 0")
    int deleteSelectedProducts();

    @Query("UPDATE products SET isSelected = :isSelected WHERE id = :productId AND isArchived = 0")
    void updateItemSelected(int productId, boolean isSelected);

    @Query("UPDATE products SET isSelected = :isSelected WHERE isSelected = 1 AND isArchived = 0")
    void updateAllSelectedItems(boolean isSelected);

    @Query("UPDATE products SET isArchived = :isArchived WHERE id = :productId")
    void updateProductArchivedState(int productId, boolean isArchived);


    // ********************** Queries for Archived Products **********************

    // Fetch archived products by pagination
    @Query("SELECT * FROM products WHERE isArchived = 1 LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> loadArchivedProductsByPage(int offset, int limit);

    // Fetch archived products sorted by ascending order
    @Query("SELECT * FROM products WHERE isArchived = 1 ORDER BY " +
            "CASE WHEN :sortField = 'name' THEN LOWER(title) END ASC, " +
            "CASE WHEN :sortField = 'id' THEN id END ASC, " +
            "CASE WHEN :sortField = 'createdAt' THEN createdAt END ASC, " +
            "CASE WHEN :sortField = 'price' THEN price END ASC " +
            "LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> loadArchivedProductsSortedByAsc(String sortField, int offset, int limit);

    // Fetch archived products sorted by descending order
    @Query("SELECT * FROM products WHERE isArchived = 1 ORDER BY " +
            "CASE WHEN :sortField = 'name' THEN LOWER(title) END DESC, " +
            "CASE WHEN :sortField = 'id' THEN id END DESC, " +
            "CASE WHEN :sortField = 'createdAt' THEN createdAt END DESC, " +
            "CASE WHEN :sortField = 'price' THEN price END DESC " +
            "LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> loadArchivedProductsSortedByDesc(String sortField, int offset, int limit);

    // Search archived products by pagination
    @Query("SELECT * FROM products WHERE LOWER(title) LIKE LOWER(:query) AND isArchived = 1 LIMIT :limit OFFSET :offset")
    PagingSource<Integer, Product> searchArchivedProductsWithPagination(String query, int offset, int limit);

    // Get total count of archived products
    @Query("SELECT COUNT(*) FROM products WHERE isArchived = 1")
    LiveData<Integer> getTotalArchivedProductCount();
}
