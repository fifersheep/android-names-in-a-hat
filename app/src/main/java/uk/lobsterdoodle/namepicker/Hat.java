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

    private static Hat mInstance = new Hat();

    public static Hat getInstance() {
        if (mInstance == null)
            mInstance = new Hat();
        return mInstance;
    }

    private Hat(){ }

    public String draw(int quantity ,ArrayList<CheckBox> checkBoxArray){
        String[] checkedNames = getCheckedNames(checkBoxArray);
        String[] pickedNames = pickRandomNames(quantity, checkedNames);
        return buildString(pickedNames);
    }

    private String buildString(String[] pickedNames) {
        String lastNameInList = pickedNames[pickedNames.length - 1];
        StringBuilder stringBuilder = new StringBuilder();

        for (String name : pickedNames) {
            stringBuilder.append(name);
            if (!name.equals(lastNameInList)) {
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private String[] getCheckedNames(ArrayList<CheckBox> checkBoxArray) {
        ArrayList<String> checkedNames = new ArrayList<String>();
        for (CheckBox cb : checkBoxArray) {
            if (cb.isChecked()) {
                checkedNames.add(cb.getText().toString());
            }
        }
        return (String[]) checkedNames.toArray();
    }

    private String[] pickRandomNames(int quantity, String[] names) {
        ArrayList<String> result = new ArrayList<String>();
        Random random = new Random();
        int randomNumber;

        for (int y = 0; y < quantity; y++) {
            randomNumber = random.nextInt(names.length);
            while (result.contains(names[randomNumber])) {
                randomNumber = random.nextInt(names.length);
            }
            result.add(names[randomNumber]);
        }
        return (String[]) result.toArray();
    }
}
