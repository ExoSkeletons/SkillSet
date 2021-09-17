package aviadl40.com.skillset;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.adapters.ArrayRecyclerViewAdapter;

public final class NetUtils {
	public enum OperationError {
		CANNOT_CONNECT,
		TIMEOUT,
		NO_AUTH,
		NO_INTERNET;

		public Toast makeToast(Context context) {
			return Toast.makeText(context, toString(), Toast.LENGTH_SHORT);
		}
	}

	public static final class OperationResult<Result> {
		public final Result result;
		public final OperationError error;

		private OperationResult(Result result, OperationError error) {
			this.error = error;
			this.result = result;
		}

		OperationResult(IOException e) {
			this(
					e instanceof SocketTimeoutException ? OperationError.TIMEOUT
							: e instanceof UnknownHostException || e instanceof NoRouteToHostException ? OperationError.NO_INTERNET
							: OperationError.CANNOT_CONNECT
			);
		}

		public OperationResult(OperationError error) {
			this(null, error);
		}

		public OperationResult(Result result) {
			this(result, null);
		}

		public boolean resultOK() {
			return error == null;
		}
	}

	public static abstract class NetTask<Params, Progress, Result> extends AsyncTask<Params, Progress, OperationResult<Result>> {
		protected void onResult(Result result) {
		}

		protected void onError(OperationError error) {
		}

		@Override
		protected final void onPostExecute(OperationResult<Result> operationResult) {
			onPostExecute();
			if (operationResult.resultOK())
				onResult(operationResult.result);
			else
				onError(operationResult.error);
		}

		protected void onPostExecute() {
		}
	}

	public static abstract class ArrayNetTask<Params, Progress, E> extends NetTask<Params, Progress, ArrayList<E>> {
		protected final int startPosition;

		protected ArrayNetTask(int startPosition) {
			this.startPosition = startPosition;
		}
	}

	public static abstract class ContentArrayNetTask<Params, Progress, Content> extends ArrayNetTask<Params, Progress, Content> {
		protected final ArrayRecyclerViewAdapter<Content, RecyclerView.ViewHolder> contentAdapter;
		protected final boolean append;

		protected ContentArrayNetTask(ArrayRecyclerViewAdapter<Content, RecyclerView.ViewHolder> contentAdapter, boolean append) {
			super(append ? contentAdapter.getItemCount() : 0);
			this.contentAdapter = contentAdapter;
			this.append = append;
		}

		@Override
		protected void onResult(ArrayList<Content> result) {
			if (!append)
				contentAdapter.clear();
			contentAdapter.add(result);
		}
	}

	private static OperationResult<InputStream> getInputStream(String url) {
		try {
			final URLConnection connection = new URL(url).openConnection();
			connection.setConnectTimeout(7500);
			connection.setReadTimeout(5000);
			return new OperationResult<>(connection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			return new OperationResult<>(e);
		}
	}

	public static OperationResult<InputStream> queryGoogle(String query, Integer start, Integer amount) {
		return getInputStream("https://google.com/search?q=" +
				query.replace(' ', '+') +
				(start != null ? ("&start=" + start) : "") +
				(amount != null ? ("&num=" + amount) : "") +
				"&nfpr=1"
		);
	}
}
