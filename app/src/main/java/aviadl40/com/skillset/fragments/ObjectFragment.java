package aviadl40.com.skillset.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import aviadl40.com.skillset.Utils;

@SuppressLint("StaticFieldLeak")
public abstract class ObjectFragment<Object> extends Fragment {
	public static final String OBJECT_KEY = "object fragment key";
	@Nullable
	Object object;
	private AsyncTask<Void, Void, Object> populateTask;

	@Nullable
	protected final Object getObject() {
		return object;
	}

	protected abstract Object readObject(InputStream is) throws IOException;

	protected void populateView(@NonNull View view, @NonNull Object object) {
	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		Utils.cancelTask(populateTask);
		Bundle args = getArguments();
		final byte[] bytes;
		if (args != null && (bytes = args.getByteArray(OBJECT_KEY)) != null) {
			if (object == null) {
				populateTask = new AsyncTask<Void, Void, Object>() {
					@Override
					protected Object doInBackground(Void... voids) {
						try {
							return readObject(new ByteArrayInputStream(bytes));
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}

					@Override
					protected void onPostExecute(Object object) {
						ObjectFragment.this.object = object;
						if (object != null) populateView(view, object);
					}
				};
				populateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else
				populateView(view, object);
		}
	}

	@Override
	public void onDestroyView() {
		Utils.cancelTask(populateTask);
		super.onDestroyView();
	}
}
