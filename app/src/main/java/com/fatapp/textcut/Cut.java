package com.fatapp.textcut;

import java.util.ArrayList;

class Cut {
    static ArrayList<String> cutText(String text, int number) {

        ArrayList<String> output = new ArrayList<>();
        int totalNumber = text.length() - 1;


        if (totalNumber % number == 0) {
            int textNumber = (int) Math.floor((double) totalNumber / (double) number);
            for (int n = 0; n <= number - 1; n++) {
                output.add(text.substring(n * textNumber, n * textNumber + textNumber));
            }
        } else {
            int cutNumber = number - 1;
            int textNumber = (int) Math.floor((double) totalNumber / (double) number);
            for (int n = 0; n <= cutNumber - 1; n++) {
                output.add(text.substring(n * textNumber, n * textNumber + textNumber));
            }
            output.add(text.substring(cutNumber * textNumber));
        }

        return output;
    }
}
