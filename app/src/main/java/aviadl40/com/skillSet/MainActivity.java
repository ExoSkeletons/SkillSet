package aviadl40.com.skillSet;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import aviadl40.com.skillSet.fragments.DiscoverFragment;
import aviadl40.com.skillSet.fragments.FeedFragment;
import aviadl40.com.skillSet.fragments.MyFragment;
import aviadl40.com.skillSet.fragments.UploadFragment;

@SuppressWarnings({"unused"})
public final class MainActivity extends FragmentActivity {
	private static MainActivity me;

	@SuppressWarnings("unchecked")
	public static ArrayList<Item> DUMMY_ITEMS() {
		Resources res = me.getResources();

		// Dummy Items
		final Item[] items = new Item[]{
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round)),
				new Item.TextItem(User.randomUser(), "Call me Ishmael. Some years ago- never mind how long precisely- having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world."),
				new Item.AudioItem(User.randomUser(), me.getContentResolver(), Utils.uriFromRes(me, R.raw.man)),
				new Item.VideoItem(User.randomUser(), me.getContentResolver(), Utils.uriFromRes(me, R.raw.snack)),
				new Item.TextItem(User.randomUser(), "This was a triumph.\nI'm making a note here- \"HUGE SUCCESS\".\nIt's hard to overstate my satisfaction.\nAperture Science."),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round)),
				new Item.AudioItem(User.randomUser(), me.getContentResolver(), Utils.uriFromRes(me, R.raw.man)),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round)),
				new Item.AudioItem(User.randomUser(), me.getContentResolver(), Utils.uriFromRes(me, R.raw.man)),
				new Item.TextItem(User.randomUser(), "Call me Ishmael. Some years ago- never mind how long precisely- having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world."),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher)),
		};
		ArrayList<Category> categories = Utils.DUMMY_CATEGORIES();
		categories.add(Category.NONE);
		ArrayList<Item.Comment> comments = Utils.DUMMY_COMMENTS();
		for (Item i : items) {
			// Dummy comments
			i.comments.addAll(comments);
			// Dummy categories
			i.setCategory(categories.get((int) (Math.random() * categories.size())));
		}
		return new ArrayList<>(Arrays.asList(items));
	}

	private final Fragment[] menuFragments = new Fragment[]{
			new FeedFragment(),
			new UploadFragment(),
			new MyFragment(),
			new DiscoverFragment()
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		me = this;

		if (AuthUser.getMe() == null) { // Unauthorized
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}

		setContentView(R.layout.activity_main);

		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		for (Fragment f : menuFragments)
			t.add(R.id.fragmentContainer, f);
		t.commit();

		final BottomNavigationView navigation = findViewById(R.id.navigation);
		navigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item) {
				final Fragment fragment;
				switch (item.getItemId()) {
					case R.id.navigation_feed:
						fragment = menuFragments[0];
						break;
					case R.id.navigation_upload:
						fragment = menuFragments[1];
						break;
					case R.id.navigation_myStuff:
						fragment = menuFragments[2];
						break;
					case R.id.navigation_discover:
						fragment = menuFragments[3];
						break;
					default:
						return false;
				}
				FragmentTransaction t = getSupportFragmentManager().beginTransaction();
				for (Fragment f : menuFragments)
					t.hide(f);
				t
						.show(fragment)
						.commit();
				return true;
			}
		});
		navigation.setSelectedItemId(R.id.navigation_feed);
	}
}