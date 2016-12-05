package ru.ncom.groupingrvexample;

import android.app.Activity;
import android.app.Dialog;
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

    private static final String POS ="POS";
    private static final String DELOPT ="DELOPT";

    /**
     * Creates AlertDialog to confirm deletion. Activity must implement {@link YesNoListener}
     * @param m movie what to delete
     * @param groupTitle when not null options (delete item or group) will be shown
     * @return
     */
    public static DeleteMovieDialogFragment createInstance(Movie m, String groupTitle){
        DeleteMovieDialogFragment f = new DeleteMovieDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(POS, m);
        b.putString(DELOPT, groupTitle);
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
        String msgMovie = String.format(getString(R.string.delete_movie_dialog_movie),
                m.getTitle(), m.getGenre(), m.getYear());
        AlertDialog.Builder db = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.delete_movie_dialog_title) ) ;
        if (b.getString(DELOPT) != null) {
            String[] arrOptions = new String[]{
                    msgMovie, String.format(getString(R.string.delete_movie_dialog_group), b.getString(DELOPT))
            };
            db.setSingleChoiceItems(arrOptions, mDeleteOption,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDeleteOption = which;
                        }
                    });
        }
        else {
            db.setMessage(msgMovie);
        }
        return db.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

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
                })
                .create();
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
