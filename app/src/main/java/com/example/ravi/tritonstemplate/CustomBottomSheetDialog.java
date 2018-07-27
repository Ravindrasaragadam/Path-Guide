package com.example.ravi.tritonstemplate;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CustomBottomSheetDialog extends BottomSheetDialogFragment {

   // static ViewGroup container;
    static View view;
    public static CustomBottomSheetDialog getInstance() {
        return new CustomBottomSheetDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //this.container=container;
        view=inflater.inflate(R.layout.layout_custom_bottom_sheet, container, false);
        return view;
    }
}
