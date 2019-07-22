package co.smartfarm.ui.auth;

import co.smartfarm.ui.base.MvpPresenter;

public interface LoginMvpPresenter<V extends LoginMvpView> extends MvpPresenter<V> {
    void onCodeEntered(final String codeArray);
}
