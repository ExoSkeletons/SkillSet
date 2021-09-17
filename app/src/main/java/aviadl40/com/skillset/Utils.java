package aviadl40.com.skillset;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.ViewParent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import aviadl40.com.skillset.Item.Comment;
import aviadl40.com.skillset.activities.MainActivity;
import aviadl40.com.skillset.adapters.ArrayRecyclerViewAdapter;

@SuppressWarnings("ALL")
public final class Utils {
	public static class ContentScrollListener extends RecyclerView.OnScrollListener {
		private final ArrayRecyclerViewAdapter adapter;

		public ContentScrollListener(ArrayRecyclerViewAdapter adapter) {
			this.adapter = adapter;
		}

		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			super.onScrolled(recyclerView, dx, dy);
			if (adapter.getItemCount() == 0 || ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1)
				onEndReached(recyclerView);
		}

		protected void onEndReached(RecyclerView recyclerView) {
		}
	}
	public static final int MAX_TEXTURE_SIZE;
	public static final Runnable DO_NOTHING = new Runnable() {
		@Override
		public void run() {
		}
	};
	public static final String FILE_PROVIDER_PACKAGE = "com.skillset.android.fileprovider";

	static {
		// Safe minimum default size
		final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

		// Get EGL Display
		EGL10 egl = (EGL10) EGLContext.getEGL();
		EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

		// Initialise
		egl.eglInitialize(display, new int[2]);

		// Query total number of configurations
		int[] totalConfigurations = new int[1];
		egl.eglGetConfigs(display, null, 0, totalConfigurations);

		// Query actual list configurations
		EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
		egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

		int[] textureSize = new int[1];
		int maximumTextureSize = 0;

		// Iterate through all the configurations to located the maximum texture size
		for (int i = 0; i < totalConfigurations[0]; i++) {
			// Only need to check for width since opengl textures are always squared
			egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

			// Keep track of the maximum texture size
			if (maximumTextureSize < textureSize[0])
				maximumTextureSize = textureSize[0];
		}

		// Release
		egl.eglTerminate(display);

		// Return largest texture size found, or default
		MAX_TEXTURE_SIZE = Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
	}

	public static ArrayList<Item> DUMMY_ITEMS() {
		Resources res = MainActivity.res;
		ContentResolver resolver = MainActivity.resolver;
		// Dummy Items
		final Item[] items = new Item[]{
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round)),
				new Item.TextItem(User.randomUser(), "Call me Ishmael.\n\nSome years ago- never mind how long precisely- having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world."),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher)),
				new Item.VideoItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.purivision)),
				new Item.AudioItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.epic_sax_guy)),
				new Item.DocumentItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.fragment_upload)),
				new Item.TextItem(User.randomUser(), "This was a triumph.\nI'm making a note here- \"HUGE SUCCESS\".\nIt's hard to overstate my satisfaction.\nAperture Science."),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round)),
				new Item.AudioItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.elevator)),
				new Item.TextItem(User.randomUser(), "Call me Ishmael.\n\nSome years ago- never mind how long precisely- having little or no money in my purse, and nothing particular to interest me on shore, I thought I would sail about a little and see the watery part of the world."),
				new Item.AudioItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.epic_sax_guy)),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher)),
				new Item.ImageItem(User.randomUser(), BitmapFactory.decodeResource(res, R.mipmap.ic_launcher_round)),
				new Item.DocumentItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.fragment_upload)),
				new Item.AudioItem(User.randomUser(), resolver, getUriFromResource(res, R.raw.elevator)),
		};
		ArrayList<Skill> categories = DUMMY_CATEGORIES();
		categories.add(Skill.NONE);
		ArrayList<Comment> comments = DUMMY_COMMENTS();
		for (Item i : items) {
			// Dummy comments
			i.comments.addAll(comments);
			// Dummy categories
			i.setSkill(categories.get((int) (Math.random() * categories.size())));
		}
		return new ArrayList<>(Arrays.asList(items));
	}

	public static ArrayList<Skill> DUMMY_CATEGORIES() {
		return new ArrayList<>(Arrays.asList(
				new Skill("Painting"),
				new Skill("Basketball"),
				new Skill("Beesechruger"),
				new Skill("Games"),
				new Skill("Gymnastics"),
				new Skill("Parkour"),
				new Skill("Writing"),
				new Skill("Food and Cooking"),
				new Skill("Comedy"),
				new Skill("Bad Comdey"),
				new Skill("Lazy Town")
		));
	}

	public static ArrayList<Comment> DUMMY_COMMENTS() {
		return new ArrayList<>(Arrays.asList(
				new Comment(User.randomUser(), "Let's get down to business, to defeat the Huns. Did they send me daughters, when i asked for sons? You're a spineless, pale, pathetic lot, and you haven't got a clue. How could I make a man out of you?"),
				new Comment(User.randomUser(), "Ok but whats Updog?"),
				new Comment(User.randomUser(), "nice"),
				new Comment(User.randomUser(), "Look at the size of these lads.\nAbsolute units."),
				new Comment(User.randomUser(), "Never gonna give~~ never gonna give~~"),
				new Comment(User.randomUser(), "Now watch and learn here's the deal..."),
				new Comment(User.randomUser(), "Oh snap, It's Thanos."),
				new Comment(User.randomUser(), "שבע עשרה שבע עשרה שבע עשרה"),
				new Comment(User.randomUser(), "Ok now this is Epic.")
		));
	}

	public static void fade(final View v, final boolean in, final Runnable runAfter) {
		v.clearAnimation();
		v.animate()
				.setDuration(v.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime))
				.alpha(in ? 1 : 0)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationCancel(Animator animation) {
						v.setVisibility(in ? View.GONE : View.VISIBLE);
						v.setAlpha(in ? 0 : 1);
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						v.setVisibility(in ? View.VISIBLE : View.GONE);
						runAfter.run();
					}

					@Override
					public void onAnimationStart(Animator animation) {
						v.setVisibility(View.VISIBLE);
						v.setAlpha(in ? 0 : 1);
					}
				})
		;
	}

	public static void fade(final View v, final boolean in) {
		fade(v, in, DO_NOTHING);
	}

	public static void fade(final View v1, final View v2, final Runnable runInBetween, final Runnable runAtEnd) {
		v1.clearAnimation();
		final Runnable reset = new Runnable() {
			@Override
			public void run() {
				v1.setVisibility(View.VISIBLE);
				v1.setAlpha(1);
				v2.setVisibility(View.GONE);
				v2.setAlpha(0);
			}
		};
		v1.animate()
				.setDuration(v1.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime))
				.alpha(0)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationCancel(Animator animation) {
						reset.run();
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						v1.setVisibility(View.GONE);
						runInBetween.run();
						v2.animate()
								.setDuration(v2.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime))
								.alpha(1)
								.setListener(new AnimatorListenerAdapter() {
									@Override
									public void onAnimationCancel(Animator animation) {
										reset.run();
									}

									@Override
									public void onAnimationEnd(Animator animation) {
										runAtEnd.run();
									}

									@Override
									public void onAnimationStart(Animator animation) {
										v2.setVisibility(View.VISIBLE);
										v2.setAlpha(0);
									}
								});
					}

					@Override
					public void onAnimationStart(Animator animation) {
						reset.run();
					}
				});
	}

	public static void fade(final View v1, final View v2) {
		fade(v1, v2, DO_NOTHING, DO_NOTHING);
	}

	public static void fade(View v1, View v2, Runnable runInBetween) {
		fade(v1, v2, runInBetween, DO_NOTHING);
	}

	public static boolean startsWith(CharSequence sb, int startIndex, String lookFor) {
		if (startIndex < 0 || sb.length() < startIndex + lookFor.length())
			return false;
		for (int i = 0; i < lookFor.length(); i++)
			if (sb.charAt(startIndex + i) != lookFor.charAt(i))
				return false;
		return true;
	}

	public static boolean cancelTask(@Nullable AsyncTask task, boolean mayInterruptIfRunning) {
		return task == null || task.cancel(mayInterruptIfRunning);
	}

	public static boolean cancelTask(@Nullable AsyncTask task) {
		return cancelTask(task, true);
	}

	public static boolean isRunning(@Nullable AsyncTask task) {
		return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
	}

	public static String formatTime(int netSeconds) {
		int seconds = netSeconds % 60;
		int minutes = (netSeconds / 60) % 3600;
		int hours = netSeconds / 3600;
		return "" +
				(hours > 0 ? (hours < 10 ? "0" : "" + hours + ":") : "") +
				((minutes < 10 ? "0" : "") + minutes) + ":" +
				((seconds < 10 ? "0" : "") + seconds);
	}

	public static String formatFileSize(long sizeInBytes) {
		if (sizeInBytes < 1024)
			return "" + sizeInBytes + "b";
		if ((sizeInBytes /= 1024) < 1024)
			return "" + sizeInBytes + "KB";
		if ((sizeInBytes /= 1024) < 1024)
			return "" + sizeInBytes + "MB";
		if ((sizeInBytes /= 1024) < 1024)
			return "" + sizeInBytes + "GB";
		if ((sizeInBytes /= 1024) < 1024)
			return "" + sizeInBytes + "TB";
		return "" + sizeInBytes + "PB";
	}

	public static String concatinate(String[] strings) {
		String res = "";
		for (String s : strings)
			res += s;
		return res;
	}

	public static Bitmap textAsBitmap(String text, float textSize, int bg, int fg) {
		Bitmap image = Bitmap.createBitmap((int) textSize * 2, (int) textSize * 2, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(image);
		canvas.drawColor(bg);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTextSize(textSize);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(fg);
		canvas.drawText(text, canvas.getWidth() / 2, (canvas.getHeight() - (paint.descent() + paint.ascent())) / 2, paint);
		return image;
	}

	public static Bitmap orientBitmap(Bitmap image, int orient) {
		Matrix matrix = new Matrix();
		switch (orient) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.setRotate(270);
				break;
			default:
				return image;
		}
		return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
	}

	public static Bitmap orientedBitmap(String imagePath) throws IOException {
		Bitmap image = BitmapFactory.decodeFile(imagePath);
		return orientBitmap(image, new ExifInterface(imagePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED));
	}

	public static Bitmap orientedBitmap(ContentResolver resolver, Uri uri) throws IOException {
		return orientBitmap(
				MediaStore.Images.Media.getBitmap(resolver, uri),
				new ExifInterface(resolver.openInputStream(uri)).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
		);
	}

	public static float floor(float val, int decimalPoints) {
		final float pow = (int) Math.pow(10, decimalPoints);
		return (float) (Math.floor(val * pow) / pow);
	}

	public static Bitmap getScaledBitmap(Bitmap bitmap) {
		if (bitmap == null) return null;
		float
				size = Math.max(bitmap.getWidth(), bitmap.getHeight()),
				scale = size > MAX_TEXTURE_SIZE ? floor(MAX_TEXTURE_SIZE / size, 2) : 1;
		return Bitmap.createScaledBitmap(
				bitmap,
				(int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale),
				false
		);
	}

	public static Uri getUriFromResource(Resources res, int resId) {
		return new Uri.Builder()
				.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
				.authority(res.getResourcePackageName(resId))
				.appendPath(res.getResourceTypeName(resId))
				.appendPath(res.getResourceEntryName(resId))
				.build();
	}

	@Nullable
	public static String getUriColumn(ContentResolver resolver, Uri uri, String columnName) {
		if (resolver == null || uri == null)
			return null;
		Cursor cursor = resolver.query(uri, null, null, null, null);
		if (cursor == null)
			return null;
		cursor.moveToFirst();
		return cursor.getString(cursor.getColumnIndex(columnName));
	}

	public static String getUriName(ContentResolver resolver, Uri uri) {
		String fullName = getUriColumn(resolver, uri, OpenableColumns.DISPLAY_NAME);
		return fullName == null ? "" : fullName.substring(0, fullName.lastIndexOf("."));
	}

	public static String getUriExtension(ContentResolver resolver, Uri uri) {
		String fullName = getUriColumn(resolver, uri, OpenableColumns.DISPLAY_NAME);
		return fullName == null ? "" : fullName.substring(fullName.lastIndexOf("."));
	}

	public static long getUriSize(ContentResolver resolver, Uri uri) {
		String size = getUriColumn(resolver, uri, OpenableColumns.SIZE);
		return size == null ? 0 : Long.parseLong(size);
	}

	public static boolean startWebActivity(Context from, String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		if (intent.resolveActivity(from.getPackageManager()) == null)
			return false;
		from.startActivity(intent);
		return true;
	}

	public static String concat(List<String> strings, @NonNull String seprator) {
		String res = "";
		for (String s : strings)
			res += s + seprator;
		return res;
	}

	public static View findParentById(View view, int id) {
		if (view.getId() == id)
			return (View) view;
		ViewParent parent = view.getParent();
		if (parent == null)
			return null;
		return findParentById((View) parent, id);
	}
}