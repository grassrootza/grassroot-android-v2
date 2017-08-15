package za.org.grassroot.android.model.dto;

/**
 * Created by luke on 2017/08/10.
 */
public class BtnReturnBundle {

    private final CharSequence charSequenceInTextInput;
    private final int buttonActionCode;

    public BtnReturnBundle(CharSequence charSequenceInTextInput,
                           int buttonActionCode) {
        this.charSequenceInTextInput = charSequenceInTextInput;
        this.buttonActionCode = buttonActionCode;
    }

    public CharSequence getCharSequenceInTextInput() {
        return charSequenceInTextInput;
    }

    public int getButtonActionCode() {
        return buttonActionCode;
    }

    @Override
    public String toString() {
        return "BtnReturnBundle{" +
                "charSequenceInTextInput=" + charSequenceInTextInput +
                ", buttonActionCode=" + buttonActionCode +
                '}';
    }
}
