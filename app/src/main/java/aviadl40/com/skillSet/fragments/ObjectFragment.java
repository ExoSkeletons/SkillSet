package aviadl40.com.skillSet.fragments;

import android.os.Bundle;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class ObjectFragment<Object> extends Fragment {
	public static final String OBJECT_KEY = "object fragment key";

	@Nullable
	private Object object = null;

	protected abstract Object readObject(InputStream is) throws Exception;

	@Override
	public void setArguments(Bundle args) {
		byte[] bs = args.getByteArray(OBJECT_KEY);
		if (bs != null) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(bs);
				object = readObject(bais);
			} catch (Exception e) {
				e.printStackTrace();
				onDestroy();
			}
		}
		super.setArguments(args);
	}

	@Override
	public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		onViewCreated(view, object, savedInstanceState);
	}

	protected void onViewCreated(View view, @Nullable Object object, Bundle savedInstanceState) {
	}
}
