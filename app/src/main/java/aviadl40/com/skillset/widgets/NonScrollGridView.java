package aviadl40.com.skillset.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NonScrollGridView extends GridView {
	public NonScrollGridView(Context context) {
		super(context);
	}

	public NonScrollGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonScrollGridView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
		getLayoutParams().height = getMeasuredHeight();

	}
}

