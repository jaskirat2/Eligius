package in.techaddicts.eligius;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public void switch_to_social(View view)
    {
        final Intent y1 = new Intent(this,SocialmediaActivity.class);
        startActivity(y1);
    }

    enum  State{
        SIGNUP,LOGIN
    }
    private State state;
    private Button btnSignUpLogin, btnOneTimeLogin;
    private RadioButton driverRadioButton, passengerRadioButton;
    private EditText edtUserName, edtPassword, edtDriverOrPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if (ParseUser.getCurrentUser() != null)                              //If the user is logged in once then always go to passenger activity
        {
            // transition
            // ParseUser.logOut();
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }

        btnSignUpLogin = findViewById(R.id.btnSignUpLogin);
        driverRadioButton = findViewById(R.id.rdbDriver);
        passengerRadioButton = findViewById(R.id.rdbPassenger);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        btnOneTimeLogin.setOnClickListener(this);                   //One time Login.....

        final LoadingDialog loadingDialog = new LoadingDialog(MainActivity.this);                      // This line is added for loading effect for sign up and Log in

        state = State.SIGNUP;

        edtUserName = findViewById(R.id.edtUserName);              //these three are the textviews..
        edtPassword = findViewById(R.id.edtPassword);
        edtDriverOrPassenger = findViewById(R.id.edtDOrP);

        btnSignUpLogin.setOnClickListener(new View.OnClickListener() {          //when sign up button is pressed...
            @Override
            public void onClick(View v) {

                loadingDialog.startLoadingDialog();                                                             // This line is added for loading effect for sign up and Log in
                Handler handler = new Handler();                                                                // This line is added for loading effect for sign up and Log in
                handler.postDelayed(new Runnable() {                                                            // This line is added for loading effect for sign up and Log in
                    @Override                                                                                   // This line is added for loading effect for sign up and Log in
                    public void run() {                                                                         // This line is added for loading effect for sign up and Log in
                        loadingDialog.dismissDialog();                                                          // This line is added for loading effect for sign up and Log in
                    }                                                                                           // This line is added for loading effect for sign up and Log in
                }, 3000);                                                                             // This line is added for loading effect for sign up and Log in

                if (state == State.SIGNUP) {

                    if (driverRadioButton.isChecked() == false && passengerRadioButton.isChecked() == false)                  //if user cannot press either driver or passenger
                    {                                                                                                         // Radio button then he will not be signup..
                        Toast t = Toasty.normal(MainActivity.this, "Are you a driver or a passenger?", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0,-690);
                        t.show();
                        return;
                    }
                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtUserName.getText().toString());
                    appUser.setPassword(edtPassword.getText().toString());
                    if (driverRadioButton.isChecked())                         //column as = Driver
                    {
                        appUser.put("as", "Driver");

                    }
                    else if (passengerRadioButton.isChecked()) {
                        appUser.put("as", "Passenger");                    //column as = Passenger
                    }

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null) {
                                Toast t = Toasty.success(MainActivity.this, "Signed Up!", Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.CENTER, 0,-200);
                                t.show();

                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();

                            }
                        }
                    });

                }
                else if (state == State.LOGIN)                    // if state is login..
                {
                    ParseUser.logInInBackground(edtUserName.getText().toString(), edtPassword.getText().toString(), new LogInCallback()
                    {
                        @Override
                        public void done(ParseUser user, ParseException e) {

                            if (user != null && e == null) {
                                Toast t = Toasty.success(MainActivity.this, "User Logged in", Toast.LENGTH_SHORT);
                                t.setGravity(Gravity.CENTER, 0,-200);
                                t.show();

                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });

                }
            }
        });


    }                   //End of on create method...

    public boolean onCreateOptionsMenu(Menu menu)                       //Create menu..
    {
        getMenuInflater().inflate(R.menu.menu_signup_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)                   //When we click on the log in or signup in the menu bar..
    {
        switch (item.getItemId())
        {
            case R.id.loginItem:                               //Id of menu bar
                if (state == State.SIGNUP)                     //If state is sign up then
                {
                    state = State.LOGIN;                      // Then change state from sign up tp login..
                    item.setTitle("Sign Up");                 // Set text of menu bar Sign up..
                    btnSignUpLogin.setText("Log In");
                } else if (state == State.LOGIN) {

                    state = State.SIGNUP;
                    item.setTitle("Log In");
                    btnSignUpLogin.setText("Sign Up");
                }


                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override                              //When the user click on the  One Time Login Button...
    public void onClick(View view)
    {
        String s = edtDriverOrPassenger.getText().toString();
        int len = s.length();
        if (len >= 1)
        {
            if (ParseUser.getCurrentUser() == null)
            {
                ParseAnonymousUtils.logIn(new LogInCallback()               //Create anonymous user..means guest..
                {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (user != null && e == null)
                        {
                            Toast t = Toasty.info(MainActivity.this, "We have an anonymous user", Toast.LENGTH_SHORT);
                            t.setGravity(Gravity.CENTER, 0,125);
                            t.show();

                            user.put("as", "Passenger");                //Column as = driver or passenger..

                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e)
                                {
                                    transitionToPassengerActivity();
                                    //transitionToDriverRequestListActivity();
                                }
                            });
                        }
                    }
                });
            }
        }
        else {
            Toast t = Toasty.error(MainActivity.this, "Please specify your Name", Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0,125);
            t.show();
            return;
        }
    }



    private void transitionToPassengerActivity()                  //Go to passenger activity........
    {
        if (ParseUser.getCurrentUser() != null)
        {
            if (ParseUser.getCurrentUser().get("as").equals("Passenger"))                //If the radio button pressed is Passenger....
            {
                Intent intent = new Intent(MainActivity.this, PassengerActivity.class);
                startActivity(intent);
            }
        }
    }

    private void transitionToDriverRequestListActivity()
    {
        if (ParseUser.getCurrentUser() != null)
        {
            if (ParseUser.getCurrentUser().get("as").equals("Driver"))
            {
                Intent intent = new Intent(this, DriverRequestListActivity.class);
                startActivity(intent);
            }
        }
    }


}