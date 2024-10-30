package com.example.myapplication.utils;

import android.util.Patterns;
import android.widget.ImageView;

public class ValidationUtils {

    public static boolean validateTitle(String title){
        return title.isEmpty();
    }

    public static boolean validatePrice(String price){
        return price.isEmpty();
    }

    public static boolean validateCategory(String category){
        return category.isEmpty();
    }

    public static boolean validateDescription(String description){
        return description.isEmpty();
    }

    public static boolean validateImageView(ImageView avatarImageView){
        return avatarImageView == null;
    }

    public static boolean isValidEmail(CharSequence email) {
        return !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
