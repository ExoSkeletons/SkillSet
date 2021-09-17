package aviadl40.com.skillSet.adapters;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
	private final ArrayList<Fragment> fragments = new ArrayList<>();

	public ViewPagerAdapter(FragmentManager fragmentManager) {
		super(fragmentManager);
	}

	public void add(Collection<Fragment> collection) {
		fragments.addAll(collection);
		notifyDataSetChanged();
	}

	public void clear() {
		fragments.clear();
		notifyDataSetChanged();
	}

	public void set(Collection<Fragment> collection) {
		clear();
		add(collection);
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}
}
