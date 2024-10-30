package com.example.myapplication.repository.implementation;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.ProductCountPerMonth;
import com.example.myapplication.repository.interfaces.IProductRepository;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ProductRepository implements IProductRepository {

    public AppDatabase appDatabase;
    private Executor executor = Executors.newSingleThreadExecutor();

    public ProductRepository(Context context) {
        appDatabase = AppDatabase.getInstance(context);
    }

    @Override
    public LiveData<String> insertProduct(Product product) {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                appDatabase.productDao().insertProduct(product);
                result.postValue(Constants.SUCCESS);
            } catch (SQLiteConstraintException e) {
                result.postValue(Constants.CONSTRAINT_FAILURE);
            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });

        return result;
    }

    @Override
    public LiveData<String> insertAllProducts(List<Product> products) {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                appDatabase.productDao().insertAllProducts(products);
                result.postValue(Constants.SUCCESS);
            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });
        return result;
    }

    @Override
    public LiveData<String> updateProduct(Product product) {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                appDatabase.productDao().updateProduct(product);
                result.postValue(Constants.SUCCESS);
            } catch (SQLiteConstraintException e) {
                result.postValue(Constants.CONSTRAINT_FAILURE);
            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });

        return result;
    }
    @Override
    public LiveData<String> deleteProduct(Product product) {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int rowsDeleted = appDatabase.productDao().deleteProduct(product);
                if (rowsDeleted > 0) {
                    result.postValue(Constants.SUCCESS);
                } else {
                    result.postValue(Constants.ERROR);
                }
            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });

        return result;
    }

    @Override
    public LiveData<String> deleteAllProducts() {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int rowsDeleted = appDatabase.productDao().deleteAllProducts();
                if (rowsDeleted > 0) {
                    result.postValue(Constants.SUCCESS);
                } else {
                    result.postValue(Constants.ERROR);
                }
            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });

        return result;
    }

    @Override
    public LiveData<List<ProductCountPerMonth>> getProductCountPerMonth(long startDate, long endDate) {
        return appDatabase.productDao().getProductCountPerMonth(startDate,endDate);
    }

    public LiveData<PagingData<Product>> loadProductsByPage(int offset, int pageSize){
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(
                        6,0,true
                ),
                () -> appDatabase.productDao().loadProductsByPage(offset,pageSize)
        ));
    }

    public LiveData<PagingData<Product>> loadProductsSortedBy(String sortField, boolean ascending, int offset, int limit) {
        if (ascending) {
            return PagingLiveData.getLiveData(new Pager<>(new PagingConfig(
                    Constants.ITEMS_PER_PAGE, 0, true),
                    () -> appDatabase.productDao().loadProductsSortedByAsc(sortField, offset, limit)
            ));
        } else {
            return PagingLiveData.getLiveData(new Pager<>(new PagingConfig(
                    Constants.ITEMS_PER_PAGE, 0, true),
                    () -> appDatabase.productDao().loadProductsSortedByDesc(sortField, offset, limit)
            ));
        }
    }

    public LiveData<PagingData<Product>> searchProductsWithPagination(String query, int offset, int limit){
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(
                        6,0,true
                ),
                () -> appDatabase.productDao().searchProductsWithPagination("%" + query + "%",offset,limit)
        ));
    }

    public LiveData<Integer> getTotalProductCount(){
        return appDatabase.productDao().getTotalProductCount();
    }

    public LiveData<String> deleteSelectedProducts() {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                int rowsDeleted = appDatabase.productDao().deleteSelectedProducts();
                if (rowsDeleted > 0) {
                    result.postValue(Constants.SUCCESS);
                } else {
                    result.postValue(Constants.ERROR);
                }
            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });

        return result;
    }

    public void updateProductSelected(int productId, boolean isSelected) {
        executor.execute(() -> {
            appDatabase.productDao().updateItemSelected(productId, isSelected);
        });
    }

    public void deselectAllItems() {
        executor.execute(() -> {
            appDatabase.productDao().updateAllSelectedItems(false);
        });
    }

    @Override
    public LiveData<String> updateProductArchivedState(int productId, boolean isArchived) {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                appDatabase.productDao().updateProductArchivedState(productId,isArchived);
                result.postValue(Constants.SUCCESS);

            } catch (Exception e) {
                result.postValue(Constants.ERROR);
            }
        });

        return result;
    }

    @Override
    public LiveData<PagingData<Product>> loadArchivedProductsByPage(int offset, int limit) {

        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(
                        6,0,true
                ),
                () -> appDatabase.productDao().loadArchivedProductsByPage(offset,limit)
        ));
    }

    @Override
    public LiveData<PagingData<Product>> loadArchivedProductsSortedBy(String sortField, boolean ascending, int offset, int limit) {
        if (ascending) {
            return PagingLiveData.getLiveData(new Pager<>(new PagingConfig(
                    Constants.ITEMS_PER_PAGE, 0, true),
                    () -> appDatabase.productDao().loadArchivedProductsSortedByAsc(sortField, offset, limit)
            ));
        } else {
            return PagingLiveData.getLiveData(new Pager<>(new PagingConfig(
                    Constants.ITEMS_PER_PAGE, 0, true),
                    () -> appDatabase.productDao().loadArchivedProductsSortedByDesc(sortField, offset, limit)
            ));
        }
    }

    @Override
    public LiveData<PagingData<Product>> searchArchivedProductsWithPagination(String query, int offset, int limit) {
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(
                        6,0,true
                ),
                () -> appDatabase.productDao().searchArchivedProductsWithPagination("%" + query + "%",offset,limit)
        ));
    }

    @Override
    public LiveData<Integer> getTotalArchivedProductCount() {
        return appDatabase.productDao().getTotalArchivedProductCount();
    }
}
