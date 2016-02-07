package se.accidis.fmfg.app.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;

import com.pdfjet.Box;
import com.pdfjet.Color;
import com.pdfjet.Letter;
import com.pdfjet.Line;
import com.pdfjet.PDF;
import com.pdfjet.Page;
import com.pdfjet.Point;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Wilhelm Svenselius on 2016-02-07.
 */
public class PdfTest {
	public static void Example(Activity activity) throws Exception {
		int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
				activity,
				new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
				1
			);

			return;
		}

		File file = new File(Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_DOWNLOADS), "Example_01.pdf");

		PDF pdf = new PDF(new BufferedOutputStream(new FileOutputStream(file)));

		Page page = new Page(pdf, Letter.PORTRAIT);

		Box flag = new Box();
		flag.setLocation(100f, 100f);
		flag.setSize(190f, 100f);
		flag.setColor(Color.white);
		flag.drawOn(page);

		float[] xy = new float[]{0f, 0f};
		float sw = 7.69f;   // stripe width
		Line stripe = new Line(0.0f, sw / 2, 190.0f, sw / 2);
		stripe.setWidth(sw);
		stripe.setColor(Color.oldgloryred);
		for (int row = 0; row < 7; row++) {
			stripe.placeIn(flag, 0.0f, row * 2 * sw);
			xy = stripe.drawOn(page);
		}
/*
		Box box = new Box();
        box.setLocation(xy[0], xy[1]);

        box.setSize(20f, 20f);
        box.drawOn(page);
*/
		Box union = new Box();
		union.setSize(76.0f, 53.85f);
		union.setColor(Color.oldgloryblue);
		union.setFillShape(true);
		union.placeIn(flag, 0f, 0f);
		union.drawOn(page);

		float h_si = 12.6f; // horizontal star interval
		float v_si = 10.8f; // vertical star interval
		Point star = new Point(h_si / 2, v_si / 2);
		star.setShape(Point.STAR);
		star.setRadius(3.0f);
		star.setColor(Color.white);
		star.setFillShape(true);

		for (int row = 0; row < 6; row++) {
			for (int col = 0; col < 5; col++) {
				star.placeIn(union, row * h_si, col * v_si);
				xy = star.drawOn(page);
			}
		}
/*
		Box box = new Box();
        box.setLocation(xy[0], xy[1]);
        box.setSize(20f, 20f);
        box.drawOn(page);
*/
		star.setLocation(h_si, v_si);
		for (int row = 0; row < 5; row++) {
			for (int col = 0; col < 4; col++) {
				star.placeIn(union, row * h_si, col * v_si);
				xy = star.drawOn(page);
			}
		}
/*
        Box box = new Box();
        box.setLocation(xy[0], xy[1]);
        box.setSize(20f, 20f);
        box.drawOn(page);
*/
		pdf.close();
	}
}
