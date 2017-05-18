package com.example.john.johnjenproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Output;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;



import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity  {
    private static final int REQUEST_READ_CONTACTS = 0;

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                Context context = getApplicationContext();
                CharSequence text = message.obj.toString();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
        };

    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (!isEmailValid(email)) {
            Context context = getApplicationContext();
            CharSequence text = "Username is invalid";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (!isPasswordValid(password)) {
            Context context = getApplicationContext();
            CharSequence text = "Password is invalid";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        mAuthTask = new UserLoginTask(email, password);
        mAuthTask.execute((Void) null);
    }

    private boolean isEmailValid(String email) {
        return email.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 0;
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private SharedPreferences sharedPref;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
            sharedPref = getSharedPreferences(getString(R.string.shared_pref_key), MODE_PRIVATE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String result = null;
            InputStream in = null;
            Log.d("Attempting to login ", mUsername);

            try {
                JSONObject cred = new JSONObject();
                cred.accumulate("username", mUsername);
                cred.accumulate("password", mPassword);

                Log.d("JSON ", cred.toString());

                String urlParameters = cred.toString();
                byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;

                URL url = new URL("http://104.236.190.64:8000/users/");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                OutputStream os = httpURLConnection.getOutputStream();
                os.write(postData);
                os.close();
                int i = httpURLConnection.getResponseCode();
                Log.d("Got response ", Integer.toString(i));

                if (i != 201 && i != 200) {
                    Log.d("Error ", httpURLConnection.getResponseMessage());
                    Message message = mHandler.obtainMessage();

                    BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    message.obj = sb.toString();
                    message.sendToTarget();
                    mAuthTask = null;
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                }

            }
            catch (Exception e) {
                Log.d("Exception: ", e.toString());
            }

            return true;
        }
    }
}

