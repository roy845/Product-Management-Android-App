package com.example.myapplication.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.myapplication.R;
import com.example.myapplication.constants.Constants;
import com.example.myapplication.interfaces.OnClickProductInterface;
import com.example.myapplication.models.Product;
import com.example.myapplication.viewmodel.ProductViewModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProductAdapter extends PagingDataAdapter<Product, ProductAdapter.ProductViewHolder> {

    private final Context context;
    private OnClickProductInterface onClickProductInterface;
    private final Map<Integer, Boolean> expandedItems;
    private final MutableLiveData<Map<Integer, Boolean>> selectedItemsLiveData;
    private ProductViewModel productViewModel;

    public ProductAdapter(@NonNull DiffUtil.ItemCallback<Product> diffCallback, Context context, Application application) {
        super(diffCallback);
        this.context = context;
        expandedItems = new HashMap<>();
        selectedItemsLiveData = new MutableLiveData<>(new HashMap<>());
        productViewModel = ProductViewModel.getInstance(application);
    }

    @NonNull
    @Override
    public ProductAdapter.ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        onClickProductInterface = (OnClickProductInterface) parent.getContext();
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(itemView);
    }

    public Product getProduct(int position){
        return getItem(position);
    }

    public MutableLiveData<Map<Integer, Boolean>> getSelectedItemsLiveData() {
        return selectedItemsLiveData;
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProductViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Product product = getItem(position);
        if(product != null){
            boolean isExpanded = Boolean.TRUE.equals(expandedItems.get(position));
            boolean isSelected = product.isSelected();

            holder.row_linearlayout.setOnClickListener(v -> onClickProductInterface.onClickProduct(product));

            holder.avatarImageView.setOnClickListener(v-> createDialog(product.getImage(),product));

            holder.row_linearlayout.setOnLongClickListener(v -> {
                if (Boolean.TRUE.equals(expandedItems.getOrDefault(position, false))) {
                    expandedItems.put(position, false);
                } else {
                    expandedItems.put(position, true);
                }
                notifyItemChanged(position);
                return true;
            });

            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(isSelected);

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                product.setSelected(isChecked);
                productViewModel.updateProductSelected(product.getId(),isChecked);

                Map<Integer, Boolean> selectedItems = selectedItemsLiveData.getValue();

                if (isChecked) {
                    selectedItems.put(position, true);
                } else {
                    selectedItems.remove(position);
                }

                selectedItemsLiveData.setValue(selectedItems);
            });


            try {
                holder.bindData(
                        product.getId(),
                        product.getTitle(),
                        product.getPrice(),
                        product.getDescription(),
                        product.getCategory(),
                        product.getImage(),
                        isExpanded,
                        isSelected
                );


            } catch (IOException e) {
                Log.e(Constants.PRODUCTS_ADAPTER, Constants.ERROR_BINDING_PRODUCT_DATA, e);
            }
        }
    }

    public static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Product>() {
                @Override
                public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.getId() == (newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
                    return oldItem.equals(newItem);
                }
            };

    private void createDialog(String imageUrl,Product product){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_image_recycler_view);
        ImageView dialogImage = dialog.findViewById(R.id.dialogImageRecyclerView);
        ImageView infoIcon = dialog.findViewById(R.id.infoIconRecyclerView);

        infoIcon.setOnClickListener(v->{
            dialog.dismiss();
            onClickProductInterface.onClickProduct(product);
        });

        RequestManager glideRequestManager;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isDestroyed() || activity.isFinishing()) {
                glideRequestManager = Glide.with(context.getApplicationContext());
            } else {
                glideRequestManager = Glide.with(activity);
            }
        } else {
            glideRequestManager = Glide.with(context.getApplicationContext());
        }

        glideRequestManager
                .load(imageUrl)
                .into(dialogImage);

        dialog.show();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder{

        private final Context context;
        private final View productItem;
        private final ImageView avatarImageView;
        private final TextView idTextView;
        private final TextView titleTextView;
        private final TextView priceTextView;
        private final TextView descriptionTextView;
        private final TextView categoryTextView;
        private final LinearLayout row_linearlayout;
        private final View extraDetailsLayout;
        private final CheckBox checkBox;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            this.context = itemView.getContext();
            this.productItem = itemView.findViewById(R.id.product_item);
            this.avatarImageView = itemView.findViewById(R.id.avatar);
            this.idTextView = itemView.findViewById(R.id.product_id);
            this.titleTextView = itemView.findViewById(R.id.product_title);
            this.priceTextView = itemView.findViewById(R.id.product_price);
            this.descriptionTextView = itemView.findViewById(R.id.product_description);
            this.categoryTextView = itemView.findViewById(R.id.product_category);
            this.row_linearlayout = itemView.findViewById(R.id.linearLayout);
            this.extraDetailsLayout = itemView.findViewById(R.id.expandable_layout);
            this.checkBox = itemView.findViewById(R.id.product_checkbox);
        }

        public void bindData(int id,String title, double price,String description,String category,String image,boolean isExpanded, boolean isSelected) throws IOException {
            idTextView.setText(String.valueOf(id));
            titleTextView.setText(title);
//            priceTextView.setText(String.format(Locale.US, "$%.2f", price));
//            descriptionTextView.setText(description);
//            categoryTextView.setText(category);
            extraDetailsLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            checkBox.setChecked(isSelected);

            if (isExpanded) {
                priceTextView.setText(String.format(Locale.US, "$%.2f", price));
                descriptionTextView.setText(description);
                categoryTextView.setText(category);
            }

            Glide.with(context)
                    .load(Uri.parse(image))
                    .into(avatarImageView);
        }
    }
}