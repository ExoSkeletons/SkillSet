package aviadl40.com.skillset.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Scanner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.NetUtils;
import aviadl40.com.skillset.NetUtils.ContentArrayNetTask;
import aviadl40.com.skillset.NetUtils.OperationResult;
import aviadl40.com.skillset.R;
import aviadl40.com.skillset.Utils;
import aviadl40.com.skillset.adapters.ArrayRecyclerViewAdapter;

@SuppressLint("StaticFieldLeak")
public final class DiscoverFragment extends Fragment {
	private static final class URLData {
		final String url, title;

		URLData(String url, String title) {
			this.title = title;
			this.url = url;
		}
	}

	private final class QueryTask extends ContentArrayNetTask<Void, Void, URLData> {
		private final int DOWNLOAD_AMOUNT = 10;
		private final String query;

		QueryTask(String query, boolean append) {
			super(resultsAdapter, append);
			this.query = query;
		}

		@Override
		protected OperationResult<ArrayList<URLData>> doInBackground(Void... params) {
			ArrayList<URLData> urls = new ArrayList<>();

			OperationResult<InputStream> queryGoogle = NetUtils.queryGoogle(query, startPosition, DOWNLOAD_AMOUNT);

			if (queryGoogle.resultOK()) {
				try {
					Scanner scanner = new Scanner(queryGoogle.result);
					String s;
					scanner.useDelimiter("\\A");
					s = scanner.next();

					for (int tagIndex = s.indexOf("<a "), URLIndex, titleIndex; tagIndex < s.length() && tagIndex != -1; tagIndex = s.indexOf("<a ", tagIndex + 1)) {
						if (!Utils.startsWith(s, tagIndex, "<a class=") // Is not link
								|| Utils.startsWith(s, tagIndex + 9, "\"q qs\"") // Is non textual link
								|| Utils.startsWith(s, tagIndex + 9, "\"Fx4vi\"") // Is google links
								|| Utils.startsWith(s, URLIndex = s.indexOf(" href=", tagIndex) + 7, "/")) // Is a loop-back
							continue;

						final String url, title;

						url = s.substring(URLIndex, s.indexOf('"', URLIndex));

						if (url.contains("google.com/setprefs?")) // Is google preferences
							continue;

						titleIndex = URLIndex;
						do {
							titleIndex = s.indexOf('>', titleIndex) + 1;
						} while (s.charAt(titleIndex) == '<');
						title = URLDecoder.decode(s.substring(titleIndex, s.indexOf('<', titleIndex)), "UTF-8");

						urls.add(new URLData(url, title));
					}
				} catch (IllegalArgumentException | UnsupportedEncodingException ignored) {
				}
				return new OperationResult<>(urls);
			}
			return new OperationResult<>(queryGoogle.error);
		}

		@Override
		protected void onPreExecute() {
			showProgress(true);
		}

		@Override
		protected void onCancelled() {
			showProgress(false);
			contentAdapter.clear();
		}

		@Override
		protected void onResult(ArrayList<URLData> result) {
			showProgress(false);
			super.onResult(result);
		}

		@Override
		protected void onError(NetUtils.OperationError error) {
			showProgress(false);
			error.makeToast(getContext()).show();
		}

		void showProgress(final boolean show) {
			if (append)
				Utils.fade(searchProgress, show);
			else {
				if (show) Utils.fade(resultsList, searchProgress, new Runnable() {
					@Override
					public void run() {
						contentAdapter.clear();
					}
				});
				else Utils.fade(searchProgress, resultsList);
			}
		}
	}

	private QueryTask queryTask;
	private SearchView searchView;
	private RecyclerView resultsList;
	private ArrayRecyclerViewAdapter<URLData, RecyclerView.ViewHolder> resultsAdapter;
	private ProgressBar searchProgress;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_discover, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		resultsList = view.findViewById(R.id.discover_search_results);
		resultsList.setLayoutManager(new LinearLayoutManager(getActivity()));
		resultsList.setAdapter(resultsAdapter = new ArrayRecyclerViewAdapter<URLData, RecyclerView.ViewHolder>() {
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.discovery_result, parent, false)) {
				};
			}

			@Override
			public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position, final URLData item) {
				((TextView) holder.itemView.findViewById(R.id.discover_result_header)).setText(item.title);
				((TextView) holder.itemView.findViewById(R.id.discover_result_url)).setText(item.url);
			}
		});
		resultsList.addOnScrollListener(new Utils.ContentScrollListener(resultsAdapter) {
			@Override
			protected void onEndReached(RecyclerView recyclerView) {
				if (!TextUtils.isEmpty(searchView.getQuery()))
					startQuery(true);
			}
		});
		searchView = view.findViewById(R.id.discover_search_bar);
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				startQuery(false); // TODO: close keyboard with IME?
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				stopQuery();
				return false;
			}
		});
		searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				stopQuery();
				return false;
			}
		});
		searchProgress = view.findViewById(R.id.discover_search_progress);
	}

	@Override
	public void onDestroyView() {
		Utils.cancelTask(queryTask);
		super.onDestroyView();
	}

	private void startQuery(boolean append) {
		if (!Utils.isRunning(queryTask)) {
			queryTask = new QueryTask(searchView.getQuery().toString(), append);
			queryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}

	private void stopQuery() {
		Utils.cancelTask(queryTask);
		resultsAdapter.clear();
	}
}
