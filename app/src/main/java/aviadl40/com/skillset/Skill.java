package aviadl40.com.skillset;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.Item.ItemPreviewAdapter;
import aviadl40.com.skillset.NetUtils.ContentArrayNetTask;
import aviadl40.com.skillset.NetUtils.OperationResult;
import aviadl40.com.skillset.activities.SingleFragmentActivity;
import aviadl40.com.skillset.adapters.ArrayRecyclerViewAdapter;
import aviadl40.com.skillset.adapters.PreviewAdapter;
import aviadl40.com.skillset.fragments.ObjectFragment;

public class Skill implements Expandable {
	public static final class SkillFragment extends ObjectFragment<Skill> {
		private static final class GetItemsTask extends ContentArrayNetTask<Void, Void, Item> {
			@NonNull
			private final Skill skill;
			private final int sortCode;

			GetItemsTask(@NonNull Skill skill, int sortCode, ArrayRecyclerViewAdapter<Item, RecyclerView.ViewHolder> contentAdapter, boolean append) {
				super(contentAdapter, append);
				this.skill = skill;
				this.sortCode = sortCode;
			}

			@Override
			protected OperationResult<ArrayList<Item>> doInBackground(Void... voids) {
				return skill.getItems(sortCode, startPosition);
			}
		}

		private final ItemPreviewAdapter itemsAdapter = new ItemPreviewAdapter();
		ContentArrayNetTask<Void, Void, Skill> getRelatedTask;
		ContentArrayNetTask<Void, Void, Item> itemsQuery;
		private int sortCode = -1;

		@Nullable
		@SuppressLint("InflateParams")
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			return inflater.inflate(R.layout.skill_full, null, false);
		}

		@Override
		protected Skill readObject(InputStream is) throws IOException {
			return readSkill(is);
		}

		@SuppressLint("StaticFieldLeak")
		@Override
		public void populateView(@NonNull View view, @NonNull final Skill skill) {
			// Add name
			((TextView) view.findViewById(R.id.category_name)).setText(skill.name);
			SkillPreviewAdapter skillPreviewAdapter = new SkillPreviewAdapter();
			((RecyclerView) view.findViewById(R.id.category_related)).setAdapter(skillPreviewAdapter);
			getRelatedTask = new ContentArrayNetTask<Void, Void, Skill>(skillPreviewAdapter, false) {
				@Override
				protected OperationResult<ArrayList<Skill>> doInBackground(Void... voids) {
					return skill.getRelated();
				}
			};
			getRelatedTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			RecyclerView itemsList = view.findViewById(R.id.category_items);
			itemsList.addOnScrollListener(new Utils.ContentScrollListener(itemsAdapter) {
				@Override
				protected void onEndReached(RecyclerView recyclerView) {
					if (sortCode >= 0 && !Utils.isRunning(itemsQuery) && !Utils.isRunning(itemsQuery)) {
						itemsQuery = new GetItemsTask(skill, sortCode, itemsAdapter, true);
						itemsQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				}
			});
			itemsList.setAdapter(itemsAdapter);
			final AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					itemsAdapter.clear();
					Utils.cancelTask(itemsQuery);
					if (position >= 0) {
						itemsQuery = new GetItemsTask(skill, sortCode, itemsAdapter, false);
						itemsQuery.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
					sortCode = position;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			};
			Spinner sortSpinner = view.findViewById(R.id.category_sort_spinner);
			sortSpinner.setOnItemSelectedListener(selectionListener);
			selectionListener.onItemSelected(sortSpinner, null, 0, -1);
		}

		@Override
		public void onDestroyView() {
			Utils.cancelTask(getRelatedTask);
			Utils.cancelTask(itemsQuery);
			super.onDestroyView();
		}
	}

	public static class SkillPreviewAdapter extends PreviewAdapter<Skill, RecyclerView.ViewHolder> {
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_skill, parent, false)) {
			};
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, final Skill skill) {
			super.onBindViewHolder(holder, position, skill);
			((TextView) holder.itemView.findViewById(R.id.subjectText)).setText(skill.name);
			((ImageView) holder.itemView.findViewById(R.id.subjectImage)).setImageResource(R.mipmap.ic_launcher);
		}
	}

	public static final class MySkillFragment extends ObjectFragment<Skill> {
		@Override
		protected Skill readObject(InputStream is) throws IOException {
			return readSkill(is);
		}

		@Nullable
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_skill_my,null);
		}
	}

	public static void cataloguedItems(@NonNull Collection<Item> allItems, @NonNull Skill filter, ArrayRecyclerViewAdapter<Item, RecyclerView.ViewHolder> adapter) {
		ArrayList<Item> catalogedItems = new ArrayList<>(allItems);
		for (int i = catalogedItems.size() - 1; i >= 0; i--)
			if (!catalogedItems.get(i).getSkill().equals(filter))
				catalogedItems.remove(i);
		adapter.set(catalogedItems);
	}

	static Skill readSkill(InputStream is) throws IOException {
		return new Skill(StreamUtils.readString(is));
	}

	static void writeSkill(OutputStream o, Skill c) throws IOException {
		StreamUtils.write(o, c.name);
	}

	public static final Skill ALL = new Skill("All");
	static final Skill NONE = new Skill("None");
	public final String name;

	Skill(String name) {
		this.name = name;
	}

	@Override
	public final boolean expand(Context context) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
			writeSkill(os, this);
			Bundle args = new Bundle(1);
			args.putByteArray(ObjectFragment.OBJECT_KEY, os.toByteArray());
			SingleFragmentActivity.start(context, SkillFragment.class, args);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public OperationResult<ArrayList<Item>> getItems(int sortCode, int startPosition) {
		ArrayList<Item> res = Utils.DUMMY_ITEMS();
		for (Item item : res)
			item.setSkill(this);
		return new OperationResult<>(res);
	}

	private OperationResult<ArrayList<Skill>> getRelated() {
		return new OperationResult<>(new ArrayList<>(Utils.DUMMY_CATEGORIES().subList(0, 4)));
	}

	@Override
	public String toString() {
		return name;
	}
}
