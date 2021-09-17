package aviadl40.com.skillset;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.NetUtils.ContentArrayNetTask;
import aviadl40.com.skillset.NetUtils.OperationResult;
import aviadl40.com.skillset.Skill.SkillPreviewAdapter;
import aviadl40.com.skillset.activities.SingleFragmentActivity;
import aviadl40.com.skillset.adapters.ArrayRecyclerViewAdapter;
import aviadl40.com.skillset.fragments.ObjectFragment;


public class User implements Expandable {
	@SuppressWarnings({"StaticFieldLeak"})
	public static class UserFragment extends ObjectFragment<User> implements HasUser<User> {
		private final class GetMyCategories extends ContentArrayNetTask<Object, Void, Skill> {
			GetMyCategories() {
				super(selectionAdapter, false);
			}

			@Override
			protected OperationResult<ArrayList<Skill>> doInBackground(Object... objects) {
				return getUser().myCategories();
			}

			@Override
			protected void onResult(ArrayList<Skill> result) {
				super.onResult(result);
				cachedItems.clear();
				for (Skill key : result)
					cachedItems.put(key, new ArrayList<Item>());
				selectionAdapter.setSelected(Skill.ALL);
			}
		}

		private final class GetMyItems extends ContentArrayNetTask<Object, Void, Item> {
			final Skill skill;

			GetMyItems(Skill skill, boolean append) {
				super(itemsAdapter, append);
				this.skill = skill;
			}

			@Override
			protected void onError(NetUtils.OperationError error) {
				error.makeToast(getContext()).show();
			}

			@Override
			protected void onPreExecute() {
				System.out.println("Starting object task with append=" + append + " and skill \"" + skill.name + "\"");
			}

			@Override
			protected OperationResult<ArrayList<Item>> doInBackground(Object... params) {
				return skill == Skill.ALL ? getUser().myItems(startPosition) : getUser().myItems(skill, startPosition);
			}

			@Override
			protected void onResult(ArrayList<Item> result) {
				super.onResult(result);
				System.out.print((append ? "added" : "got") + " " + result.size() + " items [");
				for (Item i : result)
					System.out.print(i.iType + ":" + i.getSkill().name + ",");
				System.out.println("]");
				if (!append)
					cachedItems.get(skill).clear();
				cachedItems.get(skill).addAll(result);
			}
		}

		private final class SkillSelectionAdapter extends SkillPreviewAdapter {

			//fixme: may delete

			private int selected = -1;

			private boolean setSelected(final int position) {
				itemsList.smoothScrollToPosition(0);
				if (this.selected == position)
					return false;
				System.out.print("Selecting [" + position + "]: ");
				itemsAdapter.clear();
				Utils.cancelTask(itemsQuery);
				if (position >= 0) {
					Skill skill = get(position);
					System.out.println("\"" + skill + "\"");
					System.out.println("Cache in skill \"" + skill + "\" " + (cachedItems.get(skill) == null ? "is null" : "has " + cachedItems.get(skill).size() + " items"));
					if (cachedItems.get(skill) == null || cachedItems.get(skill).isEmpty()) {
						itemsQuery = new GetMyItems(skill, false);
						itemsQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} else {
						System.out.println("Using cache items");
						itemsAdapter.set(cachedItems.get(skill));
					}
				}
				this.selected = position;
				return true;
			}

			boolean setSelected(Skill selected) {
				return setSelected(positionOf(selected));
			}

			@Nullable
			Skill getSelected() {
				return get(selected);
			}

			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return super.onCreateViewHolder(parent, viewType);
			}

			@Override
			public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position, final Skill skill) {
				super.onBindViewHolder(holder, position, skill);
				holder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						setSelected(holder.getAdapterPosition());
					}
				});
			}
		}

		private final HashMap<Skill, ArrayList<Item>> cachedItems = new HashMap<>();
		RecyclerView itemsList;
		private GetMyCategories skillsQuery = null;
		private GetMyItems itemsQuery = null;
		private SkillSelectionAdapter selectionAdapter = new SkillSelectionAdapter();
		private ArrayRecyclerViewAdapter<Item, RecyclerView.ViewHolder> itemsAdapter = new Item.ItemPreviewAdapter();

		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup

				container, Bundle savedInstanceState) {
			return inflater.inflate(R.layout.user_info, container, false);
		}

		@Override
		protected User readObject(InputStream is) throws IOException {
			return readUser(is);
		}

		@Override
		protected void populateView(@NonNull View view, @NonNull User user) {
			((TextView) view.findViewById(R.id.my_name)).setText(user.username);
			((ImageView) view.findViewById(R.id.my_picture)).setImageBitmap(user.picture);
			((RecyclerView) view.findViewById(R.id.my_skills)).setAdapter(selectionAdapter);
			itemsList = view.findViewById(R.id.my_items);
			itemsList.addOnScrollListener(new Utils.ContentScrollListener(itemsAdapter) {
				@Override
				protected void onEndReached(RecyclerView recyclerView) {
					if (selectionAdapter.getSelected() != null && !Utils.isRunning(skillsQuery) && !Utils.isRunning(itemsQuery)) {
						itemsQuery = new GetMyItems(selectionAdapter.getSelected(), true);
						itemsQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				}
			});
			itemsList.setAdapter(itemsAdapter);
			skillsQuery = new GetMyCategories();
			skillsQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		@Override
		public void onDestroyView() {
			Utils.cancelTask(skillsQuery);
			Utils.cancelTask(itemsQuery);
			super.onDestroyView();
		}

		@Override
		public User getUser() {
			return getObject();
		}
	}
	public static class UserFragment2 extends ObjectFragment<User> implements HasUser<User>{
		@Override
		public User getUser() {
			return null;
		}

		@Override
		protected User readObject(InputStream is) throws IOException {
			return null;
		}
	} //todo: complete implements this class

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

	public static void writeUser(OutputStream o, User u) throws IOException {
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
	public final OperationResult<ArrayList<Item>> myItems(Skill skill, int startPosition) {
		ArrayList<Item> res = Utils.DUMMY_ITEMS();
		if (skill != Skill.ALL)
			for (Item item : res)
				item.setSkill(skill);
		for (Item item : res)
			item.publisher = this;
		return new OperationResult<>(res);
	}

	public final OperationResult<ArrayList<Item>> myItems(int startPosition) {
		return myItems(Skill.ALL, startPosition);
	}

	public final OperationResult<ArrayList<Skill>> myCategories() {
		ArrayList<Skill> res = Utils.DUMMY_CATEGORIES();
		res.add(0, Skill.ALL);
		res.add(1, Skill.NONE);
		return new OperationResult<>(res);
	}

	@Override
	public final boolean equals(Object obj) {
		return obj instanceof User && ((User) obj).username.equals(username);
	}

	@Override
	public final boolean expand(Context context) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
			writeUser(os, this);
			Bundle args = new Bundle(1);
			args.putByteArray(ObjectFragment.OBJECT_KEY, os.toByteArray());
			SingleFragmentActivity.start(context, UserFragment.class, args);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
