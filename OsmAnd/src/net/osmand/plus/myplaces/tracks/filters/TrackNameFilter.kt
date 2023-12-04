package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.annotations.Expose
import net.osmand.CollatorStringMatcher
import net.osmand.plus.R
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.myplaces.tracks.filters.FilterType.NAME
import net.osmand.search.core.SearchPhrase
import net.osmand.util.Algorithms

class TrackNameFilter(filterType: FilterType, filterChangedListener: FilterChangedListener?)
	: BaseTrackFilter(filterType, filterChangedListener) {

	@Expose
	var value = ""
		set(value) {
			if (!Algorithms.stringsEqual(field, value)) {
				field = value
				updateMatcher()
			}
			filterChangedListener?.onFilterChanged()
		}

	private var nameMatcher = createMatcher()

	private fun updateMatcher() {
		nameMatcher = createMatcher()
	}

	private fun createMatcher(): SearchPhrase.NameStringMatcher {
		return SearchPhrase.NameStringMatcher(
			value.trim { it <= ' ' },
			CollatorStringMatcher.StringMatcherMode.CHECK_CONTAINS)
	}

	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
		if (nameMatcher == null) {
			updateMatcher()
		}
		return nameMatcher.matches(trackItem.name)
	}

	override fun isEnabled(): Boolean {
		return !Algorithms.isEmpty(value)
	}

	override fun initWithValue(sourseFilter: BaseTrackFilter) {
		if (sourseFilter is TrackNameFilter) {
			this.value = sourseFilter.value
			updateMatcher()
			super.initWithValue(sourseFilter)
		}
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other) &&
				other is TrackNameFilter &&
				Algorithms.stringsEqual(other.value, value)
	}
}