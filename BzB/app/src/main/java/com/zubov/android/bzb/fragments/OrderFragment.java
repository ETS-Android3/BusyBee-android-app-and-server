package com.zubov.android.bzb.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zubov.android.bzb.OrderPagerActivity;
import com.zubov.android.bzb.PersonListOfOrdersActivity;
import com.zubov.android.bzb.R;
import com.zubov.android.bzb.model.DTO.OrderResponseDTO;
import com.zubov.android.bzb.model.DTO.OrderToUpdateDTO;
import com.zubov.android.bzb.model.DTO.UserOrderDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OrderFragment extends Fragment {

    private static final String ARG_ORDER_ID = "bzb.order_id";

    private UserOrderDTO mUserOrder;
    private Switch mSwitchStatus;
    private boolean mOriginStateSwitcher;
    private boolean mChangesSwitcher;
    private TextView mTvStatus;
    private CheckBox mChBoxEdit;
    private Button mButtonDelete;
    private Button mRefresh;
    private EditText mEtDescription;
    private String mOriginDescription;
    private Context ctx;

    public static OrderFragment newInstance(int orderId) {
        Bundle args = new Bundle();
        args.putInt(ARG_ORDER_ID, orderId);
        OrderFragment fragment = new OrderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orderId = (int) getArguments().getInt(ARG_ORDER_ID);
        mUserOrder = OrderPagerActivity.getOrderList().get(orderId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.order_fragment, container, false);
        ctx = requireActivity().getApplicationContext();
        mSwitchStatus = v.findViewById(R.id.switch_o_status);
        mTvStatus = v.findViewById(R.id.tv_o_status);

        TextView tvDate = v.findViewById(R.id.tv_o_date);
        tvDate.setText(mUserOrder.getOrderdate());

        mEtDescription = v.findViewById(R.id.et_o_description);
        mEtDescription.setText(mUserOrder.getText());
        mEtDescription.setScroller(new Scroller(requireActivity().getApplicationContext()));
        mEtDescription.setVerticalScrollBarEnabled(true);
        mEtDescription.setMovementMethod(new ScrollingMovementMethod());
        mEtDescription.setEnabled(false);
        mOriginDescription = mUserOrder.getText();

        mSwitchStatus.setChecked(mUserOrder.isStatus());
        mTvStatus.setText(mSwitchStatus.isChecked() ? R.string.is_active : R.string.is_not_active);
        mOriginStateSwitcher = mUserOrder.isStatus();
        mSwitchStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvStatus.setText(mSwitchStatus.isChecked() ? R.string.is_active : R.string.is_not_active);
            }
        });
        mChBoxEdit = v.findViewById(R.id.cb_edit);
        mChBoxEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mChBoxEdit.isChecked()){
                    mEtDescription.setEnabled(true);
                }
                else {
                    mEtDescription.setEnabled(false);
                }
            }
        });
        mButtonDelete = v.findViewById(R.id.b_o_delete);
        mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = approveToDelete();
                dialog.show();
            }
        });

        mRefresh = v.findViewById(R.id.b_o_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderToUpdateDTO changedOrder = new OrderToUpdateDTO();
                changedOrder.setOrderId(mUserOrder.getOrderId());
                changedOrder.setText(mEtDescription.getText().toString());
                changedOrder.setStatus(mSwitchStatus.isChecked());
                OrderPagerActivity.addToChangeList(changedOrder);
                Activity a = getActivity();
                ((OrderPagerActivity) a).onMadeChanges();
            }
        });

        return v;
    }

    private AlertDialog approveToDelete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.title_approve)
                .setMessage(R.string.o_dialog_message)
                .setNegativeButton("нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteOrder(mUserOrder.getOrderId());
                    }
                });
        return builder.create();
    }

    private void deleteOrder(int orderId) {
        if(orderId == 0) return;
        OrderResponseDTO orderToDelete = new OrderResponseDTO();
        orderToDelete.setOrderID(orderId);
        NetworkService
                .getInstance()
                .getJSONApi()
                .deleteOrder(orderToDelete)
                .enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (!response.isSuccessful()){
                            new Toaster(ctx,String.valueOf(response.code())).ShowToast();
                            return;
                        }
                        if (response.body() == null) {
                            new Toaster(ctx,R.string.token_null).ShowToast();
                            return;
                        }
                        if(response.body()) {
                            new Toaster(ctx,R.string.o_delete_success);
                            startActivity(new Intent(getActivity(), PersonListOfOrdersActivity.class));
                        }
                        else {
                            new Toaster(ctx,R.string.o_delete_fail);
                        }
                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        t.printStackTrace();
                        new Toaster(ctx,R.string.o_delete_fail).ShowToast();
                    }
                });
    }
}
