package de.ekelbatzen.livesplitremote.view;

import android.app.Activity;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import de.ekelbatzen.livesplitremote.R;

public class GuideDialog {
    private static final int PAGES_TOTAL = 8;
    private final Activity context;
    private final AlertDialog dialog;
    private int currentPageIndex;
    private Button buttonPrevious;
    private Button buttonNext;
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
        currentPageIndex = 0;
        dialog = buildDialog();
        dialog.show();
        findElements();
        text.setMovementMethod(LinkMovementMethod.getInstance());
        refreshPage();
        setClickListeners();
    }

    private void findElements() {
        image = dialog.findViewById(R.id.dialog_guide_image);
        text = dialog.findViewById(R.id.dialog_guide_text);
        buttonPrevious = dialog.findViewById(R.id.dialog_guide_button_previous);
        buttonNext = dialog.findViewById(R.id.dialog_guide_button_next);
    }

    private void setClickListeners() {
        buttonPrevious.setOnClickListener(v -> changePageIndex(-1));
        buttonNext.setOnClickListener(v -> changePageIndex(1));
    }

    private void changePageIndex(int offset) {
        currentPageIndex += offset;
        dialog.setTitle(context.getString(R.string.guide_title, currentPageIndex + 1, PAGES_TOTAL));
        refreshPage();
    }

    private AlertDialog buildDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(R.layout.dialog_guide);
        builder.setTitle(context.getString(R.string.guide_title, currentPageIndex + 1, PAGES_TOTAL));
        return builder.create();
    }

    private void refreshPage() {
        setElementVisibleOnCondition(buttonPrevious, currentPageIndex > 0);
        setElementVisibleOnCondition(buttonNext, currentPageIndex + 1 < PAGES_TOTAL);
        setElementVisibleOnCondition(image, currentPageIndex != 0);

        if (currentPageIndex == 0) {
            setText(R.string.guide_text_page_1);
        } else if (currentPageIndex == 1) {
            image.setImageResource(R.drawable.guide_1);
            setText(R.string.guide_text_page_2);
        } else if (currentPageIndex == 2) {
            image.setImageResource(R.drawable.guide_2);
            setText(R.string.guide_text_page_3);
        } else if (currentPageIndex == 3) {
            image.setImageResource(R.drawable.guide_3);
            setText(R.string.guide_text_page_4);
        } else if (currentPageIndex == 4) {
            image.setImageResource(R.drawable.guide_4);
            setText(R.string.guide_text_page_5);
        } else if (currentPageIndex == 5) {
            image.setImageResource(R.drawable.guide_5);
            setText(R.string.guide_text_page_6);
        } else if (currentPageIndex == 6) {
            image.setImageResource(R.drawable.guide_6);
            setText(R.string.guide_text_page_7);
        } else if (currentPageIndex == 7) {
            image.setImageResource(R.drawable.guide_7);
            setText(R.string.guide_text_page_8);
        }
    }

    private void setElementVisibleOnCondition(View button, boolean isVisible) {
        button.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void setText(int stringId) {
        CharSequence string = context.getText(stringId);
        Spanned html = Html.fromHtml(string.toString());
        text.setText(html);
    }
}
