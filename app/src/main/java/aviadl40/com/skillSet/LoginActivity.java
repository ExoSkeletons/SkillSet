package aviadl40.com.skillSet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import aviadl40.com.skillSet.NetUtils.NetTask;
import aviadl40.com.skillSet.NetUtils.OperationResult;

@SuppressLint("StaticFieldLeak")
public final class LoginActivity extends AppCompatActivity {
	abstract class AuthUserTask extends NetTask<Void, Void, Void> {
		final String mEmail;
		final String mPassword;
		private final View authView;
		private final ProgressBar progressView;

		AuthUserTask(String mEmail, String mPassword, View authView, ProgressBar progressView) {
			this.mEmail = mEmail;
			this.mPassword = mPassword;
			this.authView = authView;
			this.progressView = progressView;
		}

		void showProgress(boolean show) {
			if (show) Utils.fade(authView, progressView);
			else Utils.fade(progressView, authView);
			if (!show) mAuthTask = null;
		}

		@Override
		protected final void onResult(@NonNull final Void result) {
			showProgress(false);
			// Got UUID, go to MainActivity
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finish();
		}

		@Override
		protected void onError(NetUtils.OperationError error) {
			error.makeToast(getApplicationContext()).show();
		}

		@Override
		protected void onPreExecute() {
			showProgress(true);
		}


		@Override
		protected final void onCancelled() {
			showProgress(false);
		}
	}

	public class LoginTask extends AuthUserTask {
		LoginTask(String email, String password) {
			super(email, password, mLoginFormView, mProgressView);
		}

		@Override
		protected OperationResult<Void> doInBackground(Void... params) {
			return AuthUser.login(mEmail, mPassword);
		}
	}

	public class RegisterTask extends AuthUserTask {
		RegisterTask(String mEmail, String mPassword) {
			super(mEmail, mPassword, null, mProgressView);
		}

		@Override
		protected OperationResult<Void> doInBackground(Void... voids) {
			return AuthUser.register(mEmail, mPassword);
		}
	}

	private static final int PASSWORD_LENGTH = 5;
	private AuthUserTask mAuthTask = null;
	private EditText mEmailView;
	private EditText mPasswordView;
	private ProgressBar mProgressView;
	private View mLoginFormView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// TODO: save UUID to file and attempt to validate if present

		mEmailView = findViewById(R.id.email);

		mPasswordView = findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});
		((TextView) findViewById(R.id.password_requirements)).setText(getString(R.string.password_requirements, PASSWORD_LENGTH));

		Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
		mEmailSignInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);

		//startActivity(new Intent(LoginActivity.this, MainActivity.class).putExtra(AuthUser.UUID_KEY, UUID.randomUUID()));
	}

	private void attemptRegister() {
		mEmailView.setError(null);
		mPasswordView.setError(null);

		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		final View failView;

		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			failView = mEmailView;
		} else if (!(
				email.contains("@") &&
						email.indexOf("@") > 0 &&
						email.indexOf("@") == email.lastIndexOf("@") &&
						email.contains(".") && email.lastIndexOf(".") > email.indexOf("@")
		)) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			failView = mEmailView;
		} else if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			failView = mPasswordView;
		} else if (password.length() < PASSWORD_LENGTH) {
			mPasswordView.setError(getString(R.string.error_password_short));
			failView = mPasswordView;
		} else if (!(
				password.matches("[a-z]") &&
						password.matches("[A-Z]") &&
						password.matches("[0-9]")
		)) {
			mPasswordView.setError(getString(R.string.error_password_requirements));
			failView = mPasswordView;
		} else {
			mAuthTask = new RegisterTask(email, password);
			return;
		}
		failView.requestFocus();
	}

	private void attemptLogin() {
		if (mAuthTask != null)
			return;

		mEmailView.setError(null);
		mPasswordView.setError(null);

		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		final View failView;

		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			failView = mEmailView;
		} else if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			failView = mPasswordView;
		} else {
			mAuthTask = new LoginTask(email, password);
			mAuthTask.execute();
			return;
		}
		failView.requestFocus();
	}
}

