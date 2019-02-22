package io.prover.common.util;

import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

/**
 * Created by babay on 21.11.2016.
 */

public class TextInputLayoutErrorWatcher implements View.OnFocusChangeListener, TextWatcher {
    private TextInputLayout focusedRow;

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getParent() instanceof TextInputLayout) {
                focusedRow = (TextInputLayout) v.getParent();
            } else if (v.getParent().getParent() instanceof TextInputLayout) {
                focusedRow = (TextInputLayout) v.getParent().getParent();
            }
        } else {
            focusedRow = null;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (focusedRow != null)
            focusedRow.setErrorEnabled(false);
    }

    public TextInputLayoutErrorWatcher addTextInputLayout(TextInputLayout layout) {
        layout.getEditText().setOnFocusChangeListener(this);
        layout.getEditText().addTextChangedListener(this);
        return this;
    }
}
