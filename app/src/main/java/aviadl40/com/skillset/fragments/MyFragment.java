package aviadl40.com.skillset.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import aviadl40.com.skillset.AuthUser;
import aviadl40.com.skillset.User;

public final class MyFragment extends User.UserFragment {

	@NonNull
	@Override
	public AuthUser getUser() {
		return AuthUser.getMe();
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		populateView(view, getUser());
	}
}
