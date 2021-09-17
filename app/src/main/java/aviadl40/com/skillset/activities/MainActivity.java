package aviadl40.com.skillset.activities;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import aviadl40.com.skillset.AuthUser;
import aviadl40.com.skillset.Item;
import aviadl40.com.skillset.R;
import aviadl40.com.skillset.fragments.DiscoverFragment;
import aviadl40.com.skillset.fragments.FeedFragment;
import aviadl40.com.skillset.fragments.MyFragment;
import aviadl40.com.skillset.fragments.UploadFragment;

@SuppressWarnings({"unused"})
public final class MainActivity extends FragmentActivity {
	public static Resources res;
	public static ContentResolver resolver;

	private final Fragment[] menuFragments = new Fragment[]{
			new FeedFragment(),
			new UploadFragment(),
			new MyFragment(),
			new DiscoverFragment()
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		res = getResources();
		resolver = getContentResolver();

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
		navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Item.AudioItem.stop();
	}
}