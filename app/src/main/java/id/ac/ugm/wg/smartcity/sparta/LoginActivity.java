package id.ac.ugm.wg.smartcity.sparta;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import id.ac.ugm.wg.smartcity.sparta.app.AppConfig;
import id.ac.ugm.wg.smartcity.sparta.app.AppController;
import id.ac.ugm.wg.smartcity.sparta.helper.SQLiteHandler;
import id.ac.ugm.wg.smartcity.sparta.helper.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private Button btnLogin, btnLinkToRegister;
    private TextInputLayout inputLayoutEmail, inputLayoutPassword;
    private EditText inputEmail, inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputLayoutEmail = (TextInputLayout) findViewById(R.id.TILemail);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.TILpassword);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite Database Handler
        db = new SQLiteHandler(getApplicationContext());

        // Session Manager
        session = new SessionManager(getApplicationContext());

        // Check if user already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to Main Activity
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // The email and password must valid first
                if (validateEmail()) {
                    if (validatePassword()) {
                        String email = inputEmail.getText().toString().trim();
                        String password = inputPassword.getText().toString().trim();
                        checkLogin(email, password);
                    }
                }
            }
        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    // Text Watcher Class
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.email:
                    validateEmail();
                    break;
                case R.id.password:
                    validatePassword();
                    break;
            }
        }
    }

    // Email Validation Function
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
            inputEmail.getBackground().mutate().setColorFilter(getResources().getColor(R.color.text_white), PorterDuff.Mode.SRC_ATOP);
        }

        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    // Password Validator
    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
            inputPassword.getBackground().mutate().setColorFilter(getResources().getColor(R.color.text_white), PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    // Login Function
    private void checkLogin(final String email, final String password){
        // TAG used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("password", password);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_LOGIN, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideDialog();
                            boolean error = response.getBoolean("error");

                            // Check for error node in json
                            if (!error) {
                                // User successfully login
                                // Create a new login session
                                session.setLogin(true);

                                // Now store the user in SQLite
                                JSONObject uid = response.getJSONObject("user");
                                String name = uid.getString("name");
                                String email = uid.getString("email");
                                String created_at = uid.getString("created_at");

                                // Inserting row in user table
                                db.addUser(name, email, uid.toString(), created_at);

                                // Launch main activity
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), response.getString("error_msg"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Toast.makeText(getApplicationContext(), response.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        // add the request object to the queue to be executed
        AppController.getInstance().addToRequestQueue(req);
    }

    private void showDialog(){
        if(!pDialog.isShowing()){
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
