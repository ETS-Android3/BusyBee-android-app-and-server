package com.zubov.android.bzb.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.zubov.android.bzb.DeleteUserDialog;
import com.zubov.android.bzb.MainActivity;
import com.zubov.android.bzb.PersonListOfOrdersActivity;
import com.zubov.android.bzb.R;
import com.zubov.android.bzb.model.DTO.UserCheckDTO;
import com.zubov.android.bzb.model.DTO.UserIdDTO;
import com.zubov.android.bzb.model.DTO.UserResponseInfoDTO;
import com.zubov.android.bzb.utils.NetworkService;
import com.zubov.android.bzb.utils.Toaster;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserPageFragment extends Fragment {

    public static final String SHEARED_PREFS = "sheared_prefs";
    public static final String EMAIL = "user_email";
    public static final String TOKEN = "token";
    public static final String USER_ID = "user_id";
    public static final String LOGIN_EMAIL = "user_email";

    private Button mButtonLeave;
    private Button mButtonChangePass;
    private Button mDeleteUser;
    private Button mAddOrder;
    private Button mListOrders;
    private String mEmail;
    private TextView mAccountName;
    private SharedPreferences mPrefs;
    private Context ctx;
    private UserPageFragmentListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_page,container,false);
        ctx = requireActivity().getApplicationContext();
        mButtonLeave = v.findViewById(R.id.b_up_leave);
        mButtonChangePass = v.findViewById(R.id.b_up_change_pass);
        mDeleteUser = v.findViewById(R.id.b_up_delete);
        mAddOrder = v.findViewById(R.id.b_up_add_order);
        mListOrders = v.findViewById(R.id.b_up_to_my_orders);
        mPrefs = ctx.getSharedPreferences(SHEARED_PREFS,Context.MODE_PRIVATE);
        mEmail = mPrefs.getString(EMAIL,"");
        mAccountName = v.findViewById(R.id.tv_up_accountmail);
        mAccountName.setText(mEmail);
        mButtonLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog dialog = approveToLogout();
                dialog.show();
            }
        });
        mButtonChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toChangePassPage();
            }
        });
        mDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.doItNow();
            }
        });
        mAddOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    isTokenOK();
                    toEditOrderPage();
            }
        });
        mListOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),PersonListOfOrdersActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }

    private void isTokenOK(){
        String curTok = mPrefs.getString(TOKEN,"");
        int userId = mPrefs.getInt(USER_ID,0);
        if (curTok.isEmpty() && mEmail.isEmpty()) toLoginPage(0);
        else if (curTok.isEmpty() && !mEmail.isEmpty()) toLoginPage(1);
        else {
            checkToken(userId, curTok);
        }

    }

    private void checkToken(int id, String token){
        UserResponseInfoDTO userToCheck = new UserResponseInfoDTO();
        userToCheck.setUserID(id);
        userToCheck.setUserToken(token);
        NetworkService
                .getInstance()
                .getJSONApi()
                .checkToken(userToCheck)
                .enqueue(new Callback<UserResponseInfoDTO>() {
                    @Override
                    public void onResponse(Call<UserResponseInfoDTO> call, Response<UserResponseInfoDTO> response) {
                        int tmpUserId = 0;
                        String tmpToken = null;
                        if (!response.isSuccessful()){
                            Toast toast = Toast.makeText(ctx,
                                    response.code(),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                            return;
                        }
                        UserResponseInfoDTO userResponseObj = response.body();
                        if(userResponseObj != null){
                           tmpUserId = userResponseObj.getUserID();
                           tmpToken = userResponseObj.getUserToken();
                        }
                        if (userResponseObj == null || tmpUserId == 0 || tmpToken == null) {
                            Toast toast = Toast.makeText(ctx,
                                    R.string.token_null,
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                        }
                        else{
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putInt(USER_ID,tmpUserId);
                            editor.putString(EMAIL,mEmail);
                            editor.putString(TOKEN,tmpToken);
                            editor.apply();
                            if(tmpToken.isEmpty()) toLoginPage(1);
                        }
                    }
                    @Override
                    public void onFailure(Call<UserResponseInfoDTO> call, Throwable t) {
                        if (mPrefs != null && !mPrefs.getString(EMAIL,"").isEmpty()) toLoginPage(1);
                        else toLoginPage(0);
                        t.printStackTrace();
                    }
                });
    }

    private void clearShearedPrefs(){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt(USER_ID,0);
        editor.putString(EMAIL,"");
        editor.putString(TOKEN,"");
        editor.apply();
    }

    private AlertDialog approveToLogout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.title_logout)
        .setMessage(R.string.up_dialog_logout_msg)
        .setNegativeButton("нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
        .setPositiveButton("да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearShearedPrefs();
                toLoginPage(0);
            }
        });
        return builder.create();
    }


    private void toLoginPage(int isEmailNecessary){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new LoginFragment();
        if (isEmailNecessary != 0)
        {
            Bundle args = new Bundle();
            args.putString(LOGIN_EMAIL,mEmail);
            fragment.setArguments(args);
        }
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container_person,
                fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void toEditOrderPage(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new EditOrderFragment();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container_person,
                fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void toChangePassPage(){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new ChangePasswordFragment();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.replace(R.id.fragment_container_person,
                fragment,"FRAGMENT_CNG_PASS");
        fragmentTransaction.addToBackStack("FRAGMENT_CNG_PASS");
        fragmentTransaction.commit();

    }

    public interface UserPageFragmentListener{
        public void doItNow();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (UserPageFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must imolement UserPageFragmentListener");
        }
    }
}
