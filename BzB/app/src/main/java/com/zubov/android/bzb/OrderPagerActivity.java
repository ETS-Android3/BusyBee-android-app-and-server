package com.zubov.android.bzb;

import static androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.zubov.android.bzb.fragments.OrderFragment;
import com.zubov.android.bzb.model.DTO.OrderToUpdateDTO;
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

public class OrderPagerActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_ID =
            "com.zubov.android.bzb.order_id";

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String TOKEN = "token";
    public static final String USER_ID = "user_id";

    private Button mBackToList;
    private ViewPager2 mViewPager;
    private static List<UserOrderDTO> sOrderList = new ArrayList<>();
    private static List<OrderToUpdateDTO> sChangedOrderList = new ArrayList<>(); //12
    private int mUserId;
    private int mOrderId;
    private String mUserToken;
    private Context ctx;


    public static Intent newIntent(Context packageContext, int orderId) {
        Intent intent = new Intent(packageContext, OrderPagerActivity.class);
        intent.putExtra(EXTRA_ORDER_ID, orderId);
        return intent;
    }

    public static List<UserOrderDTO> getOrderList(){
        return sOrderList;
    }

    public static void addToChangeList(OrderToUpdateDTO orderToUpdate){  // 12

        for(OrderToUpdateDTO x: sChangedOrderList){
            if (x.getOrderId() == orderToUpdate.getOrderId()){
                x.setStatus(orderToUpdate.isStatus());
                x.setText(orderToUpdate.getText());
                return;
            }
        }
        sChangedOrderList.add(orderToUpdate);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(sOrderList.size()>0) sOrderList.clear(); ///!!!
        int orderId = (int) getIntent().getSerializableExtra(EXTRA_ORDER_ID);
        mOrderId = orderId;
        setContentView(R.layout.activity_order_pager);
        ctx = getApplicationContext();
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
                        //overridePendingTransition(0,0);
                        return true;
                    case R.id.nav_user:
                        startActivity(new Intent(getApplicationContext(),PersonActivity.class));
                        //overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        mBackToList = findViewById(R.id.b_ao_back_to_list);
        mBackToList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(),PersonListOfOrdersActivity.class));
            }
        });

        SharedPreferences sharedPrefs = getSharedPreferences(SHEARED_PREFS, Context.MODE_PRIVATE);
        mUserId = sharedPrefs.getInt(USER_ID,0);
        mUserToken = sharedPrefs.getString(TOKEN,"");
        mViewPager = (ViewPager2) findViewById(R.id.crime_view_pager);


        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStateAdapter(fragmentManager,getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return OrderFragment.newInstance(position);
            }
            @Override
            public int getItemCount() {
                return sOrderList.size();
            }
        });

        createOrderList();
    }

    public void onMadeChanges() {                                     // 12
        if (sChangedOrderList != null && sChangedOrderList.size()>0){
            sChangedOrderList.get(0).setToken(mUserToken);
            updateOrders(sChangedOrderList);
        }
        sChangedOrderList.clear();
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mOrderId = mViewPager.getCurrentItem();
        outState.putInt(EXTRA_ORDER_ID,mOrderId);

    }

    private void createOrderList(){
        UserIdDTO ordersMaster = new UserIdDTO(mUserId,mUserToken);
        NetworkService
                .getInstance()
                .getJSONApi()
                .getOrdersList(ordersMaster)
                .enqueue(new Callback<List<UserOrderDTO>>() {

                    @Override
                    public void onResponse(Call<List<UserOrderDTO>> call,
                                           Response<List<UserOrderDTO>> response) {

                        if (!response.isSuccessful()){
                            new Toaster(ctx,
                                    String.valueOf(response.code())).ShowToast();
                            return;
                        }
                        if (response.body() == null){
                            new Toaster(ctx,
                                    ctx.getResources().getString(R.string.token_null)).ShowToast();
                            return;
                        }
                        sOrderList.clear();
                        sOrderList.addAll(response.body());
                        mViewPager.getAdapter().notifyDataSetChanged();

                        for (int i = 0; i < sOrderList.size(); i++) {
                            if (sOrderList.get(i).getOrderId() == mOrderId) {
                                mViewPager.setCurrentItem(i);
                                break;
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<UserOrderDTO>> call,
                                          Throwable t) {
                        new Toaster(ctx,R.string.err_server).ShowToast();
                    }

                });
    }

    private void updateOrders(List<OrderToUpdateDTO> list){
        if (list == null) return;
        NetworkService
            .getInstance()
            .getJSONApi()
            .updateOrders(list)
            .enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (!response.isSuccessful()){
                        new Toaster(ctx,
                                String.valueOf(response.code())).ShowToast();
                        return;
                    }
                    if (response.body() == null){
                        new Toaster(ctx,
                                ctx.getResources().getString(R.string.token_null)).ShowToast();
                        return;
                    }
                    Boolean result = response.body();
                    if (result) {
                        new Toaster(ctx,
                                ctx.getResources().getString(R.string.update_is_ok)).ShowToast();
                        mViewPager.getAdapter().notifyDataSetChanged();
                    }
                    else {
                        new Toaster(ctx,
                                ctx.getResources().getString(R.string.update_bad)).ShowToast();
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                    new Toaster(ctx,R.string.err_server).ShowToast();
                }
            });
    }

}
