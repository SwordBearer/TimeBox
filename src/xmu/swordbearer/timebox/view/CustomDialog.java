package xmu.swordbearer.timebox.view;

import xmu.swordbearer.timebox.R;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class CustomDialog extends PopupWindow {
	private Button btnOk;
	private Button btnCancel;
	private Button btnNeutral;
	private View contentView;
	private TextView tvTitle;
	private Context context;

	private LayoutInflater inflater;

	public CustomDialog(Context context) {
		super(context);
		inflater = LayoutInflater.from(context);
		this.context = context;
		contentView = inflater
				.inflate(R.layout.view_custom_dialog, null, false);
		btnOk = (Button) contentView.findViewById(R.id.customdialog_btn_ok);
		btnNeutral = (Button) contentView
				.findViewById(R.id.customdialog_btn_neutral);
		btnCancel = (Button) contentView
				.findViewById(R.id.customdialog_btn_cancel);
		tvTitle = (TextView) contentView.findViewById(R.id.customdialog_title);
	}

	public void setTitle(int stringId) {
		tvTitle.setText(stringId);
	}

	public void setContentView(View view) {
		LinearLayout container = (LinearLayout) contentView
				.findViewById(R.id.customdialog_container);
		container.addView(view, LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
	}

	public void setPositiveButton(int stringId,
			View.OnClickListener clickListener) {
		btnOk.setText(stringId);
		btnOk.setOnClickListener(clickListener);
	}

	public void setNeutralButton(int stringId,
			View.OnClickListener clickListener) {
		btnNeutral.setVisibility(View.VISIBLE);
		btnNeutral.setText(stringId);
		btnNeutral.setOnClickListener(clickListener);
	}

	public void setNegativeButton(int stringId,
			View.OnClickListener clickListener) {
		btnCancel.setText(stringId);
		btnCancel.setOnClickListener(clickListener);
		dismiss();
	}

	public void show(View view) {
		super.setContentView(contentView);
		this.setWidth(LayoutParams.WRAP_CONTENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setFocusable(true);
		this.setOutsideTouchable(false);
		this.setBackgroundDrawable(context.getResources().getDrawable(
				R.drawable.bg_dialog));
		this.showAtLocation(view, Gravity.CENTER, 0, 0);
	}
}
