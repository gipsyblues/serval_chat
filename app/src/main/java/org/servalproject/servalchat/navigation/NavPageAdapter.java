package org.servalproject.servalchat.navigation;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import org.servalproject.mid.Identity;
import org.servalproject.mid.Peer;

/**
 * Created by jeremy on 8/06/16.
 */
public class NavPageAdapter extends PagerAdapter
		implements ViewPager.OnPageChangeListener, ILifecycle {

	private final MainActivity activity;
	final Identity identity;
	final Peer peer;
	final HistoryItem[] screens;
	private final ViewState[] views;
	private boolean visible = false;
	private ViewPager pager;

	public NavPageAdapter(MainActivity activity, Identity identity, Peer peer, HistoryItem[] items) {
		this.activity = activity;
		this.identity = identity;
		this.peer = peer;
		this.screens = items;
		this.views = new ViewState[this.screens.length];
	}

	public void setViewPager(ViewPager pager){
		this.pager = pager;
		pager.setAdapter(this);
		pager.addOnPageChangeListener(this);
	}

	@Override
	public int getCount() {
		return screens.length;
	}

	ViewState getViewState(int position) {
		if (views[position] == null)
			views[position] = ViewState.Inflate(activity, screens[position].key, identity, peer, screens[position].args);
		return views[position];
	}

	@Override
	public Object instantiateItem(ViewGroup group, int position) {
		ViewState state = getViewState(position);
		group.addView(state.view);
		ILifecycle lifecycle = state.getLifecycle();
		if (visible && lifecycle != null)
			lifecycle.onVisible();
		return state;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (views[position] == null || views[position] != object)
			return;
		ViewState state = ((ViewState) object);
		ILifecycle lifecycle = state.getLifecycle();
		if (lifecycle != null) {
			if (visible)
				lifecycle.onHidden();
			lifecycle.onDetach(false);
		}
		container.removeView(state.view);
		views[position] = null;
	}

	private void pageChanged(int position) {
		activity.go(screens[position], true);
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
		super.setPrimaryItem(container, position, object);
		pageChanged(position);
		View input = views[position].getTextInput();
		if (input!=null)
			input.requestFocus();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(((ViewState) object).view);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return screens[position].key.getTitle(activity, identity, peer);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

	}

	@Override
	public void onPageSelected(int position) {
		pageChanged(position);
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onDetach(boolean configChange) {
		pager.setAdapter(null);
	}

	@Override
	public void onVisible() {
		visible = true;
		for (ViewState state : views) {
			if (state == null)
				continue;
			ILifecycle lifecycle = state.getLifecycle();
			if (lifecycle != null)
				lifecycle.onVisible();
		}
	}

	@Override
	public void onHidden() {
		visible = false;
		for (ViewState state : views) {
			if (state == null)
				continue;
			ILifecycle lifecycle = state.getLifecycle();
			if (lifecycle != null)
				lifecycle.onHidden();
		}
	}
}
