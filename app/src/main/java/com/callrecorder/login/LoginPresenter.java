package com.callrecorder.login;

import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;
import android.view.View;

import com.callrecorder.R;
import com.callrecorder.WebServiceProvider;
import com.google.gson.JsonObject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * Created by Shivam on 19/03/18.
 */

public class LoginPresenter<V extends ILoginMvpView> implements ILoginMvpPresenter {

    private final String TAG = LoginPresenter.class.getName();
    private ILoginMvpView mView;
    private WebServiceProvider apiProvider;


    public LoginPresenter(ILoginMvpView mView) {
        this.mView = mView;
        mView.attachPresenter(this);

        apiProvider = WebServiceProvider.Companion.getRetrofit().create(WebServiceProvider.class);
    }


    @Override
    public void onSubmitLogin(TextInputEditText contactNumber, TextInputEditText userName, String deviceId) {
        if (TextUtils.isEmpty(contactNumber.getText().toString())) {

            mView.showError("Mobile number cannot be empty", "number");


        } else if (contactNumber.getText().toString().length() < 10) {

            mView.showError("Invalid mobile number", "number");

        } else if (TextUtils.isEmpty(userName.getText().toString())) {

            mView.showError("UserName cannot be empty", "text");

        } else {
            mView.showLoading();

            loginUser(contactNumber.getText().toString(), userName.getText().toString(), deviceId);

        }
    }


    private void loginUser(String mobileNumber, String userName, String deviceId) {
        JsonObject user = new JsonObject();
        user.addProperty("userName", userName);
        user.addProperty("userMobile", mobileNumber);
        user.addProperty("deviceId", deviceId);

        apiProvider.login(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<LoginResponseBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(LoginResponseBean loginResponse) {
                        mView.hideLoading();
                        if (loginResponse.getStatus()) {
                            mView.onSuccess(loginResponse, userName, mobileNumber);
                        } else if (!TextUtils.isEmpty(loginResponse.getMessage())) {
                            mView.onError(loginResponse.getMessage());
                        } else {

                            mView.onError(R.string.something_went_wrong);
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        mView.hideLoading();
                        mView.onError(R.string.something_went_wrong);
                    }
                });

    }

    @Override
    public View.OnFocusChangeListener addFocusChangeEvent(TextInputEditText inputField, final String inputType) {
        return (v, hasFocus) -> mView.viewFocusChange(hasFocus, inputType);
    }


}
