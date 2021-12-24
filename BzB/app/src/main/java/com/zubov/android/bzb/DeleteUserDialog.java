package com.zubov.android.bzb;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DeleteUserDialog extends AppCompatDialogFragment {
    private EditText mEtEmail;
    private EditText mEtPassword;
    private DeleteUserDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_delete_user_dialog,null);
        builder.setView(view).setTitle(R.string.title_approve)
                .setMessage(R.string.confirm_message)
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.action_accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = mEtEmail.getText().toString();
                        String password = mEtPassword.getText().toString();
                        listener.applyUserData(email,password);
                    }
                });
        mEtEmail = view.findViewById(R.id.et_dud_email);
        mEtPassword = view.findViewById(R.id.et_dud_password);
        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteUserDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement DeleteUserDialogListener");
        }
    }

    public interface DeleteUserDialogListener{
        void applyUserData(String email,String password);
    }
}
