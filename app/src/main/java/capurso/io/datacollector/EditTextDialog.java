package capurso.io.datacollector;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

/**
 * Displays a simple dialog comprising of an EditText. In this case, it is used to enter
 * a new default file extension.
 */
public class EditTextDialog extends DialogFragment {
    /**
     * Bundle argument key for the current file extension.
     */
    public static final String ARG_CURR_EXT = "currentExtension";

    /**
     * Interface to receive button press callbacks
     */
    public interface EditTextDialogListener {
        void onPositiveClicked(String input);
        void onCancelClicked();
    }

    private String mCurrentExtension;
    private EditTextDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //Create the main EditText
        final EditText et = new EditText(getActivity());
        et.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        et.setPadding(10, 20, 10, 0);
        et.setHint(getString(R.string.current) + " \t " + mCurrentExtension); //Hint is the current default extension

        builder.setTitle(getString(R.string.set_default_extension));
        builder.setView(et);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Deliver the inputted text to the listener
                mListener.onPositiveClicked(et.getText().toString());
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onCancelClicked();
                getDialog().cancel();
            }
        });

        return builder.create();
    }

    public void onAttach(Activity activity){
        super.onAttach(activity);

        mCurrentExtension = getArguments().getString(ARG_CURR_EXT);
        mListener = (EditTextDialogListener)activity;
    }
}
