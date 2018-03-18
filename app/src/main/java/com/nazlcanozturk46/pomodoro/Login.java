package com.nazlcanozturk46.pomodoro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class Login extends AppCompatActivity {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    public static String fullName, imageUrl,email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token == null){
            setContentView(R.layout.activity_login);
            loginButton = findViewById(R.id.login_button);
            callbackManager = CallbackManager.Factory.create();
            loginButton.setReadPermissions("email");

            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    loadInfo(loginResult.getAccessToken());
                    Intent i = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(i);
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }

            });
        }else{
            loadInfo(AccessToken.getCurrentAccessToken());
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        loginButton.setVisibility(View.GONE);
    }

    public  void loadInfo(AccessToken accessToken){

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                // Application code
                Log.i("object", object.toString());
                try {
                    fullName = object.getString("name");
                    imageUrl = object.getJSONObject("picture").getJSONObject("data").getString("url");
                    email = object.getString("email");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                finish();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,picture,email");
        request.setParameters(parameters);
        request.executeAsync();

    }
}
