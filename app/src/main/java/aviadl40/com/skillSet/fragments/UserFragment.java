package aviadl40.com.skillSet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import aviadl40.com.skillSet.Category;
import aviadl40.com.skillSet.Item;
import aviadl40.com.skillSet.NetUtils;
import aviadl40.com.skillSet.NetUtils.ContentArrayNetTask;
import aviadl40.com.skillSet.NetUtils.OperationResult;
import aviadl40.com.skillSet.R;
import aviadl40.com.skillSet.User;
import aviadl40.com.skillSet.User.HasUser;
import aviadl40.com.skillSet.Utils;
import aviadl40.com.skillSet.adapters.ArrayRecyclerViewAdapter;

@SuppressWarnings({"StaticFieldLeak"})
public class UserFragment extends Fragment implements HasUser<User> {
	private final class GetMyCategories extends ContentArrayNetTask<Object, Void, Category> {
		GetMyCategories() {
			super(categoriesAdapter, false);
		}

		@Override
		protected OperationResult<ArrayList<Category>> doInBackground(Object... objects) {
			return getUser().myCategories();
		}

		@Override
		protected void onResult(ArrayList<Category> result) {
			super.onResult(result);
			cachedItems.clear();
			for (Category key : result)
				cachedItems.put(key, new ArrayList<Item>());
			categoriesAdapter.setSelected(Category.ALL);
		}
	}

	private class GetMyItems extends ContentArrayNetTask<Object, Void, Item> {
		final Category category;

		GetMyItems(Category category, boolean append) {
			super(itemsAdapter, append);
			this.category = category;
		}

		@Override
		protected void onError(NetUtils.OperationError error) {
			error.makeToast(getContext()).show();
		}

		@Override
		protected void onPreExecute() {
			System.out.println("Starting object task with append=" + append + " and category \"" + category.name + "\"");
		}


		@Override
		protected OperationResult<ArrayList<Item>> doInBackground(Object... params) {
			return category == Category.ALL ? getUser().myItems(startPosition) : getUser().myItems(category, startPosition);
		}

		@Override
		protected void onResult(ArrayList<Item> result) {
			super.onResult(result);
			System.out.print((append ? "added" : "got") + " " + result.size() + " items [");
			for (Item i : result)
				System.out.print(i.iType + ":" + i.getCategory().name + ",");
			System.out.println("]");
			ArrayList items = cachedItems.get(category);
			if (items != null) {
				if (!append)
					items.clear();
				//noinspection unchecked
				items.addAll(result);
			}
		}
	}

	private final class CategorySelectionAdapter extends Category.PreviewAdapter {
		private int selected = -1;

		private boolean setSelected(final int position) {
			itemsList.smoothScrollToPosition(0);
			if (this.selected == position)
				return false;
			System.out.print("Selecting [" + position + "]: ");
			itemsAdapter.clear();
			if (position >= 0) {
				Category category = get(position);
				ArrayList<Item> items = cachedItems.get(category);
				System.out.println("\"" + category + "\"");
				System.out.println("Cache in category \"" + category + "\" " + (items == null ? "is null" : "has " + items.size() + " items"));
				if (items == null || items.isEmpty()) {
					Utils.cancelTask(itemsQuery);
					itemsQuery = new GetMyItems(category, false) {
						@Override
						protected void onResult(ArrayList<Item> result) {
							super.onResult(result);
							CategorySelectionAdapter.this.selected = position;
						}
					};
					itemsQuery.execute();
					return true;
				}
				System.out.println("Using cache items");/**/
				itemsAdapter.set(cachedItems.get(category));
			}
			this.selected = position;
			return true;
		}

		boolean setSelected(Category selected) {
			return setSelected(positionOf(selected));
		}

		@Nullable
		Category getSelected() {
			return get(selected);
		}

		@NonNull
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return super.onCreateViewHolder(parent, viewType);
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, int position, final Category item) {
			super.onBindViewHolder(holder, position, item);
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setSelected(holder.getBindingAdapterPosition());
				}
			});
		}
	}

	private final HashMap<Category, ArrayList<Item>> cachedItems = new HashMap<>();
	private RecyclerView itemsList;
	private GetMyCategories categoriesQuery = null;
	private GetMyItems itemsQuery = null;
	private CategorySelectionAdapter categoriesAdapter = new CategorySelectionAdapter();
	private ArrayRecyclerViewAdapter<Item, ViewHolder> itemsAdapter = new Item.PreviewAdapter();
	private User user;

	@Override
	public void setArguments(Bundle args) {
		byte[] bs = args.getByteArray(User.KEY);
		if (bs != null) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(bs);
				user = User.readUser(bais);
			} catch (IOException e) {
				e.printStackTrace();
				onDestroy();
			}
		}
		super.setArguments(args);
	}

	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.my_stuff, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
		User user = getUser();
		if (user != null) {
			((TextView) view.findViewById(R.id.my_name)).setText(user.username);
			((ImageView) view.findViewById(R.id.my_picture)).setImageBitmap(user.picture);
			((RecyclerView) view.findViewById(R.id.my_categories)).setAdapter(categoriesAdapter);
			itemsList = view.findViewById(R.id.my_items);
			itemsList.addOnScrollListener(new Utils.ContentScrollListener(itemsAdapter) {
				@Override
				protected void onEndReached(RecyclerView recyclerView) {
					if (categoriesAdapter.getSelected() != null && !Utils.isRunning(categoriesQuery) && !Utils.isRunning(itemsQuery)) {
						itemsQuery = new GetMyItems(categoriesAdapter.getSelected(), true);
						itemsQuery.execute();
					}
				}
			});
			itemsList.setAdapter(itemsAdapter);
			categoriesQuery = new GetMyCategories();
			categoriesQuery.execute();
		}
	}

	@Override
	public void onDestroyView() {
		Utils.cancelTask(categoriesQuery);
		Utils.cancelTask(itemsQuery);
		super.onDestroyView();
	}

	@Override
	public User getUser() {
		return user;
	}
}