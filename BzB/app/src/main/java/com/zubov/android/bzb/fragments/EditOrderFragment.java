package com.zubov.android.bzb.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zubov.android.bzb.MainActivity;
import com.zubov.android.bzb.PersonActivity;
import com.zubov.android.bzb.R;

public class EditOrderFragment extends Fragment {
    Context ctx;
    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String DESCRIPTION = "description_order";

    private Button mBack;
    private Button mNext;
    private TextInputEditText mTextEditor;
    private SharedPreferences mPrefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_order,container,false);
        ctx = requireActivity().getApplicationContext();
        mPrefs = ctx.getSharedPreferences(SHEARED_PREFS,Context.MODE_PRIVATE);
        mBack = v.findViewById(R.id.b_eo_back);
        mNext = v.findViewById(R.id.b_eo_next);
        mNext.setEnabled(false);
        mTextEditor = v.findViewById(R.id.et_eo);
        mTextEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
        String descriptionOrder = mPrefs.getString(DESCRIPTION,"");
        if(!descriptionOrder.isEmpty()){
            mTextEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
            mTextEditor.setText(descriptionOrder);
            mNext.setEnabled(true);
        }

        mTextEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mNext.setEnabled(s.length() > 10);
                    mTextEditor.setTextSize(TypedValue.COMPLEX_UNIT_SP,24);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(DESCRIPTION,mTextEditor.getText().toString());
                editor.apply();
                requireActivity().onBackPressed();
            }
        });

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save description from textedit to shearedPreferences
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putString(DESCRIPTION,mTextEditor.getText().toString());
                editor.apply();
                //open main activity to place the marker
                Intent intent = new Intent(ctx, MainActivity.class);
                intent.putExtra("add",1);
                requireActivity().startActivity(intent);
            }
        });

        return v;
    }
}
