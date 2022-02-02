package dps924.a1;

import static dps924.a1.Enumerations.KeyTypes;
import static dps924.a1.Enumerations.OperationsTypes;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class Calculator {

    // Using a custom class instead of Strings, they evaluate the same with a .toString override
    public static ArrayList<ArrayList<Operation>> history = new ArrayList<>();

    private static EditText displayPort;
    private static boolean advancedMode = false;
    private static String errorText = "\n";


    public static void push(String value) {
        try { // Because I really don't trust myself, or anyone else
            KeyTypes key = (KeyTypes.valueOf(value)); // Attempt to cast the string to an enum key

            // Are we pushing (pressing) something that is displayable?
            if (key != KeyTypes.keyDelete && key != KeyTypes.keyClear && key != KeyTypes.keyEqual) {
                if (validate(key, history.get(0))) // Validate the equation structure
                    history.get(0).add(new Operation(key)); // Add it formally to our equation
            } else if (key != KeyTypes.keyEqual || validate(key, history.get(0)))
                KeyTypes.doTask(key,0,0); // Do advanced operation

        } catch (Exception e) { // Oops~
            System.err.println(e.getMessage());
        }
    }

    /**
     * Calculates the evaluated version of the provided equation. Probably lost 3 years of my life writing
     * this method, but the satisfaction it gave me was unprecedented. Basically a manual version of eval().
     * I am NOT writing detailed comments for this one, take away all the marks you want, it's not worth
     * losing another year of my life for trying to decipher/remember what I wrote here.
     * @param operations ArrayList of Operation objects containing the order of operands/values in the equation
     * @return Decimal-safe value of the expression
     */
    public static double calculate(ArrayList<Operation> operations) {
        for (int orderIndex = 0; orderIndex < OperationsTypes.size(); orderIndex++) { // Determines the order of operations (PEMDAS)

            int indexStart = -1; // Used for sub-equations, defines the starting index
            int counter = 0; // Used for something convoluted that I can't remember
            int indexEnd = -1; // Used for sub-equations, defines the ending index

            for (int opIndex = 0; opIndex < operations.size(); opIndex++) { // Loops through all the operations in the equation
                Operation operation = operations.get(opIndex); // Reference to the current operand
                switch (OperationsTypes.values()[orderIndex]) { // What are we testing for?

                    case concat: // Join number strings into constants
                        if (operation.keyType == KeyTypes.keyAns) // Ans is a special Boi, treat him exclusively
                            operations.get(opIndex).displayAs = Double.toString(KeyTypes.doTask(operation.keyType, 0, 0));
                        // TODO: Add PI constant substitution here?
                        else if(KeyTypes.isNumber(operation.keyType)) { // Join the numbers
                            String concat = "";
                            for (int index = opIndex; index < operations.size() && KeyTypes.isNumber(operations.get(index).keyType);) {
                                concat += operations.get(index).displayAs.trim().replace("\n", "");
                                if (index != opIndex)
                                    operations.remove(opIndex + 1);
                                else
                                    index++;
                            }
                            operations.get(opIndex).displayAs = concat;
                        }
                        break;

                    case braces: // Recursively run Calculate on sub-equations
                        if (KeyTypes.opensBrace(operation.keyType) && indexStart == -1) // Brace opened
                            indexStart = opIndex;
                        else if (KeyTypes.opensBrace(operation.keyType) && indexStart != -1) // Another brace opened, ignore and let the next recursion loop handle it
                            counter++;
                        else if (operation.keyType == KeyTypes.keyCloseBrace && indexStart != -1 && counter > 0) // Non-initial brace closed
                            counter--;
                        else if (operation.keyType == KeyTypes.keyCloseBrace && indexStart != -1 && counter == 0) // Initial brace closed
                            indexEnd = opIndex;

                        if (indexStart >= 0 && indexEnd > indexStart) { // Valid indexes?
                            opIndex = indexStart;
                            operation = operations.get(opIndex);
                            double result = 0;
                            try { // Because I REALLY REALLY *really* don't trust anything I wrote here
                                ArrayList<Operation> equation= new ArrayList<>();
                                for (int index = indexStart + 1; index < indexEnd;) {
                                    equation.add(operations.get(index).clone());
                                    operations.remove(index);
                                    indexEnd--;
                                }
                                operations.remove(indexEnd);
                                if (operation.keyType == KeyTypes.keyOpenBrace) // Simple braces
                                    operation.displayAs = Double.toString(Calculator.calculate(equation));
                                else { // Uhoh, we got some advanced boi functions
                                    for (int index = 0; index < equation.size(); index += 2) { // Handle sub-sub-equations
                                        ArrayList<Operation> subEquation = new ArrayList<>();
                                        for (int slot = index; slot < equation.size() && equation.get(slot).keyType != KeyTypes.keyComma; ) {
                                            subEquation.add(equation.get(slot).clone());
                                            if (slot != index && equation.get(slot - 1).keyType != KeyTypes.keyComma)
                                                equation.remove(slot);
                                            else
                                                slot++;
                                        }
                                        equation.get(index).displayAs = Double.toString(Calculator.calculate(subEquation));
                                    }
                                    for (int index = 0; index < equation.size();) { // Handle the advance functionality of the sub-equation itself
                                        if (equation.get(index).keyType == KeyTypes.keyComma) {
                                            equation.get(index).displayAs = Double.toString(KeyTypes.doTask(operation.keyType, Double.parseDouble(equation.get(index - 1).displayAs), Double.parseDouble(equation.get(index + 1).displayAs)));
                                            equation.remove(index + 1);
                                            equation.remove(index - 1);
                                        } else
                                            index++;
                                    }
                                    operation.displayAs = equation.get(0).displayAs;
                                }
                            } catch (Exception e) { // Oopsies~
                                System.err.println(e.getMessage());
                            }
                            // Reset values
                            counter = 0;
                            indexStart = -1;
                            indexEnd = -1;
                        }
                        break;

                    case timesAndDivide: // Simple stuff
                        if(operation.keyType == KeyTypes.keyTimes || operation.keyType == KeyTypes.keyDivide || operation.keyType == KeyTypes.keyModulo) {
                            operation.displayAs = Double.toString(KeyTypes.doTask(operation.keyType,  Double.parseDouble(operations.get(opIndex - 1).displayAs),  Double.parseDouble(operations.get(opIndex + 1).displayAs)));
                            operations.remove(opIndex + 1);
                            operations.remove(opIndex - 1);
                            opIndex--;
                        }
                        break;

                    case addAndSubtract: // Same as above, but with different keys
                        if(operation.keyType == KeyTypes.keyPlus || operation.keyType == KeyTypes.keyMinus) {
                            operation.displayAs = Double.toString(KeyTypes.doTask(operation.keyType,  Double.parseDouble(operations.get(opIndex - 1).displayAs),  Double.parseDouble(operations.get(opIndex + 1).displayAs)));
                            operations.remove(opIndex + 1);
                            operations.remove(opIndex - 1);
                            opIndex--;
                        }
                        break;
                }
            }
        }
        return Double.parseDouble(operations.get(0).displayAs); // Oh god we're finally done, unless this was a recursive call :(
    }

    /**
     * Validates an equation to ensure it makes sense, and can be parsed by the calculate method. Used
     * to ensure an equation never enters an unstable state, and to provide error feedback to the user.
     * @param attemptedKey The next key attempted by the user to be added the the equation
     * @param operations The current ArrayList of Operation objects that compose the equation
     * @return Boolean denoting the success of failure of the validation
     */
    private static boolean validate(KeyTypes attemptedKey, ArrayList<Operation> operations) { // V2.0, now with less redundant checks xd
        int openBraces = 0;         // Count our opened sub-equations
        int openMaths = 0;
        boolean hasDecimal = false; // Flag if a number already has a decimal (prevents entries like 1.22.3)
        String error = "";          // Current error, if any. "" is considered error-less

        int opIndex = 0;
        do { // Ensure we check at least what the user submitted
            KeyTypes keyCurr = (operations.size() == opIndex ? attemptedKey : operations.get(opIndex).keyType);

            if (opIndex == operations.size()) {
                if (!KeyTypes.canFollow((operations.size() == 0 ? null : operations.get(opIndex - 1).keyType), attemptedKey)) {
                    error = "Operand '" + new Operation(attemptedKey).displayAs + "'";
                    break;
                }
            }

            // Double decimals check
            if (!KeyTypes.isNumber(keyCurr))
                hasDecimal = false;
            else if (keyCurr == KeyTypes.keyDecimal && hasDecimal) {
                error = "Double decimals";
                break;
            }
            else if (keyCurr == KeyTypes.keyDecimal)
                hasDecimal = true;

            // Unbalanced braces checks
            if (KeyTypes.opensBrace(keyCurr)) {
                openBraces++;
                if (keyCurr != KeyTypes.keyOpenBrace)
                    openMaths++;
            } else if (keyCurr == KeyTypes.keyCloseBrace) {
                openBraces--;
                if (keyCurr != KeyTypes.keyOpenBrace)
                    openMaths--;
            }

            if (openBraces < 0 || (keyCurr == KeyTypes.keyEqual && openBraces > 0)) {
                error = "Unbalanced braces";
                break;
            }

            if (keyCurr == KeyTypes.keyComma && openMaths <= 0){
                error = "Unexpected parameter";
                break;
            }

            opIndex++;
        } while (opIndex <= operations.size() && error == "");

        if (error != "") // Did we find an error?
            errorText = "\nERR: " + error + " syntax not valid"; // Format the error

        return error == "";


        /*if (operations.size() == 0 ) { // Is this the first key we're entering?
            if (!KeyTypes.canFollow(null, attemptedKey)) // Can the key start off the equation?
                error = "Operand '" + new Operation(attemptedKey).displayAs + "'";
        } else { // Mid-equation entry
            if (KeyTypes.opensBrace(operations.get(0).keyType))
                openBraces++;
            for (int opIndex = 0; ; opIndex++) { // Loop through each operand
                KeyTypes keyLeft = operations.get(opIndex).keyType;
                KeyTypes keyRight = (opIndex < operations.size() - 1 ? operations.get(opIndex + 1).keyType : attemptedKey);

                if (KeyTypes.opensBrace(keyRight))
                    openBraces++;
                else if (keyRight == KeyTypes.keyCloseBrace) {
                    if (commas > 0)
                        commas--;
                    closeBraces++;
                }

                if (!KeyTypes.isNumber(keyRight))
                    hasDecimal = false;

                if (keyRight == KeyTypes.keyDecimal && hasDecimal)
                    error = "Double decimals";
                else if (keyRight == KeyTypes.keyDecimal)
                    hasDecimal = true;

                if (closeBraces > openBraces)
                    error = "Unbalanced braces";

                if (keyRight == KeyTypes.keyComma && commas + 1 > (openBraces - closeBraces))
                    error = "Unexpected parameter";

                if (error == "" && !KeyTypes.canFollow(keyLeft, keyRight))
                    error = "Operand '" + new Operation(attemptedKey).displayAs + "'";
            }
        }*/
    }

    /**
     * Toggles the existing visibility of the advanced layout
     * @param button Button view that was selected, used for changing the text
     * @param view View of the advanced group of items to show/hide
     */
    public static void toggleMode(Button button, LinearLayout view) {
        advancedMode = !advancedMode; // Invert the state
        button.setText(advancedMode?"STANDARD MODE":"ADVANCED MODE"); // Set button text
        view.setVisibility(advancedMode?View.VISIBLE:View.GONE); // Set the layout visibility
    }

    /**
     * Sets up, and resets the location in which the calculator should display its output
     * @param view View that pertains to the output of the calculator
     */
    public static void setDisplay (EditText view) {
        history.clear();
        history.add(new ArrayList<>());
        displayPort = view;
    }

    /**
     * Publicly exposes a safe way of removing operands from an equation.
     * @param index Index of which to remove an operand at, negative values work from the end.
     * @return Returns true upon success, false upon failure
     */
    public static boolean wipeOperation(int index) {
        if (history.get(0) != null && history.get(0).size() > 0) {
            if (index < 0) {
                history.get(0).remove(history.get(0).size() - index - 2);
            } else if (index < history.get(0).size())
                history.get(0).remove(index);
        } else
            return false;
        return true;

    }

    /**
     * Repaints the bound output display with the the updated Calculator history.
     */
    public static void updateDisplay() {
        String result = "";

        // Build our history String
        for (int historyIndex = history.size() - 1; historyIndex >= 0; historyIndex--) {
            ArrayList<Operation> entry = history.get(historyIndex);
            for (int opIndex = 0; opIndex < entry.size(); opIndex++)
                result += entry.get(opIndex).toString();

            // Tack any error text onto the first line of our history
            result += (historyIndex == 0 ? errorText : "\n");
        }

        // Reset the error text
        errorText = "\n";

        // Set the display's text
        displayPort.setText(result);
    }
}
