package xmu.swordbearer.timebox.fragment;

import xmu.swordbearer.timebox.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CategoryFrag extends ListFragment {
	String TAG = "CategoryListFragment";

	public class Category {
		public static final int CATEGORY_TODAY = 0;
		public static final int CATEGORY_TOMORROW = 1;
		public static final int CATEGORY_WEEK = 2;
		public static final int CATEGORY_ALL = 3;
		public static final int CATEGORY_PLAN = 4;
		public static final int CATEGORY_WORD = 5;
		public static final int CATEGORY_NOTE = 6;
		public static final int CATEGORY_STOPTODO = 7;// stop to do
	}

	private int currentCategory = Category.CATEGORY_TODAY;

	public interface OnCategoryClickedListener {
		public void onCategoryClicked(int categoryId);
	}

	private OnCategoryClickedListener onCategoryClickedListener;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.frag_category, container, false);
	}

	private void initListView() {
		String[] categories = getResources().getStringArray(R.array.categories);
		setListAdapter(new CategoryAdapter(getActivity(), categories));
		Log.e("TEST", "分类" + categories.length);

		final ListView lv = getListView();
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setItemsCanFocus(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				if (currentCategory == pos) {
					return;
				}
				// 单击事件
				onCategoryClickedListener.onCategoryClicked(pos);
				currentCategory = pos;
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		initListView();
	}

	@Override
	public void onAttach(Activity activity) {
		try {
			this.onCategoryClickedListener = (OnCategoryClickedListener) activity;
		} catch (ClassCastException e) {
			Log.e(TAG, " OnCagegoryClickedListener 转换失败 !!!");
		}
		super.onAttach(activity);
	}

	private class CategoryAdapter extends BaseAdapter {
		private String[] categories = null;
		LayoutInflater inflater;

		public CategoryAdapter(Context context, String[] categories) {
			inflater = LayoutInflater.from(context);
			this.categories = categories;
		}

		public int getCount() {
			return categories.length;
		}

		public Object getItem(int position) {
			return categories[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflater.inflate(R.layout.list_child_category,
					parent, false);
			// 用于标记
			convertView.setTag(position);
			TextView textView = (TextView) convertView
					.findViewById(R.id.simple_list_textview);
			textView.setText(categories[position]);
			if (position == currentCategory) {
				convertView.setBackgroundResource(R.color.category_bg);
				textView.setTextColor(getResources().getColor(R.color.black));
			}
			return convertView;
		}
	}

}
