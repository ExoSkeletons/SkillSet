package aviadl40.com.skillSet.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import aviadl40.com.skillSet.AuthUser;
import aviadl40.com.skillSet.Item;
import aviadl40.com.skillSet.NetUtils;
import aviadl40.com.skillSet.NetUtils.NetTask;
import aviadl40.com.skillSet.R;
import aviadl40.com.skillSet.Utils;

import static android.app.Activity.RESULT_OK;

public final class UploadFragment extends Fragment {
	private static final int REQUEST_MEDIA_CHOOSER = 53, REQUEST_USE_CAMERA = 54;

	private Item uploadItem = null;
	private File tmpCameraImageFile = null, tmpCameraVideoFile = null;

	private EditText titleEditor, textItemEditor;
	private Switch visibilitySwitch;
	private NetTask<Object, Void, Void> uploadTask = null;

	private void setUploadItem(@Nullable Item item) {
		if (item != null && !item.isValid())
			setUploadItem(null);

		if (item != uploadItem && uploadItem != null) { // Clean up
			switch (uploadItem.iType) {
				case video:
					if (tmpCameraVideoFile != null) {
						tmpCameraVideoFile.delete();
						tmpCameraVideoFile = null;
					}
					break;
				case image:
					if (tmpCameraImageFile != null) {
						tmpCameraImageFile.delete();
						tmpCameraImageFile = null;
					}
					break;
				case audio:
					Item.AudioItem.stop();
					break;
				case text:
					break;
			}
		}

		uploadItem = item;

		View v = getView();
		if (v != null) {
			LinearLayout itemLayout = (v.findViewById(R.id.upload_item_preview_layout));
			itemLayout.removeAllViewsInLayout();

			titleEditor.setError(null);
			textItemEditor.setError(null);

			boolean showTextEditor = item != null && item.iType == Item.ItemType.text;
			textItemEditor.setVisibility(showTextEditor ? View.VISIBLE : View.GONE);

			if (item != null) {
				if (showTextEditor) {
					textItemEditor.setText(((Item.TextItem) uploadItem).getIData());
					textItemEditor.requestFocus();
				} else {
					View itemContentView = item.getContentView(getLayoutInflater());
					itemLayout.addView(itemContentView);
					itemLayout.requestFocus();
				}
			} else
				v.findViewById(R.id.upload_item_preview_layout).requestFocus();

			v.findViewById(R.id.upload_discard_button).setVisibility(item != null ? View.VISIBLE : View.GONE);
		}
	}

	private void reset() {
		titleEditor.setText("");
		visibilitySwitch.setChecked(false);
		Utils.cancelTask(uploadTask);
		setUploadItem(null);
	}

	private File openCameraActivity(boolean video) {
		final Intent cameraIntent = new Intent().setAction(video ? MediaStore.ACTION_VIDEO_CAPTURE : MediaStore.ACTION_IMAGE_CAPTURE);
		Activity activity = getActivity();
		if (activity != null)
			if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) { // Check for Camera activity availability
				try {
					File resFile = File.createTempFile(
							video ? "VID" : "JPG" + "_",
							null,
							activity.getExternalFilesDir(video ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES)
					);
					resFile.deleteOnExit();
					cameraIntent.putExtra(
							MediaStore.EXTRA_OUTPUT,
							FileProvider.getUriForFile(
									activity,
									Utils.FILE_PROVIDER_PACKAGE,
									resFile
							)
					);
					activity.startActivityForResult(cameraIntent, REQUEST_USE_CAMERA);
					return resFile;
				} catch (IOException ignored) {
				}
			}
		return null;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (resultCode == RESULT_OK) {
			ContentResolver resolver = getContext().getContentResolver();
			switch (requestCode) {
				case REQUEST_MEDIA_CHOOSER:
					if (intent != null && intent.getData() != null) {
						Uri uri = intent.getData();
						String type = "" + resolver.getType(uri);
						if (type.contains("image")) {
							try {
								setUploadItem(new Item.ImageItem(AuthUser.getMe(), Utils.orientedBitmap(resolver, uri)));
							} catch (IOException ignored) {
								Toast.makeText(getContext(), getString(R.string.error_file_read), Toast.LENGTH_SHORT).show();
							}
						} else if (type.contains("video")) {
							setUploadItem(new Item.VideoItem(AuthUser.getMe(), resolver, uri));
						} else if (type.contains("audio")) {
							setUploadItem(new Item.AudioItem(AuthUser.getMe(), resolver, uri));
						} else if (type.contains("pdf")) {
							setUploadItem(new Item.DocumentItem(AuthUser.getMe(), resolver, uri));
						}
						break;
					}
					break;

				case REQUEST_USE_CAMERA:
					if (tmpCameraImageFile != null) {
						try {
							setUploadItem(new Item.ImageItem(
									AuthUser.getMe(),
									Utils.orientedBitmap(tmpCameraImageFile.getAbsolutePath())
							));
						} catch (IOException ignored) {
						}
						tmpCameraImageFile.delete();
						tmpCameraImageFile = null;
					} else if (tmpCameraVideoFile != null) {
						setUploadItem(new Item.VideoItem(
								AuthUser.getMe(),
								resolver,
								FileProvider.getUriForFile(
										getContext(),
										Utils.FILE_PROVIDER_PACKAGE,
										tmpCameraVideoFile
								)
						));
					}
					break;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_upload, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
		titleEditor = view.findViewById(R.id.upload_edit_title);
		textItemEditor = view.findViewById(R.id.upload_text_text_field);
		visibilitySwitch = view.findViewById(R.id.switch_public);

		final View uploadForm = view.findViewById(R.id.upload_form);
		final ProgressBar uploadProgress = view.findViewById(R.id.upload_progress);

		view.findViewById(R.id.upload_choose_document).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(
						Intent.createChooser(
								new Intent()
										.setAction(Intent.ACTION_GET_CONTENT)
										.setTypeAndNormalize("application/pdf")
										.addCategory(Intent.CATEGORY_OPENABLE)
								,
								"Select Media"
						),
						REQUEST_MEDIA_CHOOSER
				);
			}
		});
		view.findViewById(R.id.upload_choose_media).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(
						Intent.createChooser(
								new Intent()
										.setAction(Intent.ACTION_GET_CONTENT)
										.setTypeAndNormalize("image/*,video/*")
								,
								"Select Media"
						),
						REQUEST_MEDIA_CHOOSER
				);
			}
		});
		view.findViewById(R.id.upload_edit_text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setUploadItem(new Item.TextItem(AuthUser.getMe()));
			}
		});
		view.findViewById(R.id.upload_choose_audio).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(
						Intent.createChooser(
								new Intent()
										.setAction(Intent.ACTION_GET_CONTENT)
										.setTypeAndNormalize("audio/*")
								,
								"Select Audio"
						),
						REQUEST_MEDIA_CHOOSER
				);
			}
		});
		view.findViewById(R.id.upload_take_photo).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tmpCameraImageFile = openCameraActivity(false);
			}
		});
		view.findViewById(R.id.upload_take_video).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tmpCameraVideoFile = openCameraActivity(true);
			}
		});

		view.findViewById(R.id.upload_discard_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setUploadItem(null);
			}
		});
		view.findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
			@SuppressLint("StaticFieldLeak")
			@Override
			public void onClick(final View v) {
				if (Utils.isRunning(uploadTask))
					return;

				final View errorView;

				if (TextUtils.isEmpty(titleEditor.getText())) {
					titleEditor.setError(getString(R.string.error_field_required));
					errorView = titleEditor;
				} else if (uploadItem == null) {
					Toast.makeText(getContext(), R.string.upload_select_content, Toast.LENGTH_SHORT).show();
					errorView = view.findViewById(R.id.upload_menu);
				} else if (uploadItem instanceof Item.TextItem && textItemEditor.getText().length() == 0) {
					textItemEditor.setError(getString(R.string.error_field_required));
					errorView = textItemEditor;
				} else if (!uploadItem.isValid()) {
					Toast.makeText(getContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
					errorView = view.findViewById(R.id.upload_item_preview_layout);
				} else {
					// Start upload task
					uploadTask = new NetTask<Object, Void, Void>() {
						@Override
						protected NetUtils.OperationResult<Void> doInBackground(Object... objects) {
							return AuthUser.getMe().uploadItem(uploadItem);
						}

						@Override
						protected void onPreExecute() {
							Item.AudioItem.stop();
							Utils.fade(uploadForm, uploadProgress);
						}

						@Override
						protected void onCancelled() {
							uploadTask = null;
							uploadForm.clearAnimation();
							uploadProgress.clearAnimation();
						}

						@Override
						protected void onResult(Void result) {
							reset();
							Toast.makeText(getContext(), R.string.upload_success, Toast.LENGTH_LONG).show();
						}

						@Override
						protected void onError(NetUtils.OperationError error) {
							error.makeToast(getContext()).show();
						}

						@Override
						protected void onPostExecute() {
							Utils.fade(uploadProgress, uploadForm);
							uploadTask = null;
						}
					};
					uploadTask.execute();
					return;
				}

				errorView.requestFocus();
			}
		});

		textItemEditor.setOnKeyListener(new TextView.OnKeyListener() {
			@SuppressWarnings("RedundantCast")
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (uploadItem instanceof Item.TextItem)
					((Item.TextItem) uploadItem).setIData(textItemEditor.getText().toString());
				return false;
			}
		});

		reset();
	}

	@Override
	public void onDestroy() {
		reset();
		super.onDestroy();
	}
}