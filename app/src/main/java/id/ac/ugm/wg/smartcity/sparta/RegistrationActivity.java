package id.ac.ugm.wg.smartcity.sparta;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    private Button btnRegister, btnLinkToLogin;
    private EditText inputFullName, inputEmail, inputPassword, inputPasswordConfirm;
    private TextInputLayout inputLayoutFullName, inputLayoutEmail, inputLayoutPassword, inputLayoutPasswordConfirm;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        inputFullName = (EditText)findViewById(R.id.name);
        inputEmail = (EditText)findViewById(R.id.email);
        inputPassword = (EditText)findViewById(R.id.password);
        inputPasswordConfirm = (EditText)findViewById(R.id.passwordConfirm);
        inputLayoutFullName = (TextInputLayout)findViewById(R.id.TILfullname);
        inputLayoutEmail = (TextInputLayout)findViewById(R.id.TILemail);
        inputLayoutPassword = (TextInputLayout)findViewById(R.id.TILpassword);
        inputLayoutPasswordConfirm = (TextInputLayout) findViewById(R.id.TILpasswordConfirm);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button)findViewById(R.id.btnLinkToLoginScreen);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));
        inputPasswordConfirm.addTextChangedListener(new MyTextWatcher(inputPasswordConfirm));

        // Progress Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session Manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String name = inputFullName.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();
                String passwordConfirm = inputPasswordConfirm.getText().toString().trim();

                if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !passwordConfirm.isEmpty()) {
                    registerUser(name, email, password);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        LoginActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

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
                case R.id.passwordConfirm:
                    validatePasswordConfirmation();
                    break;
            }
        }
    }

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

    private boolean validatePasswordConfirmation(){
        if (!inputPasswordConfirm.getText().toString().trim().equals(inputPassword.getText().toString().trim())){
            inputLayoutPasswordConfirm.setError(getString(R.string.err_msg_password_confirmation));
            requestFocus(inputPasswordConfirm);
            return false;
        } else {
            inputLayoutPasswordConfirm.setErrorEnabled(false);
            inputPasswordConfirm.getBackground().mutate().setColorFilter(getResources().getColor(R.color.text_white), PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    private void registerUser(final String name, final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("email", email);
        params.put("password", password);

        JsonObjectRequest req = new JsonObjectRequest(AppConfig.URL_REGISTER, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            hideDialog();
                            boolean error = response.getBoolean("error");
                            if (!error) {
                                // User successfully stored in MySQL
                                // Now store the user in sqlite
                                String uid = response.getString("uid");

                                JSONObject user = response.getJSONObject("user");
                                String name = user.getString("name");
                                String email = user.getString("email");
                                String created_at = user.getString("created_at");

                                // Inserting row in users table
                                db.addUser(name, email, uid, created_at);

                                Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                                // Launch login activity
                                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }else {

                                // Error occurred in registration. Get the error message
                                String errorMsg = response.getString("error_msg");
                                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
