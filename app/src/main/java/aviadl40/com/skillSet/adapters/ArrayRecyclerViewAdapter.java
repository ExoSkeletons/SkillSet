package aviadl40.com.skillSet.adapters;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class ArrayRecyclerViewAdapter<E, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
	private final ArrayList<E> items = new ArrayList<>();

	public ArrayRecyclerViewAdapter() {
	}

	public ArrayRecyclerViewAdapter(Collection<E> items) {
		set(items);
	}

	public void add(Collection<E> collection) {
		int positionStart = getItemCount();
		items.addAll(collection);
		notifyItemRangeInserted(getItemCount(), collection.size());
	}

	public void clear() {
		items.clear();
		notifyDataSetChanged();
	}

	public void set(Collection<E> collection) {
		clear();
		add(collection);
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
	public final void onBindViewHolder(@NonNull VH holder, int position) {
		onBindViewHolder(holder, position, items.get(position));
	}

	@Override
	public final int getItemCount() {
		return items.size();
	}

	protected abstract void onBindViewHolder(VH holder, int position, E item);
}
