package aviadl40.com.skillset.adapters;

import java.util.ArrayList;
import java.util.Collection;

import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class ArrayRecyclerViewAdapter<E, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	private final ArrayList<E> items = new ArrayList<>();

	public ArrayRecyclerViewAdapter() {
	}

	public ArrayRecyclerViewAdapter(Collection<E> items) {
		set(items);
	}

	public ArrayRecyclerViewAdapter<E, VH> add(Collection<E> collection) {
		int positionStart = getItemCount();
		items.addAll(collection);
		notifyItemRangeInserted(getItemCount(), collection.size());
		return this;
	}

	public ArrayRecyclerViewAdapter<E, VH> clear() {
		items.clear();
		notifyDataSetChanged();
		return this;
	}

	public ArrayRecyclerViewAdapter<E, VH> set(Collection<E> collection) {
		clear();
		add(collection);
		return this;
	}

	public E get(int position) {
		return items.get(position);
	}

	public int positionOf(E item) {
		return items.indexOf(item);
	}

	public int lastPositionOf(E item) {
		return items.lastIndexOf(item);
	}

	@Override
	public final void onBindViewHolder(VH holder, int position) {
		onBindViewHolder(holder, position, items.get(position));
	}

	@Override
	public final int getItemCount() {
		return items.size();
	}

	protected abstract void onBindViewHolder(VH holder, int position, E item);
}
