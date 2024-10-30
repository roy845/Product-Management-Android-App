package com.example.myapplication.ui.activity;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;

public class ErrorLoadingProducts extends AppCompatActivity {
    private ProductViewModel productViewModel;
    private MaterialButton retryButton;
    ProgressBar progressBarLoadingProducts;
    ImageView errorImageView;
    TextView errorTextView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_error_loading_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.errorLoadingUsers), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupActionBar();
        setupViewModel();
        initRetryButtonClickListener();
    }

    private void setupActionBar(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View customTitleView = inflater.inflate(R.layout.toolbar_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.toolbar_title);
        titleTextView.setText(R.string.error_loading_users_title);
        toolbar.addView(customTitleView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initViews(){
        retryButton = findViewById(R.id.retry_button);
        errorImageView = findViewById(R.id.error_image);
        errorTextView = findViewById(R.id.error_msg_text);
        progressBarLoadingProducts = findViewById(R.id.progressBarLoadingProducts);
        toolbar = findViewById(R.id.toolbar);
    }

    public void initRetryButtonClickListener(){
        retryButton.setOnClickListener(v-> {

            productViewModel.retryLoadProducts();
            productViewModel.getErrorLiveData().observe(ErrorLoadingProducts.this, error -> {
                if(!error){
                    progressBarLoadingProducts.setVisibility(View.VISIBLE);
                    retryButton.setVisibility(View.GONE);
                    errorImageView.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.GONE);
                    finish();
                }
            });
        });
    }

    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }
}