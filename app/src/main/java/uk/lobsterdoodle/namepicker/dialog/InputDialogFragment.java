package uk.lobsterdoodle.namepicker.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import eu.inmite.android.lib.dialogs.BaseDialogFragment;
import eu.inmite.android.lib.dialogs.ISimpleDialogCancelListener;
import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;
import uk.lobsterdoodle.namepicker.R;

/**
 * Dialog for displaying text input, with an optional message and title.
 * Implement {@link IInputDialogListener} in the Fragment or Activity
 * to react on positive and negative button clicks.
 *
 * @author David Vávra (david@inmite.eu)
 */
public class InputDialogFragment extends SimpleDialogFragment {

    protected final static String ARG_MESSAGE = "message";
    protected final static String ARG_TITLE = "title";
    protected final static String ARG_POSITIVE_BUTTON = "positive_button";
    protected final static String ARG_NEGATIVE_BUTTON = "negative_button";
    protected final static String ARG_HINT = "hint";

    protected int mRequestCode;

    public static MyDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
        return new MyDialogBuilder(context, fragmentManager, InputDialogFragment.class);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            mRequestCode = getTargetRequestCode();
        } else {
            if (args != null) {
                mRequestCode = args.getInt("request_code", 0);
            }
        }
    }

    /**
     * Children can extend this to add more things to base builder.
     */
    @Override
    protected BaseDialogFragment.Builder build(BaseDialogFragment.Builder builder) {
        final String title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }

        final RelativeLayout layout = (RelativeLayout) LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_edit_text, null);
        builder.setView(layout);

        TextView msg = (TextView) layout.findViewById(R.id.dialog_edit_message);
        msg.setText(getMessage());

        final EditText input = (EditText) layout.findViewById(R.id.dialog_edit_input);
        input.setHint(getHint());

        toggleSoftKeyboard();

        final String positiveButtonText = getPositiveButtonText();
        if (!TextUtils.isEmpty(positiveButtonText)) {
            builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    IInputDialogListener listener = getDialogListener();
                    if (listener != null) {
                        String inputString = input.getText().toString();
                        listener.onInputReceived(mRequestCode, inputString);
                        listener.onPositiveButtonClicked(mRequestCode);
                    }
                    dismiss();
                    toggleSoftKeyboard();
                }
            });
        }

        final String negativeButtonText = getNegativeButtonText();
        if (!TextUtils.isEmpty(negativeButtonText)) {
            builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ISimpleDialogListener listener = getDialogListener();
                    if (listener != null) {
                        listener.onNegativeButtonClicked(mRequestCode);
                    }
                    dismiss();
                    toggleSoftKeyboard();
                }
            });
        }

        return builder;
    }

    protected CharSequence getMessage() {
        return getArguments().getCharSequence(ARG_MESSAGE);
    }

    protected String getTitle() {
        return getArguments().getString(ARG_TITLE);
    }

    protected String getPositiveButtonText() {
        return getArguments().getString(ARG_POSITIVE_BUTTON);
    }

    protected String getNegativeButtonText() {
        return getArguments().getString(ARG_NEGATIVE_BUTTON);
    }

    protected  String getHint() {
        return getArguments().getString(ARG_HINT);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        ISimpleDialogCancelListener listener = getCancelListener();
        if (listener != null) {
            listener.onCancelled(mRequestCode);
        }
    }

    protected IInputDialogListener getDialogListener() {
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            if (targetFragment instanceof IInputDialogListener) {
                return (IInputDialogListener) targetFragment;
            }
        } else {
            if (getActivity() instanceof IInputDialogListener) {
                return (IInputDialogListener) getActivity();
            }
        }
        return null;
    }

    protected ISimpleDialogCancelListener getCancelListener() {
        final Fragment targetFragment = getTargetFragment();
        if (targetFragment != null) {
            if (targetFragment instanceof ISimpleDialogCancelListener) {
                return (ISimpleDialogCancelListener) targetFragment;
            }
        } else {
            if (getActivity() instanceof ISimpleDialogCancelListener) {
                return (ISimpleDialogCancelListener) getActivity();
            }
        }
        return null;
    }

    private void toggleSoftKeyboard() {
        InputMethodManager manager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(
                InputMethodManager.SHOW_FORCED, 0);
    }

    public static class MyDialogBuilder extends SimpleDialogBuilder {

        private String mTitle;
        private CharSequence mMessage;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private String mHint;
        private Context mContext;

        private boolean mShowDefaultButton = true;

        protected MyDialogBuilder(Context context, FragmentManager fragmentManager, Class<? extends SimpleDialogFragment> clazz) {
            super(context, fragmentManager, clazz);
            mContext = context;
        }

        @Override
        protected MyDialogBuilder self() {
            return this;
        }

        public MyDialogBuilder setTitle(int titleResourceId) {
            mTitle = mContext.getString(titleResourceId);
            return this;
        }


        public MyDialogBuilder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public MyDialogBuilder setMessage(int messageResourceId) {
            mMessage = mContext.getText(messageResourceId);
            return this;
        }

        /**
         * Allow to set resource string with HTML formatting and bind %s,%i.
         * This is workaround for https://code.google.com/p/android/issues/detail?id=2923
         */
        public MyDialogBuilder setMessage(int resourceId, Object... formatArgs){
            mMessage = Html.fromHtml(String.format(Html.toHtml(new SpannedString(mContext.getText(resourceId))), formatArgs));
            return this;
        }

        public MyDialogBuilder setMessage(CharSequence message) {
            mMessage = message;
            return this;
        }

        public MyDialogBuilder setHint(String hint) {
            mHint = hint;
            return this;
        }

        public MyDialogBuilder setPositiveButtonText(int textResourceId) {
            mPositiveButtonText = mContext.getString(textResourceId);
            return this;
        }

        public MyDialogBuilder setPositiveButtonText(String text) {
            mPositiveButtonText = text;
            return this;
        }

        public MyDialogBuilder setNegativeButtonText(int textResourceId) {
            mNegativeButtonText = mContext.getString(textResourceId);
            return this;
        }

        public MyDialogBuilder setNegativeButtonText(String text) {
            mNegativeButtonText = text;
            return this;
        }

        /**
         * When there is neither positive nor negative button, default "close" button is created if it was enabled.<br/>
         * Default is true.
         */
        public MyDialogBuilder hideDefaultButton(boolean hide) {
            mShowDefaultButton = !hide;
            return this;
        }

        @Override
        protected Bundle prepareArguments() {
            if (mShowDefaultButton && mPositiveButtonText == null && mNegativeButtonText == null) {
                mPositiveButtonText = "Send";
            }

            Bundle args = new Bundle();
            args.putCharSequence(ARG_MESSAGE, mMessage);
            args.putString(ARG_TITLE, mTitle);
            args.putString(ARG_POSITIVE_BUTTON, mPositiveButtonText);
            args.putString(ARG_NEGATIVE_BUTTON, mNegativeButtonText);
            args.putString(ARG_HINT, mHint);

            return args;
        }
    }
}