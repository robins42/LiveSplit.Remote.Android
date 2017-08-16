package de.ekelbatzen.livesplitremote.gui;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.ekelbatzen.livesplitremote.R;

public class GuideDialog {
    private static final int PAGES_TOTAL = 8;
    private final Activity context;
    private int page = 0;
    private Button btnPrev;
    private Button btnNext;
    private ImageView image;
    private TextView text;

    public static void askForGuide(Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.guide_first_time_title);
        builder.setMessage(R.string.guide_first_time_msg);
        builder.setNegativeButton(R.string.guide_first_time_cancel, (dialog, which) -> dialog.cancel());
        builder.setPositiveButton(R.string.guide_first_time_agree, (dialog, which) -> new GuideDialog(context));
        builder.create().show();
    }

    public GuideDialog(Activity context) {
        this.context = context;
        page = 0;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(R.layout.dialog_guide);
        builder.setTitle(context.getString(R.string.guide_title, page + 1, PAGES_TOTAL));
        AlertDialog ad = builder.create();
        ad.show();

        image = (ImageView) ad.findViewById(R.id.dialog_guide_image);
        text = (TextView) ad.findViewById(R.id.dialog_guide_text);
        btnPrev = (Button) ad.findViewById(R.id.dialog_guide_button_previous);
        btnNext = (Button) ad.findViewById(R.id.dialog_guide_button_next);

        text.setMovementMethod(LinkMovementMethod.getInstance());

        refreshPage();

        btnPrev.setOnClickListener(v -> {
            page--;
            ad.setTitle(context.getString(R.string.guide_title, page + 1, PAGES_TOTAL));
            refreshPage();
        });
        btnNext.setOnClickListener(v -> {
            page++;
            ad.setTitle(context.getString(R.string.guide_title, page + 1, PAGES_TOTAL));
            refreshPage();
        });
    }

    private void refreshPage() {
        if (page <= 0) btnPrev.setVisibility(View.GONE);
        else btnPrev.setVisibility(View.VISIBLE);

        if (page + 1 >= PAGES_TOTAL) btnNext.setVisibility(View.GONE);
        else btnNext.setVisibility(View.VISIBLE);

        if (page == 0) {
            image.setVisibility(View.GONE);
            setText(R.string.guide_text_page_1);
        } else if (page == 1) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_1);
            setText(R.string.guide_text_page_2);
        } else if (page == 2) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_2);
            setText(R.string.guide_text_page_3);
        } else if (page == 3) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_3);
            setText(R.string.guide_text_page_4);
        } else if (page == 4) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_4);
            setText(R.string.guide_text_page_5);
        } else if (page == 5) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_5);
            setText(R.string.guide_text_page_6);
        } else if (page == 6) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_6);
            setText(R.string.guide_text_page_7);
        } else if (page == 7) {
            image.setVisibility(View.VISIBLE);
            image.setImageResource(R.drawable.guide_7);
            setText(R.string.guide_text_page_8);
        }
    }

    private void setText(int stringId) {
        CharSequence string = context.getText(stringId);
        Spanned html = Html.fromHtml(string.toString());
        text.setText(html);
    }
}
