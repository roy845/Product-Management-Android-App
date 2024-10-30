package com.example.myapplication.ui.activity;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.models.Product;
import com.example.myapplication.utils.ValidationUtils;
import com.example.myapplication.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddProductActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private ImageView productAvatarImageView,uploadIcon;
    TextView addUserTitleTextView;
    MaterialButton addUserButton;
    MaterialButton cancelButton;
    private EditText editTextTitle, editTextPrice, editTextCategory,editTextDescription;
    ProductViewModel productsViewModel;
    private Uri selectedImageUri;
    private TextView errorMissingTitleTextView, errorMissingPriceTextView,errorInvalidPriceTextView, errorMissingCategoryTextView, errorMissingDescriptionTextView,
            errorImageViewTextView,generalFeedbackTextView;
    private Dialog imageDialog;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_product), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupViewModel();
        setGravityEditTexts();
        initUserAvatarClickListener();
        initAddButtonClickListener();
        initUploadIconClickListener();
        restoreErrorTexts(savedInstanceState);
        restoreImageView(savedInstanceState);
        restoreEditTextsTexts(savedInstanceState);
        restoreImageDialog(savedInstanceState);
        setupDrawerNavigation();
        navigateToMainActivity();
    }


    private void setupDrawerNavigation(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View customTitleView = inflater.inflate(R.layout.toolbar_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.toolbar_title);
        titleTextView.setText(R.string.add_new_product_title);
        toolbar.addView(customTitleView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        navigationView.setNavigationItemSelectedListener(this);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, android.R.color.white));
    }


    private void restoreImageDialog(Bundle savedInstanceState){
        if (savedInstanceState != null) {
            boolean wasDialogShowing = savedInstanceState.getBoolean(Constants.IS_DIALOG_SHOWING, false);
            if (wasDialogShowing) {
                createDialog();
            }
        }
    }

    private void createDialog(){
        if (imageDialog == null || !imageDialog.isShowing()) {
            imageDialog = new Dialog(this);
            imageDialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = imageDialog.findViewById(R.id.dialogImage);

            if (selectedImageUri != null) {
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(dialogImage);
            }

            imageDialog.show();
        }
    }

    private void initUploadIconClickListener() {
        uploadIcon.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void showImageDialog() {
        if(selectedImageUri!=null){
            createDialog();
        }
    }

    private void initUserAvatarClickListener() {
        productAvatarImageView.setOnClickListener(v -> showImageDialog());
    }

    private void setGravityEditTexts() {

        editTextTitle.setTextDirection(View.TEXT_DIRECTION_LTR);
        editTextPrice.setTextDirection(View.TEXT_DIRECTION_LTR);
        editTextCategory.setTextDirection(View.TEXT_DIRECTION_LTR);
        editTextDescription.setGravity(View.TEXT_DIRECTION_LTR);
    }

    private void restoreErrorTexts(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            if (errorMissingTitleTextView != null) {
                errorMissingTitleTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_MISSING_TITLE_VISIBILITY));
            }
            if (errorMissingPriceTextView != null) {
                errorMissingPriceTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_MISSING_PRICE_VISIBILITY));
            }
            if (errorInvalidPriceTextView != null) {
                errorInvalidPriceTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_INVALID_PRICE_VISIBILITY));
            }
            if (errorMissingCategoryTextView != null) {
                errorMissingCategoryTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_MISSING_CATEGORY_VISIBILITY));
            }
            if (errorMissingDescriptionTextView != null) {
                errorMissingDescriptionTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_MISSING_DESCRIPTION_VISIBILITY));
            }
            if (errorImageViewTextView != null) {
                errorImageViewTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_IMAGE_VIEW_VISIBILITY));
            }
        }
    }

    private void restoreImageView(@Nullable Bundle savedInstanceState){
        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString(Constants.SELECTED_IMAGE_URI);
            if (uriString != null) {
                selectedImageUri = Uri.parse(uriString);
                Glide.with(this)
                        .load(selectedImageUri)
                        .into(productAvatarImageView);
            }
        }
    }

    private void restoreEditTextsTexts(@Nullable Bundle savedInstanceState){
        if (savedInstanceState != null) {
            editTextTitle.setText(savedInstanceState.getString(Constants.TITLE, Constants.EMPTY_STRING));
            editTextPrice.setText(savedInstanceState.getString(Constants.PRICE, Constants.EMPTY_STRING));
            editTextCategory.setText(savedInstanceState.getString(Constants.CATEGORY, Constants.EMPTY_STRING));
            editTextDescription.setText(savedInstanceState.getString(Constants.DESCRIPTION, Constants.EMPTY_STRING));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(Constants.ERROR_MISSING_TITLE_VISIBILITY, errorMissingTitleTextView.getVisibility());
        outState.putInt(Constants.ERROR_MISSING_PRICE_VISIBILITY, errorMissingPriceTextView.getVisibility());
        outState.putInt(Constants.ERROR_INVALID_PRICE_VISIBILITY, errorInvalidPriceTextView.getVisibility());
        outState.putInt(Constants.ERROR_MISSING_CATEGORY_VISIBILITY, errorMissingCategoryTextView.getVisibility());
        outState.putInt(Constants.ERROR_MISSING_DESCRIPTION_VISIBILITY, errorMissingDescriptionTextView.getVisibility());
        outState.putInt(Constants.ERROR_IMAGE_VIEW_VISIBILITY, errorImageViewTextView.getVisibility());


        outState.putString(Constants.TITLE,editTextTitle.getText().toString());
        outState.putString(Constants.PRICE, editTextPrice.getText().toString());
        outState.putString(Constants.CATEGORY, editTextCategory.getText().toString());
        outState.putString(Constants.DESCRIPTION, editTextDescription.getText().toString());

        if (selectedImageUri != null) {
            outState.putString(Constants.SELECTED_IMAGE_URI, selectedImageUri.toString());
        }

        outState.putBoolean(Constants.IS_DIALOG_SHOWING, imageDialog != null && imageDialog.isShowing());

    }


    private void showUnexpectedError(){
        generalFeedbackTextView.setVisibility(View.VISIBLE);
        generalFeedbackTextView.setText(R.string.unexpected_error_occured);
        Toast.makeText(AddProductActivity.this,Constants.UNEXPECTED_ERROR_OCCURRED, Toast.LENGTH_SHORT).show();
    }

    private void initAddButtonClickListener() {

        addUserButton.setOnClickListener(v -> {

            double price;

            String title = editTextTitle.getText().toString().trim();
            String priceText = editTextPrice.getText().toString().trim();
            String category = editTextCategory.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();

            AtomicBoolean hasError = new AtomicBoolean(false);

            errorMissingTitleTextView.setVisibility(View.GONE);
            errorMissingPriceTextView.setVisibility(View.GONE);
            errorInvalidPriceTextView.setVisibility(View.GONE);
            errorMissingCategoryTextView.setVisibility(View.GONE);
            errorMissingDescriptionTextView.setVisibility(View.GONE);
            errorImageViewTextView.setVisibility(View.GONE);
            generalFeedbackTextView.setVisibility(View.GONE);


            if (ValidationUtils.validateTitle(title)) {
                errorMissingTitleTextView.setVisibility(View.VISIBLE);
                hasError.set(true);
            }


            if (ValidationUtils.validatePrice(priceText)) {
                errorMissingPriceTextView.setVisibility(View.VISIBLE);
                hasError.set(true);
            }

            if (ValidationUtils.validateCategory(category)) {
                errorMissingCategoryTextView.setVisibility(View.VISIBLE);
                hasError.set(true);
            }

            if (ValidationUtils.validateDescription(description)) {
                errorMissingDescriptionTextView.setVisibility(View.VISIBLE);
                hasError.set(true);
            }

            if (!ValidationUtils.validateImageView(productAvatarImageView) && selectedImageUri == null) {
                errorImageViewTextView.setVisibility(View.VISIBLE);
                hasError.set(true);
            }

            try {
                price = Double.parseDouble(priceText);
            } catch (NumberFormatException e) {
                errorInvalidPriceTextView.setVisibility(View.VISIBLE);
                Toast.makeText(this, Constants.INVALID_PRICE_FORMAT, Toast.LENGTH_SHORT).show();
                return;
            }



            if (hasError.get()) {
                Toast.makeText(this, Constants.CORRECT_ERRORS_AND_TRY_AGAIN, Toast.LENGTH_SHORT).show();
                return;
            }

            Product newProduct = new Product(title, price, description,category, selectedImageUri !=null ? selectedImageUri.toString() : null,new Date(),new Date());

            LiveData<String> result = productsViewModel.insertProduct(newProduct);
            result.observe(this, result1 -> {
                if (Constants.SUCCESS.equals(result1)) {
                    Toast.makeText(AddProductActivity.this, "Product " + title + " added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else{
                    showUnexpectedError();
                }
            });
        });
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private void initViews(){
        addUserTitleTextView = findViewById(R.id.add_product_title);
        addUserButton = findViewById(R.id.add_user_button);
        cancelButton = findViewById(R.id.cancel_add_user_button);
        productAvatarImageView = findViewById(R.id.add_product_avatar);
        uploadIcon = findViewById(R.id.upload_avatar);
        editTextTitle = findViewById(R.id.add_edit_text_product_title);
        editTextPrice = findViewById(R.id.add_edit_text_product_price);
        editTextCategory = findViewById(R.id.add_edit_text_product_category);
        editTextDescription = findViewById(R.id.add_edit_text_product_description);
        errorMissingTitleTextView = findViewById(R.id.error_label_product_title);
        errorMissingPriceTextView = findViewById(R.id.error_label_product_price);
        errorInvalidPriceTextView = findViewById(R.id.error_label_invalid_product_price);
        errorMissingCategoryTextView = findViewById(R.id.error_label_product_category);
        errorMissingDescriptionTextView = findViewById(R.id.error_label_product_description);
        errorImageViewTextView =  findViewById(R.id.error_label_avatar_missing);
        generalFeedbackTextView = findViewById(R.id.general_feedback_text);
        drawerLayout = findViewById(R.id.drawer_layout_add_product);
        navigationView = findViewById(R.id.navigation_view_add_user);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupViewModel() {
        productsViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    private void navigateToMainActivity(){
        cancelButton.setOnClickListener(v -> finish());
    }

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Glide.with(AddProductActivity.this)
                            .load(uri)
                            .into(productAvatarImageView);
                } else {
                    Toast.makeText(AddProductActivity.this, Constants.NO_MEDIA_SELECTED, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        String currentActivity = this.getClass().getSimpleName();

        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(AddProductActivity.this,MainActivity.class));
        } else if (id == R.id.nav_add_new_product) {
            if (!currentActivity.equals(AddProductActivity.class.getSimpleName())) {
                Intent addUserIntent = new Intent(this, MainActivity.class);
                startActivity(addUserIntent);
                finish();
            }

        } else if (id == R.id.nav_graphs) {
            startActivity(new Intent(AddProductActivity.this, GraphsActivity.class));
        } else if(id == R.id.nav_archived_products){
            startActivity(new Intent(AddProductActivity.this, ArchivedProductsActivity.class));
        }else {
            return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}