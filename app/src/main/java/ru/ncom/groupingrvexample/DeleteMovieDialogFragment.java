package ru.ncom.groupingrvexample;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import ru.ncom.groupingrvexample.model.Movie;

/**
 * Created by gerg on 10.10.2016.
 */

public class DeleteMovieDialogFragment extends DialogFragment {

    public interface YesNoListener {
        void onDeleteYes(Movie m, int delOption);

        void onDeleteDismiss();
    }

    private static final String MSG ="MSG";
    private static final String POS ="POS";
    private static final String DELOPT ="DELOPT";

    /**
     * Creates AlertDialog to confirm deletion. Activity must implement {@link YesNoListener}
     * @param ctx
     * @param m what to delete
     * @param withOptions should create deletion options (item or group)
     * @return
     */
    public static DeleteMovieDialogFragment createInstance(Context ctx, Movie m, boolean withOptions){
        String message = String.format(ctx.getString(R.string.delete_movie_dialog_message),
                m.getTitle(), m.getGenre(), m.getYear());
        DeleteMovieDialogFragment f = new DeleteMovieDialogFragment();
        Bundle b = new Bundle();
        b.putString(MSG, message);
        b.putSerializable(POS, m);
        b.putBoolean(DELOPT, withOptions);
        f.setArguments(b);
        return f;
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof YesNoListener)) {
            throw new ClassCastException(activity.toString() + " must implement YesNoListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();
        final Movie m = (Movie)b.getSerializable(POS);
        mDeleteOption = 0;
        AlertDialog.Builder db= new AlertDialog.Builder(getActivity())
                .setTitle(R.string.delete_movie_dialog_title)
                .setMessage(b.getString(MSG))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((YesNoListener) getActivity()).onDeleteYes(m, mDeleteOption);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeleteMovieDialogFragment.this.getDialog().cancel();
                    }
                });
        if (b.getBoolean(DELOPT))
            db.setSingleChoiceItems(R.array.deleteMovieOptions, mDeleteOption,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeleteOption = which;
                    }
                });

        return db.create();
    }

    int mDeleteOption = 0;


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // No actvity when config change
        if (getActivity()!= null)
            ((YesNoListener) getActivity()).onDeleteDismiss();
    }
}
