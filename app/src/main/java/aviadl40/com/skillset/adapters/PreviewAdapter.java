package aviadl40.com.skillset.adapters;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.Expandable;

public abstract class PreviewAdapter<E extends Expandable, VH extends RecyclerView.ViewHolder> extends ArrayRecyclerViewAdapter<E, VH> {
	@Override
	protected void onBindViewHolder(VH holder, int position, final E item) {
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				item.expand(v.getContext());
			}
		});
	}
}
