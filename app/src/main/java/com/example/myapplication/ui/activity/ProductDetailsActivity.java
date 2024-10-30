package com.example.myapplication.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.material.navigation.NavigationView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProductDetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ImageView productAvatarImageView,uploadIcon;
    private TextView productTitleTextView;
    private Button updateButton, cancelButton;
    private EditText titleEditText, priceEditText, categoryEditText,descriptionEditText,createdAtEditText,updatedAtEditText;
    ProductViewModel productViewModel;
    private Uri selectedImageUri;
    private TextView errorMissingTitleTextView, errorMissingPriceTextView,errorInvalidPriceTextView, errorMissingCategoryTextView, errorMissingDescriptionTextView,generalFeedbackTextView;
    private Dialog imageDialog;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;

    private Product productToUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_product_details), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

            initViews();
            setupViewModel();
            setGravityEditTexts();
            restoreImageView(savedInstanceState);
            restoreErrorTexts(savedInstanceState);
            restoreEditTextsTexts(savedInstanceState);
            restoreImageDialog(savedInstanceState);
            initUploadIconClickListener();
            initUpdateButtonClickListener();
            setupDrawerNavigation();
            navigateToMainActivity();

            loadProduct();


    }

    private void loadProduct(){
        if(getIntent().hasExtra(Constants.PRODUCT_MODEL)){
            productToUpdate = getIntent().getParcelableExtra(Constants.PRODUCT_MODEL);

            if(productToUpdate!=null){
                loadProduct(productToUpdate);
            }
        }
    }

    private void setupDrawerNavigation(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View customTitleView = inflater.inflate(R.layout.toolbar_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.toolbar_title);
        titleTextView.setText(R.string.update_product_title);
        toolbar.addView(customTitleView);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, android.R.color.white));
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void loadProduct(Product product) {

        productTitleTextView.setText(String.format("%s", product.getTitle()));
        titleEditText.setText(product.getTitle());
        priceEditText.setText(String.valueOf(product.getPrice()));
        categoryEditText.setText(product.getCategory());
        descriptionEditText.setText(product.getDescription());

        initUserAvatarClickListener(product.getImage());

        if(selectedImageUri!=null) {
            Glide.with(this)
                    .load(Uri.parse(selectedImageUri.toString()))
                    .into(productAvatarImageView);
            initUserAvatarClickListener(selectedImageUri);

        }else if(product.getImage() != null){
            Glide.with(this)
                    .load(Uri.parse(product.getImage()))
                    .into(productAvatarImageView);
            initUserAvatarClickListener(product.getImage());
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        if (product.getCreatedAt() != null) {
            createdAtEditText.setText(dateFormat.format(product.getCreatedAt()));
        }

        if (product.getUpdatedAt() != null) {
            updatedAtEditText.setText(dateFormat.format(product.getUpdatedAt()));
        }
    }

    private void restoreImageDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            boolean wasDialogShowing = savedInstanceState.getBoolean(Constants.IS_DIALOG_SHOWING, false);
            String imageSourceType = savedInstanceState.getString(Constants.IMAGE_SOURCE_TYPE);

            if (wasDialogShowing && imageSourceType != null) {
                if (Constants.URI_STRING.equals(imageSourceType)) {
                    String uriString = savedInstanceState.getString(Constants.SELECTED_IMAGE_URI);
                    if (uriString != null) {
                        Uri imageUri = Uri.parse(uriString);
                        createDialog(imageUri);
                    }
                } else if (Constants.STRING.equals(imageSourceType)) {
                    String imageString = savedInstanceState.getString(Constants.SELECTED_IMAGE_STRING);
                    if (imageString != null) {
                        createDialog(imageString);
                    }
                }
            }
        }
    }


    private void initViews(){
        uploadIcon = findViewById(R.id.upload_icon_product_details);
        productAvatarImageView = findViewById(R.id.product_avatar);
        productTitleTextView = findViewById(R.id.product_name_header);
        titleEditText = findViewById(R.id.update_edit_text_product_title);
        priceEditText = findViewById(R.id.update_edit_text_product_price);
        categoryEditText = findViewById(R.id.update_edit_text_product_category);
        descriptionEditText = findViewById(R.id.update_edit_text_product_description);

        updateButton = findViewById(R.id.button_update);
        cancelButton = findViewById(R.id.button_cancel);

        errorMissingTitleTextView = findViewById(R.id.update_error_label_product_title);
        errorMissingPriceTextView = findViewById(R.id.update_error_label_product_price);
        errorInvalidPriceTextView = findViewById(R.id.update_error_label_invalid_product_price);
        errorMissingCategoryTextView =findViewById(R.id.update_error_label_product_category);
        errorMissingDescriptionTextView = findViewById(R.id.update_error_label_product_description);

        generalFeedbackTextView = findViewById(R.id.update_general_feedback_text);

        createdAtEditText = findViewById(R.id.edit_text_created_at);
        updatedAtEditText = findViewById(R.id.edit_text_updated_at);

        drawerLayout = findViewById(R.id.drawer_layout_product_details);
        navigationView = findViewById(R.id.navigation_view_user_details);
        toolbar = findViewById(R.id.toolbar_product_details);
    }

    private void setGravityEditTexts() {

        titleEditText.setTextDirection(View.TEXT_DIRECTION_LTR);
        priceEditText.setTextDirection(View.TEXT_DIRECTION_LTR);
        categoryEditText.setTextDirection(View.TEXT_DIRECTION_LTR);
        descriptionEditText.setTextDirection(View.TEXT_DIRECTION_LTR);

        titleEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        priceEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        categoryEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        descriptionEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        createdAtEditText.setTextDirection(View.TEXT_DIRECTION_LTR);
        createdAtEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);

        updatedAtEditText.setTextDirection(View.TEXT_DIRECTION_LTR);
        updatedAtEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
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

            if (generalFeedbackTextView != null) {
                generalFeedbackTextView.setVisibility(savedInstanceState.getInt(Constants.ERROR_GENERAL_FEEDBACK_VISIBILITY));
                generalFeedbackTextView.setText(savedInstanceState.getString(Constants.ERROR_GENERAL_FEEDBACK_TEXT));
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
            titleEditText.setText(savedInstanceState.getString(Constants.TITLE, Constants.EMPTY_STRING));
            priceEditText.setText(savedInstanceState.getString(Constants.PRICE, Constants.EMPTY_STRING));
            categoryEditText.setText(savedInstanceState.getString(Constants.CATEGORY, Constants.EMPTY_STRING));
            descriptionEditText.setText(savedInstanceState.getString(Constants.DESCRIPTION, Constants.EMPTY_STRING));
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


        outState.putInt(Constants.ERROR_GENERAL_FEEDBACK_VISIBILITY, generalFeedbackTextView.getVisibility());
        outState.putString(Constants.ERROR_GENERAL_FEEDBACK_TEXT, generalFeedbackTextView.getText().toString());

        outState.putString(Constants.TITLE, titleEditText.getText().toString());
        outState.putString(Constants.PRICE, priceEditText.getText().toString());
        outState.putString(Constants.CATEGORY, categoryEditText.getText().toString());
        outState.putString(Constants.DESCRIPTION, descriptionEditText.getText().toString());

        if (selectedImageUri != null) {
            outState.putString(Constants.SELECTED_IMAGE_URI, selectedImageUri.toString());
            outState.putString(Constants.IMAGE_SOURCE_TYPE, Constants.URI_STRING);
        } else if (productToUpdate != null && productToUpdate.getImage() != null) {
            outState.putString(Constants.SELECTED_IMAGE_STRING, productToUpdate.getImage());
            outState.putString(Constants.IMAGE_SOURCE_TYPE, Constants.STRING);
        }

        outState.putBoolean(Constants.IS_DIALOG_SHOWING, imageDialog != null && imageDialog.isShowing());
    }


    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }


    private void createDialog(Uri imageUri) {
        if (imageDialog == null || !imageDialog.isShowing()) {
            imageDialog = new Dialog(this);
            imageDialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = imageDialog.findViewById(R.id.dialogImage);

            Glide.with(this)
                    .load(imageUri)
                    .into(dialogImage);

            imageDialog.show();
        }
    }

    private void createDialog(String imageString) {
        if (imageDialog == null || !imageDialog.isShowing()) {
            imageDialog = new Dialog(this);
            imageDialog.setContentView(R.layout.dialog_image);
            ImageView dialogImage = imageDialog.findViewById(R.id.dialogImage);

            Glide.with(this)
                    .load(imageString)
                    .into(dialogImage);

            imageDialog.show();
        }
    }
    private void initUserAvatarClickListener(String imageUri) {
        productAvatarImageView.setOnClickListener(v -> showImageDialog(imageUri));
    }

    private void initUserAvatarClickListener(Uri imageUri) {
        productAvatarImageView.setOnClickListener(v -> showImageDialogSelectedImageUri(imageUri));
    }

    private void showImageDialogSelectedImageUri(Uri imageUri) {
        if(imageUri!=null){
            createDialog(imageUri);
        }
    }

    private void showImageDialog(String imageUri) {
        if(imageUri!=null){
            createDialog(imageUri);
        }
    }

    private void initUploadIconClickListener() {
        uploadIcon.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
    }

    private void showUnexpectedError(){
        generalFeedbackTextView.setVisibility(View.VISIBLE);
        generalFeedbackTextView.setText(R.string.unexpected_error_occured);
        Toast.makeText(ProductDetailsActivity.this, Constants.UNEXPECTED_ERROR_OCCURRED, Toast.LENGTH_SHORT).show();
    }


    private void initUpdateButtonClickListener(){
        updateButton.setOnClickListener(v -> {

            AtomicBoolean hasError = new AtomicBoolean(false);

            errorMissingTitleTextView.setVisibility(View.GONE);
            errorMissingPriceTextView.setVisibility(View.GONE);
            errorInvalidPriceTextView.setVisibility(View.GONE);
            errorMissingCategoryTextView.setVisibility(View.GONE);
            errorMissingDescriptionTextView.setVisibility(View.GONE);
            generalFeedbackTextView.setVisibility(View.GONE);

            if(productToUpdate!=null){
                double price;

                String title = titleEditText.getText().toString().trim();
                String priceText = priceEditText.getText().toString().trim();
                String category = categoryEditText.getText().toString().trim();
                String description = descriptionEditText.getText().toString().trim();

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

                productToUpdate.setTitle(title);
                productToUpdate.setPrice(price);
                productToUpdate.setCategory(category);
                productToUpdate.setDescription(description);

                if (selectedImageUri != null) {
                    productToUpdate.setImage(selectedImageUri.toString());
                }

                productToUpdate.setUpdatedAt(new Date());

                LiveData<String> result = productViewModel.updateProduct(productToUpdate);

                result.observe(this, result1 -> {
                    if (Constants.SUCCESS.equals(result1)) {
                        Toast.makeText(ProductDetailsActivity.this, "Product " + title + " updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        showUnexpectedError();
                    }
                });
            }
        });
    }


    private void navigateToMainActivity(){
        cancelButton.setOnClickListener(v -> finish());
    }

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Glide.with(ProductDetailsActivity.this)
                            .load(uri)
                            .into(productAvatarImageView);
                    initUserAvatarClickListener(selectedImageUri);

                } else {
                    Toast.makeText(ProductDetailsActivity.this, Constants.NO_MEDIA_SELECTED, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(ProductDetailsActivity.this,MainActivity.class));
        } else if (id == R.id.nav_add_new_product) {
            startActivity(new Intent(ProductDetailsActivity.this, AddProductActivity.class));
        } else if (id == R.id.nav_graphs) {
            startActivity(new Intent(ProductDetailsActivity.this, GraphsActivity.class));
        } else if(id == R.id.nav_archived_products){
            startActivity(new Intent(ProductDetailsActivity.this, ArchivedProductsActivity.class));
        }else {
            return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}