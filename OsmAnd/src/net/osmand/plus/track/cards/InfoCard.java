package net.osmand.plus.track.cards;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.track.fragments.controller.SelectRouteActivityController;
import net.osmand.plus.track.helpers.RouteActivityHelper;
import net.osmand.plus.utils.AndroidUtils;
import net.osmand.shared.gpx.GpxUtilities;
import net.osmand.shared.gpx.primitives.Metadata;
import net.osmand.shared.gpx.primitives.RouteActivity;
import net.osmand.util.Algorithms;

import java.util.List;

public class InfoCard extends BaseMetadataCard {

	private final RouteActivityHelper routeActivityHelper;

	public InfoCard(@NonNull MapActivity mapActivity, @NonNull Metadata metadata,
	                @NonNull RouteActivityHelper routeActivityHelper) {
		super(mapActivity, metadata);
		this.routeActivityHelper = routeActivityHelper;
	}

	@Override
	@StringRes
	protected int getTitleId() {
		return R.string.info_button;
	}

	@Override
	public void updateContent() {
		super.updateContent();

		List<RouteActivity> activities = routeActivityHelper.getActivities();
		RouteActivity routeActivity = routeActivityHelper.getSelectedActivity();
		String keywords = metadata != null ? GpxUtilities.INSTANCE.getFilteredKeywords(metadata, activities) : null;
		String link = metadata != null ? metadata.getLink() : null;

		String label = routeActivity != null
				? routeActivity.getLabel()
				: app.getString(R.string.shared_string_none);

		Drawable icon = getContentIcon(routeActivity != null
				? AndroidUtils.getIconId(app, routeActivity.getIconName())
				: R.drawable.ic_action_activity);

		createItemRow(getString(R.string.shared_string_activity), label, icon).setOnClickListener(
				v -> SelectRouteActivityController.showDialog(activity, routeActivityHelper)
		);
		if (!Algorithms.isEmpty(keywords)) {
			createItemRow(getString(R.string.shared_string_keywords), keywords, getContentIcon(R.drawable.ic_action_label));
		}
		if (!Algorithms.isEmpty(link)) {
			createLinkItemRow(getString(R.string.shared_string_link), link, R.drawable.ic_action_link);
		}
	}
}