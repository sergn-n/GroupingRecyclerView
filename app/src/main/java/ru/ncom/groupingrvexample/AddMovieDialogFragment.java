package ru.ncom.groupingrvexample;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


/**
 * Created by Ника-Ком on 10.11.2016.
 */

public class AddMovieDialogFragment extends DialogFragment {

    public interface YesNoListener {
        void onAddYes(String title, String genre, String year);

        void onAddDismiss();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof AddMovieDialogFragment.YesNoListener)) {
            throw new ClassCastException(activity.toString() + " must implement AddMovieDialogFragment.YesNoListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View movieEdit = inflater.inflate(R.layout.movie_dialog, null);
        builder.setView(movieEdit)
                .setTitle(R.string.movie_dialog_title)
                // Add action buttons
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //
                        String title = ((EditText)movieEdit.findViewById(R.id.title)).getText().toString();
                        String genre = ((EditText)movieEdit.findViewById(R.id.genre)).getText().toString();
                        String year = ((EditText)movieEdit.findViewById(R.id.year)).getText().toString();
                        ((AddMovieDialogFragment.YesNoListener) getActivity())
                                .onAddYes(title, genre, year);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddMovieDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // No actvity when config change
        if (getActivity()!= null)
            ((AddMovieDialogFragment.YesNoListener) getActivity()).onAddDismiss();
    }
}
