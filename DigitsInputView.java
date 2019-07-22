package co.smartfarm.ui.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.smartfarm.R;
import co.smartfarm.ui.keyboard.KeyboardView;
import timber.log.Timber;


public class DigitsInputView extends FrameLayout implements KeyboardView.OnNumberPressedListener {

    private final Context mContext;
    private ArrayDeque<ImageView> dequeSelected = new ArrayDeque();
    private ArrayDeque<ImageView> dequeNotSelected = new ArrayDeque();

    @BindView(R.id.iv_dot1)
    ImageView dotOne;

    @BindView(R.id.iv_dot2)
    ImageView dotTwo;

    @BindView(R.id.iv_dot3)
    ImageView dotThree;

    @BindView(R.id.iv_dot4)
    ImageView dotFour;

    public DigitsInputView(Context context) {
        this(context, null);
    }

    public DigitsInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(getLayoutId(), this);
        ButterKnife.bind(this, v);
        dotTwo.setSelected(true);
        reset();
    }

    protected int getLayoutId() {
        return R.layout.digits_input_view;
    }

    /**
     * KeyboardView.OnNumberPressedListener */
    @Override
    public void onNumberPressed(int val) {
//        Timber.d("onNumberPressed");
        ImageView dot  = dequeNotSelected.pop();
        if (dot != null) {
            dot.setSelected(true);
            dequeSelected.push(dot);
        }

    }

    /**
     * KeyboardView.OnNumberPressedListener
     */
    @Override
    public void onDeletePressed() {
//        Timber.d("onDeletePressed");
        ImageView dot  = dequeSelected.pop();
        if (dot != null) {
            dot.setSelected(false);
            dequeNotSelected.push(dot);
        }


    }

    public void reset() {
        dequeNotSelected.clear();
        dequeSelected.clear();
        dotFour.setSelected(false);
        dotThree.setSelected(false);
        dotTwo.setSelected(false);
        dotOne.setSelected(false);
        dequeNotSelected.push(dotFour);
        dequeNotSelected.push(dotThree);
        dequeNotSelected.push(dotTwo);
        dequeNotSelected.push(dotOne);
    }


}
