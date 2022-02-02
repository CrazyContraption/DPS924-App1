package dps924.a1;

import static dps924.a1.Enumerations.KeyTypes;

import androidx.annotation.NonNull;

/**
 * Defines what an individual operation on the calculator is, and how to handle it, including how to
 * display it and replicate them, along with their ordinal values if applicable.
 */
public class Operation {

    /**
     * How the operand should be rendered
     */
    public String displayAs;


    /**
     * What the operand represents in terms of its value, type, and task
     */
    public KeyTypes keyType;

    /**
     * Operation Constructor
     * @param type The KeyTypes enum to base the operation on, including its display properties
     */
    public Operation(KeyTypes type) {
        this.keyType = type;
        switch (this.keyType) {

            case key0: case key1: case key2: case key3: case key4: case key5: case key6: case key7: case key8: case key9:
                this.displayAs = this.keyType.ordinal()+""; break;

            case keyPlus: this.displayAs = " + "; break;
            case keyMinus: this.displayAs = " - "; break;
            case keyTimes: this.displayAs = " * "; break;
            case keyDivide: this.displayAs = " / "; break;
            case keyModulo: this.displayAs = " % "; break;
            case keyDecimal: this.displayAs = "."; break;
            case keyPower: this.displayAs = " Pow("; break;
            case keyMin: this.displayAs = " Min("; break;
            case keyMax: this.displayAs = " Max("; break;
            case keyOpenBrace: this.displayAs = "("; break;
            case keyComma: this.displayAs = ", "; break;
            case keyCloseBrace: this.displayAs = ")"; break;
            case keyAns: this.displayAs = "ANS"; break;

            case keyEqual: this.displayAs = " = "; break;

            default: this.displayAs = " NaN "; break;
        }
    }

    /**
     * toString override for default behaviour safety
     * @return String value of how the operation should be displayed safely
     */
    @NonNull
    @Override
    public String toString() {
        return this.displayAs;
    }

    /**
     * @return Newly created deep copied Operation object, based on the values of the current object.
     */
    public Operation clone() {
        Operation newOp = new Operation(this.keyType);
        newOp.displayAs = this.displayAs;
        return newOp;
    }
}
