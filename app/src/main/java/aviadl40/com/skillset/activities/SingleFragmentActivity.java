package aviadl40.com.skillset.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.io.Serializable;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import aviadl40.com.skillset.R;

public class SingleFragmentActivity extends FragmentActivity {
	public static void start(Context context, Class<? extends Fragment> fClass, @Nullable Bundle fArgs) {
		context.startActivity(
				new Intent(context, SingleFragmentActivity.class)
						.putExtra(FRAGMENT_CLASS_KEY, fClass)
						.putExtra(FRAGMENT_ARGS_KEY, fArgs)
		);
	}

	private static final String
			FRAGMENT_CLASS_KEY = "fragment activity fragment class key",
			FRAGMENT_ARGS_KEY = "fragment activity fragment args key";

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_single_fragment);

		try {
			Serializable s = getIntent().getSerializableExtra(FRAGMENT_CLASS_KEY);
			if (s instanceof Class) {
				Fragment fragment = ((Class<? extends Fragment>) s).newInstance();

				fragment.setArguments(getIntent().getBundleExtra(FRAGMENT_ARGS_KEY));

				getSupportFragmentManager().beginTransaction()
						.add(R.id.single_fragment_container, fragment)
						.commit();
			}
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
