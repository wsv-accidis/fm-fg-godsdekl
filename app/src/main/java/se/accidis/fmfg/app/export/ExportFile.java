package se.accidis.fmfg.app.export;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import se.accidis.fmfg.app.R;
import se.accidis.fmfg.app.model.Document;

/**
 * Encapsulates file operations related to exporting documents to PDF.
 */
public final class ExportFile {
	private static final String EXPORTED_DOC_DIRECTORY = "exported_doc";
	private static final int EXPORTED_FILE_MAX_AGE_MS = 1000 * 60 * 60 * 24;
	private static final String FILENAME_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
	private static final String FILE_PROVIDER_AUTHORITY = "se.accidis.fmfg.fileprovider";
	private static final char SEPARATOR_CHAR = '_';
	private static final String TAG = ExportFile.class.getSimpleName();
	private final File mFile;
	private final String mFilename;
	private final Uri mUri;

	private ExportFile(String filename, File file, Uri uri) {
		mFilename = filename;
		mFile = file;
		mUri = uri;
	}

	public static void cleanUpOldExports(Context context) {
		try {
			File baseDir = new File(context.getFilesDir(), EXPORTED_DOC_DIRECTORY);
			if (!baseDir.isDirectory()) {
				return;
			}

			long limit = System.currentTimeMillis() - EXPORTED_FILE_MAX_AGE_MS;
			File[] exportFiles = baseDir.listFiles();
			for (File file : exportFiles) {
				long lastModified = file.lastModified();
				if (lastModified < limit) {
					Log.d(TAG, "Deleting old export \"" + file.getName() + "\", last modified " + lastModified + ", limit " + limit + ".");
					if (!file.delete()) {
						Log.e(TAG, "Error deleting export \"" + file.getName() + "\".");
					}
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, "Exception while cleaning up old exports:", ex);
		}
	}

	public static ExportFile fromDocument(Document document, String extension, Context context) throws IOException {
		File baseDir = new File(context.getFilesDir(), EXPORTED_DOC_DIRECTORY);
		if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
			throw new IOException("Could not create the base directory \"" + baseDir.getPath() + "\".");
		}

		String fileName = createUniqueFilename(baseDir, document.getName(), extension, context.getString(R.string.document_export_default_name));
		File file = new File(baseDir, fileName);
		Uri uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
		return new ExportFile(fileName, file, uri);
	}

	public File getFile() {
		return mFile;
	}

	public String getFilename() {
		return mFilename;
	}

	public Uri getUri() {
		return mUri;
	}

	private static String createUniqueFilename(File baseDir, String documentName, String extension, String defaultName) {
		if (!TextUtils.isEmpty(documentName)) {
			documentName = sanitizeName(documentName.trim());
		} else {
			documentName = defaultName;
		}

		String fileName = documentName + extension;
		File candidate = new File(baseDir, fileName);

		// Add a counter to the filename, eventually we'll find one that is unused
		int i = 1;
		while (candidate.exists()) {
			fileName = documentName + SEPARATOR_CHAR + String.valueOf(i++) + extension;
			candidate = new File(baseDir, fileName);
		}

		return fileName;
	}

	private static String sanitizeName(String documentName) {
		StringBuilder buffer = new StringBuilder(documentName);
		for (int i = 0; i < buffer.length(); i++) {
			if (-1 == FILENAME_ALLOWED_CHARS.indexOf(buffer.charAt(i))) {
				buffer.setCharAt(i, SEPARATOR_CHAR);
			}
		}
		return buffer.toString();
	}
}
