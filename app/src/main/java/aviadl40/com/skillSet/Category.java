package aviadl40.com.skillSet;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillSet.NetUtils.OperationResult;
import aviadl40.com.skillSet.adapters.ArrayRecyclerViewAdapter;
import aviadl40.com.skillSet.fragments.ObjectFragment;

public class Category {
	public static final class CategoryFragment extends ObjectFragment<Category> {
		@Nullable
		@SuppressLint("InflateParams")
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			return inflater.inflate(R.layout.category_full, null, false);
		}

		@Override
		protected Category readObject(InputStream is) throws IOException {
			return readCategory(is);
		}

		@Override
		public void onViewCreated(View view, @Nullable Category category, @Nullable Bundle savedInstanceState) {
			if (category != null) {
				// Add name
				((TextView) view.findViewById(R.id.category_name)).setText(category.name);
			}
		}
	}

	public static class PreviewAdapter extends ArrayRecyclerViewAdapter<Category, RecyclerView.ViewHolder> {
		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_category, parent, false)) {
			};
		}

		@Override
		public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position, final Category item) {
			((TextView) holder.itemView.findViewById(R.id.subjectText)).setText(item.name);
			((ImageView) holder.itemView.findViewById(R.id.subjectImage)).setImageResource(R.mipmap.ic_launcher);
		}
	}

	public static void cataloguedItems(@NonNull Collection<Item> allItems, @NonNull Category filter, ArrayRecyclerViewAdapter<Item, RecyclerView.ViewHolder> adapter) {
		ArrayList<Item> catalogedItems = new ArrayList<>(allItems);
		for (int i = catalogedItems.size() - 1; i >= 0; i--)
			if (!catalogedItems.get(i).getCategory().equals(filter))
				catalogedItems.remove(i);
		adapter.set(catalogedItems);
	}

	static Category readCategory(InputStream is) throws IOException {
		return new Category(StreamUtils.readString(is));
	}

	static void writeCategory(Category c, OutputStream o) throws IOException {
		StreamUtils.write(o, c.name);
	}

	public static final Category ALL = new Category("All");
	static final Category NONE = new Category("None");
	private static final String KEY = "category key";

	public final String name;

	Category(String name) {
		this.name = name;
	}

	public OperationResult<ArrayList<Item>> getItems(int sortCode, int startPosition) {
		return new OperationResult<>(MainActivity.DUMMY_ITEMS());
	}

	@Override
	public String toString() {
		return name;
	}
}
