package com.zubov.android.bzb;

import static com.zubov.android.bzb.OrderPagerActivity.USER_ID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.zubov.android.bzb.fragments.LoginFragment;
import com.zubov.android.bzb.fragments.UserPageFragment;
import com.zubov.android.bzb.model.DTO.UserCheckDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import java.util.Objects;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonActivity extends AppCompatActivity implements DeleteUserDialog.DeleteUserDialogListener, UserPageFragment.UserPageFragmentListener {

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String TOKEN = "token";
    public static final String EMAIL = "user_email";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        Objects.requireNonNull(getSupportActionBar()).hide();
        LoginFragment loginFragment = new LoginFragment();
        UserPageFragment userPageFragment = new UserPageFragment();

        //initialize & assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Set map selected
        bottomNavigationView.setSelectedItemId(R.id.nav_user);
        // Perform itemSelectListener
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id){
                    //check id
                    case R.id.nav_map:
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        return true;
                    case R.id.nav_user:
                        return true;
                }
                return false;
            }
        });

        SharedPreferences mSharedPreferences = getSharedPreferences(SHEARED_PREFS,MODE_PRIVATE);
        String token = mSharedPreferences.getString(TOKEN, "");
        if(token.isEmpty()){
            String email = mSharedPreferences.getString(EMAIL,"");
            Bundle args = new Bundle();
            args.putString(EMAIL,email);
            loginFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_person,
                    loginFragment,"LOGIN_FRAGMENT").commit();
        }
        else{
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_person,
                    userPageFragment,"USER_PAGE_FRAGMENT").commit();
        }


    }


    public static boolean checkEmailValid(CharSequence text){

        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(text).matches();
    }

    public static boolean checkPasswordValid(@NonNull CharSequence text){
        return text.length() > 4;
    }

    @Override
    public void applyUserData(String email, String password) {
        if (email == null || password == null ) return;
        UserCheckDTO userToDelete = new UserCheckDTO(email,password);
        NetworkService
                .getInstance()
                .getJSONApi()
                .deleteUser(userToDelete)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (!response.isSuccessful()){
                            new Toaster(getApplicationContext(),String.valueOf(response.code())).ShowToast();
                            return;
                        }
                        if (response.body() == null){
                            new Toaster(getApplicationContext(), R.string.token_null).ShowToast();
                        }
                        else if (response.body()){
                            clearShearedPrefs();
                            new Toaster(getApplicationContext(), R.string.action_user_deleted_success).ShowToast();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }
                        else{
                            new Toaster(getApplicationContext(), R.string.action_user_not_deleted).ShowToast();
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        new Toaster(getApplicationContext(), R.string.err_server).ShowToast();
                        t.printStackTrace();
                    }
                });
    }

    @Override
    public void doItNow() {
        DeleteUserDialog deleteDialog = new DeleteUserDialog();
        deleteDialog.show(getSupportFragmentManager(),"DELETE_USER");
    }
    private void clearShearedPrefs(){
        SharedPreferences sp = getSharedPreferences(SHEARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(USER_ID,0);
        editor.putString(EMAIL,"");
        editor.putString(TOKEN,"");
        editor.apply();
    }
}