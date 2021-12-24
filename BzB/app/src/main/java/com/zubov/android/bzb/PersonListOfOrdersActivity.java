package com.zubov.android.bzb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.zubov.android.bzb.fragments.LoginFragment;
import com.zubov.android.bzb.fragments.PersonsListOfOrdersFragment;
import com.zubov.android.bzb.fragments.UserPageFragment;
import com.zubov.android.bzb.model.DTO.UserIdDTO;
import com.zubov.android.bzb.model.DTO.UserOrderDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonListOfOrdersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_list_of_orders);
        Objects.requireNonNull(getSupportActionBar()).hide();


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
                        startActivity(new Intent(getApplicationContext(),PersonActivity.class));
                        return true;
                }
                return false;
            }
        });
        Button backToUserPage = findViewById(R.id.b_pl_back);
        backToUserPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),PersonActivity.class));
            }
        });
        PersonsListOfOrdersFragment fragment = new PersonsListOfOrdersFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container_person_list_of_orders,fragment,"ORDERS_LIST_FRAGMENT").commit();
    }

}
