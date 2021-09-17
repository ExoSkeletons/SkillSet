package aviadl40.com.skillSet;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillSet.adapters.ArrayRecyclerViewAdapter;
import aviadl40.com.skillSet.fragments.ObjectFragment;

@SuppressWarnings({"StaticFieldLeak", "InflateParams"})
public abstract class Item<ItemData> implements Dumpable {
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

	public static final class PreviewAdapter extends ArrayRecyclerViewAdapter<Item, RecyclerView.ViewHolder> { // TODO: convert to ArrayRecyclerViewAdapter
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, null)) {
			};
		}

		@Override
		protected void onBindViewHolder(RecyclerView.ViewHolder holder, int position, Item item) {
			ViewGroup container = (ViewGroup) holder.itemView;
			container.removeAllViewsInLayout();
			View previewView = item.getPreviewView(LayoutInflater.from(container.getContext()));
			previewView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			container.addView(previewView);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	public static final class ItemFragment extends ObjectFragment<Item> {
		@Override
		public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
			return inflater.inflate(R.layout.item_full, null, false);
		}

		@Override
		protected Item readObject(InputStream is) throws IOException, ReflectiveOperationException {
			return readItem(is);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onViewCreated(View view, Item item, @Nullable Bundle savedInstanceState) {
			if (item != null) {
				// Add profile pic
				((ImageView) view.findViewById(R.id.item_full_publisher_picture)).setImageBitmap(item.publisher.picture);
				// Add name
				((TextView) view.findViewById(R.id.item_full_publisher_name)).setText(item.publisher.username);
				// Add category name
				((TextView) view.findViewById(R.id.item_full_category_name)).setText(item.category.name);
				// Add content
				((FrameLayout) view.findViewById(R.id.item_full_content)).addView(item.getContentView(getLayoutInflater()));
				// Add comments
				RecyclerView commentList = view.findViewById(R.id.item_full_comments);
				commentList.setAdapter(new Comment.ListAdapter(item.comments));
				commentList.setLayoutManager(new LinearLayoutManager(getLayoutInflater().getContext()) {
					@Override
					public boolean canScrollVertically() {
						return false;
					}
				});
			}
		}
	}

	public static final class Comment implements Dumpable {
		public static class ListAdapter extends ArrayRecyclerViewAdapter<Comment, RecyclerView.ViewHolder> {
			ListAdapter(ArrayList<Comment> comments) {
				super(comments);
			}

			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				return new RecyclerView.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false)) {
				};
			}

			@Override
			public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, Comment item) {
				((ImageView) holder.itemView.findViewById(R.id.comment_publisher_picture)).setImageBitmap(item.publisher.picture);
				((TextView) holder.itemView.findViewById(R.id.comment_publisher_name)).setText(item.publisher.username);
				((TextView) holder.itemView.findViewById(R.id.comment_text)).setText(item.text);
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
			User.writeUser(publisher, o);
		}
	}

	public static final class TextItem extends Item<String> {
		public TextItem(User publisher) {
			this(publisher, "");
		}

		TextItem(User publisher, @NonNull String iData) {
			super(publisher, ItemType.text, iData);
		}

		@Override
		public boolean isValid() {
			return iData != null;
		}

		@Override
		public TextView getContentView(LayoutInflater inflater) {
			final TextView textView = new TextView(inflater.getContext());
			textView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
			textView.setTextDirection(View.TEXT_DIRECTION_LTR);
			textView.setText(iData);
			return textView;
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

		public ImageItem(User publisher) {
			this(publisher, null);
		}

		public ImageItem(User publisher, Bitmap iData) {
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
			imageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
			imageView.setImageBitmap(getScaledImage());
			return imageView;
		}

		@Override
		public boolean isValid() {
			return iData != null;
		}

		@Override
		public View getPreviewView(LayoutInflater inflater) {
			return getContentView(inflater);
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
		final ContentResolver resolver;

		URIItem(User publisher, ItemType iType, ContentResolver resolver, Uri iData) {
			super(publisher, iType, iData);
			this.resolver = resolver;
		}

		@Override
		public void readFrom(InputStream is) throws IOException {
			super.readFrom(is);
		}

		@Override
		public void writeTo(OutputStream o) throws IOException {
			super.writeTo(o);
		}

		@Override
		public boolean isValid() {
			return iData != null;
		}
	}

	public static final class AudioItem extends URIItem {
		private static class UpdateTask extends AsyncTask<Void, Void, Void> {
			private final AudioItem item;
			private final View root;

			UpdateTask(AudioItem item, View root) {
				this.item = item;
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
				updateViewTask = null;
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
				updateViewTask = null;
			}
		}

		static void pause() {
			System.out.println("pausing...");
			if (player != null) {
				player.pause();
				System.out.println("player paused");
			}
			Utils.cancelTask(updateViewTask);
		}

		public static void stop() {
			System.out.println("stopping...");
			pause();
			if (player != null) {
				player.stop();
				System.out.println("player stopped");
				player.release();
				System.out.println("player released");
				player = null;
				System.out.println("player nullified");
			}
		}

		@Nullable
		private static MediaPlayer player = null;
		@Nullable
		private static UpdateTask updateViewTask = null;

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

		public AudioItem(User publisher, ContentResolver resolver, Uri iData) {
			super(publisher, ItemType.audio, resolver, iData);
		}

		private void preparePlayerAsync(final View root, final boolean autoPlay) {
			if (iData != null) {
				if (player != null)
					stop();
				try {
					player = new MediaPlayer();

					player.setDataSource(root.getContext(), iData);

					final ProgressBar progress = root.findViewById(R.id.audio_prepare_progress);
					final ImageView playToggle = root.findViewById(R.id.audio_play);
					final SeekBar seek = root.findViewById(R.id.audio_seek);

					seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							if (fromUser)
								player.seekTo(progress);
							((TextView) seekBar.getRootView().findViewById(R.id.audio_time)).setText(root.getContext().getString(R.string.player_time, Utils.formatTime(progress / 1000), Utils.formatTime(seekBar.getMax() / 1000)));
						}

						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
						}
					});

					player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							stop();
							progress.setVisibility(View.INVISIBLE);
							playToggle.setVisibility(View.VISIBLE);
							update(root);
							Toast.makeText(root.getContext(), root.getContext().getString(R.string.error_media_player, what), Toast.LENGTH_LONG).show();
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

							progress.setVisibility(View.INVISIBLE);
							playToggle.setVisibility(View.VISIBLE);
							seek.setActivated(true);
							seek.setMax(player.getDuration());

							if (autoPlay) play(root);

							update(root);
						}
					});
					System.out.println("preparing async...");
					player.prepareAsync();
				} catch (Exception e) {
					stop();
					Toast.makeText(root.getContext(), R.string.error_file_read, Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}
		}

		void play(View target) {
			System.out.println("playing...");
			if (player == null)
				preparePlayerAsync(target, true);
			else {
				if (player.isPlaying())
					pause();
				player.start();
				System.out.println("playback started");
				Utils.cancelTask(updateViewTask);
				updateViewTask = new UpdateTask(this, target);
				updateViewTask.execute();
			}
		}

		private void update(View root) {
			System.out.println("updating root");
			SeekBar seek = root.findViewById(R.id.audio_seek);
			ImageView playToggle = root.findViewById(R.id.audio_play);
			if (!seek.isPressed())
				seek.setProgress(player == null ? 0 : player.getCurrentPosition());
			if (player != null) {
				playToggle.setOnClickListener(player.isPlaying() ? pa : pl);
				playToggle.setImageResource(player.isPlaying() ? R.drawable.ic_media_pause_light : R.drawable.ic_media_play_light);
			}
		}

		@Override
		public boolean isValid() {
			return super.isValid() && ("" + resolver.getType(iData)).contains("audio");
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			View view = inflater.inflate(R.layout.audio, null, false);
			preparePlayerAsync(view.findViewById(R.id.audio_root), false);
			return view;
		}
	}

	public static final class VideoItem extends URIItem {
		public VideoItem(User publisher, ContentResolver resolver, Uri iData) {
			super(publisher, ItemType.video, resolver, iData);
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			final VideoView videoView = new VideoView(inflater.getContext());
			videoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
			videoView.setVideoURI(iData);
			videoView.seekTo(1); // Seek to first frame

			final MediaController mediaController = new MediaController(inflater.getContext());
			mediaController.setAnchorView(videoView);
			videoView.setMediaController(mediaController);
			videoView.setBackground(mediaController.getForeground());

			// TODO: icon [ImageView] + duration [TextView]
			return videoView;
		}

		@Override
		public boolean isValid() {
			return super.isValid() && ("" + resolver.getType(iData)).contains("video") || ("" + resolver.getType(iData)).contains("octet-stream");
		}
	}

	public static final class DocumentItem extends URIItem {
		public DocumentItem(User publisher, ContentResolver resolver, Uri iData) {
			super(publisher, ItemType.document, resolver, iData);
		}

		@Override
		public View getContentView(LayoutInflater inflater) {
			final ImageView view = new ImageView(inflater.getContext());
			view.setScaleType(ImageView.ScaleType.FIT_CENTER);
			view.setImageResource(R.drawable.ic_vol_type_tv_light);
			return view;
		}

		@Override
		public boolean isValid() {
			return super.isValid() && ("" + resolver.getType(iData)).contains("pdf");
		}
	}

	public static Item readItem(InputStream i) throws IOException, ReflectiveOperationException {
		Item res = StreamUtils.readEnum(i, ItemType.class)
				.iClass
				.getConstructor(User.class)
				.newInstance(User.readUser(i));
		res.readFrom(i);
		return res;
	}

	public static ArrayList<Item> readItems(InputStream i) {
		final ArrayList<Item> items = new ArrayList<>(DOWNLOAD_AMOUNT);
		for (int index = 0; index < DOWNLOAD_AMOUNT; index++) {
			try {
				items.add(readItem(i));
			} catch (ReflectiveOperationException ignored) {
			} catch (IOException ignored) {
				break;
			}
		}
		return items;
	}

	public static void writeItem(OutputStream o, Item i) throws IOException {
		StreamUtils.write(o, i.iType);
		User.writeUser(i.publisher, o);
		i.writeTo(o);
	}

	public static final String KEY = "item key";
	private static final short DOWNLOAD_AMOUNT = 5;
	public final ItemType iType;
	public final ArrayList<Comment> comments = new ArrayList<>();
	@NonNull
	private final User publisher;
	ItemData iData;
	private Category category = Category.NONE;

	Item(@Nullable User publisher, @NonNull ItemType iType, @NonNull ItemData iData, @NonNull Comment... comments) {
		this.iType = iType;
		this.publisher = publisher == null ? User.ANON : publisher;
		this.iData = iData;
		this.comments.clear();
		this.comments.addAll(Arrays.asList(comments));
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category cat) {
		category = cat;
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
	public void readFrom(InputStream is) throws IOException {
		category = Category.readCategory(is);
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
		Category.writeCategory(category, o);
		for (byte i = 0; i < comments.size(); i++)
			comments.get(i).writeTo(o);
		new Comment().writeTo(o); // empty comment
	} // FIXME: don't send comments, this is only for testing
}