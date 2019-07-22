package co.smartfarm.ui.auth;

import android.content.Context;
import co.smartfarm.R;
import co.smartfarm.SmartFarmApp;
import co.smartfarm.data.DataManager;
import co.smartfarm.data.api.mapper.UserDtoToDBMapper;
import co.smartfarm.data.api.model.response.UserTokenDto;
import co.smartfarm.ui.base.BasePresenter;
import co.smartfarm.utils.ElasticSearchUtils;
import co.smartfarm.utils.NetworkManager;
import com.google.gson.stream.MalformedJsonException;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import javax.inject.Inject;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.concurrent.Callable;

public class LoginPresenter <V extends LoginMvpView> extends BasePresenter<V> implements LoginMvpPresenter<V> {

    private final CompositeDisposable mDisposable;
    private DataManager mDataManager;
    private String code;
    private Context context = SmartFarmApp.applicationContext;

    @Inject
    ElasticSearchUtils mElasticSearchClient;

    @Inject
    public LoginPresenter(DataManager dataManager, CompositeDisposable compositeDisposable) {
        super(dataManager, compositeDisposable);
        mDisposable = new CompositeDisposable();
        mDataManager = getDataManager();
    }

    @Override
    public void onCodeEntered(final String codeArray) {
        if (!isViewDetached()) getMvpView().showLoading();
//        code = arrayToString(codeArray);
        code = codeArray;
        String msg = String.format (Locale.getDefault(),"onCodeEntered %s", code);
        Timber.d(msg);
//        mElasticSearchClient.log(ElasticSearchUtils.LOG_LEVEL.info, msg, getClass().getCanonicalName());

        checkInternetConnection();
        //send auth request
        // handle result
        // save token to prefs
        // save user info to db
        /*sendAuthRequest(code);*/

        //for debug only
        /*if (code.equals(passCode)) {
            if (!isViewDetached()) {
                getMvpView().hideLoading();
                getMvpView().onLoginSuccess();
            }
        } else {
            if (!isViewDetached()) {
                getMvpView().hideLoading();
                getMvpView().onLoginError("error");
            }
        }*/

    }

    private void checkInternetConnection() {
        mDisposable.add(Observable.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        if (!isViewDetached()) getMvpView().showMessage("Checking Internet Connection");
                        Timber.d("Checking Internet Connection");
                        return NetworkManager.isInternetAvailable(context, mDataManager.getWifiPoint());
                    }
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(available ->{
                            if (available){
                                sendAuthRequest(code);
                            } else { if (!isViewDetached()) {
                                        getMvpView().hideLoading();
                                        getMvpView().onLoginError(context.getString(R.string.no_internet_text));
                                    } }
                            }, throwable -> {
                            String msg = String.format(Locale.getDefault(), "Checking Internet Connection: %s", throwable.toString());
                            mElasticSearchClient.log(ElasticSearchUtils.LOG_LEVEL.error, msg, getClass().getCanonicalName());
                            Timber.e("Checking Internet Connection: throwable = %s", throwable.getMessage());
                        })
        );
    }

    private void sendAuthRequest(String code) {
//        User user = mDataManager.getUserByCode(code);
        mDisposable.add(mDataManager
                .getUserByCode(code)
                .flatMap(user -> { return mDataManager.authenticateUser(user.getEmail(), user.getCode()); })
                .map(tokenResponseDto -> {
                    if (tokenResponseDto.getError() == null) {
                        mDataManager.saveToken(tokenResponseDto.getToken());
                        mDataManager.saveCurrentUser(new UserDtoToDBMapper().apply(tokenResponseDto.getUserData()));
                    }
                    return tokenResponseDto;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> getMvpView().showLoading())
                .subscribe(this::handleResponse,
                        t -> {
                            if (t instanceof NullPointerException ) {
                                if (!isViewDetached()) {
                                    getMvpView().hideLoading();
                                    getMvpView().onLoginError("User not found. Please update user info from server.");
                                }
                            } else if (t instanceof UnknownHostException) {
                                if (!isViewDetached()) {
                                    getMvpView().hideLoading();
                                    getMvpView().onLoginError(context.getString(R.string.no_internet_text));
                                }
                            } else if (t instanceof MalformedJsonException) {
                                getMvpView().hideLoading();
                                getMvpView().onLoginError(context.getString(R.string.connection_error_text));
                                Timber.e("sendRoundDetailsToFMS: MalformedJsonException = %s", t.getMessage());
                            } else {
                                if (!isViewDetached()) {
                                    getMvpView().hideLoading();
                                    getMvpView().onLoginError(t.getMessage());
                                }

                            }
                            String msg = String.format(Locale.getDefault(), "sendAuthRequest: throwable = %s", t.toString());
                            mElasticSearchClient.log(ElasticSearchUtils.LOG_LEVEL.error, msg, getClass().getCanonicalName());
                            Timber.e("sendAuthRequest: throwable = %s", t.toString());
                        }));
    }

    private void handleResponse(UserTokenDto tokenResponseDto) {
        Timber.d("handleResponse");
        if (tokenResponseDto.getError() != null) {
            StringBuilder s = new StringBuilder();
            if (!tokenResponseDto.getError().isEmpty()) {
                s.append(tokenResponseDto.getError());
            }
            if (!isViewDetached()) {
                getMvpView().hideLoading();
                getMvpView().onLoginError(s.toString());
            }
        } else {
            if (!isViewDetached()) {
                getMvpView().hideLoading();
                getMvpView().onLoginSuccess();
            }
        }
    }

    /*private String arrayToString(int[] codeArray) {
        StringBuilder str = new StringBuilder();
        for (int i = codeArray.length; i > 0; i--) {
            str.append(codeArray[i-1]);
        }
        *//*for (int aCode : codeArray) {
            str.append(aCode);
        }*//*
        return str.toString();
    }*/


    @Override
    public void onDetach() {
        super.onDetach();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.clear();
        }
    }


}
