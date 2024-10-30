package com.example.myapplication.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.adapter.SwipeToDeleteCallback;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.interfaces.OnClickProductInterface;
import com.example.myapplication.models.Product;
import com.example.myapplication.viewmodel.ProductViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArchivedProductsActivity extends AppCompatActivity implements OnClickProductInterface,NavigationView.OnNavigationItemSelectedListener {
    private ProductViewModel productViewModel;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    ProgressBar progressBar,progressBarLoadingInitialUsers;
    Toolbar toolbar;
    EditText searchEditText;
    LinearLayout layoutSwitchGroup;
//    FloatingActionButton floatingActionButton;
    MaterialButton prevButton,nextButton;
    private final Handler handler = new Handler();
    private Runnable searchRunnable;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView emptyResultsTextView,currentPageTextView,totalPagesTextView,pageSeparatorTextView;
    private ImageView emptyResultsImageView;
    private ImageButton buttonListLayout, buttonGridLayout,deleteAllUsersImageButton,deleteSelectedProductsImageButton;
    private Spinner sortingSpinner;
    private boolean isDialogShown = false;
    int CURRENT_PAGE = 1,TOTAL_PAGES = 2;
    String sortField = Constants.NAME;
    boolean ascending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archived_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.archived_products_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initViews();
        setupViewModel();
        observeErrorLiveData();
        initRecyclerViewAndAdapter();
//        observeAllProducts();
        observeTotalProductCount();
        setupEditTextSearch();
        setupLayoutSwitchButtons();
        enableSwipeToDeleteAndUndo();
        setCurrentPage(1);
        handlePrevPage();
        handleNextPage();
        observeInitialLoadingLiveData();
        deleteAllProductsOnClickListener();
//        navigateToAddProductActivity();
        setupDrawerNavigation();
        setupSortingSpinner();
        showHideDeleteSelectedProducts();
        deleteSelectedProducts();
    }

    private void showHideDeleteSelectedProducts(){
        productAdapter.getSelectedItemsLiveData().observe(this,selectedItems->{
            if(selectedItems.isEmpty()){
                deleteSelectedProductsImageButton.setVisibility(View.GONE);
            }else{
                deleteSelectedProductsImageButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void deleteSelectedProducts(){
        deleteSelectedProductsImageButton.setOnClickListener(view->{
            AlertDialog dialog = new AlertDialog.Builder(ArchivedProductsActivity.this)
                    .setMessage(Constants.ARE_YOU_SURE_DO_YOU_WANT_TO_DELETE_ALL_SELECTED_PRODUCTS_PROMPT)
                    .setView(R.layout.custom_dialog_buttons)
                    .setCancelable(false)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {

                Button positiveButton = dialog.findViewById(R.id.dialog_positive_button);
                Button negativeButton = dialog.findViewById(R.id.dialog_negative_button);

                positiveButton.setOnClickListener(v -> {


                    productViewModel.deleteSelectedProducts().observe(ArchivedProductsActivity.this, success -> {

                        if (Constants.SUCCESS.equals(success)) {
                            Toast.makeText(ArchivedProductsActivity.this,Constants.ALL_SELECTED_PRODUCTS_REMOVED_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
                            deleteSelectedProductsImageButton.setVisibility(View.GONE);
                            dialog.dismiss();
                        }else {
                            Toast.makeText(ArchivedProductsActivity.this, Constants.FAILED_TO_REMOVE_SELECTED_PRODUCTS, Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                negativeButton.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            });

            dialog.show();
        });
    }

    private void setupSortingSpinner() {
        ArrayAdapter<String> adapter = getStringArrayAdapter();

        sortingSpinner = findViewById(R.id.spinner_sort_options);
        sortingSpinner.setAdapter(adapter);

        sortingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedOption = (String) parentView.getItemAtPosition(position);
                switch (selectedOption) {
                    case Constants.SORT_BY_NAME_ASC:
                        sortField = Constants.NAME;
                        ascending = true;
                        break;
                    case Constants.SORT_BY_NAME_DESC:
                        sortField = Constants.NAME;
                        ascending = false;
                        break;
                    case Constants.SORT_BY_ID_ASC:
                        sortField = Constants.ID;
                        ascending = true;
                        break;
                    case Constants.SORT_BY_ID_DESC:
                        sortField = Constants.ID;
                        ascending = false;
                        break;
                    case Constants.SORT_BY_DATE_ASC:
                        sortField = Constants.CREATED_AT;
                        ascending = true;
                        break;
                    case Constants.SORT_BY_DATE_DESC:
                        sortField = Constants.CREATED_AT;
                        ascending = false;
                        break;

                    case Constants.SORT_BY_PRICE_ASC:
                        sortField = Constants.PRICE;
                        ascending = true;
                        break;

                    case Constants.SORT_BY_PRICE_DESC:
                        sortField = Constants.PRICE;
                        ascending = false;
                        break;
                }

                observeSortedProducts(sortField, ascending);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    private @NonNull ArrayAdapter<String> getStringArrayAdapter() {
        List<String> sortingOptions = new ArrayList<>();
        sortingOptions.add(Constants.SORT_BY_NAME_ASC);
        sortingOptions.add(Constants.SORT_BY_NAME_DESC);
        sortingOptions.add(Constants.SORT_BY_PRICE_ASC);
        sortingOptions.add(Constants.SORT_BY_PRICE_DESC);
        sortingOptions.add(Constants.SORT_BY_ID_ASC);
        sortingOptions.add(Constants.SORT_BY_ID_DESC);
        sortingOptions.add(Constants.SORT_BY_DATE_ASC);
        sortingOptions.add(Constants.SORT_BY_DATE_DESC);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, sortingOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void setupDrawerNavigation(){
        toolbar = findViewById(R.id.toolbar);
        LayoutInflater inflater = LayoutInflater.from(this);
        View customTitleView = inflater.inflate(R.layout.toolbar_title, null);
        TextView titleTextView = customTitleView.findViewById(R.id.toolbar_title);
        titleTextView.setText(R.string.app_name);
        toolbar.addView(customTitleView);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void deleteAllProductsOnClickListener(){
        deleteAllUsersImageButton.setOnClickListener(view->{
            AlertDialog dialog = new AlertDialog.Builder(ArchivedProductsActivity.this)
                    .setMessage(Constants.ARE_YOU_SURE_DO_YOU_WANT_TO_DELETE_ALL_PRODUCTS_PROMPT)
                    .setView(R.layout.custom_dialog_buttons)
                    .setCancelable(false)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {

                Button positiveButton = dialog.findViewById(R.id.dialog_positive_button);
                Button negativeButton = dialog.findViewById(R.id.dialog_negative_button);

                positiveButton.setOnClickListener(v -> {


                    productViewModel.deleteAllProducts().observe(ArchivedProductsActivity.this, success -> {

                        if (Constants.SUCCESS.equals(success)) {
                            Toast.makeText(ArchivedProductsActivity.this,Constants.ALL_PRODUCTS_REMOVED_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else {
                            Toast.makeText(ArchivedProductsActivity.this, Constants.FAILED_TO_REMOVE_PRODUCTS, Toast.LENGTH_SHORT).show();
                        }
                    });


                });

                negativeButton.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            });

            dialog.show();
        });
    }

    private void setCurrentPage(Integer currentPage) {
        if (currentPage > 0 && currentPage <= TOTAL_PAGES) {
            currentPageTextView.setText(String.valueOf(currentPage));
            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage < TOTAL_PAGES);
            observeSortedProducts(sortField, ascending);
        }

        if(TOTAL_PAGES == 1){
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
    }

    private void handleNextPage() {
        nextButton.setOnClickListener(view -> {
            if (CURRENT_PAGE < TOTAL_PAGES) {
                CURRENT_PAGE += 1;
                setCurrentPage(CURRENT_PAGE);
                observeSortedProducts(sortField, ascending);
            }
        });
    }

    private void handlePrevPage() {
        prevButton.setOnClickListener(view -> {
            if (CURRENT_PAGE > 1) {
                CURRENT_PAGE -= 1;
                setCurrentPage(CURRENT_PAGE);
                observeSortedProducts(sortField, ascending);
            }
        });
    }

    private void observeTotalProductCount(){
        productViewModel.getTotalArchivedProductCount().observe(this,totalProductCount -> {
            TOTAL_PAGES = (int) Math.ceil((double) totalProductCount / Constants.ITEMS_PER_PAGE);

            totalPagesTextView.setText(String.valueOf(TOTAL_PAGES));

            if (CURRENT_PAGE > 0 && CURRENT_PAGE <= TOTAL_PAGES) {
                currentPageTextView.setText(String.valueOf(CURRENT_PAGE));
                prevButton.setEnabled(CURRENT_PAGE > 1);
                nextButton.setEnabled(CURRENT_PAGE < TOTAL_PAGES);
            }

            if(totalProductCount == 0){
                prevButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                currentPageTextView.setVisibility(View.GONE);
                pageSeparatorTextView.setVisibility(View.GONE);
                totalPagesTextView.setVisibility(View.GONE);
            }else{
                prevButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                currentPageTextView.setVisibility(View.VISIBLE);
                pageSeparatorTextView.setVisibility(View.VISIBLE);
                totalPagesTextView.setVisibility(View.VISIBLE);
            }

        });
    }

    private void setupLayoutSwitchButtons() {
        buttonListLayout.setOnClickListener(v -> {
            switchToLinearLayout();
            saveLayoutChoice(Constants.LAYOUT_LIST);
        });

        buttonGridLayout.setOnClickListener(v -> {
            switchToGridLayout();
            saveLayoutChoice(Constants.LAYOUT_GRID);
        });

        String savedLayout = getSavedLayoutChoice();
        if (Constants.LAYOUT_GRID.equals(savedLayout)) {
            switchToGridLayout();
        } else {
            switchToLinearLayout();
        }
    }

    private void saveLayoutChoice(String layout) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(Constants.PREF_LAYOUT_TYPE, layout);
        editor.apply();
    }

    private String getSavedLayoutChoice() {
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(Constants.PREF_LAYOUT_TYPE, Constants.LAYOUT_LIST);
    }

    private void switchToLinearLayout() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        buttonListLayout.setBackgroundColor(Color.WHITE);
        buttonListLayout.setColorFilter(Color.BLACK);
        buttonGridLayout.setBackgroundColor(Color.TRANSPARENT);
        buttonGridLayout.setColorFilter(Color.WHITE);
    }

    private void switchToGridLayout() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        buttonGridLayout.setBackgroundColor(Color.WHITE);
        buttonGridLayout.setColorFilter(Color.BLACK);
        buttonListLayout.setBackgroundColor(Color.TRANSPARENT);
        buttonListLayout.setColorFilter(Color.WHITE);
    }


    private void observeInitialLoadingLiveData(){
        productViewModel.getLoadingInitialProducts().observe(ArchivedProductsActivity.this,isLoadingProducts->{
            if(isLoadingProducts){
                progressBarLoadingInitialUsers.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }else{
                progressBarLoadingInitialUsers.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
    }

    public void observeErrorLiveData(){
        productViewModel.getErrorLiveData().observe(this, error -> {
            if(error){
                navigateToErrorActivity();
            }else {
                checkAndShowWelcomeDialog();
            }
        });
    }

    private void  navigateToErrorActivity(){
        startActivity(new Intent(ArchivedProductsActivity.this, ErrorLoadingProducts.class));
    }

    private boolean shouldShowWelcomeDialog() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Constants.PREFS_NAME1, Context.MODE_PRIVATE);
        return !sharedPreferences.getBoolean(Constants.PREF_DONT_SHOW_AGAIN, false);
    }

    private void checkAndShowWelcomeDialog() {
        if (!isDialogShown && shouldShowWelcomeDialog()) {
            showWelcomeDialog();
            isDialogShown = true;
        }
    }

    private void showWelcomeDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_welcome, null);

        AlertDialog alertDialog = new AlertDialog.Builder(ArchivedProductsActivity.this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        CheckBox checkboxDontShow = dialogView.findViewById(R.id.checkbox_dont_show);
        Button btnOk = dialogView.findViewById(R.id.btn_ok);
        Button learnMoreBtn = dialogView.findViewById(R.id.btn_learn_more);

        btnOk.setOnClickListener(v -> {

            if (checkboxDontShow.isChecked()) {
                SharedPreferences.Editor editor = this.getSharedPreferences(Constants.PREFS_NAME1, Context.MODE_PRIVATE).edit();
                editor.putBoolean(Constants.PREF_DONT_SHOW_AGAIN, true);
                editor.apply();
            }

            alertDialog.dismiss();
        });

        learnMoreBtn.setOnClickListener(v -> {

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.EASY_SALE_URL));
            startActivity(browserIntent);
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void initRecyclerViewAndAdapter(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        productAdapter = new ProductAdapter(ProductAdapter.DIFF_CALLBACK,this,getApplication());
        recyclerView.setAdapter(productAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.custom_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void initViews(){
        deleteSelectedProductsImageButton = findViewById(R.id.button_deleteSelectedProducts);
        sortingSpinner = findViewById(R.id.spinner_sort_options);
//        floatingActionButton = findViewById(R.id.fab_add_user);
        recyclerView = findViewById(R.id.recycler_view);
        searchEditText = findViewById(R.id.editSearch);
        progressBar = findViewById(R.id.progressBarId);
        progressBarLoadingInitialUsers = findViewById(R.id.progressBarLoadingInitialUsers);
        emptyResultsImageView = findViewById(R.id.empty_results_image);
        emptyResultsTextView = findViewById(R.id.empty_results_text);
        buttonListLayout = findViewById(R.id.button_list_layout);
        buttonGridLayout = findViewById(R.id.button_grid_layout);
        layoutSwitchGroup = findViewById(R.id.layoutSwitchGroup);
        currentPageTextView = findViewById(R.id.current_page);
        pageSeparatorTextView = findViewById(R.id.page_separator);
        totalPagesTextView = findViewById(R.id.total_pages);
        prevButton = findViewById(R.id.button_prev);
        nextButton = findViewById(R.id.button_next);
        deleteAllUsersImageButton = findViewById(R.id.button_deleteAll);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
    }

    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
    }

    private void observeSortedProducts(String sortField, boolean ascending) {
        productViewModel.loadArchivedProductsSortedBy(sortField, ascending, (CURRENT_PAGE - 1) * Constants.ITEMS_PER_PAGE, Constants.ITEMS_PER_PAGE)
                .observe(this, products -> {
                    productAdapter.submitData(getLifecycle(), products);
                    handleEmptyResults();
                });
    }


    private void observeAllProducts(){
        productViewModel.loadArchivedProductsByPage((CURRENT_PAGE-1)*Constants.ITEMS_PER_PAGE,Constants.ITEMS_PER_PAGE).observe(this,products -> {
            productAdapter.submitData(getLifecycle(), products);
            handleEmptyResults();
        });
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyResultsTextView.setVisibility(View.GONE);
        emptyResultsImageView.setVisibility(View.GONE);
        buttonListLayout.setVisibility(View.GONE);
        buttonGridLayout.setVisibility(View.GONE);
        deleteAllUsersImageButton.setVisibility(View.GONE);
        deleteSelectedProductsImageButton.setVisibility(View.GONE);
        sortingSpinner.setVisibility(View.GONE);
        prevButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        pageSeparatorTextView.setVisibility(View.GONE);
        currentPageTextView.setVisibility(View.GONE);
        totalPagesTextView.setVisibility(View.GONE);
    }


    private void setupEditTextSearch() {

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                showLoadingState();

                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    CURRENT_PAGE = 1;
                    setCurrentPage(CURRENT_PAGE);
                    if(s.toString().isEmpty()){
                        observeAllProducts();
                    }else {
                        searchArchivedProducts(s.toString());
                    }
                };

                handler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void handleEmptyResults() {
        productAdapter.addLoadStateListener(loadStates -> {
            if (loadStates.getRefresh() instanceof LoadState.NotLoading && productAdapter.getItemCount() == 0) {
                if (CURRENT_PAGE > 1) {
                    CURRENT_PAGE -= 1;
                    setCurrentPage(CURRENT_PAGE);
                    observeAllProducts();
                } else {
                    showNoResults();
                }
            } else {
                showResults();
            }
            progressBar.setVisibility(View.GONE);
            return null;
        });
    }

    private void showResults() {
        recyclerView.setVisibility(View.VISIBLE);
        sortingSpinner.setVisibility(View.VISIBLE);
        emptyResultsImageView.setVisibility(View.GONE);
        emptyResultsTextView.setVisibility(View.GONE);


        if (TOTAL_PAGES > 1) {
            buttonListLayout.setVisibility(View.VISIBLE);
            buttonGridLayout.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            currentPageTextView.setVisibility(View.VISIBLE);
            pageSeparatorTextView.setVisibility(View.VISIBLE);
            totalPagesTextView.setVisibility(View.VISIBLE);
        } else {
            buttonListLayout.setVisibility(View.VISIBLE);
            buttonGridLayout.setVisibility(View.VISIBLE);
            prevButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.GONE);
            currentPageTextView.setVisibility(View.GONE);
            pageSeparatorTextView.setVisibility(View.GONE);
            totalPagesTextView.setVisibility(View.GONE);
        }

        deleteAllUsersImageButton.setVisibility(View.VISIBLE);
    }



    private void showNoResults() {
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        sortingSpinner.setVisibility(View.GONE);
        emptyResultsImageView.setVisibility(View.VISIBLE);
        emptyResultsTextView.setVisibility(View.VISIBLE);
        buttonListLayout.setVisibility(View.GONE);
        buttonGridLayout.setVisibility(View.GONE);
        prevButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        currentPageTextView.setVisibility(View.GONE);
        pageSeparatorTextView.setVisibility(View.GONE);
        totalPagesTextView.setVisibility(View.GONE);
        deleteAllUsersImageButton.setVisibility(View.GONE);
    }

    private void searchArchivedProducts(String query) {

        productViewModel.searchArchivedProductsWithPagination(query,(CURRENT_PAGE-1)*Constants.ITEMS_PER_PAGE,Constants.ITEMS_PER_PAGE).observe(this, usersData -> {
            productAdapter.submitData(getLifecycle(),usersData);
            handleEmptyResults();
        });
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(ArchivedProductsActivity.this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int itemPosition = viewHolder.getAbsoluteAdapterPosition();
                Product product = productAdapter.getProduct(itemPosition);
                if(direction == ItemTouchHelper.LEFT) {

                    Snackbar snackbar = Snackbar.make(viewHolder.itemView,"You removed "+product.getTitle(),Snackbar.LENGTH_LONG);
                    snackbar.setAction(Constants.UNDO, v -> {

                        productViewModel.insertProduct(product).observe(ArchivedProductsActivity.this, success -> {
                            if (Constants.SUCCESS.equals(success)) {
                                Toast.makeText(ArchivedProductsActivity.this, product.getTitle() + " restored successfully!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    AlertDialog dialog = new AlertDialog.Builder(ArchivedProductsActivity.this)
                            .setMessage("Do you want to delete \"" + product.getTitle() + "\"?")
                            .setView(R.layout.custom_dialog_buttons)
                            .setCancelable(false)
                            .create();

                    dialog.setOnShowListener(dialogInterface -> {

                        Button positiveButton = dialog.findViewById(R.id.dialog_positive_button);
                        Button negativeButton = dialog.findViewById(R.id.dialog_negative_button);

                        positiveButton.setOnClickListener(v -> {
                            if(itemPosition!=-1){

                                productViewModel.deleteProduct(product).observe(ArchivedProductsActivity.this, success -> {

                                    if (Constants.SUCCESS.equals(success)) {
                                        Toast.makeText(ArchivedProductsActivity.this, product.getTitle() + " removed successfully!", Toast.LENGTH_SHORT).show();
                                        snackbar.show();
                                        dialog.dismiss();
                                    }else {
                                        Toast.makeText(ArchivedProductsActivity.this, "Failed to remove " + product.getTitle() + ". Please try again.", Toast.LENGTH_SHORT).show();
                                        productAdapter.notifyItemChanged(itemPosition);
                                        dialog.dismiss();
                                    }
                                });
                            }

                        });

                        negativeButton.setOnClickListener(v -> {
                            productAdapter.notifyItemChanged(itemPosition);
                            dialog.dismiss();
                        });
                    });

                    dialog.show();
                }

                if(direction == ItemTouchHelper.RIGHT){

                    Snackbar snackbar = Snackbar.make(viewHolder.itemView,"You unarchived "+product.getTitle(),Snackbar.LENGTH_LONG);
                    snackbar.setAction(Constants.UNDO, v -> {

                        productViewModel.updateProductArchivedState(product.getId(),true).observe(ArchivedProductsActivity.this, success -> {
                            if (Constants.SUCCESS.equals(success)) {
                                Toast.makeText(ArchivedProductsActivity.this, product.getTitle() + " restored successfully!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    AlertDialog dialog = new AlertDialog.Builder(ArchivedProductsActivity.this)
                            .setMessage("Do you want to unarchive \"" + product.getTitle() + "\"?")
                            .setView(R.layout.custom_dialog_unarchive_buttons)
                            .setCancelable(false)
                            .create();

                    dialog.setOnShowListener(dialogInterface -> {

                        Button positiveButton = dialog.findViewById(R.id.dialog_positive_button);
                        Button negativeButton = dialog.findViewById(R.id.dialog_negative_button);

                        positiveButton.setOnClickListener(v -> {
                            if(itemPosition!=-1){

                                productViewModel.updateProductArchivedState(product.getId(),false).observe(ArchivedProductsActivity.this, success -> {

                                    if (Constants.SUCCESS.equals(success)) {
                                        Toast.makeText(ArchivedProductsActivity.this, product.getTitle() + " archived successfully!", Toast.LENGTH_SHORT).show();
                                        snackbar.show();
                                        dialog.dismiss();
                                    }else {
                                        Toast.makeText(ArchivedProductsActivity.this, "Failed to archive " + product.getTitle() + ". Please try again.", Toast.LENGTH_SHORT).show();
                                        productAdapter.notifyItemChanged(itemPosition);
                                        dialog.dismiss();
                                    }
                                });
                            }

                        });

                        negativeButton.setOnClickListener(v -> {
                            productAdapter.notifyItemChanged(itemPosition);
                            dialog.dismiss();
                        });
                    });

                    dialog.show();
                }
            }
        };
        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }

//    private void navigateToAddProductActivity(){
//        floatingActionButton.setOnClickListener(view -> startActivity(new Intent(ArchivedProductsActivity.this, AddProductActivity.class))
//        );
//    }

    @Override
    public void onClickProduct(Product product) {
        Intent intent = new Intent(ArchivedProductsActivity.this, ProductDetailsActivity.class);
        intent.putExtra(Constants.PRODUCT_MODEL,product);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String currentActivity = this.getClass().getSimpleName();

        int id = item.getItemId();
        if (id == R.id.nav_home) {
            if (!currentActivity.equals(MainActivity.class.getSimpleName())) {
                Intent homeIntent = new Intent(this, MainActivity.class);
                startActivity(homeIntent);
                finish();
            }
        } else if (id == R.id.nav_add_new_product) {
            startActivity(new Intent(ArchivedProductsActivity.this, AddProductActivity.class));
        } else if (id == R.id.nav_graphs) {
            startActivity(new Intent(ArchivedProductsActivity.this, GraphsActivity.class));
        } else if(id == R.id.nav_archived_products){
            startActivity(new Intent(ArchivedProductsActivity.this, ArchivedProductsActivity.class));
        }else {
            return false;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}