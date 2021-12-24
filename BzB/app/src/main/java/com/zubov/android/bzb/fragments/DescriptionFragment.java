package com.zubov.android.bzb.fragments;

import android.content.Context;
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

import com.zubov.android.bzb.R;
import com.zubov.android.bzb.model.DTO.DescriptionMarkerDTO;
import com.zubov.android.bzb.model.DTO.MarkerDTO;
import com.zubov.android.bzb.utils.NetworkService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DescriptionFragment extends Fragment {

    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";

    private double mLatitude;
    private double mLongitude;

    private OnFragmentInteractionListener mListener;

    private Button mBtnToMap;
    private TextView mTextView;

    public static DescriptionFragment newInstance(Double lat,Double lon) {
        DescriptionFragment fragment = new DescriptionFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE,lat);
        args.putDouble(ARG_LONGITUDE,lon);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            mLatitude = getArguments().getDouble(ARG_LATITUDE);
            mLongitude = getArguments().getDouble(ARG_LONGITUDE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_description,container,false);
        mBtnToMap = v.findViewById(R.id.button_to_map);
        mTextView = v.findViewById(R.id.tv_description);


        mBtnToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            sendBack();
            }
        });
        placeDescription();
        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener){
            mListener = (OnFragmentInteractionListener) context;
        } else{
            throw new RuntimeException(context.toString()+
                    " must implement OnFragmentInteractionListener interface");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void sendBack(){
        if(mListener != null){
            mListener.onFragmentInteraction();
        }
    }

    public interface OnFragmentInteractionListener{
        void onFragmentInteraction();
    }

    private void placeDescription() {
        // asking server for text from db
        MarkerDTO marker = new MarkerDTO();
        marker.setMarkerLatitude(mLatitude);
        marker.setMarkerLongitude(mLongitude);
        NetworkService
                .getInstance()
                .getJSONApi()
                .getDescription(marker)
                .enqueue(new Callback<DescriptionMarkerDTO>() {
                    @Override
                    public void onResponse(Call<DescriptionMarkerDTO> call,
                                           Response<DescriptionMarkerDTO> response) {
                        if (!response.isSuccessful()){
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                    response.code(),
                                    Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.BOTTOM, 0, 100);
                            toast.show();
                            return;
                        }
                        DescriptionMarkerDTO markerDesc = response.body();
                        mTextView.setText(markerDesc.getText());
                    }

                    @Override
                    public void onFailure(Call<DescriptionMarkerDTO> call, Throwable t) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                R.string.err_server,
                                Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.BOTTOM, 0, 100);
                        toast.show();
                        t.printStackTrace();
                    }
                });
    }

}
