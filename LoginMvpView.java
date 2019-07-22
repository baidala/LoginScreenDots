package co.smartfarm.ui.auth;

import co.smartfarm.ui.base.MvpView;

public interface LoginMvpView extends MvpView {
    void onLoginSuccess();
    void onLoginError(String msg);
}
