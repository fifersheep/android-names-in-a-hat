package uk.lobsterdoodle.namepicker.dialog;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;

/**
 * Created by Scott on 24/08/2014
 */
public interface IInputDialogListener extends ISimpleDialogListener {

    public void onInputReceived(int inputId, String input);
}
