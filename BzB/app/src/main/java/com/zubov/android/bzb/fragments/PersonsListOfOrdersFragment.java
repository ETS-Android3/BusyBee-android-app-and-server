package com.zubov.android.bzb.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zubov.android.bzb.OrderPagerActivity;
import com.zubov.android.bzb.PersonActivity;
import com.zubov.android.bzb.PersonListOfOrdersActivity;
import com.zubov.android.bzb.R;
import com.zubov.android.bzb.model.DTO.UserIdDTO;
import com.zubov.android.bzb.model.DTO.UserOrderDTO;
import com.zubov.android.bzb.model.DTO.UserResponseInfoDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonsListOfOrdersFragment extends Fragment {

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String TOKEN = "token";
    public static final String USER_ID = "user_id";
    public static final String EMAIL = "user_email";
    public static final String LOGIN_EMAIL = "user_email";
    private List<UserOrderDTO> mOrderList;
    private RecyclerView mOrdersRecyclerView;
    private OrdersAdapter mAdapter;
    private int mUserId;
    private String mUserToken;
    Context ctx;
    SharedPreferences mShearedPrefs;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.orders_list_fragment, container,
                false);
        ctx = requireActivity().getApplicationContext();
        //currentFragment =(PersonsListOfOrdersFragment) this;
        mOrderList = new ArrayList<>();
        mOrdersRecyclerView = (RecyclerView) v
                .findViewById(R.id.order_list_recycler_view);
        mOrdersRecyclerView.setLayoutManager(new LinearLayoutManager
                (getActivity()));
        mShearedPrefs = requireActivity().getSharedPreferences(SHEARED_PREFS, Context.MODE_PRIVATE);
        mUserId = mShearedPrefs.getInt(USER_ID,0);
        mUserToken = mShearedPrefs.getString(TOKEN,"");

        updateUI();
        createOrderList();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }


    private void updateUI() {
        if (mAdapter == null) {
            mAdapter = new OrdersAdapter(mOrderList);
            mOrdersRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
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
                        List<UserOrderDTO> tmp = response.body();
                        if (tmp.size() != 0){
                            UserOrderDTO order = tmp.get(0);
                            String description = order.getText();
                            String date = order.getOrderdate();
                            boolean status = order.isStatus();
                            if(description.isEmpty() && date.isEmpty() && status){
                                SharedPreferences.Editor editor = mShearedPrefs.edit();
                                editor.putString(TOKEN,"");
                                editor.apply();
                                Intent intent = new Intent(getActivity(), PersonActivity.class);
                                startActivity(intent);
                                getActivity().overridePendingTransition(0,0);
                            }
                        }
                        mOrderList.addAll(tmp);
                        mOrdersRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                    @Override
                    public void onFailure(Call<List<UserOrderDTO>> call,
                                          Throwable t) {
                        new Toaster(getActivity().getApplicationContext(),"ошибка при создании листа").ShowToast();  // R.string.err_server
                    }
                });
    }


    private class OrderListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private UserOrderDTO mUserOrder;
        private final TextView mDateTextView;
        private final TextView mDescriptionTextView;
        private final ImageView mSolvedImageView;
        private final TextView mSolvedImageDescription;

        public OrderListHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_order, parent, false));
            itemView.setOnClickListener(this);

            mDateTextView = (TextView) itemView.findViewById(R.id.tv_lio_date);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.tv_lio_description);
            mSolvedImageView = (ImageView) itemView.findViewById(R.id.iv_lio);
            mSolvedImageDescription = (TextView) itemView.findViewById(R.id.tv_lio_image_description);
        }

        public void bind (UserOrderDTO userOrder) {
            if (userOrder == null) return;
            mUserOrder = userOrder;
            //mOrder = mUserOrder;
            mDateTextView.setText(userOrder.getOrderdate());
            StringBuilder descriptionOrder = new StringBuilder();
            String fullDescription = userOrder.getText();
            if (fullDescription == null) fullDescription = "no description";
            if (fullDescription.length() > 30){
                descriptionOrder
                        .append(fullDescription.substring(0,30));
            }
            else {
                descriptionOrder.append(fullDescription);
            }
            descriptionOrder.append("...");
            mDescriptionTextView.setText(descriptionOrder.toString());
            if (!userOrder.isStatus()){
                mSolvedImageView.setImageResource(R.drawable.ic_location_off_dark);
                mSolvedImageDescription.setText(R.string.is_not_active);
            }

        }

        @Override
        public void onClick(View v) {
            Intent intent = OrderPagerActivity.newIntent(getActivity(),mUserOrder.getOrderId());
            startActivity(intent);
        }
    }

    private class OrdersAdapter extends RecyclerView.Adapter<OrderListHolder> {
        private List<UserOrderDTO> mPersonOrders;
        public OrdersAdapter (List<UserOrderDTO> userOrderDTO) {mPersonOrders = userOrderDTO;}

        @NonNull
        @Override
        public OrderListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new OrderListHolder(layoutInflater,parent);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderListHolder holder, int position) {
            UserOrderDTO personOrder = mPersonOrders.get(position);
            holder.bind(personOrder);
        }

        @Override
        public int getItemCount() {
            return mPersonOrders.size();
        }
    }

}
