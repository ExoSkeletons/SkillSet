package aviadl40.com.skillset;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import aviadl40.com.skillset.NetUtils.OperationError;
import aviadl40.com.skillset.NetUtils.OperationResult;

public final class AuthUser extends User {
	private static AuthUser me = null;

	public static AuthUser getMe() {
		return me;
	}

	public static boolean isUserNameValid(String userName) {
		return userName.matches("[A-z,0-9]");
	}

	public static boolean isPhoneNumberValid(long phoneNumber) {
		return 1000000000L < phoneNumber && phoneNumber < 9999999999L;
	}

	public static OperationResult<Void> login(String email, String password) {
		// TODO: attempt authentication against a network service
		me = new AuthUser(UUID.randomUUID(), "Auth", null);
		return new OperationResult<>((Void) null);
	}

	public static OperationResult<Void> register(String email, String password) {
		// TODO: do register
		return login(email, password);
	}

	public static OperationResult<Void> logout() {
		if (me != null)
			me.invalidateKey();
		me = null;
		return new OperationResult<>((Void) null);
	}

	public static OperationResult<Void> change(String email, String password) {
		logout();
		return login(email, password);
	}

	private final UUID authKey;

	private AuthUser(UUID authKey, @NonNull String username, @Nullable Bitmap picture) {
		super(username, picture);
		this.authKey = authKey;
	}

	private OperationResult<Void> invalidateKey() {
		// TODO: inform server
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<Void> updateProfilePicture(@Nullable Bitmap profilePic) {
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<Void> updatePhoneNumber(byte phoneNumber) {
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<User> follow(User friend, boolean follow) {
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<Void> sendFriendRequest(User user) {
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<User> acceptFriendRequest(int requestID) {
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<Void> uploadItem(Item item) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		return new OperationResult<>((Void) null);
	}

	public OperationResult<Void> removeItem(int itemID) {
		return new OperationResult<>(OperationError.TIMEOUT);
	}

	public OperationResult<ArrayList<Item>> myFeed(int startPosition) {
		return new OperationResult<>(Utils.DUMMY_ITEMS());
	}
}
