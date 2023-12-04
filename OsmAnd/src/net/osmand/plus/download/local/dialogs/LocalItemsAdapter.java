package net.osmand.plus.download.local.dialogs;

import static net.osmand.plus.download.local.dialogs.livegroup.LiveGroupItemsFragment.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import net.osmand.plus.R;
import net.osmand.plus.download.local.LocalItem;
import net.osmand.plus.download.local.dialogs.viewholders.HeaderViewHolder;
import net.osmand.plus.download.local.dialogs.viewholders.LiveGroupItemHolder;
import net.osmand.plus.download.local.dialogs.viewholders.LocalItemHolder;
import net.osmand.plus.download.local.dialogs.viewholders.MemoryViewHolder;
import net.osmand.plus.utils.UiUtilities;

import java.util.ArrayList;
import java.util.List;

public class LocalItemsAdapter extends RecyclerView.Adapter<ViewHolder> {

	protected static final int LIST_ITEM_TYPE = 0;
	public static final int LIST_HEADER_TYPE = 1;
	public static final int MEMORY_USAGE_TYPE = 2;
	public static final int LIVE_GROUP_ITEM = 3;

	private final List<Object> items = new ArrayList<>();

	@Nullable
	private final LocalItemListener listener;
	@Nullable
	private final LiveGroupItemListener liveGroupItemListener;

	private final LayoutInflater themedInflater;
	private final boolean nightMode;
	private boolean selectionMode;

	public LocalItemsAdapter(@NonNull Context context, @Nullable LocalItemListener listener, @Nullable LiveGroupItemListener liveGroupItemListener, boolean nightMode) {
		this.listener = listener;
		this.liveGroupItemListener = liveGroupItemListener;
		this.nightMode = nightMode;
		themedInflater = UiUtilities.getInflater(context, nightMode);
	}

	public void setItems(@NonNull List<Object> items) {
		this.items.clear();
		this.items.addAll(items);

		notifyDataSetChanged();
	}

	public void setSelectionMode(boolean selectionMode) {
		this.selectionMode = selectionMode;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		switch (viewType) {
			case LIST_ITEM_TYPE:
				View itemView = themedInflater.inflate(R.layout.local_list_item, parent, false);
				return new LocalItemHolder(itemView, listener, nightMode);
			case MEMORY_USAGE_TYPE:
				itemView = themedInflater.inflate(R.layout.local_memory_card, parent, false);
				return new MemoryViewHolder(itemView, false, nightMode);
			case LIST_HEADER_TYPE:
				itemView = themedInflater.inflate(R.layout.changes_list_header_item, parent, false);
				return new HeaderViewHolder(itemView);
			case LIVE_GROUP_ITEM:
				itemView = themedInflater.inflate(R.layout.local_list_item, parent, false);
				return new LiveGroupItemHolder(itemView, liveGroupItemListener, nightMode);
			default:
				throw new IllegalArgumentException("Unsupported view type");
		}
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		if (holder instanceof MemoryViewHolder) {
			MemoryInfo memoryInfo = (MemoryInfo) items.get(position);

			boolean lastItem = position == getItemCount() - 1;
			boolean hideDivider = !lastItem && items.get(position + 1) instanceof HeaderGroup;

			MemoryViewHolder viewHolder = (MemoryViewHolder) holder;
			viewHolder.bindView(memoryInfo, !hideDivider);
		} else if (holder instanceof LocalItemHolder) {
			LocalItem item = (LocalItem) items.get(position);
			boolean lastItem = position == getItemCount() - 1;
			boolean hideDivider = !lastItem && items.get(position + 1) instanceof HeaderGroup;

			LocalItemHolder viewHolder = (LocalItemHolder) holder;
			viewHolder.bindView(item, selectionMode, lastItem, hideDivider);
		} else if (holder instanceof HeaderViewHolder) {
			HeaderGroup headerGroup = (HeaderGroup) items.get(position);

			HeaderViewHolder viewHolder = (HeaderViewHolder) holder;
			viewHolder.bindView(headerGroup);
		} else if (holder instanceof LiveGroupItemHolder) {
			LiveGroupItem item = (LiveGroupItem) items.get(position);
			boolean lastItem = position == getItemCount() - 1;
			boolean hideDivider = !lastItem && items.get(position + 1) instanceof HeaderGroup;

			LiveGroupItemHolder viewHolder = (LiveGroupItemHolder) holder;
			viewHolder.bindView(item, lastItem, hideDivider);
		}
	}

	@Override
	public int getItemViewType(int position) {
		Object object = items.get(position);
		if (object instanceof LocalItem) {
			return LIST_ITEM_TYPE;
		} else if (object instanceof HeaderGroup) {
			return LIST_HEADER_TYPE;
		} else if (object instanceof MemoryInfo) {
			return MEMORY_USAGE_TYPE;
		} else if (object instanceof LiveGroupItem) {
			return LIVE_GROUP_ITEM;
		}
		throw new IllegalArgumentException("Unsupported view type");
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public int getItemPosition(@NonNull Object object) {
		return items.indexOf(object);
	}

	public void updateItem(@NonNull Object object) {
		int index = getItemPosition(object);
		if (index != -1) {
			notifyItemChanged(index);
		}
	}

	public interface LocalItemListener {

		default boolean isItemSelected(@NonNull LocalItem item) {
			return false;
		}

		default boolean itemUpdateAvailable(@NonNull LocalItem item) {
			return false;
		}

		default void onItemSelected(@NonNull LocalItem item) {

		}

		default void onItemOptionsSelected(@NonNull LocalItem item, @NonNull View view) {

		}
	}

	public interface LiveGroupItemListener {
		default void onItemSelected(@NonNull LiveGroupItem item) {}

		default void onItemOptionsSelected(@NonNull LiveGroupItem item, @NonNull View view) {}
	}
}
