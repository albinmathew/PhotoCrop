package com.albinmathew.samples.photocrop;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


/**
 * @author albin
 * @date 23/6/15
 */
public class PicModeSelectDialogFragment extends DialogFragment {

    private String[] picMode = {Constants.PicModes.CAMERA, Constants.PicModes.GALLERY};

    private IPicModeSelectListener iPicModeSelectListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(picMode, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (iPicModeSelectListener != null)
                    iPicModeSelectListener.onPicModeSelected(picMode[which]);
            }
        });
        return builder.create();
    }

    public void setiPicModeSelectListener(IPicModeSelectListener iPicModeSelectListener) {
        this.iPicModeSelectListener = iPicModeSelectListener;
    }

    public interface IPicModeSelectListener {
        void onPicModeSelected(String mode);
    }
}
