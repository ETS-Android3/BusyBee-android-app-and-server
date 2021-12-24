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

import com.zubov.android.bzb.PersonActivity;
import com.zubov.android.bzb.R;
import com.zubov.android.bzb.model.DTO.UserCheckDTO;
import com.zubov.android.bzb.model.DTO.UserIdDTO;
import com.zubov.android.bzb.model.DTO.UserResponseInfoDTO;
import com.zubov.android.bzb.model.DTO.UserUpdatePasswordDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordFragment extends Fragment {

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String EMAIL = "user_email";
    public static final String TOKEN = "token";
    public static final String USER_ID = "user_id";
    private SharedPreferences mShearedPrefs;

    private Context ctx;
    private String mEmail;

    private EditText mCurPassET;
    private EditText mNewPassET;
    private EditText mNewPassConfirmET;
    private Button mBtnChangePass;
    private Button mBtnBackToUserPage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_change_password, container, false);
        ctx = getActivity().getApplicationContext();
        mShearedPrefs = ctx.getSharedPreferences(SHEARED_PREFS, MODE_PRIVATE);
        mEmail = mShearedPrefs.getString(EMAIL, "");

        mCurPassET = v.findViewById(R.id.et_cp_password);
        mNewPassET = v.findViewById(R.id.et_cp_new_password);
        mNewPassConfirmET = v.findViewById(R.id.et_cp_new_password_confirm);
        mBtnChangePass = v.findViewById(R.id.b_cp_change_pass);
        mBtnBackToUserPage = v.findViewById(R.id.b_cp_back_to_user_page);

        setListenersOnFields();
        setListenersOnButtons();

        return v;
    }

    private void setListenersOnButtons() {
        mBtnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = mNewPassET.getText().toString();
                String newPasswordConfirm = mNewPassConfirmET.getText().toString();
                if (newPassword.equals(newPasswordConfirm))
                {
                    changePassword();
                    requireActivity().onBackPressed();
                }
                else{
                    new Toaster(ctx,R.string.cp_pass_not_equals).ShowToast();
                }
            }
        });

        mBtnBackToUserPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });
    }

    private void setListenersOnFields() {
        mCurPassET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mCurPassET.hasFocus()) return;
                if (!PersonActivity.checkPasswordValid(mCurPassET.getText())) {
                    Toast toast = Toast.makeText(ctx,
                            R.string.invalid_password,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 150);
                    toast.show();
                } else {
                    if (PersonActivity.checkPasswordValid(mNewPassET.getText()) &&
                            PersonActivity.checkPasswordValid(mNewPassConfirmET.getText())) {
                        mBtnChangePass.setEnabled(true);
                    }
                }
            }
        });
        mNewPassET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mNewPassET.hasFocus()) return;
                if (!PersonActivity.checkPasswordValid(mNewPassET.getText())) {
                    Toast toast = Toast.makeText(ctx,
                            R.string.invalid_password,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 150);
                    toast.show();
                } else {
                    if (PersonActivity.checkPasswordValid(mCurPassET.getText()) &&
                            PersonActivity.checkPasswordValid(mNewPassConfirmET.getText())) {
                        mBtnChangePass.setEnabled(true);
                    }
                }
            }
        });
        mNewPassConfirmET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(mNewPassConfirmET.hasFocus()) return;
                if (!PersonActivity.checkPasswordValid(mNewPassConfirmET.getText())) {
                    Toast toast = Toast.makeText(ctx,
                            R.string.invalid_password,
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 150);
                    toast.show();
                } else {
                    if (PersonActivity.checkPasswordValid(mCurPassET.getText()) &&
                            PersonActivity.checkPasswordValid(mNewPassET.getText())) {
                        mBtnChangePass.setEnabled(true);
                    }
                }
            }
        });
        mCurPassET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mBtnChangePass.setEnabled(PersonActivity.checkPasswordValid(s) &&
                        PersonActivity.checkPasswordValid(mNewPassET.getText()) &&
                        PersonActivity.checkPasswordValid(mNewPassConfirmET.getText()));
            }
        });
        mNewPassET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mBtnChangePass.setEnabled(PersonActivity.checkPasswordValid(s) &&
                        PersonActivity.checkPasswordValid(mCurPassET.getText()) &&
                        PersonActivity.checkPasswordValid(mNewPassConfirmET.getText()));
            }
        });
        mNewPassConfirmET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mBtnChangePass.setEnabled(PersonActivity.checkPasswordValid(s) &&
                        PersonActivity.checkPasswordValid(mCurPassET.getText()) &&
                        PersonActivity.checkPasswordValid(mNewPassET.getText()));
            }
        });
    }

    private void changePassword() {
        String curPassword = mCurPassET.getText().toString();
        String newPassword = mNewPassET.getText().toString();
        if(curPassword.isEmpty() && mEmail.isEmpty() && newPassword.isEmpty()) return;
        UserUpdatePasswordDTO passToUpdate = new UserUpdatePasswordDTO();
        passToUpdate.setEmail(mEmail);
        passToUpdate.setOldPassword(curPassword);
        passToUpdate.setNewPassword(newPassword);
        NetworkService
                .getInstance()
                .getJSONApi()
                .updatePassword(passToUpdate)
                .enqueue(new Callback<UserResponseInfoDTO>() {
                    @Override
                    public void onResponse(Call<UserResponseInfoDTO> call, Response<UserResponseInfoDTO> response) {
                        if (!response.isSuccessful()) {
                            new Toaster(ctx, String.valueOf(response.code())).ShowToast();
                            return;
                        }
                        assert response.body() != null;
                        String token = response.body().getUserToken();
                        if (token != null) {
                            int userId = response.body().getUserID();
                            if (userId == mShearedPrefs.getInt(USER_ID, 0) && userId != 0) {
                                SharedPreferences.Editor editor = mShearedPrefs.edit();
                                editor.putString(TOKEN, token);
                                editor.apply();
                                new Toaster(ctx, R.string.action_change_password_success).ShowToast();
                            } else if (userId == -1) {
                                new Toaster(ctx, R.string.password_failed).ShowToast();
                            }
                        } else {
                            new Toaster(ctx, R.string.token_null).ShowToast();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponseInfoDTO> call, Throwable t) {
                        new Toaster(ctx, R.string.err_server).ShowToast();
                        t.printStackTrace();
                    }
                });

    }
}
