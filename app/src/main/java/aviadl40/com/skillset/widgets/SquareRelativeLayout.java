package aviadl40.com.skillset.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquareRelativeLayout extends RelativeLayout {
	public SquareRelativeLayout(Context context) {
		super(context);
	}

	public SquareRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@SuppressWarnings("SuspiciousNameCombination")
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Set a square layout.
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}