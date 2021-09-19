package aviadl40.com.skillset.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import aviadl40.com.skillset.AuthUser;
import aviadl40.com.skillset.Item;
import aviadl40.com.skillset.NetUtils;
import aviadl40.com.skillset.NetUtils.ArrayNetTask;
import aviadl40.com.skillset.NetUtils.OperationResult;
import aviadl40.com.skillset.R;
import aviadl40.com.skillset.User.HasUser;
import aviadl40.com.skillset.Utils;
import aviadl40.com.skillset.adapters.ViewPagerAdapter;

@SuppressWarnings({"StaticFieldLeak"})
public final class FeedFragment extends Fragment implements HasUser<AuthUser> {
	private class FeedItemsTask extends ArrayNetTask<Void, Item, Item> {
		FeedItemsTask(ViewPagerAdapter pager) {
			super(pager.getCount());
		}

		// loadItems(ba)
		// if not empty(ba)
		//     iterate in ba ...?
		//         read ItemType from ba -> iType.iClass.newInstance().readCategory(ba) ...?
		// return items

		// TODO: INIT DOWNLOAD
		// onCreateView
		// Show loading icon
		// download X amount from web into ba
		// items = loadItems(ba)
		// for object : items
		//    put object into f = new ItemFragment()
		//    add f to fragments
		// TODO: DYNAMIC DOWNLOAD
		// onEndReached(?)
		// download Y amount from web into ba
		// items = loadItems(ba)
		// for object : items
		//    put object into f = new ItemFragment()
		//    add f to fragment

		@Override
		protected OperationResult<ArrayList<Item>> doInBackground(Void... voids) {
			return getUser().myFeed(startPosition);
		}

		@Override
		protected void onResult(ArrayList<Item> result) {
			final ArrayList<Fragment> fragments = new ArrayList<>();
			for (Item i : result) {
				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
					Item.writeItem(baos, i);
					Fragment f = new Item.ItemFragment();
					Bundle args = new Bundle();
					args.putByteArray(ObjectFragment.OBJECT_KEY, baos.toByteArray());
					f.setArguments(args);
					fragments.add(f);
				} catch (Exception ignored) {
				}
			}
			pager.add(fragments);
		}

		@Override
		protected void onError(NetUtils.OperationError error) {
			error.makeToast(getContext()).show();
		}
	}

	private static FeedItemsTask itemQuery = null;

	private ViewPagerAdapter pager;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		pager = new ViewPagerAdapter(getChildFragmentManager());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_feed, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		((ViewPager) view.findViewById(R.id.feed_pager)).setAdapter(pager);
		Utils.cancelTask(itemQuery);
		itemQuery = new FeedItemsTask(pager);
		itemQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	@Override
	public void onDestroyView() {
		Utils.cancelTask(itemQuery);
		super.onDestroyView();
	}

	@NonNull
	@Override
	public AuthUser getUser() {
		return AuthUser.getMe();
	}
}
