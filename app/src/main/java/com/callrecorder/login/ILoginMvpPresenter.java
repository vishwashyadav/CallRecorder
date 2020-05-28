package com.callrecorder.login;


import android.support.design.widget.TextInputEditText;
import android.view.View;


public interface ILoginMvpPresenter {

    void onSubmitLogin(TextInputEditText contactNumber, TextInputEditText userName, String deviceId);

    View.OnFocusChangeListener addFocusChangeEvent(TextInputEditText inputField, String inputType);

}
