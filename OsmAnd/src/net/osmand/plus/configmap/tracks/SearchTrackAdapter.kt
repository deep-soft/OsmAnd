package net.osmand.plus.configmap.tracks

import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import net.osmand.CallbackWithObject
import net.osmand.plus.OsmandApplication
import net.osmand.plus.R
import net.osmand.plus.configmap.tracks.viewholders.EmptySearchResultViewHolder
import net.osmand.plus.configmap.tracks.viewholders.EmptyTracksViewHolder
import net.osmand.plus.configmap.tracks.viewholders.EmptyTracksViewHolder.EmptyTracksListener
import net.osmand.plus.configmap.tracks.viewholders.NoVisibleTracksViewHolder
import net.osmand.plus.configmap.tracks.viewholders.RecentlyVisibleViewHolder
import net.osmand.plus.configmap.tracks.viewholders.SortTracksViewHolder
import net.osmand.plus.configmap.tracks.viewholders.SortTracksViewHolder.SortTracksListener
import net.osmand.plus.configmap.tracks.viewholders.TrackViewHolder
import net.osmand.plus.configmap.tracks.viewholders.TrackViewHolder.TrackSelectionListener
import net.osmand.plus.myplaces.tracks.TracksSearchFilter
import net.osmand.plus.myplaces.tracks.filters.BaseTrackFilter
import net.osmand.plus.settings.enums.TracksSortMode
import net.osmand.plus.track.data.TrackFolder
import net.osmand.plus.utils.ColorUtilities
import net.osmand.plus.utils.UiUtilities
import net.osmand.plus.utils.UpdateLocationUtils
import net.osmand.plus.utils.UpdateLocationUtils.UpdateLocationViewCache
import net.osmand.util.Algorithms
import java.util.Collections

class SearchTracksAdapter(
    private val app: OsmandApplication,
    private var trackItems: List<TrackItem>,
    private val nightMode: Boolean,
    private var selectionMode: Boolean,
    private var filter: TracksSearchFilter
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private val locationViewCache: UpdateLocationViewCache
    private var items: MutableList<Any> = mutableListOf()
    private var filteredItems: List<TrackItem> = mutableListOf()
    private var sortMode: TracksSortMode = TracksSortMode.getDefaultSortMode()

    private var sortTracksListener: SortTracksListener? = null
    private var selectionListener: TrackSelectionListener? = null
    private var emptyTracksListener: EmptyTracksListener? = null

    init {
        if (filter.filteredTrackItems != null) {
            updateFilteredItems(filter.filteredTrackItems!!)
        } else {
            updateFilteredItems(trackItems)
        }
        locationViewCache = UpdateLocationUtils.getUpdateLocationViewCache(app)
        locationViewCache.arrowResId = R.drawable.ic_direction_arrow
        locationViewCache.arrowColor = ColorUtilities.getActiveIconColorId(nightMode)
    }

    constructor(
        app: OsmandApplication,
        trackItems: List<TrackItem>,
        nightMode: Boolean,
        selectionMode: Boolean
    ) : this(app, trackItems, nightMode, selectionMode, TracksSearchFilter(app, trackItems))

    constructor(
        app: OsmandApplication,
        trackItems: List<TrackItem>,
        nightMode: Boolean,
        selectionMode: Boolean,
        currentFolder: TrackFolder?
    ) : this(app, trackItems, nightMode, selectionMode, TracksSearchFilter(app, trackItems, currentFolder))

    fun getFilteredItems(): Set<TrackItem> {
        return HashSet(filteredItems)
    }

    fun setTracksSortMode(sortMode: TracksSortMode) {
        this.sortMode = sortMode
        sortItems()
        notifyDataSetChanged()
    }

    fun setSelectionMode(selectionMode: Boolean) {
        this.selectionMode = selectionMode
    }

    private fun sortItems() {
        val latLon = app.mapViewTrackingUtilities.defaultLocation
        Collections.sort(items, TracksComparator(sortMode, latLon))
    }

    fun setSortTracksListener(sortTracksListener: SortTracksListener?) {
        this.sortTracksListener = sortTracksListener
    }

    fun setSelectionListener(selectionListener: TrackSelectionListener?) {
        this.selectionListener = selectionListener
    }

    fun setImportTracksListener(emptyTracksListener: EmptyTracksListener?) {
        this.emptyTracksListener = emptyTracksListener
    }

    fun setFilterCallback(filterCallback: CallbackWithObject<List<TrackItem>>) {
        filter.setCallback(filterCallback)
    }

    fun updateAllItems(allItems: List<TrackItem>) {
        trackItems = allItems
        filter.setAllItems(allItems)
    }

    fun updateFilteredItems(filteredItems: List<TrackItem>) {
        this.filteredItems = filteredItems

        items = ArrayList<Any>()
        items.add(TracksAdapter.TYPE_SORT_TRACKS)

        if (Algorithms.isEmpty(trackItems)) {
            items.add(TracksAdapter.TYPE_NO_TRACKS)
        } else if (Algorithms.isEmpty(filteredItems)) {
            items.add(TYPE_NO_FOUND_TRACKS)
        } else {
            items.addAll(filteredItems)
        }
        sortItems()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = UiUtilities.getInflater(parent.context, nightMode)
        return when (viewType) {
            TracksAdapter.TYPE_TRACK -> {
                val view = inflater.inflate(R.layout.track_list_item, parent, false)
                TrackViewHolder(view, selectionListener, locationViewCache, nightMode)
            }
            TracksAdapter.TYPE_NO_TRACKS -> {
                val view = inflater.inflate(R.layout.track_folder_empty_state, parent, false)
                EmptyTracksViewHolder(view, emptyTracksListener)
            }
            TYPE_NO_FOUND_TRACKS -> {
                val view = inflater.inflate(R.layout.empty_search_results, parent, false)
                EmptySearchResultViewHolder(view)
            }
            TracksAdapter.TYPE_SORT_TRACKS -> {
                val view = inflater.inflate(R.layout.sort_type_view, parent, false)
                SortTracksViewHolder(view, sortTracksListener, nightMode)
            }
            else -> throw IllegalArgumentException("Unsupported view type $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val any = items[position]
        if (any is TrackItem) {
            return TracksAdapter.TYPE_TRACK
        } else if (any is Int) {
            if (TracksAdapter.TYPE_NO_TRACKS == any) {
                return TracksAdapter.TYPE_NO_TRACKS
            } else if (TracksAdapter.TYPE_SORT_TRACKS == any) {
                return TracksAdapter.TYPE_SORT_TRACKS
            } else if (TYPE_NO_FOUND_TRACKS == any) {
                return TYPE_NO_FOUND_TRACKS
            }
        }
        throw IllegalArgumentException("Unsupported view type")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TrackViewHolder) {
            val item = items[position] as TrackItem
            val showDivider = position != itemCount - 1
            holder.bindView(sortMode, item, showDivider, true, selectionMode)
        } else if (holder is NoVisibleTracksViewHolder) {
            holder.bindView()
        } else if (holder is EmptyTracksViewHolder) {
            holder.bindView()
        } else if (holder is RecentlyVisibleViewHolder) {
            holder.bindView()
        } else if (holder is SortTracksViewHolder) {
            val enabled = !Algorithms.isEmpty(trackItems)
            holder.bindView(enabled, filter)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getFilter(): Filter {
        return filter
    }

	fun filter(constraint: CharSequence?) {
		var query = constraint
		if (query == null) {
			query = "";
		}
        if (!query.toString().equals(filter.nameFilter.getValue())) {
            filter.nameFilter.setValue(query.toString())
            filter.filter(query)
        }
	}

	fun updateItem(item: Any) {
		val index = items.indexOf(item)
		if (index != -1) {
			notifyItemChanged(index)
		}
	}

    fun onItemsSelected(items: Set<Any>) {
        for (item in items) {
            updateItem(item)
        }
    }

	fun getCurrentSearchQuery(): String {
		return filter.nameFilter.getValue() as String
	}

    fun initSelectedFilters(selectedFilters: List<BaseTrackFilter>?) {
        filter.initSelectedFilters(selectedFilters)
    }

	companion object {
		const val TYPE_NO_FOUND_TRACKS = 5
	}
}