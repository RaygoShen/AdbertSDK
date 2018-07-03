package com.adbert.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OpenFileUtil {

	public static String getRealPathFromURI(Context context, Uri contentURI) {
		String path = "";
		try {
			String result;
			Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
			if (cursor == null) { // Source is Dropbox or other similar local file path
				result = contentURI.getPath();
			} else {
				cursor.moveToFirst();
				int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				result = cursor.getString(idx);
				cursor.close();
			}
			path = result;
		}
		catch (Exception e) {
			SDKUtil.logException(e);
			path = getPath(context, contentURI);
		}
		if (path.isEmpty())
			return contentURI.getPath();
		else return path;
	}


	public static File createImageFile(Context context) throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = new File(context.getExternalCacheDir(), "browser-cache");
		storageDir.mkdirs();
		File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);
		return imageFile;
	}

	public static String getPath(Context context, Uri uri) {
		InputStream inStream;
		try {
			inStream = context.getContentResolver().openInputStream(uri);
			Bitmap bitmap = BitmapFactory.decodeStream(inStream);
			if (bitmap != null) {
				String filename = System.currentTimeMillis() + ".jpg";
				String path = context.getCacheDir().getAbsolutePath() + File.separator + filename;
				FileOutputStream fos = null;
				fos = new FileOutputStream(path);
				if (null != fos) {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
					fos.flush();
					fos.close();
				}
				File file = new File(context.getCacheDir(), filename);
				String realPath = file.getAbsolutePath();
				if (!bitmap.isRecycled())
					bitmap.recycle();
				return realPath;
			}
		}
		catch (FileNotFoundException e1) {
			SDKUtil.logException(e1);
		}
		catch (Exception e) {
			SDKUtil.logException(e);
		}
		return "";
	}

	public static int getHeight(int width, int scaleW, int scaleH) {
		return (int) (((float) width / (float) scaleW) * (float) scaleH);
	}

	static int limitWidth = 720;

	public static String encodeTobase64(Context context, Uri uri, String uriPath) {
		try {
			Bitmap immagex = null;
			if (Build.VERSION.SDK_INT > 19) {
				immagex = manageImageFromUri(context, uri);
				if (immagex == null)
					immagex = BitmapFactory.decodeFile(uriPath);
			} else {
				immagex = BitmapFactory.decodeFile(uriPath);
				if (immagex == null)
					immagex = manageImageFromUri(context, uri);
			}
			if (immagex == null)
				immagex = getBitmap(context, uri);
			if (immagex != null) {
				if (immagex.getHeight() > limitWidth) {
					immagex = Bitmap.createScaledBitmap(immagex, limitWidth, getHeight(limitWidth, immagex.getWidth(), immagex.getHeight()), false);
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byte[] b = baos.toByteArray();
				String imageEncoded = Base64.encodeToString(b, Base64.NO_WRAP);
				if (immagex != null && !immagex.isRecycled()) {
					immagex.recycle();
					immagex = null;
				}
				return imageEncoded;
			}
		}
		catch (OutOfMemoryError e) {
		}
		catch (Exception e) {
			SDKUtil.logException(e);
		}
		return "";
	}

	public static Bitmap manageImageFromUri(Context context, Uri imageUri) {
		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
		}
		catch (Exception e) {
			SDKUtil.logException(e);
		}
		return bitmap;
	}

	public static Bitmap getBitmap(Context context, Uri uri) {
		Bitmap bitmap = null;
		try {
			InputStream inStream = context.getContentResolver().openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(inStream);
		}
		catch (Exception e) {
			SDKUtil.logException(e);
		}
		return bitmap;
	}

	public void savePic(Bitmap b, String filePath) {
		boolean isJPG = true;
		if (filePath.contains(".")) {
			if (filePath.substring(filePath.lastIndexOf(".")).toLowerCase(Locale.ENGLISH).contains("jpg")) {
				isJPG = true;
			} else if (filePath.substring(filePath.lastIndexOf(".")).toLowerCase(Locale.ENGLISH).contains("png")) {
				isJPG = false;
			}
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			if (null != fos) {
				if (isJPG)
					b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
				else b.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			}
		}
		catch (FileNotFoundException e) {
			SDKUtil.logException(e);
		} catch (IOException e) {
			SDKUtil.logException(e);
		}
	}
}
