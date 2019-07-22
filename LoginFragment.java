package co.smartfarm.ui.auth;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.smartfarm.R;
import co.smartfarm.ui.base.AlertDialogFragment;
import co.smartfarm.ui.base.BaseFragment;
import co.smartfarm.ui.keyboard.KeyboardView;
import co.smartfarm.ui.widget.DigitsInputFrame;
import co.smartfarm.utils.ElasticSearchUtils;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.Locale;

/**
 * Created by Maksim Baidala
 * maksimbaidala@gmail.com
 */

public class LoginFragment extends BaseFragment implements
        LoginMvpView,
        KeyboardView.OnPassCodeListener {

    public interface LoginInterface {
        void onLoginSuccess();
    }

    public static final int HIDE_DELAY_MS = 500;

    @Inject
    ElasticSearchUtils mElasticSearchClient;

    @Inject
    LoginMvpPresenter<LoginMvpView> mPresenter;

    @BindView(R.id.keyboard)
    KeyboardView mKeyboard;

    @BindView(R.id.digits_frame)
    DigitsInputFrame mDigitsView;

    private LoginInterface mCallback;

    private Handler mHandler = new Handler();


    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (LoginInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ItemClickCallback");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_new, container, false);
        setUnBinder(ButterKnife.bind(this, view));
        mKeyboard.setOnPassCodeListener(this);
        mKeyboard.setOnNumberPressedListener(mDigitsView);
        mPresenter.onAttach(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        resetKeyboard();
    }

    @Override
    protected void setUp(View view) {
        mKeyboard.reset();
    }

    @Override
    public void onDestroyView() {
        mPresenter.onDetach();
        super.onDestroyView();
    }

    @Override
    public void onLoginSuccess() {
        Timber.d("onLoginSuccess");
        // presenter - save auth token
        // main_activity - open next screen
        if (!isNetworkConnected()) {
            showAlertDialog(getString(R.string.login_title_text), getString(R.string.no_internet_text), AlertDialogFragment.TYPE_OK_BUTTON);
        }
        mCallback.onLoginSuccess();
        resetKeyboard();
    }

    @Override
    public void onLoginError(String msg) {
        String s = String.format(Locale.getDefault(), "onLoginError error: %s", msg);
        Timber.d("onLoginError error: %s", msg);
        mElasticSearchClient.log(ElasticSearchUtils.LOG_LEVEL.error, s, getClass().getCanonicalName());
        // presenter - save activation code
        //main_activity - open next screen
        mKeyboard.setEnabled(false);
        resetKeyboard();
        mKeyboard.getErrorView().setText(msg); // "Invalid Password"
        mKeyboard.getErrorView().show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mKeyboard.setEnabled(true);
            }
        }, HIDE_DELAY_MS);

    }

    /** KeyboardView.OnPassCodeListener */
    @Override
    public void onCodeEntered(String codeArray) {
        Timber.d("onCodeEntered");
        if (isLogout()) {
            resetKeyboard();
        }
        mPresenter.onCodeEntered(codeArray);

    }

    private void resetKeyboard() {
        mKeyboard.reset();
        mDigitsView.reset();
    }




}
