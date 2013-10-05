package xmu.swordbearer.timebox.view;

import xmu.swordbearer.timebox.R;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopupMenu extends PopupWindow {
	private Context context;
	private ListView lv;

	public PopupMenu(Context context) {
		super(context);
		this.context = context;
		lv = new ListView(context);
	}

	/**
	 * 设置弹出菜单的选项，背景，长宽
	 * 
	 * @param itemsId菜单项数组的ID
	 * @param itemLayoutId一行布局的ID
	 * @param bgDrawableId背景ID
	 * @param width菜单宽度
	 * @param height菜单高度
	 */
	public void setWindow(int[] icons, String[] items, int itemLayoutId, int bgDrawableId, int width, int height) {
		CustomMenuAdapter adapter = new CustomMenuAdapter(context, icons, items);
		lv.setAdapter(adapter);
		setContentView(lv);
		setFocusable(true);
		setOutsideTouchable(true);
		setBackgroundDrawable(context.getResources().getDrawable(bgDrawableId));
		setWidth(200);
		setHeight(height);
		update();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		lv.setOnItemClickListener(listener);
	}

	private class CustomMenuAdapter extends BaseAdapter {
		int[] icons;
		String[] items;
		LayoutInflater inflate;

		private class MenuItemView {
			ImageView iconView;
			TextView textView;
		}

		public CustomMenuAdapter(Context context, int[] icons, String[] items) {
			this.icons = icons;
			this.items = items;
			this.inflate = LayoutInflater.from(context);
		}

		public int getCount() {
			return items.length;
		}

		public Object getItem(int position) {
			return items[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			MenuItemView itemView = new MenuItemView();
			convertView = inflate.inflate(R.layout.list_child_popup_menu, null);
			itemView.iconView = (ImageView) convertView.findViewById(R.id.menu_item_img);
			itemView.textView = (TextView) convertView.findViewById(R.id.menu_item_text);
			if (icons != null) {
				itemView.iconView.setImageResource(icons[position]);
			}
			Log.e("TEST", "Menu Items " + items.length);
			itemView.textView.setText(items[position]);
			convertView.setTag(itemView);
			return convertView;
		}
	}
}
