package aviadl40.com.skillset;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.activities.SingleFragmentActivity;
import aviadl40.com.skillset.adapters.ArrayRecyclerViewAdapter;
import aviadl40.com.skillset.adapters.PreviewAdapter;
import aviadl40.com.skillset.fragments.ObjectFragment;

@SuppressWarnings({"StaticFieldLeak", "InflateParams", "unused"})
public abstract class Item<ItemData> implements Expandable, Dumpable {
	public enum ItemType {
		video(VideoItem.class),
		image(ImageItem.class),
		audio(AudioItem.class),
		text(TextItem.class),
		document(DocumentItem.class);

		public final Class<? extends Item> iClass;

		ItemType(final Class<? extends Item> iClass) {
			this.iClass = iClass;
		}
	}

	public static final class ItemFragment extends ObjectFragment<Item> {
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			return inflater.inflate(R.layout.item_full, null, false);
		}

		@Override
		protected Item readObject(InputStream is) throws IOException {
			try {
				Context context = requireContext();
				return readItem(is, context.getContentResolver());
			} catch (IllegalStateException e) {
				throw new IOException(e);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void populateView(@NonNull final View view, @NonNull final Item item) {
			ImageView pic = view.findViewById(R.id.item_full_publisher_picture);
			TextView name = view.findViewById(R.id.item_full_publisher_name);
			pic.setImageBitmap(item.publisher.picture);
			pic.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					item.publisher.expand(v.getContext());
				}
			});
			name.setText(item.publisher.username);

			TextView categoryName = view.findViewById(R.id.item_full_category_name);
			categoryName.setText(item.skill.name);
			categoryName.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					item.skill.expand(v.getContext());
				}
			});

			View contentView = item.getContentView(getLayoutInflater());
			contentView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			((ViewGroup) view.findViewById(R.id.item_full_content)).addView(contentView);

			Spinner p = view.findViewById(R.id.item_full_praise), c = view.findViewById(R.id.item_full_criticise);
			p.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
					// Comment c = new PositiveComment(position);
					// -- submit c
					parent.setVisibility(View.INVISIBLE);
					showFeedbackBox(view, true);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					hideFeedbackBox(view);
				}
			});
			c.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
					showFeedbackBox(view, false);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					hideFeedbackBox(view);
				}
			});

			hideFeedbackBox(view);

			RecyclerView commentList = view.findViewById(R.id.item_full_comments);
			commentList.setAdapter(new Comment.ListAdapter(item).add(item.comments));
			commentList.setLayoutManager(new LinearLayoutManager(getContext()) {
				@Override
				public boolean canScrollVertically() {
					return false;
				}
			});
		}

		private void showFeedbackBox(@NonNull final View view, final boolean positive) { // TODO: replace with CommentType
			EditText feedback = view.findViewById(R.id.item_full_feedback_box);

			feedback.setHint(positive ? R.string.hint_feedback_positive : R.string.hint_feedback_explain_critique);
			feedback.requestFocus();

			feedback.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						if (!positive && TextUtils.isEmpty(v.getText()))
							v.setError(getString(R.string.error_field_required));
						else {
							// Re/submit comment (edit)
							Toast.makeText(getContext(), positive ? getString(R.string.feedback_updated) : getString(R.string.feedback_sent), Toast.LENGTH_SHORT).show();
							hideFeedbackBox(view);
						}
					}
					return false;
				}
			});
		}

		private void hideFeedbackBox(@NonNull View view) {
			EditText feedback = view.findViewById(R.id.item_full_feedback_box);
			feedback.setVisibility(View.GONE);
		}
	}

	public static final class ItemPreviewAdapter extends PreviewAdapter<Item, RecyclerView.ViewHolder> {
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, null)) {
			};
		}

		@Override
		protected void onBindViewHolder(RecyclerView.ViewHolder holder, int position, final Item item) {
			super.onBindViewHolder(holder, position, item);
			ViewGroup container = (ViewGroup) holder.itemView;
			container.removeAllViewsInLayout();
			View previewView = item.getPreviewView(LayoutInflater.from(container.getContext()));
			previewView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			container.addView(previewView);
		}
	}

	public static final class Comment implements Dumpable {
		public static class ListAdapter extends ArrayRecyclerViewAdapter<Comment, RecyclerView.ViewHolder> {
			@NonNull
			private final Item item;

			ListAdapter(@NonNull Item item) {
				this.item = item;
			}

			@Override
			public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, final Comment comment) {
				ImageView pic = holder.itemView.findViewById(R.id.comment_publisher_picture);
				TextView name = holder.itemView.findViewById(R.id.comment_publisher_name);

				pic.setImageBitmap(comment.publisher.picture);
				name.setText(comment.publisher.username);
				if (comment.publisher.equals(item.publisher))
					((TextView) holder.itemView.findViewById(R.id.comment_publisher_name)).setTextColor(Color.YELLOW);
				View.OnClickListener listener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						comment.publisher.expand(v.getContext());
					}
				};
				pic.setOnClickListener(listener);
				name.setOnClickListener(listener);

				((TextView) holder.itemView.findViewById(R.id.comment_text)).setText(comment.text);
			}

			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false)) {
				};
			}


		}

		static final short DOWNLOAD_AMOUNT = 25;

		User publisher;
		String text;

		Comment(User user, String text) {
			this.publisher = user;
			this.text = text;
		}

		Comment() {
			this(User.ANON, "");
		}

		boolean isEmpty() {
			return TextUtils.isEmpty(text);
		}

		@Override
		public void readFrom(InputStream i) throws IOException {
			text = StreamUtils.readString(i);
			publisher = User.readUser(i);
		}

		@Override
		public void writeTo(OutputStream o) throws IOException {
			StreamUtils.write(o, text);
			User.writeUser(o, publisher);
		}
	}

	public static final class TextItem extends Item<String> {
		public TextItem() {
			super(ItemType.text, "");
		}

		public TextItem(User publisher, @NonNull String iData) {
			super(publisher, ItemType.text, iData);
		}

		@Override
		public boolean isValid() {
			return iData != null;
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			final LinearLayout container = new LinearLayout(inflater.getContext());
			final int pad = inflater.getContext().getResources().getDimensionPixelSize(R.dimen.sparsity);
			container.setPadding(pad, pad, pad, pad);

			final TextView textView = new TextView(inflater.getContext());
			textView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
			textView.setTextDirection(View.TEXT_DIRECTION_LTR);
			textView.setText(iData);

			container.addView(textView);
			return container;
		}

		@Override
		public void readFrom(InputStream i) throws IOException {
			iData = StreamUtils.readString(i);
			super.readFrom(i);
		}

		@Override
		public void writeTo(OutputStream o) throws IOException {
			StreamUtils.write(o, iData);
			super.writeTo(o);
		}
	}

	public static final class ImageItem extends Item<Bitmap> {
		static final int MAX_SIZE = 0xffff;
		private Bitmap cachedScaledImage;

		public ImageItem() {
			super(ItemType.image, null);
		}

		public ImageItem(User publisher, @NonNull Bitmap iData) {
			super(publisher, ItemType.image, iData);
		}

		private Bitmap getScaledImage() {
			return cachedScaledImage == null
					? cachedScaledImage = Utils.getScaledBitmap(iData)
					: cachedScaledImage;
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			final ImageView imageView = new ImageView(inflater.getContext());
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageBitmap(getScaledImage());
			return imageView;
		}

		@Override
		public boolean isValid() {
			return iData != null;
		}

		@Override
		public void readFrom(InputStream i) throws IOException {
			iData = StreamUtils.readBitmap(i);
			super.readFrom(i);
		}

		@Override
		public void writeTo(OutputStream o) throws IOException {
			StreamUtils.write(o, iData, false);
			super.writeTo(o);
		}
	}

	private static abstract class URIItem extends Item<Uri> {
		@NonNull
		final ContentResolver resolver;

		URIItem(ItemType iType, @NonNull ContentResolver resolver) {
			super(iType, null);
			this.resolver = resolver;
		}

		URIItem(User publisher, ItemType iType, @NonNull ContentResolver resolver, @NonNull Uri iData) {
			super(publisher, iType, iData);
			this.resolver = resolver;
		}

		@Override
		public void readFrom(InputStream is) throws IOException {
			super.readFrom(is);
			String s = StreamUtils.readString(is);
			if (s.equals(""))
				iData = null;
			else {
				String
						scheme = StreamUtils.readString(is),
						fragment = StreamUtils.readString(is),
						query = StreamUtils.readString(is),
						path = StreamUtils.readString(is);
				iData = new Uri.Builder()
						.authority(s)
						.scheme(scheme.equals("") ? null : scheme)
						.fragment(fragment.equals("") ? null : fragment)
						.query(query.equals("") ? null : query)
						.path(path.equals("") ? null : path)
						.build();
			}
		}

		@Override
		public void writeTo(OutputStream o) throws IOException {
			super.writeTo(o);
			if (iData == null)
				StreamUtils.write(o, "");
			else {
				String
						authority = iData.getAuthority(),
						scheme = iData.getScheme(),
						fragment = iData.getFragment(),
						query = iData.getQuery(),
						path = iData.getPath();
				StreamUtils.write(o, authority == null ? "" : authority);
				StreamUtils.write(o, scheme == null ? "" : scheme);
				StreamUtils.write(o, fragment == null ? "" : fragment);
				StreamUtils.write(o, query == null ? "" : query);
				StreamUtils.write(o, path == null ? "" : path);
			}
		}

		@Override
		public boolean isValid() {
			return iData != null;
		}
	}

	public static final class AudioItem extends URIItem {
		private static class UpdateTask extends AsyncTask<Void, Void, Void> {
			private final AudioItem item;
			private View root;

			UpdateTask(AudioItem item, View root) {
				this.item = item;
				setRoot(root);
			}

			void setRoot(View root) {
				this.root = root;
			}

			@Override
			protected Void doInBackground(Void... voids) {
				do {
					System.out.println("updater running");
					publishProgress();
					try {
						Thread.sleep(250);
					} catch (InterruptedException ignored) {
					}
				} while (!isCancelled() && player != null);
				return null;
			}

			@Override
			protected void onPreExecute() {
				System.out.println("started updater");
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				item.update(root);
			}

			@Override
			protected void onProgressUpdate(Void... values) {
				item.update(root);
				System.out.println("updater updating");
			}

			@Override
			protected void onCancelled() {
				System.out.println("updater canceled");
				item.update(root);
			}
		}

		@Nullable
		private static MediaPlayer player = null;
		@Nullable
		private static Uri current = null;
		@Nullable
		private static UpdateTask updateViewTask = null;

		static void pause() {
			System.out.println("pausing...");
			if (player != null && current != null) {
				player.pause();
				System.out.println("player paused");
			}
			Utils.cancelTask(updateViewTask);
		}

		public static void stop() {
			System.out.println("stopping...");
			pause();
			if (player != null) {
				current = null;
				System.out.println("current = null");
				player.stop();
				System.out.println("player stopped");
				player.release();
				System.out.println("player released");
				player = null;
				System.out.println("player nullified");
			}
		}

		final View.OnClickListener
				pl = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				play(Utils.findParentById(v, R.id.audio_root));
			}
		},
				pa = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						pause();
					}
				};

		public AudioItem(@NonNull ContentResolver resolver) {
			super(ItemType.audio, resolver);
		}

		public AudioItem(User publisher, @NonNull ContentResolver resolver, @NonNull Uri iData) {
			super(publisher, ItemType.audio, resolver, iData);
		}

		private void onError(View root, String err) {
			stop();
			update(root);
			root.findViewById(R.id.audio_prepare_progress).setVisibility(View.INVISIBLE);
			root.findViewById(R.id.audio_play).setVisibility(View.VISIBLE);
			Toast.makeText(root.getContext(), err, Toast.LENGTH_LONG).show();
		}

		void play(final View root) {
			System.out.println("playing " + iData + "...");
			if (iData == null) {
				onError(root, root.getContext().getString(R.string.error_playing_media));
				return;
			}
			if (player == null)
				player = new MediaPlayer();
			if (current == iData) {
				if (player.isPlaying())
					pause();
				// Play
				player.start();
				System.out.println("player playing");
				updateViewTask = new UpdateTask(this, root);
				updateViewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				try {
					System.out.println("setting data source to " + iData);
					player.seekTo(0);
					pause();
					player.reset();
					player.setDataSource(root.getContext(), iData);

					final ProgressBar progress = root.findViewById(R.id.audio_prepare_progress);
					final ImageView playToggle = root.findViewById(R.id.audio_play);
					final SeekBar seek = root.findViewById(R.id.audio_seek);

					progress.setVisibility(View.VISIBLE);
					playToggle.setVisibility(View.INVISIBLE);

					player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							AudioItem.this.onError(root, root.getContext().getString(R.string.error_media_player, what));
							return false;
						}
					});
					player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							System.out.println("playback completed");
							player.seekTo(0);
							pause();
						}
					});
					player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							System.out.println("done prepare");

							current = iData;
							System.out.println("current = " + current);

							AudioItem.this.onPrepared(root);

							play(root);
						}
					});
					System.out.println("preparing async...");
					player.prepareAsync();
				} catch (IOException e) {
					onError(root, root.getContext().getString(R.string.error_file_read));
					e.printStackTrace();
				}
			}
		}

		private void onPrepared(View root) {
			SeekBar seek = root.findViewById(R.id.audio_seek);
			seek.setMax(player == null ? 0 : player.getDuration());
			seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (isCurrent()) {
						if (fromUser && player != null)
							player.seekTo(progress);
						View root = Utils.findParentById(seekBar, R.id.audio_root);
						if (root != null)
							((TextView) root.findViewById(R.id.audio_time)).setText(seekBar.getContext().getString(R.string.player_time, Utils.formatTime(progress / 1000), Utils.formatTime(seekBar.getMax() / 1000)));
					} else
						seekBar.setProgress(0);
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
			});
			root.findViewById(R.id.audio_prepare_progress).setVisibility(View.INVISIBLE);
			root.findViewById(R.id.audio_play).setVisibility(View.VISIBLE);
			if (updateViewTask != null)
				updateViewTask.setRoot(root);
		}

		private void update(View root) {
			System.out.println("updating root");
			ImageView playToggle = root.findViewById(R.id.audio_play);
			if (isCurrent()) {
				SeekBar seek = root.findViewById(R.id.audio_seek);
				if (!seek.isPressed())
					seek.setProgress(player == null ? 0 : player.getCurrentPosition());
			}
			playToggle.setOnClickListener(player != null && player.isPlaying() && isCurrent() ? pa : pl);
			playToggle.setImageResource(player != null && player.isPlaying() && isCurrent() ? R.drawable.ic_media_pause_light : R.drawable.ic_media_play_light);
		}

		public boolean isCurrent() {
			return iData != null && current == iData;
		}

		@Override
		public boolean isValid() {
			return super.isValid() && ("" + resolver.getType(iData)).contains("audio");
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.audio, null, false);
			if (isCurrent())
				onPrepared(view);
			update(view);
			((TextView) view.findViewById(R.id.audio_name)).setText(Utils.getUriName(resolver, iData));
			return view;
		}
	}

	public static final class VideoItem extends URIItem {
		public VideoItem(@NonNull ContentResolver resolver) {
			super(ItemType.video, resolver);
		}

		public VideoItem(User publisher, @NonNull ContentResolver resolver, @NonNull Uri iData) {
			super(publisher, ItemType.video, resolver, iData);
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			final VideoView videoView = new VideoView(inflater.getContext());
			videoView.setVideoURI(iData);
			videoView.seekTo(1); // Seek to first frame

			final MediaController mediaController = new MediaController(inflater.getContext());
			mediaController.setAnchorView(videoView);
			mediaController.setBackgroundColor(Color.BLACK);
			videoView.setMediaController(mediaController);

			return videoView;
		}

		@Override
		public View getPreviewView(LayoutInflater inflater) {
			final ImageView imageView = new ImageView(inflater.getContext());
			imageView.setImageResource(R.drawable.ic_media_play_light);
			return imageView;
		}

		@Override
		public boolean isValid() {
			return super.isValid() && ("" + resolver.getType(iData)).contains("video") || ("" + resolver.getType(iData)).contains("octet-stream");
		}
	}

	public static final class DocumentItem extends URIItem {
		public DocumentItem(@NonNull ContentResolver resolver) {
			super(ItemType.document, resolver);
		}

		public DocumentItem(User publisher, @NonNull ContentResolver resolver, @NonNull Uri iData) {
			super(publisher, ItemType.document, resolver, iData);
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.file, null, false);
			((TextView) view.findViewById(R.id.file_name)).setText(Utils.getUriName(resolver, iData));
			((TextView) view.findViewById(R.id.file_extension)).setText(Utils.getUriExtension(resolver, iData));
			((TextView) view.findViewById(R.id.file_size)).setText(Utils.formatFileSize(Utils.getUriSize(resolver, iData)));
			return view;
		}
	}

	private static Item readItem(InputStream i, ContentResolver resolver) throws IOException {
		try {
			final Class<? extends Item> iClass = StreamUtils.readEnum(i, ItemType.class).iClass;
			Item res = URIItem.class.isAssignableFrom(iClass)
					? iClass.getConstructor(ContentResolver.class).newInstance(resolver)
					: iClass.getConstructor().newInstance();
			res.readFrom(i);
			return res;
		} catch (ReflectiveOperationException e) {
			throw new IOException(e);
		}
	}

	public static ArrayList<Item> readItems(InputStream i, ContentResolver resolver, int amount) {
		final ArrayList<Item> items = new ArrayList<>();
		for (int index = 0; index < amount; index++) {
			try {
				items.add(readItem(i, resolver));
			} catch (IOException ignored) {
				break;
			}
		}
		return items;
	}

	public static void writeItem(OutputStream o, Item i) throws IOException {
		StreamUtils.write(o, i.iType);
		i.writeTo(o);
	}

	public final ItemType iType;
	final ArrayList<Comment> comments = new ArrayList<>();
	ItemData iData;
	@NonNull
	User publisher;
	private Skill skill = Skill.NONE;

	Item(@Nullable User publisher, @NonNull ItemType iType, @Nullable ItemData iData, @NonNull Comment... comments) {
		this.iType = iType;
		this.publisher = publisher == null ? User.ANON : publisher;
		this.iData = iData;
		this.comments.clear();
		this.comments.addAll(Arrays.asList(comments));
	}

	Item(@NonNull ItemType iType, @Nullable ItemData iData, @NonNull Comment... comments) {
		this(User.ANON, iType, iData, comments);
	}

	Skill getSkill() {
		return skill;
	}

	void setSkill(Skill cat) {
		skill = cat;
	}

	public final ItemData getIData() {
		return iData;
	}

	public final void setIData(@Nullable ItemData iData) {
		this.iData = iData;
	}

	public abstract boolean isValid();

	public abstract View getContentView(LayoutInflater inflater);

	public View getPreviewView(LayoutInflater inflater) {
		return getContentView(inflater);
	}

	@Override
	public final boolean expand(Context context) {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
			writeItem(os, this);
			Bundle args = new Bundle(1);
			args.putByteArray(ObjectFragment.OBJECT_KEY, os.toByteArray());
			SingleFragmentActivity.start(context, ItemFragment.class, args);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void readFrom(InputStream is) throws IOException {
		publisher = User.readUser(is);
		skill = Skill.readSkill(is);
		comments.clear();
		for (byte i = 0; i < Comment.DOWNLOAD_AMOUNT; i++) {
			Comment c = new Comment();
			c.readFrom(is);
			if (c.isEmpty())
				break;
			comments.add(c);
		}
		// TODO: readMoreComments(is)
	}

	@Override
	public void writeTo(OutputStream o) throws IOException {
		User.writeUser(o, publisher);
		Skill.writeSkill(o, skill);
		for (byte i = 0; i < comments.size(); i++)
			comments.get(i).writeTo(o);
		new Comment().writeTo(o); // empty comment
	} // FIXME: don't send comments, this is only for testing
}