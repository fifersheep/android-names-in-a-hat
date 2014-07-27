package uk.lobsterdoodle.namepicker;

import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by: Scott Laing
 * * Date: 19/01/14 @ 18:03 *
 */
public class Hat {

    private List<String> mNameList;
    private static Hat mInstance = new Hat();

    public static Hat getInstance() {
        if (mInstance == null)
            mInstance = new Hat();
        return mInstance;
    }

    private Hat(){
        mNameList = new ArrayList<String>();
    }

    public Hat setNameList(ArrayList<CheckBox> checkBoxArray) {
        // Add each name to the list if the corresponding checkbox is checked
        for (CheckBox cb : checkBoxArray) {
            if (cb.isChecked()) {
                mNameList.add(cb.getText().toString());
            }
        }

        // Return self in order to chain methods
        return this;
    }

    public String draw(int quantity){

        ArrayList<String> drawnNames = new ArrayList<String>();

        // Validate input
        if (quantity > 0 && quantity <= mNameList.size()) {
            for (int y = 0; y < quantity; y++) {

                Random r = new Random();
                int randomNumber = r.nextInt(mNameList.size());

                // Check if name drawn is already in the hat, and replaces if necessary
                while (drawnNames.contains(mNameList.get(randomNumber))) {
                    randomNumber = r.nextInt(mNameList.size());
                }
                drawnNames.add(mNameList.get(randomNumber));
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String string : drawnNames) {
            sb.append(string);

            if (!string.equals(drawnNames.get(drawnNames.size() - 1))) {
               sb.append("\n");
            }
        }

        return sb.toString();
    }

    public int size() {
        return mNameList.size();
    }
}
