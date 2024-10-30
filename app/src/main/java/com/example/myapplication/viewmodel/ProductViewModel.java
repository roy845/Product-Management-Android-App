package com.example.myapplication.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingData;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.models.Product;
import com.example.myapplication.models.ProductCountPerMonth;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ApiService;
import com.example.myapplication.repository.implementation.ProductRepository;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;


public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository productRepository;
    private final ExecutorService executorService;
    private final MutableLiveData<Boolean> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingInitialProductsLiveData = new MutableLiveData<>();
    public LiveData<Boolean> getErrorLiveData() {
        return errorLiveData;
    }
    public LiveData<Boolean> getLoadingInitialProducts(){
        return loadingInitialProductsLiveData;
    }
    private final SharedPreferences sharedPreferences;

    private static ProductViewModel instance;


    public ProductViewModel(@NonNull Application application) {
        super(application);
        productRepository = new ProductRepository(application);
        this.executorService = Executors.newSingleThreadExecutor();
        this.sharedPreferences = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        checkAndFetchProductsFromApi();
        deselectAllItems();
    }

    public static ProductViewModel getInstance(@NonNull Application application) {
        if (instance == null) {
            synchronized (ProductViewModel.class) {
                if (instance == null) {
                    instance = new ProductViewModel(application);
                }
            }
        }
        return instance;
    }


    public LiveData<List<ProductCountPerMonth>> getProductCountPerMonth(long startDate, long endDate) {
        return productRepository.getProductCountPerMonth(startDate, endDate);
    }

    public void retryLoadProducts() {
        fetchAndStoreProducts();
    }

    public LiveData<String> insertProduct(Product product) {
        return productRepository.insertProduct(product);
    }

    public void insertAllProducts(List<Product> products) {
        productRepository.insertAllProducts(products);
    }

    public LiveData<String> updateProduct(Product product) {
        return productRepository.updateProduct(product);
    }

    public LiveData<String> deleteProduct(Product product) {
       return productRepository.deleteProduct(product);
    }

    public LiveData<String> deleteAllProducts() {
        return productRepository.deleteAllProducts();
    }

    public LiveData<PagingData<Product>> loadProductsByPage(int offset, int pageSize) {
        return productRepository.loadProductsByPage(offset,pageSize);
    }

    public LiveData<PagingData<Product>> loadProductsSortedBy(String sortField, boolean ascending, int offset, int limit) {
        return productRepository.loadProductsSortedBy(sortField, ascending, offset, limit);
    }

    public LiveData<PagingData<Product>> searchProductsWithPagination(String query,int offset, int pageSize) {
        return productRepository.searchProductsWithPagination(query,offset,pageSize);
    }

    public LiveData<Integer> getTotalProductCount() {
        return productRepository.getTotalProductCount();
    }

    public void updateProductSelected(int productId, boolean isSelected){
        productRepository.updateProductSelected(productId,isSelected);
    }

    public LiveData<String> deleteSelectedProducts() {
        return productRepository.deleteSelectedProducts();
    }

    public void deselectAllItems() {
         productRepository.deselectAllItems();
    }

    public LiveData<String> updateProductArchivedState(int productId, boolean isArchived){
        return productRepository.updateProductArchivedState(productId,isArchived);
    }

    // ************************* Archived Product Methods *************************

    public LiveData<PagingData<Product>> loadArchivedProductsByPage(int offset, int limit) {
        return productRepository.loadArchivedProductsByPage(offset, limit);
    }

    public LiveData<PagingData<Product>> loadArchivedProductsSortedBy(String sortField, boolean ascending, int offset, int limit) {
        return productRepository.loadArchivedProductsSortedBy(sortField, ascending, offset, limit);
    }

    public LiveData<PagingData<Product>> searchArchivedProductsWithPagination(String query, int offset, int limit) {
        return productRepository.searchArchivedProductsWithPagination(query, offset, limit);
    }

    public LiveData<Integer> getTotalArchivedProductCount() {
        return productRepository.getTotalArchivedProductCount();
    }

    private void checkAndFetchProductsFromApi() {
        boolean hasFetchedProducts = sharedPreferences.getBoolean(Constants.PREF_FETCHED_PRODUCTS, false);
        if (!hasFetchedProducts) {
            fetchAndStoreProducts();
            errorLiveData.setValue(false);
        } else {
            errorLiveData.setValue(false);
            loadingInitialProductsLiveData.setValue(false);
        }
    }

    private void fetchAndStoreProducts() {
        loadingInitialProductsLiveData.setValue(true);
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Call<List<Product>> call = apiService.getProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> productsFromApi = response.body();

                    Date now = new Date();

                    for (Product product : productsFromApi) {
                        product.setCreatedAt(now);
                        product.setUpdatedAt(now);
                        product.setArchived(false);
                    }

                    executorService.execute(() -> insertAllProducts(productsFromApi));

                    sharedPreferences.edit().putBoolean(Constants.PREF_FETCHED_PRODUCTS, true).apply();
                    loadingInitialProductsLiveData.setValue(false);
                    errorLiveData.setValue(false);

                    Toast.makeText(getApplication().getApplicationContext(), Constants.PRODUCT_FETCHED_SUCCESSFULLY, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                handleApiFailure(t);
                errorLiveData.setValue(true);
                loadingInitialProductsLiveData.setValue(false);
            }
        });
    }


    private void handleApiFailure(Throwable t) {
        String errorMessage;
        if (t instanceof IOException) {
            errorMessage = Constants.NETWORK_ERROR;
        } else if (t instanceof HttpException) {
            errorMessage = Constants.SERVER_ERROR;
        } else {
            errorMessage = Constants.UNEXPECTED_ERROR;
        }
        Toast.makeText(getApplication().getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    }
}
