package aviadl40.com.skillSet.fragments;

import androidx.annotation.NonNull;
import aviadl40.com.skillSet.AuthUser;

public final class MyFragment extends UserFragment {
	@NonNull
	@Override
	public AuthUser getUser() {
		return AuthUser.getMe();
	}
}
