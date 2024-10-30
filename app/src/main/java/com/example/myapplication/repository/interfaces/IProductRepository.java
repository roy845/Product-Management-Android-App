package com.example.myapplication.repository.interfaces;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.ProductCountPerMonth;
import java.util.List;

public interface IProductRepository {
    LiveData<String> insertProduct(Product product);
    LiveData<String> insertAllProducts(List<Product> products);
    LiveData<String> updateProduct(Product product);
    LiveData<String> deleteProduct(Product product);
    LiveData<String> deleteAllProducts();
    LiveData<List<ProductCountPerMonth>> getProductCountPerMonth(long startDate, long endDate);
    LiveData<Integer> getTotalProductCount();
    LiveData<PagingData<Product>> loadProductsSortedBy(String sortField, boolean ascending, int offset, int limit);
    LiveData<PagingData<Product>> loadProductsByPage(int offset, int pageSize);
    LiveData<PagingData<Product>> searchProductsWithPagination(String query,int offset,int limit);
    LiveData<String> deleteSelectedProducts();
    void updateProductSelected(int productId, boolean isSelected);
    void deselectAllItems();
    LiveData<String> updateProductArchivedState(int productId, boolean isArchived);

    // Methods for archived products
    LiveData<PagingData<Product>> loadArchivedProductsByPage(int offset, int limit);
    LiveData<PagingData<Product>> loadArchivedProductsSortedBy(String sortField, boolean ascending, int offset, int limit);
    LiveData<PagingData<Product>> searchArchivedProductsWithPagination(String query, int offset, int limit);
    LiveData<Integer> getTotalArchivedProductCount();
}
