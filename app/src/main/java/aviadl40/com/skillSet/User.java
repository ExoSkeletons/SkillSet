package aviadl40.com.skillSet;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import aviadl40.com.skillSet.NetUtils.OperationResult;


public class User {
	private static Bitmap nameToBitmap(String username) {
		StringBuilder shortName = new StringBuilder();
		for (String word : username.split(" ", 2))
			shortName.append(word.charAt(0));
		return Utils.textAsBitmap(shortName.toString(), 64, Color.BLUE, Color.BLACK);
	}

	public static User randomUser() {
		String[] names = new String[]{
				"דן",
				"herobrine",
				"mulan",
				"Jeff",
				"אברהם",
				"ישראל",
				"Charles Christopher Williams",
				"Woof",
				"נחום"
		};
		return new User(names[(int) (Math.random() * names.length)], null);
	}

	public static User findUser(String username) {
		return null; // TODO: implement
	}

	public static User readUser(InputStream i) throws IOException {
		return new User(StreamUtils.readString(i), StreamUtils.readBitmap(i));
	}

	public static void writeUser(User u, OutputStream o) throws IOException {
		StreamUtils.write(o, u.username);
		StreamUtils.write(o, u.picture, true);
	}

	public interface HasUser<U extends User> {
		U getUser();
	}

	public static final User ANON = new User("anon", null);
	public static final String KEY = "user data key";
	@NonNull
	public final String username;
	@NonNull
	public final Bitmap picture;

	User(@NonNull String username, @Nullable Bitmap picture) {
		this.username = username;
		this.picture = picture == null ? nameToBitmap(username) : picture;
	}

	// FIXME: actually contact server, send our identifier and get items back
	public final OperationResult<ArrayList<Item>> myItems(Category category, int startPosition) {
		ArrayList<Item> res = MainActivity.DUMMY_ITEMS();
		if (category != Category.ALL)
			for (Item item : res)
				item.setCategory(category);
		return new OperationResult<>(res);
	}

	public final OperationResult<ArrayList<Item>> myItems(int startPosition) {
		return myItems(Category.ALL, startPosition);
	}

	public final OperationResult<ArrayList<Category>> myCategories() {
		ArrayList<Category> res = Utils.DUMMY_CATEGORIES();
		res.add(0, Category.ALL);
		res.add(1, Category.NONE);
		return new OperationResult<>(res);
	}

	@Override
	public final boolean equals(Object obj) {
		return obj instanceof User && ((User) obj).username.equals(username);
	}
}
