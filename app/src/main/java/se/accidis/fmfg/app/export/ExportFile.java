package se.accidis.fmfg.app.export;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

import se.accidis.fmfg.app.model.Document;

/**
 * Encapsulates file operations related to exporting documents to PDF.
 */
public final class ExportFile {
	private static final String DEFAULT_DOC_NAME = "Godsdeklaration";
	private static final String EXPORTED_DOC_DIRECTORY = "exported_doc";
	private static final String FILENAME_ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
	private static final String FILE_PROVIDER_AUTHORITY = "se.accidis.fmfg.fileprovider";
	private static final char SEPARATOR_CHAR = '_';
	private final File mFile;
	private final String mFilename;
	private final Uri mUri;

	private ExportFile(String filename, File file, Uri uri) {
		mFilename = filename;
		mFile = file;
		mUri = uri;
	}

	public static ExportFile fromDocument(Document document, String extension, Context context) throws IOException {
		File baseDir = new File(context.getFilesDir(), EXPORTED_DOC_DIRECTORY);
		if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
			throw new IOException("Could not create the base directory \"" + baseDir.getPath() + "\".");
		}

		String fileName = createUniqueFilename(baseDir, document.getName(), extension);
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

	private static String createUniqueFilename(File baseDir, String documentName, String extension) {
		if (!TextUtils.isEmpty(documentName)) {
			documentName = sanitizeName(documentName.trim());
		} else {
			documentName = DEFAULT_DOC_NAME;
		}

		int i = 1;
		String fileName = documentName + SEPARATOR_CHAR + getTimestamp(i) + extension;
		File candidate = new File(baseDir, fileName);

		// Since the filename is based on currentTimeMillis() we are very unlikely to ever get a collision but just in case...
		while (candidate.exists()) {
			fileName = documentName + SEPARATOR_CHAR + getTimestamp(++i) + extension;
			candidate = new File(baseDir, fileName);
		}

		return fileName;
	}

	private static String getTimestamp(int offset) {
		return String.valueOf(offset * System.currentTimeMillis());
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
