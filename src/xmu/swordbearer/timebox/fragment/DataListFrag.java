package xmu.swordbearer.timebox.fragment;

import java.util.Calendar;

import xmu.swordbearer.timebox.R;
import xmu.swordbearer.timebox.activity.NewNoteActivity;
import xmu.swordbearer.timebox.activity.NewStopToDoActivity;
import xmu.swordbearer.timebox.activity.NewWordActivity;
import xmu.swordbearer.timebox.activity.PlanDetailActivity;
import xmu.swordbearer.timebox.activity.TaskDetailActivity;
import xmu.swordbearer.timebox.data.BaseCursorAdapter;
import xmu.swordbearer.timebox.data.CommonVar;
import xmu.swordbearer.timebox.data.DataHandler;
import xmu.swordbearer.timebox.data.Items.NoteColumns;
import xmu.swordbearer.timebox.data.Items.StoptodoColumns;
import xmu.swordbearer.timebox.data.NoteListAdapter;
import xmu.swordbearer.timebox.data.Plan.PlanColumns;
import xmu.swordbearer.timebox.data.PlanListAdapter;
import xmu.swordbearer.timebox.data.StoptodoListAdapter;
import xmu.swordbearer.timebox.data.Task;
import xmu.swordbearer.timebox.data.TaskListAdapter;
import xmu.swordbearer.timebox.data.Word.WordColumns;
import xmu.swordbearer.timebox.data.WordListAdapter;
import xmu.swordbearer.timebox.fragment.CategoryFrag.Category;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class DataListFrag extends Fragment {
	String TAG = "SimpleListFragment";

	public static final String CATEGORY = "cur_category";

	private TextView emptyTextView;
	private ListView listView;
	protected BaseCursorAdapter listAdapter;
	Cursor cursor = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View contentView = inflater.inflate(R.layout.frag_simplelist,
				container, false);
		emptyTextView = (TextView) contentView.findViewById(R.id.simple_empty);
		listView = (ListView) contentView.findViewById(R.id.simple_listview);
		updateCategory(Category.CATEGORY_TODAY);
		return contentView;
	}

	public void updateCategory(final int category) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Context context = getActivity();
		switch (category) {
		case Category.CATEGORY_TODAY:
			cursor = DataHandler.getTaskByDate(context, calendar);
			listAdapter = new TaskListAdapter(context, cursor);
			break;
		case Category.CATEGORY_TOMORROW:
			calendar.add(Calendar.DAY_OF_WEEK, 1);
			cursor = DataHandler.getTaskByDate(context, calendar);
			listAdapter = new TaskListAdapter(context, cursor);
			break;
		case Category.CATEGORY_WEEK:
			// cursor = DataHandler.getTaskOfWeek(getActivity(),
			// Calendar.getInstance());
			// listAdapter = new WeekListAdapter(getActivity(), cursor);
			break;
		case Category.CATEGORY_ALL:
			cursor = DataHandler.getTaskByStatus(context, Task.TASK_STATUS_ALL);
			listAdapter = new TaskListAdapter(context, cursor);
			break;
		case Category.CATEGORY_PLAN:
			cursor = DataHandler.getAllPlan(context, 2);
			listAdapter = new PlanListAdapter(context, cursor);
			break;
		case Category.CATEGORY_WORD:
			cursor = DataHandler.getAllWord(context);
			listAdapter = new WordListAdapter(context, cursor);
			break;
		case Category.CATEGORY_NOTE:// note
			cursor = DataHandler.getAllNote(context);
			listAdapter = new NoteListAdapter(context, cursor);
			break;
		case Category.CATEGORY_STOPTODO:// stop to do
			cursor = DataHandler.getAllStoptodo(context);
			listAdapter = new StoptodoListAdapter(context, cursor);
			break;
		default:
			break;
		}
		if (listAdapter.getCount() > 0) {
			emptyTextView.setVisibility(View.GONE);
		} else {
			emptyTextView.setVisibility(View.VISIBLE);
		}
		if (category == Category.CATEGORY_WORD) {
			listView.setDivider(null);
		} else {
			listView.setDivider(getResources().getDrawable(
					R.drawable.list_divider));
		}
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.e(TAG, "setOnItemClickListener");
				goToDetails(category, position);
			}
		});
	}

	private void goToDetails(int category, int position) {
		Cursor cursor = listAdapter.getCursor();
		if (!cursor.moveToPosition(position)) {
			Log.e(TAG, "goToDetails ERROR!，单击无效");
			return;
		}
		int id = cursor.getInt(0);
		switch (category) {
		case Category.CATEGORY_TODAY:// today
		case Category.CATEGORY_TOMORROW:// tomorrow
		case Category.CATEGORY_WEEK:// week
		case Category.CATEGORY_ALL:// all
			goToTaskDetail(category, position);
			break;
		case Category.CATEGORY_PLAN:// plan
			goToPlanDetail(id);
			break;
		case Category.CATEGORY_WORD:
			goToWordDetail(cursor);
			break;
		case Category.CATEGORY_NOTE:// note
			goToNoteDetail(cursor);
			break;
		case Category.CATEGORY_STOPTODO:// stoptodo
			goToStoptodoDetail(cursor);
			break;
		default:
			break;
		}
	}

	private void goToTaskDetail(int category, int position) {
		Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
		intent.putExtra(CATEGORY, category);
		intent.putExtra(TaskDetailActivity.CUR_TASK_POS, position);
		startActivity(intent);
	}

	private void goToPlanDetail(int planId) {
		Intent intent = new Intent(getActivity(), PlanDetailActivity.class);
		intent.putExtra(PlanColumns._ID, planId);
		startActivity(intent);
	}

	private void goToWordDetail(Cursor cursor) {
		Intent intent = new Intent(getActivity(), NewWordActivity.class);
		Bundle extra = new Bundle();
		extra.putInt(WordColumns._ID, cursor.getInt(0));
		extra.putString(WordColumns.WORD_CONTENT, cursor.getString(2));
		extra.putInt(WordColumns.WORD_COLOR, cursor.getInt(3));
		intent.putExtra(CommonVar.WORD_BUNDLE, extra);
		startActivity(intent);
	}

	private void goToNoteDetail(Cursor cursor) {
		Intent intent = new Intent(getActivity(), NewNoteActivity.class);
		Bundle extra = new Bundle();
		extra.putInt(NoteColumns._ID, cursor.getInt(0));
		extra.putString(NoteColumns.NOTE_DATE, cursor.getString(1));
		extra.putString(NoteColumns.NOTE_COLOR, cursor.getString(2));
		extra.putInt(NoteColumns.NOTE_COLOR, cursor.getInt(3));
		intent.putExtra(CommonVar.NOTE_BUNDLE, extra);
		startActivity(intent);
	}

	private void goToStoptodoDetail(Cursor cursor) {
		Intent intent = new Intent(getActivity(), NewStopToDoActivity.class);
		Bundle extra = new Bundle();
		extra.putInt(StoptodoColumns._ID, cursor.getInt(0));
		extra.putString(StoptodoColumns.STOPTODO_DATE, cursor.getString(1));
		extra.putString(StoptodoColumns.STOPTODO_DETAIL, cursor.getString(2));
		intent.putExtra(CommonVar.STOP_BUNDLE, extra);
		startActivity(intent);
	}

	// // listView的拖动事件：向左：刪除；向右：编辑
	// public class ListItemSlideListener implements View.OnTouchListener {
	// float x1, x2, y1, y2;
	//
	// public boolean onTouch(View v, MotionEvent event) {
	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
	// x1 = event.getX();
	// y1 = event.getY();
	// }
	// if (event.getAction() == MotionEvent.ACTION_UP) {
	// x2 = event.getX();
	// y2 = event.getY();
	// int pos1 = listView.pointToPosition((int) x1, (int) y1);
	// int pos2 = listView.pointToPosition((int) x2, (int) y2);
	//
	// if (pos1 == pos2) {
	// if (Math.abs(x1 - x2) < 6) {
	// return false;// 距离较小，当作click事件来处理
	// }
	// if ((x1 - x2) > 80) {
	// Log.e(TAG, "向左移动 " + pos1);
	// removeListItem(pos1);
	// }
	// if ((x2 - x1) > 80) {
	// Log.e(TAG, "向右移动 " + pos1);
	// editListItem(pos1);
	// }
	// }
	// }
	// return true;// 返回true，不执行click事件
	// }
	//
	// private void editListItem(final int position) {
	// View rowView = listView.getChildAt(position);
	// final Animation animation = (Animation) AnimationUtils
	// .loadAnimation(rowView.getContext(), R.anim.trans_right);
	// animation.setAnimationListener(new AnimationListener() {
	// public void onAnimationStart(Animation animation) {
	// }
	//
	// public void onAnimationRepeat(Animation animation) {
	// }
	//
	// public void onAnimationEnd(Animation animation) {
	// // goToDetails(position);
	// }
	// });
	// rowView.startAnimation(animation);
	// }
	//
	// private void removeListItem(final int position) {
	// View rowView = listView.getChildAt(position);
	// final Animation animation = (Animation) AnimationUtils
	// .loadAnimation(rowView.getContext(), R.anim.trans_left);
	// animation.setAnimationListener(new AnimationListener() {
	// public void onAnimationStart(Animation animation) {
	// }
	//
	// public void onAnimationRepeat(Animation animation) {
	// }
	//
	// public void onAnimationEnd(Animation animation) {
	// // do delete
	// listAdapter.notifyDataSetChanged();
	// Log.e(TAG, "删除后，通知");
	// }
	// });
	// rowView.startAnimation(animation);
	// }
	// }

}
