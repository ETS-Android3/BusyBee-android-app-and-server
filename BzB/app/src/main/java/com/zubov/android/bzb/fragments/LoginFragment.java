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

public class LoginFragment extends Fragment {
    Context ctx;
    private String mEmail;
    private int mUserId;
    private String mToken;

    private EditText mEmailET;
    private EditText mPassET;
    private Button mBtnLogin;

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String EMAIL = "user_email";
    public static final String TOKEN = "token";
    public static final String USER_ID = "user_id";
    public static final String LOGIN_EMAIL = "user_email";

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
        View v = inflater.inflate(R.layout.fragment_login,container,false);
        ctx = requireActivity().getApplicationContext();
        mEmailET = v.findViewById(R.id.et_log_username);
        mPassET = v.findViewById(R.id.et_log_password);
        mBtnLogin = v.findViewById(R.id.b_log_login);
        Button btnRegister = v.findViewById(R.id.b_log_register);
        btnRegister.setEnabled(true);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // redirect to registration fragment
                redirectToRegistration();
            }
        });
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
                            mBtnLogin.setEnabled(true);
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
                        mBtnLogin.setEnabled(true);
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
                mBtnLogin.setEnabled(PersonActivity.checkEmailValid(s) &&
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
                mBtnLogin.setEnabled(PersonActivity.checkEmailValid(mEmailET.getText()) &&
                        PersonActivity.checkPasswordValid(s));
            }
        });
        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        return v;
    }

    private void loginUser(){
        // create and fill the "package" with data to send
        UserCheckDTO userToCheck = new UserCheckDTO();
        mEmail = mEmailET.getText().toString();
        userToCheck.setUserEmail(mEmail);
        userToCheck.setUserPassword(mPassET.getText().toString());

        NetworkService
                .getInstance()
                .getJSONApi()
                .checkUser(userToCheck)
                .enqueue(new Callback<UserResponseInfoDTO>() {
                    @Override
                    public void onResponse(Call<UserResponseInfoDTO> call, Response<UserResponseInfoDTO> response) {
                        if (!response.isSuccessful()){
                            Toast toast = Toast.makeText(ctx,
                                    String.valueOf(response.code()),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                            return;
                        }
                        UserResponseInfoDTO userResponseObj = response.body();
                        if(userResponseObj != null){
                            mUserId = userResponseObj.getUserID();
                            mToken = userResponseObj.getUserToken();
                        }
                        if (userResponseObj == null || mUserId == 0 || mToken == null) {
                            Toast toast = Toast.makeText(ctx,
                                    R.string.token_null,
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                        }
                        else if(mUserId == -1){
                            Toast toast = Toast.makeText(ctx,
                                    R.string.no_such_user,
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                        }
                        else if(mUserId == -2){
                            Toast toast = Toast.makeText(ctx,
                                    R.string.password_failed,
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                        }
                        else {
                            SharedPreferences mSharedPreferences = ctx.getSharedPreferences(SHEARED_PREFS,MODE_PRIVATE);
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            editor.putInt(USER_ID,mUserId);
                            editor.putString(EMAIL,mEmail);
                            editor.putString(TOKEN,mToken);
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

    private void redirectToRegistration(){
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle args = new Bundle();
        args.putString(LOGIN_EMAIL,mEmailET.getText().toString());
        Fragment fragment = new RegistrationFragment();
        fragment.setArguments(args);
        fragmentTransaction.replace(R.id.fragment_container_person,
                fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
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
