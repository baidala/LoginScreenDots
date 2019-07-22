package co.smartfarm.ui.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;
import co.smartfarm.R;
import co.smartfarm.SmartFarmApp;
import com.codetroopers.betterpickers.numberpicker.NumberPicker;
import timber.log.Timber;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;


public class KeyboardView extends NumberPicker {

    private OnPassCodeListener mOnPassCodeListener;
    private OnNumberPressedListener mOnNumberPressedListener;
    private ConcurrentLinkedDeque<Integer> inputs = new ConcurrentLinkedDeque<>();

    public interface OnPassCodeListener {
        void onCodeEntered(final String code);
    }
    public interface OnNumberPressedListener {
        void onNumberPressed(int n);
        void onDeletePressed();
    }

    public KeyboardView(Context context) {
        this(SmartFarmApp.applicationContext, null);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        /** set left_button INVISIBLE to have right columns number*/
        setPlusMinusVisibility(View.INVISIBLE);

        for (int i = 0; i < 10; i++) {
            mNumbers[i].setOnClickListener(this);
        }

        mError.setText("Invalid Passcode");
    }

    @Override
    public void onClick(View v) {
        if (!this.isEnabled()) return;
        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        mError.hideImmediately();
        doOnClick(v);
        updateDeleteButton();
        if (inputs.size() == INPUT_LIMIT) {
            Timber.d("INPUT_LIMIT => onCodeEntered %s", inputs.toString());
            mOnPassCodeListener.onCodeEntered(convertToString(inputs));
        }
    }

    @Override
    protected void doOnClick(View v) {
        Integer val = (Integer) v.getTag(R.id.numbers_key);
        if (val != null) {
            // A number was pressed
            addClickedNumber(val);
        } else if (v == mDelete) {
            if (mInputPointer >= 0) {
                inputs.poll();
                mInputPointer--;
                mOnNumberPressedListener.onDeletePressed();
            }
        }
        updateKeypad();
    }

    @Override
    protected void addClickedNumber(int val) {
        if (inputs.size() < INPUT_LIMIT) {
            inputs.add(val);
            mInputPointer++;
            mOnNumberPressedListener.onNumberPressed(val);
        }
    }

    public void reset() {
        inputs.clear();
        super.reset();
    }

    public void setOnPassCodeListener(OnPassCodeListener listener) {
        mOnPassCodeListener = listener;
    }

    public void setOnNumberPressedListener(OnNumberPressedListener listener) {
        mOnNumberPressedListener = listener;
    }

    private String convertToString(final Deque<Integer> codeArray){
        Iterator<Integer> it = codeArray.iterator();
        StringBuilder builder = new StringBuilder();
        while (it.hasNext()){
            Integer i = it.next();
            builder.append(i);
        }
        return builder.toString();
    }
}
