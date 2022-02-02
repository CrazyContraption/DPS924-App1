package dps924.a1;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Public class which contains all the enumerables the application uses.
 */
public class Enumerations {

    /**
     * Determines the order of operations for the Calculator's calculate method.
     */
    enum OperationsTypes {
        concat,         // Joins individual numbers together to form complete number sets, and substitutes constants like ANS and PI (PI not Implemented)
        braces,         // Sets out to identify and recursively call sub-equations which are defined in brace pairs, including Math functions like Min & Max
        timesAndDivide, // Does all multiplication and division operations, including modulo
        addAndSubtract; // Does all addition and subtraction operations

        /**
         * @return The number of operations defined, used to determine loop upper limits
         */
        public static int size() {
            return OperationsTypes.values().length;
        }
    }

    /**
     * Defines all the keys of which the application assigns, and how to handle them individually. Operations classes define how they are displayed and created/owned.
     */
    enum KeyTypes {
        key0, key1, key2, key3, key4, key5, key6, key7, key8, key9, keyDecimal, keyMin, keyMax, keyPower, keyDelete, keyEqual, keyPlus, keyMinus, keyTimes, keyDivide, keyModulo, keyClear, keyOpenBrace, keyComma, keyCloseBrace, keyAns;

        /**
         * Statically defines actions that each key type performs. Standard keys (like numbers) simply
         * output themselves, and are omitted here. This is reserved for advanced functionalities.
         * @param key The KeyTypes of which to perform the task as, since the method is static
         * @param value The initial value for the operation
         * @param modifierValue The value of which to modify the initial via the predefined task
         * @return The resulting value from completing the predefined task with the provided values
         */
        static double doTask(KeyTypes key, double value, double modifierValue) {
            switch (key) {
                case keyEqual: // Initiates validation and calculation functions
                    Operation equals = new Operation(key);
                    double result = 0;
                    try { // Because I don't trust myself
                        ArrayList<Operation> equation = new ArrayList<>();
                        for(Operation op : Calculator.history.get(0)) {
                            equation.add(op.clone());
                        }
                        result = Calculator.calculate(equation);
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                    }
                    DecimalFormat format = new DecimalFormat("0.################");
                    equals.displayAs += "\n" + format.format(result) + "\n";
                    Calculator.history.get(0).add(equals);
                    Calculator.history.add(0,new ArrayList<>());
                    return result;
                case keyPlus: return value + modifierValue; // Adding
                case keyMinus: return value - modifierValue; // Subtracting
                case keyTimes: return value * modifierValue; // Multiplication
                case keyDivide: return value / modifierValue; // Division
                case keyModulo: return value % modifierValue; // Modulo
                case keyMin: return Math.min(value, modifierValue); // Math.Min
                case keyMax: return Math.max(value, modifierValue); // Math.Max
                case keyPower: return Math.pow(value, modifierValue); // Math.Pow
                case keyDelete: Calculator.wipeOperation(-1); return value; // Delete/Undo
                case keyClear: Calculator.history.clear(); Calculator.history.add(new ArrayList<>()); return value; // Clear history
                case keyAns: // Get last value (previous answer)
                    if (Calculator.history.size() > 1) { // Is there a previous answer?
                        String lastAnswer = Calculator.history.get(1).get(Calculator.history.get(1).size() - 1).displayAs;
                        return Double.parseDouble(lastAnswer.replace(" = ", "").replace("\n", ""));
                    }
                    return 0; // No answer, so just assume 0
                default: return value; // Undefined task
            }
        }

        /**
         * Check if a given KeyType is a value holder, used for concatenation operations to determine
         * what forms a complete number, or should otherwise be substituted.
         * @param key A key to test
         * @return Boolean denoting if a given KeyType is derrived to hold a value
         */
        static boolean isNumber(KeyTypes key) {
            switch (key) {
                case key0: case key1: case key2: case key3: case key4: case key5: case key6: case key7: case key8: case key9: case keyDecimal: case keyAns:
                    return true;
                default: return false;
            }
        }

        /**
         * Check if a given KeyType is a operand, used for delimiting values, and determining if a given
         * key holds a task functionality.
         * @param key A key to test
         * @return Boolean denoting if a given KeyType is derived to be an operand
         */
        static boolean isOperator(KeyTypes key) {
            switch (key) {
                case keyEqual: case keyPlus: case keyMinus: case keyTimes: case keyDivide: case keyModulo:
                    return true;
                default: return false;
            }
        }

        /**
         * Check if a given KeyType is the start of a sub-equation. Used for recursion delimiting and
         * definitions.
         * @param key A key to test
         * @return Boolean denoting if a given KeyType is derived opens a braced sub-equation
         */
        static boolean opensBrace(KeyTypes key) {
            switch (key) {
                case keyOpenBrace: case keyPower: case keyMin: case keyMax:
                    return true;
                default: return false;
            }
        }

        /**
         * Determines given two keys, if they are logistically allowed to queue adjacently. Used for
         * validating equations as they're built. Does NOT handle complex tests like brace balancing
         * and parameter validation for Math functions.
         * @param keyLeft The key proceeding the query, can be null to denote no previous key
         * @param keyRight The key that wants to follow up keyLeft, cannot be null
         * @return Boolean denoting if the keys can queue in the order provided
         */
        static boolean canFollow(KeyTypes keyLeft, KeyTypes keyRight) {
            switch (keyRight) { // aka "keyPressed"

                // Numbers don't really have any limits other than they cannot follow up a closing brace
                case key0: case key1: case key2: case key3: case key4: case key5: case key6: case key7: case key8: case key9: case keyAns:
                    return keyLeft != KeyTypes.keyCloseBrace;

                // Can only follow any number, but not itself or an answer (considered numbers for concatenation purposes)
                case keyDecimal:
                    return isNumber(keyLeft) && keyLeft != KeyTypes.keyDecimal && keyLeft != KeyTypes.keyAns;

                // Can follow a closing brace || Can follow any number
                case keyPlus: case keyMinus: case keyTimes: case keyDivide: case keyComma: case keyModulo: case keyEqual:
                    if (keyLeft == KeyTypes.keyDecimal)
                        Calculator.wipeOperation(-1); // Remove trailing decimal
                    else if (keyLeft == null) { // Is it the first key in the equation?
                        if (keyRight == KeyTypes.keyEqual) // Equals with no equation?
                            Calculator.push("key0"); // Assume equation of 0
                        else if (keyRight != KeyTypes.keyComma && Calculator.history.size() > 1) // First key is an operation and we have history of answers?
                            Calculator.push("keyAns"); // Insert ANS operation
                        else
                            return false; // Otherwise, fail validation
                        return true;
                    }
                    return keyLeft == KeyTypes.keyCloseBrace || isNumber(keyLeft);

                // Can be first || Can follow any operator or itself
                case keyOpenBrace: case keyMin: case keyMax: case keyPower:
                    return keyLeft == null || isOperator(keyLeft) || opensBrace(keyLeft);

                // Cannot be first && Can follow itself or any number && removes trailing decimals
                case keyCloseBrace:
                    if (keyLeft == KeyTypes.keyDecimal)
                        Calculator.wipeOperation(-1);
                    return keyLeft != null && (keyLeft == KeyTypes.keyCloseBrace || isNumber(keyLeft));

                default: return false; // No-no's
            }
        }
    }
}
