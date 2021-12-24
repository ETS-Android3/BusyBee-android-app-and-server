package com.zubov.android.bzb.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zubov.android.bzb.PersonActivity;
import com.zubov.android.bzb.R;
import com.zubov.android.bzb.model.DTO.UserCheckDTO;
import com.zubov.android.bzb.model.DTO.UserResponseInfoDTO;
import com.zubov.android.bzb.utils.NetworkService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationFragment extends Fragment {

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String EMAIL = "user_email";
    public static final String TOKEN = "token";
    public static final String USER_ID = "user_id";
    public static final String LOGIN_EMAIL = "user_email";

    private String mEmail;
    private EditText mEmailET;
    private EditText mPassET;
    private Button mBtnRegister;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            mEmail = getArguments().getString(LOGIN_EMAIL);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_registration,container,false);
        Context ctx = requireActivity().getApplicationContext();
        mEmailET = v.findViewById(R.id.et_reg_username);
        mPassET = v.findViewById(R.id.et_reg_password);
        mBtnRegister = v.findViewById(R.id.b_reg_register);
        mBtnRegister.setEnabled(false);
        if(mEmail != null) mEmailET.setText(mEmail);
        mEmailET.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mEmailET.hasFocus()) return;
                if (!PersonActivity.checkEmailValid(mEmailET.getText())) {
                    Toast toast = Toast.makeText(ctx,
                            R.string.invalid_username,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 150);
                    toast.show();
                } else {
                    if (PersonActivity.checkPasswordValid(mPassET.getText())) {
                        mBtnRegister.setEnabled(true);
                    }
                }

            }
        });
        mPassET.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mPassET.hasFocus()) return;
                if (!PersonActivity.checkPasswordValid(mPassET.getText())) {
                    Toast toast = Toast.makeText(ctx,
                            R.string.invalid_password,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 150);
                    toast.show();
                } else {
                    if (PersonActivity.checkEmailValid(mEmailET.getText())) {
                        mBtnRegister.setEnabled(true);
                    }
                }

            }
        });
        mEmailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                mBtnRegister.setEnabled(PersonActivity.checkEmailValid(s) &&
                        PersonActivity.checkPasswordValid(mPassET.getText()));
            }
        });
        mPassET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                mBtnRegister.setEnabled(PersonActivity.checkEmailValid(mEmailET.getText()) &&
                        PersonActivity.checkPasswordValid(s));
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        return v;
    }

    private void registerNewUser(){
        Context ctx = requireActivity().getApplicationContext();
        UserCheckDTO userToRegister = new UserCheckDTO();
        mEmail = mEmailET.getText().toString();
        userToRegister.setUserEmail(mEmail);
        userToRegister.setUserPassword(mPassET.getText().toString());

        NetworkService
                .getInstance()
                .getJSONApi()
                .registerUser(userToRegister)
                .enqueue(new Callback<UserResponseInfoDTO>() {
                    @Override
                    public void onResponse(Call<UserResponseInfoDTO> call, Response<UserResponseInfoDTO> response) {
                        if (!response.isSuccessful()){
                            Toast toast = Toast.makeText(ctx,
                                    response.code(),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                            return;
                        }
                        UserResponseInfoDTO userResponseObj = response.body();
                        if ( userResponseObj == null|| userResponseObj.getUserID() == 0
                                || userResponseObj.getUserToken() == null) {
                            Toast toast = Toast.makeText(ctx,
                                    R.string.token_null,
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                        }
                        else{

                            SharedPreferences mSharedPreferences =
                                    ctx
                                    .getSharedPreferences(SHEARED_PREFS,MODE_PRIVATE);
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(USER_ID,userResponseObj.getUserID());
                            editor.putString(EMAIL,mEmail);
                            editor.putString(TOKEN,userResponseObj.getUserToken());
                            editor.apply();
                            Toast toast = Toast.makeText(ctx,
                                    R.string.welcome,
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();

                            redirectToUserPage();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponseInfoDTO> call, Throwable t) {
                        Toast toast = Toast.makeText(ctx,
                                R.string.err_server,
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                        toast.show();
                        t.printStackTrace();
                    }
                });
    }

    private void redirectToUserPage(){
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_person,
                new UserPageFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
